package com.codenzi.payday

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ParallaxPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        page.apply {
            // Gerekli view'ları ID'leri ile buluyoruz
            val imageView = findViewById<View>(R.id.intro_image)
            val titleView = findViewById<View>(R.id.intro_title)
            val descriptionView = findViewById<View>(R.id.intro_description)

            // pozisyon < -1 ise (ekranın çok solunda)
            // -1 <= pozisyon <= 1 ise (ekranda veya geçiş anında)
            // pozisyon > 1 ise (ekranın çok sağında)
            if (position >= -1 && position <= 1) {
                // Görseli daha hızlı hareket ettirerek derinlik kat
                imageView?.translationX = -position * (width / 1.5f)

                // Başlığı biraz daha yavaş hareket ettir
                titleView?.translationX = -position * (width / 2f)

                // Açıklamayı en yavaş hareket ettir
                descriptionView?.translationX = -position * (width / 3f)

                // Sayfa kenarlara yaklaştıkça yavaşça kaybolsun (fade out)
                alpha = 1.0f - abs(position)
            } else {
                // Ekran dışındaki sayfaları tamamen görünmez yap
                alpha = 0.0f
            }
        }
    }
}