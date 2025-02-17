package com.android.exampke.diecipomodori.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
    @Query("SELECT MAX(score) FROM user")
    suspend fun getMaxScore(): Int?

    @Insert
    suspend fun insert(user: User)

    @Transaction
    suspend fun insertIfHigher(user: User) {
        // 현재 최고 점수 조회 (null이면 0으로 간주)
        val maxScore = getMaxScore() ?: 0
        // 새로운 점수가 더 높을 경우에만 저장
        if ((user.score ?: 0) > maxScore) {
            insert(user)
        }
    }
}