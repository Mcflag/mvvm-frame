package cn.lcsw.diningpos.ui.setting

import android.content.Intent
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.databinding.ActivityPayStyleBinding
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.widget.CustomDialogFragment
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_pay_style.*
import kotlinx.android.synthetic.main.layout_top.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class PayStyleActivity : BaseActivity<ActivityPayStyleBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
    }

    override val layoutId: Int = R.layout.activity_pay_style

    val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner
    private var mDeleteAllDialog: CustomDialogFragment = CustomDialogFragment()

    override fun initView() {
        scanner = ToolScanner()
        initScanListener()

        top_back.setOnClickListener { finish() }
        top_title.text = "收款方式设置"

        when (prefs.payStyle) {
            "0" -> showScan()
            "1" -> showQr()
        }
    }

    fun showScan() {
        scan_text.setTextColor(resources.getColor(R.color.colorAccent))
        scan_check.visibility = View.VISIBLE
        qr_text.setTextColor(resources.getColor(R.color.color_33))
        qr_check.visibility = View.GONE
    }

    fun showQr() {
        scan_text.setTextColor(resources.getColor(R.color.color_33))
        scan_check.visibility = View.GONE
        qr_text.setTextColor(resources.getColor(R.color.colorAccent))
        qr_check.visibility = View.VISIBLE
    }

    @Suppress("DEPRECATION")
    fun chooseScan() {
        if (prefs.payStyle == "0") return
        mDeleteAllDialog.putTitle("确定切换模式？")
            .putPositiveText("确定")
            .putNegativeText("取消")
            .setNoticeDialogListener(object : CustomDialogFragment.NoticeDialogListener {
                override fun onDialogPositiveClick(dialog: CustomDialogFragment) {
                    prefs.payStyle = "0"
                    showScan()
                    dialog.dismissAllowingStateLoss()
                }

                override fun onDialogNegativeClick(dialog: CustomDialogFragment) {
                    dialog.dismissAllowingStateLoss()
                }

                override fun onDialogNetualClick(dialog: CustomDialogFragment) {

                }
            })
        if (!mDeleteAllDialog.isVisible()) {
            mDeleteAllDialog.showAllowingStateLoss(this.supportFragmentManager, "PayStyleDialog")
        }
    }

    @Suppress("DEPRECATION")
    fun chooseQr() {
        if (prefs.payStyle == "1") return
        mDeleteAllDialog.putTitle("确定切换模式？")
            .putPositiveText("确定")
            .putNegativeText("取消")
            .setNoticeDialogListener(object : CustomDialogFragment.NoticeDialogListener {
                override fun onDialogPositiveClick(dialog: CustomDialogFragment) {
                    prefs.payStyle = "1"
                    showQr()
                    dialog.dismissAllowingStateLoss()
                }

                override fun onDialogNegativeClick(dialog: CustomDialogFragment) {
                    dialog.dismissAllowingStateLoss()
                }

                override fun onDialogNetualClick(dialog: CustomDialogFragment) {

                }
            })
        if (!mDeleteAllDialog.isVisible()) {
            mDeleteAllDialog.showAllowingStateLoss(this.supportFragmentManager, "PayStyleDialog")
        }
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onDelPress() {
                super.onDelPress()
                this@PayStyleActivity.finish()
            }
        })
    }

    companion object {
        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, PayStyleActivity::class.java))
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun onResume(){
        super.onResume()
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
}