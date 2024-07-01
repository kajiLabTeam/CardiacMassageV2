package net.harutiro.on_heart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public class PracticeMode extends Activity { //よろしくお願いします。
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
    boolean manual = false; //true = 一定時間押し込みが無かった際に気圧をリセットする設定を消す
    boolean Pause = true;
    int count = 0;
    int rateChecker = 0;
    int samplingRate = 0;

    private MediaPlayer mediaPlayer;

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

        //画面右上のテンポ支援ボタンのオーティオセッティング
        audioSetup();
        mediaPlayer.setLooping(true);
        //Log.d("MediaCheck","-------------------------------");


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorEventListener = new SensorEventListener() {

            int QueueSizeReseter = 0;
            LinkedList<Double> queue = new LinkedList<>();
            double measured_pressure = 0.00;
            double Max_pressure, sum_pressure, ave_pressure = 0.00;
            double Min_pressure = 100.00;
            boolean decrease = false;


            public void onSensorChanged(SensorEvent event) {

                float[] val = event.values.clone();
                double pressure;

                TextView feedText = findViewById(R.id.FeedBackText);
                TextView an = findViewById(R.id.AnnounceText);
                //an.setText("");

                pressure = ((double) round(val[0] * 100) / 100);
                //Log.d("PRESSURE",String.valueOf(pressure));

                //こいつで気圧センサの取得数を計測
                count++;
                //Log.d("count ------------",String.valueOf(count));
                //if(count > 100)imageResize(measured_pressure,imageWidth,imageHeight);

                //気圧セッティング(大気圧の測定)
                if (count <= 100) {
                    queue.add(pressure);
                    //b1.setTextSize(45);
                    //feedText.setText("気圧設定中です。");

                    //セッティング終了 26-100個目のデータを採用し平均
                    if (count == 100) {
                        for (int i = 25; i < 100; i++) {
                            sum_pressure += queue.get(i);
                            //Log.d("count",String.valueOf(queue.get(i)));
                        }
                        ave_pressure = sum_pressure / (100 - 25);
                        today_pressure = ave_pressure;
                        Log.d("today's_pressure", String.valueOf(today_pressure));

                        QueueSizeReseter = queue.size();
                        for (int i = 0; i < QueueSizeReseter; i++) queue.remove(0); //all clear (queue)

                        feedText.setText("練習を開始してください");
                        samplingRate = (samplingRate - rateChecker) / 3;
                        //feedText.setText("SamplingRate = " + samplingRate + "Hz");
                    }

                } else {

                    //measured_pressure = 現在取れている気圧の値 - 大気圧 = 上昇気圧
                    measured_pressure = pressure - today_pressure;
                    queue.add(measured_pressure);
                    if(queue.size() > 3) queue.remove(0);

                    //queue.add(measured_pressure);
                    //Log.d("added_this_pressure = ", String.valueOf(measured_pressure));

                    //押し込まれた時の処理
                    if (lowPassFilter_now > 1) {  //lowpass.get(0) != 0 atta

                        for (int i = 0; i < queue_lowPass.size(); i++) {
                            if (Max_pressure < queue_lowPass.get(i)) Max_pressure = queue_lowPass.get(i);
                            if (Min_pressure > queue_lowPass.get(i)) Min_pressure = queue_lowPass.get(i);
                        }

                        if (Max_pressure == queue_lowPass.get(0) && !decrease) {
                            pressured_counter++;
                            //Log.i("pressured_counter = ", String.valueOf(pressured_counter));
                            //feedText.setText(String.valueOf(pressured_counter));

                            decrease = true;
                            //Log.d("decrease = ", "true");
                            feedText.setText(String.valueOf(pressured_counter));
                            Log.d("pressured_counter = ", String.valueOf(pressured_counter));
                            Log.d("Max = ", String.valueOf(Max_pressure));
                            Log.d("Min = ", String.valueOf(Min_pressure));

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

                //一定時間押し込まれなかった時に 大気圧をリセットする
                if (count > 100) CheckingPressureDifferences(pressure);

                //移動平均フィルタ(前々回気圧, 前回気圧, 今回気圧の3つ)
                if (count > 100 && queue.size() == 3)
                    LowPassFilter(queue.get(0), queue.get(1), queue.get(2));

            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
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
    public void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
        super.onPause();
    }

    public static void LowPassFilter(double l3, double l2, double l1) {
        lowPassFilter_now = (l3 + l2 + l1) / 3 * 1.2;
        queue_lowPass.add(lowPassFilter_now);
        if(queue_lowPass.size() > 5) queue_lowPass.remove(0);
    }

    public void feedBack(double Tempo, double Power) {
        ImageView point_power = findViewById(R.id.point_power);
        ImageView point_tempo = findViewById(R.id.point_tempo);
        TextView feedText = findViewById(R.id.FeedBackText);

        //推奨値の設定 Max=許容最大値 Min=許容最小値
        double powerApMax = 85 * samplingRate / 25;
        double powerApMin = 75 * samplingRate / 25;
        double tempoApMax = 12.5 * samplingRate / 25;
        double tempoApMin = 13.3 * samplingRate / 25;

        double powerApAve = (powerApMax + powerApMin) / 2;
        double tempoApAve = (tempoApMax + tempoApMin) / 2;

        double perOfPower = (Power / powerApAve) * 100;
        double perOfTempo = (Tempo / tempoApAve) * 100;

        int moveY = (int)(100 - perOfPower) * 8 ;
        int moveX = (int)(100 - perOfTempo) * 10 * 2;

        // \n" + "F:" + FlameByOnce + "P:" + text_Power
        point_power.setTranslationY(moveY);
        point_tempo.setTranslationX(moveX);

        BigDecimal bd1 = new BigDecimal(String.valueOf(perOfPower));
        BigDecimal bd2 = new BigDecimal(String.valueOf(perOfTempo));
        BigDecimal perOfPower_2pt = bd1.setScale(2, RoundingMode.HALF_UP);
        BigDecimal perOfTempo_2pt = bd2.setScale(2, RoundingMode.HALF_UP);

        //feedText.setText(String.valueOf(Power));
        //feedText.setText("Pow:"+ perOfPower_2pt +"\n" + "Tem:" + perOfTempo_2pt);
        //feedText.setText("T" + Tempo + "P" + Power);


    }

    //強さの推定 テンポの測定を行う
    public void rhythm_manager(int count, double pressured_counter, double power) {
        int rhythm_Counter_Reseter;
        double power_sum = 0.00;
        double flame_sum = 0.00;
        double power_ave;
        double flame_ave;
        int CountReseter;

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
                    flame_sum += rhythmCounter.get(i);
                }
                flame_ave = flame_sum / (rhythmCounter.size() - 1);

                CountReseter = rhythmCounter.size();
                for (int i = 0; i < CountReseter; i++) rhythmCounter.remove(0); //all clear (queue)


                for(int i=0; i<power_manager.size(); i++){
                    power_sum += power_manager.get(i);
                }
                power_ave = power_sum / power_manager.size();

                CountReseter = power_manager.size();
                for (int i = 0; i < CountReseter; i++) power_manager.remove(0); //all clear (queue)


                feedBack(flame_ave, power_ave);

            }
        }
    }

    //一定時間押し込まれなかった時に 大気圧をリセットする
    public void CheckingPressureDifferences(double pre) {
        double sum = 0.00;
        double mean;
        double v_sum = 0.00;
        double variance;
        int checkSizeReseter;

        ImageView point_power = findViewById(R.id.point_power);
        ImageView point_tempo = findViewById(R.id.point_tempo);
        TextView feedText = findViewById(R.id.FeedBackText);

        if (check.size() < 200) { //4s
            check.add(pre);
        } else {
            for (int i = 0; i < check.size(); i++) sum += check.get(i);
            mean = sum / check.size();

            for (int i = 0; i < check.size(); i++) v_sum += abs(mean - check.get(i));
            variance = v_sum / check.size();
            //Log.d("variance", String.valueOf(variance));

            if (variance < 0.05 && !manual) {
                today_pressure = mean;
                Log.d("today_pre", String.valueOf(mean));
                pressured_counter = 0;
                feedText.setText("練習を開始してください");
                //L1.setBackgroundColor(Color.parseColor("#f8f8ff"));
                checkSizeReseter = check.size();
                for (int i = 0; i < checkSizeReseter; i++) check.remove(0); //all clear (check)

                point_power.setTranslationY(0);
                point_tempo.setTranslationX(0);

            } else {
                check.remove(0);
            }
            check.add(pre);
        }
        //Log.d("checker_size",String.valueOf(check.size()));
    }

    //テンポ支援ボタンの ON/OFF設定
    private final View.OnClickListener onClick_button = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageButton soundButton = findViewById(R.id.soundButton);

            if (!Pause) {
                mediaPlayer.pause();
                Pause = true;
                soundButton.setBackgroundResource(R.drawable.sound_off);
            } else {
                mediaPlayer.start();
                Pause = false;
                soundButton.setBackgroundResource(R.drawable.sound_on);
            }
        }
    };

    //テンポ支援セットアップ
    private boolean audioSetup(){
        boolean fileCheck = false;

        // インタンスを生成
        mediaPlayer = new MediaPlayer();

        // rawにファイルがある場合
        mediaPlayer = MediaPlayer.create(this, R.raw.two);
        // 音量調整を端末のボタンに任せる
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        fileCheck = true;

        return fileCheck;
    }

    //テンポ支援Viewセッティング
    private void setViews(){
        ImageButton Mute_button = findViewById(R.id.soundButton);
        Mute_button.setOnClickListener(onClick_button);
    }

    //Pixel4などで OS指定の"戻る"を使用された時に正しく戻るため
    @Override
    public void onBackPressed(){
        final Intent Sub_Activity= new Intent(this, SubActivity01.class);
        mediaPlayer.stop();
        startActivity(Sub_Activity);
    }

    //ローディング画面
    public void loading () {

        VideoView loading_video = findViewById(R.id.Loading_practice);

        loading_video.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.mov_practice));
        loading_video.start();

        final Intent topPage = new Intent(this, SubActivity01.class);

        new Handler().postDelayed(new Runnable() {
            // Runnable型のインスタンス化と定義
            @Override
            public void run() {
                setContentView(R.layout.practice);
                setViews();

                Button BackButton = findViewById(R.id.BackButton);

                BackButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(topPage);
                    }
                });

                ImageView pp = findViewById(R.id.point_power);
                ImageView pt = findViewById(R.id.point_tempo);
                pp.setTranslationY(50);
                pt.setTranslationX(-50);

                samplingRate = count;
                Log.d("SamplingRate = " , String.valueOf(samplingRate));
            }
        }, 4000); //4s

        new Handler().postDelayed(new Runnable() {
            // Runnable型のインスタンス化と定義
            @Override
            public void run() {
                rateChecker = count;
            }
        }, 1000); //1s
    }
}