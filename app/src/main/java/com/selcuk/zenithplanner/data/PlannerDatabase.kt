package com.selcuk.zenithplanner.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "daily_plans")
data class DailyPlanEntity(
    @PrimaryKey val date: String,
    val notes: String = "",
    val reflection: String = ""
)

@Entity(tableName = "tasks", indices = [Index("date")])
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val title: String = "",
    val isDone: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(tableName = "schedule_entries", indices = [Index("date")])
data class ScheduleEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val hour: Int,
    val title: String = ""
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val icon: String,
    val sortOrder: Int = 0
)

@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "date"], indices = [Index("date")])
data class HabitLogEntity(
    val habitId: Long,
    val date: String,
    val isDone: Boolean
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val horizon: String,
    val progress: Float,
    val notes: String = ""
)

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val author: String,
    val isFavorite: Boolean = true
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "finance_entries", indices = [Index("date")])
data class FinanceEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val amount: Double,
    val type: String,
    val date: String
)

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plans WHERE date = :date")
    fun observePlan(date: String): Flow<DailyPlanEntity?>

    @Query("SELECT * FROM daily_plans WHERE date = :date")
    suspend fun getPlan(date: String): DailyPlanEntity?

    @Query("SELECT * FROM daily_plans ORDER BY date ASC")
    suspend fun getAllPlans(): List<DailyPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: DailyPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(plans: List<DailyPlanEntity>)

    @Query("UPDATE daily_plans SET notes = :notes WHERE date = :date")
    suspend fun updateNotes(date: String, notes: String)

    @Query("UPDATE daily_plans SET reflection = :reflection WHERE date = :date")
    suspend fun updateReflection(date: String, reflection: String)

    @Query("DELETE FROM daily_plans")
    suspend fun clear()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY sortOrder ASC, id ASC")
    fun observeTasks(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY sortOrder ASC, id ASC")
    suspend fun getTasksForDate(date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks ORDER BY date ASC, sortOrder ASC, id ASC")
    suspend fun getAllTasks(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE date = :date")
    suspend fun countForDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Query("UPDATE tasks SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)

    @Query("UPDATE tasks SET isDone = :isDone WHERE id = :id")
    suspend fun updateDone(id: Long, isDone: Boolean)

    @Query("SELECT DISTINCT date FROM tasks WHERE title != '' AND isDone = 0")
    fun observeActiveDates(): kotlinx.coroutines.flow.Flow<List<String>>

    @Query("DELETE FROM tasks")
    suspend fun clear()
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_entries WHERE date = :date ORDER BY hour ASC")
    fun observeSchedule(date: String): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries ORDER BY date ASC, hour ASC")
    suspend fun getAllSchedule(): List<ScheduleEntryEntity>

    @Query("SELECT COUNT(*) FROM schedule_entries WHERE date = :date")
    suspend fun countForDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ScheduleEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ScheduleEntryEntity>)

    @Query("UPDATE schedule_entries SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)

    @Query("DELETE FROM schedule_entries")
    suspend fun clear()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, id ASC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllHabits(): List<HabitEntity>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun countHabits(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun observeLogs(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY date ASC, habitId ASC")
    suspend fun getAllLogs(): List<HabitLogEntity>

    @Query("SELECT date FROM habit_logs WHERE habitId = :habitId AND isDone = 1 ORDER BY date DESC")
    suspend fun getDoneDatesForHabit(habitId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLogs(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs")
    suspend fun clearLogs()

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("DELETE FROM habits")
    suspend fun clearHabits()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY horizon ASC, id ASC")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY horizon ASC, id ASC")
    suspend fun getAllGoals(): List<GoalEntity>

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun countGoals(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>)

    @Query("UPDATE goals SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Float)

    @Query("UPDATE goals SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM goals")
    suspend fun clear()
}

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY id DESC")
    fun observeQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes ORDER BY id DESC")
    suspend fun getAllQuotes(): List<QuoteEntity>

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun countQuotes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: QuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<QuoteEntity>)

    @Query("DELETE FROM quotes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM quotes")
    suspend fun clear()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getAllNotes(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Query("UPDATE notes SET title = :title, body = :body, updatedAt = :updatedAt WHERE id = :id")
    suspend fun update(id: Long, title: String, body: String, updatedAt: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Query("DELETE FROM notes")
    suspend fun clear()
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_entries ORDER BY date DESC, id DESC")
    fun observeEntries(): Flow<List<FinanceEntryEntity>>

    @Query("SELECT * FROM finance_entries ORDER BY date DESC, id DESC")
    suspend fun getAllEntries(): List<FinanceEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FinanceEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<FinanceEntryEntity>)

    @Query("DELETE FROM finance_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM finance_entries")
    suspend fun clear()
}

@Database(
    entities = [
        DailyPlanEntity::class,
        TaskEntity::class,
        ScheduleEntryEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        GoalEntity::class,
        QuoteEntity::class,
        NoteEntity::class,
        FinanceEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun taskDao(): TaskDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun quoteDao(): QuoteDao
    abstract fun noteDao(): NoteDao
    abstract fun financeDao(): FinanceDao
}


