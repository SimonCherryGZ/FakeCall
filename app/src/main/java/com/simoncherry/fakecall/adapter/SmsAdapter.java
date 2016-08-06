package com.simoncherry.fakecall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.simoncherry.fakecall.R;
import com.simoncherry.fakecall.bean.SmsBean;

import java.util.List;

/**
 * Created by Simon on 2016/8/4.
 */
public class SmsAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<SmsBean> list;
    private Context ctx;

    public SmsAdapter(Context context, List<SmsBean> list) {
        this.ctx = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_sms, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_text = (TextView) convertView.findViewById(R.id.tv_text);
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SmsBean smsBean = list.get(position);
        holder.tv_name.setText(smsBean.getName());
        holder.tv_text.setText(smsBean.getText());
        holder.tv_time.setText(smsBean.getTime());

        return convertView;
    }

    private static class ViewHolder {
        TextView tv_name;
        TextView tv_text;
        TextView tv_time;
    }
}
