package cn.lcsw.diningpos.function.speech

enum class PayType(
    val payTypeDesc: String,
    val payTypeSpeech: String,
    val commonCode: String,
    val recordCode: Int
) {
    ALL("全部", "", "000", 0),
    WECHAT("微信支付", "微信", "010", 1),
    ALIPAY("支付宝支付", "支付宝", "020", 2),
    BANKCARD("银行卡", "银行卡", "030", 3),
    CASH("现金", "现金", "040", 4),
    NOCARD("无卡支付", "无卡", "050", 5),
    QQ("QQ钱包", "QQ钱包", "060", 6),
    BAIDU("百度钱包", "百度钱包", "070", 7),
    JD("京东钱包", "京东", "080", 8),
    KOUBEI("口碑支付", "口碑", "090", 9),
    BESTPAY("翼支付", "翼支付", "100", 10),
    UNION("银联云闪付", "银联云闪付", "110", 11),
    DRAGON("龙支付", "龙支付", "120", 12),
    FENQI("分期支付", "分期", "130", 13),
    HEBAO("和包支付", "和包支付", "140", 14),
    QM_STORE("会员储值", "会员储值", "1140", 114),
    QM_PAY("储值余额支付", "储值余额支付", "1150", 115),
    QM_DIANCAN("点餐成功", "点餐成功", "1160", 116);

    companion object {
        fun forSpeechText(speechText: String): PayType? {
            try {
                for (type in values()) {
                    if (type.payTypeSpeech == speechText) {
                        return type
                    }
                }
                return null
            } catch (e: Exception) {
                return null
            }
        }
    }
}