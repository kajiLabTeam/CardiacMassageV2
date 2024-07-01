package net.harutiro.on_heart

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.VideoView

class Opening : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.opening)

        val Sub_Activity = Intent(this, MainActivity::class.java)

        val Video1 = findViewById<VideoView>(R.id.Opening_video)

        Video1.setVideoURI(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.opening))
        Video1.start()

        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        Handler().postDelayed({ startActivity(Sub_Activity) }, 7000)
    }
}
