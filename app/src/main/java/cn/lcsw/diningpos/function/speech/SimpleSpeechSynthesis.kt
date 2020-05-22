package cn.lcsw.diningpos.function.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import cn.lcsw.diningpos.R
import java.io.IOException
import java.util.*

class SimpleSpeechSynthesis constructor(var mContext: Context) {

    private val TAG = "SpeechSynthesis"

    private val VOLUME = 1.0f   //播放音量
    private val MAX_STREAMS = 1   //同时播放音频数量

    private lateinit var soundPool: SoundPool

    private val mSoundMap = HashMap<String, Int>()  //声音对象容器，key = 文件名

    init {
        createSoundPool()
        loadSounds()
    }

    /**
     * 初始化soundPool
     */
    private fun createSoundPool() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attr = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attr)
                .build()
        } else {
            SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0)
        }
    }

    /**
     * 加载本地音频
     */
    private fun loadSounds() {
        mSoundMap["welcome"] = soundPool.load(mContext, R.raw.welcome, 1)
        mSoundMap["0"] = soundPool.load(mContext, R.raw.a0, 1)
        mSoundMap["1"] = soundPool.load(mContext, R.raw.a1, 1)
        mSoundMap["2"] = soundPool.load(mContext, R.raw.a2, 1)
        mSoundMap["3"] = soundPool.load(mContext, R.raw.a3, 1)
        mSoundMap["4"] = soundPool.load(mContext, R.raw.a4, 1)
        mSoundMap["5"] = soundPool.load(mContext, R.raw.a5, 1)
        mSoundMap["6"] = soundPool.load(mContext, R.raw.a6, 1)
        mSoundMap["7"] = soundPool.load(mContext, R.raw.a7, 1)
        mSoundMap["8"] = soundPool.load(mContext, R.raw.a8, 1)
        mSoundMap["9"] = soundPool.load(mContext, R.raw.a9, 1)
        mSoundMap["point"] = soundPool.load(mContext, R.raw.point, 1)
        mSoundMap["qrcode"] = soundPool.load(mContext, R.raw.qrcode, 1)
        mSoundMap["scan"] = soundPool.load(mContext, R.raw.scan, 1)
        mSoundMap["success"] = soundPool.load(mContext, R.raw.success, 1)
        mSoundMap["fail"] = soundPool.load(mContext, R.raw.fail, 1)
    }

    @Throws(IOException::class)
    fun speakEmpty() {
        soundPool.play(mSoundMap["welcome"] ?: 0, 0f, 0f, 1, 0, 1.0f)
    }

    @Throws(IOException::class)
    fun speak0() {
        soundPool.play(mSoundMap["0"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speak1() {
        soundPool.play(mSoundMap["1"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speak2() {
        soundPool.play(mSoundMap["2"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speak3() {
        soundPool.play(mSoundMap["3"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speak4() {
        soundPool.play(mSoundMap["4"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speak5() {
        soundPool.play(mSoundMap["5"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speak6() {
        soundPool.play(mSoundMap["6"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speak7() {
        soundPool.play(mSoundMap["7"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speak8() {
        soundPool.play(mSoundMap["8"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speak9() {
        soundPool.play(mSoundMap["9"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
    @Throws(IOException::class)
    fun speakPoint() {
        soundPool.play(mSoundMap["point"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(IOException::class)
    fun speakWelcome() {
        soundPool.play(mSoundMap["welcome"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(IOException::class)
    fun speakScan() {
        soundPool.play(mSoundMap["scan"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(IOException::class)
    fun speakFail() {
        soundPool.play(mSoundMap["fail"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(IOException::class)
    fun speakSuccess() {
        soundPool.play(mSoundMap["success"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(IOException::class)
    fun speakQrcode() {
        soundPool.play(mSoundMap["qrcode"] ?: 0, VOLUME, VOLUME, 1, 0, 1.0f)
    }
}