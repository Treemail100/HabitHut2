package com.habithut.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(blockedApp: BlockedApp): Long

    @Delete
    suspend fun delete(blockedApp: BlockedApp)

    @Query("SELECT * FROM blocked_apps ORDER BY id DESC")
    fun observeBlocked(): Flow<List<BlockedApp>>

    @Query("SELECT COUNT(*) FROM blocked_apps")
    suspend fun count(): Int

    @Query("SELECT * FROM blocked_apps")
    suspend fun getAll(): List<BlockedApp>
}