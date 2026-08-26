package com.gyosanila.kartcilik

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/** Pegangan Activity aktif — dipakai ViewModel untuk menampilkan iklan. */
object AppActivityHolder {
    var current: Activity? = null
}

// ─── AdMob: debug selalu TEST. Release pakai REAL cuma kalau ADS_LIVE=true
// (setelah app di AdMob console terdaftar dengan package com.gyosanila.logichild). ───
const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
const val REAL_BANNER = "ca-app-pub-6023230476562279/9234156447"
const val REAL_INTERSTITIAL = "ca-app-pub-6023230476562279/1340566000"

/** true = release pakai iklan asli (wajib: app AdMob sudah terdaftar dgn package baru). */
const val ADS_LIVE = false

val AD_UNIT_BANNER: String
    get() = if (BuildConfig.DEBUG || !ADS_LIVE) TEST_BANNER else REAL_BANNER
val AD_UNIT_INTERSTITIAL: String
    get() = if (BuildConfig.DEBUG || !ADS_LIVE) TEST_INTERSTITIAL else REAL_INTERSTITIAL

private var interstitial: InterstitialAd? = null
private var interstitialLoading = CompletableDeferred<Unit>()

/**
 * Konfigurasi iklan aman untuk aplikasi anak (COPPA):
 * - tagForChildDirectedTreatment = 1
 * - maxAdContentRating = G (family safe)
 * Diterapkan global via RequestConfiguration.
 */
fun childSafeAdRequest(): AdRequest = AdRequest.Builder().build()

fun loadInterstitial(activity: Activity) {
    interstitialLoading = CompletableDeferred()
    InterstitialAd.load(
        activity,
        AD_UNIT_INTERSTITIAL,
        childSafeAdRequest(),
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitial = ad
                interstitialLoading.complete(Unit)
            }

            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                interstitialLoading.complete(Unit) // lepas penunggu walau gagal
            }
        },
    )
}

/** Tunggu interstitial siap (maks timeoutMs); true = siap & langsung tampil. */
suspend fun awaitAndShowInterstitial(activity: Activity, timeoutMs: Long): Boolean {
    val ready = withTimeoutOrNull(timeoutMs) { interstitialLoading.await() } != null
    if (ready && interstitial != null) {
        showInterstitialIfReady(activity)
        return true
    }
    return false
}

fun showInterstitialIfReady(activity: Activity): Boolean {
    val ad = interstitial ?: return false
    interstitial = null
    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            // Siapkan interstitial berikutnya begitu yang sekarang ditutup.
            loadInterstitial(activity)
        }
    }
    ad.show(activity)
    return true
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
