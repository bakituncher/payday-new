package com.codenzi.payday.ui.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codenzi.payday.Account
import com.codenzi.payday.R
import java.text.NumberFormat
import java.util.*

class AccountAdapter : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val accountName: TextView = itemView.findViewById(R.id.accountName)
        private val accountType: TextView = itemView.findViewById(R.id.accountType)
        private val accountBalance: TextView = itemView.findViewById(R.id.accountBalance)

        fun bind(account: Account) {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            accountName.text = account.name
            accountType.text = account.type
            accountBalance.text = currencyFormat.format(account.balance)
        }
    }
}

class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
    override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
        return oldItem == newItem
    }
}
