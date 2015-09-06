package com.learning.jonathan.popularmovies2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.jonathan.popularmovies2.data.MovieContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    static final String DETAIL_FAVORITE = "FAVORITE";
    static final String DETAIL_VIDEOS = "VIDEOS";
    static final String DETAIL_KEY_IDS = "KEY_IDS";

    public static final int COLUMN_RATING = 1;
    public static final int COLUMN_SYNOPSIS = 2;
    public static final int COLUMN_POSTER_IMAGE = 3;
    public static final int COLUMN_CATEGORY = 4;
    public static final int COLUMN_TITLE = 5;
    public static final int COLUMN_IS_FAVORITE = 6;
    public static final int COLUMN_DATE = 7;
    public static final int COLUMN_REVIEWS = 8;
    public static final int COLUMN_VIDEOS = 9;



    private ImageView mPosterImage;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mAverageRating;
    private TextView mSynopsis;
    private TextView mReviews;
    private ImageView mFavoriteButton;
    private String mVideos;
    private HashMap<Integer, String> mKeyIds;


    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private boolean mFavorite = false;
    private String mShareURL;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_acivity, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareURL != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent(mShareURL));
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(DETAIL_URI, mUri);
        outState.putBoolean(DETAIL_FAVORITE, mFavorite);
        outState.putString(DETAIL_VIDEOS, mVideos);
        outState.putSerializable(DETAIL_KEY_IDS, mKeyIds);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mUri = savedInstanceState.getParcelable(DETAIL_URI);
            mFavorite = savedInstanceState.getBoolean(DETAIL_FAVORITE);
            mKeyIds = (HashMap<Integer, String>) savedInstanceState.getSerializable(DETAIL_KEY_IDS);
            mVideos = savedInstanceState.getString(DETAIL_VIDEOS);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mPosterImage = (ImageView) rootView.findViewById(R.id.detail_image_poster);
        mTitle = (TextView) rootView.findViewById(R.id.movie_title);
        mReleaseDate = (TextView) rootView.findViewById(R.id.detail_release_date);
        mAverageRating = (TextView) rootView.findViewById(R.id.detail_average_rating);
        mSynopsis = (TextView) rootView.findViewById(R.id.detail_overview);
        mReviews = (TextView) rootView.findViewById(R.id.detail_reviews);
        mFavoriteButton = (ImageView) rootView.findViewById(R.id.detail_favorite_button);

        // Get the extra information from the intent and populate the detailActivity view
        Bundle args = getArguments();
        if (args != null) {
            mUri = args.getParcelable(DetailFragment.DETAIL_URI);
        }
        if (mUri != null) {
            Cursor c = getActivity().getContentResolver().query(mUri, MainFragment.MOVIE_COLUMNS,null,null,null);
            if (c.moveToFirst()) {
                rootView.setBackgroundColor(Color.WHITE);
                mTitle.setText(c.getString(COLUMN_TITLE));
                String releaseDate = c.getString(COLUMN_DATE);
                if (releaseDate.length() == 10) {
                    releaseDate = releaseDate.substring(5, 7) + "/" + releaseDate.substring(8, 10) + "/" + releaseDate.substring(0, 4);
                    mReleaseDate.setText(releaseDate);
                }
                mAverageRating.setText(c.getString(COLUMN_RATING) + "/10");
                mSynopsis.setText(c.getString(COLUMN_SYNOPSIS));
                mReviews.setText(c.getString(COLUMN_REVIEWS).replace("|", "\n\n"));
                mFavorite = c.getInt(COLUMN_IS_FAVORITE) != 0;
                mVideos = c.getString(COLUMN_VIDEOS);
                mFavoriteButton.setImageResource(mFavorite ? R.drawable.ic_action_star_10 : R.drawable.ic_action_star_0);
                byte[] bytes = c.getBlob(COLUMN_POSTER_IMAGE);
                int length = bytes.length;
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, length);
                mPosterImage.setImageBitmap(bmp);
                mPosterImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                rootView.findViewById(R.id.detail_layout).setVisibility(View.INVISIBLE);
                rootView.findViewById(R.id.movie_title).setVisibility(View.INVISIBLE);
                mShareURL = null;
            }
            c.close();
        }

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite();
            }
        });

        // Split the different movies and dump them in a collection
        if (mVideos != null && !mVideos.equals("")) {
            // Switch the mVideos string to a map
            ArrayList<String> movieStrings = new ArrayList<>();
            int pos;
            do {
                pos = mVideos.indexOf("|");
                if (pos != -1) {
                    movieStrings.add(mVideos.substring(0,pos));
                    mVideos = mVideos.substring(pos + 1,mVideos.length());
                }
            } while (pos != -1);
            movieStrings.add(mVideos);

            // Next, iterate thru that collection and get a map of the name, key pairs

            Map<String, String> movieDetails = new HashMap<>();
            // Perform some string manipulation to parse out the name and key of the movie from the database entry
            boolean isFirst = true;
            for (String movieString : movieStrings) {
                int ix = movieString.indexOf("^");
                if (ix != -1) {
                    String site = movieString.substring(0, ix);
                    movieString = movieString.substring(ix + 1, movieString.length());
                    ix = movieString.indexOf("^");
                    if (ix != -1) {
                        String name = movieString.substring(0, ix);
                        String key = movieString.substring(ix + 1, movieString.length());
                        if (site.toUpperCase().equals("YOUTUBE")) {
                            movieDetails.put(name, key);
                            if (isFirst) {
                                mShareURL = getVideoUri(key).toString();
                                isFirst = false;
                            }
                        }
                    }
                }
            }

            // Add dynamic entries for the video trailers
            RelativeLayout detailLayout = (RelativeLayout) rootView.findViewById(R.id.detail_layout);

            mKeyIds = new HashMap<Integer,String>();
            int ix = 0;
            for (Map.Entry<String,String> entry : movieDetails.entrySet()) {
                String name = entry.getKey();
                String key = entry.getValue();
                // Inflate our video section
                RelativeLayout videoLayout = (RelativeLayout) inflater.inflate(R.layout.video_item, container, false);
                TextView videoName = (TextView) videoLayout.findViewById(R.id.video_name);

                videoLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the ID of the videoLayout that has been clicked so that we can start the implied intent and pass
                        // it the youTube key to be played
                        int viewid = v.getId();
                        playVideo(mKeyIds.get(viewid));
                    }
                });

                videoName.setText(name);
                // Create some relative layout params for our new layout
                RelativeLayout.LayoutParams videoLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                // Add a rule to say that we are going to add the VideoLayout below the video title from the xml layout
                videoLayoutParams.addRule(RelativeLayout.BELOW, R.id.detail_videos_title + ix);
                ix++;
                // Give the video layout an id so that we can use the ID to add further VideoLayout pieces, and also store that
                // ID as reference for the video key that will be played
                videoLayout.setId(R.id.detail_videos_title + ix);
                mKeyIds.put(R.id.detail_videos_title + ix, key);
                videoLayout.setLayoutParams(videoLayoutParams);
                detailLayout.addView(videoLayout);
            }

        }

        return rootView;
    }

    private void toggleFavorite() {

        ContentValues values = new ContentValues();
        if (mFavorite) {
            values.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, 0);
        } else {
            values.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, 1);
        }
        int rowsUpdated = getActivity().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,values, MovieContract.MovieEntry._ID + " = ?", new String[]{MovieContract.MovieEntry.getIDSettingFromUri(mUri)});
        if (rowsUpdated == 1) {
            if (mFavorite) {
                mFavorite = false;
                mFavoriteButton.setImageResource(R.drawable.ic_action_star_0);
            } else {
                mFavorite = true;
                mFavoriteButton.setImageResource(R.drawable.ic_action_star_10);
            }
        }
    }

    private void playVideo(String key) {
        //String location = Utility.getPreferredLocation(this);

        Uri uri = getVideoUri(key);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + uri + ", no receiving apps installed!");
        }
    }

    private Uri getVideoUri(String key) {
        return new Uri.Builder().scheme("https")
                .authority("www.youtube.com")
                .appendPath("watch")
                .appendQueryParameter("v", key).build();
    }

    private Intent createShareTrailerIntent(String URL) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, URL);
        return shareIntent;
    }

}
