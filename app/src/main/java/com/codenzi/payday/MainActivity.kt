package com.codenzi.payday

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.codenzi.payday.databinding.ActivityMainBinding
import com.codenzi.payday.ui.budgets.BudgetsFragment
import com.codenzi.payday.ui.dashboard.DashboardFragment
import com.codenzi.payday.ui.transactions.TransactionsFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: PaydayViewModel by viewModels()
    private lateinit var repository: PaydayRepository
    private val gson = Gson()
    private val TAG = "PaydayBackup"
    private lateinit var googleDriveManager: GoogleDriveManager

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                showSnackbar(getString(R.string.welcome_message_user, account.displayName))
                // TODO: Handle post-sign-in logic, like checking for backups
            } catch (e: ApiException) {
                Log.w(TAG, "Sign-in failed, code: " + e.statusCode)
                showSnackbar(getString(R.string.google_sign_in_failed), isError = true)
            }
        }
    }

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // The ViewModel can observe the result if needed, or we can reload data here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = PaydayRepository(this)
        googleDriveManager = GoogleDriveManager(this)

        setSupportActionBar(binding.toolbar)
        setupViewPager()
        setupBottomNav()
        setupFab()
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = ViewPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 2
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> binding.viewPager.currentItem = 0
                R.id.navigation_transactions -> binding.viewPager.currentItem = 1
                R.id.navigation_budgets -> binding.viewPager.currentItem = 2
                R.id.navigation_accounts -> binding.viewPager.currentItem = 3
            }
            true
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavigation.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupFab() {
        binding.addTransactionFab.setOnClickListener {
            TransactionDialogFragment.newInstance(null).show(supportFragmentManager, TransactionDialogFragment.TAG)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_backup -> performActionWithSignIn(::backupData)
            R.id.action_restore -> performActionWithSignIn(::restoreData)
            R.id.action_achievements -> startActivity(Intent(this, AchievementsActivity::class.java))
            R.id.action_settings -> settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
            R.id.action_reports -> startActivity(Intent(this, ReportsActivity::class.java)) // TODO: Create new reports screen
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun performActionWithSignIn(action: () -> Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            action.invoke()
        } else {
            googleSignInLauncher.launch(GoogleDriveManager.getSignInIntent(this))
        }
    }

    private fun backupData() {
        lifecycleScope.launch {
            showSnackbar(getString(R.string.backup_started))
            val result = withContext(Dispatchers.IO) {
                try {
                    val backupData = repository.getAllDataForBackup()
                    val backupJson = gson.toJson(backupData)
                    googleDriveManager.uploadFileContent(backupJson)
                    repository.saveLastBackupTimestamp(System.currentTimeMillis())
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Error during backup!", e)
                    false
                }
            }
            if (result) {
                showSnackbar(getString(R.string.backup_success))
            } else {
                showSnackbar(getString(R.string.backup_failed), isError = true)
            }
        }
    }

    private fun restoreData() {
         lifecycleScope.launch {
            showSnackbar(getString(R.string.restore_started))
            val result = withContext(Dispatchers.IO) {
                try {
                    val backupJson = googleDriveManager.downloadFileContent()
                    if (backupJson != null) {
                        val backupData = gson.fromJson(backupJson, BackupData::class.java)
                        repository.restoreDataFromBackup(backupData)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during restore!", e)
                    false
                }
            }
            if (result) {
                showSnackbar(getString(R.string.restore_success))
                // TODO: Add a mechanism to notify fragments to reload data
            } else {
                showSnackbar(getString(R.string.restore_failed), isError = true)
            }
        }
    }

    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.red_500))
        }
        snackbar.show()
    }

    private inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DashboardFragment()
                1 -> TransactionsFragment()
                2 -> BudgetsFragment()
                3 -> AccountsFragment()
                else -> throw IllegalStateException("Invalid position: $position")
            }
        }
    }
}
