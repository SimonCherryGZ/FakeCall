package com.simoncherry.fakecall;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.turing.androidsdk.InitListener;
import com.turing.androidsdk.SDKInit;
import com.turing.androidsdk.SDKInitBuilder;
import com.turing.androidsdk.TuringApiManager;

import org.json.JSONException;
import org.json.JSONObject;

import turing.os.http.core.ErrorMessage;
import turing.os.http.core.HttpConnectionListener;
import turing.os.http.core.RequestResult;

public class TuringTestActivity extends AppCompatActivity {

    private final String TAG = TuringTestActivity.class.getSimpleName();

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

    public final int RESONESE = 1024;

    private Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case RESONESE:
                    tv_response.setText("机器人：" + msg.obj);
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turing_test);

        tv_send = (TextView) findViewById(R.id.tv_send);
        tv_response = (TextView) findViewById(R.id.tv_response);
        edt_text = (EditText) findViewById(R.id.edt_text);
        btn_send = (Button) findViewById(R.id.btn_send);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = edt_text.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    tv_send.setText("我：" + result);
                    edt_text.setText("");
                    mTuringApiManager.requestTuringAPI(result);
                } else {
                    Toast.makeText(TuringTestActivity.this, "发送内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        init();
    }

    private void init() {
        // turingSDK初始化
        SDKInitBuilder builder = new SDKInitBuilder(this)
                .setSecret(TURING_SECRET).setTuringKey(TURING_APIKEY).setUniqueId(UNIQUEID);
        SDKInit.init(builder,new InitListener() {
            @Override
            public void onFail(String error) {
                Log.e(TAG, error);
                Toast.makeText(TuringTestActivity.this, "turingSDK初始化失败！ msg:" + error, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onComplete() {
                // 获取userid成功后，才可以请求Turing服务器，需要请求必须在此回调成功，才可正确请求
                mTuringApiManager = new TuringApiManager(TuringTestActivity.this);
                mTuringApiManager.setHttpListener(myHttpConnectionListener);
                Toast.makeText(TuringTestActivity.this, "turingSDK初始化成功！", Toast.LENGTH_SHORT).show();
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
                        myHandler.obtainMessage(RESONESE,
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
}
