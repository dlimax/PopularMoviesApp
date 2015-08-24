package com.nobledata.nanodegree.popularmoviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
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
public class FetchMoviesFragment extends Fragment {

    private static final String LOG_TAG = "FetchMoviesFragment";
    private GridMoviesAdapter itemsAdapter;
    private View rootView;

    public FetchMoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_fetchmovies, container, false);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchMoviesTask task = new FetchMoviesTask();
        task.execute();
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


    public class FetchMoviesTask extends AsyncTask<String, Void, List<Map<String, String>>> {
        @Override
        protected List<Map<String, String>> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            List<Map<String, String>> to_return = null;

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

        @Override
        protected void onPostExecute(List<Map<String, String>> list) {
            super.onPostExecute(list);
            itemsAdapter = new GridMoviesAdapter(getActivity(), list);
            GridView gview = (GridView) rootView.findViewById(R.id.gridview_movies);
            gview.setAdapter(itemsAdapter);
            gview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GridMoviesAdapter adapter = (GridMoviesAdapter) parent.getAdapter();
                    HashMap<String, String> hmap = (HashMap<String, String>)adapter.getItem(position);
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra("title", hmap.get("title"));
                    intent.putExtra("vote_average", hmap.get("vote_average"));
                    intent.putExtra("overview", hmap.get("overview"));
                    intent.putExtra("poster_path", hmap.get("poster_path"));
                    intent.putExtra("release_date", hmap.get("release_date"));
                    startActivity(intent);
                }
            });
        }

        /**
         * Take the String representing the list of movies from TMDB in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         */
        private List<Map<String, String>> getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            List<Map<String, String>> hmaps = new ArrayList<Map<String, String>>();

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
                HashMap<String, String> hmap = new HashMap<String, String>();
                hmap.put(TMDB_ID, movieJSONObject.getString(TMDB_ID));
                hmap.put(TMDB_OVERVIEW, movieJSONObject.getString(TMDB_OVERVIEW));
                hmap.put(TMDB_POSTER_PATH, movieJSONObject.getString(TMDB_POSTER_PATH));
                hmap.put(TMDB_TITLE, movieJSONObject.getString(TMDB_TITLE));
                hmap.put(TMDB_POPULARITY, movieJSONObject.getString(TMDB_POPULARITY));
                hmap.put(TMDB_VOTE_AVERAGE, movieJSONObject.getString(TMDB_VOTE_AVERAGE));
                hmap.put(TMDB_RELEASE_DATE, movieJSONObject.getString(TMDB_RELEASE_DATE));
                hmaps.add(hmap);
            }

            /* Display hashmap content using Iterator*/
            for (Map hmap : hmaps) {
                Set set = hmap.entrySet();
                Iterator iterator = set.iterator();
                Log.v(LOG_TAG, "*Movie entry* ");
                while (iterator.hasNext()) {
                    Map.Entry mentry = (Map.Entry) iterator.next();
                    Log.v(LOG_TAG, "Key: " + mentry.getKey() + " - Value: " + mentry.getValue());
                }
            }
            return hmaps;
        }
    }
}
