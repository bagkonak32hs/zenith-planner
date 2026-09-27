package com.selcuk.zenithplanner.sync

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.selcuk.zenithplanner.R
import com.selcuk.zenithplanner.data.DailyPlanEntity
import com.selcuk.zenithplanner.data.FinanceEntryEntity
import com.selcuk.zenithplanner.data.GoalEntity
import com.selcuk.zenithplanner.data.HabitEntity
import com.selcuk.zenithplanner.data.HabitLogEntity
import com.selcuk.zenithplanner.data.NoteEntity
import com.selcuk.zenithplanner.data.PlannerDatabase
import com.selcuk.zenithplanner.data.QuoteEntity
import com.selcuk.zenithplanner.data.ScheduleEntryEntity
import com.selcuk.zenithplanner.data.TaskEntity
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.room.withTransaction
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

private const val FIRESTORE_OPERATION_TIMEOUT_MS = 15_000L
private const val FIRESTORE_DATABASE_ID = "plannerdb"
private const val TAG = "FirebaseSyncManager"

data class SyncUiState(
    val isConfigured: Boolean = false,
    val isGoogleSignInConfigured: Boolean = false,
    val isSignedIn: Boolean = false,
    val isBusy: Boolean = false,
    val userEmail: String? = null,
    val statusRes: Int = R.string.firebase_not_configured
)

