package com.simoncherry.fakecall.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.simoncherry.fakecall.R;
import com.simoncherry.fakecall.adapter.ChatAdapter;
import com.simoncherry.fakecall.bean.ChatBean;
import com.turing.androidsdk.TuringApiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import turing.os.http.core.ErrorMessage;
import turing.os.http.core.HttpConnectionListener;
import turing.os.http.core.RequestResult;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private final String TAG = ChatFragment.class.getSimpleName();

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

    private ListView chatList;
    private List<ChatBean> list;
    private ChatAdapter adapter;

    private TextView tvTitleHead;
    private ImageView ivTitleLeft;
    private EditText editText;
    private Button btnSend;

    private Context mContext;

    private String[] ContactNameEntries;
    private String[] ChatLogEntries;
    private int index = 0;
    public final int RESONESE = 1024;

    private Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case RESONESE:
                    addChatMsg2List(msg.obj.toString(), false);
                    break;
                default:
                    break;
            }
        }
    };


    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(int index) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        chatFragment.setArguments(args);
        return chatFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        chatList = (ListView) view.findViewById(R.id.list_chat);
        tvTitleHead = (TextView) view.findViewById(R.id.topbar_title);
        ivTitleLeft = (ImageView) view.findViewById(R.id.iv_back);
        editText = (EditText) view.findViewById(R.id.edt_text);
        btnSend = (Button) view.findViewById(R.id.btn_send);

        if (getArguments() != null) {
            index = getArguments().getInt("index");
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        // turingSDK初始化
//        SDKInitBuilder builder = new SDKInitBuilder(mContext)
//                .setSecret(TURING_SECRET).setTuringKey(TURING_APIKEY).setUniqueId(UNIQUEID);
//        SDKInit.init(builder,new InitListener() {
//            @Override
//            public void onFail(String error) {
//                Log.e(TAG, error);
//                Toast.makeText(mContext, "turingSDK初始化失败！ msg:" + error, Toast.LENGTH_SHORT).show();
//            }
//            @Override
//            public void onComplete() {
//                // 获取userid成功后，才可以请求Turing服务器，需要请求必须在此回调成功，才可正确请求
//                mTuringApiManager = new TuringApiManager(mContext);
//                mTuringApiManager.setHttpListener(myHttpConnectionListener);
//                Toast.makeText(mContext, "turingSDK初始化成功！", Toast.LENGTH_SHORT).show();
//            }
//        });

        ContactNameEntries = getResources().getStringArray(R.array.contact_name_entries);
        tvTitleHead.setText(ContactNameEntries[index]);

        list = new ArrayList<>();
        loadMockChat(index);
        setAdapter(list);
        adapter.notifyDataSetChanged();

        ivTitleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = editText.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    editText.setText("");
                    addChatMsg2List(result, true);
                    mTuringApiManager.requestTuringAPI(result);
                } else {
                    Toast.makeText(mContext, "发送内容不能为空", Toast.LENGTH_SHORT).show();
                }
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

    private void setAdapter(List<ChatBean> list) {
        adapter = new ChatAdapter(mContext, list);
        chatList.setAdapter(adapter);
    }

    private void loadMockChat(int index) {
//        for (int i=0; i<10; i++) {
//            ChatBean chatBean = new ChatBean();
//            chatBean.setText("模拟对话——" + i);
//            if (i%2 == 0) {
//                chatBean.setMine(false);
//            } else {
//                chatBean.setMine(true);
//            }
//            list.add(chatBean);
//        }
        if (index >= 0 && index <= 17) {
            String entryName = "chat_history_" + index;
            int resId = getResources().getIdentifier(entryName, "array", mContext.getPackageName());
            //ChatLogEntries = getResources().getStringArray(R.array.contact_name_entries);
            ChatLogEntries = getResources().getStringArray(resId);

            for (int i=0; i<ChatLogEntries.length; i++) {
                ChatBean chatBean = new ChatBean();
                chatBean.setText(ChatLogEntries[i]);
                if (i%2 == 0) {
                    chatBean.setMine(false);
                } else {
                    chatBean.setMine(true);
                }
                list.add(chatBean);
            }
        }
    }

    private void addChatMsg2List(String text, boolean isMine) {
        ChatBean chatBean = new ChatBean();
        chatBean.setText(text);
        chatBean.setMine(isMine);
        list.add(chatBean);
        adapter.notifyDataSetChanged();
        scroll2Bottom();
    }

    private void scroll2Bottom() {
        chatList.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (chatList != null && list != null && list.size() > 0) {
                    chatList.smoothScrollToPosition(list.size() - 1);
                }
            }
        }, 300);
    }
}
