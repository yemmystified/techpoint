package com.formatrix.techpoint.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.formatrix.techpoint.R;
import com.formatrix.techpoint.utils.Utils;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.marshalchen.ultimaterecyclerview.animators.internal.ViewHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Design and developed by formatrix.com
 *
 * AdapterArticles is created to article list item.
 * Created using UltimateViewAdapter.
 */
public class AdapterComments extends UltimateViewAdapter<RecyclerView.ViewHolder> {

    // Create arraylist variable to store data
    private final ArrayList<HashMap<String,String>> DATA;
    private Context mContext;

    public AdapterComments(Context context, ArrayList<HashMap<String, String>> list){
        DATA = list;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        // Connect with adapter layout
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_comments, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (position < getItemCount() && (customHeaderView != null ?
                position <= DATA.size() : position < DATA.size()) &&
                (customHeaderView == null || position > 0)) {
            HashMap<String, String> item;
            item = DATA.get(customHeaderView != null ? position - 1 : position);
            String date = mContext.getString(R.string.published_at) + " " + item.get(Utils.KEY_DATE);
            // Set data to the view
            ((ViewHolder) holder).txtTitle.setText(item.get(Utils.KEY_NAME));
            ((ViewHolder) holder).txtContent.setText(Html.fromHtml(item.get(Utils.KEY_CONTENT)));


        }

        ViewHelper.clear(holder.itemView);
    }

    @Override
    public int getAdapterItemCount() {
        return DATA.size();
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return new UltimateRecyclerviewViewHolder(view);
    }

    @Override
    public long generateHeaderId(int i) {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

    }

    public static class ViewHolder extends UltimateRecyclerviewViewHolder {
        // Create view objects
        final TextView txtTitle, txtContent, txtDate;

        public ViewHolder(View v) {
            super(v);
            // Connect view objects with view ids in xml
            txtTitle        = (TextView) v.findViewById(R.id.txtTitle);
            txtContent      = (TextView) v.findViewById(R.id.txtContent);
            txtDate         = (TextView) v.findViewById(R.id.txtDate);
        }
    }

}
