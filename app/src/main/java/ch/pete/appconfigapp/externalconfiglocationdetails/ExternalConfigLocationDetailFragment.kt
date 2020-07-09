package ch.pete.appconfigapp.externalconfiglocationdetails

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.TitleFragment
import ch.pete.appconfigapp.model.ExternalConfigLocation
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.name
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.url
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.view.explanation
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.view.name
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.view.url
import timber.log.Timber

class ExternalConfigLocationDetailFragment :
    Fragment(), ExternalConfigLocationDetailView, TitleFragment {
    companion object {
        const val ARG_EXTERNAL_CONFIG_LOCATION_ID = "external_config_location_id"
    }

    private val viewModel: ExternalConfigLocationDetailViewModel by viewModels()
    override val title: String by lazy { getString(R.string.external_config_location_details) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.view = this
        viewModel.mainActivityViewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.init(arguments?.getLong(ARG_EXTERNAL_CONFIG_LOCATION_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView =
            inflater.inflate(
                R.layout.fragment_external_config_location_detail,
                container,
                false
            )
        rootView.explanation.movementMethod = LinkMovementMethod.getInstance()

        if (viewModel.initialised) {
            loadData(rootView)
        }
        return rootView
    }

    private fun loadData(rootView: View) {
        val liveData =
            viewModel.externalConfigLocation()
        liveData.observe(viewLifecycleOwner, object : Observer<ExternalConfigLocation> {
            override fun onChanged(externalConfigLocation: ExternalConfigLocation?) {
                liveData.removeObserver(this)

                if (externalConfigLocation != null) {
                    rootView.name.setText(externalConfigLocation.name)
                    rootView.url.setText(externalConfigLocation.url)
                } else {
                    Timber.w("externalConfigLocation is null, close fragment")
                    close()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        viewModel.storeExternalConfigLocation(
            name = name.text.toString(),
            url = url.text.toString()
        )
    }

    override fun close() {
        parentFragmentManager.popBackStack()
    }
}
