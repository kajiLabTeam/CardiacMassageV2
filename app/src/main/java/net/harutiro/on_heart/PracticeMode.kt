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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.LinkedList
import kotlin.math.abs

class PracticeMode : Activity() {
    //よろしくお願いします。
    private var mSensorManager: SensorManager? = null
    private var mSensorEventListener: SensorEventListener? = null
    var pressured_counter: Int = 0 //押し込み回数カウンター
    var check: LinkedList<Double> = LinkedList()
    var rhythmCounter: LinkedList<Double> = LinkedList()
    var power_manager: LinkedList<Double> = LinkedList()
    var manual: Boolean = false //true = 一定時間押し込みが無かった際に気圧をリセットする設定を消す
    var Pause: Boolean = true
    var count: Int = 0
    var rateChecker: Int = 0
    var samplingRate: Int = 0

    private var mediaPlayer: MediaPlayer? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading)

        //NavigationBarの非表示設定
        val decor = window.decorView
        decor.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        //画面がスリープ状態にならないようにする設定
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //最初4秒間 気圧セッティング時間のためのローディング画面へ
        //気圧センサの取得数によっては終わらないが問題無し
        loading()

        //画面右上のテンポ支援ボタンのオーティオセッティング
        audioSetup()
        mediaPlayer!!.isLooping = true


        //Log.d("MediaCheck","-------------------------------");
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensorEventListener = object : SensorEventListener {
            var QueueSizeReseter: Int = 0
            var queue: LinkedList<Double> = LinkedList()
            var measured_pressure: Double = 0.00
            var Max_pressure: Double = 0.0
            var sum_pressure: Double = 0.0
            var ave_pressure: Double = 0.00
            var Min_pressure: Double = 100.00
            var decrease: Boolean = false


            override fun onSensorChanged(event: SensorEvent) {
                val `val` = event.values.clone()

                val feedText = findViewById<TextView>(R.id.FeedBackText)
                val an = findViewById<TextView>(R.id.AnnounceText)

                //an.setText("");
                val pressure = Math.round(`val`[0] * 100).toDouble() / 100

                //Log.d("PRESSURE",String.valueOf(pressure));

                //こいつで気圧センサの取得数を計測
                count++

                //Log.d("count ------------",String.valueOf(count));
                //if(count > 100)imageResize(measured_pressure,imageWidth,imageHeight);

                //気圧セッティング(大気圧の測定)
                if (count <= 100) {
                    queue.add(pressure)

                    //b1.setTextSize(45);
                    //feedText.setText("気圧設定中です。");

                    //セッティング終了 26-100個目のデータを採用し平均
                    if (count == 100) {
                        for (i in 25..99) {
                            sum_pressure += queue[i]
                            Log.d("count", queue[i].toString())
                        }
                        ave_pressure = sum_pressure / (100 - 25)
                        today_pressure = ave_pressure
                        Log.d("today's_pressure", today_pressure.toString())

                        QueueSizeReseter = queue.size
                        for (i in 0 until QueueSizeReseter) queue.removeAt(0) //all clear (queue)


                        //                        feedText.setText("練習を開始してください");
                        samplingRate = (samplingRate - rateChecker) / 3
                        //feedText.setText("SamplingRate = " + samplingRate + "Hz");
                    }
                } else {
                    //measured_pressure = 現在取れている気圧の値 - 大気圧 = 上昇気圧

                    measured_pressure = pressure - today_pressure
                    queue.add(measured_pressure)
                    if (queue.size > 3) queue.removeAt(0)

                    //queue.add(measured_pressure);
                    //Log.d("added_this_pressure = ", String.valueOf(measured_pressure));

                    //押し込まれた時の処理
                    if (lowPassFilter_now > 1) {  //lowpass.get(0) != 0 atta

                        for (i in queue_lowPass.indices) {
                            if (Max_pressure < queue_lowPass[i]) Max_pressure = queue_lowPass[i]
                            if (Min_pressure > queue_lowPass[i]) Min_pressure = queue_lowPass[i]
                        }

                        if (Max_pressure == queue_lowPass[0] && !decrease) {
                            pressured_counter++

                            //Log.i("pressured_counter = ", String.valueOf(pressured_counter));
                            //feedText.setText(String.valueOf(pressured_counter));
                            decrease = true
                            //Log.d("decrease = ", "true");
                            feedText.text = pressured_counter.toString()
                            Log.d("pressured_counter = ", pressured_counter.toString())
                            Log.d("Max = ", Max_pressure.toString())
                            Log.d("Min = ", Min_pressure.toString())

                            rhythm_manager(count, pressured_counter.toDouble(), Max_pressure)
                        }

                        if (Min_pressure == queue_lowPass[0] && decrease) {
                            decrease = false
                            Log.d("decrease = ", "false")
                        }

                        Max_pressure = 0.00
                        Min_pressure = 100.00

                        //                    Log.d("queue.size = ", String.valueOf(queue.size()));
//                    Log.d("queue_first = ", String.valueOf(queue.get(0)));
//                    Log.d("queue_last = ", String.valueOf(queue.get(queue.size()-1)));
                    }
                }

                //一定時間押し込まれなかった時に 大気圧をリセットする
                if (count > 100) CheckingPressureDifferences(pressure)

                //移動平均フィルタ(前々回気圧, 前回気圧, 今回気圧の3つ)
                if (count > 100 && queue.size == 3) LowPassFilter(queue[0], queue[1], queue[2])
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }
    }


    public override fun onResume() {
        super.onResume()

        val sl = mSensorManager!!.getSensorList(Sensor.TYPE_PRESSURE)
        if (!sl.isEmpty()) {
            // 気圧センサーがあれば，開始する
            mSensorManager!!.registerListener(
                mSensorEventListener,
                sl[0], SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    public override fun onPause() {
        mSensorManager!!.unregisterListener(mSensorEventListener)
        super.onPause()
    }

    fun feedBack(Tempo: Double, Power: Double) {
        val point_power = findViewById<ImageView>(R.id.point_power)
        val point_tempo = findViewById<ImageView>(R.id.point_tempo)
        val feedText = findViewById<TextView>(R.id.FeedBackText)

        //推奨値の設定 Max=許容最大値 Min=許容最小値
        val powerApMax = (85 * samplingRate / 25).toDouble()
        val powerApMin = (75 * samplingRate / 25).toDouble()
        val tempoApMax = 12.5 * samplingRate / 25
        val tempoApMin = 13.3 * samplingRate / 25

        val powerApAve = (powerApMax + powerApMin) / 2
        val tempoApAve = (tempoApMax + tempoApMin) / 2

        val perOfPower = (Power / powerApAve) * 100
        val perOfTempo = (Tempo / tempoApAve) * 100

        val moveY = (100 - perOfPower).toInt() * 8
        val moveX = (100 - perOfTempo).toInt() * 10 * 2

        // \n" + "F:" + FlameByOnce + "P:" + text_Power
        point_power.translationY = moveY.toFloat()
        point_tempo.translationX = moveX.toFloat()

        val bd1 = BigDecimal(perOfPower.toString())
        val bd2 = BigDecimal(perOfTempo.toString())
        val perOfPower_2pt = bd1.setScale(2, RoundingMode.HALF_UP)
        val perOfTempo_2pt = bd2.setScale(2, RoundingMode.HALF_UP)


        //feedText.setText(String.valueOf(Power));
        //feedText.setText("Pow:"+ perOfPower_2pt +"\n" + "Tem:" + perOfTempo_2pt);
        //feedText.setText("T" + Tempo + "P" + Power);
    }

    //強さの推定 テンポの測定を行う
    fun rhythm_manager(count: Int, pressured_counter: Double, power: Double) {
        val rhythm_Counter_Reseter: Int
        var power_sum = 0.00
        var flame_sum = 0.00
        val power_ave: Double
        val flame_ave: Double
        var CountReseter: Int

        power_manager.add(power)

        if (rhythmCounter.size != 0 && count - lastCounter >= 200) {
            rhythm_Counter_Reseter = rhythmCounter.size
            for (i in 0 until rhythm_Counter_Reseter) {
                power_manager.removeAt(0)
                rhythmCounter.removeAt(0)
            }
        }

        if (pressured_counter >= 2) {
            if (rhythmCounter.size == 0) {
                rhythmCounter.add(0.0)
                lastCounter = count
            } else if (rhythmCounter.size < 4) { //if rhythmCounter = 0,1,2,3
                rhythmCounter.add(count.toDouble() - lastCounter)
                lastCounter = count
            } else {
                rhythmCounter.add(count.toDouble() - lastCounter)
                lastCounter = count

                for (i in rhythmCounter.indices) {
                    flame_sum += rhythmCounter[i]
                }
                flame_ave = flame_sum / (rhythmCounter.size - 1)

                CountReseter = rhythmCounter.size
                for (i in 0 until CountReseter) rhythmCounter.removeAt(0) //all clear (queue)


                for (i in power_manager.indices) {
                    power_sum += power_manager[i]
                }
                power_ave = power_sum / power_manager.size

                CountReseter = power_manager.size
                for (i in 0 until CountReseter) power_manager.removeAt(0) //all clear (queue)


                feedBack(flame_ave, power_ave)
            }
        }
    }

    //一定時間押し込まれなかった時に 大気圧をリセットする
    fun CheckingPressureDifferences(pre: Double) {
        var sum = 0.00
        val mean: Double
        var v_sum = 0.00
        val variance: Double
        val checkSizeReseter: Int

        val point_power = findViewById<ImageView>(R.id.point_power)
        val point_tempo = findViewById<ImageView>(R.id.point_tempo)
        val feedText = findViewById<TextView>(R.id.FeedBackText)

        if (check.size < 200) { //4s
            check.add(pre)
        } else {
            for (i in check.indices) sum += check[i]
            mean = sum / check.size

            for (i in check.indices) v_sum += abs(mean - check[i])
            variance = v_sum / check.size

            //Log.d("variance", String.valueOf(variance));
            if (variance < 0.05 && !manual) {
                today_pressure = mean
                Log.d("today_pre", mean.toString())
                pressured_counter = 0
                feedText.text = "練習を開始してください"
                //L1.setBackgroundColor(Color.parseColor("#f8f8ff"));
                checkSizeReseter = check.size
                for (i in 0 until checkSizeReseter) check.removeAt(0) //all clear (check)


                point_power.translationY = 0f
                point_tempo.translationX = 0f
            } else {
                check.removeAt(0)
            }
            check.add(pre)
        }
        //Log.d("checker_size",String.valueOf(check.size()));
    }

    //テンポ支援ボタンの ON/OFF設定
    private val onClick_button = View.OnClickListener {
        val soundButton = findViewById<ImageButton>(R.id.soundButton)
        if (!Pause) {
            mediaPlayer!!.pause()
            Pause = true
            soundButton.setBackgroundResource(R.drawable.sound_off)
        } else {
            mediaPlayer!!.start()
            Pause = false
            soundButton.setBackgroundResource(R.drawable.sound_on)
        }
    }

    //テンポ支援セットアップ
    private fun audioSetup(): Boolean {
        var fileCheck = false

        // インタンスを生成
        mediaPlayer = MediaPlayer()

        // rawにファイルがある場合
        mediaPlayer = MediaPlayer.create(this, R.raw.two)
        // 音量調整を端末のボタンに任せる
        volumeControlStream = AudioManager.STREAM_MUSIC
        fileCheck = true

        return fileCheck
    }

    //テンポ支援Viewセッティング
    private fun setViews() {
        val Mute_button = findViewById<ImageButton>(R.id.soundButton)
        Mute_button.setOnClickListener(onClick_button)
    }

    //Pixel4などで OS指定の"戻る"を使用された時に正しく戻るため
    override fun onBackPressed() {
        val Sub_Activity = Intent(this, SubActivity01::class.java)
        mediaPlayer!!.stop()
        startActivity(Sub_Activity)
    }

    //ローディング画面
    fun loading() {
        val loading_video = findViewById<VideoView>(R.id.Loading_practice)

        loading_video.setVideoURI(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.mov_practice))
        loading_video.start()

        val topPage = Intent(this, SubActivity01::class.java)

        Handler().postDelayed({
            setContentView(R.layout.practice)
            setViews()

            val BackButton = findViewById<Button>(R.id.BackButton)

            BackButton.setOnClickListener { startActivity(topPage) }

            val pp = findViewById<ImageView>(R.id.point_power)
            val pt = findViewById<ImageView>(R.id.point_tempo)
            pp.translationY = 50f
            pt.translationX = -50f

            samplingRate = count
            Log.d("SamplingRate = ", samplingRate.toString())
        }, 4000) //4s

        Handler().postDelayed({ rateChecker = count }, 1000) //1s
    }

    companion object {
        var lowPassFilter_now: Double = 0.00
        var today_pressure: Double = 0.00 //大気圧
        var lastCounter: Int = 0 //前回押し込み時のCount
        var queue_lowPass: LinkedList<Double> = LinkedList()
        fun LowPassFilter(l3: Double, l2: Double, l1: Double) {
            lowPassFilter_now = (l3 + l2 + l1) / 3 * 1.2
            queue_lowPass.add(lowPassFilter_now)
            if (queue_lowPass.size > 5) queue_lowPass.removeAt(0)
        }
    }
}