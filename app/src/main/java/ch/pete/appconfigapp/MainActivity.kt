package ch.pete.appconfigapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import ch.pete.appconfigapp.externalconfiglocation.ExternalConfigLocationFragment
import com.google.android.material.snackbar.Snackbar
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
        menu?.findItem(R.id.externalConfigLocation)?.isVisible = !canGoBack
        menu?.findItem(R.id.sync)?.isVisible = !canGoBack
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.externalConfigLocation -> {
                viewModel.onMenuExternalConfigLocation()
                true
            }
            R.id.sync -> {
                viewModel.onMenuSync()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackStackChanged() {
        invalidateOptionsMenu()
        shouldDisplayHomeUp()
        val titleFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? TitleFragment
        titleFragment?.title?.let {
            supportActionBar?.title = it
        }
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

    override fun showExternalConfigLocation() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = ExternalConfigLocationFragment()
        fragmentTransaction
            .replace(
                R.id.fragmentContainer,
                fragment
            )

        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun showSnackbar(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message, Snackbar.LENGTH_LONG
        ).show()
    }
}
