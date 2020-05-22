package cn.lcsw.diningpos.manager

import android.content.SharedPreferences
import cn.lcsw.diningpos.entity.*
import cn.lcsw.mvvm.util.prefs.gson
import cn.lcsw.mvvm.util.prefs.long
import cn.lcsw.mvvm.util.prefs.string

class PrefsHelper(prefs: SharedPreferences) {
    var initCache by prefs.gson(InitResult(), "init")
    var initNewCache by prefs.gson(InitNewResult(), "initNew")
    var authCache by prefs.gson(AuthInfoResult(), "auth")
    var payResult by prefs.gson(BarCodePayResult(), "payResult")
    var queryResult by prefs.gson(PayQueryResult(), "queryResult")
    var signResult by prefs.gson(SignResult(), "wxsign")
    var signExpire by prefs.long("SIGN_EXPIRE_TIME", 0)
    var uuid by prefs.string("SIGN_UUID", "")
    var timeStamp by prefs.long("SIGN_TIMESTAMP", 0)
    var beginTime by prefs.string("BEGIN_TIME", "")
    var endTime by prefs.string("END_TIME", "")

    var payStyle by prefs.string("PAY_STYLE", "0") //默认扫码支付
    var total by prefs.string("TOTAL_FEE", "")

    var outTradeNo by prefs.string("OUT_TRADE_NO", "")
    var channelTradeNo by prefs.string("CHANNEL_TRADE_NO", "")
}