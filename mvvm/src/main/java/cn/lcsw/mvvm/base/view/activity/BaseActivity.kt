package cn.lcsw.mvvm.base.view.activity

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import cn.lcsw.mvvm.BR
import cn.lcsw.mvvm.R

abstract class BaseActivity<B : ViewDataBinding> : InjectionActivity() {

    protected lateinit var binding: B

    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        initBinding()
        initView()
    }

    open fun initView() {

    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, layoutId)
        with(binding) {
            setVariable(BR.activity, this@BaseActivity)
            setLifecycleOwner(this@BaseActivity)
        }
    }

    override fun onBackPressed() {
        //屏蔽返回键
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.hold, R.anim.fade_exit)
    }
}