package com.codenzi.payday.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codenzi.payday.PaydayViewModel
import com.codenzi.payday.R
import com.codenzi.payday.databinding.FragmentDashboardBinding
import com.codenzi.payday.ui.budgets.BudgetAdapter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaydayViewModel by activityViewModels()
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupPieChart()
        observeBudgets()
        observeTotalBalance()
        observePieChartData()
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter()
        binding.budgetsRecyclerView.apply {
            adapter = budgetAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupPieChart() {
        binding.spendingPieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "Spending"
            setCenterTextSize(16f)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            legend.isEnabled = false
        }
    }

    private fun observeBudgets() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.budgets.collectLatest { budgets ->
                if (budgets.isEmpty()) {
                    binding.budgetsTitle.visibility = View.GONE
                    binding.budgetsRecyclerView.visibility = View.GONE
                } else {
                    binding.budgetsTitle.visibility = View.VISIBLE
                    binding.budgetsRecyclerView.visibility = View.VISIBLE
                    budgetAdapter.submitList(budgets)
                }
            }
        }
    }

    private fun observeTotalBalance() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalBalance.collectLatest { balance ->
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
                binding.totalBalanceTextView.text = currencyFormat.format(balance)
            }
        }
    }

    private fun observePieChartData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pieChartData.collectLatest { pieEntries ->
                if (pieEntries.isEmpty()) {
                    binding.spendingChartCard.visibility = View.GONE
                } else {
                    binding.spendingChartCard.visibility = View.VISIBLE
                    val dataSet = PieDataSet(pieEntries, "Spending by Category")
                    dataSet.sliceSpace = 3f
                    dataSet.selectionShift = 5f
                    dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.VORDIPLOM_COLORS.toList()

                    val data = PieData(dataSet)
                    data.setValueFormatter(PercentFormatter(binding.spendingPieChart))
                    data.setValueTextSize(12f)
                    data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                    binding.spendingPieChart.data = data
                    binding.spendingPieChart.invalidate()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
