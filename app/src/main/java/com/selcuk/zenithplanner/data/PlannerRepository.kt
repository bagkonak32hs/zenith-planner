package com.selcuk.zenithplanner.data

import android.content.Context
import com.selcuk.zenithplanner.R
import com.selcuk.zenithplanner.widget.PlannerWidgetUpdater
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class PlannerRepository(
    private val appContext: Context,
    private val dailyPlanDao: DailyPlanDao,
    private val taskDao: TaskDao,
    private val scheduleDao: ScheduleDao,
    private val habitDao: HabitDao,
    private val goalDao: GoalDao,
    private val quoteDao: QuoteDao,
    private val noteDao: NoteDao,
    private val financeDao: FinanceDao
) {
    fun observePlan(date: String): Flow<DailyPlanEntity?> = dailyPlanDao.observePlan(date)
    fun observeTasks(date: String): Flow<List<TaskEntity>> = taskDao.observeTasks(date)
    fun observeSchedule(date: String): Flow<List<ScheduleEntryEntity>> = scheduleDao.observeSchedule(date)
    fun observeHabits(): Flow<List<HabitEntity>> = habitDao.observeHabits()
    fun observeHabitLogs(date: String): Flow<List<HabitLogEntity>> = habitDao.observeLogs(date)
    fun observeGoals(): Flow<List<GoalEntity>> = goalDao.observeGoals()
    fun observeQuotes(): Flow<List<QuoteEntity>> = quoteDao.observeQuotes()
    fun observeNotes(): Flow<List<NoteEntity>> = noteDao.observeNotes()
    fun observeFinanceEntries(): Flow<List<FinanceEntryEntity>> = financeDao.observeEntries()
    fun observeActiveDates(): Flow<Set<String>> = taskDao.observeActiveDates().map { it.toSet() }

    fun observeStreaks(): Flow<Map<Long, Int>> =
        habitDao.observeHabits().map { habits ->
            habits.associate { habit ->
                habit.id to calculateStreak(habitDao.getDoneDatesForHabit(habit.id))
            }
        }

    private fun calculateStreak(dates: List<String>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.mapNotNull {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }.sortedDescending()
        var streak = 0
        var expected = LocalDate.now()
        for (date in sorted) {
            if (date == expected || date == expected.minusDays(1) && streak == 0) {
                streak++
                expected = date.minusDays(1)
            } else if (date < expected) break
        }
        return streak
    }

    suspend fun ensureDayScaffold(date: String) {
        if (dailyPlanDao.getPlan(date) == null) {
            dailyPlanDao.upsert(DailyPlanEntity(date))
        }

        val taskCount = taskDao.countForDate(date)
        if (taskCount < 3) {
            repeat(3 - taskCount) { index ->
                taskDao.insert(TaskEntity(date = date, sortOrder = taskCount + index))
            }
        }

        if (scheduleDao.countForDate(date) == 0) {
            (6..22).forEach { hour ->
                scheduleDao.insert(ScheduleEntryEntity(date = date, hour = hour))
            }
        }
    }

    suspend fun seedDefaults() {
        val ctx = appContext
        if (habitDao.countHabits() == 0) {
            listOf(
                HabitEntity(name = ctx.getString(R.string.seed_habit_water), category = ctx.getString(R.string.seed_cat_health), icon = "Water", sortOrder = 0),
                HabitEntity(name = ctx.getString(R.string.seed_habit_workout), category = ctx.getString(R.string.seed_cat_health), icon = "FitnessCenter", sortOrder = 1),
                HabitEntity(name = ctx.getString(R.string.seed_habit_meditation), category = ctx.getString(R.string.seed_cat_mind), icon = "Spa", sortOrder = 2),
                HabitEntity(name = ctx.getString(R.string.seed_habit_reading), category = ctx.getString(R.string.seed_cat_growth), icon = "MenuBook", sortOrder = 3),
                HabitEntity(name = ctx.getString(R.string.seed_habit_deep_work), category = ctx.getString(R.string.seed_cat_career), icon = "Work", sortOrder = 4)
            ).forEach { habitDao.insertHabit(it) }
        }

        if (goalDao.countGoals() == 0) {
            listOf(
                GoalEntity(title = ctx.getString(R.string.seed_goal_1), category = ctx.getString(R.string.seed_goal_cat_personal), horizon = "MONTHLY", progress = 0.35f),
                GoalEntity(title = ctx.getString(R.string.seed_goal_2), category = ctx.getString(R.string.seed_cat_career), horizon = "90_DAY", progress = 0.2f),
                GoalEntity(title = ctx.getString(R.string.seed_goal_3), category = ctx.getString(R.string.seed_goal_cat_finance), horizon = "LIFE", progress = 0.18f),
                GoalEntity(title = ctx.getString(R.string.seed_goal_4), category = ctx.getString(R.string.seed_cat_health), horizon = "LIFE", progress = 0.42f)
            ).forEach { goalDao.insert(it) }
        }

        if (quoteDao.countQuotes() == 0) {
            quoteDao.insert(
                QuoteEntity(
                    text = ctx.getString(R.string.seed_quote),
                    author = ctx.getString(R.string.seed_quote_author)
                )
            )
        }

        ensureDayScaffold(LocalDate.now().toString())
        PlannerWidgetUpdater.updateAll(appContext)
    }

    suspend fun updateTaskTitle(id: Long, title: String) {
        taskDao.updateTitle(id, title)
        PlannerWidgetUpdater.updateAll(appContext)
    }

    suspend fun updateTaskDone(id: Long, isDone: Boolean) {
        taskDao.updateDone(id, isDone)
        PlannerWidgetUpdater.updateAll(appContext)
    }
    suspend fun updateScheduleTitle(id: Long, title: String) = scheduleDao.updateTitle(id, title)
    suspend fun updateNotes(date: String, notes: String) = dailyPlanDao.updateNotes(date, notes)
    suspend fun updateReflection(date: String, reflection: String) = dailyPlanDao.updateReflection(date, reflection)
    suspend fun setHabitDone(habitId: Long, date: String, isDone: Boolean) = habitDao.upsertLog(HabitLogEntity(habitId, date, isDone))
    suspend fun updateGoalProgress(id: Long, progress: Float) = goalDao.updateProgress(id, progress.coerceIn(0f, 1f))
    suspend fun updateGoalNotes(id: Long, notes: String) = goalDao.updateNotes(id, notes)
    suspend fun addHabit(name: String, category: String, icon: String) {
        val current = habitDao.getAllHabits()
        habitDao.insertHabit(HabitEntity(name = name.trim(), category = category.trim(), icon = icon, sortOrder = current.size))
    }

    suspend fun deleteHabit(id: Long) {
        habitDao.deleteHabit(id)
        habitDao.clearLogs()
    }

    suspend fun addGoal(title: String, category: String, horizon: String) =
        goalDao.insert(GoalEntity(title = title.trim(), category = category.trim(), horizon = horizon, progress = 0f))

    suspend fun deleteGoal(id: Long) = goalDao.delete(id)

    suspend fun addQuote(text: String, author: String) = quoteDao.insert(QuoteEntity(text = text, author = author.ifBlank { "Personal" }))
    suspend fun deleteQuote(id: Long) = quoteDao.delete(id)
    suspend fun addNote(title: String, body: String) = noteDao.insert(NoteEntity(title = title.ifBlank { "Untitled" }, body = body))
    suspend fun updateNote(id: Long, title: String, body: String) =
        noteDao.update(id, title.ifBlank { "Untitled" }, body, System.currentTimeMillis())

    suspend fun deleteNote(id: Long) = noteDao.delete(id)
    suspend fun deleteFinanceEntry(id: Long) = financeDao.delete(id)
    suspend fun addFinanceEntry(title: String, category: String, amount: Double, type: String, date: String) {
        financeDao.insert(
            FinanceEntryEntity(
                title = title.ifBlank { category.ifBlank { "Entry" } },
                category = category.ifBlank { "General" },
                amount = amount,
                type = type,
                date = date
            )
        )
    }
}


