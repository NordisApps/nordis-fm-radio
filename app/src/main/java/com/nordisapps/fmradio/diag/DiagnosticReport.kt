package com.nordisapps.fmradio.diag

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import android.os.Process
import com.nordisapps.fmradio.fm.IFMPlayerProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DiagnosticReport {

    // имя, код в старой раскладке, код в новой (из прошивки A08)
    private val probes = listOf(
        Triple("isHeadsetPlugged", 38, 35),
        Triple("isRDSEnable", 30, 27),
        Triple("getSoftMuteMode", 48, 45),
        Triple("isOn", 7, 7)
    )

    suspend fun build(
        context: Context,
        binder: IBinder?,
        includeNewLayout: Boolean
    ): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.appendLine("== DEVICE ==")
        sb.appendLine("model=${Build.MODEL} sdk=${Build.VERSION.SDK_INT} release=${Build.VERSION.RELEASE}")
        sb.appendLine("patch=${Build.VERSION.SECURITY_PATCH}")
        sb.appendLine("fingerprint=${Build.FINGERPRINT}")

        sb.appendLine("\n== AUDIO OUTPUTS (type: 3 wired headset, 4 headphones, 11 usb device, 22 usb headset) ==")
        val am = context.getSystemService(AudioManager::class.java)
        am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).forEach {
            sb.appendLine("type=${it.type} name=${it.productName}")
        }

        sb.appendLine("\n== FM SERVICE ==")
        if (binder == null) {
            sb.appendLine("binder = null (сервис не найден)")
        } else {
            sb.appendLine("alive=${binder.pingBinder()} descriptor=${runCatching { binder.interfaceDescriptor }.getOrNull()}")
            sb.appendLine("\n-- OLD layout codes --")
            probes.forEach { (name, old, _) -> sb.appendLine("$name: ${rawProbe(binder, old)}") }
            if (includeNewLayout) {
                sb.appendLine("\n-- NEW layout codes --")
                probes.forEach { (name, _, new) -> sb.appendLine("$name: ${rawProbe(binder, new)}") }
            }
        }

        sb.appendLine("\n== LOGCAT (own process) ==")
        sb.appendLine(readLogcat())
        sb.toString()
    }

    private fun rawProbe(binder: IBinder, code: Int): String {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(IFMPlayerProxy.DESCRIPTOR)
            val ok = binder.transact(code, data, reply, 0)
            val size = reply.dataSize()
            reply.readException()
            "code=$code transact=$ok replySize=$size int=${reply.readInt()}"
        } catch (t: Throwable) {
            "code=$code EXCEPTION ${t.javaClass.simpleName}: ${t.message}"
        } finally {
            data.recycle(); reply.recycle()
        }
    }

    private val interesting = listOf(
        "FmRadioManager", "FMPlayer", "FMRadio", "ServiceManager", "AndroidRuntime"
    )

    private fun readLogcat(): String = runCatching {
        val p = Runtime.getRuntime().exec(
            arrayOf("logcat", "-d", "-t", "2000", "--pid=${Process.myPid()}")
        )
        p.inputStream.bufferedReader().readLines()
            .filter { line ->
                if ("Accessing hidden" in line) "ServiceManager" in line
                else " E " in line || interesting.any { it in line }
            }
            .takeLast(120)
            .joinToString("\n")
            .ifBlank { "(no relevant log lines)" }
    }.getOrElse { "logcat error: ${it.message}" }

    fun share(context: Context, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}