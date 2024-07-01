package net.harutiro.on_heart

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.LinkedList
import kotlin.math.abs

class ChallengeMode : Activity() {
    //よろしくお願いします。
    private var mSensorManager: SensorManager? = null
    private var mSensorEventListener: SensorEventListener? = null
    var pressured_counter: Int = 0 //押し込み回数カウンター
    var check: LinkedList<Double> = LinkedList()
    var rhythmCounter: LinkedList<Double> = LinkedList()
    var power_manager: LinkedList<Double> = LinkedList()
    var graphData: ArrayList<Float> = ArrayList()
    private val mChart: LineChart? = null
    var playing: Boolean = false
    var result: Boolean = false
    var summary: Boolean = false
    var power_ave: ArrayList<Double> = ArrayList()
    var tempo_ave: ArrayList<Double> = ArrayList()


    var powOfPressured: ArrayList<Double> = ArrayList()
    var tempoOfCount: ArrayList<Int> = ArrayList()
    var lastCount: Int = 0
    var samplingRate: Int = 0
    var count: Int = 0
    var loading_boo: Boolean = true


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

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensorEventListener = object : SensorEventListener {
            var wallCount: Int = 0
            var QueueSizeReseter: Int = 0
            var queue: LinkedList<Double> = LinkedList()
            var measured_pressure: Double = 0.00
            var Max_pressure: Double = 0.00
            var sum_pressure: Double = 0.00
            var ave_pressure: Double = 0.00
            var Min_pressure: Double = 100.00
            var decrease: Boolean = false

            var tmp_counter: Int = 0

            override fun onSensorChanged(event: SensorEvent) {
                val `val` = event.values.clone()

                val iv = findViewById<ImageView>(R.id.trial)
                val feedText = findViewById<TextView>(R.id.challenge_text)

                //テスト中 = playing
                if (playing) {
                    wallCount += 2
                    iv.translationY = wallCount.toFloat() //max 950

                    if (wallCount == 1080) {
                        feedText.setTextColor(Color.rgb(89, 84, 87))
                        feedText.text = "お疲れ様でした。\n結果画面に移行します。"
                    }

                    if (wallCount == 1180) {
                        setContentView(R.layout.summary)
                        summaryInfo()
                        summary = true
                        playing = false
                    }
                }


                val pressure = Math.round(`val`[0] * 100).toDouble() / 100

                //Log.d("PRESSURE",String.valueOf(pressure));

                //こいつが起動してから取得した気圧センサの値の数
                count++
                Log.d("count ------------", count.toString())

                //if(count > 100)imageResize(measured_pressure,imageWidth,imageHeight);

                //PracticeModeと同じ部分は省略(Class分けてなくてごめんね)
                if (wallCount != 1180) {
                    if (count <= 100) {
                        queue.add(pressure)
                        Log.d("----------", pressure.toString())

                        if (!loading_boo) feedText.text = "少々お待ちください"

                        if (count == 100) {
                            for (i in 25..99) {
                                sum_pressure += queue[i]
                                //Log.d("count",String.valueOf(queue.get(i)));
                            }
                            ave_pressure = sum_pressure / (100 - 25)
                            today_pressure = ave_pressure
                            Log.d("today's_pressure", today_pressure.toString())

                            QueueSizeReseter = queue.size
                            for (i in 0 until QueueSizeReseter) queue.removeAt(0) //all clear (queue)
                        }
                    } else {
                        measured_pressure = pressure - today_pressure
                        queue.add(measured_pressure)
                        if (queue.size > 3) queue.removeAt(0)

                        //queue.add(measured_pressure);
                        //Log.d("added_this_pressure = ", String.valueOf(measured_pressure));
                        if (lowPassFilter_now > 1) {  //lowpass.get(0) != 0 atta

                            for (i in queue_lowPass.indices) {
                                if (Max_pressure < queue_lowPass[i]) Max_pressure = queue_lowPass[i]
                                if (Min_pressure > queue_lowPass[i]) Min_pressure = queue_lowPass[i]
                            }

                            //Log.d("Max = ", String.valueOf(Max_pressure));
                            //Log.d("Min = ", String.valueOf(Min_pressure));
                            if (Max_pressure == queue_lowPass[0] && !decrease) {
                                pressured_counter++

                                //if(playing) feedText.setText(String.valueOf(pressured_counter));
                                if (!playing && count - tmp_counter < 25) {
                                    val BackButton = findViewById<Button>(R.id.BackButton3)
                                    BackButton.visibility = View.INVISIBLE

                                    playing = true
                                    feedText.text = ""
                                }

                                if (playing) {
                                    powOfPressured.add(Max_pressure)
                                    if (tempoOfCount.size >= 1) tempoOfCount.add(count - lastCount)
                                    else tempoOfCount.add(0)
                                    lastCount = count
                                }

                                tmp_counter = count //前回押し込み時のcount

                                //Log.i("pressured_counter = ", String.valueOf(pressured_counter));
                                //feedText.setText(String.valueOf(pressured_counter));
                                decrease = true
                                Log.d("decrease = ", "true")
                                Log.d("pressured_counter = ", pressured_counter.toString())

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

                    if (count > 100) CheckingPressureDifferences(pressure)

                    if (count > 100 && queue.size == 3) LowPassFilter(queue[0], queue[1], queue[2])

                    if (count == 101) feedText.text = "任意のタイミングで\n開始してください。"
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }
    }

    public override fun onPause() {
        mSensorManager!!.unregisterListener(mSensorEventListener)
        super.onPause()
    }

    fun LowPassFilter(l3: Double, l2: Double, l1: Double) {
        lowPassFilter_now = (l3 + l2 + l1) / 3 * 1.2
        queue_lowPass.add(lowPassFilter_now)
        if (queue_lowPass.size > 5) queue_lowPass.removeAt(0)
        if (playing) graphData.add(lowPassFilter_now.toFloat())
    }

    fun rhythm_manager(count: Int, pressured_counter: Double, power: Double) {
        val rhythm_Counter_Reseter: Int
        var power_sum = 0.00
        var tempo_sum = 0.00
        var CountReseter: Int

        val feedBackText = findViewById<TextView>(R.id.challenge_text)
        val resultText = findViewById<TextView>(R.id.result_text)

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
                    tempo_sum += rhythmCounter[i]
                }
                tempo_ave.add(tempo_sum / (rhythmCounter.size - 1))

                CountReseter = rhythmCounter.size
                for (i in 0 until CountReseter) rhythmCounter.removeAt(0) //all clear (queue)


                for (i in power_manager.indices) {
                    power_sum += power_manager[i]
                }
                power_ave.add(power_sum / power_manager.size)

                CountReseter = power_manager.size
                for (i in 0 until CountReseter) power_manager.removeAt(0) //all clear (queue)


                //feedBackText.setText("Pow :" + power_ave + "\nSpd :" + flame_ave);
                Log.d("POWER_AVE_SIZE", power_ave.size.toString())
                Log.d("TEMPO_AVE_SIZE", tempo_ave.size.toString())
            }
        }
    }

    fun CheckingPressureDifferences(pre: Double) {
        var sum = 0.00
        val mean: Double
        var v_sum = 0.00
        val variance: Double
        val checkSizeReseter: Int

        val feedText = findViewById<TextView>(R.id.FeedBackText)

        if (check.size < 200) { //4s
            check.add(pre)
        } else {
            for (i in check.indices) sum += check[i]
            mean = sum / check.size

            for (i in check.indices) v_sum += abs(mean - check[i])
            variance = v_sum / check.size
            Log.d("variance", variance.toString())

            if (variance < 0.05) {
                today_pressure = mean
                Log.d("today_pre", mean.toString())
                pressured_counter = 0
                //b1.setText("開始してください。");
                //L1.setBackgroundColor(Color.parseColor("#f8f8ff"));
                checkSizeReseter = check.size
                for (i in 0 until checkSizeReseter) check.removeAt(0) //all clear (check)
            } else {
                check.removeAt(0)
            }
            check.add(pre)
        }
        //Log.d("checker_size",String.valueOf(check.size()));
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

    override fun onBackPressed() {
        val Sub_Activity = Intent(this, SubActivity01::class.java)
        startActivity(Sub_Activity)
    }

    //一応実装してるけど中途半端です サマリー画面で画面をタップするとテスト中に取得した気圧のグラフが見られます
    fun graphExport() {
        val resultText = findViewById<TextView>(R.id.result_text)

        val values = ArrayList<Entry>()

        for (i in graphData.indices) {
            values.add(Entry(i.toFloat(), graphData[i], null, null))
        }


        //              for (int i = 0; i < testData.length; i++) {
//                values.add(new Entry(i, testData[i], null, null));
//            }
        val set1: LineDataSet

        if (mChart!!.data != null &&
            mChart.data.dataSetCount > 0
        ) {
            set1 = mChart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = values
            mChart.data.notifyDataChanged()
            mChart.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values, "")

            //mChart.getDescription().setEnabled(false);
            set1.lineWidth = 2f //線の太さ
            set1.color = Color.rgb(239, 247, 246) //線の色
            set1.setDrawValues(false)
            set1.setDrawCircles(false)

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the datasets

            // create a data object with the datasets
            val lineData = LineData(dataSets)

            // set data
            mChart.data = lineData

            resultText.setTextColor(Color.rgb(239, 247, 246))
            resultText.textSize = 27f
            resultText.text = ""
        }
    }

    //テスト後 タップで表示方法を変えられるヤツ
    //    @Override
    //    public boolean onTouchEvent(MotionEvent motionEvent) {
    //
    //        TextView resultText = findViewById(R.id.result_text);
    //
    //        switch (motionEvent.getAction()) {
    //            case MotionEvent.ACTION_DOWN:
    //                if(summary){
    //                    setContentView(R.layout.result);
    //
    //                    mChart = findViewById(R.id.line_chart);
    //
    //                    mChart.getDescription().setEnabled(false);
    //                    mChart.getAxisLeft().setDrawGridLines(false);
    //                    mChart.getXAxis().setDrawGridLines(false);
    //                    mChart.getXAxis().setEnabled(false);
    //                    mChart.getAxisLeft().setEnabled(false);
    //                    mChart.getAxisRight().setEnabled(false);
    //                    mChart.getLegend().setEnabled(false);
    //                    mChart.setTouchEnabled(false);
    //
    //                    // y軸の設定
    //                    YAxis yAxis = mChart.getAxisLeft();
    //                    // Y軸最大最小設定
    //                    yAxis.setAxisMaximum(80f);
    //                    yAxis.setAxisMinimum(0f);
    //
    //                    // add data
    //                    graphExport();
    //
    //                    summary = false;
    //                    result = true;
    //                }else if(result){
    //                    setContentView(R.layout.summary);
    //
    //                    summaryInfo();
    //
    //                    summary = true;
    //                    result = false;
    //                }
    //                break;
    //        }
    //
    //        return false;
    //    }
    //サマリー画面で表示する 最大値,最小値,第一四分位数,第二四分位数,第三四分位数 を計算し出力
    //ほんとにリファクタリングできてなくてごめん
    fun summaryInfo() {
        val topPage = Intent(this, SubActivity01::class.java)

        val BackButton = findViewById<Button>(R.id.BackButton2)

        BackButton.setOnClickListener { startActivity(topPage) }

        for (i in 0 until powOfPressured.size - 1) {
            for (j in powOfPressured.size - 1 downTo i + 1) {
                if (powOfPressured[j - 1] > powOfPressured[j]) {
                    val powTmp = powOfPressured[j - 1]
                    powOfPressured[j - 1] = powOfPressured[j]
                    powOfPressured[j] = powTmp
                }
                if (tempoOfCount[j - 1] > tempoOfCount[j]) {
                    val tmpTmp = tempoOfCount[j - 1]
                    tempoOfCount[j - 1] = tempoOfCount[j]
                    tempoOfCount[j] = tmpTmp
                }
            }
        }

        Log.d("PRESSURE---------------------", powOfPressured.size.toString())
        Log.d("TEMPO------------------------", tempoOfCount.size.toString())

        for (i in powOfPressured.indices) {
            Log.d("PRESSURE---------------------", powOfPressured[i].toString())
        }

        for (i in powOfPressured.indices) {
            Log.d("TEMPPPPP---------------------", tempoOfCount[i].toString())
        }

        val quarterLength = powOfPressured.size / 4

        val maxPower = powOfPressured[powOfPressured.size - 1]
        val quarterOfThree_pow = powOfPressured[quarterLength * 3]
        val quarterOfTwo_pow = powOfPressured[quarterLength * 2]
        val quarterOfOne_pow = powOfPressured[quarterLength]
        val minPower = powOfPressured[0]

        val maxTempo = tempoOfCount[tempoOfCount.size - 1].toDouble()
        val quarterOfThree_tempo = tempoOfCount[quarterLength * 3].toDouble()
        val quarterOfTwo_tempo = tempoOfCount[quarterLength * 2].toDouble()
        val quarterOfOne_tempo = tempoOfCount[quarterLength].toDouble()
        val minTempo = tempoOfCount[1].toDouble()

        val pp_max = findViewById<ImageView>(R.id.summary_pp_up2)
        val pp_q3 = findViewById<ImageView>(R.id.summary_pp_up1)
        val pp_q2 = findViewById<ImageView>(R.id.summary_pp)
        val pp_q1 = findViewById<ImageView>(R.id.summary_pp_low1)
        val pp_min = findViewById<ImageView>(R.id.summary_pp_low2)

        val pt_max = findViewById<ImageView>(R.id.summary_pt_up2)
        val pt_q3 = findViewById<ImageView>(R.id.summary_pt_up1)
        val pt_q2 = findViewById<ImageView>(R.id.summary_pt)
        val pt_q1 = findViewById<ImageView>(R.id.summary_pt_low1)
        val pt_min = findViewById<ImageView>(R.id.summary_pt_low2)

        val powerApMax = (85 * samplingRate / 25).toDouble() //85,75,12.5,13.3
        val powerApMin = (75 * samplingRate / 25).toDouble()
        val tempoApMax = 12.5 * samplingRate / 25

        val tempoApMin = 13.3 * samplingRate / 25


        val powerApAve = (powerApMax + powerApMin) / 2
        val tempoApAve = (tempoApMax + tempoApMin) / 2

        val mov_maxPower = (1 - (maxPower / powerApAve))
        val mov_quarterOfThree_pow = (1 - (quarterOfThree_pow / powerApAve))
        val mov_quarterOfTwo_pow = (1 - (quarterOfTwo_pow / powerApAve))
        val mov_quarterOfOne_pow = (1 - (quarterOfOne_pow / powerApAve))
        val mov_minPower = (1 - (minPower / powerApAve))

        val mov_maxTempo = (1 - (maxTempo / tempoApAve))
        val mov_quarterOfThree_tempo = (1 - (quarterOfThree_tempo / tempoApAve))
        val mov_quarterOfTwo_tempo = (1 - (quarterOfTwo_tempo / tempoApAve))
        val mov_quarterOfOne_tempo = (1 - (quarterOfOne_tempo / tempoApAve))
        val mov_minTempo = (1 - (minTempo / tempoApAve))

        pp_max.translationY = mov_maxPower.toFloat() * 800
        pp_q3.translationY = mov_quarterOfThree_pow.toFloat() * 800
        pp_q2.translationY = mov_quarterOfTwo_pow.toFloat() * 800
        pp_q1.translationY = mov_quarterOfOne_pow.toFloat() * 800
        pp_min.translationY = mov_minPower.toFloat() * 800

        pt_max.translationX = mov_maxTempo.toFloat() * 800
        pt_q3.translationX = mov_quarterOfThree_tempo.toFloat() * 800
        pt_q2.translationX = mov_quarterOfTwo_tempo.toFloat() * 800
        pt_q1.translationX = mov_quarterOfOne_tempo.toFloat() * 800
        pt_min.translationX = mov_minTempo.toFloat() * 800

        Log.d("11111111111", mov_maxPower.toString())
        Log.d("11111111111", mov_quarterOfThree_pow.toString())
        Log.d("11111111111", mov_quarterOfTwo_pow.toString())
        Log.d("11111111111", mov_quarterOfOne_pow.toString())
        Log.d("11111111111", mov_minPower.toString())

        Log.d("22222222222", mov_maxTempo.toString())
        Log.d("11111111111", mov_quarterOfThree_tempo.toString())
        Log.d("11111111111", mov_quarterOfTwo_tempo.toString())
        Log.d("11111111111", mov_quarterOfOne_tempo.toString())
        Log.d("11111111111", mov_minTempo.toString())
    }

    fun loading() {
        val loading_video = findViewById<VideoView>(R.id.Loading_practice)
        val topPage = Intent(this, SubActivity01::class.java)

        loading_video.setVideoURI(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.mov_challenge))
        loading_video.start()

        Handler().postDelayed({
            setContentView(R.layout.challenge)
            val BackButton = findViewById<Button>(R.id.BackButton3)

            BackButton.setOnClickListener { startActivity(topPage) }

            samplingRate = (count + 1) / 4
            Log.d("SamplingRate = ", samplingRate.toString())
            loading_boo = false
        }, 4000) //4s
    }

    companion object {
        var lowPassFilter_now: Double = 0.00
        var today_pressure: Double = 0.00 //大気圧
        var lastCounter: Int = 0 //前回押し込み時のCount
        var queue_lowPass: LinkedList<Double> = LinkedList()
    }
}