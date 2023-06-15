package com.dream.uhrs

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.TextView
import kotlin.math.max


class MainFragment : Fragment() {
    private var counter = 0
    private var result = 0
    private var isCounterEnabled = true
    private lateinit var textView: TextView
    private lateinit var textView4: TextView
    private lateinit var editText6: EditText
    private lateinit var textView10: TextView
    private lateinit var chronometer: Chronometer
    private var touchesPerHour = 0
    private var revenuePerTouch = 0.0
    private var totalRevenue = 0.0
    private var pauseOffset: Long = 0
    private var isChronometerStarted = false
    private var isChronometerReset = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        textView = view.findViewById(R.id.textView)
        textView = view.findViewById(R.id.textView)
        textView4 = view.findViewById(R.id.textView4)
        editText6 = view.findViewById(R.id.editText6)
        textView10 = view.findViewById(R.id.textView10)
        chronometer = view.findViewById(R.id.chronometer)
        textView4.text = "0"
        editText6.setText("0")
        textView10.text = "0"

        val rootView = view
        rootView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN && !isChronometerStarted) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                isChronometerStarted = true
            }
            v.performClick()
            true
        }


        Log.d("MainActivity", "Loading saved state from SharedPreferences")
        val savedTime = loadTimeFromSharedPreferences()
        val savedPauseOffset = loadPauseOffsetFromSharedPreferences()
        chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
        pauseOffset = savedPauseOffset
        if (savedTime != 0L) {
            chronometer.base = SystemClock.elapsedRealtime() - savedPauseOffset
            chronometer.start()
            pauseOffset = savedPauseOffset
            isChronometerStarted = true
        }

        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            if (isCounterEnabled) {
                counter = max(0, counter - 1)
                textView.text = counter.toString()
                val counterValue = textView.text.toString().toIntOrNull() ?: 0
                val editTextValue = editText6.text.toString().toDoubleOrNull() ?: 0.0
                val result = counterValue * editTextValue
                textView10.text = result.toString()
            }
        }

        val button4 = view.findViewById<Button>(R.id.button4)
        button4.setOnClickListener {
            counter = 0
            result = 0
            textView.text = counter.toString()
            textView10.text = result.toString()
            chronometer.base = SystemClock.elapsedRealtime()
            pauseOffset = 0
            isChronometerStarted = false
            isChronometerReset = true
            saveTimeToSharedPreferences(0)
        }

        val button5 = view.findViewById<Button>(R.id.button5)
        button5.setOnClickListener {
            if (isCounterEnabled) {
                isCounterEnabled = false
                isChronometerStarted = false
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                saveTimeToSharedPreferences(0)
            } else {
                if (isChronometerReset) {
                    isChronometerReset = true
                    isChronometerStarted = true
                    chronometer.start()
                    isCounterEnabled = true
                } else {
                    chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                    isChronometerReset = false
                    isChronometerStarted = true
                    chronometer.start()
                    isCounterEnabled = true
                    saveTimeToSharedPreferences(SystemClock.elapsedRealtime())
                }
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                isChronometerStarted = true
                isCounterEnabled = true
                chronometer.start()
                saveTimeToSharedPreferences(SystemClock.elapsedRealtime())
            }
        }

        editText6.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val counterValue = textView.text.toString().toIntOrNull() ?: 0
                val editTextValue = s.toString().toDoubleOrNull() ?: 0.0
                val result = counterValue * editTextValue
                textView10.text = result.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isCounterEnabled) {
                counter++
                touchesPerHour++
                totalRevenue += revenuePerTouch
                textView.text = counter.toString()
                textView4.text = getString(R.string.hits_hour, touchesPerHour)
                val counterValue = textView.text.toString().toIntOrNull() ?: 0
                val editTextValue = editText6.text.toString().toDoubleOrNull() ?: 0.0
                val result = counterValue * editTextValue
                textView10.text = result.toString()
                v.performClick()
            }
            true
        }
        return view
    }


    override fun onResume() {
        if (isChronometerStarted) {
            chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
            chronometer.start()
        }
        super.onResume()
        Log.d("MainActivity", "onResume called")
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        sharedPref.getLong("time", 0L)
    }

    override fun onPause() {
        super.onPause()
        chronometer.stop()
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
        Log.d("MainActivity", "onPause called")
        savePauseOffsetToSharedPreferences(pauseOffset)
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putLong("time", System.currentTimeMillis())
            apply()
        }
    }

    private fun saveTimeToSharedPreferences(time: Long) {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putLong("SAVED_TIME", time)
            apply()
        }
    }

    private fun loadTimeFromSharedPreferences(): Long {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val savedTime = sharedPreferences.getLong("SAVED_TIME", 0L)
        Log.d("MainActivity", "loadTimeFromSharedPreferences: $savedTime")
        return savedTime
    }

    private fun savePauseOffsetToSharedPreferences(pauseOffset: Long) {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putLong("PAUSE_OFFSET", pauseOffset)
            apply()
        }
    }

    private fun loadPauseOffsetFromSharedPreferences(): Long {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val savedPauseOffset = sharedPreferences.getLong("PAUSE_OFFSET", 0L)
        Log.d("MainActivity", "loadPauseOffsetFromSharedPreferences: $savedPauseOffset")
        return savedPauseOffset
    }


    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        pauseOffset = 0
        savePauseOffsetToSharedPreferences(pauseOffset)
        Log.d("MainActivity", "onDestroy called")
    }

}