package cn.lcsw.diningpos

import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import timber.log.Timber
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    var appid = "wxe568c727d466aef9"
    var mch_id = "211266759"
    var sub_mch_id = "260609682"
    var serial_no = "1D642AEDBF63EF899CEBFCB5D98CBF6C865D7AD4"
    var timestamp : Long = 1575508806
    var nonce_str = "079A2BE865FD4038A463F52E0D846CB9"
    var mch_sign = "g6743+jeimXUUL7bNPjT1a2xHx2JJak4W96zeCJunnHVVroYQxTJQSFgvBzOWShUWwXMbJ9Yw5uYAq7Zl5/2yvIcy64qFDz/TZrgZAtey/Tz/PnI8QWfKsBxjo+t+o2G1pUHmctEUqT5NPtMj89bV8u8LHzosTvjEM9BohLzQXvPSSA2MbHcQf1aX07frwnI/kZfUTk3VOeba4gfVqVsVRRn+B1PT2ADhcvIFz7jsc5AtRV2+mPp5ykLH5Q9zqOR9LGC4bbvc33zsHW+FvGUuryuOGn28PYXdyCSE9lY3D+cc2QozPqZbNAaksLuQGE5WyAmTOD13kxc3NhhCKx3NA=="
    var outTradeNo = "114963105121119120509213204506"
    var channelNo = "4200000438201912057592309720"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mm)
        val report = findViewById<Button>(R.id.report)
        report.setOnClickListener {
            initReport(appid, mch_id, sub_mch_id, serial_no, timestamp, nonce_str, mch_sign)
        }
    }

    override fun onResume() {
        super.onResume()
        initReport(appid, mch_id, sub_mch_id, serial_no, timestamp, nonce_str, mch_sign)
    }

    private fun initReport(
        appid: String,
        mchid: String,
        subMchid: String,
        serialNo: String,
        timestamp: Long,
        nonce: String,
        mchSign: String
    ) {
        val info2 = HashMap<String, Any>()
        info2["appid"] = appid
        info2["mch_id"] = mchid
        info2["sub_mch_id"] = subMchid
        info2["device_model"] = Build.MODEL
        if (!nonce.isNullOrEmpty()) {
            info2["nonce_str"] = nonce
        } else {
            info2["nonce_str"] = "1"
        }

        info2["timestamp"] = timestamp

        info2["device_category"] = 4
        info2["device_class"] = 102

        if (!serialNo.isNullOrEmpty()) {
            info2["serial_no"] = serialNo
        } else {
            info2["serial_no"] = "1"
        }

        if (!mchSign.isNullOrEmpty()) {
            info2["mch_sign"] = mchSign
        } else {
            info2["mch_sign"] = "1"
        }
    }

    private fun wxReportOrder() {
        val reportOrderMap = HashMap<String, Any>()
        reportOrderMap["transaction_id"] = channelNo
        reportOrderMap["out_trade_no"] = outTradeNo
        Timber.tag("cccccc").d("out_trade_no:" + outTradeNo + "transaction_id:" + channelNo)
    }
}