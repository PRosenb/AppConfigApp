package ch.pete.appconfigapp.nameauthority

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.TitleFragment
import ch.pete.appconfigapp.model.Config
import kotlinx.android.synthetic.main.fragment_name_authority.authority
import kotlinx.android.synthetic.main.fragment_name_authority.name
import kotlinx.android.synthetic.main.fragment_name_authority.view.authority
import kotlinx.android.synthetic.main.fragment_name_authority.view.explanation
import kotlinx.android.synthetic.main.fragment_name_authority.view.name
import timber.log.Timber

class NameAuthorityFragment : Fragment(), TitleFragment, NameAuthorityView {
    companion object {
        const val ARG_CONFIG_ID = "config_id"
    }

    private val viewModel: NameAuthorityViewModel by viewModels()
    override val title: String by lazy { getString(R.string.details) }

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
        val rootView = inflater.inflate(R.layout.fragment_name_authority, container, false)
        rootView.explanation.movementMethod = LinkMovementMethod.getInstance()
        if (viewModel.initialised) {
            loadData(rootView)
        }
        return rootView
    }

    private fun loadData(rootView: View) {
        val liveData =
            viewModel.config()
        liveData.observe(viewLifecycleOwner, object : Observer<Config> {
            override fun onChanged(config: Config) {
                liveData.removeObserver(this)
                rootView.name.setText(config.name)
                rootView.authority.setText(config.authority)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        arguments?.getLong(ARG_CONFIG_ID)?.let {
            viewModel.storeNameAndAuthority(
                name = name.text.toString(),
                authority = authority.text.toString(),
                configId = it
            )
        } ?: Timber.e("Argument '$ARG_CONFIG_ID' missing.")
    }

    override fun close() {
        parentFragmentManager.popBackStack()
    }
}
