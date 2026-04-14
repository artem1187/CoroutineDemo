package com.example.coroutinedemo

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutinedemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var count: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSeekBar()
    }

    private fun setupSeekBar() {
        // Устанавливаем начальное значение
        binding.seekBar.progress = 1
        binding.countText.text = "$count coroutine"

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Минимум 1 корутина
                count = if (progress < 1) 1 else progress

                // Обновляем текст с правильным склонением
                val text = when {
                    count == 1 -> "$count coroutine"
                    else -> "$count coroutines"
                }
                binding.countText.text = text
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Не требуется
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Не требуется
            }
        })
    }
}