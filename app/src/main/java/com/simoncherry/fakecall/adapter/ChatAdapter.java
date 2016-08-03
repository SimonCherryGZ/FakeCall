package com.simoncherry.fakecall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.simoncherry.fakecall.R;
import com.simoncherry.fakecall.bean.ChatBean;
import com.simoncherry.fakecall.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Simon on 2016/8/2.
 */
public class ChatAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<ChatBean> list;
    private Context ctx;
    private SimpleDateFormat sdf;

    public ChatAdapter(Context context, List<ChatBean> list) {
        this.ctx = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
            convertView = inflater.inflate(R.layout.item_chat, null);
            holder = new ViewHolder();
            holder.layout_other = (RelativeLayout) convertView.findViewById(R.id.layout_other_msg);
            holder.layout_mine = (RelativeLayout) convertView.findViewById(R.id.layout_mine_msg);
            holder.text_other_msg = (TextView) convertView.findViewById(R.id.tv_other_msg);
            holder.text_mine_msg = (TextView) convertView.findViewById(R.id.tv_mine_msg);
            holder.text_other_time = (TextView) convertView.findViewById(R.id.tv_other_time);
            holder.text_mine_time = (TextView) convertView.findViewById(R.id.tv_mine_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.layout_other.setVisibility(View.GONE);
        holder.layout_mine.setVisibility(View.GONE);

        ChatBean chatBean = list.get(position);
        String text = chatBean.getText();
        boolean isMine = chatBean.isMine();
        if (isMine) {
            //holder.layout_other.setVisibility(View.GONE);
            holder.layout_mine.setVisibility(View.VISIBLE);
            holder.text_mine_msg.setText(text);
            holder.text_mine_time.setText(DateUtil.getTimeInterval(sdf.format(System.currentTimeMillis())));
        } else {
            //holder.text_mine_msg.setVisibility(View.GONE);
            holder.layout_other.setVisibility(View.VISIBLE);
            holder.text_other_msg.setText(text);
            holder.text_other_time.setText(DateUtil.getTimeInterval(sdf.format(System.currentTimeMillis())));
        }

        return convertView;
    }

    private static class ViewHolder {
        RelativeLayout layout_other;
        RelativeLayout layout_mine;
        TextView text_other_msg;
        TextView text_mine_msg;
        TextView text_other_time;
        TextView text_mine_time;
    }
}
