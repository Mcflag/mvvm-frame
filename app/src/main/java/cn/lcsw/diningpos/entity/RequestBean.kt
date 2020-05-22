package cn.lcsw.diningpos.entity

open class BaseRequest(
    var merchant_no: String? = null,
    var terminal_no: String? = null,
    var trace_no: String? = null,
    var terminal_time: String? = null,
    var key_sign: String? = null
)

data class InitRequest(
    var trace_no: String? = null,
    var terminal_mac: String? = null,
    var terminal_brand: String? = null,
    var terminal_model: String? = null,
    var terminal_time: String? = null,
    var terminal_ver: String? = null,
    var device_mac: String? = null,
    var device_type: String? = null,
    var app_verno: String? = null,
    var app_name: String? = null,
    var app_package: String? = null
)

data class InitNewRequest(
    var version: String? = null,
    var inst_no: String? = null,
    var terminal_mac: String? = null,
    var terminal_model: String? = null,
    var serialnum: String? = null,
    var trace_no: String? = null,
    var key_sign: String? = null
)

data class BarCodePayRequest(
    var pay_ver: String = "100",
    var service_id: String? = null,
    var pay_type: String? = null,
    var merchant_no: String? = null,
    var terminal_id: String? = null,
    var terminal_trace: String? = null,
    var terminal_time: String? = null,
    var operator_id: String? = null,
    var key_sign: String? = null,
    var auth_no: String? = null,
    var total_fee: String? = null,
    var order_body: String? = null,
    var attach: String? = null,
    var terminal_params: String? = null,
    var open_id: String? = null,
    var sub_appid: String? = null,
    var out_trade_no: String? = null
)

data class AuthInfoRequest(
    var rawdata: String? = null,
    var pay_ver: String? = null,
    var pay_type: String? = null
) : BaseRequest()

data class PayQueryRequest(
    val pay_ver: String = "100",
    val service_id: String = "020",
    var pay_type: String? = null,
    var merchant_no: String? = null,
    var terminal_id: String? = null,
    var terminal_trace: String? = null,
    var terminal_time: String? = null,
    var operator_id: String? = null,
    var key_sign: String? = null,
    var out_trade_no: String? = null,
    var pay_time: String? = null,
    var pay_trace: String? = null
)

data class QuerySumRequest(
    var begin_time: String? = null,
    var end_time: String? = null,
    var pay_type: String? = "0",
    var pay_status_code: String? = null,
    var current_pageid: String = "0",
    var page_size: String? = "10"
) : BaseRequest()

data class RefundRequest(
    val pay_ver: String = "100",
    val service_id: String = "030",
    var pay_type: String? = "000",
    var merchant_no: String? = null,
    var terminal_id: String? = null,
    var terminal_trace: String? = null,
    var terminal_time: String? = null,
    var refund_fee: String? = null,
    var key_sign: String? = null,
    var out_trade_no: String? = null,
    var pay_time: String? = null,
    var pay_trace: String? = null
)