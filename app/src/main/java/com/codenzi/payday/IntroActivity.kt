package com.codenzi.payday

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.codenzi.payday.databinding.ActivityIntroBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var repository: PaydayRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = PaydayRepository(this)

        val adapter = IntroPagerAdapter(this)
        binding.introViewPager.adapter = adapter

        TabLayoutMediator(binding.tabIndicator, binding.introViewPager) { _, _ -> }.attach()

        binding.introViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == adapter.itemCount - 1) {
                    binding.nextButton.text = getString(R.string.onboarding_finish)
                    binding.skipButton.visibility = View.GONE
                } else {
                    binding.nextButton.text = getString(R.string.onboarding_next)
                    binding.skipButton.visibility = View.VISIBLE
                }
            }
        })

        binding.nextButton.setOnClickListener {
            if (binding.introViewPager.currentItem == adapter.itemCount - 1) {
                finishOnboarding()
            } else {
                binding.introViewPager.currentItem += 1
            }
        }

        binding.skipButton.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        lifecycleScope.launch {
            repository.setIntroShown(true)
            startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
            finish()
        }
    }

    private class IntroPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return IntroPageFragment.newInstance(position)
        }
    }
}