package com.simoncherry.fakecall.fragment;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.github.promeg.pinyinhelper.Pinyin;
import com.simoncherry.fakecall.R;
import com.simoncherry.fakecall.adapter.CallLogAdapter;
import com.simoncherry.fakecall.adapter.T9Adapter;
import com.simoncherry.fakecall.application.SpeechApp;
import com.simoncherry.fakecall.bean.CallLogBean;
import com.simoncherry.fakecall.bean.ContactBean;
import com.simoncherry.fakecall.util.ToPinYin;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class CallFragment extends Fragment implements View.OnClickListener {

    private String[] ContactNameEntries;
    private String[] ContactNumberEntries;
    private String[] ContactVoicerValues;

    private CallLogAdapter adapter;
    private ListView callLogList;
    private List<CallLogBean> list;

    private LinearLayout bohaopan;
    private LinearLayout keyboard_show_ll;
    private Button keyboard_show;

    private Button phone_view;
    private Button delete;
    private Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    private SoundPool spool;
    private AudioManager am = null;

    private SpeechApp application;
    private ListView listView;
    private T9Adapter t9Adapter;
    private List<ContactBean> contact;

    private Context mContext;

    public CallFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);
        listView = (ListView) view.findViewById(R.id.contact_list);
        bohaopan = (LinearLayout) view.findViewById(R.id.bohaopan);
        keyboard_show_ll = (LinearLayout) view.findViewById(R.id.keyboard_show_ll);
        keyboard_show = (Button) view.findViewById(R.id.keyboard_show);
        callLogList = (ListView) view.findViewById(R.id.call_log_list);
        phone_view = (Button) view.findViewById(R.id.phone_view);
        delete = (Button) view.findViewById(R.id.delete);

        for (int i = 0; i < 12; i++) {
            View v = view.findViewById(R.id.dialNum1 + i);
            v.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        init();
    }

    private void init() {
        application = (SpeechApp) getActivity().getApplication();
        initContact();

        keyboard_show.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialPadShow();
            }
        });

        am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        spool = new SoundPool(11, AudioManager.STREAM_SYSTEM, 5);
        map.put(0, spool.load(mContext, R.raw.dtmf0, 0));
        map.put(1, spool.load(mContext, R.raw.dtmf1, 0));
        map.put(2, spool.load(mContext, R.raw.dtmf2, 0));
        map.put(3, spool.load(mContext, R.raw.dtmf3, 0));
        map.put(4, spool.load(mContext, R.raw.dtmf4, 0));
        map.put(5, spool.load(mContext, R.raw.dtmf5, 0));
        map.put(6, spool.load(mContext, R.raw.dtmf6, 0));
        map.put(7, spool.load(mContext, R.raw.dtmf7, 0));
        map.put(8, spool.load(mContext, R.raw.dtmf8, 0));
        map.put(9, spool.load(mContext, R.raw.dtmf9, 0));
        map.put(11, spool.load(mContext, R.raw.dtmf11, 0));
        map.put(12, spool.load(mContext, R.raw.dtmf12, 0));

        phone_view.setOnClickListener(this);
        phone_view.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //if(null == application.getContactBeanList() || application.getContactBeanList().size()<1 || "".equals(s.toString())){
                if(null == contact || contact.size()<1 || "".equals(s.toString())){
                    listView.setVisibility(View.INVISIBLE);
                    callLogList.setVisibility(View.VISIBLE);
                }else{
                    if(null == t9Adapter){
                        t9Adapter = new T9Adapter(mContext);
                        //t9Adapter.assignment(application.getContactBeanList()); // TODO
                        t9Adapter.assignment(contact);

//						TextView tv = new TextView(HomeDialActivity.this);
//						tv.setBackgroundResource(R.drawable.dial_input_bg2);
//						listView.addFooterView(tv);
                        listView.setAdapter(t9Adapter);
                        listView.setTextFilterEnabled(true);
                        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                            public void onScrollStateChanged(AbsListView view, int scrollState) {
                                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                                    if(bohaopan.getVisibility() == View.VISIBLE){
                                        bohaopan.setVisibility(View.GONE);
                                        keyboard_show_ll.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                            public void onScroll(AbsListView view, int firstVisibleItem,
                                                 int visibleItemCount, int totalItemCount) {
                            }
                        });
                    }else{
                        callLogList.setVisibility(View.INVISIBLE);
                        listView.setVisibility(View.VISIBLE);
                        t9Adapter.getFilter().filter(s);
                    }
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void afterTextChanged(Editable s) {
            }
        });

        delete.setOnClickListener(this);
        delete.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                phone_view.setText("");
                return false;
            }
        });

        SimpleDateFormat sfd = new SimpleDateFormat("MM-dd hh:mm");
        list = new ArrayList<>();
        for(int i=0; i<ContactNameEntries.length; i++) {
            CallLogBean callLogBean = new CallLogBean();
            callLogBean.setId(i);
            callLogBean.setName(ContactNameEntries[i]);
            callLogBean.setNumber(ContactNumberEntries[i]);
            callLogBean.setDate(sfd.format(System.currentTimeMillis()));
            callLogBean.setType(new Random().nextInt(3)+1);
            list.add(callLogBean);
        }
        setAdapter(list);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialNum0:
                if (phone_view.getText().length() < 12) {
                    play(1);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum1:
                if (phone_view.getText().length() < 12) {
                    play(1);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum2:
                if (phone_view.getText().length() < 12) {
                    play(2);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum3:
                if (phone_view.getText().length() < 12) {
                    play(3);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum4:
                if (phone_view.getText().length() < 12) {
                    play(4);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum5:
                if (phone_view.getText().length() < 12) {
                    play(5);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum6:
                if (phone_view.getText().length() < 12) {
                    play(6);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum7:
                if (phone_view.getText().length() < 12) {
                    play(7);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum8:
                if (phone_view.getText().length() < 12) {
                    play(8);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum9:
                if (phone_view.getText().length() < 12) {
                    play(9);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialx:
                if (phone_view.getText().length() < 12) {
                    play(11);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialj:
                if (phone_view.getText().length() < 12) {
                    play(12);
                    input(v.getTag().toString());
                }
                break;
            case R.id.delete:
                delete();
                break;
            case R.id.phone_view:
                if (phone_view.getText().toString().length() >= 4) {
                    call(phone_view.getText().toString());
                }
                break;
            default:
                break;
        }
    }

    private void initContact() {
        ContactNameEntries = getResources().getStringArray(R.array.contact_name_entries);
        ContactNumberEntries = getResources().getStringArray(R.array.contact_number_entries);
        ContactVoicerValues = getResources().getStringArray(R.array.voicer_cloud_values);

        //List<ContactBean> contact = new ArrayList<>();
        contact = new ArrayList<>();
        for(int i=0; i<ContactNameEntries.length; i++) {
            ContactBean contactBean = new ContactBean();
            contactBean.setContactId(i);
            contactBean.setDisplayName(ContactNameEntries[i]);
            contactBean.setPhoneNum(ContactNumberEntries[i]);
            contactBean.setSortKey(Pinyin.toPinyin(ContactNameEntries[i].charAt(0)));
            contactBean.setFormattedNumber(getNameNum(ContactNameEntries[i] + ""));
            try {
                contactBean.setPinyin(ToPinYin.getPinYin(ContactNameEntries[i] + ""));
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
            }
            contact.add(contactBean);
        }
        application.setContactBeanList(contact);
    }

    private String getNameNum(String name) {
        try {
            if (name != null && name.length() != 0) {
                int len = name.length();
                char[] nums = new char[len];
                for (int i = 0; i < len; i++) {
                    String tmp = name.substring(i);
                    nums[i] = getOneNumFromAlpha(ToPinYin.getPinYin(tmp).toLowerCase().charAt(0));
                }
                return new String(nums);
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return null;
    }

    private char getOneNumFromAlpha(char firstAlpha) {
        switch (firstAlpha) {
            case 'a':
            case 'b':
            case 'c':
                return '2';
            case 'd':
            case 'e':
            case 'f':
                return '3';
            case 'g':
            case 'h':
            case 'i':
                return '4';
            case 'j':
            case 'k':
            case 'l':
                return '5';
            case 'm':
            case 'n':
            case 'o':
                return '6';
            case 'p':
            case 'q':
            case 'r':
            case 's':
                return '7';
            case 't':
            case 'u':
            case 'v':
                return '8';
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                return '9';
            default:
                return '0';
        }
    }

    private void setAdapter(List<CallLogBean> list) {
        adapter = new CallLogAdapter(mContext, list);
//		TextView tv = new TextView(this);
//		tv.setBackgroundResource(R.drawable.dial_input_bg2);
//		callLogList.addFooterView(tv);
        callLogList.setAdapter(adapter);
        callLogList.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    if(bohaopan.getVisibility() == View.VISIBLE){
                        bohaopan.setVisibility(View.GONE);
                        keyboard_show_ll.setVisibility(View.VISIBLE);
                    }
                }
            }
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });
        callLogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    public void dialPadShow(){
        if(bohaopan.getVisibility() == View.VISIBLE){
            bohaopan.setVisibility(View.GONE);
            keyboard_show_ll.setVisibility(View.VISIBLE);
        }else{
            bohaopan.setVisibility(View.VISIBLE);
            keyboard_show_ll.setVisibility(View.INVISIBLE);
        }
    }

    private void play(int id) {
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        float value = (float)0.7 / max * current;
        spool.setVolume(spool.play(id, value, value, 0, 0, 1f), value, value);
    }
    private void input(String str) {
        String p = phone_view.getText().toString();
        phone_view.setText(p + str);
    }
    private void delete() {
        String p = phone_view.getText().toString();
        if(p.length()>0){
            phone_view.setText(p.substring(0, p.length()-1));
        }
    }
    private void call(String phone) {
//        Uri uri = Uri.parse("tel:" + phone);
//        Intent it = new Intent(Intent.ACTION_CALL, uri);
//        startActivity(it);
    }
}
