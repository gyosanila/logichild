package com.gyosanila.kartcilik

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

// ─── TEST IDs (AdMob). Ganti dengan ID asli dari akun AdMob sebelum rilis. ───
const val AD_UNIT_BANNER = "ca-app-pub-3940256099942544/6300978111"
const val AD_UNIT_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"

private var interstitial: InterstitialAd? = null

/**
 * Konfigurasi iklan aman untuk aplikasi anak (COPPA):
 * - tagForChildDirectedTreatment = 1
 * - maxAdContentRating = G (family safe)
 * Diterapkan global via RequestConfiguration.
 */
fun childSafeAdRequest(): AdRequest = AdRequest.Builder().build()

fun loadInterstitial(activity: Activity) {
    InterstitialAd.load(
        activity,
        AD_UNIT_INTERSTITIAL,
        childSafeAdRequest(),
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitial = ad
            }
        },
    )
}

fun showInterstitialIfReady(activity: Activity) {
    val ad = interstitial ?: return
    interstitial = null
    ad.show(activity)
}

/** Banner adaptif di bawah layar (test unit). */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adView = remember(context) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AD_UNIT_BANNER
        }
    }
    AndroidView(
        factory = { adView },
        modifier = modifier,
        update = { it.loadAd(childSafeAdRequest()) },
    )
}

/** Init AdMob — panggil sekali di Application/Activity. */
fun initAds(context: Context) {
    MobileAds.initialize(context) {}
    MobileAds.setRequestConfiguration(
        com.google.android.gms.ads.RequestConfiguration.Builder()
            .setMaxAdContentRating(com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .setTagForChildDirectedTreatment(
                com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE,
            )
            .build(),
    )
}
