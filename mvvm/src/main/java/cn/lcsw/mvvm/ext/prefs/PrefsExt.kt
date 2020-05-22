package cn.lcsw.mvvm.ext.prefs

import android.content.Context
import android.content.SharedPreferences
import cn.lcsw.mvvm.constant.SP_NAME_DEFAULT

fun Context.sharedPreferences(spName: String = SP_NAME_DEFAULT): SharedPreferences =
    getSharedPreferences(spName, Context.MODE_PRIVATE)