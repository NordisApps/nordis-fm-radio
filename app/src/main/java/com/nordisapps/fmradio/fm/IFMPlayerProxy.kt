package com.nordisapps.fmradio.fm

import android.os.IBinder
import android.os.Parcel
import android.util.Log

class IFMPlayerProxy(
    private val remote: IBinder,
    private val codes: FmCodes
) : IFMPlayer {
    companion object {
        const val DESCRIPTOR =
            "com.samsung.android.media.fmradio.internal.IFMPlayer"

        const val TRANSACTION_SET_LISTENER = 1
        const val TRANSACTION_TUNE = 3
        const val TRANSACTION_ON = 4
        const val TRANSACTION_OFF = 6
        const val TRANSACTION_IS_ON = 7
        const val TRANSACTION_SEEK_UP = 8
        const val TRANSACTION_SEEK_DOWN = 9
        const val TRANSACTION_GET_CURRENT_CHANNEL = 11
        const val TRANSACTION_SCAN = 12
        const val TRANSACTION_CANCEL_SCAN = 13
        const val TRANSACTION_IS_SCANNING = 14
        const val TRANSACTION_ENABLE_RDS = 20
        const val TRANSACTION_DISABLE_RDS = 21

        private const val PROBE_OLD_ONLY = 56

        fun create(remote: IBinder): IFMPlayerProxy {
            val old = probeOldOnly(remote)
            Log.d("IFMPlayerProxy", "layout = ${if (old) "OLD" else "NEW"}")
            return IFMPlayerProxy(remote, if (old) FmCodes.OLD else FmCodes.NEW)
        }

        private fun probeOldOnly(binder: IBinder): Boolean {
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            return try {
                data.writeInterfaceToken(DESCRIPTOR)
                binder.transact(PROBE_OLD_ONLY, data, reply, 0)
            } catch (_: Exception) {
                false
            } finally {
                data.recycle()
                reply.recycle()
            }
        }
    }

    private inline fun <T> transact(
        code: Int,
        writeArgs: Parcel.() -> Unit = {},
        readResult: Parcel.() -> T
    ): T {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()

        return try {
            data.writeInterfaceToken(DESCRIPTOR)
            data.writeArgs()

            if (!remote.transact(code, data, reply, 0)) {
                throw UnsupportedOperationException("transaction $code rejected")
            }

            reply.readException()
            reply.readResult()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    override fun on(): Boolean = transact(TRANSACTION_ON) { readInt() != 0 }

    override fun isOn(): Boolean = transact(TRANSACTION_IS_ON) { readInt() != 0 }

    override fun off(): Boolean = transact(TRANSACTION_OFF) { readInt() != 0 }

    override fun tune(freq: Long) = transact(
        TRANSACTION_TUNE,
        writeArgs = { writeLong(freq) },
        readResult = {}
    )

    override fun seekDown(): Long = transact(TRANSACTION_SEEK_DOWN) { readLong() }

    override fun seekUp(): Long = transact(TRANSACTION_SEEK_UP) { readLong() }

    override fun scan() = transact(TRANSACTION_SCAN) {}

    override fun cancelScan(): Boolean = transact(TRANSACTION_CANCEL_SCAN) { readInt() != 0 }

    override fun isScanning(): Boolean = transact(TRANSACTION_IS_SCANNING) { readInt() != 0 }

    override fun getLastScanResult(): LongArray = transact(codes.getLastScanResult) {
        createLongArray() ?: LongArray(0)
    }

    override fun getCurrentChannel(): Long =
        transact(TRANSACTION_GET_CURRENT_CHANNEL) { readLong() }

    override fun setSpeakerOn(enabled: Boolean) = transact(
        codes.setSpeaker,
        writeArgs = { writeInt(if (enabled) 1 else 0) }
    ) {}

    override fun setStereo() = transact(codes.setStereo) {}

    override fun setMono() = transact(codes.setMono) {}

    override fun setSoftmute(enabled: Boolean) = transact(
        codes.setSoftmute,
        writeArgs = { writeInt(if (enabled) 1 else 0) }
    ) {}

    override fun getSoftMuteMode(): Boolean =
        transact(codes.getSoftmute) { readInt() != 0 }

    override fun isHeadsetPlugged(): Boolean =
        transact(codes.isHeadsetPlugged) { readInt() != 0 }

    override fun disableRDS() = transact(TRANSACTION_DISABLE_RDS) {}

    override fun enableRDS() = transact(TRANSACTION_ENABLE_RDS) {}

    override fun isRDSEnable(): Boolean =
        transact(codes.isRdsEnable) { readInt() != 0 }

    override fun enableAF() = transact(codes.enableAf) {}

    override fun disableAF() = transact(codes.disableAf) {}

    override fun setBand(band: Int) = transact(
        codes.setBand,
        writeArgs = { writeInt(band) }
    ) {}

    override fun setChannelSpacing(spacing: Int) = transact(
        codes.setChannelSpacing,
        writeArgs = { writeInt(spacing) }
    ) {}

    override fun setListener(listener: IFMEventListener) = transact(
        TRANSACTION_SET_LISTENER,
        writeArgs = { writeStrongBinder(listener.asBinder()) }
    ) {}

    override fun getIntegerTunningParameter(
        key: String,
        defaultValue: Int
    ): Int = transact(
        codes.getIntegerTuning,
        writeArgs = {
            writeString(key)
            writeInt(defaultValue)
        }
    ) { readInt() }
}