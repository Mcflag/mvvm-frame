package cn.lcsw.diningpos.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.String as String1

object TimeConverter {

    /**
     * 2019-03-25T14:50:46Z -> 2 days ago
     */
    fun transTimeAgo(time: String1?): String1 =
        transTimeStamp(time).let {
            DateUtils.getRelativeTimeSpanString(it).toString()
        }

    /**
     * 2019-03-25T14:50:46Z -> github time stamp
     */
    fun transTimeStamp(time: String1?): Long =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .let {
                it.timeZone = TimeZone.getTimeZone("GMT+1")
                it.parse(time).time
            }

    /**
     *  20191119155502   ->  2019/03/08 04:13:12
     */
    fun conver(time: kotlin.String?): kotlin.String {
        return SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
            SimpleDateFormat("yyyyMMddHHmmss").parse(time).time
        ).toString()
    }
}