package cn.lcsw.diningpos.function.speech

import android.content.Context
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import cn.lcsw.diningpos.function.speech.String2Voice.int2Money
import cn.lcsw.diningpos.utils.UnitUtils
import timber.log.Timber
import java.io.IOException
import java.util.*


class SpeechSynthesis constructor(mContext: Context) {

    private val TAG = "SpeechSynthesis"
    private val SOUNDS_FOLDER = "tts2"  //音频资源目录

    private val VOLUME = 1.0f   //播放音量
    private val MAX_STREAMS = 1   //同时播放音频数量
    //  private static final long PLAY_SPEED = 520; //millis 金额播放间隔
    private val PLAY_SPEED: Long = 400 //millis 金额播放间隔, 也可以单独对每个文件设置不同速度

    private var assetManager: AssetManager = mContext.assets
    private lateinit var soundPool: SoundPool

    private val sounds = ArrayList<Sound>()
    private val mSoundMap = HashMap<String, Sound>()  //声音对象容器，key = 文件名

    private val zero = "零"
    private val one = "壹"
    private val two = "贰"
    private val three = "叁"
    private val four = "肆"
    private val five = "伍"
    private val six = "陆"
    private val seven = "柒"
    private val eight = "捌"
    private val nine = "玖"

    private val yuan = "元"
    private val zheng = "整"
    private val shi = "拾"
    private val bai = "佰"
    private val qian = "仟"
    private val wan = "万"
    private val yi = "亿"
    private val dot = "点"

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
        val soundNames: Array<String>?
        try {
            soundNames = assetManager.list(SOUNDS_FOLDER)
            Timber.i("Found " + soundNames?.size + " sounds")
        } catch (ioe: IOException) {
            return
        }

