package com.admob.inhouseads.views

import aglibs.loading.skeleton.layout.SkeletonConstraintLayout
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.admob.inhouseads.R
import com.admob.inhouseads.data.ToolbarHouseAdModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener


class InHouseAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val adCard: CardView
    private val icon: ImageView
    private val attrIcon: ImageView
    private val skeleton: SkeletonConstraintLayout

    init {

        LayoutInflater.from(context).inflate(R.layout.iha_toolbar_inhouse_ad, this, true)

        adCard = findViewById(R.id.adHouseCard)
        icon = findViewById(R.id.adHouseIconHolder)
        attrIcon = findViewById(R.id.inhouseAdAttr)
        skeleton = findViewById(R.id.skeletonLayout)
    }

    fun setAd(
        toobarHouseAdModel: ToolbarHouseAdModel?,
        defaulttoobarHouseAdModel: ToolbarHouseAdModel?,
        event: (String) -> Unit = {}
    ) {


        var model = toobarHouseAdModel
        if (model == null || model.destination_url == "") {
            model = defaulttoobarHouseAdModel
        }

        if (model?.show_house_ad == false) {
            adCard.visibility = GONE
            return
        }

        event.invoke("${model?.destination_app}_icon")

        adCard.visibility = VISIBLE
        attrIcon.visibility = VISIBLE

        skeleton.visibility = VISIBLE
        icon.visibility = INVISIBLE

        if (model?.app_icon?.isNotEmpty() == true) {

            Glide.with(context)
                .load(model.app_icon)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        showFallback()
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        showImage()
                        return false
                    }

                })
                .into(icon)

        } else {
            showFallback()
        }

        adCard.setOnClickListener {
            try {
                event.invoke("icon_onClick")
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(model?.destination_url))
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showImage() {
        skeleton.stopLoading()
        skeleton.visibility = GONE
        icon.visibility = VISIBLE
    }

    private fun showFallback() {
        skeleton.stopLoading()
        skeleton.visibility = GONE
        icon.visibility = VISIBLE
        icon.setImageResource(R.drawable.iha_iconholder)
    }
}