package com.nobledata.nanodegree.popularmoviesapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        ((TextView)rootView.findViewById(R.id.detail_title)).setText(intent.getStringExtra("title"));
        ((TextView)rootView.findViewById(R.id.detail_overview)).setText(intent.getStringExtra("overview"));
        ((TextView)rootView.findViewById(R.id.detail_release_date)).setText(intent.getStringExtra("release_date"));
        ((TextView)rootView.findViewById(R.id.detail_vote_average)).setText(intent.getStringExtra("vote_average"));
        ImageView imageView = ((ImageView) rootView.findViewById(R.id.detail_poster));
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath("w185")
                .appendPath(intent.getStringExtra("poster_path").replace("/", ""));
        String imgUrl = builder.build().toString();
        Picasso.with(getActivity()).load(imgUrl).into(imageView);
        return rootView;
    }
}
