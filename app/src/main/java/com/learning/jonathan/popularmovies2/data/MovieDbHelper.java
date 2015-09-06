package com.learning.jonathan.popularmovies2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.learning.jonathan.popularmovies2.MainFragment;

import static com.learning.jonathan.popularmovies2.data.MovieContract.MovieEntry;

/**
 * Created by Jonathan.Cook on 8/22/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 21;

    public static final String LOG_TAG = MainFragment.class.getSimpleName();

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " ( " +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_DATE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_IS_FAVORITE + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_CATEGORY + " TEXT NOT NULL," +
                MovieEntry.COLUMN_POSTER_IMAGE + " BLOB," +
                MovieEntry.COLUMN_REVIEWS + " TEXT," +
                MovieEntry.COLUMN_VIDEOS + " TEXT" +
                " );";
        final String SQL_CREATE_UNIQUE_INDEX = "CREATE UNIQUE INDEX IX_MOVIE ON " +
                MovieEntry.TABLE_NAME +
                "(" +
                MovieEntry.COLUMN_TITLE +
                "," +
                MovieEntry.COLUMN_DATE +
                ")";
        Log.d(LOG_TAG, "Create database");
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        // Create a unique index on movie title and release date. This will ensure that the same
        // movie will not be stored twice in the database.
        //
        // This is useful as movies marked as favorite will not be deleted when the user refreshes
        // the movie list, and therefore the potential exists for a duplicate of the favorite movie
        // to be reloaded on refresh. This constraint will prevent that from happening.
        db.execSQL(SQL_CREATE_UNIQUE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "Drop database");
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
    }
}
