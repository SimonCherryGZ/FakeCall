package com.simoncherry.fakecall;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;

import net.frakbot.glowpadbackport.GlowPadView;

public class FakeCallActivity extends AppCompatActivity {

    private GlowPadView glowPad;
    private RelativeLayout layoutOnCall;
    private Chronometer chronometer;
    private Button btnCancelCall;

    private CountDownTimer cTimer;
    private MediaPlayer mediaPlayer = new MediaPlayer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        glowPad = (GlowPadView) findViewById(R.id.incomingCallWidget);
        layoutOnCall = (RelativeLayout) findViewById(R.id.layout_on_call);
        chronometer = (Chronometer) findViewById(R.id.tv_time);
        btnCancelCall = (Button) findViewById(R.id.btn_cancel_call);


        glowPad.setOnTriggerListener(new GlowPadView.OnTriggerListener() {
            @Override
            public void onGrabbed(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onReleased(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onTrigger(View v, int target) {
                //Toast.makeText(FakeCallActivity.this, "Target triggered! ID=" + target, Toast.LENGTH_SHORT).show();
                glowPad.reset(true);

                if(target == 0) {
                    mediaPlayer.stop();
                    cTimer.cancel();
                    glowPad.setVisibility(View.GONE);
                    layoutOnCall.setVisibility(View.VISIBLE);

                    chronometer.setFormat("%s");
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                } else {
                    mediaPlayer.stop();
                    cTimer.cancel();
                    finish();
                }
            }

            @Override
            public void onGrabbedStateChange(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onFinishFinalAnimation() {
                // Do nothing
            }
        });

        btnCancelCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                finish();
            }
        });

        cTimer = new CountDownTimer(15000, 500) {
            public void onTick(long millisUntilFinished) {
                glowPad.ping();
            }

            public void onFinish() {
                mediaPlayer.stop();
                finish();
            }
        };
        cTimer.start();

        try {
            mediaPlayer.setDataSource(this, RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_RINGTONE));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
