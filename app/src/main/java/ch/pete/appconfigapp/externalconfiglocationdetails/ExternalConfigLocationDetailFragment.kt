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
import ch.pete.appconfigapp.model.ExternalConfigLocation
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.name
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.url
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.view.explanation
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.view.name
import kotlinx.android.synthetic.main.fragment_external_config_location_detail.view.url
import timber.log.Timber

class ExternalConfigLocationDetailFragment : Fragment() {
    companion object {
        const val ARG_EXTERNAL_CONFIG_LOCATION_ID = "external_config_location_id"
    }

    private val viewModel: ExternalConfigLocationDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.mainActivityViewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = arguments?.let { args ->
            val rootView = if (args.containsKey(ARG_EXTERNAL_CONFIG_LOCATION_ID)) {
                val externalConfigLocationId = args.getLong(ARG_EXTERNAL_CONFIG_LOCATION_ID)
                val rootView =
                    inflater.inflate(
                        R.layout.fragment_external_config_location_detail,
                        container,
                        false
                    )
                rootView.explanation.movementMethod = LinkMovementMethod.getInstance()

                loadData(externalConfigLocationId, rootView)
                rootView
            } else {
                null
            }

            rootView
        }

        if (rootView == null) {
            parentFragmentManager.popBackStack()
        }
        return rootView
    }

    private fun loadData(externalConfigLocationId: Long, rootView: View) {
        val liveData =
            viewModel.externalConfigLocationById(externalConfigLocationId)
        liveData.observe(viewLifecycleOwner, object : Observer<ExternalConfigLocation> {
            override fun onChanged(externalConfigLocation: ExternalConfigLocation) {
                liveData.removeObserver(this)
                rootView.name.setText(externalConfigLocation.name)
                rootView.url.setText(externalConfigLocation.url)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        arguments?.getLong(ARG_EXTERNAL_CONFIG_LOCATION_ID)?.let {
            viewModel.storeExternalConfigLocation(
                name = name.text.toString(),
                url = url.text.toString(),
                id = it
            )
        } ?: Timber.e("Argument '$ARG_EXTERNAL_CONFIG_LOCATION_ID' missing.")
    }
}
