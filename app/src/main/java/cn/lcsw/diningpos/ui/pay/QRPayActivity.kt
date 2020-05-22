package cn.lcsw.diningpos.ui.pay

import android.content.Intent
import android.graphics.Bitmap
import android.os.RemoteException
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivityQrpayBinding
import cn.lcsw.diningpos.entity.AuthInfoResult
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.ui.result.ResultActivity
import cn.lcsw.diningpos.utils.ClickUtil
import cn.lcsw.diningpos.utils.KeySign
import cn.lcsw.diningpos.utils.UnitUtils
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.uber.autodispose.autoDisposable
import kotlinx.android.synthetic.main.activity_qrpay.*
import kotlinx.android.synthetic.main.layout_top.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class QRPayActivity : BaseActivity<ActivityQrpayBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(payKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_qrpay

    private val viewModel: QRPayViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()

    val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner

    var terminalTrace: String? = null
    var terminalTime: String? = null
    var totalFee: String = "0"
    var isPaySuccess: Boolean = false

    override fun initView() {
        totalFee = prefs.total
        money.text = "￥${UnitUtils.fen2Yuan(totalFee.toLong())}"
        scanner = ToolScanner()
        initScanListener()
        top_back.setOnClickListener { finish() }
        top_divider.visibility = View.INVISIBLE
        top_title.text = "二维码支付"
        face_pay.setOnClickListener { startFacePay() }
        qrPay()

        viewModel.loadingLayout.toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
                Toast.makeText(this@QRPayActivity, it, Toast.LENGTH_SHORT).show()
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.authInfo.toReactiveStream()
            .doOnNext {
                if (totalFee.isNotEmpty() && totalFee != "0") {
                    facePay(it, totalFee)
                } else {
                    Toast.makeText(this@QRPayActivity, "输入金额有误：\" $totalFee \"，请重新输入", Toast.LENGTH_SHORT).show()
                }
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.facePay.toReactiveStream()
            .doOnNext {
                if ("01" == it.return_code && "01" == it.result_code) {
                    isPaySuccess = true

                } else {

                }
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.barcodePay.toReactiveStream()
            .doOnNext {
                if ("01" == it.return_code && "01" == it.result_code) {
                    it.qr_url?.let { url ->
                        var baos = ByteArrayOutputStream()
                        encodeAsBitmap(url)?.let { bitmap ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                            var bytes: ByteArray = baos.toByteArray()
                            Glide.with(this).load(bytes).into(qr_code)
                        }
                    }
                } else {
                    Toast.makeText(this@QRPayActivity, "获取支付二维码失败", Toast.LENGTH_SHORT).show()
                }
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.queryResult.toReactiveStream()
            .doOnNext {
                isPaySuccess = true
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.noResult.toReactiveStream()
            .doOnNext {
            }
            .autoDisposable(scopeProvider)
            .subscribe()
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String) {
                startFacePay()
            }

            override fun onDelPress() {
                super.onDelPress()
                this@QRPayActivity.finish()
            }
        })
    }

    private fun encodeAsBitmap(url: String): Bitmap? {
        var bitmap: Bitmap? = null
        val result: BitMatrix
        val multiFormatWriter = MultiFormatWriter()
        try {
            result = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200)
            val barcodeEncoder = BarcodeEncoder()
            bitmap = barcodeEncoder.createBitmap(result)
        } catch (e: WriterException) {
            e.printStackTrace()
        } catch (iae: IllegalArgumentException) {
            return null
        }
        return bitmap
    }

    private fun qrPay() {
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        terminalTime = formatter.format(Date(System.currentTimeMillis()))
        terminalTrace = KeySign.getRandomUUID()
        viewModel.qrPay(totalFee, terminalTrace ?: "", terminalTime ?: "", "")
    }

    fun startFacePay() {
        if (ClickUtil.isFastClick()) {

        } else {
//            Toast.makeText(this@QRPayActivity, "请勿频繁操作", Toast.LENGTH_SHORT).show()
        }
    }

    private fun facePay(auth: AuthInfoResult, totalFee: String) {
    }

    companion object {
        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, QRPayActivity::class.java))
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
}