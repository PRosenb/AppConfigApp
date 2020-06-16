package ch.pete.appconfigapp.keyvaluedetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.model.KeyValue
import kotlinx.android.synthetic.main.dialogfragment_keyvalues.view.key
import kotlinx.android.synthetic.main.dialogfragment_keyvalues.view.nullCheckbox
import kotlinx.android.synthetic.main.dialogfragment_keyvalues.view.ok
import kotlinx.android.synthetic.main.dialogfragment_keyvalues.view.value


class KeyValueDialogFragment : DialogFragment() {
    companion object {
        const val ARG_CONFIG_ID = "config_id"
        const val ARG_KEY_VALUE_ID = "keyValue_id"
    }

    private val viewModel: KeyValueDialogViewModel by viewModels()

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
            val rootView = if (args.containsKey(ARG_CONFIG_ID)) {
                val rootView = inflater.inflate(R.layout.dialogfragment_keyvalues, container, false)

                if (args.containsKey(ARG_KEY_VALUE_ID)) {
                    loadData(args, rootView)
                }

                rootView.value.addTextChangedListener(
                    afterTextChanged = { editable ->
                        if (editable?.isNotBlank() == true) {
                            rootView.nullCheckbox.isChecked = false
                        }
                    }
                )
                rootView.nullCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        rootView.value.setText("")
                    }
                }

                rootView.ok.setOnClickListener {
                    onOkClicked(args.getLong(ARG_CONFIG_ID), rootView)
                }

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

    private fun loadData(args: Bundle, rootView: View) {
        val keyValueLiveData =
            viewModel.keyValueEntryByKeyValueId(args.getLong(ARG_KEY_VALUE_ID))
        keyValueLiveData.observe(viewLifecycleOwner, object : Observer<KeyValue> {
            override fun onChanged(keyValue: KeyValue) {
                keyValueLiveData.removeObserver(this)
                rootView.key.setText(keyValue.key)
                rootView.value.setText(keyValue.value)
                rootView.nullCheckbox.isChecked = keyValue.value == null
            }
        })
    }

    private fun onOkClicked(configId: Long, rootView: View) {
        val keyValue = KeyValue(
            id = if (arguments?.containsKey(ARG_KEY_VALUE_ID) == true) {
                arguments?.getLong(ARG_KEY_VALUE_ID)
            } else {
                null
            },
            configId = configId,
            key = rootView.key.text.toString(),
            value = if (rootView.nullCheckbox.isChecked) {
                null
            } else {
                rootView.value.text.toString()
            }
        )
        viewModel.storeKeyValue(keyValue)
        dialog?.dismiss()
    }
}