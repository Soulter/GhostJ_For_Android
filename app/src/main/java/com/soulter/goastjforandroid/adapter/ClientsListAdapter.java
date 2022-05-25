package com.soulter.goastjforandroid.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.soulter.goastjforandroid.pojo.ClientsField;
import com.soulter.goastjforandroid.R;

import java.util.List;

public class ClientsListAdapter extends ArrayAdapter<ClientsField> {
    private int resourceId;

    public ClientsListAdapter(Context context, int itemResId, List<ClientsField> list){
        super(context, itemResId, list);
        resourceId = itemResId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ClientsField clientsField = getItem(position);

        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        View view;
        ViewHolder viewHolder;
        if (convertView==null){

            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder=new ViewHolder();
            viewHolder.clientName=view.findViewById(R.id.tv_client_name);
            viewHolder.clientName.setTypeface(viewHolder.clientName.getTypeface(), Typeface.BOLD);
            viewHolder.clientNum=view.findViewById(R.id.tv_client_num);
            viewHolder.clientNum.setTypeface(viewHolder.clientNum.getTypeface(), Typeface.BOLD);

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.setTag(viewHolder);
        } else{
            view=convertView;
            viewHolder=(ViewHolder) view.getTag();
        }

        // 获取控件实例，并调用set...方法使其显示出来
        viewHolder.clientName.setText(clientsField.getClientName());
        viewHolder.clientNum.setText(clientsField.getClientNum());
        return view;

    }

    // 定义一个内部类，用于对控件的实例进行缓存
    class ViewHolder{
        TextView clientName;
        TextView clientNum;
    }
}
