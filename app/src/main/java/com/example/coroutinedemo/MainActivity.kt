package com.example.coroutinedemo

import android.os.Bundle
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

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()
        setupSeekBar()
    }

    override fun onDestroy() {
        super.onDestroy()
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
            delay(5000)
            "Finished Coroutine $taskNumber"
        }
    }

    /**
     * Метод, вызываемый при нажатии кнопки
     * Запускает указанное количество корутин
     */
    fun launchCoroutines(view: View) {
        // Отключаем кнопку на время выполнения, чтобы избежать множественных нажатий
        binding.launchButton.isEnabled = false

        // Запускаем корутины в цикле
        for (i in 1..count) {
            // Обновляем UI: корутина запущена
            binding.statusText.text = "Started Coroutine $i"

            // Запускаем корутину
            launch(Dispatchers.Main) {
                val result = performTask(i).await()
                // Обновляем UI с результатом
                binding.statusText.text = result

                // Если это последняя корутина - включаем кнопку обратно
                if (i == count) {
                    binding.launchButton.isEnabled = true
                    // Через 2 секунды сбрасываем статус
                    delay(2000)
                    binding.statusText.text = getString(com.example.coroutinedemo.R.string.ready)
                }
            }
        }

        // Если корутин нет, включаем кнопку обратно
        if (count == 0) {
            binding.launchButton.isEnabled = true
        }
    }
}