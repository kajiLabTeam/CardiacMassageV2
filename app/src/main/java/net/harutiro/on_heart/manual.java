package net.harutiro.on_heart;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class manual extends Activity { //説明書です

    int num = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual);

        final Intent Sub_Activity = new Intent(this, SubActivity01.class);

        final TextView title = findViewById(R.id.manual_title);
        final TextView text = findViewById(R.id.manual_text);
        final Button b1 = findViewById(R.id.manual_button1);
        final Button b2 = findViewById(R.id.manual_button2);
        final Button b3 = findViewById(R.id.manual_button3);
        final Button b4 = findViewById(R.id.manual_button4);
        ImageButton next = findViewById(R.id.allow1);
        ImageButton back = findViewById(R.id.allow2);

        //NavigationBarの非表示設定
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        final VideoView Video1 = findViewById(R.id.outline_video1);
        Video1.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.settings));

        final int[] tf = {0};
        Video1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(tf[0] % 2 == 0) {
                    Video1.pause();
                }else{
                    Video1.start();
                }
                tf[0]++;
                Log.d("------------",String.valueOf(tf[0]));
            }
        });

        final VideoView Video2 = findViewById(R.id.outline_video2);
        Video2.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.information));

        final int[] ft = {0};
        Video2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(ft[0] % 2 == 0) {
                    Video1.pause();
                }else{
                    Video1.start();
                }
                ft[0]++;
                Log.d("------------",String.valueOf(ft[0]));
            }
        });

        b1.setText("アプリ概要");
        b2.setText("必要な物");
        b3.setText("使い方1/2");
        b4.setText("使い方2/2");

        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                change_manual(num , 1);
                num = 1;
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                change_manual(num , 2);
                num = 2;
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                change_manual(num , 3);
                num = 3;
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                change_manual(num , 4);
                num = 4;
            }
        });

        //pink .setTextColor(Color.rgb(255, 20, 147));
        //gray .setTextColor(Color.rgb(89, 84, 87));

        if (num == 1){
            title.setText("アプリ概要");
            text.setText("これは、簡易的に\n心臓マッサージを練習する\nアプリケーションです");
            b1.setTextColor(Color.rgb(255, 20, 147));
        }

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(num == 1){
                    startActivity(Sub_Activity);
                }else {
                    change_manual(num,num-1);
                }
                num --;
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(num != 4) {
                    change_manual(num, num + 1);
                    num++;
                }else{
                    startActivity(Sub_Activity);
                }
            }
        });

    }

    public void change_bg(int num){
        if(num == 1)((ImageView) findViewById(R.id.outline_image)).setImageResource(R.drawable.outline);
        if(num == 2)((ImageView) findViewById(R.id.outline_image)).setImageResource(R.drawable.ziploc);
    }

    public void change_manual (int num , int to) {

        final TextView title = findViewById(R.id.manual_title);
        final TextView text = findViewById(R.id.manual_text);
        final Button b1 = findViewById(R.id.manual_button1);
        final Button b2 = findViewById(R.id.manual_button2);
        final Button b3 = findViewById(R.id.manual_button3);
        final Button b4 = findViewById(R.id.manual_button4);
        final ImageView outline_image = findViewById(R.id.outline_image);
        final VideoView outline_video1 = findViewById(R.id.outline_video1);
        final VideoView outline_video2 = findViewById(R.id.outline_video2);


        String text1 = "これは、簡易的に\n心臓マッサージを練習する\nアプリケーションです";
        String text2 = "AsahiKASEI\nZiplocフリーザーバッグL\n\n\n動作確認済みデバイス\nXperia-XZ2 GooglePixel4";

        final VideoView Video1 = findViewById(R.id.outline_video1);
        Video1.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.settings));
        final int[] tf = {0};
        Video1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (tf[0] % 2 == 0) {
                    Video1.pause();
                } else {
                    Video1.start();
                }
                tf[0]++;
                Log.d("------------", String.valueOf(tf[0]));
            }
        });

        final VideoView Video2 = findViewById(R.id.outline_video2);
        Video2.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.information));

        final int[] ft = {0};
        Video2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ft[0] % 2 == 0) {
                    Video2.pause();
                } else {
                    Video2.start();
                }
                ft[0]++;
                Log.d("------------", String.valueOf(ft[0]));
            }
        });


        if (num == 1) {
            if (to == 2) {
                title.setText("必要な物");
                text.setText(text2);
                b2.setTextColor(Color.rgb(255, 20, 147));
                change_bg(2);
            } else if (to == 3) {
                title.setText("使い方1/2");
                text.setText("");
                b3.setTextColor(Color.rgb(255, 20, 147));
                outline_image.setVisibility(View.INVISIBLE);
                outline_video1.setVisibility(View.VISIBLE);
                Video1.start();
            } else if (to == 4) {
                title.setText("使い方2/2");
                text.setText("");
                b4.setTextColor(Color.rgb(255, 20, 147));
                outline_image.setVisibility(View.INVISIBLE);
                outline_video2.setVisibility(View.VISIBLE);
                Video2.start();
            }
            if(to != num) b1.setTextColor(Color.rgb(89, 84, 87));

        } else if (num == 2) {
            if (to == 1) {
                title.setText("アプリ概要");
                text.setText(text1);
                b1.setTextColor(Color.rgb(255, 20, 147));
                change_bg(1);
            }else if(to == 3){
                title.setText("使い方1/2");
                text.setText("");
                b3.setTextColor(Color.rgb(255, 20, 147));
                outline_image.setVisibility(View.INVISIBLE);
                outline_video1.setVisibility(View.VISIBLE);
                Video1.start();
            }else if(to == 4){
                title.setText("使い方2/2");
                text.setText("");
                b4.setTextColor(Color.rgb(255, 20, 147));
                outline_image.setVisibility(View.INVISIBLE);
                outline_video2.setVisibility(View.VISIBLE);
                Video2.start();
            }
            if(to != num) b2.setTextColor(Color.rgb(89, 84, 87));

        }else if (num == 3) {
            if (to == 1) {
                title.setText("アプリ概要");
                text.setText(text1);
                b1.setTextColor(Color.rgb(255, 20, 147));
                outline_video1.setVisibility(View.INVISIBLE);
                outline_image.setVisibility(View.VISIBLE);
                change_bg(1);
            }else if(to == 2){
                title.setText("必要な物");
                text.setText(text2);
                b2.setTextColor(Color.rgb(255, 20, 147));
                outline_video1.setVisibility(View.INVISIBLE);
                outline_image.setVisibility(View.VISIBLE);
                change_bg(2);
            }else if(to == 4){
                title.setText("使い方2/2");
                text.setText("");
                b4.setTextColor(Color.rgb(255, 20, 147));
                outline_video1.setVisibility(View.INVISIBLE);
                outline_video2.setVisibility(View.VISIBLE);
                Video2.start();
            }
            if(to != num) b3.setTextColor(Color.rgb(89, 84, 87));

        }else if (num == 4) {
            if (to == 1) {
                title.setText("アプリ概要");
                text.setText(text1);
                b1.setTextColor(Color.rgb(255, 20, 147));
                outline_video2.setVisibility(View.INVISIBLE);
                outline_image.setVisibility(View.VISIBLE);
                change_bg(1);
            }else if(to == 2){
                title.setText("必要な物");
                text.setText(text2);
                b2.setTextColor(Color.rgb(255, 20, 147));
                outline_video2.setVisibility(View.INVISIBLE);
                outline_image.setVisibility(View.VISIBLE);
                change_bg(2);
            }else if(to == 3){
                title.setText("使い方1/2");
                text.setText("");
                b3.setTextColor(Color.rgb(255, 20, 147));
                outline_video2.setVisibility(View.INVISIBLE);
                outline_video1.setVisibility(View.VISIBLE);
                Video1.start();
            }
            if(to != num) b4.setTextColor(Color.rgb(89, 84, 87));
        }

        setLink(to);
    }

    @Override
    public void onResume() {
        final VideoView outline_video1 = findViewById(R.id.outline_video1);
        final VideoView outline_video2 = findViewById(R.id.outline_video2);


        final VideoView Video1 = findViewById(R.id.outline_video1);
        Video1.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.settings));

        final int[] tf = {0};
        Video1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (tf[0] % 2 == 0) {
                    Video1.pause();
                } else {
                    Video1.start();
                }
                tf[0]++;
                Log.d("------------", String.valueOf(tf[0]));
            }
        });

        final VideoView Video2 = findViewById(R.id.outline_video2);
        Video2.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.information));

        final int[] ft = {0};
        Video2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ft[0] % 2 == 0) {
                    Video2.pause();
                } else {
                    Video2.start();
                }
                ft[0]++;
                Log.d("------------", String.valueOf(ft[0]));
            }
        });

        super.onResume();
        Log.d("called", "onResume called");
        Video1.seekTo(0);
        Video1.start(); //Or use resume()
        Video2.seekTo(0);
        Video2.start(); //Or use resume()
    }

    public void setLink(int nextNum){
        final TextView linkText = findViewById(R.id.link);

        if(nextNum == 2) {
            MovementMethod mMethod = LinkMovementMethod.getInstance();
            linkText.setMovementMethod(mMethod);
            String url = "https://www.amazon.co.jp/ジップロック-フリーザーバッグ-L-12枚入/dp/B08LL8F8SH?ref_=ast_sto_dp";
            CharSequence link = Html.fromHtml("<a href=\"" + url + "\">Amazonで購入</a>");
            linkText.setText(link);
        }else{
            linkText.setText("");
        }
    }


}