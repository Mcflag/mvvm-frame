package cn.lcsw.diningpos.entity

import android.os.Parcel
import android.os.Parcelable

open class BaseResult(
    var return_code: String? = null,
    var result_code: String? = null,
    var return_msg: String? = null,
    var trace_no: String? = null,
    var key_sign: String? = null
)

data class InitResult(
    var app_verno: String? = null,
    var app_must_update: String? = null,
    var app_download_url: String? = null,
    var app_change_log: String? = null,
    var terminal_no: String? = null,
    var merchant_no: String? = null,
    var merchant_name: String? = null,
    var access_token: String? = null,
    var store_name: String? = null,
    var store_code: String? = null,
    var ali_mch_id: String? = null,
    var ali_pnr_id: String? = null,
    var ali_brand_code: String? = null,
    var ali_appid: String? = null,
    var wx_appid: String? = null,
    var wx_mchid: String? = null,
    var wx_sub_app_id: String? = null,
    var wx_sub_mch_id: String? = null
) : BaseResult()

data class InitNewResult(
    var terminal_id: String? = null,
    var merchant_no: String? = null,
    var merchant_name: String? = null,
    var store_name: String? = null,
    var store_no: String? = null,
    var bootup_voice: String? = null,
    var brand_name: String? = null,
    var is_oem_version: String? = null,
    var brand_logo_url: String? = null,
    var brand_voice_url: String? = null,
    var access_token: String? = null
) : BaseResult()

data class BarCodePayResult(
    var pay_type: String? = null,
    var merchant_name: String? = null,
    var merchant_no: String? = null,
    var terminal_id: String? = null,
    var terminal_trace: String? = null,
    var terminal_time: String? = null,
    var total_fee: String? = null,
    var end_time: String? = null,
    var out_trade_no: String? = null,
    var receipt_fee: String? = null,
    var user_id: String? = null,
    var qr_url: String? = null,
    var channel_trade_no: String? = null
) : BaseResult()

data class AuthInfoResult(
    var authinfo: String? = null,
    var expires_in: Int = 0,
    var out_trade_no: String? = null
) : BaseResult()

data class PayQueryResult(
    var pay_type: String? = null,
    var merchant_name: String? = null,
    var merchant_no: String? = null,
    var terminal_id: String? = null,
    var terminal_trace: String? = null,
    var terminal_time: String? = null,
    var total_fee: String? = null,
    var end_time: String? = null,
    var out_trade_no: String? = null,
    var receipt_fee: String? = null,
    val trade_state: String? = null,
    val pay_trace: String? = null,
    val pay_time: String? = null,
    var channel_trade_no: String? = null,
    val channel_order_no: String? = null,
    var user_id: String? = null,
    var attach: String? = null
) : BaseResult(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(pay_type)
        parcel.writeString(merchant_name)
        parcel.writeString(merchant_no)
        parcel.writeString(terminal_id)
        parcel.writeString(terminal_trace)
        parcel.writeString(terminal_time)
        parcel.writeString(total_fee)
        parcel.writeString(end_time)
        parcel.writeString(out_trade_no)
        parcel.writeString(receipt_fee)
        parcel.writeString(trade_state)
        parcel.writeString(pay_trace)
        parcel.writeString(pay_time)
        parcel.writeString(channel_trade_no)
        parcel.writeString(channel_order_no)
        parcel.writeString(user_id)
        parcel.writeString(attach)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PayQueryResult> {
        override fun createFromParcel(parcel: Parcel): PayQueryResult {
            return PayQueryResult(parcel)
        }

        override fun newArray(size: Int): Array<PayQueryResult?> {
            return arrayOfNulls(size)
        }
    }
}

data class SignResult(
    var mch_sign: String? = null,
    var serial_no: String? = null,
    var timestamp: String? = null,
    var nonce_str: String? = null,
    var appid: String? = null,
    var mch_id: String? = null,
    var sub_mch_id: String? = null
) : BaseResult()

data class QuerySumResult(
    var trade_sum: String? = null,//当前条件的总和
    var trade_num: String? = null,//当前条件的总和
    var total_size: String? = null//总数据条数
) : BaseResult()

data class RefundResult(
    var merchant_name: String? = null,
    var merchant_no: String? = null,
    var terminal_id: String? = null,
    var terminal_trace: String? = null,
    var terminal_time: String? = null,
    var refund_fee: String? = null,
    var end_time: String? = null,
    var out_trade_no: String? = null,
    var out_refund_no: String? = null
) : BaseResult()