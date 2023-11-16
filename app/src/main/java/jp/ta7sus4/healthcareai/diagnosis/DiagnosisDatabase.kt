package jp.ta7sus4.healthcareai.diagnosis

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "diagnosis_history")
data class DiagnosisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val score: Int,
    val comment: String = "",
    val date: Date = Date()
)

@Dao
interface DiagnosisDao {
    @Query("select * from diagnosis_history order by date asc")
    fun getAll(): MutableList<DiagnosisEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun post(diagnosisEntity: DiagnosisEntity)

    @Delete
    fun delete(diagnosisEntity: DiagnosisEntity)

    @Query("DELETE FROM diagnosis_history")
    fun deleteAll()
}

class DateTimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(entities = [DiagnosisEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diagnosisDao(): DiagnosisDao
}
