package ch.pete.appconfigapp.keyvalue

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
import kotlinx.android.synthetic.main.fragment_keyvalue.empty
import kotlinx.android.synthetic.main.fragment_keyvalue.view.addKeyValueButton
import kotlinx.android.synthetic.main.fragment_keyvalue.view.emptyText
import kotlinx.android.synthetic.main.fragment_keyvalue.view.keyValues

class KeyValuesFragment : Fragment(), KeyValueView, TitleFragment {
    companion object {
        const val ARG_CONFIG_ID = "config_id"
        const val ARG_READONLY = "readonly"
    }

    private val viewModel: KeyValueViewModel by viewModels()
    override val title: String by lazy { getString(R.string.key_values) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.view = this
        viewModel.mainActivityViewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.init(arguments?.getLong(ARG_CONFIG_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_keyvalue, container, false)
        if (viewModel.initialised) {
            viewModel.readonly = arguments?.getBoolean(ARG_READONLY, false) ?: false

            initKeyValuesView(rootView)

            if (viewModel.readonly) {
                rootView.addKeyValueButton.visibility = View.GONE
            } else {
                rootView.addKeyValueButton.setOnClickListener {
                    viewModel.onAddKeyValueClicked()
                }
            }
        }
        return rootView
    }

    override fun close() {
        parentFragmentManager.popBackStack()
    }

    override fun showEmptyView() {
        empty.visibility = View.VISIBLE
    }

    override fun hideEmptyView() {
        empty.visibility = View.GONE
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

    private fun initKeyValuesView(rootView: View) {
        val adapter =
            if (viewModel.readonly) {
                KeyValueAdapter(
                    onItemClickListener = null,
                    onDeleteClickListener = null
                )
            } else {
                KeyValueAdapter(
                    onItemClickListener = {
                        viewModel.onKeyValueEntryClicked(it)
                    },
                    onDeleteClickListener = {
                        viewModel.onKeyValueDeleteClicked(it)
                    }
                )
            }
        viewModel.keyValueEntriesByConfigId()
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
        rootView.emptyText.movementMethod = LinkMovementMethod.getInstance()
    }
}
