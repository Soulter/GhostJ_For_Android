//package com.soulter.goastjforandroid;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//public class ScreenShotsListAdapter extends RecyclerView.Adapter<ScreenShotsListAdapter.ViewHolder>  {
//    private List<ScreenshotsField> mScrList;
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView scrImage;
//        TextView scrName;
//
//        public ViewHolder(View view) {
//            super(view);
//            scrImage = (ImageView) view.findViewById(R.id.screenshot_image_view);
//            scrName = (TextView) view.findViewById(R.id.screenshot_name);
//        }
//    }
//
//    public ScreenShotsListAdapter(List<ScreenshotsField> screenshotsFieldList) {
//        mScrList = screenshotsFieldList;
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.screen_show_item, parent, false);
//        ViewHolder holder = new ViewHolder(view);
//        return holder;
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        ScreenshotsField screenshotsField = mScrList.get(position);
//        holder.scrImage.setImageResource();
//        holder.scrName.setText(fruit.getName());
//    }
//
//    @Override
//    public int getItemCount() {
//        return mScrList.size();
//    }
//}
//}
