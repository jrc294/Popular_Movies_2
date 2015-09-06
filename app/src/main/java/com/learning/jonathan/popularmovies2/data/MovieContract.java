package com.learning.jonathan.popularmovies2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Jonathan.Cook on 8/22/2015.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.learning.jonathan.popularmovies2";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final  String PATH_MOVIE = "movie";

    public static enum Category {popular, highest_rated, favorite}

    public static final class MovieEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table name
        public static final String TABLE_NAME = "movie";

        // Movie title
        public static final String COLUMN_TITLE = "title";

        // Movie synopsis
        public static final String COLUMN_SYNOPSIS = "synopsis";

        // Average rating by user
        public static final String COLUMN_RATING = "rating";

        // Movie release date
        public static final String COLUMN_DATE = "date";

        // Flag indicating if Movie is a user favorite
        public static final String COLUMN_IS_FAVORITE = "is_favorite";

        // Column to store if the movie is in the most popular category or highest rated
        public static final String COLUMN_CATEGORY = "category";

        // Column to store movie image byte array
        public static final String COLUMN_POSTER_IMAGE = "poster_image";

        // Column to store reviews delimited with a ^
        public static final String COLUMN_REVIEWS = "reviews";

        // Column to store youTube keys for playing video trailers delimited with a ^
        public static final String COLUMN_VIDEOS = "videos";

        // Method to build a Uri for querying the Movie with category which is
        public static Uri buildMoviesByCategory(Category categorySetting) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_CATEGORY).appendPath(categorySetting.toString()).build();
        }

        // Method to build a Uri for querying an individual movie back
        public static Uri buildMovieById(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Method to return the ID of the movie from the Uri
        public static String getIDSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        // Method to return the category from the Uri
        public static String getCategorySettingFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

}
