package ch.pete.appconfigapp.centralconfig

import android.os.Bundle
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
import ch.pete.appconfigapp.centralConfigdetails.CentralConfigDetailFragment
import kotlinx.android.synthetic.main.fragment_central_config.view.addCentralConfigButton
import kotlinx.android.synthetic.main.fragment_central_config.view.recyclerView

class CentralConfigFragment : Fragment(), CentralConfigView {
    companion object {
        const val ARG_CONFIG_ENTRY_ID = "ARG_CONFIG_ENTRY_ID"
        const val ARG_NEW = "ARG_NEW"
    }

    private val viewModel: CentralConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.view = this
        viewModel.mainActivityViewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_central_config, container, false)

        initView(rootView)
        return rootView
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
    }

    private fun initView(rootView: View) {
        val centralConfigAdapter = CentralConfigAdapter(
            onItemClickListener = {
                viewModel.onCentralConfigEntryClicked(it)
            },
            onDeleteClickListener = {
                viewModel.deleteCentralConfig(it)
            }
        )
        viewModel.centralConfigs()
            .observe(viewLifecycleOwner, Observer {
                centralConfigAdapter.submitList(it)
            })
        rootView.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
            addItemDecoration(dividerItemDecoration)
            this.adapter = centralConfigAdapter
        }
        rootView.addCentralConfigButton.setOnClickListener {
            viewModel.onAddCentralConfigClicked()
        }
    }

    override fun showCentralConfigDetailFragment(centralConfigId: Long) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        val fragment = CentralConfigDetailFragment()

        fragment.arguments = Bundle().apply {
            putLong(CentralConfigDetailFragment.ARG_CENTRAL_CONFIG_ID, centralConfigId)
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
