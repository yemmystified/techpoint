package com.formatrix.techpoint.adapters;

import android.animation.Animator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.formatrix.techpoint.R;
import com.formatrix.techpoint.libs.MySingleton;
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
public class AdapterArticles extends UltimateViewAdapter<RecyclerView.ViewHolder> {

    // Create arraylist variable to store data
    private final ArrayList<HashMap<String,String>> DATA;
    // Create ImageLoader object
    private final ImageLoader IMAGE_LOADER;
    // Create variable to store category id
    private String mCategoryId, mKeyword;
    // Create context object
    private Context mContext;

    // Create Interpolator object for item animation
    private Interpolator mInterpolator = new LinearInterpolator();

    // Set last position
    private int mLastPosition = 5;
    // Set default animation duration
    private final int ANIMATION_DURATION = 300;

    public AdapterArticles(String categoryId, String keyword, Context context, ArrayList<HashMap<String, String>> list){
        IMAGE_LOADER = MySingleton.getInstance(context).getImageLoader();
        DATA = list;
        mCategoryId = categoryId;
        mKeyword = keyword;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        // Connect with adapter layout
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_articles, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (position < getItemCount() && (customHeaderView != null ?
                position <= DATA.size() : position < DATA.size()) &&
                (customHeaderView == null || position > 0)) {
            HashMap<String, String> item;
            item = DATA.get(customHeaderView != null ? position - 1 : position);
            // Format string first
            String authorAndDate = mContext.getString(R.string.by)+" "+ item.get(Utils.KEY_AUTHOR) +
                    " " + mContext.getString(R.string.published_at)+
                    "  " + item.get(Utils.KEY_DATE);
            String categoryName = item.get(Utils.KEY_CATEGORY).toUpperCase();

            // Set data to the view
            ((ViewHolder) holder).txtTitle.setText(item.get(Utils.KEY_TITLE));
            // Check sCategoryId value
            if((mCategoryId != null) && (mKeyword == null)) {
                // If sCategoryId is not empty then hide category view as we do not need it
                ((ViewHolder) holder).txtCategoryName.setVisibility(View.GONE);
            }else{
                // If sCategoryId is empty then show category view
                ((ViewHolder) holder).txtCategoryName.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).txtCategoryName.setText(categoryName);
            }
            try {
                // check whether image is default value
                if (!item.get(Utils.KEY_IMAGE_THUMBNAIL_URL).equals(Utils.VALUE_IMAGE_DEFAULT)) {
                    // If image is not default value display image
                    // with ImageLoader object
                    IMAGE_LOADER.get(item.get(Utils.KEY_IMAGE_THUMBNAIL_URL),
                            ImageLoader.getImageListener(((ViewHolder) holder).imgThumbnail,
            //                                R.mipmap.empty_photo, R.mipmap.empty_photo));
                                    R.mipmap.empty, R.mipmap.empty));
                } else {
                    // If image is default value set image
                    // background color with empty_photo.png
                    ((ViewHolder) holder).imgThumbnail.setImageResource(R.mipmap.empty);
                }
            }catch (Exception e){


            }
        }

        boolean isFirstOnly = true;
        if (!isFirstOnly || position > mLastPosition) {
            // Add animation to the item
            for (Animator anim : getAdapterAnimations(holder.itemView,
                    AdapterAnimationType.ScaleIn)) {
                anim.setDuration(ANIMATION_DURATION).start();
                anim.setInterpolator(mInterpolator);
            }
            mLastPosition = position;
        } else {
            ViewHelper.clear(holder.itemView);
        }
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
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {   }

    public static class ViewHolder extends UltimateRecyclerviewViewHolder {
        // Create view objects
        final TextView txtTitle, txtCategoryName, txtAuthorAndDate;
        final ImageView imgThumbnail;

        public ViewHolder(View v) {
            super(v);
            // Connect view objects with view ids in xml
            txtTitle        = (TextView) v.findViewById(R.id.txtTitle);
            txtAuthorAndDate   = (TextView) v.findViewById(R.id.txtAuthorAndDate);
            txtCategoryName = (TextView) v.findViewById(R.id.txtCategoryName);
            imgThumbnail    = (ImageView) v.findViewById(R.id.imgThumbnail);
        }
    }

}
