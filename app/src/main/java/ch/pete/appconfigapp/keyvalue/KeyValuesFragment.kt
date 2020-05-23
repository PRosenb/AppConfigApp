package ch.pete.appconfigapp.keyvalue

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
import kotlinx.android.synthetic.main.fragment_keyvalue.view.addKeyValueButton
import kotlinx.android.synthetic.main.fragment_keyvalue.view.keyValues

class KeyValuesFragment : Fragment(), KeyValueView {
    companion object {
        const val ARG_CONFIG_ID = "config_id"
    }

    private val viewModel: KeyValueViewModel by viewModels()

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
        arguments?.let { args ->
            val configId = if (!args.containsKey(ARG_CONFIG_ID)) {
                parentFragmentManager.popBackStack()
                return null
            } else {
                args.getLong(ARG_CONFIG_ID)
            }

            val rootView = inflater.inflate(R.layout.fragment_keyvalue, container, false)

            initKeyValuesView(configId, rootView)
            rootView.addKeyValueButton.setOnClickListener {
                viewModel.onAddKeyValueClicked(configId)
            }
            return rootView
        } ?: run {
            parentFragmentManager.popBackStack()
            return null
        }
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
}