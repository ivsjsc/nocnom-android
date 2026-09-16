package vn.ivsjsc.nocnom.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import vn.ivsjsc.nocnom.data.auth.AuthState

@Composable
fun AuthScreen(
    state: AuthState,
    onSignInEmail: (String, String) -> Unit,
    onCreateAccount: (String, String) -> Unit,
    onGoogleToken: (String) -> Unit,
    onResetPassword: (String) -> Unit,
    onClearMessage: () -> Unit,
) {
    val context = LocalContext.current
    var createMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val webClientId = remember {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (id != 0) context.getString(id) else ""
    }
    val googleClient = remember(webClientId) {
        if (webClientId.isBlank()) {
            null
        } else {
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build(),
            )
        }
    }
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
        }.onSuccess { account ->
            val token = account.idToken
            if (token.isNullOrBlank()) {
                localError = "Google không trả về ID token. Cần kiểm tra SHA-1/SHA-256 trong Firebase."
            } else {
                localError = null
                onGoogleToken(token)
            }
        }.onFailure {
            localError = "Không thể đăng nhập Google. Vui lòng thử lại."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "nOcnOm",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Kế hoạch ăn uống · Calories · Macro",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = if (createMode) "Tạo tài khoản" else "Đăng nhập",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )

                if (!state.firebaseAvailable) {
                    Text(
                        text = "Bản Android này chưa có cấu hình Firebase hợp lệ. Không sử dụng dữ liệu demo thay cho tài khoản thật.",
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it.trim()
                        localError = null
                        onClearMessage()
                    },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = state.firebaseAvailable && !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localError = null
                        onClearMessage()
                    },
                    label = { Text("Mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = state.firebaseAvailable && !state.isLoading,
                    supportingText = if (createMode) {
                        { Text("Tối thiểu 6 ký tự") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                )

                val emailValid = email.contains('@') && email.substringAfter('@').contains('.')
                val passwordValid = password.length >= 6
                Button(
                    onClick = {
                        localError = null
                        if (createMode) onCreateAccount(email, password)
                        else onSignInEmail(email, password)
                    },
                    enabled = state.firebaseAvailable && !state.isLoading && emailValid && passwordValid,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(if (createMode) "Tạo tài khoản" else "Đăng nhập")
                    }
                }

                if (!createMode) {
                    TextButton(
                        onClick = { onResetPassword(email) },
                        enabled = state.firebaseAvailable && !state.isLoading && emailValid,
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("Quên mật khẩu?")
                    }
                }

                HorizontalDivider()
                OutlinedButton(
                    onClick = {
                        localError = null
                        googleClient?.signOut()?.addOnCompleteListener {
                            googleLauncher.launch(googleClient.signInIntent)
                        }
                    },
                    enabled = state.firebaseAvailable && !state.isLoading && googleClient != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Tiếp tục với Google")
                }
                if (googleClient == null && state.firebaseAvailable) {
                    Text(
                        text = "Google Sign-In chưa sẵn sàng. Cần google-services.json có Web OAuth client.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                val message = localError ?: state.errorMessage
                if (!message.isNullOrBlank()) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (!state.infoMessage.isNullOrBlank()) {
                    Text(
                        text = state.infoMessage,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                TextButton(
                    onClick = {
                        createMode = !createMode
                        password = ""
                        localError = null
                        onClearMessage()
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        if (createMode) "Đã có tài khoản? Đăng nhập"
                        else "Chưa có tài khoản? Tạo tài khoản",
                    )
                }
            }
        }
    }
}
