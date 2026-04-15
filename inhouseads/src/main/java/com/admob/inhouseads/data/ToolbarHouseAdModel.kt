package com.admob.inhouseads.data

import androidx.annotation.Keep

@Keep
data class ToolbarHouseAdModel(

    var show_house_ad: Boolean = true,
    var destination_app: String = "",
    var destination_url: String = "",
    var app_icon: String = "",

    )
