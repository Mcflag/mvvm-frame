package cn.lcsw.diningpos.utils

import cn.lcsw.diningpos.base.BaseApplication
import cn.lcsw.mvvm.ext.toast

inline fun toast(value: () -> String): Unit =
    BaseApplication.INSTANCE.toast(value)