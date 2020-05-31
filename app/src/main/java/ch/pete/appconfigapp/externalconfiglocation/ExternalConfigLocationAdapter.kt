package ch.pete.appconfigapp.externalconfiglocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.model.ExternalConfigLocation
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import kotlinx.android.synthetic.main.external_config_location_list_item.view.delete
import kotlinx.android.synthetic.main.external_config_location_list_item.view.mainLayout
import kotlinx.android.synthetic.main.external_config_location_list_item.view.name
import kotlinx.android.synthetic.main.external_config_location_list_item.view.swipeLayout
import kotlinx.android.synthetic.main.external_config_location_list_item.view.url

class ExternalConfigLocationAdapter(
    private val onItemClickListener: ((ExternalConfigLocation) -> Unit)?,
    private val onDeleteClickListener: ((ExternalConfigLocation) -> Unit)?
) :
    ListAdapter<ExternalConfigLocation, ExternalConfigLocationAdapter.ViewHolder>(
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
                .inflate(R.layout.external_config_location_list_item, parent, false)
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val externalConfigLocation = getItem(position)
        holder.name.text = externalConfigLocation.name
        holder.url.text = externalConfigLocation.url

        viewBinderHelper.setOpenOnlyOne(true)
        viewBinderHelper.bind(holder.swipeLayout, externalConfigLocation.id.toString())
        viewBinderHelper.closeLayout(externalConfigLocation.id.toString())

        onItemClickListener?.let {
            holder.mainLayout.setOnClickListener {
                it(externalConfigLocation)
            }
        }
        onDeleteClickListener?.let {
            holder.delete.setOnClickListener {
                it(externalConfigLocation)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<ExternalConfigLocation>() {
                override fun areItemsTheSame(
                    oldItem: ExternalConfigLocation,
                    newItem: ExternalConfigLocation
                ) =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ExternalConfigLocation,
                    newItem: ExternalConfigLocation
                ) =
                    oldItem == newItem
            }
    }
}
