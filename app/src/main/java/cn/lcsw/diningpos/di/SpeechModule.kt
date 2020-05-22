package cn.lcsw.diningpos.di

import cn.lcsw.diningpos.base.BaseApplication
import cn.lcsw.diningpos.function.speech.SimpleSpeechSynthesis
import cn.lcsw.diningpos.utils.AudioMngHelper
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

private const val SERVICE_MODULE_TAG = "speechModule"

val speechModule = Kodein.Module(SERVICE_MODULE_TAG) {

//    bind<TextToSpeech>() with singleton {
//        TextToSpeech(BaseApplication.INSTANCE, TextToSpeech.OnInitListener {  })
//    }
    bind<SimpleSpeechSynthesis>() with singleton {
        SimpleSpeechSynthesis(BaseApplication.INSTANCE)
    }

    bind<AudioMngHelper>() with singleton {
        AudioMngHelper(BaseApplication.INSTANCE)
    }
}