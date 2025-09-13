package com.codenzi.payday.ui.budgets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codenzi.payday.Budget
import com.codenzi.payday.Category
import com.codenzi.payday.R
import java.text.NumberFormat
import java.util.*

data class BudgetWithDetails(
    val budget: Budget,
    val category: Category?,
    val spentAmount: Double
)

class BudgetAdapter : ListAdapter<BudgetWithDetails, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val budgetProgressBar: ProgressBar = itemView.findViewById(R.id.budgetProgressBar)
        private val spentAmount: TextView = itemView.findViewById(R.id.spentAmount)
        private val totalAmount: TextView = itemView.findViewById(R.id.totalAmount)

        fun bind(budgetWithDetails: BudgetWithDetails) {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

            categoryName.text = budgetWithDetails.category?.name ?: "Uncategorized"
            spentAmount.text = "Spent: ${currencyFormat.format(budgetWithDetails.spentAmount)}"
            totalAmount.text = "Budget: ${currencyFormat.format(budgetWithDetails.budget.amount)}"

            val progress = if (budgetWithDetails.budget.amount > 0) {
                (budgetWithDetails.spentAmount / budgetWithDetails.budget.amount * 100).toInt()
            } else {
                0
            }
            budgetProgressBar.progress = progress

            // TODO: Set category icon based on category.icon string
        }
    }
}

class BudgetDiffCallback : DiffUtil.ItemCallback<BudgetWithDetails>() {
    override fun areItemsTheSame(oldItem: BudgetWithDetails, newItem: BudgetWithDetails): Boolean {
        return oldItem.budget.id == newItem.budget.id
    }

    override fun areContentsTheSame(oldItem: BudgetWithDetails, newItem: BudgetWithDetails): Boolean {
        return oldItem == newItem
    }
}
