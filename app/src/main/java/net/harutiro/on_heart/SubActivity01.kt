// A_mode_Only_moving
package net.harutiro.on_heart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import net.harutiro.on_heart.ChallengeMode
import net.harutiro.on_heart.PracticeMode

class SubActivity01 : AppCompatActivity() {
    //起動画面です
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub01)

        val manual_page = Intent(this, manual::class.java)
        val PracticeMode = Intent(this, PracticeMode::class.java)
        val ChallengeMode = Intent(this, ChallengeMode::class.java)

        //NavigationBarの非表示設定
        val decor = window.decorView
        decor.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        val A_mode = findViewById<Button>(R.id.A_mode)
        val B_mode = findViewById<Button>(R.id.B_mode)
        val manual = findViewById<Button>(R.id.manual)

        A_mode.text = ""
        B_mode.text = ""
        manual.text = ""

        manual.setOnClickListener {
            Log.d("CLICK", "Manual")
            startActivity(manual_page)
        }

        A_mode.setOnClickListener {
            Log.d("CLICK", "A")
            startActivity(PracticeMode)
        }

        B_mode.setOnClickListener {
            Log.d("CLICK", "B")
            startActivity(ChallengeMode)
        }
    }
}
