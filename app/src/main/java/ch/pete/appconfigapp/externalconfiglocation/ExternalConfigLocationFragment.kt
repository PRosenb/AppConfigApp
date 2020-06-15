package ch.pete.appconfigapp.externalconfiglocation

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.TitleFragment
import ch.pete.appconfigapp.externalconfiglocationdetails.ExternalConfigLocationDetailFragment
import kotlinx.android.synthetic.main.fragment_external_config_location.empty
import kotlinx.android.synthetic.main.fragment_external_config_location.view.addExternalConfigLocationButton
import kotlinx.android.synthetic.main.fragment_external_config_location.view.emptyText
import kotlinx.android.synthetic.main.fragment_external_config_location.view.recyclerView

class ExternalConfigLocationFragment : Fragment(), ExternalConfigLocationView, TitleFragment {
    private val viewModel: ExternalConfigLocationViewModel by viewModels()
    override val title: String by lazy { getString(R.string.external_config_location) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.view = this
        viewModel.mainActivityViewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView =
            inflater.inflate(R.layout.fragment_external_config_location, container, false)

        initView(rootView)
        return rootView
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
    }

    private fun initView(rootView: View) {
        val externalConfigLocationAdapter = ExternalConfigLocationAdapter(
            onItemClickListener = {
                viewModel.onExternalConfigLocationEntryClicked(it)
            },
            onDeleteClickListener = {
                viewModel.deleteExternalConfigLocation(it)
            }
        )
        viewModel.externalConfigLocations()
            .observe(viewLifecycleOwner, Observer {
                externalConfigLocationAdapter.submitList(it)
            })
        rootView.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
            addItemDecoration(dividerItemDecoration)
            this.adapter = externalConfigLocationAdapter
        }
        rootView.addExternalConfigLocationButton.setOnClickListener {
            viewModel.onAddExternalConfigLocationClicked()
        }
        rootView.emptyText.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun showEmptyView() {
        empty.visibility = View.VISIBLE
    }

    override fun hideEmptyView() {
        empty.visibility = View.GONE
    }

    override fun showExternalConfigLocationDetailFragment(externalConfigLocationId: Long) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        val fragment = ExternalConfigLocationDetailFragment()

        fragment.arguments = Bundle().apply {
            putLong(
                ExternalConfigLocationDetailFragment.ARG_EXTERNAL_CONFIG_LOCATION_ID,
                externalConfigLocationId
            )
        }
        fragmentTransaction
            .replace(
                R.id.fragmentContainer,
                fragment
            )

        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}
