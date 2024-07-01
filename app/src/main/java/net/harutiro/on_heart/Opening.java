package net.harutiro.on_heart;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.VideoView;
public class Opening extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opening);

        final Intent Sub_Activity = new Intent(this, SubActivity01.class);

        VideoView Video1 = findViewById(R.id.Opening_video);

        Video1.setVideoURI(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.opening));
        Video1.start();

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(Sub_Activity);

            }
        }, 7000);
    }
}
