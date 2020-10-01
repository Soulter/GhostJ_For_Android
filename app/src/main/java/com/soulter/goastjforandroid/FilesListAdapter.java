package com.soulter.goastjforandroid;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesListAdapter extends ArrayAdapter<FilesField> {
    private int resourceId;

    public FilesListAdapter(Context context, int itemResId, List<FilesField> list){
        super(context, itemResId, list);
        resourceId = itemResId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FilesField filesField = getItem(position);

        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        View view;
        FilesListAdapter.ViewHolder viewHolder;
        if (convertView==null){

            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder=new FilesListAdapter.ViewHolder();
            viewHolder.fileName=view.findViewById(R.id.item_file_name);
            viewHolder.fileName.setTypeface(viewHolder.fileName.getTypeface(), Typeface.BOLD);
            viewHolder.fileSize=view.findViewById(R.id.item_file_size);
            viewHolder.fileSize.setTypeface(viewHolder.fileSize.getTypeface(), Typeface.BOLD);
            viewHolder.dictTag=view.findViewById(R.id.file_is_dict);

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.setTag(viewHolder);
        } else{
            view=convertView;
            viewHolder=(FilesListAdapter.ViewHolder) view.getTag();
        }

        // 获取控件实例，并调用set...方法使其显示出来
        viewHolder.fileName.setText(filesField.getfileName());
        viewHolder.fileSize.setText(filesField.getfileSize());
        if (filesField.getIsDict() == 1){
            viewHolder.dictTag.setVisibility(View.VISIBLE);
        }else
            viewHolder.dictTag.setVisibility(View.INVISIBLE);
        return view;

    }

    // 定义一个内部类，用于对控件的实例进行缓存
    class ViewHolder{
        TextView fileName;
        TextView fileSize;
        TextView dictTag;
    }


}
