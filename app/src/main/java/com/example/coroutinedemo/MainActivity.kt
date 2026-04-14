package com.example.coroutinedemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutinedemo.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var count: Int = 1
    private var activeCoroutinesCount = 0
    private var completedCoroutinesCount = 0

    // CoroutineScope для управления жизненным циклом
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val TAG = "CoroutineDemo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSeekBar()
        Log.d(TAG, "Activity created successfully")
    }

    private fun setupSeekBar() {
        binding.seekBar.apply {
            max = 2000
            progress = 1

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                    count = if (progress < 1) 1 else progress
                    val text = when {
                        count == 1 -> "$count coroutine"
                        else -> "$count coroutines"
                    }
                    binding.countText.text = text
                    Log.d(TAG, "SeekBar changed: $count coroutines selected")
                }

                override fun onStartTrackingTouch(seek: SeekBar) {
                    Log.d(TAG, "SeekBar tracking started")
                }

                override fun onStopTrackingTouch(seek: SeekBar) {
                    Log.d(TAG, "SeekBar tracking stopped at $count")
                }
            })
        }
    }


    private suspend fun performTaskOptimized(taskNumber: Int): String =
        withContext(Dispatchers.IO) {
            val threadName = Thread.currentThread().name
            Log.d(TAG, "Task $taskNumber started on $threadName")

            // Имитация реальной работы (загрузка данных, вычисления и т.д.)
            delay(5000)

            Log.d(TAG, "Task $taskNumber completed on $threadName")
            return@withContext "Finished Coroutine $taskNumber"
        }


    private suspend fun measureAndLaunchCoroutines(): Long {
        var totalTime = 0L

        // Используем measureTimeMillis для точного измерения
        totalTime = measureTimeMillis {
            // Запускаем корутины и ждем их завершения
            val jobs = mutableListOf<Job>()

            for (i in 1..count) {
                val job = coroutineScope.launch(Dispatchers.Main) {
                    val result = performTaskOptimized(i)
                    withContext(Dispatchers.Main) {
                        binding.statusText.text = result
                        completedCoroutinesCount++

                        // Обновляем прогресс
                        if (completedCoroutinesCount == count) {
                            Log.d(TAG, "All $count coroutines completed!")
                        }
                    }
                }
                jobs.add(job)

                // Обновляем UI о запуске каждой корутины
                binding.statusText.text = "Started Coroutine $i/$count"
                delay(10) // Небольшая задержка для UI обновлений
            }

            // Ждем завершения всех корутин
            jobs.joinAll()
        }

        return totalTime
    }

    /**
     * Основной метод запуска корутин с мониторингом производительности
     */
    fun launchCoroutines(view: View) {
        // Проверка на уже запущенные корутины
        if (activeCoroutinesCount > 0) {
            Log.w(TAG, "Coroutines already running! Please wait.")
            binding.statusText.text = "Please wait, previous coroutines are still running..."
            return
        }

        Log.d(TAG, "=== Starting benchmark for $count coroutines ===")

        // Отключаем кнопку во время выполнения
        binding.launchButton.isEnabled = false
        activeCoroutinesCount = count
        completedCoroutinesCount = 0

        // Запускаем корутину для мониторинга производительности
        coroutineScope.launch(Dispatchers.Main) {
            try {
                // Показываем начало выполнения
                binding.statusText.text = "Starting $count coroutines..."

                // Измеряем время выполнения
                val startTime = System.currentTimeMillis()
                val executionTime = measureAndLaunchCoroutines()
                val endTime = System.currentTimeMillis()

                // Логируем метрики производительности
                Log.d(TAG, "=== Performance Metrics ===")
                Log.d(TAG, "Coroutines launched: $count")
                Log.d(TAG, "Execution time (measureTimeMillis): ${executionTime}ms")
                Log.d(TAG, "Execution time (manual): ${endTime - startTime}ms")
                Log.d(TAG, "Average time per coroutine: ${executionTime / count}ms")

                // Показываем результат пользователю
                val message = buildString {
                    append("✅ Completed $count coroutines\n")
                    append("⏱️ Time: ${executionTime}ms\n")
                    append("⚡ Avg: ${executionTime / count}ms/coroutine")
                }
                binding.statusText.text = message

                // Проверка на пропущенные кадры
                if (executionTime > 1000 && count > 100) {
                    Log.w(TAG, "Performance warning: Long execution time detected!")
                    Log.w(TAG, "Consider using more efficient coroutine patterns")
                }

                // Через 3 секунды возвращаем готовый статус
                delay(3000)
                binding.statusText.text = "Ready for next test"

            } catch (e: CancellationException) {
                Log.e(TAG, "Coroutines cancelled: ${e.message}")
                binding.statusText.text = "Cancelled: ${e.message}"
            } catch (e: Exception) {
                Log.e(TAG, "Error during coroutine execution: ${e.message}", e)
                binding.statusText.text = "Error: ${e.message}"
            } finally {
                // Включаем кнопку обратно
                binding.launchButton.isEnabled = true
                activeCoroutinesCount = 0
                completedCoroutinesCount = 0
                Log.d(TAG, "=== Benchmark completed ===")
            }
        }
    }

    /**
     * Дополнительный метод для тестирования с разными диспетчерами
     */
    fun launchCoroutinesWithDifferentDispatchers(view: View) {
        coroutineScope.launch(Dispatchers.Main) {
            binding.statusText.text = "Testing with different dispatchers..."
            Log.d(TAG, "Testing dispatcher performance")

            // Тест с Dispatchers.Default
            val defaultTime = measureTimeMillis {
                val jobs = List(100) { index ->
                    launch(Dispatchers.Default) {
                        delay(100)
                        Log.d(TAG, "Default dispatcher task $index done")
                    }
                }
                jobs.joinAll()
            }
            Log.d(TAG, "Default dispatcher time for 100 tasks: ${defaultTime}ms")

            // Тест с Dispatchers.IO
            val ioTime = measureTimeMillis {
                val jobs = List(100) { index ->
                    launch(Dispatchers.IO) {
                        delay(100)
                        Log.d(TAG, "IO dispatcher task $index done")
                    }
                }
                jobs.joinAll()
            }
            Log.d(TAG, "IO dispatcher time for 100 tasks: ${ioTime}ms")

            binding.statusText.text = "Default: ${defaultTime}ms | IO: ${ioTime}ms"
            delay(3000)
            binding.statusText.text = "Ready"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отменяем все запущенные корутины при уничтожении Activity
        coroutineScope.cancel()
        Log.d(TAG, "Activity destroyed, all coroutines cancelled")
    }
}