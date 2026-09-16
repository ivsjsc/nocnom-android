package vn.ivsjsc.nocnom.data

object FirestoreContract {
    const val USER_STATE_SCHEMA_VERSION = 2

    fun profile(uid: String) = "users/$uid/profile/main"
    fun timetable(uid: String) = "users/$uid/state/timetable"
    fun dishes(uid: String) = "users/$uid/state/dishes"
    fun categories(uid: String) = "users/$uid/state/categories"
    fun logs(uid: String) = "users/$uid/state/logs"
    fun meta(uid: String) = "users/$uid/state/meta"

    // Legacy path is documented for compatibility only. Android does not write to it.
    fun legacyAppState(uid: String) = "users/$uid/data/appState"
}
