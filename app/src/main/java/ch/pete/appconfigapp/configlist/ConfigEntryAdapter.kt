package ch.pete.appconfigapp.configlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.model.ConfigEntry
import ch.pete.appconfigapp.model.ResultType
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import kotlinx.android.synthetic.main.config_entry_list_item.view.clone
import kotlinx.android.synthetic.main.config_entry_list_item.view.delete
import kotlinx.android.synthetic.main.config_entry_list_item.view.execute
import kotlinx.android.synthetic.main.config_entry_list_item.view.keysCount
import kotlinx.android.synthetic.main.config_entry_list_item.view.lastResult
import kotlinx.android.synthetic.main.config_entry_list_item.view.mainLayout
import kotlinx.android.synthetic.main.config_entry_list_item.view.resultTitle
import kotlinx.android.synthetic.main.config_entry_list_item.view.swipeLayout
import kotlinx.android.synthetic.main.config_entry_list_item.view.title


class ConfigEntryAdapter(
    private val onItemClickListener: ((ConfigEntry) -> Unit)?,
    private val onExecuteClickListener: ((ConfigEntry) -> Unit)?,
    private val onCloneClickListener: ((ConfigEntry) -> Unit)?,
    private val onDeleteClickListener: ((ConfigEntry) -> Unit)?
) :
    ListAdapter<ConfigEntry, ConfigEntryAdapter.ConfigEntryViewHolder>(
        DIFF_CALLBACK
    ) {
    private val viewBinderHelper = ViewBinderHelper()

    class ConfigEntryViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val mainLayout: ConstraintLayout = rootView.mainLayout
        val execute: ImageView = rootView.execute
        val title: TextView = rootView.title
        val keysCount: TextView = rootView.keysCount
        val resultTitle: TextView = rootView.resultTitle
        val lastResult: TextView = rootView.lastResult

        val swipeLayout: SwipeRevealLayout = rootView.swipeLayout
        val clone: TextView = rootView.clone
        val delete: TextView = rootView.delete
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConfigEntryViewHolder {
        val rootView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.config_entry_list_item, parent, false)
        return ConfigEntryViewHolder(
            rootView
        )
    }

    override fun onBindViewHolder(holder: ConfigEntryViewHolder, position: Int) {
        val configEntry = getItem(position)

        holder.title.text = configEntry.config.name
        val context = holder.keysCount.context
        holder.keysCount.text =
            context.resources.getQuantityString(
                R.plurals.keys_count,
                configEntry.keyValues.size,
                configEntry.keyValues.size
            )
        showLastResult(configEntry, holder, context)

        onExecuteClickListener?.let {
            holder.execute.setOnClickListener {
                it(configEntry)
            }
        }
        onItemClickListener?.let {
            holder.mainLayout.setOnClickListener {
                it(configEntry)
            }
        }

        viewBinderHelper.setOpenOnlyOne(true)
        viewBinderHelper.bind(holder.swipeLayout, configEntry.config.id.toString())
        viewBinderHelper.closeLayout(configEntry.config.id.toString())
        onCloneClickListener?.let {
            holder.clone.setOnClickListener {
                it(configEntry)
                viewBinderHelper.closeLayout(configEntry.config.id.toString())
            }
        }

        if (configEntry.config.readonly) {
            holder.delete.visibility = View.GONE
        } else {
            holder.delete.visibility = View.VISIBLE
            onDeleteClickListener?.let {
                holder.delete.setOnClickListener {
                    it(configEntry)
                }
            }
        }
    }

    private fun showLastResult(
        configEntry: ConfigEntry, holder: ConfigEntryViewHolder,
        context: Context
    ) {
        holder.lastResult.text = configEntry.executionResults.maxByOrNull { it.timestamp }
            ?.let {
                when (it.resultType) {
                    ResultType.SUCCESS -> {
                        // set color to default text color, same as resultTitle
                        holder.lastResult.setTextColor(holder.resultTitle.textColors)
                        context.resources.getQuantityString(
                            R.plurals.result_success,
                            it.valuesCount,
                            it.valuesCount
                        )
                    }
                    ResultType.ACCESS_DENIED -> {
                        holder.lastResult.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.failure_color
                            )
                        )
                        context.getText(R.string.result_access_denied)
                    }
                    ResultType.EXCEPTION -> {
                        holder.lastResult.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.failure_color
                            )
                        )
                        String.format(context.getString(R.string.result_exception), it.message)
                    }
                }
            } ?: ""
    }

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<ConfigEntry>() {
                override fun areItemsTheSame(oldItem: ConfigEntry, newItem: ConfigEntry) =
                    oldItem.config.id == newItem.config.id

                override fun areContentsTheSame(oldItem: ConfigEntry, newItem: ConfigEntry) =
                    oldItem == newItem
            }
    }
}
