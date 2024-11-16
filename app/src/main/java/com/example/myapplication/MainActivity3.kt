package com.example.myapplication

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Vibrator
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat



class MainActivity3 : AppCompatActivity() {

    private var timeSelected:Int =0
    private var countDown:CountDownTimer?=null
    private var progressTime=0
    private var pauseOffSet: Long =0
    private var isStart=true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main3)

        //Add Time Duration
        val addButton :ImageButton=findViewById(R.id.btnAdd)
        addButton.setOnClickListener{
            setTime()//Open the dialog

        }

        //Start/Pause Timer
        val startBtn:Button=findViewById(R.id.playPauseButton)
        startBtn.setOnClickListener{
            startTimeSetUp()
        }

        //reset
        val resetButton:ImageButton=findViewById(R.id.btnReset)
        resetButton.setOnClickListener{
            resetTime()
        }

        //Add more 15 sec
        val addTime:TextView=findViewById(R.id.timeAdd)
        addTime.setOnClickListener{
            addExtraTime()
        }
    }



    private fun addExtraTime(){
        val progressBar:ProgressBar=findViewById(R.id.goTimer)
        if (timeSelected!=0)
        {
            timeSelected+=15
            progressBar.max=timeSelected
            timePause()
            startTimer(pauseOffSet)//Restart with upload time
            Toast.makeText(this,"15 sec Added",Toast.LENGTH_SHORT).show()

        }
    }
    private fun resetTime(){
        if (countDown!=null)
        {
            countDown!!.cancel()//countdown stop
            progressTime=0
            timeSelected=0
            pauseOffSet=0
            countDown=null

            val startButton:Button=findViewById(R.id.playPauseButton)
            startButton.text="Start"
            isStart=true
            val progressBar=findViewById<ProgressBar>(R.id.goTimer)
            progressBar.progress=0
            val timeLeft:TextView=findViewById(R.id.LeftTtime)
            timeLeft.text="0"
        }
    }



    private fun timePause(){
        if (countDown!=null){
            countDown!!.cancel()
        }
    }

    //start or resume
    private fun startTimeSetUp(){
        val startButton:Button=findViewById(R.id.playPauseButton)
        if (timeSelected>progressTime){

            if (isStart){
                startButton.text="Pause"
                startTimer(pauseOffSet)
                isStart=false
            }else{
                isStart=true
                startButton.text="Resume"
                timePause()
            }

        }else{
            Toast.makeText(this,"Enter Time",Toast.LENGTH_SHORT).show()
        }

    }


    //start or resume countdown
    private fun startTimer(pauseOffsetL: Long){
        val progressBar=findViewById<ProgressBar>(R.id.goTimer)
        progressBar.progress=progressTime
        countDown=object :CountDownTimer(
            (timeSelected*1000).toLong()-pauseOffsetL*1000,1000)

        {
            override fun onTick(p0: Long) {
                progressTime++
                pauseOffSet=timeSelected.toLong()-p0/1000 //calculate remaining time
                progressBar.progress=timeSelected-progressTime
                val timeLeft:TextView=findViewById(R.id.LeftTtime)
                timeLeft.text=(timeSelected-progressTime).toString()//Update time
            }

            override fun onFinish() {
                resetTime()
                showNotification()
                vibratePhone()
                Toast.makeText(this@MainActivity3,"Times Up",Toast.LENGTH_SHORT).show()
            }

        }.start()
    }

    //Dialog box set Time
    private fun setTime(){
        val dialogTime=Dialog(this)
        dialogTime.setContentView(R.layout.add_dialog)
        val setTime=dialogTime.findViewById<EditText>(R.id.goalGetTime)
        val timeLeft:TextView=findViewById(R.id.LeftTtime)
        val startButton:Button=findViewById(R.id.playPauseButton)
        val progressBar=findViewById<ProgressBar>(R.id.goTimer)
        dialogTime.findViewById<Button>(R.id.okButton).setOnClickListener{
            if (setTime.text.isEmpty())
            {
                Toast.makeText(this,"Enter Time Duration",Toast.LENGTH_SHORT).show()
            }
            else{
                resetTime()
                timeLeft.text=setTime.text
                startButton.text="Start"
                timeSelected=setTime.text.toString().toInt()
                progressBar.max=timeSelected
            }
            dialogTime.dismiss()
        }
        dialogTime.show()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDown!=null)
        {
            countDown?.cancel()
            progressTime=0
        }
    }

    //Check notification permission(Android 13+)
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // not need permisson for older version
        }
    }

    //when times Up show notification
    private fun showNotification() {
        if (!checkNotificationPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1
            )
            return
        }

        val channelId = "timer_channel"
        val notificationId = 101

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer Notification"
            val descriptionText = "Notification when timer finishes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time's Up!")
            .setContentText("Your fixed time's Up")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(notificationSound)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    private fun checkVibratePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.VIBRATE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }


    private fun vibratePhone() {
        if (checkVibratePermission()) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        500, android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(500)
            }
        } else {
            // Request vibrate permission if not granted
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.VIBRATE), 2
            )
        }
    }


}