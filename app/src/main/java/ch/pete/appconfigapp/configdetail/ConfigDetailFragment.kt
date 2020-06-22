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
import ch.pete.appconfigapp.TitleFragment
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

class ConfigDetailFragment : Fragment(), ConfigDetailView, TitleFragment {
    companion object {
        const val ARG_CONFIG_ENTRY_ID = "ARG_CONFIG_ENTRY_ID"
        const val ARG_NEW = "ARG_NEW"
    }

    private val viewModel: ConfigDetailViewModel by viewModels()
    override val title: String by lazy { getString(R.string.config_details) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.view = this
        viewModel.mainActivityViewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.init(arguments?.getLong(ARG_CONFIG_ENTRY_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_config_detail, container, false)
        if (viewModel.initialised) {
            initView(rootView)
            if (arguments?.getBoolean(ARG_NEW) == true) {
                viewModel.onNewItem()
                // only show it the first time
                arguments?.remove(ARG_NEW)
            }
        }
        return rootView
    }

    private fun initView(rootView: View) {
        val configLiveDataConfig = viewModel.config()
        configLiveDataConfig.observe(viewLifecycleOwner, object : Observer<Config> {
            override fun onChanged(config: Config?) {
                if (config != null) {
                    configLiveDataConfig.removeObserver(this)
                    name.text = config.name
                    authority.text = config.authority

                    if (config.readonly) {
                        editNameAuthority.visibility = View.GONE
                        editKeyValue.setImageDrawable(context?.getDrawable(android.R.drawable.ic_menu_view))
                    } else {
                        editNameAuthority.setOnClickListener {
                            viewModel.onEditNameAuthorityClicked()
                        }
                        editKeyValue.setImageDrawable(context?.getDrawable(android.R.drawable.ic_menu_edit))
                    }

                    initKeyValues(config)
                } else {
                    close()
                }
            }
        })

        rootView.execute.setOnClickListener {
            viewModel.onDetailExecuteClicked()
        }

        initExecutionResultView(rootView)
    }

    private fun initKeyValues(config: Config) {
        if (config.id == null) {
            Timber.e("config.id null")
            return
        }

        val configLiveDataKeyValues = viewModel.keyValueEntriesByConfigId(config.id)
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
                    viewModel.onEditKeyValueClicked(config)
                }
            }
        })
    }

    private fun initExecutionResultView(rootView: View) {
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
        viewModel.executionResultEntries()
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

    override fun close() {
        parentFragmentManager.popBackStack()
    }

    override fun showNameAuthorityFragment(configId: Long, newItem: Boolean) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        val fragment = NameAuthorityFragment()

        fragment.arguments = Bundle().apply {
            putLong(NameAuthorityFragment.ARG_CONFIG_ID, configId)
            putBoolean(NameAuthorityFragment.ARG_NEW, newItem)
        }
        fragmentTransaction
            .replace(
                R.id.fragmentContainer,
                fragment
            )

        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun showKeyValuesFragment(configId: Long, readonly: Boolean) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        val fragment = KeyValuesFragment()

        fragment.arguments = Bundle().apply {
            putLong(KeyValuesFragment.ARG_CONFIG_ID, configId)
            putBoolean(KeyValuesFragment.ARG_READONLY, readonly)
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
