package net.harutiro.on_heart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public class ChallengeMode extends Activity { //よろしくお願いします。
    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    static double lowPassFilter_now = 0.00;
    static double today_pressure = 0.00; //大気圧
    int pressured_counter = 0; //押し込み回数カウンター
    static int lastCounter = 0; //前回押し込み時のCount
    LinkedList<Double> check = new LinkedList<Double>();
    LinkedList<Double> rhythmCounter = new LinkedList<>();
    LinkedList<Double> power_manager = new LinkedList<>();
    static LinkedList<Double> queue_lowPass = new LinkedList<>();
    ArrayList<Float> graphData = new ArrayList<Float>();
    private LineChart mChart;
    boolean playing = false;
    boolean result = false;
    boolean summary = false;
    ArrayList<Double> power_ave = new ArrayList<>();
    ArrayList<Double> tempo_ave = new ArrayList<>();


    ArrayList<Double> powOfPressured = new ArrayList<>();
    ArrayList<Integer> tempoOfCount = new ArrayList<>();
    int lastCount;
    int samplingRate = 0;
    int count = 0;
    boolean loading_boo = true;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        //NavigationBarの非表示設定
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        //画面がスリープ状態にならないようにする設定
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //最初4秒間 気圧セッティング時間のためのローディング画面へ
        //気圧センサの取得数によっては終わらないが問題無し
        loading();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorEventListener = new SensorEventListener() {

            int wallCount = 0;
            int QueueSizeReseter = 0;
            LinkedList<Double> queue = new LinkedList<>();
            double measured_pressure = 0.00;
            double Max_pressure = 0.00 , sum_pressure = 0.00, ave_pressure = 0.00;
            double Min_pressure = 100.00;
            boolean decrease = false;

            int tmp_counter;

            public void onSensorChanged(SensorEvent event) {

                float[] val = event.values.clone();
                double pressure;

                ImageView iv = findViewById(R.id.trial);
                TextView feedText = findViewById(R.id.challenge_text);

                //テスト中 = playing
                if (playing) {
                    wallCount += 2;
                    iv.setTranslationY(wallCount); //max 950

                    if(wallCount == 1080) {
                        feedText.setTextColor(Color.rgb(89, 84, 87));
                        feedText.setText("お疲れ様でした。\n結果画面に移行します。");
                    }

                    if(wallCount == 1180){
                        setContentView(R.layout.summary);
                        summaryInfo();
                        summary = true;
                        playing = false;
                    }
                }


                pressure = ((double) round(val[0] * 100) / 100);
                //Log.d("PRESSURE",String.valueOf(pressure));

                //こいつが起動してから取得した気圧センサの値の数
                count++;
                Log.d("count ------------", String.valueOf(count));
                //if(count > 100)imageResize(measured_pressure,imageWidth,imageHeight);

                //PracticeModeと同じ部分は省略(Class分けてなくてごめんね)
                if (wallCount != 1180) {
                    if (count <= 100) {
                        queue.add(pressure);
                        Log.d("----------", String.valueOf(pressure));

                        if(!loading_boo) feedText.setText("少々お待ちください");

                        if (count == 100) {
                            for (int i = 25; i < 100; i++) {
                                sum_pressure += queue.get(i);
                                //Log.d("count",String.valueOf(queue.get(i)));
                            }
                            ave_pressure = sum_pressure / (100 - 25);
                            today_pressure = ave_pressure;
                            Log.d("today's_pressure", String.valueOf(today_pressure));

                            QueueSizeReseter = queue.size();
                            for (int i = 0; i < QueueSizeReseter; i++)
                                queue.remove(0); //all clear (queue)

                        }

                    } else {

                        measured_pressure = pressure - today_pressure;
                        queue.add(measured_pressure);
                        if (queue.size() > 3) queue.remove(0);

                        //queue.add(measured_pressure);
                        //Log.d("added_this_pressure = ", String.valueOf(measured_pressure));

                        if (lowPassFilter_now > 1) {  //lowpass.get(0) != 0 atta

                            for (int i = 0; i < queue_lowPass.size(); i++) {
                                if (Max_pressure < queue_lowPass.get(i))
                                    Max_pressure = queue_lowPass.get(i);
                                if (Min_pressure > queue_lowPass.get(i))
                                    Min_pressure = queue_lowPass.get(i);
                            }
                            //Log.d("Max = ", String.valueOf(Max_pressure));
                            //Log.d("Min = ", String.valueOf(Min_pressure));

                            if (Max_pressure == queue_lowPass.get(0) && !decrease) {
                                pressured_counter++;
                                //if(playing) feedText.setText(String.valueOf(pressured_counter));

                                if (!playing && count - tmp_counter < 25) {
                                    Button BackButton = findViewById(R.id.BackButton3);
                                    BackButton.setVisibility(View.INVISIBLE);

                                    playing = true;
                                    feedText.setText("");

                                }

                                if (playing) {
                                    powOfPressured.add(Max_pressure);
                                    if (tempoOfCount.size() >= 1)
                                        tempoOfCount.add(count - lastCount);
                                    else tempoOfCount.add(0);
                                    lastCount = count;
                                }

                                tmp_counter = count; //前回押し込み時のcount
                                //Log.i("pressured_counter = ", String.valueOf(pressured_counter));
                                //feedText.setText(String.valueOf(pressured_counter));

                                decrease = true;
                                Log.d("decrease = ", "true");
                                Log.d("pressured_counter = ", String.valueOf(pressured_counter));

                                rhythm_manager(count, pressured_counter, Max_pressure);
                            }

                            if (Min_pressure == queue_lowPass.get(0) && decrease) {
                                decrease = false;
                                Log.d("decrease = ", "false");
                            }

                            Max_pressure = 0.00;
                            Min_pressure = 100.00;

//                    Log.d("queue.size = ", String.valueOf(queue.size()));
//                    Log.d("queue_first = ", String.valueOf(queue.get(0)));
//                    Log.d("queue_last = ", String.valueOf(queue.get(queue.size()-1)));
                        }
                    }

                    if (count > 100) CheckingPressureDifferences(pressure);

                    if (count > 100 && queue.size() == 3)
                        LowPassFilter(queue.get(0), queue.get(1), queue.get(2));

                    if (count == 101) feedText.setText("任意のタイミングで\n開始してください。");
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
        super.onPause();
    }

    public void LowPassFilter(double l3, double l2, double l1) {
        lowPassFilter_now = (l3 + l2 + l1) / 3 * 1.2;
        queue_lowPass.add(lowPassFilter_now);
        if(queue_lowPass.size() > 5) queue_lowPass.remove(0);
        if(playing)graphData.add((float)lowPassFilter_now);
    }

    public void rhythm_manager(int count, double pressured_counter, double power) {
        int rhythm_Counter_Reseter;
        double power_sum = 0.00;
        double tempo_sum = 0.00;
        int CountReseter;

        TextView feedBackText = findViewById(R.id.challenge_text);
        TextView resultText = findViewById(R.id.result_text);

        power_manager.add(power);

        if (rhythmCounter.size() != 0 && count - lastCounter >= 200) {
            rhythm_Counter_Reseter = rhythmCounter.size();
            for (int i = 0; i < rhythm_Counter_Reseter; i++) {
                power_manager.remove(0);
                rhythmCounter.remove(0);
            }
        }

        if (pressured_counter >= 2) {
            if (rhythmCounter.size() == 0) {
                rhythmCounter.add(0.0);
                lastCounter = count;
            } else if (rhythmCounter.size() < 4) { //if rhythmCounter = 0,1,2,3
                rhythmCounter.add((double) count - lastCounter);
                lastCounter = count;
            } else {
                rhythmCounter.add((double) count - lastCounter);
                lastCounter = count;

                for (int i = 0; i < rhythmCounter.size(); i++) {
                    tempo_sum += rhythmCounter.get(i);
                }
                tempo_ave.add(tempo_sum / (rhythmCounter.size() - 1));

                CountReseter = rhythmCounter.size();
                for (int i = 0; i < CountReseter; i++) rhythmCounter.remove(0); //all clear (queue)


                for(int i=0; i<power_manager.size(); i++){
                    power_sum += power_manager.get(i);
                }
                power_ave.add(power_sum / power_manager.size());

                CountReseter = power_manager.size();
                for (int i = 0; i < CountReseter; i++) power_manager.remove(0); //all clear (queue)

                //feedBackText.setText("Pow :" + power_ave + "\nSpd :" + flame_ave);
                Log.d("POWER_AVE_SIZE",String.valueOf(power_ave.size()));
                Log.d("TEMPO_AVE_SIZE",String.valueOf(tempo_ave.size()));


            }
        }
    }

    public void CheckingPressureDifferences(double pre) {
        double sum = 0.00;
        double mean;
        double v_sum = 0.00;
        double variance;
        int checkSizeReseter;

        TextView feedText = findViewById(R.id.FeedBackText);

        if (check.size() < 200) { //4s
            check.add(pre);
        } else {
            for (int i = 0; i < check.size(); i++) sum += check.get(i);
            mean = sum / check.size();

            for (int i = 0; i < check.size(); i++) v_sum += abs(mean - check.get(i));
            variance = v_sum / check.size();
            Log.d("variance", String.valueOf(variance));

            if (variance < 0.05) {
                today_pressure = mean;
                Log.d("today_pre", String.valueOf(mean));
                pressured_counter = 0;
                //b1.setText("開始してください。");
                //L1.setBackgroundColor(Color.parseColor("#f8f8ff"));
                checkSizeReseter = check.size();
                for (int i = 0; i < checkSizeReseter; i++) check.remove(0); //all clear (check)


            } else {
                check.remove(0);
            }
            check.add(pre);
        }
        //Log.d("checker_size",String.valueOf(check.size()));
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Sensor> sl = mSensorManager.getSensorList(Sensor.TYPE_PRESSURE);
        if (!sl.isEmpty()) {
            // 気圧センサーがあれば，開始する
            mSensorManager.registerListener(mSensorEventListener,
                    sl.get(0), SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onBackPressed() {
        final Intent Sub_Activity = new Intent(this, SubActivity01.class);
        startActivity(Sub_Activity);
    }

    //一応実装してるけど中途半端です サマリー画面で画面をタップするとテスト中に取得した気圧のグラフが見られます
    public  void  graphExport(){

            TextView resultText = findViewById(R.id.result_text);

            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < graphData.size(); i++) {
                values.add(new Entry(i, graphData.get(i), null, null));
            }

//              for (int i = 0; i < testData.length; i++) {
//                values.add(new Entry(i, testData[i], null, null));
//            }


            LineDataSet set1;

            if (mChart.getData() != null &&
                    mChart.getData().getDataSetCount() > 0) {

                set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
                set1.setValues(values);
                mChart.getData().notifyDataChanged();
                mChart.notifyDataSetChanged();

            } else {
                // create a dataset and give it a type
                set1 = new LineDataSet(values,"");

                //mChart.getDescription().setEnabled(false);
                set1.setLineWidth(2F);     //線の太さ
                set1.setColor(Color.rgb(239, 247, 246)); //線の色
                set1.setDrawValues(false);
                set1.setDrawCircles(false);

                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSets.add(set1); // add the datasets

                // create a data object with the datasets
                LineData lineData = new LineData(dataSets);

                // set data
                mChart.setData(lineData);

                resultText.setTextColor(Color.rgb(239, 247, 246));
                resultText.setTextSize(27);
                resultText.setText("");
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
    public void summaryInfo(){

        final Intent topPage = new Intent(this, SubActivity01.class);

        Button BackButton = findViewById(R.id.BackButton2);

        BackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(topPage);
            }
        });

        for (int i=0; i<powOfPressured.size()-1; i++) {
            for (int j=powOfPressured.size()-1; j>i; j--) {

                if (powOfPressured.get(j - 1) > powOfPressured.get(j)) {
                    double powTmp = powOfPressured.get(j-1);
                    powOfPressured.set(j-1 ,powOfPressured.get(j));
                    powOfPressured.set(j, powTmp);
                }
                if (tempoOfCount.get(j - 1) > tempoOfCount.get(j)) {
                    int tmpTmp = tempoOfCount.get(j-1);
                    tempoOfCount.set(j-1 ,tempoOfCount.get(j));
                    tempoOfCount.set(j, tmpTmp);
                }

            }
        }

        Log.d("PRESSURE---------------------",String.valueOf(powOfPressured.size()));
        Log.d("TEMPO------------------------",String.valueOf(tempoOfCount.size()));

        for(int i=0; i<powOfPressured.size(); i++){
            Log.d("PRESSURE---------------------",String.valueOf(powOfPressured.get(i)));
        }

        for(int i=0; i<powOfPressured.size(); i++){
            Log.d("TEMPPPPP---------------------",String.valueOf(tempoOfCount.get(i)));
        }

        int quarterLength = (int)powOfPressured.size() / 4;

        double maxPower = powOfPressured.get(powOfPressured.size() - 1);
        double quarterOfThree_pow = powOfPressured.get(quarterLength * 3);
        double quarterOfTwo_pow = powOfPressured.get(quarterLength * 2);
        double quarterOfOne_pow = powOfPressured.get(quarterLength);
        double minPower = powOfPressured.get(0);

        double maxTempo = tempoOfCount.get(tempoOfCount.size() - 1);
        double quarterOfThree_tempo = tempoOfCount.get(quarterLength * 3);
        double quarterOfTwo_tempo = tempoOfCount.get(quarterLength * 2);
        double quarterOfOne_tempo = tempoOfCount.get(quarterLength);
        double minTempo = tempoOfCount.get(1);

        ImageView pp_max = findViewById(R.id.summary_pp_up2);
        ImageView pp_q3 = findViewById(R.id.summary_pp_up1);
        ImageView pp_q2 = findViewById(R.id.summary_pp);
        ImageView pp_q1 = findViewById(R.id.summary_pp_low1);
        ImageView pp_min = findViewById(R.id.summary_pp_low2);

        ImageView pt_max = findViewById(R.id.summary_pt_up2);
        ImageView pt_q3 = findViewById(R.id.summary_pt_up1);
        ImageView pt_q2 = findViewById(R.id.summary_pt);
        ImageView pt_q1 = findViewById(R.id.summary_pt_low1);
        ImageView pt_min = findViewById(R.id.summary_pt_low2);

        double powerApMax = 85 * samplingRate / 25;//85,75,12.5,13.3
        double powerApMin = 75 * samplingRate / 25;
        double tempoApMax = 12.5 * samplingRate / 25;;
        double tempoApMin = 13.3 * samplingRate / 25;;

        double powerApAve = (powerApMax + powerApMin) / 2;
        double tempoApAve = (tempoApMax + tempoApMin) / 2;

        double mov_maxPower = (1 - (maxPower / powerApAve));
        double mov_quarterOfThree_pow = (1 - (quarterOfThree_pow / powerApAve));
        double mov_quarterOfTwo_pow = (1 - (quarterOfTwo_pow / powerApAve));
        double mov_quarterOfOne_pow = (1 - (quarterOfOne_pow / powerApAve));
        double mov_minPower = (1 - (minPower / powerApAve));

        double mov_maxTempo = (1 - (maxTempo / tempoApAve));
        double mov_quarterOfThree_tempo = (1 - (quarterOfThree_tempo / tempoApAve));
        double mov_quarterOfTwo_tempo = (1 - (quarterOfTwo_tempo / tempoApAve));
        double mov_quarterOfOne_tempo = (1 - (quarterOfOne_tempo / tempoApAve));
        double mov_minTempo = (1 - (minTempo / tempoApAve));

        pp_max.setTranslationY((float) mov_maxPower * 800);
        pp_q3.setTranslationY((float) mov_quarterOfThree_pow * 800);
        pp_q2.setTranslationY((float) mov_quarterOfTwo_pow * 800);
        pp_q1.setTranslationY((float) mov_quarterOfOne_pow * 800);
        pp_min.setTranslationY((float) mov_minPower * 800);

        pt_max.setTranslationX((float) mov_maxTempo * 800);
        pt_q3.setTranslationX((float) mov_quarterOfThree_tempo * 800);
        pt_q2.setTranslationX((float) mov_quarterOfTwo_tempo * 800);
        pt_q1.setTranslationX((float) mov_quarterOfOne_tempo * 800);
        pt_min.setTranslationX((float) mov_minTempo * 800);

        Log.d("11111111111", String.valueOf(mov_maxPower));
        Log.d("11111111111", String.valueOf(mov_quarterOfThree_pow));
        Log.d("11111111111", String.valueOf(mov_quarterOfTwo_pow));
        Log.d("11111111111", String.valueOf(mov_quarterOfOne_pow));
        Log.d("11111111111", String.valueOf(mov_minPower));

        Log.d("22222222222", String.valueOf(mov_maxTempo));
        Log.d("11111111111", String.valueOf(mov_quarterOfThree_tempo));
        Log.d("11111111111", String.valueOf(mov_quarterOfTwo_tempo));
        Log.d("11111111111", String.valueOf(mov_quarterOfOne_tempo));
        Log.d("11111111111", String.valueOf(mov_minTempo));

    }

    public void loading () {

        VideoView loading_video = findViewById(R.id.Loading_practice);
        final Intent topPage = new Intent(this, SubActivity01.class);

        loading_video.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.mov_challenge));
        loading_video.start();

        new Handler().postDelayed(new Runnable() {
            // Runnable型のインスタンス化と定義
            @Override
            public void run() {
                setContentView(R.layout.challenge);

                Button BackButton = findViewById(R.id.BackButton3);

                BackButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(topPage);
                    }
                });

                samplingRate = (count + 1) / 4;
                Log.d("SamplingRate = " , String.valueOf(samplingRate));

                loading_boo = false;
            }
        }, 4000); //4s

    }
}