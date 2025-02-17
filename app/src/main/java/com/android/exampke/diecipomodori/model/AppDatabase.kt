package com.android.exampke.diecipomodori.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao //추상화
}

object MyDb {
    @Volatile //휘발성이라는 뜻
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) { //동시접근을 방지한다. 실행 중일땐 lock이 걸린다
            Room.databaseBuilder(
                context,
                AppDatabase::class.java, "diecipomodori.db"//이름 한 번 정해주면 고정된다
            )
                .build()
                .also { instance = it } //also 문법
        }
    }
}