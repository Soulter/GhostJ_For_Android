package com.soulter.goastjforandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DialogItemAdapter extends BaseAdapter {
    //这里可以传递个对象，用来控制不同的item的效果
    //比如每个item的背景资源，选中样式等
    public List<String> list;
    LayoutInflater inflater;

    public DialogItemAdapter(Context context, List<String> list) {
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int i) {
        if (i == getCount() || list == null) {
            return null;
        }
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.dialog_item, null);
            holder.typeTextview = (TextView) convertView.findViewById(R.id.dialog_item_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.typeTextview.setText(getItem(position));
        return convertView;
    }

    public static class ViewHolder { public TextView typeTextview; }
}
