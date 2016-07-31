package com.simoncherry.fakecall;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;
import com.turing.androidsdk.SDKInit;
import com.turing.androidsdk.SDKInitBuilder;
import com.turing.androidsdk.TuringApiManager;

import net.frakbot.glowpadbackport.GlowPadView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import turing.os.http.core.ErrorMessage;
import turing.os.http.core.HttpConnectionListener;
import turing.os.http.core.RequestResult;

public class RobotChatActivity extends AppCompatActivity {

    private final String TAG = RobotChatActivity.class.getSimpleName();
    private Context mContext;
    private boolean isSpeaker = false;
    private boolean isMute = false;
    private boolean isKeyBoard = false;
    private boolean isMore = false;

    private SpeechRecognizer mIat;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private boolean isListening = false;

    private SpeechSynthesizer mTts;
    private String voicer = "xiaoyan";
    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue ;
    private int mPercentForBuffering = 0;
    private int mPercentForPlaying = 0;
    private int ret = 0;

    private String mEngineTypeTTS = SpeechConstant.TYPE_CLOUD;
    private String mEngineTypeIAT = SpeechConstant.TYPE_CLOUD;
    private SharedPreferences mSharedPreferencesIAT;
    private SharedPreferences mSharedPreferencesTTS;
    ApkInstaller mInstaller ;
    private Toast mToast;

    private TuringApiManager mTuringApiManager;
    private final String TURING_APIKEY = "ba43961a07e546ed987e2b57473a66dd";
    private final String TURING_SECRET = "cdb46ccaf70cd67d";
    private final String UNIQUEID = "0816203538";


    private GlowPadView glowPad;
    private RelativeLayout layoutOnCall;
    private Chronometer chronometer;
    private Button btnCancelCall;
    private ImageView ivSpeaker;
    private ImageView ivMute;
    private ImageView ivKeyBoard;
    private ImageView ivMore;

    private CountDownTimer cTimer;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    AudioManager audioManager;

    public final int TYPE_RESPONSE = 1024;
    public final int TYPE_RESULT = 2048;

