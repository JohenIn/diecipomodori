package com.android.exampke.diecipomodori.data

import android.content.Context
import android.content.SharedPreferences

class CoinRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USED_COIN = "used_coin"
    }

    fun saveUsedCoin(usedCoin: Int) {
        prefs.edit().putInt(KEY_USED_COIN, usedCoin).apply()
    }

    fun getUsedCoin(): Int {
        return prefs.getInt(KEY_USED_COIN, 0) // 기본값 0
    }
}
// Compare this snippet from app/src/main/java/com/android/exampke/diecipomodori/LobbyScreen.kt:
// package com.android.exampke.diecipomodori
//