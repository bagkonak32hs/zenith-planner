package com.selcuk.zenithplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import android.app.Activity
import com.selcuk.zenithplanner.data.DailyPlanEntity
import com.selcuk.zenithplanner.subscription.SubscriptionManager
import com.selcuk.zenithplanner.data.FinanceEntryEntity
import com.selcuk.zenithplanner.data.GoalEntity
import com.selcuk.zenithplanner.data.HabitEntity
import com.selcuk.zenithplanner.data.HabitLogEntity
import com.selcuk.zenithplanner.data.NoteEntity
import com.selcuk.zenithplanner.data.PlannerRepository
import com.selcuk.zenithplanner.data.QuoteEntity
import com.selcuk.zenithplanner.data.ScheduleEntryEntity
import com.selcuk.zenithplanner.data.TaskEntity
import com.selcuk.zenithplanner.sync.FirebaseSyncManager
import com.selcuk.zenithplanner.sync.SyncUiState
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModel(
    private val repository: PlannerRepository,
    private val firebaseSyncManager: FirebaseSyncManager,
    private val subscriptionManager: SubscriptionManager
) : ViewModel() {

    val isPro: StateFlow<Boolean> = subscriptionManager.isPro
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    private val selectedDateKey = _selectedDate.map { it.toString() }.distinctUntilChanged()

    val dailyPlan: StateFlow<DailyPlanEntity?> = selectedDateKey
        .flatMapLatest(repository::observePlan)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val tasks: StateFlow<List<TaskEntity>> = selectedDateKey
        .flatMapLatest(repository::observeTasks)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val schedule: StateFlow<List<ScheduleEntryEntity>> = selectedDateKey
        .flatMapLatest(repository::observeSchedule)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val habitLogs: StateFlow<List<HabitLogEntity>> = selectedDateKey
        .flatMapLatest(repository::observeHabitLogs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = repository.observeHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val goals: StateFlow<List<GoalEntity>> = repository.observeGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val quotes: StateFlow<List<QuoteEntity>> = repository.observeQuotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val financeEntries: StateFlow<List<FinanceEntryEntity>> = repository.observeFinanceEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeDates: StateFlow<Set<String>> = repository.observeActiveDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val habitStreaks: StateFlow<Map<Long, Int>> = repository.observeStreaks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val syncState: StateFlow<SyncUiState> = firebaseSyncManager.state

    init {
        viewModelScope.launch {
            repository.seedDefaults()
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        viewModelScope.launch {
            repository.ensureDayScaffold(date.toString())
        }
    }

    fun updateTaskTitle(id: Long, title: String) = viewModelScope.launch {
        repository.updateTaskTitle(id, title)
    }

    fun updateTaskDone(id: Long, isDone: Boolean) = viewModelScope.launch {
        repository.updateTaskDone(id, isDone)
    }

    fun updateScheduleTitle(id: Long, title: String) = viewModelScope.launch {
        repository.updateScheduleTitle(id, title)
    }

    fun updateNotes(notes: String) = viewModelScope.launch {
        repository.updateNotes(_selectedDate.value.toString(), notes)
    }

    fun updateReflection(reflection: String) = viewModelScope.launch {
        repository.updateReflection(_selectedDate.value.toString(), reflection)
    }

    fun setHabitDone(habitId: Long, isDone: Boolean) = viewModelScope.launch {
        repository.setHabitDone(habitId, _selectedDate.value.toString(), isDone)
    }

    fun updateGoalProgress(id: Long, progress: Float) = viewModelScope.launch {
        repository.updateGoalProgress(id, progress)
    }

    fun updateGoalNotes(id: Long, notes: String) = viewModelScope.launch {
        repository.updateGoalNotes(id, notes)
    }

    fun addHabit(name: String, category: String, icon: String) = viewModelScope.launch {
        if (name.isNotBlank()) repository.addHabit(name, category, icon)
    }

    fun deleteHabit(id: Long) = viewModelScope.launch {
        repository.deleteHabit(id)
    }

    fun addGoal(title: String, category: String, horizon: String) = viewModelScope.launch {
        if (title.isNotBlank()) repository.addGoal(title, category, horizon)
    }

    fun deleteGoal(id: Long) = viewModelScope.launch {
        repository.deleteGoal(id)
    }

    fun addQuote(text: String, author: String) = viewModelScope.launch {
        if (text.isNotBlank()) repository.addQuote(text.trim(), author.trim())
    }

    fun deleteQuote(id: Long) = viewModelScope.launch {
        repository.deleteQuote(id)
    }

    fun addNote(title: String, body: String) = viewModelScope.launch {
        if (title.isNotBlank() || body.isNotBlank()) repository.addNote(title.trim(), body.trim())
    }

    fun updateNote(id: Long, title: String, body: String) = viewModelScope.launch {
        repository.updateNote(id, title, body)
    }

    fun deleteNote(id: Long) = viewModelScope.launch {
        repository.deleteNote(id)
    }

    fun deleteFinanceEntry(id: Long) = viewModelScope.launch {
        repository.deleteFinanceEntry(id)
    }

    fun addFinanceEntry(title: String, category: String, amount: Double, type: String) = viewModelScope.launch {
        if (amount > 0.0) {
            repository.addFinanceEntry(
                title = title.trim(),
                category = category.trim(),
                amount = amount,
                type = type,
                date = _selectedDate.value.toString()
            )
        }
    }

    fun launchSubscription(activity: Activity) = viewModelScope.launch {
        subscriptionManager.launchBillingFlow(activity)
    }

    fun refreshPurchases() {
        subscriptionManager.refreshPurchases()
    }

    fun signInWithGoogle(activity: Activity) = viewModelScope.launch {
        firebaseSyncManager.signInWithGoogle(activity)
    }

    fun signOut() = viewModelScope.launch {
        firebaseSyncManager.signOut()
    }

    fun syncNow() = viewModelScope.launch {
        firebaseSyncManager.syncNow()
    }

    fun restoreFromCloud() = viewModelScope.launch {
        firebaseSyncManager.restoreFromCloud()
    }

    companion object {
        fun factory(
            repository: PlannerRepository,
            firebaseSyncManager: FirebaseSyncManager,
            subscriptionManager: SubscriptionManager
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    return PlannerViewModel(repository, firebaseSyncManager, subscriptionManager) as T
                }
            }
    }
}


