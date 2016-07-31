package com.simoncherry.fakecall.test;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;
import com.simoncherry.fakecall.util.ApkInstaller;
import com.simoncherry.fakecall.util.FucUtil;
import com.simoncherry.fakecall.setting.IatSettings;
import com.simoncherry.fakecall.util.JsonParser;
import com.simoncherry.fakecall.R;
import com.simoncherry.fakecall.setting.TtsSettings;
import com.turing.androidsdk.SDKInit;
import com.turing.androidsdk.SDKInitBuilder;
import com.turing.androidsdk.TuringApiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import turing.os.http.core.ErrorMessage;
import turing.os.http.core.HttpConnectionListener;
import turing.os.http.core.RequestResult;

public class RobotChatActivity extends AppCompatActivity {

    private final String TAG = RobotChatActivity.class.getSimpleName();

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "xiaoyan";
    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue ;
    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;
    private boolean isListening = false;
    private int ret = 0; // 函数调用返回值

    // 引擎类型
    private String mEngineTypeTTS = SpeechConstant.TYPE_CLOUD;
    private String mEngineTypeIAT = SpeechConstant.TYPE_CLOUD;
    // 语记安装助手类
    ApkInstaller mInstaller ;

    private Toast mToast;
    private SharedPreferences mSharedPreferencesIAT;
    private SharedPreferences mSharedPreferencesTTS;

    private TuringApiManager mTuringApiManager;

    /**
     * 申请的turing的apikey
     *  **/
    private final String TURING_APIKEY = "ba43961a07e546ed987e2b57473a66dd";
    /**
     * 申请的secret
     * **/
    private final String TURING_SECRET = "cdb46ccaf70cd67d";
    /**
     * 填写一个任意的标示，没有具体要求，，但一定要写，
     * **/
    private final String UNIQUEID = "0816203538";

    private TextView tv_send;
    private TextView tv_response;
    private EditText edt_text;
    private Button btn_send;
    private Button btn_listen;
    private Button btn_setting;
    private Button btn_person_select;
    private RadioGroup mRadioGroupTTS;
    private RadioGroup mRadioGroupIAT;

    public final int TYPE_RESPONSE = 1024;
    public final int TYPE_RESULT = 2048;

    private Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TYPE_RESPONSE:
                    tv_response.setText("机器人：" + msg.obj);

