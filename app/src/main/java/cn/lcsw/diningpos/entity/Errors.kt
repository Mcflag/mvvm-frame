package cn.lcsw.diningpos.entity

sealed class Errors(msg: String) : Throwable(msg) {
    object NetworkError : Errors("")
    object EmptyInputError : Errors("")
    object EmptyResultsError : Errors("")
    object ReturnError : Errors("")
    object SingleError : Errors("")
    class ResultError(msg: String) : Errors(msg)
}