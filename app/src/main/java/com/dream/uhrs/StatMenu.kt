package com.dream.uhrs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.dream.uhrs.databinding.FragmentStatMenuBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class StatMenu : Fragment() {
    private var _binding: FragmentStatMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatMenuBinding.inflate(inflater, container, false)
        val view = binding.root

        val chart = binding.chart

        val xData = viewModel.xData.toFloatOrNull() ?: 0f
        val yData = viewModel.yData.toFloatOrNull() ?: 0f

        val entries = ArrayList<Entry>()
        entries.add(Entry(xData, yData))

        val dataSet = LineDataSet(entries, "Label")
        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate()

        return view
    }
}