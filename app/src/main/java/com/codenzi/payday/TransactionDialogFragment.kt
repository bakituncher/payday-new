package com.codenzi.payday

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class TransactionDialogFragment : DialogFragment() {

    private val viewModel: PaydayViewModel by activityViewModels()
    private var existingTransaction: Transaction? = null
    private var accountsList: List<Account> = emptyList()
    private var categoriesList: List<Category> = emptyList()
    private var selectedAccountId: Int? = null
    private var selectedCategoryId: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.dialog_transaction_input, null, false)

        val nameEditText = dialogView.findViewById<EditText>(R.id.transactionNameEditText)
        val amountEditText = dialogView.findViewById<EditText>(R.id.transactionAmountEditText)
        val categoryChipGroup = dialogView.findViewById<ChipGroup>(R.id.categoryChipGroup)
        val accountSpinner = dialogView.findViewById<Spinner>(R.id.accountSpinner)
        val recurringSwitch = dialogView.findViewById<SwitchCompat>(R.id.recurringSwitch)

        val transactionId = arguments?.getInt(ARG_TRANSACTION_ID, -1) ?: -1
        if (transactionId != -1) {
            viewModel.loadTransactionToEdit(transactionId)
        }

        // Set up observers and UI
        setupObservers(accountSpinner, categoryChipGroup, nameEditText, amountEditText, recurringSwitch)

        val dialogTitleRes = if (transactionId == -1) R.string.add_transaction else R.string.edit_transaction_title

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(dialogTitleRes)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameEditText.text.toString()
                val amount = amountEditText.text.toString().toDoubleOrNull()
                val isRecurring = recurringSwitch.isChecked

                if (name.isNotBlank() && amount != null && amount > 0 && selectedAccountId != null) {
                    if (existingTransaction == null) {
                        viewModel.insertTransaction(name, amount, selectedCategoryId, selectedAccountId!!, isRecurring)
                    } else {
                        val updatedTransaction = existingTransaction!!.copy(
                            name = name,
                            amount = amount,
                            categoryId = selectedCategoryId,
                            accountId = selectedAccountId!!,
                            isRecurringTemplate = isRecurring
                        )
                        viewModel.updateTransaction(updatedTransaction)
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun setupObservers(accountSpinner: Spinner, categoryChipGroup: ChipGroup, nameET: EditText, amountET: EditText, recurringSwitch: SwitchCompat) {
        // Observe accounts and set up spinner
        viewLifecycleOwner.lifecycleScope.launch {
            accountsList = viewModel.accounts.first()
            val accountNames = accountsList.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, accountNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            accountSpinner.adapter = adapter
        }

        accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position < accountsList.size) {
                    selectedAccountId = accountsList[position].id
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedAccountId = null
            }
        }

        // Observe categories and set up chip group
        viewLifecycleOwner.lifecycleScope.launch {
            categoriesList = viewModel.categories.first()
            categoryChipGroup.removeAllViews()
            categoriesList.forEach { category ->
                val chip = Chip(requireContext()).apply {
                    text = category.name
                    tag = category.id // Store category ID in tag
                    isCheckable = true
                }
                categoryChipGroup.addView(chip)
            }
        }

        categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
             selectedCategoryId = if (checkedIds.isNotEmpty()) {
                group.findViewById<Chip>(checkedIds.first()).tag as? Int
            } else {
                null
            }
        }

        viewModel.transactionToEdit.observe(this) { transaction ->
            existingTransaction = transaction
            if (transaction != null) {
                nameET.setText(transaction.name)
                amountET.setText(transaction.amount.toString())
                recurringSwitch.isChecked = transaction.isRecurringTemplate

                val accountSelection = accountsList.indexOfFirst { acc -> acc.id == transaction.accountId }
                if(accountSelection != -1) accountSpinner.setSelection(accountSelection)

                transaction.categoryId?.let { catId ->
                     for(i in 0 until categoryChipGroup.childCount) {
                        val chip = categoryChipGroup.getChildAt(i) as Chip
                        if (chip.tag == catId) {
                            chip.isChecked = true
                            break
                        }
                    }
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onDialogDismissed()
    }

    companion object {
        const val TAG = "TransactionDialog"
        private const val ARG_TRANSACTION_ID = "transaction_id"

        fun newInstance(transactionId: Int?): TransactionDialogFragment {
            return TransactionDialogFragment().apply {
                arguments = transactionId?.let { bundleOf(ARG_TRANSACTION_ID to it) }
            }
        }
    }
}
