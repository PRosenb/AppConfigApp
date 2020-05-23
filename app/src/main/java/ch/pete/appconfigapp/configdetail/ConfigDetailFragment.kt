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
import ch.pete.appconfigapp.keyvalue.KeyValuesFragment
import ch.pete.appconfigapp.model.Config
import ch.pete.appconfigapp.model.KeyValue
import ch.pete.appconfigapp.nameauthority.NameAuthorityFragment
import kotlinx.android.synthetic.main.fragment_config_detail.authority
import kotlinx.android.synthetic.main.fragment_config_detail.editKeyValue
import kotlinx.android.synthetic.main.fragment_config_detail.editNameAuthority
import kotlinx.android.synthetic.main.fragment_config_detail.keyValue
import kotlinx.android.synthetic.main.fragment_config_detail.name
import kotlinx.android.synthetic.main.fragment_config_detail.view.execute
import kotlinx.android.synthetic.main.fragment_config_detail.view.executionResults
import timber.log.Timber

class ConfigDetailFragment : Fragment(), ConfigDetailView {
    companion object {
        const val ARG_CONFIG_ENTRY_ID = "ARG_CONFIG_ENTRY_ID"
        const val ARG_NEW = "ARG_NEW"
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
                if (it.getBoolean(ARG_NEW)) {
                    viewModel.onNewItem(configId)
                    // only show it the first time
                    it.remove(ARG_NEW)
                }
            } else {
                parentFragmentManager.popBackStack()
            }
        } ?: parentFragmentManager.popBackStack()
        return rootView
    }

    private fun initView(rootView: View, configId: Long) {
        val configLiveDataConfig = viewModel.configById(configId)
        configLiveDataConfig.observe(viewLifecycleOwner, object : Observer<Config> {
            override fun onChanged(loadedConfig: Config) {
                configLiveDataConfig.removeObserver(this)
                name.text = loadedConfig.name
                authority.text = loadedConfig.authority

                loadedConfig.id?.let { configId ->
                    editNameAuthority.setOnClickListener {
                        viewModel.onEditNameAuthorityClicked(configId)
                    }
                } ?: Timber.e("loadedConfig.id is null")
            }
        })

        val configLiveDataKeyValues = viewModel.keyValueEntriesByConfigId(configId)
        configLiveDataKeyValues.observe(viewLifecycleOwner, object : Observer<List<KeyValue>> {
            override fun onChanged(keyValues: List<KeyValue>) {
                configLiveDataKeyValues.removeObserver(this)
                keyValue.text =
                    context?.resources?.getQuantityString(
                        R.plurals.keys_count,
                        keyValues.size,
                        keyValues.size
                    ) ?: ""

                editKeyValue.setOnClickListener {
                    viewModel.onEditKeyValueClicked(configId)
                }
            }
        })

        rootView.execute.setOnClickListener {
            viewModel.onDetailExecuteClicked(configId)
        }

        initExecutionResultView(configId, rootView)
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

    override fun showNameAuthorityFragment(configId: Long) {
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

    override fun showKeyValuesFragment(configId: Long) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        val fragment = KeyValuesFragment()

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
}
