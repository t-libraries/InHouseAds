package com.admob.inhouseads.data

import androidx.annotation.Keep

@Keep
data class InHouseModel(

    var use_splash_back_fill: Boolean = true,
    var destination_app: String = "",
    var destination_url: String = "",
    var app_icon: String = "",
    var title: String = "",
    var headline: String = "",
    var benifit_1: String = "",
    var benifit_2: String = "",
    var cross_timer: Long = 0L,
    var cross_position: String = "",
    var backpress: Boolean = false,
    var ad_type: String = "1",

    )
