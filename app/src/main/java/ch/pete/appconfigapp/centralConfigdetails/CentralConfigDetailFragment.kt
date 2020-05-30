package ch.pete.appconfigapp.centralConfigdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.model.CentralConfig
import kotlinx.android.synthetic.main.fragment_central_config_detail.name
import kotlinx.android.synthetic.main.fragment_central_config_detail.url
import kotlinx.android.synthetic.main.fragment_central_config_detail.view.name
import kotlinx.android.synthetic.main.fragment_central_config_detail.view.url
import timber.log.Timber

class CentralConfigDetailFragment : Fragment() {
    companion object {
        const val ARG_CENTRAL_CONFIG_ID = "central_config_id"
    }

    private val viewModel: CentralConfigDetailViewModel by viewModels()

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
            val rootView = if (args.containsKey(ARG_CENTRAL_CONFIG_ID)) {
                val centralConfigId = args.getLong(ARG_CENTRAL_CONFIG_ID)
                val rootView =
                    inflater.inflate(R.layout.fragment_central_config_detail, container, false)

                loadData(centralConfigId, rootView)
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

    private fun loadData(centralConfigId: Long, rootView: View) {
        val liveData =
            viewModel.centralConfigByKeyValueId(centralConfigId)
        liveData.observe(viewLifecycleOwner, object : Observer<CentralConfig> {
            override fun onChanged(centralConfig: CentralConfig) {
                liveData.removeObserver(this)
                rootView.name.setText(centralConfig.name)
                rootView.url.setText(centralConfig.url)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        arguments?.getLong(ARG_CENTRAL_CONFIG_ID)?.let {
            viewModel.storeCentralConfig(
                name = name.text.toString(),
                url = url.text.toString(),
                id = it
            )
        } ?: Timber.e("Argument '$ARG_CENTRAL_CONFIG_ID' missing.")
    }
}
