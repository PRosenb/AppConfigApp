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
import timber.log.Timber


class KeyValueDialogFragment : DialogFragment(), KeyValueDialogView {
    companion object {
        const val ARG_CONFIG_ID = "config_id"
        const val ARG_KEY_VALUE_ID = "keyValue_id"
        private const val EMPTY_LONG = 0L
    }

    private val viewModel: KeyValueDialogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.view = this
        viewModel.mainActivityViewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        var keyValueId = arguments?.getLong(ARG_KEY_VALUE_ID, EMPTY_LONG)
        if (keyValueId == EMPTY_LONG) {
            keyValueId = null
        }
        viewModel.init(
            arguments?.getLong(ARG_CONFIG_ID),
            keyValueId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialogfragment_keyvalues, container, false)
        if (viewModel.initialised) {
            loadData(rootView)
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
                viewModel.onOkClicked(
                    rootView.key.text.toString(),
                    rootView.value.text.toString(),
                    rootView.nullCheckbox.isChecked
                )
            }
        }
        return rootView
    }

    override fun close() {
        dialog?.dismiss()
    }

    private fun loadData(rootView: View) {
        val keyValueLiveData =
            viewModel.keyValueEntry()
        keyValueLiveData.observe(viewLifecycleOwner, object : Observer<KeyValue> {
            override fun onChanged(keyValue: KeyValue?) {
                keyValueLiveData.removeObserver(this)

                if (keyValue != null) {
                    rootView.key.setText(keyValue.key)
                    rootView.value.setText(keyValue.value)
                    rootView.nullCheckbox.isChecked = keyValue.value == null
                } else {
                    Timber.w("keyValue is null, close dialog")
                    close()
                }
            }
        })
    }
}
