package cn.lcsw.diningpos.ui.trade_query

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.databinding.ActivityTradeEndTimeBinding
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.utils.AppManager
import cn.lcsw.diningpos.utils.ClickUtil
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import com.blankj.utilcode.util.ToastUtils
import kotlinx.android.synthetic.main.activity_trade_end_time.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class EndTimeActivity : BaseActivity<ActivityTradeEndTimeBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
    }

    override val layoutId: Int = R.layout.activity_trade_end_time

    val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner

    var message = "输入时间错误"

    override fun initView() {
        AppManager.getAppManager().addActivity(this)
        scanner = ToolScanner()
        scanner.setIsAiboshi(true)
        scanner.setNumberInput(true)
        initScanListener()

        val s2 = SpannableString("按【确认】键默认为当前时间")
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            0,
            1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red)),
            1,
            5,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            5,
            s2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        notice.text = s2
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String) {
                if (ClickUtil.isFastClick()) {
                    prefs.endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Timestamp(System.currentTimeMillis()))
                    SumDetailActivity.launch(this@EndTimeActivity, false)
                }
            }

            override fun onF2Press() {
                super.onF2Press()
                if (ClickUtil.isFastClick()) {
                    AppManager.getAppManager().finishAllActivity()
                    finish()
                }
            }

            override fun onTyping(barcode: String, nowKeyName: String) {
                setTime(barcode)
            }

        })
    }

    private fun setTime(barcode: String) {
        when (barcode.length) {
            0 -> {
                tv_year.text = ""
                tv_month.text = ""
                tv_day.text = ""
                tv_hour.text = ""
                tv_minute.text = ""
                tv_second.text = ""
            }
            in 1..4 -> {
                tv_year.text = barcode
                tv_month.text = ""
                tv_day.text = ""
                tv_hour.text = ""
                tv_minute.text = ""
                tv_second.text = ""
                if (barcode.length == 4 && !checkYear()) {
                    ToastUtils.showShort("请输入正确的年份")
                    scanner.clear()
                    tv_year.text = ""
                }
            }
            in 5..6 -> {
                tv_year.text = barcode.substring(0, 4)
                tv_month.text = barcode.substring(4, barcode.length)
                tv_day.text = ""
                tv_hour.text = ""
                tv_minute.text = ""
                tv_second.text = ""
                if(barcode.length == 6 && !checkMonth()){
                    ToastUtils.showShort("请输入正确的月份")
                    scanner.delete(2)
                    tv_month.text = ""
                }
            }
            in 7..8 -> {
                tv_year.text = barcode.substring(0, 4)
                tv_month.text = barcode.substring(4, 6)
                tv_day.text = barcode.substring(6, barcode.length)
                tv_hour.text = ""
                tv_minute.text = ""
                tv_second.text = ""
                if(barcode.length == 8 && !checkDay()){
                    ToastUtils.showShort("请输入正确的日期")
                    scanner.delete(2)
                    tv_day.text = ""
                }
            }
            in 9..10 -> {
                tv_year.text = barcode.substring(0, 4)
                tv_month.text = barcode.substring(4, 6)
                tv_day.text = barcode.substring(6, 8)
                tv_hour.text = barcode.substring(8, barcode.length)
                tv_minute.text = ""
                tv_second.text = ""
                if(barcode.length == 10 && !checkHour()){
                    ToastUtils.showShort("请输入正确的小时")
                    scanner.delete(2)
                    tv_hour.text = ""
                }
            }
            in 11..12 -> {
                tv_year.text = barcode.substring(0, 4)
                tv_month.text = barcode.substring(4, 6)
                tv_day.text = barcode.substring(6, 8)
                tv_hour.text = barcode.substring(8, 10)
                tv_minute.text = barcode.substring(10, barcode.length)
                tv_second.text = ""
                if(barcode.length == 12 && !checkMinute()){
                    ToastUtils.showShort("请输入正确的分钟")
                    scanner.delete(2)
                    tv_minute.text = ""
                }
            }
            in 13..14 -> {
                tv_year.text = barcode.substring(0, 4)
                tv_month.text = barcode.substring(4, 6)
                tv_day.text = barcode.substring(6, 8)
                tv_hour.text = barcode.substring(8, 10)
                tv_minute.text = barcode.substring(10, 12)
                tv_second.text = barcode.substring(12, barcode.length)
                if(barcode.length == 14 && !checkSecond()){
                    ToastUtils.showShort("请输入正确的秒")
                    scanner.delete(2)
                    tv_second.text = ""
                }
            }
        }
        showUnderLine()
        if (barcode.length == 14 && tv_second.text.isNotEmpty()) {
            if (check(barcode)) {
                prefs.endTime = convert(barcode)
                SumDetailActivity.launch(this@EndTimeActivity, false)
            } else {
                ToastUtils.showShort(message)
                scanner.clear()
                tv_year.text = ""
                tv_month.text = ""
                tv_day.text = ""
                tv_hour.text = ""
                tv_minute.text = ""
                tv_second.text = ""
                showUnderLine()
            }
        }
    }

    companion object {
        fun launch(activity: AppCompatActivity) =
            activity.apply {
                val intent = Intent(this, EndTimeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        scanner.analysisKeyEvent(event)
        if (event.keyCode == KeyEvent.KEYCODE_MENU) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun showUnderLine() {
        if (tv_year.text.isNotEmpty()) {
            line_year.visibility = View.INVISIBLE
        } else {
            line_year.visibility = View.VISIBLE
        }
        if (tv_month.text.isNotEmpty()) {
            line_month.visibility = View.INVISIBLE
        } else {
            line_month.visibility = View.VISIBLE
        }
        if (tv_day.text.isNotEmpty()) {
            line_day.visibility = View.INVISIBLE
        } else {
            line_day.visibility = View.VISIBLE
        }
        if (tv_hour.text.isNotEmpty()) {
            line_hour.visibility = View.INVISIBLE
        } else {
            line_hour.visibility = View.VISIBLE
        }
        if (tv_minute.text.isNotEmpty()) {
            line_minute.visibility = View.INVISIBLE
        } else {
            line_minute.visibility = View.VISIBLE
        }
        if (tv_second.text.isNotEmpty()) {
            line_second.visibility = View.INVISIBLE
        } else {
            line_second.visibility = View.VISIBLE
        }
    }

    private fun checkYear(): Boolean {
        return tv_year.text.toString().toInt() in 1000..3000
    }

    private fun checkMonth(): Boolean {
        return tv_month.text.toString().toInt() in 1..12
    }

    private fun checkDay(): Boolean {
        var c = Calendar.getInstance()
        c.set(tv_year.text.toString().toInt(), tv_month.text.toString().toInt(), 0)
        var dayOfMonth = c.get(Calendar.DAY_OF_MONTH)
        return tv_day.text.toString().toInt() in 1..dayOfMonth
    }

    private fun checkHour():Boolean {
        return tv_hour.text.toString().toInt() <= 23
    }

    private fun checkMinute():Boolean {
        return tv_minute.text.toString().toInt() <= 59
    }

    private fun checkSecond():Boolean {
        return tv_second.text.toString().toInt() <= 59
    }

    private fun check(time: String): Boolean {
        var current = System.currentTimeMillis()
        var beginTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(prefs.beginTime).time
        var inputTime = SimpleDateFormat("yyyyMMddHHmmss").parse(time).time
        var onDay: Long = 24 * 60 * 60 * 1000
        var gap: Long = onDay * 31
        when {
            inputTime <= beginTime -> message = "输入时间错误，结束时间不能小于开始时间"
            inputTime > current -> message = "输入时间错误，结束时间不能大于当前时间"
        }
        if((inputTime - beginTime) > gap){
            message = "输入时间错误，结束时间与开始时间不能超过31天"
        }
        return (inputTime in (beginTime + 1) until current) && (inputTime - beginTime) < gap
    }

    private fun convert(time: String?): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
            SimpleDateFormat("yyyyMMddHHmmss").parse(time).time
        ).toString()
    }
}