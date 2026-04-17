package com.admob.inhouseads.ads

import aglibs.loading.skeleton.layout.SkeletonConstraintLayout
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admob.inhouseads.R
import com.admob.inhouseads.data.InHouseModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.lang.ref.WeakReference

object InterstitialInHouseAds {

    private var TAG = "InterstitialInHouseAds_"
    private var isInhouseAdShowing = false
    private var isPurchased = false

    private var currentOverlayView: WeakReference<View>? = null

    private var waiting_dialog: Dialog? = null
    var rootView: WeakReference<ViewGroup>? = null


    fun setPurchased(isPurchased: Boolean = false) {
        this.isPurchased = isPurchased
    }

    fun isInHouseAdShowing(): Boolean {
        return isInhouseAdShowing
    }

    fun setisInHouseAdShowing(isInhouseAdShowing: Boolean) {
        this.isInhouseAdShowing = isInhouseAdShowing
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("StaticFieldLeak")
    fun Activity.showInHouseAd(
        inHouseAdModel: InHouseModel?,
        defaultAdModel: InHouseModel?,
        onAdDissmissed: () -> Unit = {},
        event: (String) -> Unit = {}
    ) {

        var successinHouseAdModel = inHouseAdModel

        if (successinHouseAdModel == null || successinHouseAdModel.destination_url == "") {
            successinHouseAdModel = defaultAdModel
        }

        if (isPurchased) {
            onAdDissmissed.invoke()
            return
        }

        if (successinHouseAdModel?.use_splash_back_fill == false) {
            onAdDissmissed.invoke()
            return
        }

        if (successinHouseAdModel?.destination_url == "") {
            onAdDissmissed.invoke()
            return
        }

        if (isPurchased || isInhouseAdShowing) return
        isInhouseAdShowing = true


        val overlayView = successinHouseAdModel?.ad_type?.let { returnViewType(it) }
        currentOverlayView = WeakReference(overlayView)
        successinHouseAdModel?.cross_position?.let {
            successinHouseAdModel?.cross_timer?.let { crossTimer ->
                initCloseLayout(
                    overlayView,
                    it,
                    crossTimer
                )
            }
        }

        overlayView?.alpha = 0f

        try {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }


        showWaitingDialog()


        Handler(Looper.getMainLooper()).postDelayed({
            event.invoke("SIBF_onCreate")
            event.invoke("${successinHouseAdModel?.destination_app}_SIBF")
            hideWaitingDialog()

            val overlayView = successinHouseAdModel?.ad_type?.let { returnViewType(it) }
            currentOverlayView = WeakReference(overlayView)
            successinHouseAdModel?.cross_position?.let {
                successinHouseAdModel?.cross_timer?.let { crossTimer ->
                    initCloseLayout(
                        overlayView,
                        it,
                        crossTimer
                    )
                }
            }

            overlayView?.alpha = 0f

            rootView = WeakReference(window.decorView as ViewGroup)
            rootView?.get()?.addView(overlayView)

            if (overlayView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(overlayView) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                    )
                    insets
                }
                ViewCompat.requestApplyInsets(overlayView)
            }

            overlayView?.post {
                overlayView.translationY = overlayView.height.toFloat()

                overlayView.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }

            successinHouseAdModel?.let { setupTexts(overlayView, it) }

            val closeBtnLeft = overlayView?.findViewById<ImageView>(R.id.leftcloseBtn)
            val closeBtnRight = overlayView?.findViewById<ImageView>(R.id.rightcloseBtn)
            val mainContainer = overlayView?.findViewById<ConstraintLayout>(R.id.mainContainer)



            if (successinHouseAdModel?.ad_type != "4") {
                val installBtn = overlayView?.findViewById<ConstraintLayout>(R.id.installBtn)

                installBtn?.setOnClickListener {
                    event.invoke("SIBF_onClick")
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                successinHouseAdModel.destination_url.toUri()
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }

                val skeletonLayout =
                    overlayView?.findViewById<SkeletonConstraintLayout>(R.id.skeletonLayout)

                val appIcon = overlayView?.findViewById<ImageView>(R.id.appIcon)

                appIcon?.post {
                    skeletonLayout?.startLoading()
                    skeletonLayout?.visibility = View.VISIBLE
                    appIcon.visibility = View.INVISIBLE

                    if (successinHouseAdModel.app_icon != "") {
                        skeletonLayout?.let { loadImage(appIcon, it, successinHouseAdModel) }
                    } else {
                        skeletonLayout?.stopLoading()
                        skeletonLayout?.visibility = View.GONE
                        appIcon.visibility = View.VISIBLE
                        appIcon.setImageResource(R.drawable.iha_iconholder)

                    }
                }

            } else {
                val installBtn = overlayView?.findViewById<TextView>(R.id.installBtn)
                val laterBtn = overlayView?.findViewById<TextView>(R.id.laterBtn)

                installBtn?.setOnClickListener {
                    event.invoke("SIBF_onClick")
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                successinHouseAdModel.destination_url.toUri()
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }

                laterBtn?.setOnClickListener {
                    installBtn?.performClick()
                }
            }



            closeBtnLeft?.setOnClickListener {
                event.invoke("SIBF_onDismiss")
                onAdDissmissed.invoke()
                rootView?.get()?.removeView(overlayView)
                isInhouseAdShowing = false
            }


            mainContainer?.setOnClickListener {
                event.invoke("SIBF_onClick")
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            successinHouseAdModel.destination_url.toUri()
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            closeBtnRight?.setOnClickListener {
                event.invoke("SIBF_onDismiss")
                onAdDissmissed.invoke()
                rootView?.get()?.removeView(overlayView)
                isInhouseAdShowing = false
            }

        }, 1200)


    }

