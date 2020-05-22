package cn.lcsw.mvvm.binding.support

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import cn.lcsw.mvvm.functional.Consumer

interface ToolbarMenuClickListener : Consumer<MenuItem>

@BindingAdapter("bind_menuClick")
fun onToolbarMenuClick(
    toolbar: Toolbar,
    consumer: ToolbarMenuClickListener
) =
    toolbar.setOnMenuItemClickListener {
        consumer.accept(it)
        true
    }