package net.harutiro.on_heart

import android.app.Activity
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.LinkedList
import kotlin.math.abs

class PracticeMode : Activity() {
    // センサー管理とリスナー
    private var mSensorManager: SensorManager? = null
    private var mSensorEventListener: SensorEventListener? = null

    // カウンターやデータの管理
    private var pressuredCounter = 0
    private var check = LinkedList<Double>()
    private var rhythmCounter = LinkedList<Double>()
    private var powerManager = LinkedList<Double>()
    private var manual = false
    private var isPaused = true
    private var count = 0
    private var rateChecker = 0
    private var samplingRate = 0
    private var mediaPlayer: MediaPlayer? = null
    private var queue = LinkedList<Double>()
    var decrease: Boolean = false

    // アクティビティの初期化
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading)
        setupWindow()  // ウィンドウ設定
        loading()  // ローディング画面の設定
        setupAudio()  // オーディオ設定
        setupSensor()  // センサー設定
    }

    // ウィンドウの設定
    private fun setupWindow() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // ローディング画面の設定
    private fun loading() {
        val loadingVideo = findViewById<VideoView>(R.id.Loading_practice)
        loadingVideo.setVideoURI(Uri.parse("android.resource://${packageName}/${R.raw.mov_practice}"))
        loadingVideo.start()

        Handler().postDelayed({
            setContentView(R.layout.practice)
            setViews()  // ビューの設定
            findViewById<Button>(R.id.BackButton).setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
            }
            samplingRate = count
            Log.d("SamplingRate", samplingRate.toString())
        }, 4000)

        Handler().postDelayed({ rateChecker = count }, 1000)
    }

    // オーディオの設定
    private fun setupAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.two).apply {
            isLooping = true
            volumeControlStream = AudioManager.STREAM_MUSIC
        }
    }

    // センサーの設定
    private fun setupSensor() {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensorEventListener = object : SensorEventListener {
            private var sumPressure = 0.0
            private var avePressure = 0.0

            // センサーの値が変わったときの処理
            override fun onSensorChanged(event: SensorEvent) {
                val pressure = Math.round(event.values[0] * 100).toDouble() / 100
                count++

                // 初期計測（100回分のデータ収集）
                if (count <= 100) {
                    queue.add(pressure)
                    if (count == 100) {
                        avePressure = queue.subList(25, 100).sum() / 75
                        todayPressure = avePressure
                        queue.clear()
                        samplingRate = (samplingRate - rateChecker) / 3
                    }
                } else {
                    processPressureData(pressure)  // 圧力データの処理
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    // 圧力データの処理
    private fun processPressureData(pressure: Double) {
        val measuredPressure = pressure - todayPressure
        queue.add(measuredPressure)
        if (queue.size > 3) queue.removeAt(0)

        if (lowPassFilterNow > 1) {
            val maxPressure = queue_lowPass.maxOrNull() ?: 0.0
            val minPressure = queue_lowPass.minOrNull() ?: 100.0

            if (maxPressure == queue_lowPass[0]) {
                pressuredCounter++
                findViewById<TextView>(R.id.FeedBackText).text = pressuredCounter.toString()
                rhythmManager(count, pressuredCounter.toDouble(), maxPressure)
            }

            if (minPressure == queue_lowPass[0]) decrease = false

            queue_lowPass.clear()
        }

        if (count > 100) checkPressureDifferences(pressure)  // 圧力差のチェック
        if (count > 100 && queue.size == 3) lowPassFilter(queue[0], queue[1], queue[2])  // ローパスフィルタの適用
    }

    // 圧力差のチェック
    private fun checkPressureDifferences(pressure: Double) {
        if (check.size < 200) {
            check.add(pressure)
        } else {
            val mean = check.average()
            val variance = check.sumOf { abs(mean - it) } / check.size
            if (variance < 0.05 && !manual) {
                todayPressure = mean
                pressuredCounter = 0
                findViewById<TextView>(R.id.FeedBackText).text = "練習を開始してください"
                check.clear()
            } else {
                check.removeFirst()
                check.add(pressure)
            }
        }
    }

    // ローパスフィルタの適用
    private fun lowPassFilter(l3: Double, l2: Double, l1: Double) {
        lowPassFilterNow = (l3 + l2 + l1) / 3 * 1.2
        queue_lowPass.add(lowPassFilterNow)
        if (queue_lowPass.size > 5) queue_lowPass.removeFirst()
    }

    // アクティビティが再開されたときの処理
    override fun onResume() {
        super.onResume()
        mSensorManager?.getSensorList(Sensor.TYPE_PRESSURE)?.firstOrNull()?.let {
            mSensorManager?.registerListener(mSensorEventListener, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    // アクティビティが一時停止されたときの処理
    override fun onPause() {
        mSensorManager?.unregisterListener(mSensorEventListener)
        super.onPause()
    }

    // リズム管理
    private fun rhythmManager(count: Int, pressuredCounter: Double, power: Double) {
        powerManager.add(power)
        if (rhythmCounter.isNotEmpty() && count - lastCounter >= 200) {
            rhythmCounter.clear()
            powerManager.clear()
        }

        if (pressuredCounter >= 2) {
            if (rhythmCounter.isEmpty()) {
                rhythmCounter.add(0.0)
                lastCounter = count
            } else {
                rhythmCounter.add(count.toDouble() - lastCounter)
                lastCounter = count
                if (rhythmCounter.size == 4) {
                    val flameAvg = rhythmCounter.sum() / (rhythmCounter.size - 1)
                    val powerAvg = powerManager.average()
                    feedBack(flameAvg, powerAvg)  // フィードバック
                    rhythmCounter.clear()
                    powerManager.clear()
                }
            }
        }
    }

    // フィードバックの表示
    private fun feedBack(tempo: Double, power: Double) {
        val powerApMax = (85 * samplingRate / 25).toDouble()
        val powerApMin = (75 * samplingRate / 25).toDouble()
        val tempoApMax = 12.5 * samplingRate / 25
        val tempoApMin = 13.3 * samplingRate / 25

        val perOfPower = (power / ((powerApMax + powerApMin) / 2)) * 100
        val perOfTempo = (tempo / ((tempoApMax + tempoApMin) / 2)) * 100

        findViewById<ImageView>(R.id.point_power).translationY = ((100 - perOfPower).toInt() * 8).toFloat()
        findViewById<ImageView>(R.id.point_tempo).translationX = ((100 - perOfTempo).toInt() * 10 * 2).toFloat()
    }

    // ボタンのクリックリスナー
    private val onClickButton = View.OnClickListener {
        val soundButton = findViewById<ImageButton>(R.id.soundButton)
        if (isPaused) {
            mediaPlayer?.start()
            soundButton.setBackgroundResource(R.drawable.sound_on)
        } else {
            mediaPlayer?.pause()
            soundButton.setBackgroundResource(R.drawable.sound_off)
        }
        isPaused = !isPaused
    }

    // ビューの設定
    private fun setViews() {
        findViewById<ImageButton>(R.id.soundButton).setOnClickListener(onClickButton)
    }

    // 戻るボタンが押されたときの処理
    override fun onBackPressed() {
        mediaPlayer?.stop()
        startActivity(Intent(this, MainActivity::class.java))
    }

    // クラス内のコンパニオンオブジェクト
    companion object {
        var lowPassFilterNow = 0.00
        var todayPressure = 0.00
        var lastCounter = 0
        var queue_lowPass = LinkedList<Double>()
    }
}
