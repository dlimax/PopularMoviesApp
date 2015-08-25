package com.nobledata.nanodegree.popularmoviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class FetchMoviesFragment extends android.app.Fragment {

    private static final String LOG_TAG = "FetchMoviesFragment";
    private GridMoviesAdapter itemsAdapter;
    public ArrayList<ParcelableMovie> mData;
    private View rootView;
    private Bundle savedState = null;
    private boolean createdStateInDestroyView;
    private static final String SAVED_BUNDLE_TAG = "key";


    public FetchMoviesFragment() {
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null){
            mData = savedInstanceState.getParcelableArrayList(SAVED_BUNDLE_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_fetchmovies, container, false);
        ((MainActivity)getActivity()).mContent = (android.app.Fragment)this;
        setHasOptionsMenu(true);
        setRetainInstance(true);
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        if(mData==null) {
            FetchMoviesTask task = new FetchMoviesTask();
            task.execute(savedState);
        }
        else{
            updateGridMovies();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVED_BUNDLE_TAG, mData);
    }
    


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateGridMovies(){
        itemsAdapter = new GridMoviesAdapter(getActivity(), mData);
        GridView gview = (GridView) rootView.findViewById(R.id.gridview_movies);
        gview.setAdapter(itemsAdapter);
        gview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridMoviesAdapter adapter = (GridMoviesAdapter) parent.getAdapter();
                ParcelableMovie hmap = (ParcelableMovie)adapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("title", hmap.getTitle());
                intent.putExtra("vote_average", hmap.getVoteAverage());
                intent.putExtra("overview", hmap.getOverview());
                intent.putExtra("poster_path", hmap.getPosterPath());
                intent.putExtra("release_date", hmap.getReleaseDate());
                startActivity(intent);
            }
        });
    }


    public ArrayList<ParcelableMovie> getMovies(){
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;
        ArrayList<ParcelableMovie> to_return = null;

        try {
            String api_key = getString(R.string.tmdb_api_key);
            // Construct the URL for the TMDB query
            // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=###
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("api_key", api_key);
            SharedPreferences settings;
            settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String order = settings.getString("pref_order_criteria", "1");
            if(order.equals("1"))
                builder.appendQueryParameter("sort_by", "popularity.desc");
            if(order.equals("2"))
                builder.appendQueryParameter("sort_by", "vote_average.desc");
            String myUrl = builder.build().toString();
            URL url = new URL(myUrl);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                moviesJsonStr = null;
            }
            moviesJsonStr = buffer.toString();
            to_return = getMoviesDataFromJson(moviesJsonStr);

        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            moviesJsonStr = null;
        }
        catch (JSONException e){
            Log.e("PlaceholderFragment", "Error ", e);
        }
        finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return to_return;
    }

    /**
     * Take the String representing the list of movies from TMDB in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     */
    private ArrayList<ParcelableMovie> getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {

        ArrayList<ParcelableMovie> movies = new ArrayList<ParcelableMovie>();

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results";
        final String TMDB_ID = "id";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_TITLE = "title";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_RELEASE_DATE = "release_date";


        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

        for (int i = 0; i < moviesArray.length(); i++) {
            // Get the JSON object representing a movie
            JSONObject movieJSONObject = moviesArray.getJSONObject(i);

            // get movie data
            ParcelableMovie movie = new ParcelableMovie();
            movie.setOverview(movieJSONObject.getString(TMDB_OVERVIEW));
            movie.setPosterPath(movieJSONObject.getString(TMDB_POSTER_PATH));
            movie.setTitle(movieJSONObject.getString(TMDB_TITLE));
            movie.setVoteAverage(movieJSONObject.getString(TMDB_VOTE_AVERAGE));
            movie.setReleaseDate(movieJSONObject.getString(TMDB_RELEASE_DATE));
            movies.add(movie);
        }
        return movies;
    }


        public class FetchMoviesTask extends AsyncTask<Bundle, Void, ArrayList<ParcelableMovie>> {
        @Override
        protected ArrayList<ParcelableMovie> doInBackground(Bundle... params) {
            Log.v(LOG_TAG, "Asynctasking");
            ArrayList<ParcelableMovie> aMovies = getMovies();
            if(params[0]!=null)
                params[0].putParcelableArrayList("key", aMovies);
            return aMovies;
        }

        @Override
        protected void onPostExecute(ArrayList<ParcelableMovie> list) {
            super.onPostExecute(list);
            mData =  list;
            updateGridMovies();
        }
    }
}
