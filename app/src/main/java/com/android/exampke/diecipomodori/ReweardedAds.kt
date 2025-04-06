package com.android.exampke.diecipomodori

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.android.exampke.diecipomodori.viewmodel.GameViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.android.exampke.diecipomodori.BuildConfig
import kotlinx.coroutines.delay

@Composable
fun PreloadedRewardedAd(
    viewAds: Boolean,
    onViewAdsConsumed: () -> Unit, // viewAds를 false로 만드는 콜백
    gameViewModel: GameViewModel,
    TAG: String = "LobbyScreen"
) {
    val context = LocalContext.current
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            BuildConfig.AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Rewarded ad failed to load: ${adError.message}")
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded Ad was loaded.")
                    rewardedAd = ad
                }
            }
        )
    }

    // 초기 로드: 컴포저블 생성 시 한 번 광고 로드
    LaunchedEffect(Unit) {
        loadRewardedAd()
    }

    // viewAds가 true이면 광고를 표시
    LaunchedEffect(viewAds) {
        if (viewAds) {
            if (rewardedAd == null) {
                Log.d(TAG, "Rewarded ad not ready yet. Loading now...")
                loadRewardedAd()
                // 잠시 기다렸다가 재시도(예: 1초 후)
                delay(1000L)
                if (rewardedAd == null) {
                    Log.d(TAG, "Still not ready. Cancelling ad request.")
                    onViewAdsConsumed()
                    return@LaunchedEffect
                }
            }
            rewardedAd?.let { ad ->
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        Log.d(TAG, "Rewarded ad clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Rewarded ad dismissed.")
                        gameViewModel.resetCoins()
                        onViewAdsConsumed()
                        loadRewardedAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                        onViewAdsConsumed()
                        loadRewardedAd()
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Rewarded ad impression recorded.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Rewarded ad showed fullscreen content.")
                    }
                }
                (context as? Activity)?.let { activity ->
                    ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                        Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                        gameViewModel.resetCoins()
                        onViewAdsConsumed()
                        loadRewardedAd()
                    })
                }
            }
        }
    }
}
