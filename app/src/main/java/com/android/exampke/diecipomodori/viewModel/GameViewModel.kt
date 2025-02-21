package com.android.exampke.diecipomodori.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.exampke.diecipomodori.data.CoinRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val coinRepo = CoinRepository(application.applicationContext)

    // 기본 코인 수 3
    var defaultCoinCount by mutableStateOf(5)
        private set

    // 사용된 코인 수, SharedPreferences에서 불러오기
    var usedCoin by mutableStateOf(coinRepo.getUsedCoin())
        private set

    // 사용 가능한 코인 수 = 기본 코인 수 - 사용된 코인 수
    val coinsForPlaying: Int
        get() = defaultCoinCount - usedCoin

    //usedCoin 증가 (최대 defaultCoinCount까지만 증가)
    fun increaseUsedCoin() {
        if (usedCoin < defaultCoinCount) {
            usedCoin++
            coinRepo.saveUsedCoin(usedCoin)
        }
    }

    //코인 보충
    fun replenishCoin() {
        if (usedCoin > 0) {
            usedCoin--
            coinRepo.saveUsedCoin(usedCoin)
        }
    }

    // usedCoin 초기화
    fun resetCoins() {
        usedCoin = 0
        coinRepo.saveUsedCoin(usedCoin)
    }

    init {
        // 5분(300,000ms)마다 replenishCoin()을 호출하여 코인을 보충합니다.
        viewModelScope.launch {
            while (true) {
                delay(300_000L) // 5분 지연
                replenishCoin()
            }
        }
    }
}
