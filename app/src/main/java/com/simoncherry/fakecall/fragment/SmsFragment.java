package com.simoncherry.fakecall.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.simoncherry.fakecall.R;
import com.simoncherry.fakecall.activity.MainActivity;
import com.simoncherry.fakecall.adapter.SmsAdapter;
import com.simoncherry.fakecall.bean.SmsBean;
import com.simoncherry.fakecall.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SmsFragment extends Fragment {

    private String[] ContactNameEntries;
    private String[] ContactNumberEntries;
    private String[] SmsTextEntries;

    ListView SmsList;
    List<SmsBean> list;
    SmsAdapter adapter;
    private SimpleDateFormat sdf;

    private Context mContext;


    public SmsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sms, container, false);
        SmsList = (ListView) view.findViewById(R.id.list_sms);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        init();
    }

    private void init() {
        ContactNameEntries = getResources().getStringArray(R.array.contact_name_entries);
        ContactNumberEntries = getResources().getStringArray(R.array.contact_number_entries);
        SmsTextEntries = getResources().getStringArray(R.array.sms_text_entries);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        list = new ArrayList<>();
        loadMockSms();
        setAdapter(list);
        adapter.notifyDataSetChanged();

        SmsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) getActivity()).showChatFragment();
            }
        });
    }

    private void setAdapter(List<SmsBean> list) {
        adapter = new SmsAdapter(mContext, list);
        SmsList.setAdapter(adapter);
    }

    private void loadMockSms() {
        for(int i=0; i<ContactNameEntries.length; i++) {
            SmsBean smsBean = new SmsBean();
            smsBean.setName(ContactNameEntries[i]);
            smsBean.setText(SmsTextEntries[i]);
            smsBean.setTime(DateUtil.getTimeInterval(sdf.format(System.currentTimeMillis())));
            list.add(smsBean);
        }
    }
}
