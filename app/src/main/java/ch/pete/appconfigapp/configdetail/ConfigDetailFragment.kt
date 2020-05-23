package ch.pete.appconfigapp.configdetail

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
import androidx.recyclerview.widget.RecyclerView
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.keyvalue.KeyValueDialogFragment
import ch.pete.appconfigapp.model.Config
import ch.pete.appconfigapp.nameauthority.NameAuthorityFragment
import kotlinx.android.synthetic.main.fragment_config_detail.authority
import kotlinx.android.synthetic.main.fragment_config_detail.editNameAuthority
import kotlinx.android.synthetic.main.fragment_config_detail.name
import kotlinx.android.synthetic.main.fragment_config_detail.view.addKeyValueButton
import kotlinx.android.synthetic.main.fragment_config_detail.view.execute
import kotlinx.android.synthetic.main.fragment_config_detail.view.executionResults
import kotlinx.android.synthetic.main.fragment_config_detail.view.keyValues
import timber.log.Timber

class ConfigDetailFragment : Fragment(), ConfigDetailView {
    companion object {
        const val ARG_CONFIG_ENTRY_ID = "ARG_CONFIG_ENTRY_ID"
    }

    private val viewModel: ConfigDetailViewModel by viewModels()

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
        val rootView = inflater.inflate(R.layout.fragment_config_detail, container, false)

        arguments?.let {
            if (it.containsKey(ARG_CONFIG_ENTRY_ID)) {
                val configId = it.getLong(ARG_CONFIG_ENTRY_ID)
                initView(rootView, configId)
            } else {
                parentFragmentManager.popBackStack()
            }
        } ?: parentFragmentManager.popBackStack()
        return rootView
    }

    private fun initView(rootView: View, configId: Long) {
        val configLiveData = viewModel.configById(configId)
        configLiveData.observe(viewLifecycleOwner, object : Observer<Config> {
            override fun onChanged(loadedConfig: Config) {
                configLiveData.removeObserver(this)
                name.text = loadedConfig.name
                authority.text = loadedConfig.authority

                loadedConfig.id?.let { configId ->
                    editNameAuthority.setOnClickListener {
                        showNameAuthorityFragment(configId)
                    }
                } ?: Timber.e("loadedConfig.id is null")
            }
        })

        rootView.execute.setOnClickListener {
            viewModel.onDetailExecuteClicked(configId)
        }

        initExecutionResultView(configId, rootView)
        initKeyValuesView(configId, rootView)

        rootView.addKeyValueButton.setOnClickListener {
            viewModel.onAddKeyValueClicked(configId)
        }
    }

    private fun initExecutionResultView(configId: Long, rootView: View) {
        val executionResultAdapter = ExecutionResultAdapter(
            onItemClickListener = null
        ).apply {
            registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        rootView.executionResults.layoutManager?.scrollToPosition(0)
                    }
                })
        }
        viewModel.executionResultEntriesByConfigId(configId)
            .observe(viewLifecycleOwner, Observer {
                executionResultAdapter.submitList(it)
            })
        rootView.executionResults.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
            addItemDecoration(dividerItemDecoration)
            this.adapter = executionResultAdapter
        }
    }

    private fun initKeyValuesView(configId: Long, rootView: View) {
        val adapter = KeyValueAdapter(
            onItemClickListener = {
                viewModel.onKeyValueEntryClicked(it)
            },
            onDeleteClickListener = {
                viewModel.onKeyValueDeleteClicked(it)
            }
        )
        viewModel.keyValueEntriesByConfigId(configId)
            .observe(viewLifecycleOwner, Observer {
                adapter.submitList(it)
            })
        rootView.keyValues.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
            addItemDecoration(dividerItemDecoration)
            this.adapter = adapter
        }
    }

    fun showNameAuthorityFragment(configId: Long) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        val fragment = NameAuthorityFragment()

        fragment.arguments = Bundle().apply {
            putLong(NameAuthorityFragment.ARG_CONFIG_ID, configId)
        }
        fragmentTransaction
            .replace(
                R.id.fragmentContainer,
                fragment
            )

        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun showKeyValueDetails(configId: Long, keyValueId: Long?) {
        val keyValueDialogFragment =
            KeyValueDialogFragment()
        val args = Bundle()
        args.putLong(KeyValueDialogFragment.ARG_CONFIG_ID, configId)
        keyValueId?.let { args.putLong(KeyValueDialogFragment.ARG_KEY_VALUE_ID, it) }
        keyValueDialogFragment.arguments = args
        keyValueDialogFragment.show(parentFragmentManager, "keyValueDialogFragment")
    }
}
