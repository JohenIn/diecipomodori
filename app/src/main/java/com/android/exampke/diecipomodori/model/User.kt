package com.android.exampke.diecipomodori.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0, //고유아이디값. Entity와 PrimaryKey 밑에서 자동으로 +1 증가하며 생성됨. 고유 값이기 때문에 절대 겹치면 안된다.
    @ColumnInfo(name = "score") val score: Int?,
)