class FirebaseSyncManager(
    private val context: Context,
    private val database: PlannerDatabase
) {
    private val credentialManager = CredentialManager.create(context)
    private val webClientId = context.getString(R.string.default_web_client_id)
    private val _state = MutableStateFlow(SyncUiState())
    val state: StateFlow<SyncUiState> = _state.asStateFlow()

    init {
        refreshState()
    }

    suspend fun signInWithGoogle(activity: Activity) {
        if (!isFirebaseConfigured()) {
            refreshState(R.string.firebase_not_configured)
            return
        }
        if (!isGoogleSignInConfigured()) {
            refreshState(R.string.google_sign_in_not_configured)
            return
        }
        _state.value = _state.value.copy(isBusy = true)
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
                refreshState(R.string.sync_ready)
            } else {
                refreshState(R.string.sync_failed)
            }
        } catch (exception: NoCredentialException) {
            Log.w(TAG, "No Google credentials available on the device.", exception)
            refreshState(R.string.google_account_required)
        } catch (exception: GetCredentialCancellationException) {
            Log.w(TAG, "Google sign-in was cancelled.", exception)
            refreshState(R.string.google_sign_in_cancelled)
        } catch (exception: GetCredentialProviderConfigurationException) {
            Log.w(TAG, "Credential provider is not configured correctly.", exception)
            refreshState(R.string.google_credential_provider_unavailable)
        } catch (exception: GetCredentialUnsupportedException) {
            Log.w(TAG, "Credential provider is not supported on this device.", exception)
            refreshState(R.string.google_credential_provider_unavailable)
        } catch (exception: GetCredentialException) {
            Log.w(TAG, "Google credential request failed.", exception)
            refreshState(R.string.google_oauth_registration_failed)
        } catch (exception: Exception) {
            Log.e(TAG, "Unexpected Google sign-in failure.", exception)
            refreshState(R.string.sync_failed)
        }
    }

    suspend fun signOut() {
        if (isFirebaseConfigured()) {
            FirebaseAuth.getInstance().signOut()
        }
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: ClearCredentialException) {
            // Firebase sign-out already happened; credential cleanup can fail on devices without Google services.
        }
        refreshState(R.string.sync_signed_out)
    }

    suspend fun syncNow() {
        if (!isFirebaseConfigured()) {
            refreshState(R.string.firebase_not_configured)
            return
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            refreshState(R.string.sync_signed_out)
            return
        }
        _state.value = _state.value.copy(isBusy = true)
        try {
            val payload = buildSnapshot(user.uid)
            firestore()
                .collection("users")
                .document(user.uid)
                .collection("plannerSnapshots")
                .document("current")
                .set(payload)
                .awaitWithFirestoreTimeout()
            refreshState(R.string.sync_complete)
        } catch (exception: Exception) {
            refreshState(firestoreFailureStatus(exception, R.string.sync_failed))
        }
    }

    suspend fun restoreFromCloud() {
        if (!isFirebaseConfigured()) {
            refreshState(R.string.firebase_not_configured)
            return
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            refreshState(R.string.sync_signed_out)
            return
        }
        _state.value = _state.value.copy(isBusy = true)
        try {
            val snapshot = firestore()
                .collection("users")
                .document(user.uid)
                .collection("plannerSnapshots")
                .document("current")
                .get()
                .awaitWithFirestoreTimeout()
                .data

            if (snapshot == null) {
                refreshState(R.string.restore_failed)
                return
            }

            restoreSnapshot(snapshot)
            refreshState(R.string.restore_complete)
        } catch (exception: Exception) {
            refreshState(firestoreFailureStatus(exception, R.string.restore_failed))
        }
    }

    private fun isFirebaseConfigured(): Boolean =
        FirebaseApp.getApps(context).isNotEmpty()

    private fun isGoogleSignInConfigured(): Boolean =
        isFirebaseConfigured() && webClientId.isNotBlank()

    private fun refreshState(statusRes: Int? = null) {
        val configured = isFirebaseConfigured()
        val googleConfigured = isGoogleSignInConfigured()
        val user = if (configured) FirebaseAuth.getInstance().currentUser else null
        _state.value = SyncUiState(
            isConfigured = configured,
            isGoogleSignInConfigured = googleConfigured,
            isSignedIn = user != null,
            isBusy = false,
            userEmail = user?.email,
            statusRes = statusRes ?: when {
                !configured -> R.string.firebase_not_configured
                !googleConfigured -> R.string.google_sign_in_not_configured
                user != null -> R.string.sync_ready
                else -> R.string.sync_signed_out
            }
        )
    }

    private fun firestoreFailureStatus(exception: Exception, fallback: Int): Int =
        when {
            exception is TimeoutCancellationException -> R.string.firestore_unavailable
            exception is FirebaseFirestoreException &&
                exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED -> R.string.firestore_permission_denied
            else -> fallback
        }

    private fun firestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance(FIRESTORE_DATABASE_ID)

    private suspend fun buildSnapshot(uid: String): Map<String, Any?> =
        mapOf(
            "ownerUid" to uid,
            "snapshotId" to UUID.randomUUID().toString(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "dailyPlans" to database.dailyPlanDao().getAllPlans().map { plan ->
                mapOf("date" to plan.date, "notes" to plan.notes, "reflection" to plan.reflection)
            },
            "tasks" to database.taskDao().getAllTasks().map { task ->
                mapOf(
                    "id" to task.id,
                    "date" to task.date,
                    "title" to task.title,
                    "isDone" to task.isDone,
                    "sortOrder" to task.sortOrder
                )
            },
            "schedule" to database.scheduleDao().getAllSchedule().map { entry ->
                mapOf("id" to entry.id, "date" to entry.date, "hour" to entry.hour, "title" to entry.title)
            },
            "habits" to database.habitDao().getAllHabits().map { habit ->
                mapOf(
                    "id" to habit.id,
                    "name" to habit.name,
                    "category" to habit.category,
                    "icon" to habit.icon,
                    "sortOrder" to habit.sortOrder
                )
            },
            "habitLogs" to database.habitDao().getAllLogs().map { log ->
                mapOf("habitId" to log.habitId, "date" to log.date, "isDone" to log.isDone)
            },
            "goals" to database.goalDao().getAllGoals().map { goal ->
                mapOf(
                    "id" to goal.id,
                    "title" to goal.title,
                    "category" to goal.category,
                    "horizon" to goal.horizon,
                    "progress" to goal.progress,
                    "notes" to goal.notes
                )
            },
            "quotes" to database.quoteDao().getAllQuotes().map { quote ->
                mapOf("id" to quote.id, "text" to quote.text, "author" to quote.author, "isFavorite" to quote.isFavorite)
            },
            "notes" to database.noteDao().getAllNotes().map { note ->
                mapOf(
                    "id" to note.id,
                    "title" to note.title,
                    "body" to note.body,
                    "isPinned" to note.isPinned,
                    "updatedAt" to note.updatedAt
                )
            },
            "financeEntries" to database.financeDao().getAllEntries().map { entry ->
                mapOf(
                    "id" to entry.id,
                    "title" to entry.title,
                    "category" to entry.category,
                    "amount" to entry.amount,
                    "type" to entry.type,
                    "date" to entry.date
                )
            }
        )

    private suspend fun restoreSnapshot(snapshot: Map<String, Any?>) {
        val dailyPlans = mapList(snapshot, "dailyPlans").map { plan ->
            DailyPlanEntity(
                date = plan["date"].asString(),
                notes = plan["notes"].asString(),
                reflection = plan["reflection"].asString()
            )
        }.filter { it.date.isNotBlank() }

        val tasks = mapList(snapshot, "tasks").map { task ->
            TaskEntity(
                id = task["id"].asLong(),
                date = task["date"].asString(),
                title = task["title"].asString(),
                isDone = task["isDone"].asBoolean(),
                sortOrder = task["sortOrder"].asInt()
            )
        }.filter { it.date.isNotBlank() }

        val schedule = mapList(snapshot, "schedule").map { entry ->
            ScheduleEntryEntity(
                id = entry["id"].asLong(),
                date = entry["date"].asString(),
                hour = entry["hour"].asInt(),
                title = entry["title"].asString()
            )
        }.filter { it.date.isNotBlank() }

        val habits = mapList(snapshot, "habits").map { habit ->
            HabitEntity(
                id = habit["id"].asLong(),
                name = habit["name"].asString(),
                category = habit["category"].asString(),
                icon = habit["icon"].asString(),
                sortOrder = habit["sortOrder"].asInt()
            )
        }.filter { it.name.isNotBlank() }

        val habitLogs = mapList(snapshot, "habitLogs").map { log ->
            HabitLogEntity(
                habitId = log["habitId"].asLong(),
                date = log["date"].asString(),
                isDone = log["isDone"].asBoolean()
            )
        }.filter { it.habitId > 0 && it.date.isNotBlank() }

        val goals = mapList(snapshot, "goals").map { goal ->
            GoalEntity(
                id = goal["id"].asLong(),
                title = goal["title"].asString(),
                category = goal["category"].asString(),
                horizon = goal["horizon"].asString(),
                progress = goal["progress"].asFloat().coerceIn(0f, 1f),
                notes = goal["notes"].asString()
            )
        }.filter { it.title.isNotBlank() }

        val quotes = mapList(snapshot, "quotes").map { quote ->
            QuoteEntity(
                id = quote["id"].asLong(),
                text = quote["text"].asString(),
                author = quote["author"].asString(),
                isFavorite = quote["isFavorite"].asBoolean(true)
            )
        }.filter { it.text.isNotBlank() }

        val notes = mapList(snapshot, "notes").map { note ->
            NoteEntity(
                id = note["id"].asLong(),
                title = note["title"].asString(),
                body = note["body"].asString(),
                isPinned = note["isPinned"].asBoolean(),
                updatedAt = note["updatedAt"].asLong(System.currentTimeMillis())
            )
        }.filter { it.title.isNotBlank() || it.body.isNotBlank() }

        val financeEntries = mapList(snapshot, "financeEntries").map { entry ->
            FinanceEntryEntity(
                id = entry["id"].asLong(),
                title = entry["title"].asString(),
                category = entry["category"].asString(),
                amount = entry["amount"].asDouble(),
                type = entry["type"].asString(),
                date = entry["date"].asString()
            )
        }.filter { it.date.isNotBlank() && it.amount > 0.0 }

        database.withTransaction {
            database.habitDao().clearLogs()
            database.taskDao().clear()
            database.scheduleDao().clear()
            database.dailyPlanDao().clear()
            database.habitDao().clearHabits()
            database.goalDao().clear()
            database.quoteDao().clear()
            database.noteDao().clear()
            database.financeDao().clear()

            if (dailyPlans.isNotEmpty()) database.dailyPlanDao().upsertAll(dailyPlans)
            if (tasks.isNotEmpty()) database.taskDao().insertAll(tasks)
            if (schedule.isNotEmpty()) database.scheduleDao().insertAll(schedule)
            if (habits.isNotEmpty()) database.habitDao().insertHabits(habits)
            if (habitLogs.isNotEmpty()) database.habitDao().upsertLogs(habitLogs)
            if (goals.isNotEmpty()) database.goalDao().insertAll(goals)
            if (quotes.isNotEmpty()) database.quoteDao().insertAll(quotes)
            if (notes.isNotEmpty()) database.noteDao().insertAll(notes)
            if (financeEntries.isNotEmpty()) database.financeDao().insertAll(financeEntries)
        }
    }

    private fun mapList(source: Map<String, Any?>, key: String): List<Map<String, Any?>> =
        (source[key] as? List<*>)
            ?.mapNotNull { item ->
                (item as? Map<*, *>)?.mapKeys { entry -> entry.key.toString() }
            }
            ?: emptyList()

    private fun Any?.asString(default: String = ""): String =
        this as? String ?: default

    private fun Any?.asLong(default: Long = 0L): Long = when (this) {
        is Number -> toLong()
        is String -> toLongOrNull() ?: default
        else -> default
    }

    private fun Any?.asInt(default: Int = 0): Int = when (this) {
        is Number -> toInt()
        is String -> toIntOrNull() ?: default
        else -> default
    }

    private fun Any?.asFloat(default: Float = 0f): Float = when (this) {
        is Number -> toFloat()
        is String -> toFloatOrNull() ?: default
        else -> default
    }

    private fun Any?.asDouble(default: Double = 0.0): Double = when (this) {
        is Number -> toDouble()
        is String -> toDoubleOrNull() ?: default
        else -> default
    }

    private fun Any?.asBoolean(default: Boolean = false): Boolean = when (this) {
        is Boolean -> this
        is Number -> toInt() != 0
        is String -> toBooleanStrictOrNull() ?: default
        else -> default
    }
}

private suspend fun <T> Task<T>.awaitWithFirestoreTimeout(): T =
    withTimeout(FIRESTORE_OPERATION_TIMEOUT_MS) {
        await()
    }

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}


