package ch.pete.appconfigapp.configlist

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.TitleFragment
import ch.pete.appconfigapp.configdetail.ConfigDetailFragment
import kotlinx.android.synthetic.main.fragment_config_list.empty
import kotlinx.android.synthetic.main.fragment_config_list.view.addConfigButton
import kotlinx.android.synthetic.main.fragment_config_list.view.emptyText
import kotlinx.android.synthetic.main.fragment_config_list.view.recyclerView

@Suppress("unused")
class ConfigListFragment : Fragment(), ConfigListView, TitleFragment {
    private val viewModel: ConfigListViewModel by activityViewModels()
    override val title: String by lazy { getString(R.string.app_name) }

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
        val rootView = inflater.inflate(R.layout.fragment_config_list, container, false)
        val adapter = ConfigEntryAdapter(
            onExecuteClickListener = {
                viewModel.onExecuteClicked(it)
            },
            onItemClickListener = {
                viewModel.onConfigEntryClicked(it)
            },
            onCloneClickListener = {
                viewModel.onConfigEntryCloneClicked(it)
            },
            onDeleteClickListener = {
                viewModel.onConfigEntryDeleteClicked(it)
            }
        )
        viewModel.configEntries.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        rootView.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
            addItemDecoration(dividerItemDecoration)
            this.adapter = adapter
        }

        rootView.addConfigButton.setOnClickListener {
            viewModel.onAddConfigClicked()
        }
        rootView.emptyText.movementMethod = LinkMovementMethod.getInstance()

        return rootView
    }

    override fun showEmptyView() {
        empty.visibility = View.VISIBLE
    }

    override fun hideEmptyView() {
        empty.visibility = View.GONE
    }

    override fun showDetailsOfNewItem(configId: Long) {
        showDetails(configId, true)
    }

    override fun showDetails(configId: Long) {
        showDetails(configId, false)
    }

    private fun showDetails(configId: Long, isNew: Boolean) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        val fragment = ConfigDetailFragment()

        fragment.arguments = Bundle().apply {
            putLong(ConfigDetailFragment.ARG_CONFIG_ENTRY_ID, configId)
            if (isNew) {
                putBoolean(ConfigDetailFragment.ARG_NEW, true)
            }
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