        for (filename in soundNames) {
            try {
                val assetPath = "$SOUNDS_FOLDER/$filename"
                val sound = Sound(assetPath)
                load(sound)
                sounds.add(sound)
                mSoundMap[sound.mName] = sound
            } catch (ioe: IOException) {
            }

        }
    }

    @Throws(IOException::class)
    private fun load(sound: Sound) {
        val afd = assetManager.openFd(sound.assetPath)
        val soundId = soundPool.load(afd, 1)
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            Timber.d("sampleId:$sampleId,status:$status")
        }
        sound.mSoundId = soundId
    }

    private fun play(sound: Sound): Int {
        val soundId = sound.mSoundId
        Timber.i("play: success---soundId:" + sound.mName)
        return soundPool.play(soundId, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(IllegalArgumentException::class, InterruptedException::class)
    fun money2Voice(payType: Int, money: Int) {
        val text = int2Money(money)
        Timber.i("money2Voice: $text")
        //支付方式：0全部 1 微信 2支付宝 3银行卡 4现金 5无卡支付 6qq钱包 7百度钱包8京东钱包 9口碑支付 10翼支付 11银联二维码 12龙支付 13分期
        when (payType) {
            0 -> {
                play(mSoundMap["tts_success"]!!) //收款成功
                Thread.sleep(1160)
            }
            1 -> {
                play(mSoundMap["tts_success_weixin"]!!)
                Thread.sleep(1409)
            }
            2 -> {
                play(mSoundMap["tts_success_zhifubao"]!!)
                Thread.sleep(1280)
            }
            3 -> {
            }
            4 -> {
                play(mSoundMap["tts_success_xianjin"]!!)
                Thread.sleep(1120)
            }
            5 -> {
            }
            6 -> {
                play(mSoundMap["tts_success_qqqianbao"]!!)
                Thread.sleep(1798)
            }
            7 -> {
                play(mSoundMap["tts_success_baiduqianbao"]!!)
                Thread.sleep(1878)
            }
            8 -> {
                play(mSoundMap["tts_success_jdqianbao"]!!)
                Thread.sleep(1872)
            }
            9 -> {
                play(mSoundMap["tts_success_koubei"]!!)
                Thread.sleep(1458)
            }
            10 -> {
                play(mSoundMap["tts_success_yizhifu"]!!)
                Thread.sleep(1592)
            }
            11 -> {
                play(mSoundMap["tts_success_yinlianqr"]!!)
                Thread.sleep(1757)
            }
            12 -> {
                play(mSoundMap["tts_success_longzhifu"]!!)
                Thread.sleep(1642)
            }
            13 -> {
                play(mSoundMap["tts_success_fenqi"]!!)
                Thread.sleep(982)
                play(mSoundMap["tts_success_hebao"]!!)
                Thread.sleep(1569)
            }
            14 -> {
                play(mSoundMap["tts_success_hebao"]!!)
                Thread.sleep(1569)
            }
            114 -> {
                play(mSoundMap["tts_qm_store"]!!)
                Thread.sleep(1117)
            }
            115 -> {
                play(mSoundMap["tts_qm_pay"]!!)
                Thread.sleep(1398)
            }
            116 -> {
                play(mSoundMap["tts_qm_diancan"]!!)
                Thread.sleep(1026)
            }
            else -> {
                play(mSoundMap["tts_success"]!!)  //收款成功
                Thread.sleep(1160)
            }
        }

        if (payType == 116) {
            return
        }

        //金额播报
        val chars = text.toCharArray()
        for (i in chars.indices) {
            when (chars[i] + "") {
                zero -> play(mSoundMap["tts_0"]!!)
                one -> play(mSoundMap["tts_1"]!!)
                two -> play(mSoundMap["tts_2"]!!)
                three -> play(mSoundMap["tts_3"]!!)
                four -> play(mSoundMap["tts_4"]!!)
                five -> play(mSoundMap["tts_5"]!!)
                six -> play(mSoundMap["tts_6"]!!)
                seven -> play(mSoundMap["tts_7"]!!)
                eight -> play(mSoundMap["tts_8"]!!)
                nine -> play(mSoundMap["tts_9"]!!)
                yuan -> play(mSoundMap["tts_yuan"]!!)
                shi -> play(mSoundMap["tts_ten"]!!)
                bai -> play(mSoundMap["tts_hundred"]!!)
                qian -> play(mSoundMap["tts_thousand"]!!)
                wan -> play(mSoundMap["tts_ten_thousand"]!!)
                yi -> play(mSoundMap["tts_ten_million"]!!)
                dot -> play(mSoundMap["tts_dot"]!!)
                else -> {
                }
            }
            Thread.sleep(PLAY_SPEED)
        }
    }

    /**
     * String text = type_msg + "收款" + " " + total_fee + "元";
     * @param speechText
     * 语音播报方法入口
     */
    @Throws(Exception::class)
    fun speak(speechText: String) {
        val strings = speechText.split("收款".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val typeMsg = strings[0]
        val trim = strings[1].trim { it <= ' ' }
        val totalFee = trim.substring(0, trim.length - 1)
        val payType = PayType.forSpeechText(typeMsg)
        payType?.let {
            money2Voice(it.recordCode, UnitUtils.yuan2FenInt(totalFee))
        }
    }

    @Throws(Exception::class)
    fun speakEmpty() {
        val soundId = mSoundMap["tts_0"]!!.mSoundId
        soundPool.play(soundId, 0f, 0f, 1, 0, 1.0f)
    }

    @Throws(Exception::class)
    fun speakWelcome() {
        val soundId = mSoundMap["tts_welcome"]!!.mSoundId
        soundPool.play(soundId, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(Exception::class)
    fun speakScan() {
        val soundId = mSoundMap["a_scan"]!!.mSoundId
        soundPool.play(soundId, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(Exception::class)
    fun speakFail() {
        val soundId = mSoundMap["a_fail"]!!.mSoundId
        soundPool.play(soundId, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(Exception::class)
    fun speakSuccess() {
        val soundId = mSoundMap["tts_success"]!!.mSoundId
        soundPool.play(soundId, VOLUME, VOLUME, 1, 0, 1.0f)
    }

    @Throws(IllegalArgumentException::class, InterruptedException::class)
    fun speakMoney(totalFee: Int) {
        val text = int2Money(totalFee)

        play(mSoundMap["a_topay"]!!)
        Thread.sleep(1400)

        //金额播报
        val chars = text.toCharArray()
        for (i in chars.indices) {
            when (chars[i] + "") {
                zero -> play(mSoundMap["0"]!!)
                one -> play(mSoundMap["1"]!!)
                two -> play(mSoundMap["2"]!!)
                three -> play(mSoundMap["3"]!!)
                four -> play(mSoundMap["4"]!!)
                five -> play(mSoundMap["5"]!!)
                six -> play(mSoundMap["6"]!!)
                seven -> play(mSoundMap["7"]!!)
                eight -> play(mSoundMap["8"]!!)
                nine -> play(mSoundMap["9"]!!)
                yuan -> play(mSoundMap["a_yuan"]!!)
                shi -> play(mSoundMap["a_shi"]!!)
                bai -> play(mSoundMap["a_bai"]!!)
                qian -> play(mSoundMap["a_qian"]!!)
                wan -> play(mSoundMap["a_wan"]!!)
                yi -> play(mSoundMap["tts_ten_million"]!!)
                dot -> play(mSoundMap["a_dot"]!!)
                else -> {
                }
            }
            Thread.sleep(PLAY_SPEED)
        }
    }
}