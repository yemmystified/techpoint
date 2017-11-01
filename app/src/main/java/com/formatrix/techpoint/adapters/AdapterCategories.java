package com.formatrix.techpoint.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.formatrix.techpoint.R;
import com.formatrix.techpoint.utils.Utils;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Design and developed by formatrix.com
 *
 * AdapterCategories is created to category item.
 * Created using UltimateViewAdapter.
 */
public class AdapterCategories extends UltimateViewAdapter<RecyclerView.ViewHolder> {

    // Create arraylist variable to store data
    private final ArrayList<HashMap<String,String>> DATA;

    public AdapterCategories(ArrayList<HashMap<String, String>> list){
        DATA = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_categories, parent, false);


        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        HashMap<String, String> item;
        item = DATA.get(customHeaderView != null ? position - 1 : position);
        // Set category name to textview
        ((ViewHolder) holder).txtCategoryName.setText(item.get(Utils.KEY_TITLE));

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
        // Create view object
        final TextView txtCategoryName;

        public ViewHolder(View v) {
            super(v);
            // Connect view object with view id in xml
            txtCategoryName = (TextView) v.findViewById(R.id.txtCategoryName);
        }
    }
}
