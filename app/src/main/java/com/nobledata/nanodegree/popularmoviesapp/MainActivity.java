package com.nobledata.nanodegree.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public FetchMoviesFragment mContent;
    public ArrayList<ParcelableMovie> mData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mContent =  (FetchMoviesFragment)getFragmentManager().getFragment(
                    savedInstanceState, "mContent");
            mContent.mData = savedInstanceState.getParcelableArrayList(FetchMoviesFragment.SAVED_BUNDLE_TAG);
        }
        setContentView(R.layout.activity_main);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Save the fragment's instance
        mContent = (FetchMoviesFragment)getFragmentManager().findFragmentById(R.id.fragment);
        mData = mContent.mData;
        outState.putParcelableArrayList(mContent.SAVED_BUNDLE_TAG, mData);
        super.onSaveInstanceState(outState);
    }
}
