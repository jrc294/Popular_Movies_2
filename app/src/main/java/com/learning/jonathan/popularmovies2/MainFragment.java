package com.learning.jonathan.popularmovies2;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.learning.jonathan.popularmovies2.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;


public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private GridView m_gridView = null;

    private static final int MOVIE_LOADER = 0;
    // category = ? and is_favorite = ?
    private static final String sCategoryAndFavorite = MovieContract.MovieEntry.COLUMN_CATEGORY + " = ? and " + MovieContract.MovieEntry.COLUMN_IS_FAVORITE + " = ?";
    // category = ?
    private static final String sCategory = MovieContract.MovieEntry.COLUMN_CATEGORY + " = ?";

    public static final String API_KEY = "";
    private static final String SORT_ORDER_POPULAR = "popularity.desc";
    private static final String SORT_ORDER_RATED = "vote_average.desc";

    private static final String PREVIOUS_CATEGORY = "previous_category";
    private static final String MOVIESTOTE = "movieStore";

    // Movie array stores the details of all movies loaded from the json return value

    ArrayList<MovieData> m_movieStore;
    String m_previous_category;
    ProgressDialog progressDialog;

    public static final String LOG_TAG = MainFragment.class.getSimpleName();

    public static String[] MOVIE_COLUMNS = new String[] {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_POSTER_IMAGE,
            MovieContract.MovieEntry.COLUMN_CATEGORY,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_IS_FAVORITE,
            MovieContract.MovieEntry.COLUMN_DATE,
            MovieContract.MovieEntry.COLUMN_REVIEWS,
            MovieContract.MovieEntry.COLUMN_VIDEOS
    };

    private static String[] MOVIE_ID = new String[] {"_ID"};

    public MainFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_main, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Reload the gridview if the activity starts. This will occur on screen orientation changes as well
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (((Callback) getActivity()).hasTwoPanes()) {
                // Set the number of columns to three for tablet landscape
                m_gridView.setNumColumns(3);
            } else {
                // Set the number of columns to four for phone landscape
                m_gridView.setNumColumns(4);
            }
        } else {
            m_gridView.setNumColumns(2);
        }
        // Only refresh the grid if the activity was not started as a result of coming back from the detailed activity
        loadMovieCache();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Start the settings activity when the menu option is chosen
        if (id == R.id.settings_action) {
            Intent i = new Intent(getActivity(), SettingsActivity.class);
            startActivity(i);
        }

        if (id == R.id.refresh) {
            if (!getCurrentSortOrder().equals(MovieContract.Category.favorite)) {
                // To refresh the data for the current , remove all movies from the database except favorite movies and reload the movie cache which will repopulate
                getActivity().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, MovieContract.MovieEntry.COLUMN_IS_FAVORITE + " = ? AND " + MovieContract.MovieEntry.COLUMN_CATEGORY + " = ?", new String[]{"0", getCurrentSortOrder().toString()});
            }
            m_previous_category = "";
            loadMovieCache();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        m_gridView = (GridView) rootView.findViewById(R.id.grid_view_movies);

        //1. Retrieve the MovieStores from the cache
        if (savedInstanceState != null) {
            m_previous_category = savedInstanceState.getString(PREVIOUS_CATEGORY);
            m_movieStore = savedInstanceState.getParcelableArrayList(MOVIESTOTE);
        }

        // We'll call our MainActivity
        m_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                int id = m_movieStore.get(position).getID();
                // Build the uri for the movie
                Uri uri = MovieContract.MovieEntry.buildMovieById(id);
                ((Callback) getActivity()).onItemSelected(uri);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Store the movie stores
        outState.putString(PREVIOUS_CATEGORY, m_previous_category);
        outState.putParcelableArrayList(MOVIESTOTE, m_movieStore);
    }

    private void loadMovieCache() {
        // Load movie cache will load up the database, representing popular movies and highly rated movies.
        // If we cache these on create, then no further internet access will be required until the activity is refreshed.

        // 1. Retrieve the poster images for the category

        MovieContract.Category category = getCurrentSortOrder();

        if (!category.toString().equals(m_previous_category) && (((Callback) getActivity()).hasTwoPanes())) {
            // Load up the first movie into the detail pane
            Uri uri = MovieContract.MovieEntry.buildMovieById(0);
            ((Callback) getActivity()).onItemSelected(uri);
        }
        // Get the current count of movies in the current category minus the favorites
        Cursor c = getActivity().getContentResolver().query(MovieContract.MovieEntry.buildMoviesByCategory(category),
                MOVIE_ID, sCategoryAndFavorite, new String[] {category.toString(), "0"},null);
        int numberOfMoviesInCategory = c.getCount();
        c.close();

        // 1.1 If no movies exist in the database, load them up as long as we don't just want favorites
        if ((numberOfMoviesInCategory == 0) && (!category.equals(MovieContract.Category.favorite))) {

            Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            switch (display.getRotation()) {
                case Surface.ROTATION_0 :
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case Surface.ROTATION_90 :
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case Surface.ROTATION_180 :
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    break;
                case Surface.ROTATION_270 :
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    break;
                default:
                    break;
            }

            m_previous_category = category.toString();
            FetchMoviesTask moviesTask = new FetchMoviesTask();
            moviesTask.execute(category);
            progressDialog = ProgressDialog.show(getActivity(), "Loading", "Please wait...", true);
        } else {
            // 2. If the category has changed, refresh the adapter, or if we are showing only favories. In that case, we will also refresh just in case the user has moved some
            // movies out of favories
            if (!category.toString().equals(m_previous_category) || category.equals(MovieContract.Category.favorite)) {
                // Get all the movies for the category plus any favories for that category
                Cursor cursor = getActivity().getContentResolver().query(MovieContract.MovieEntry.buildMoviesByCategory(category),
                        MOVIE_COLUMNS, sCategory, new String[]{category.toString()},null);
                m_movieStore = decodeMovieCursor(category, cursor);
                m_gridView.setAdapter(new ImageAdapter(getActivity(), m_movieStore));
                m_previous_category = category.toString();
                cursor.close();
            }
        }
    }

    private ArrayList<MovieData> decodeMovieCursor(MovieContract.Category category, Cursor c) {

        ArrayList<MovieData> movies = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                MovieContract.Category movie_category = MovieContract.Category.popular;
                if (c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_CATEGORY)).equals(MovieContract.Category.highest_rated.toString())) {
                    movie_category =  MovieContract.Category.highest_rated;
                }
                if (category.equals(movie_category) || (category.equals(MovieContract.Category.favorite) && c.getInt(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_IS_FAVORITE)) == 1)) {
                    MovieData movie = new MovieData();
                    movie.setOverview(c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS)));
                    movie.setReleaseDate(c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_DATE)));
                    movie.setVoteAverage(c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING)));
                    movie.setTitle(c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
                    movie.setID(c.getInt(c.getColumnIndex(MovieContract.MovieEntry._ID)));
                    movie.setCategory(movie_category);
                    try {
                        byte[] bytes = c.getBlob(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE));
                        int length = bytes.length;
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, length);
                        movie.setPosterW185(bmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    movie.setReviews(c.getString(c.getColumnIndex((MovieContract.MovieEntry.COLUMN_REVIEWS))));
                    movie.setVideos(c.getString(c.getColumnIndex((MovieContract.MovieEntry.COLUMN_VIDEOS))));
                    movies.add(movie);
                }
            } while (c.moveToNext());
        }
        return movies;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Call the content resolver to get the images for all movies
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        return new CursorLoader(getActivity(),uri,MOVIE_COLUMNS,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MovieContract.Category category = getCurrentSortOrder();
        m_movieStore = decodeMovieCursor(category, data);

        if (m_movieStore.size() > 0) {
            m_gridView.setAdapter(new ImageAdapter(getActivity(), m_movieStore));
            if (progressDialog != null) {
                progressDialog.dismiss();
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        } else {
            m_gridView.setAdapter(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Do nothing
    }




    public class ImageAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<MovieData> mMovies;

        public ImageAdapter(Context c, ArrayList<MovieData> movies) {
            mContext = c;
            mMovies = movies;
        }

        @Override
        public int getCount() {
            return mMovies.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        // getView will create the imageViews for the gridWview
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }

            //imageView.setAdjustViewBounds(true);
            imageView.setImageBitmap(mMovies.get(position).getPosterW185());
            return imageView;

        }
    }

    public class FetchMoviesTask extends AsyncTask<MovieContract.Category, Void, Void> {

        // Async task to retrieve the movie details from the movie database and receive a json string which is parsed and loaded into an ArrayList of Movie classes

        @Override
        protected Void doInBackground(MovieContract.Category... params) {
            MovieContract.Category category = params[0];
            getMovieData(category);
            return null;
        }

        private void getMovieData(MovieContract.Category category) {

            String sort_by = SORT_ORDER_POPULAR;
            if (MovieContract.Category.highest_rated.equals(category)) {
                sort_by = SORT_ORDER_RATED;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStrArray;

            try {
                // Construct the URL for the themoviedb query
                URL url = new URL(new Uri.Builder().scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", sort_by)
                        .appendQueryParameter("api_key", API_KEY).build().toString());
                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
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
                    moviesJsonStrArray = null;
                }
                moviesJsonStrArray = buffer.toString();

                try {
                    getMovieDataFromJson(moviesJsonStrArray, category);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }


        private ArrayList<MovieData> getMovieDataFromJson(String moviesJsonStr, MovieContract.Category category) throws JSONException {

            // Parse thru the json string and load into a collection of Movie data

            final String MDB_POSTER_PATH = "poster_path";
            final String MDB_TITLE = "original_title";
            final String MDB_RESULTS = "results";
            final String MDB_RELEASE_DATE = "release_date";
            final String MDB_AVERAGE_RATING = "vote_average";
            final String MDB_OVERVIEW = "overview";
            final String MDB_MOVIE_ID = "id";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray(MDB_RESULTS);
            byte[] byteArray = null;
            Vector<ContentValues> cVVector = new Vector<ContentValues>(resultsArray.length());

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject movieObject = resultsArray.getJSONObject(i);

                Uri posteruri185 = new Uri.Builder().scheme("http")
                        .authority("image.tmdb.org")
                        .appendPath("t")
                        .appendPath("p")
                        .appendPath("w342")
                        .appendPath(movieObject.getString(MDB_POSTER_PATH).replace("/", ""))
                        .appendQueryParameter("api_key", API_KEY).build();
                try {
                    Bitmap bmp185 = Picasso.with(getActivity()).load(posteruri185).get();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp185.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Get the reviews
                String movie_id = movieObject.getString(MDB_MOVIE_ID);

                ContentValues values = new ContentValues();
                values.put(MovieContract.MovieEntry.COLUMN_TITLE, movieObject.getString(MDB_TITLE));
                values.put(MovieContract.MovieEntry.COLUMN_DATE, movieObject.getString(MDB_RELEASE_DATE));
                values.put(MovieContract.MovieEntry.COLUMN_RATING, movieObject.getString(MDB_AVERAGE_RATING));
                values.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, movieObject.getString(MDB_OVERVIEW));
                values.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, "0");
                values.put(MovieContract.MovieEntry.COLUMN_CATEGORY, category.toString());
                values.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, byteArray);
                // The reviews will be placed in a pipe delimited string in the SQLiteDatabase
                // to be parsed out later when displaying
                values.put(MovieContract.MovieEntry.COLUMN_REVIEWS, getReviews(movie_id));
                // The video keys will also be placed in a pipe delimited string in the SQListeDatabase
                // to be parsed out later when displaying to the user
                values.put(MovieContract.MovieEntry.COLUMN_VIDEOS, getVideos(movie_id));
                cVVector.add(values);
            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = getActivity().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
            }
            return null;
        }


        private String getReviews(String movie_id) {
            // Retrieve the reviews from themoviedb.org.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String reviewsJsonStrArray = null;

            String reviews = "";

            final String MDB_AUTHOR = "author";
            final String MDB_CONTENT = "content";
            final String MDB_RESULTS = "results";

            // Create the URL
            try {
                URL url = new URL(new Uri.Builder().scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(movie_id)
                        .appendPath("reviews")
                        .appendQueryParameter("api_key", API_KEY).build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));;

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() != 0) {
                    reviewsJsonStrArray = buffer.toString();
                    JSONObject reviewsJson = new JSONObject(reviewsJsonStrArray);
                    JSONArray resultsArray = reviewsJson.getJSONArray(MDB_RESULTS);
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject reviewObject = resultsArray.getJSONObject(i);
                        if (reviews != "") {
                            reviews += "|";
                        }
                        reviews += reviewObject.getString(MDB_AUTHOR);
                        reviews += " - " + reviewObject.getString(MDB_CONTENT);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return reviews;
        }

    }

    private String getVideos(String movie_id) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String videosJsonStrArray = null;

        String videos = "";

        final String MDB_KEY = "key";
        final String MDB_NAME = "name";
        final String MDB_SITE = "site";
        final String MDB_RESULTS = "results";

        // Create the URL
        try {
            URL url = new URL(new Uri.Builder().scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(movie_id)
                    .appendPath("videos")
                    .appendQueryParameter("api_key", API_KEY).build().toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));;

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() != 0) {
                videosJsonStrArray = buffer.toString();
                JSONObject videosJson = new JSONObject(videosJsonStrArray);
                JSONArray resultsArray = videosJson.getJSONArray(MDB_RESULTS);
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject reviewObject = resultsArray.getJSONObject(i);
                    if (videos != "") {
                        videos += "|";
                    }
                    videos += reviewObject.getString(MDB_SITE);
                    videos += "^" + reviewObject.getString(MDB_NAME);
                    videos += "^" + reviewObject.getString(MDB_KEY);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return videos;
    }



    private MovieContract.Category getCurrentSortOrder() {

        MovieContract.Category category = MovieContract.Category.popular;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (settings.getString(getString(R.string.pref_sort_order_key),getString(R.string.pref_sort_order_default)).equals("highestRated")) {
            category = MovieContract.Category.highest_rated;
        } else if (settings.getString(getString(R.string.pref_sort_order_key),getString(R.string.pref_sort_order_default)).equals("favorites")) {
            category = MovieContract.Category.favorite;
        }

        return category;

    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);

        public boolean hasTwoPanes();

    }


}
