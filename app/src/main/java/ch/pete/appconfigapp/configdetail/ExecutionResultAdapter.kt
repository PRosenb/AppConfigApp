package ch.pete.appconfigapp.configdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.model.ExecutionResult
import ch.pete.appconfigapp.model.ResultType
import kotlinx.android.synthetic.main.execution_result_list_item.view.executionResult
import kotlinx.android.synthetic.main.execution_result_list_item.view.timestamp
import java.text.SimpleDateFormat
import java.util.*

class ExecutionResultAdapter(
    private val onItemClickListener: ((ExecutionResult) -> Unit)?
) :
    ListAdapter<ExecutionResult, ExecutionResultAdapter.ConfigEntryViewHolder>(
        DIFF_CALLBACK
    ) {

    class ConfigEntryViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val timestamp: TextView = rootView.timestamp
        val executionResult: TextView = rootView.executionResult
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConfigEntryViewHolder {
        val rootView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.execution_result_list_item, parent, false)
        return ConfigEntryViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ConfigEntryViewHolder, position: Int) {
        val executionResult = getItem(position)

        val formatter = SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault())
        val formattedDateTime: String = formatter.format(executionResult.timestamp.time)
        holder.timestamp.text = formattedDateTime

        val context = holder.executionResult.context
        holder.executionResult.text =
            when (executionResult.resultType) {
                ResultType.SUCCESS -> {
                    // set color to default text color, same as resultTitle
                    holder.executionResult.setTextColor(
                        ContextCompat.getColor(
                            context,
                            android.R.color.black
                        )
                    )
                    context.resources.getQuantityString(
                        R.plurals.result_success,
                        executionResult.valuesCount,
                        executionResult.valuesCount
                    )
                }
                ResultType.ACCESS_DENIED -> {
                    holder.executionResult.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.failure_color
                        )
                    )
                    context.getText(R.string.result_access_denied)
                }
                ResultType.EXCEPTION -> {
                    holder.executionResult.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.failure_color
                        )
                    )
                    String.format(
                        context.getString(R.string.result_exception),
                        executionResult.message
                    )
                }
            }
        onItemClickListener?.let {
            holder.itemView.setOnClickListener {
                it(executionResult)
            }
        }
    }

    companion object {
        const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm"

        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<ExecutionResult>() {
                override fun areItemsTheSame(oldItem: ExecutionResult, newItem: ExecutionResult) =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ExecutionResult,
                    newItem: ExecutionResult
                ) =
                    oldItem == newItem
            }
    }
}
