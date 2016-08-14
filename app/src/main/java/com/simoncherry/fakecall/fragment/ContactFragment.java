package com.simoncherry.fakecall.fragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.promeg.pinyinhelper.Pinyin;
import com.simoncherry.fakecall.R;
import com.simoncherry.fakecall.activity.RobotCallActivity;
import com.simoncherry.fakecall.adapter.ContactAdapter;
import com.simoncherry.fakecall.bean.ContactBean;
import com.simoncherry.fakecall.custom.QuickAlphabeticBar;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

    private String[] ContactNameEntries;
    private String[] ContactNumberEntries;
    private String[] ContactVoicerValues;

    private ListView personList;
    private List<ContactBean> list;
    private ContactAdapter adapter;
    private QuickAlphabeticBar alpha;

    private Activity mContext;


    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        personList = (ListView) view.findViewById(R.id.acbuwa_list);
        alpha = (QuickAlphabeticBar) view.findViewById(R.id.fast_scroller);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //mContext = getActivity();
        //init();
    }

    @Override
    public void onViewCreated(View view, Bundle saved) {
        super.onViewCreated(view, saved);
        mContext = getActivity();
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    observer.removeOnGlobalLayoutListener(this);
                } else {
                    observer.removeGlobalOnLayoutListener(this);
                }
                // get width and height of the view
                init();
            }
        });
    }

    private void init() {
        ContactNameEntries = getResources().getStringArray(R.array.contact_name_entries);
        ContactNumberEntries = getResources().getStringArray(R.array.contact_number_entries);
        ContactVoicerValues = getResources().getStringArray(R.array.voicer_cloud_values);

        list = new ArrayList<>();
        loadContact();
        setAdapter(list);
        adapter.notifyDataSetChanged();
    }

    private String[] sortContact(String[] list) {
        Comparator comparator = Collator.getInstance(java.util.Locale.CHINA);
        String[] temp = new String[list.length];
        System.arraycopy(list, 0, temp, 0, list.length);
        Arrays.sort(temp, comparator);
        return temp;
    }

    private void loadContact() {
//        for(int i=0; i<ContactNameEntries.length; i++) {
//            ContactBean contactBean = new ContactBean();
//            contactBean.setDisplayName(ContactNameEntries[i]);
//            contactBean.setPhoneNum(ContactNumberEntries[i]);
//            contactBean.setSortKey(Pinyin.toPinyin(ContactNameEntries[i].charAt(0)));
//            list.add(contactBean);
//        }
        String[] contact = sortContact(ContactNameEntries);

        for(int i=0; i<contact.length; i++) {
            for(int j=0; j<ContactNameEntries.length; j++) {
                if (ContactNameEntries[j].equals(contact[i])) {
                    //Log.e("index: ", String.valueOf(j));
                    //Log.e("name: ", ContactNameEntries[j]);
                    //Log.e("number: ", ContactNumberEntries[j]);
                    //Log.e("sortKey: ", Pinyin.toPinyin(ContactNameEntries[j].charAt(0)));
                    ContactBean contactBean = new ContactBean();
                    contactBean.setDisplayName(ContactNameEntries[j]);
                    contactBean.setPhoneNum(ContactNumberEntries[j]);
                    contactBean.setSortKey(Pinyin.toPinyin(ContactNameEntries[j].charAt(0)));
                    contactBean.setPinyin(ContactVoicerValues[j]);
                    list.add(contactBean);
                    break;
                }
            }
        }
    }

    private void setAdapter(List<ContactBean> list) {
        adapter = new ContactAdapter(mContext, list, alpha);
        personList.setAdapter(adapter);
        alpha.init(mContext);
        alpha.setListView(personList);
        //Log.e("setHeight", String.valueOf(alpha.getHeight())); // TODO onActivityCreated 无法获取控件高度，导致无法使用快速索引条
        alpha.setHight(alpha.getHeight());
        alpha.setVisibility(View.VISIBLE);
        personList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactBean contactBean = (ContactBean) adapter.getItem(position);
                showContactDialog(list_menu, contactBean, position);
                //Toast.makeText(mContext, contactBean.getPinyin(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String[] list_menu = new String[] { "拨打电话", "发送短信", "删除" };

    //群组联系人弹出页
    private void showContactDialog(final String[] arg ,final ContactBean cb, final int position){
        new AlertDialog.Builder(mContext).setTitle(cb.getDisplayName()).setItems(arg,
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        switch(which){
                            case 0://打电话
                                Intent call = new Intent(mContext, RobotCallActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("voicer", cb.getPinyin());
                                bundle.putBoolean("isDial", true);
                                call.putExtras(bundle);
                                startActivity(call);
                                break;
                            case 1://发短息
                                break;
                            case 2:// 删除
                                showDelete(cb.getContactId(), position);
                                break;
                        }
                    }
                }).show();
    }

    // 删除联系人方法
    private void showDelete(final int contactsID, final int position) {
        new AlertDialog.Builder(mContext).setIcon(R.drawable.ic_launcher).setTitle("是否删除此联系人")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        adapter.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(mContext, "该联系人已经被删除.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
    }

}
