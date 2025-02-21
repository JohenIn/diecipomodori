package com.android.exampke.diecipomodori.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


class GameViewModel : ViewModel() {
    // 기본 코인 수 3
    var defaultCoinCount by mutableStateOf(3)
        private set

    // 사용된 코인 수
    var usedCoin by mutableStateOf(0)
        private set

    // 사용 가능한 코인 수 = 기본 코인 수 - 사용된 코인 수
    val coinsForPlaying: Int
        get() = defaultCoinCount - usedCoin

    // Play 버튼을 누를 때마다 usedCoin 증가 (최대 defaultCoinCount까지만 증가)
    fun increaseUsedCoin() {
        if (usedCoin < defaultCoinCount) {
            usedCoin++
        }
    }

    // 사용된 코인이 있을 때 1개를 보충하는 함수
    fun replenishCoin() {
        if (usedCoin > 0) {
            usedCoin--
        }
    }


    // 필요에 따라 초기화 함수도 추가 가능
    fun resetCoins() {
        usedCoin = 0
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
