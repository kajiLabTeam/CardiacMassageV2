// A_mode_Only_moving

package net.harutiro.on_heart;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SubActivity01 extends AppCompatActivity { //起動画面です

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub01);

        final Intent manual_page = new Intent(this, manual.class);
        final Intent PracticeMode = new Intent(this, PracticeMode.class);
        final Intent ChallengeMode = new Intent(this, ChallengeMode.class);

        //NavigationBarの非表示設定
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Button A_mode = findViewById(R.id.A_mode);
        Button B_mode = findViewById(R.id.B_mode);
        Button manual = findViewById(R.id.manual);

        A_mode.setText("");
        B_mode.setText("");
        manual.setText("");

        manual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("CLICK", "Manual");
                startActivity(manual_page);
            }
        });

        A_mode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("CLICK", "A");
                startActivity(PracticeMode);

           }
        });

        B_mode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("CLICK", "B");
                startActivity(ChallengeMode);
            }
        });
    }
}
