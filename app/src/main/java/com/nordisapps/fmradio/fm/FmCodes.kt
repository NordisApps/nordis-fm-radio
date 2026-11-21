package com.nordisapps.fmradio.fm

class FmCodes(
    val getLastScanResult: Int,
    val setSpeaker: Int,
    val setStereo: Int,
    val setMono: Int,
    val setSoftmute: Int,
    val getSoftmute: Int,
    val isHeadsetPlugged: Int,
    val isRdsEnable: Int,
    val enableAf: Int,
    val disableAf: Int,
    val setBand: Int,
    val setChannelSpacing: Int,
    val getIntegerTuning: Int
) {
    companion object {
        val OLD = FmCodes(33, 40, 34, 35, 47, 48, 38, 30, 25, 26, 27, 28, 51)
        val NEW = FmCodes(30, 37, 31, 32, 44, 45, 35, 27, 22, 23, 24, 25, 47)
    }
}