    private Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TYPE_RESPONSE:
                    //tv_response.setText("机器人：" + msg.obj);
                    String text = msg.obj.toString();
                    setTTSParam();
                    int code = mTts.startSpeaking(text, mTtsListener);
                    if (code != ErrorCode.SUCCESS) {
                        if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
                            //未安装则跳转到提示安装页面
                            mInstaller.install();
                        }else {
                            showTip("语音合成失败,错误码: " + code);
                        }
                    }
                    break;
                case TYPE_RESULT:
                    String result = msg.obj.toString();
                    if (!TextUtils.isEmpty(result)) {
                        //tv_send.setText("我：" + result);
                        //edt_text.setText("");
                        mTuringApiManager.requestTuringAPI(result);
                    } else {
                        Toast.makeText(mContext, "识别内容为空", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_chat);
        mContext = RobotChatActivity.this;

        glowPad = (GlowPadView) findViewById(R.id.incomingCallWidget);
        layoutOnCall = (RelativeLayout) findViewById(R.id.layout_on_call);
        chronometer = (Chronometer) findViewById(R.id.tv_time);
        btnCancelCall = (Button) findViewById(R.id.btn_cancel_call);
        ivSpeaker = (ImageView) findViewById(R.id.iv_speaker);
        ivMute = (ImageView) findViewById(R.id.iv_mute);
        ivKeyBoard = (ImageView) findViewById(R.id.iv_keyboard);
        ivMore = (ImageView) findViewById(R.id.iv_more);

        initLayout();
        initSystem();
        initIAT();
        initTTS();
        initTurling();

        cTimer.start();
        vibrator.vibrate(new long[] { 2000, 500, 2000, 500, 2000 }, 0);

        try {
            mediaPlayer.setDataSource(this, RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_RINGTONE));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_speaker:
                    if (isSpeaker) {
                        isSpeaker = false;
                        ivSpeaker.setImageResource(R.drawable.ic_volume_mute_white_48dp);
                        audioManager.setSpeakerphoneOn(false);//关闭扬声器
                        audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
                        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                        audioManager.setMode(AudioManager.MODE_IN_CALL);  //把声音设定成Earpiece（听筒）出来，设定为正在通话中
                    } else {
                        isSpeaker = true;
                        ivSpeaker.setImageResource(R.drawable.ic_volume_up_white_48dp);
                        audioManager.setSpeakerphoneOn(true);
                        audioManager.setMode(AudioManager.MODE_NORMAL);
                    }
                    break;
                case R.id.iv_mute:
                    if (isMute) {
                        isMute = false;
                        ivMute.setImageResource(R.drawable.ic_mic_off_white_48dp);
                    } else {
                        isMute = true;
                        ivMute.setImageResource(R.drawable.ic_mic_white_48dp);
                    }
                    break;
                case R.id.iv_keyboard:
                    if (isKeyBoard) {
                        isKeyBoard = false;
                        ivKeyBoard.setImageResource(R.drawable.ic_keyboard_white_48dp);
                    } else {
                        isKeyBoard = true;
                        ivKeyBoard.setImageResource(R.drawable.ic_keyboard_hide_white_48dp);
                    }
                    break;
                case R.id.iv_more:
                    if (isMore) {
                        isMore = false;
                        ivMore.setImageResource(R.drawable.ic_more_vert_white_48dp);
                    } else {
                        isMore = true;
                        ivMore.setImageResource(R.drawable.ic_more_horiz_white_48dp);
                    }
                    break;
                case R.id.btn_cancel_call:
                    if (mIat.isListening()) {
                        mIat.stopListening();
                    }
                    mIat.cancel();
                    mIatResults.clear();

                    if (mTts.isSpeaking()) {
                        mTts.stopSpeaking();
                    }
                    mTts.destroy();

                    finish();
                    break;
            }
        }
    };

    private void initLayout() {
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
                    vibrator.cancel();
                    mediaPlayer.stop();
                    cTimer.cancel();
                    glowPad.setVisibility(View.GONE);
                    layoutOnCall.setVisibility(View.VISIBLE);

                    chronometer.setFormat("%s");
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();

                    setTTSParam();
                    int code = mTts.startSpeaking("喂，你好，请问是王小明吗？", mTtsListener);
                    if (code != ErrorCode.SUCCESS) {
                        if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
                            //未安装则跳转到提示安装页面
                            mInstaller.install();
                        }else {
                            showTip("语音合成失败,错误码: " + code);
                        }
                    }

                    mIatResults.clear();
                    setIATParam();
                    ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("听写失败,错误码：" + ret);
                    } else {
                        showTip(getString(R.string.text_begin));
                    }

                } else {
                    vibrator.cancel();
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

        ivSpeaker.setOnClickListener(myOnClickListener);
        ivMute.setOnClickListener(myOnClickListener);
        ivKeyBoard.setOnClickListener(myOnClickListener);
        ivMore.setOnClickListener(myOnClickListener);
        btnCancelCall.setOnClickListener(myOnClickListener);
    }

    private void initSystem() {
        mediaPlayer = new MediaPlayer();
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setSpeakerphoneOn(false);//关闭扬声器
//        audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
//        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//        audioManager.setMode(AudioManager.MODE_IN_CALL);  //把声音设定成Earpiece（听筒）出来，设定为正在通话中

        cTimer = new CountDownTimer(30000, 500) {
            public void onTick(long millisUntilFinished) {
                glowPad.ping();
            }

            public void onFinish() {
                mediaPlayer.stop();
                finish();
            }
        };
//        //cTimer.start();
//
//        try {
//            mediaPlayer.setDataSource(this, RingtoneManager
//                    .getDefaultUri(RingtoneManager.TYPE_RINGTONE));
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mInstaller = new  ApkInstaller(RobotChatActivity.this);
    }

    private void initIAT() {
        mIat = SpeechRecognizer.createRecognizer(mContext, mInitListener);
        mSharedPreferencesIAT = getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
    }

    private void initTTS() {
        mTts = SpeechSynthesizer.createSynthesizer(mContext, mTtsInitListener);
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
        mSharedPreferencesTTS = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
    }

    private void initTurling() {
        SDKInitBuilder builder = new SDKInitBuilder(this)
                .setSecret(TURING_SECRET).setTuringKey(TURING_APIKEY).setUniqueId(UNIQUEID);
        SDKInit.init(builder,new com.turing.androidsdk.InitListener() {
            @Override
            public void onFail(String error) {
                Log.e(TAG, error);
                Toast.makeText(mContext, "turingSDK初始化失败！ msg:" + error, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onComplete() {
                // 获取userid成功后，才可以请求Turing服务器，需要请求必须在此回调成功，才可正确请求
                mTuringApiManager = new TuringApiManager(mContext);
                mTuringApiManager.setHttpListener(myHttpConnectionListener);
                Toast.makeText(mContext, "turingSDK初始化成功！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);
        //Log.e(TAG, "printResult！");
    }

    private com.iflytek.cloud.InitListener mInitListener = new com.iflytek.cloud.InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }
        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));

            if (error.getErrorCode() == 10118) {
                mIatResults.clear();
                ret = mIat.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("听写失败,错误码：" + ret);
                } else {
                    showTip(getString(R.string.text_begin));
                }
            }
        }
        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            //Log.d(TAG, results.getResultString());
            printResult(results);
            if (isLast) {
                // TODO 最后的结果
//                mIat.stopListening();
//                showTip("停止识别");
//                btn_listen.setText("开始识别");
//                isListening = false;

                StringBuffer resultBuffer = new StringBuffer();
                for (String key : mIatResults.keySet()) {
                    resultBuffer.append(mIatResults.get(key));
                }

                myHandler.obtainMessage(TYPE_RESULT,
                        resultBuffer.toString()).sendToTarget();

                Log.e(TAG, "听写：" + resultBuffer.toString());
            }
        }
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private com.iflytek.cloud.InitListener mTtsInitListener = new com.iflytek.cloud.InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }
        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }
        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }
        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
            }

            mIatResults.clear();
            ret = mIat.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                showTip("听写失败,错误码：" + ret);
            } else {
                showTip(getString(R.string.text_begin));
            }
        }
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    HttpConnectionListener myHttpConnectionListener = new HttpConnectionListener() {
        @Override
        public void onSuccess(RequestResult result) {
            if (result != null) {
                try {
                    Log.e(TAG, result.getContent().toString());
                    JSONObject result_obj = new JSONObject(result.getContent()
                            .toString());
                    if (result_obj.has("text")) {
                        Log.e(TAG, result_obj.get("text").toString());
                        myHandler.obtainMessage(TYPE_RESPONSE,
                                result_obj.get("text")).sendToTarget();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException:" + e.getMessage());
                }
            }
        }
        @Override
        public void onError(ErrorMessage errorMessage) {
            Log.e(TAG, errorMessage.getMessage());
        }
    };

    public void setIATParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineTypeIAT);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mSharedPreferencesIAT.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferencesIAT.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferencesIAT.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferencesIAT.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    private void setTTSParam(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if(mEngineTypeTTS.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, mSharedPreferencesTTS.getString("speed_preference", "50"));
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, mSharedPreferencesTTS.getString("pitch_preference", "50"));
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferencesTTS.getString("volume_preference", "50"));
        }else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferencesTTS.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
    }

    @Override
    protected void onResume() {
        //移动数据统计分析
        FlowerCollector.onResume(mContext);
        FlowerCollector.onPageStart(TAG);
        super.onResume();
    }
    @Override
    protected void onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG);
        FlowerCollector.onPause(mContext);

        if (mIat.isListening()) {
            mIat.stopListening();
        }
        mIat.cancel();
        mIatResults.clear();

        if (mTts.isSpeaking()) {
            mTts.stopSpeaking();
        }
        mTts.destroy();

        showTip("停止识别");
        //btn_listen.setText("开始识别");
        isListening = false;

        super.onPause();
    }
}