                    FlowerCollector.onEvent(RobotChatActivity.this, "tts_play");
                    String text = msg.obj.toString();
                    // 设置参数
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
                        tv_send.setText("我：" + result);
                        edt_text.setText("");
                        mTuringApiManager.requestTuringAPI(result);
                    } else {
                        Toast.makeText(RobotChatActivity.this, "识别内容为空", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_chat);

        tv_send = (TextView) findViewById(R.id.tv_send);
        tv_response = (TextView) findViewById(R.id.tv_response);
        edt_text = (EditText) findViewById(R.id.edt_text);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_listen = (Button) findViewById(R.id.btn_listen);
        btn_setting = (Button) findViewById(R.id.btn_setting);
        btn_person_select = (Button) findViewById(R.id.tts_btn_person_select);
        mRadioGroupTTS = (RadioGroup) findViewById(R.id.tts_rediogroup);
        mRadioGroupIAT = (RadioGroup) findViewById(R.id.radioGroup);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = edt_text.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    tv_send.setText("我：" + result);
                    edt_text.setText("");
                    mTuringApiManager.requestTuringAPI(result);
                } else {
                    Toast.makeText(RobotChatActivity.this, "发送内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isListening) {
                    mIat.stopListening();
                    mIat.cancel();
                    mIatResults.clear();
                    showTip("停止识别");
                    btn_listen.setText("开始识别");
                    isListening = false;
                } else {
                    showTip("开始识别");
                    btn_listen.setText("停止识别");
                    isListening = true;

                    // 移动数据分析，收集开始听写事件
                    FlowerCollector.onEvent(RobotChatActivity.this, "iat_recognize");

                    mIatResults.clear();
                    // 设置参数
                    setIATParam();
                    boolean isShowDialog = mSharedPreferencesIAT.getBoolean(
                            getString(R.string.pref_key_iat_show), false);
                    if (isShowDialog) {
                        // 显示听写对话框
                        mIatDialog.setListener(mRecognizerDialogListener);
                        mIatDialog.show();
                        showTip(getString(R.string.text_begin));
                    } else {
                        // 不显示听写对话框
                        ret = mIat.startListening(mRecognizerListener);
                        if (ret != ErrorCode.SUCCESS) {
                            showTip("听写失败,错误码：" + ret);
                        } else {
                            showTip(getString(R.string.text_begin));
                        }
                    }

                }
            }
        });

        btn_person_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPresonSelectDialog();
            }
        });

        mRadioGroupTTS.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.tts_radioCloud:
                        mEngineTypeTTS = SpeechConstant.TYPE_CLOUD;
                        break;
                    case R.id.tts_radioLocal:
                        mEngineTypeTTS =  SpeechConstant.TYPE_LOCAL;
                        /**
                         * 选择本地合成
                         * 判断是否安装语记,未安装则跳转到提示安装页面
                         */
                        if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                            mInstaller.install();
                        }
                        break;
                    default:
                        break;
                }

            }
        } );

        mRadioGroupIAT.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.iatRadioCloud:
                        mEngineTypeIAT = SpeechConstant.TYPE_CLOUD;
                        break;
                    case R.id.iatRadioLocal:
                        mEngineTypeIAT = SpeechConstant.TYPE_LOCAL;
                        /**
                         * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
                         */
                        if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                            mInstaller.install();
                        } else {
                            String result = FucUtil.checkLocalResource();
                            if (!TextUtils.isEmpty(result)) {
                                showTip(result);
                            }
                        }
                        break;
                    case R.id.iatRadioMix:
                        mEngineTypeIAT = SpeechConstant.TYPE_MIX;
                        /**
                         * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
                         */
                        if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                            mInstaller.install();
                        } else {
                            String result = FucUtil.checkLocalResource();
                            if (!TextUtils.isEmpty(result)) {
                                showTip(result);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intents = new Intent(RobotChatActivity.this, IatSettings.class);
                startActivity(intents);
            }
        });

        init();
    }

    private void init() {
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(RobotChatActivity.this, mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(RobotChatActivity.this, mInitListener);

        mSharedPreferencesIAT = getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);

        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(RobotChatActivity.this, mTtsInitListener);
        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);

        mSharedPreferencesTTS = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);

        mInstaller = new  ApkInstaller(RobotChatActivity.this);

        // turingSDK初始化
        SDKInitBuilder builder = new SDKInitBuilder(this)
                .setSecret(TURING_SECRET).setTuringKey(TURING_APIKEY).setUniqueId(UNIQUEID);
        SDKInit.init(builder,new com.turing.androidsdk.InitListener() {
            @Override
            public void onFail(String error) {
                Log.e(TAG, error);
                Toast.makeText(RobotChatActivity.this, "turingSDK初始化失败！ msg:" + error, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onComplete() {
                // 获取userid成功后，才可以请求Turing服务器，需要请求必须在此回调成功，才可正确请求
                mTuringApiManager = new TuringApiManager(RobotChatActivity.this);
                mTuringApiManager.setHttpListener(myHttpConnectionListener);
                Toast.makeText(RobotChatActivity.this, "turingSDK初始化成功！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 网络请求回调
     */
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

    /**
     * 初始化监听。
     */
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

    /**
     * 合成回调监听。
     */
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

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    /**
     * 参数设置
     */
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

    /**
     * 初始化监听器。
     */
    private com.iflytek.cloud.InitListener mInitListener = new com.iflytek.cloud.InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    /**
     * 听写监听器。
     */
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
            Log.d(TAG, results.getResultString());
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
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
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

//        StringBuffer resultBuffer = new StringBuffer();
//        for (String key : mIatResults.keySet()) {
//            resultBuffer.append(mIatResults.get(key));
//        }

//        mResultText.setText(resultBuffer.toString());
//        mResultText.setSelection(mResultText.length());
//        myHandler.obtainMessage(TYPE_RESULT,
//                resultBuffer.toString()).sendToTarget();
        Log.e(TAG, "printResult！");
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            //printResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

    };

    /**
     * 参数设置
     */
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

    private int selectedNum = 0;
    /**
     * 发音人选择。
     */
    private void showPresonSelectDialog() {
        switch (mRadioGroupTTS.getCheckedRadioButtonId()) {
            // 选择在线合成
            case R.id.tts_radioCloud:
                new AlertDialog.Builder(this).setTitle("在线合成发音人选项")
                        .setSingleChoiceItems(mCloudVoicersEntries, // 单选框有几项,各是什么名字
                                selectedNum, // 默认的选项
                                new DialogInterface.OnClickListener() { // 点击单选框后的处理
                                    public void onClick(DialogInterface dialog,
                                                        int which) { // 点击了哪一项
                                        voicer = mCloudVoicersValue[which];
//                                        if ("catherine".equals(voicer) || "henry".equals(voicer) || "vimary".equals(voicer)) {
//                                            ((EditText) findViewById(R.id.tts_text)).setText(R.string.text_tts_source_en);
//                                        }else {
//                                            ((EditText) findViewById(R.id.tts_text)).setText(R.string.text_tts_source);
//                                        }
                                        selectedNum = which;
                                        dialog.dismiss();
                                    }
                                }).show();
                break;

            // 选择本地合成
            case R.id.tts_radioLocal:
                if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                    mInstaller.install();
                }else {
                    SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_TTS);
                }
                break;
            default:
                break;
        }
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
        FlowerCollector.onResume(RobotChatActivity.this);
        FlowerCollector.onPageStart(TAG);
        super.onResume();
    }
    @Override
    protected void onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG);
        FlowerCollector.onPause(RobotChatActivity.this);

        mIat.stopListening();
        mIat.cancel();
        mIatResults.clear();
        showTip("停止识别");
        btn_listen.setText("开始识别");
        isListening = false;

        super.onPause();
    }
}
