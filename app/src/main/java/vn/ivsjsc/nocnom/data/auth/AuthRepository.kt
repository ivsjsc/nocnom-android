package vn.ivsjsc.nocnom.data.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import vn.ivsjsc.nocnom.data.FirestoreContract

/** Minimal auth model kept separate from Firebase SDK types so UI stays testable. */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)

data class AuthState(
    val firebaseAvailable: Boolean,
    val user: AuthUser? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

interface AuthRepository {
    val state: StateFlow<AuthState>
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun createAccount(email: String, password: String)
    suspend fun signInWithGoogleIdToken(idToken: String)
    suspend fun sendPasswordReset(email: String)
    suspend fun deleteAccount()
    fun signOut()
    fun clearMessage()
}

@Singleton
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext context: Context,
) : AuthRepository {
    private val auth: FirebaseAuth? = if (FirebaseApp.getApps(context).isNotEmpty()) {
        FirebaseAuth.getInstance()
    } else {
        null
    }

    private val mutableState = MutableStateFlow(
        AuthState(
            firebaseAvailable = auth != null,
            user = auth?.currentUser?.let { user ->
                AuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                )
            },
        ),
    )
    override val state: StateFlow<AuthState> = mutableState

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            mutableState.update {
                it.copy(
                    user = user?.let { current ->
                        AuthUser(
                            uid = current.uid,
                            email = current.email,
                            displayName = current.displayName,
                            photoUrl = current.photoUrl?.toString(),
                        )
                    },
                    isLoading = false,
                )
            }
        }
    }

    override suspend fun signInWithEmail(email: String, password: String) = execute {
        signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun createAccount(email: String, password: String) = execute {
        createUserWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun signInWithGoogleIdToken(idToken: String) = execute {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        signInWithCredential(credential).await()
    }

    override suspend fun sendPasswordReset(email: String) {
        val instance = auth ?: return unavailable()
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) {
            mutableState.update { it.copy(errorMessage = "Nhập email để nhận liên kết đặt lại mật khẩu.") }
            return
        }
        mutableState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        runCatching { instance.sendPasswordResetEmail(normalizedEmail).await() }
            .onSuccess {
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Đã gửi liên kết đặt lại mật khẩu nếu email tồn tại trong hệ thống.",
                    )
                }
            }
            .onFailure { throwable ->
                mutableState.update {
                    it.copy(isLoading = false, errorMessage = userMessage(throwable))
                }
            }
    }

    override suspend fun deleteAccount() {
        val instance = auth ?: return unavailable()
        val user = instance.currentUser ?: run {
            mutableState.update { it.copy(errorMessage = "Không tìm thấy phiên đăng nhập để xóa tài khoản.") }
            return
        }

        mutableState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        runCatching {
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val paths = listOf(
                FirestoreContract.profile(uid),
                FirestoreContract.timetable(uid),
                FirestoreContract.dishes(uid),
                FirestoreContract.categories(uid),
                FirestoreContract.logs(uid),
                FirestoreContract.meta(uid),
                FirestoreContract.legacyAppState(uid),
            )
            val batch = db.batch()
            paths.forEach { path -> batch.delete(db.document(path)) }
            batch.commit().await()
            user.delete().await()
        }.onSuccess {
            mutableState.value = AuthState(firebaseAvailable = true, user = null)
        }.onFailure { throwable ->
            mutableState.update {
                it.copy(isLoading = false, errorMessage = userMessage(throwable))
            }
        }
    }

    override fun signOut() {
        auth?.signOut()
        mutableState.update { it.copy(user = null, errorMessage = null, infoMessage = null) }
    }

    override fun clearMessage() {
        mutableState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private suspend fun execute(block: suspend FirebaseAuth.() -> Unit) {
        val instance = auth ?: return unavailable()
        mutableState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        runCatching { block(instance) }
            .onSuccess {
                val user = instance.currentUser
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        user = user?.let { current ->
                            AuthUser(
                                uid = current.uid,
                                email = current.email,
                                displayName = current.displayName,
                                photoUrl = current.photoUrl?.toString(),
                            )
                        },
                    )
                }
            }
            .onFailure { throwable ->
                mutableState.update {
                    it.copy(isLoading = false, errorMessage = userMessage(throwable))
                }
            }
    }

    private fun unavailable() {
        mutableState.update {
            it.copy(
                firebaseAvailable = false,
                isLoading = false,
                errorMessage = "Firebase chưa được cấu hình cho bản Android này.",
            )
        }
    }

    private fun userMessage(throwable: Throwable): String {
        val raw = throwable.localizedMessage.orEmpty()
        return when {
            raw.contains("recent", ignoreCase = true) || raw.contains("CREDENTIAL_TOO_OLD", ignoreCase = true) ->
                "Phiên đăng nhập đã quá cũ. Hãy đăng xuất, đăng nhập lại rồi thực hiện xóa tài khoản ngay."
            raw.contains("password", ignoreCase = true) && raw.contains("invalid", ignoreCase = true) ->
                "Email hoặc mật khẩu không đúng."
            raw.contains("email", ignoreCase = true) && raw.contains("already", ignoreCase = true) ->
                "Email này đã có tài khoản. Hãy đăng nhập hoặc đặt lại mật khẩu."
            raw.contains("network", ignoreCase = true) ->
                "Không thể kết nối. Kiểm tra mạng và thử lại."
            raw.isNotBlank() -> raw
            else -> "Không thể xác thực. Vui lòng thử lại."
        }
    }
}