    private fun setupTexts(overlayView: View?, inHouseAdModel: InHouseModel) {
        val titleText = overlayView?.findViewById<TextView>(R.id.titleText)
        val headlineText = overlayView?.findViewById<TextView>(R.id.headlineText)
        val benifitOneText = overlayView?.findViewById<TextView>(R.id.benifitOneText)
        val benifitTwoText = overlayView?.findViewById<TextView>(R.id.benifitTwoText)

        titleText?.text = inHouseAdModel.title.take(30)
        headlineText?.text = inHouseAdModel.headline.take(60)
        benifitOneText?.text = inHouseAdModel.benifit_1.take(30)
        benifitTwoText?.text = inHouseAdModel.benifit_2.take(30)

    }

    @SuppressLint("InflateParams")
    private fun Activity.returnViewType(adtype: String): View? {

        val inflater = LayoutInflater.from(this)
        return when (adtype) {

            "1" -> inflater.inflate(R.layout.iha_ad_layout_two, null)
            "2" -> inflater.inflate(R.layout.iha_ad_layout_three, null)
            "3" -> inflater.inflate(R.layout.iha_ad_layout_four, null)
            "4" -> inflater.inflate(R.layout.iha_ad_layout_one, null)
            "5" -> inflater.inflate(R.layout.iha_ad_layout_default, null)

            else -> inflater.inflate(R.layout.iha_ad_layout_three, null)
        }

    }


    private fun initCloseLayout(view: View?, crossPosition: String, crossTimer: Long) {

        val rightclosebtnLayout = view?.findViewById<ConstraintLayout>(R.id.rightclosebtnLayout)
        val leftclosebtnLayout = view?.findViewById<ConstraintLayout>(R.id.leftclosebtnLayout)

        val closeBtnLeft = view?.findViewById<ImageView>(R.id.leftcloseBtn)
        val closeBtnRight = view?.findViewById<ImageView>(R.id.rightcloseBtn)

        if (crossPosition == "left") {
            rightclosebtnLayout?.visibility = View.GONE
            leftclosebtnLayout?.visibility = View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({
                closeBtnLeft?.visibility = View.VISIBLE
            }, crossTimer * 1000)

        } else {
            rightclosebtnLayout?.visibility = View.VISIBLE
            leftclosebtnLayout?.visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({
                closeBtnRight?.visibility = View.VISIBLE
            }, crossTimer * 1000)
        }


    }


    fun Activity.dismissInHouseAd(onAdDismissed: () -> Unit = {}) {
        currentOverlayView?.get()?.let { view ->
            val rootView = window.decorView as ViewGroup

            view.animate()
                .translationY(view.height.toFloat())
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    rootView.removeView(view)
                    currentOverlayView = null
                    isInhouseAdShowing = false
                    onAdDismissed.invoke()
                }
                .start()
        }
    }

    @Suppress("DEPRECATION")
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    private fun Activity.showWaitingDialog() {
        waiting_dialog = Dialog(this)
        waiting_dialog?.setContentView(R.layout.iha_inter_ad_loading_dialog)

        waiting_dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        waiting_dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        waiting_dialog?.setCancelable(false)
        waiting_dialog?.show()
    }

    private fun hideWaitingDialog() {
        try {
            waiting_dialog?.dismiss()
            waiting_dialog = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun Activity.loadImage(
        imageView: ImageView,
        skeletonLayout: SkeletonConstraintLayout,
        inHouseAdModel: InHouseModel
    ) {

        Glide.with(imageView)
            .load(inHouseAdModel.app_icon)
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {

                    Log.d(TAG, "FAILED: ${e?.message}")

                    runOnUiThread {
                        skeletonLayout.stopLoading()
                        skeletonLayout.visibility = View.GONE
                        imageView.visibility = View.VISIBLE
                        imageView.setImageResource(R.drawable.iha_iconholder)
                    }

                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {

                    Log.d(TAG, "SUCCESS")

                    runOnUiThread {
                        skeletonLayout.stopLoading()
                        skeletonLayout.visibility = View.GONE
                        imageView.visibility = View.VISIBLE
                    }

                    return false
                }

            })
            .into(imageView)
    }


}