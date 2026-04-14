package com.example.coroutinedemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutinedemo.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivityMainBinding
    private var count: Int = 1
    private lateinit var job: Job
    private var activeCoroutinesCount = 0

    companion object {
        private const val TAG = "CoroutineDemo"
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()
        setupSeekBar()

        Log.d(TAG, "Activity created")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        Log.d(TAG, "Activity destroyed, all coroutines cancelled")
    }

    private fun setupSeekBar() {
        binding.seekBar.progress = 1
        binding.countText.text = "$count coroutine"

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                count = if (progress < 1) 1 else progress
                val text = if (count == 1) "$count coroutine" else "$count coroutines"
                binding.countText.text = text
                Log.d(TAG, "SeekBar progress changed: $count")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * Оптимизированная версия с использованием фонового диспетчера
     */
    private suspend fun performTaskOptimized(taskNumber: Int): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Task $taskNumber started on thread ${Thread.currentThread().name}")
        delay(5000) // Имитация работы
        Log.d(TAG, "Task $taskNumber completed")
        return@withContext "Finished Coroutine $taskNumber"
    }

    fun launchCoroutines(view: View) {
        Log.d(TAG, "Launching $count coroutines")

        binding.launchButton.isEnabled = false
        activeCoroutinesCount = count

        for (i in 1..count) {
            binding.statusText.text = "Started Coroutine $i"

            launch(Dispatchers.Main) {
                // Используем оптимизированную версию с IO диспетчером
                val result = performTaskOptimized(i)
                binding.statusText.text = result

                synchronized(this@MainActivity) {
                    activeCoroutinesCount--
                }

                if (i == count || activeCoroutinesCount == 0) {
                    binding.launchButton.isEnabled = true
                    delay(2000)
                    binding.statusText.text = getString(R.string.ready)
                    Log.d(TAG, "All coroutines completed")
                }
            }
        }
    }
}