package com.codenzi.payday.ui.budgets

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
import com.codenzi.payday.databinding.FragmentBudgetsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BudgetsFragment : Fragment() {

    private var _binding: FragmentBudgetsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaydayViewModel by activityViewModels()
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeBudgets()
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter()
        binding.budgetsRecyclerView.apply {
            adapter = budgetAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupFab() {
        binding.addBudgetFab.setOnClickListener {
            // TODO: Implement dialog to create a new budget
            Toast.makeText(context, "Add new budget clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeBudgets() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.budgets.collectLatest { budgets ->
                binding.progressBar.visibility = View.GONE
                if (budgets.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.budgetsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.GONE
                    binding.budgetsRecyclerView.visibility = View.VISIBLE
                    budgetAdapter.submitList(budgets)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
