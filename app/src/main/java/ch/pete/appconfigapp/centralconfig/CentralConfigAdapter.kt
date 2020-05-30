package ch.pete.appconfigapp.centralconfig

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.model.CentralConfig
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import kotlinx.android.synthetic.main.central_config_list_item.view.delete
import kotlinx.android.synthetic.main.central_config_list_item.view.mainLayout
import kotlinx.android.synthetic.main.central_config_list_item.view.name
import kotlinx.android.synthetic.main.central_config_list_item.view.swipeLayout
import kotlinx.android.synthetic.main.central_config_list_item.view.url

class CentralConfigAdapter(
    private val onItemClickListener: ((CentralConfig) -> Unit)?,
    private val onDeleteClickListener: ((CentralConfig) -> Unit)?
) :
    ListAdapter<CentralConfig, CentralConfigAdapter.ViewHolder>(
        DIFF_CALLBACK
    ) {
    private val viewBinderHelper = ViewBinderHelper()

    class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val mainLayout: ConstraintLayout = rootView.mainLayout
        val name: TextView = rootView.name
        val url: TextView = rootView.url

        val swipeLayout: SwipeRevealLayout = rootView.swipeLayout
        val delete: TextView = rootView.delete
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val rootView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.central_config_list_item, parent, false)
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val centralConfig = getItem(position)
        holder.name.text = centralConfig.name
        holder.url.text = centralConfig.url

        viewBinderHelper.setOpenOnlyOne(true)
        viewBinderHelper.bind(holder.swipeLayout, centralConfig.id.toString())
        viewBinderHelper.closeLayout(centralConfig.id.toString())

        onItemClickListener?.let {
            holder.mainLayout.setOnClickListener {
                it(centralConfig)
            }
        }
        onDeleteClickListener?.let {
            holder.delete.setOnClickListener {
                it(centralConfig)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<CentralConfig>() {
                override fun areItemsTheSame(oldItem: CentralConfig, newItem: CentralConfig) =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: CentralConfig,
                    newItem: CentralConfig
                ) =
                    oldItem == newItem
            }
    }
}
