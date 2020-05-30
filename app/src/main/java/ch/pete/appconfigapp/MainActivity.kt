package ch.pete.appconfigapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import ch.pete.appconfigapp.centralconfig.CentralConfigFragment
import timber.log.Timber


class MainActivity :
    AppCompatActivity(),
    FragmentManager.OnBackStackChangedListener,
    MainActivityView {
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.view = this
        viewModel.init()

        supportFragmentManager.addOnBackStackChangedListener(this)
        shouldDisplayHomeUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val canGoBack = supportFragmentManager.backStackEntryCount > 0
        // only visible on main screen
        menu?.findItem(R.id.centralConfig)?.isVisible = !canGoBack
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.centralConfig -> {
                viewModel.onMenuCentralConfig()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackStackChanged() {
        invalidateOptionsMenu()
        shouldDisplayHomeUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        //This method is called when the up button is pressed. Just the pop back stack.
        supportFragmentManager.popBackStack()
        return true
    }

    private fun shouldDisplayHomeUp() {
        // Enable Up button only if there are entries in the back stack
        val canGoBack = supportFragmentManager.backStackEntryCount > 0
        supportActionBar?.setDisplayHomeAsUpEnabled(canGoBack)
    }

    override fun showCentralConfig() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = CentralConfigFragment()
        fragmentTransaction
            .replace(
                R.id.fragmentContainer,
                fragment
            )

        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}
