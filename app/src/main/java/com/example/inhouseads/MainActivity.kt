package com.example.inhouseads

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admob.inhouseads.ads.InterstitialInHouseAds
import com.admob.inhouseads.ads.InterstitialInHouseAds.dismissInHouseAd
import com.admob.inhouseads.ads.InterstitialInHouseAds.showInHouseAd
import com.admob.inhouseads.data.InHouseModel
import com.example.inhouseads.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    var a = 1
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {
            if (InterstitialInHouseAds.isInHouseAdShowing()) {

                dismissInHouseAd {
                    onBackPressedDispatcher.onBackPressed()
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        binding.apply {

            showinterBtn.setOnClickListener {
                showInHouseAd(

                    InHouseModel(
                        title = "Smart Transfer Copy My Data",
                        headline = "Transfer your valuable data from an old phone to new one",
                        benifit_1 = "Data transfer without cables",
                        benifit_2 = "Easily data transfer across devices",
                        use_splash_back_fill = true,
                        app_icon = "https://play-lh.googleusercontent.com/EEuZGfK0ow_ssK9Rn8AEKHpdkDJ-a4te966wJudTka0oQS_tYrq3H8jttVN8cYeqxoZ4VFTLBUeffyFdp_ab=w240-h480-rw",
                        cross_position = "right",
                        destination_url = "https://play.google.com/store/apps/details?id=com.mydeviceinfo.phoneinfo&hl=en",
                        cross_timer = 0L,
                        ad_type = "$a"

                    ),

                    event = {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    }

                )
                if (a == 4)
                    a = 0
                a++

            }

        }

    }
}