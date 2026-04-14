package com.example.coroutinedemo

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutinedemo.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivityMainBinding
    private var count: Int = 1

    // Создаем CoroutineScope с Main диспетчером
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем job
        job = Job()

        setupSeekBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отменяем все запущенные корутины при уничтожении Activity
        job.cancel()
    }

    private fun setupSeekBar() {
        binding.seekBar.progress = 1
        binding.countText.text = "$count coroutine"

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                count = if (progress < 1) 1 else progress
                val text = if (count == 1) "$count coroutine" else "$count coroutines"
                binding.countText.text = text
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    private suspend fun performTask(taskNumber: Int): Deferred<String> = coroutineScope {
        async(Dispatchers.Main) {
            // Имитация длительной операции (5 секунд)
            delay(5000)
            "Finished Coroutine $taskNumber"
        }
    }
}