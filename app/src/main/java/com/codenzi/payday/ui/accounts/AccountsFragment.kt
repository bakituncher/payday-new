package com.codenzi.payday.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codenzi.payday.PaydayViewModel
import com.codenzi.payday.databinding.FragmentAccountsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaydayViewModel by activityViewModels()
    private lateinit var accountAdapter: AccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeAccounts()
    }

    private fun setupRecyclerView() {
        accountAdapter = AccountAdapter()
        binding.accountsRecyclerView.apply {
            adapter = accountAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupFab() {
        binding.addAccountFab.setOnClickListener {
            // TODO: Implement dialog to create a new account
            Toast.makeText(context, "Add new account clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeAccounts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.accounts.collectLatest { accounts ->
                binding.progressBar.visibility = View.GONE
                if (accounts.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.accountsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.GONE
                    binding.accountsRecyclerView.visibility = View.VISIBLE
                    accountAdapter.submitList(accounts)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
