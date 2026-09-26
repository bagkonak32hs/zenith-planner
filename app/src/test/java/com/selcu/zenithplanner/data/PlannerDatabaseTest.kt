package com.selcu.zenithplanner.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlannerDatabaseTest {

    private lateinit var db: PlannerDatabase
    private lateinit var habitDao: HabitDao
    private lateinit var goalDao: GoalDao
    private lateinit var taskDao: TaskDao
    private lateinit var noteDao: NoteDao
    private lateinit var financeDao: FinanceDao
    private lateinit var dailyPlanDao: DailyPlanDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PlannerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habitDao = db.habitDao()
        goalDao = db.goalDao()
        taskDao = db.taskDao()
        noteDao = db.noteDao()
        financeDao = db.financeDao()
        dailyPlanDao = db.dailyPlanDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun insertAndObserveHabit() = runTest {
        val habit = HabitEntity(name = "Run", category = "Health", icon = "FitnessCenter")
        habitDao.insertHabit(habit)
        val habits = habitDao.observeHabits().first()
        assertEquals(1, habits.size)
        assertEquals("Run", habits[0].name)
    }

    @Test
    fun deleteHabitRemovesIt() = runTest {
        habitDao.insertHabit(HabitEntity(name = "Yoga", category = "Mind", icon = "Spa"))
        val id = habitDao.observeHabits().first()[0].id
        habitDao.deleteHabit(id)
        val habits = habitDao.observeHabits().first()
        assertTrue(habits.isEmpty())
    }

    @Test
    fun insertAndObserveGoal() = runTest {
        goalDao.insert(GoalEntity(title = "Read 10 books", category = "Growth", horizon = "MONTHLY", progress = 0f))
        val goals = goalDao.observeGoals().first()
        assertEquals(1, goals.size)
        assertEquals("Read 10 books", goals[0].title)
    }

    @Test
    fun deleteGoalRemovesIt() = runTest {
        goalDao.insert(GoalEntity(title = "Learn Kotlin", category = "Career", horizon = "90_DAY", progress = 0.1f))
        val id = goalDao.observeGoals().first()[0].id
        goalDao.delete(id)
        assertTrue(goalDao.observeGoals().first().isEmpty())
    }

    @Test
    fun updateGoalProgress() = runTest {
        goalDao.insert(GoalEntity(title = "Sleep better", category = "Health", horizon = "LIFE", progress = 0f))
        val id = goalDao.observeGoals().first()[0].id
        goalDao.updateProgress(id, 0.75f)
        val updated = goalDao.observeGoals().first()[0]
        assertEquals(0.75f, updated.progress, 0.001f)
    }

    @Test
    fun insertAndDeleteNote() = runTest {
        noteDao.insert(NoteEntity(title = "Meeting notes", body = "Discuss roadmap"))
        val id = noteDao.observeNotes().first()[0].id
        noteDao.delete(id)
        assertTrue(noteDao.observeNotes().first().isEmpty())
    }

    @Test
    fun insertAndDeleteFinanceEntry() = runTest {
        financeDao.insert(FinanceEntryEntity(title = "Salary", category = "Work", amount = 5000.0, type = "income", date = "2026-01-01"))
        val id = financeDao.observeEntries().first()[0].id
        financeDao.delete(id)
        assertTrue(financeDao.observeEntries().first().isEmpty())
    }

    @Test
    fun ensureDayScaffoldCreatesDailyPlan() = runTest {
        dailyPlanDao.upsert(DailyPlanEntity(date = "2026-01-15"))
        val plan = dailyPlanDao.getPlan("2026-01-15")
        assertEquals("2026-01-15", plan?.date)
    }

    @Test
    fun taskInsertAndComplete() = runTest {
        taskDao.insert(TaskEntity(date = "2026-01-15", title = "Write tests"))
        val tasks = taskDao.observeTasks("2026-01-15").first()
        assertEquals(1, tasks.size)
        taskDao.updateDone(tasks[0].id, true)
        val updated = taskDao.observeTasks("2026-01-15").first()
        assertTrue(updated[0].isDone)
    }

    @Test
    fun observeActiveDatesReturnsTaskDates() = runTest {
        taskDao.insert(TaskEntity(date = "2026-02-01", title = "Plan sprint"))
        taskDao.insert(TaskEntity(date = "2026-02-03", title = "Code review"))
        val dates = taskDao.observeActiveDates().first()
        assertTrue("2026-02-01" in dates)
        assertTrue("2026-02-03" in dates)
    }

    @Test
    fun habitLogUpsertAndObserve() = runTest {
        val habitId = habitDao.insertHabit(HabitEntity(name = "Water", category = "Health", icon = "Water"))
        habitDao.upsertLog(HabitLogEntity(habitId = habitId, date = "2026-01-15", isDone = true))
        val logs = habitDao.observeLogs("2026-01-15").first()
        assertEquals(1, logs.size)
        assertTrue(logs[0].isDone)
    }

    @Test
    fun dailyPlanUpdateNotesAndReflection() = runTest {
        dailyPlanDao.upsert(DailyPlanEntity(date = "2026-01-20"))
        dailyPlanDao.updateNotes("2026-01-20", "Focus on deep work")
        dailyPlanDao.updateReflection("2026-01-20", "Productive day")
        val plan = dailyPlanDao.getPlan("2026-01-20")
        assertEquals("Focus on deep work", plan?.notes)
        assertEquals("Productive day", plan?.reflection)
    }

    @Test
    fun clearDailyPlansRemovesAll() = runTest {
        dailyPlanDao.upsert(DailyPlanEntity(date = "2026-01-01"))
        dailyPlanDao.upsert(DailyPlanEntity(date = "2026-01-02"))
        dailyPlanDao.clear()
        assertNull(dailyPlanDao.getPlan("2026-01-01"))
        assertNull(dailyPlanDao.getPlan("2026-01-02"))
    }
}
