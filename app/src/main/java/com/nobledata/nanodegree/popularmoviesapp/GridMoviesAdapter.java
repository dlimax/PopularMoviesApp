package com.nobledata.nanodegree.popularmoviesapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridMoviesAdapter extends BaseAdapter {
    private final List<Map<String, String>> mData;
    private Context mContext;


    public GridMoviesAdapter(Context context, List<Map<String, String>> list) {
        mContext = context;
        mData = list;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map<String, String> getItem(int position) {
        return  mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        } else {
            result = convertView;
        }

        HashMap<String, String> item = (HashMap<String, String>) getItem(position);
        ImageView imageView = ((ImageView) result.findViewById(R.id.movie_poster));
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath("w185")
                .appendPath(item.get("poster_path").replace("/", ""));
        String imgUrl = builder.build().toString();
        Picasso.with(mContext).load(imgUrl).into(imageView);
        return result;
    }
}