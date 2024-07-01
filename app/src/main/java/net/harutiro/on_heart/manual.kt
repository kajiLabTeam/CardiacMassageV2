package net.harutiro.on_heart

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView

class manual : Activity() {
    //説明書です
    var num: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manual)

        val Sub_Activity = Intent(this, SubActivity01::class.java)

        val title = findViewById<TextView>(R.id.manual_title)
        val text = findViewById<TextView>(R.id.manual_text)
        val b1 = findViewById<Button>(R.id.manual_button1)
        val b2 = findViewById<Button>(R.id.manual_button2)
        val b3 = findViewById<Button>(R.id.manual_button3)
        val b4 = findViewById<Button>(R.id.manual_button4)
        val next = findViewById<ImageButton>(R.id.allow1)
        val back = findViewById<ImageButton>(R.id.allow2)

        //NavigationBarの非表示設定
        val decor = window.decorView
        decor.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        val Video1 = findViewById<VideoView>(R.id.outline_video1)
        Video1.setVideoURI(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.settings))

        val tf = intArrayOf(0)
        Video1.setOnClickListener {
            if (tf[0] % 2 == 0) {
                Video1.pause()
            } else {
                Video1.start()
            }
            tf[0]++
            Log.d("------------", tf[0].toString())
        }

        val Video2 = findViewById<VideoView>(R.id.outline_video2)
        Video2.setVideoURI(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.information))

        val ft = intArrayOf(0)
        Video2.setOnClickListener {
            if (ft[0] % 2 == 0) {
                Video1.pause()
            } else {
                Video1.start()
            }
            ft[0]++
            Log.d("------------", ft[0].toString())
        }

        b1.text = "アプリ概要"
        b2.text = "必要な物"
        b3.text = "使い方1/2"
        b4.text = "使い方2/2"

        b1.setOnClickListener {
            change_manual(num, 1)
            num = 1
        }

        b2.setOnClickListener {
            change_manual(num, 2)
            num = 2
        }

        b3.setOnClickListener {
            change_manual(num, 3)
            num = 3
        }

        b4.setOnClickListener {
            change_manual(num, 4)
            num = 4
        }

        //pink .setTextColor(Color.rgb(255, 20, 147));
        //gray .setTextColor(Color.rgb(89, 84, 87));
        if (num == 1) {
            title.text = "アプリ概要"
            text.text = "これは、簡易的に\n心臓マッサージを練習する\nアプリケーションです"
            b1.setTextColor(Color.rgb(255, 20, 147))
        }

        back.setOnClickListener {
            if (num == 1) {
                startActivity(Sub_Activity)
            } else {
                change_manual(num, num - 1)
            }
            num--
        }

        next.setOnClickListener {
            if (num != 4) {
                change_manual(num, num + 1)
                num++
            } else {
                startActivity(Sub_Activity)
            }
        }
    }

    fun change_bg(num: Int) {
        if (num == 1) (findViewById<View>(R.id.outline_image) as ImageView).setImageResource(R.drawable.outline)
        if (num == 2) (findViewById<View>(R.id.outline_image) as ImageView).setImageResource(R.drawable.ziploc)
    }

    fun change_manual(num: Int, to: Int) {
        val title = findViewById<TextView>(R.id.manual_title)
        val text = findViewById<TextView>(R.id.manual_text)
        val b1 = findViewById<Button>(R.id.manual_button1)
        val b2 = findViewById<Button>(R.id.manual_button2)
        val b3 = findViewById<Button>(R.id.manual_button3)
        val b4 = findViewById<Button>(R.id.manual_button4)
        val outline_image = findViewById<ImageView>(R.id.outline_image)
        val outline_video1 = findViewById<VideoView>(R.id.outline_video1)
        val outline_video2 = findViewById<VideoView>(R.id.outline_video2)


        val text1 = "これは、簡易的に\n心臓マッサージを練習する\nアプリケーションです"
        val text2 =
            "AsahiKASEI\nZiplocフリーザーバッグL\n\n\n動作確認済みデバイス\nXperia-XZ2 GooglePixel4"

        val Video1 = findViewById<VideoView>(R.id.outline_video1)
        Video1.setVideoURI(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.settings))
        val tf = intArrayOf(0)
        Video1.setOnClickListener {
            if (tf[0] % 2 == 0) {
                Video1.pause()
            } else {
                Video1.start()
            }
            tf[0]++
            Log.d("------------", tf[0].toString())
        }

        val Video2 = findViewById<VideoView>(R.id.outline_video2)
        Video2.setVideoURI(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.information))

        val ft = intArrayOf(0)
        Video2.setOnClickListener {
            if (ft[0] % 2 == 0) {
                Video2.pause()
            } else {
                Video2.start()
            }
            ft[0]++
            Log.d("------------", ft[0].toString())
        }


        if (num == 1) {
            if (to == 2) {
                title.text = "必要な物"
                text.text = text2
                b2.setTextColor(Color.rgb(255, 20, 147))
                change_bg(2)
            } else if (to == 3) {
                title.text = "使い方1/2"
                text.text = ""
                b3.setTextColor(Color.rgb(255, 20, 147))
                outline_image.visibility = View.INVISIBLE
                outline_video1.visibility = View.VISIBLE
                Video1.start()
            } else if (to == 4) {
                title.text = "使い方2/2"
                text.text = ""
                b4.setTextColor(Color.rgb(255, 20, 147))
                outline_image.visibility = View.INVISIBLE
                outline_video2.visibility = View.VISIBLE
                Video2.start()
            }
            if (to != num) b1.setTextColor(Color.rgb(89, 84, 87))
        } else if (num == 2) {
            if (to == 1) {
                title.text = "アプリ概要"
                text.text = text1
                b1.setTextColor(Color.rgb(255, 20, 147))
                change_bg(1)
            } else if (to == 3) {
                title.text = "使い方1/2"
                text.text = ""
                b3.setTextColor(Color.rgb(255, 20, 147))
                outline_image.visibility = View.INVISIBLE
                outline_video1.visibility = View.VISIBLE
                Video1.start()
            } else if (to == 4) {
                title.text = "使い方2/2"
                text.text = ""
                b4.setTextColor(Color.rgb(255, 20, 147))
                outline_image.visibility = View.INVISIBLE
                outline_video2.visibility = View.VISIBLE
                Video2.start()
            }
            if (to != num) b2.setTextColor(Color.rgb(89, 84, 87))
        } else if (num == 3) {
            if (to == 1) {
                title.text = "アプリ概要"
                text.text = text1
                b1.setTextColor(Color.rgb(255, 20, 147))
                outline_video1.visibility = View.INVISIBLE
                outline_image.visibility = View.VISIBLE
                change_bg(1)
            } else if (to == 2) {
                title.text = "必要な物"
                text.text = text2
                b2.setTextColor(Color.rgb(255, 20, 147))
                outline_video1.visibility = View.INVISIBLE
                outline_image.visibility = View.VISIBLE
                change_bg(2)
            } else if (to == 4) {
                title.text = "使い方2/2"
                text.text = ""
                b4.setTextColor(Color.rgb(255, 20, 147))
                outline_video1.visibility = View.INVISIBLE
                outline_video2.visibility = View.VISIBLE
                Video2.start()
            }
            if (to != num) b3.setTextColor(Color.rgb(89, 84, 87))
        } else if (num == 4) {
            if (to == 1) {
                title.text = "アプリ概要"
                text.text = text1
                b1.setTextColor(Color.rgb(255, 20, 147))
                outline_video2.visibility = View.INVISIBLE
                outline_image.visibility = View.VISIBLE
                change_bg(1)
            } else if (to == 2) {
                title.text = "必要な物"
                text.text = text2
                b2.setTextColor(Color.rgb(255, 20, 147))
                outline_video2.visibility = View.INVISIBLE
                outline_image.visibility = View.VISIBLE
                change_bg(2)
            } else if (to == 3) {
                title.text = "使い方1/2"
                text.text = ""
                b3.setTextColor(Color.rgb(255, 20, 147))
                outline_video2.visibility = View.INVISIBLE
                outline_video1.visibility = View.VISIBLE
                Video1.start()
            }
            if (to != num) b4.setTextColor(Color.rgb(89, 84, 87))
        }

        setLink(to)
    }

    public override fun onResume() {
        val outline_video1 = findViewById<VideoView>(R.id.outline_video1)
        val outline_video2 = findViewById<VideoView>(R.id.outline_video2)


        val Video1 = findViewById<VideoView>(R.id.outline_video1)
        Video1.setVideoURI(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.settings))

        val tf = intArrayOf(0)
        Video1.setOnClickListener {
            if (tf[0] % 2 == 0) {
                Video1.pause()
            } else {
                Video1.start()
            }
            tf[0]++
            Log.d("------------", tf[0].toString())
        }

        val Video2 = findViewById<VideoView>(R.id.outline_video2)
        Video2.setVideoURI(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.information))

        val ft = intArrayOf(0)
        Video2.setOnClickListener {
            if (ft[0] % 2 == 0) {
                Video2.pause()
            } else {
                Video2.start()
            }
            ft[0]++
            Log.d("------------", ft[0].toString())
        }

        super.onResume()
        Log.d("called", "onResume called")
        Video1.seekTo(0)
        Video1.start() //Or use resume()
        Video2.seekTo(0)
        Video2.start() //Or use resume()
    }

    fun setLink(nextNum: Int) {
        val linkText = findViewById<TextView>(R.id.link)

        if (nextNum == 2) {
            val mMethod = LinkMovementMethod.getInstance()
            linkText.movementMethod = mMethod
            val url =
                "https://www.amazon.co.jp/ジップロック-フリーザーバッグ-L-12枚入/dp/B08LL8F8SH?ref_=ast_sto_dp"
            val link: CharSequence = Html.fromHtml("<a href=\"$url\">Amazonで購入</a>")
            linkText.text = link
        } else {
            linkText.text = ""
        }
    }
}