package com.learning.jonathan.popularmovies2.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Jonathan.Cook on 8/23/2015.
 */
public class TestProvider extends AndroidTestCase {

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        // content://com.learning.jonathan.popularmovies2/movie/
        long testID = 1;
        String type = mContext.getContentResolver().getType(MovieContract.MovieEntry.buildMovieById(testID));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the MovieContract.MovieEntry CONTENT_URI should return MovieContract.MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_ITEM_TYPE, type);

        MovieContract.Category testCategory = MovieContract.Category.popular;
        // content://com.learning.jonathan.popularmovies2/movie/category
        type = mContext.getContentResolver().getType(
                MovieContract.MovieEntry.buildMoviesByCategory(testCategory));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the MovieContract.MovieEntry CONTENT_URI with location should return MovieContract.MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_TYPE, type);

    }

    /*public void testBasicLocationQueries() {
        // insert our test records into the database

        long locationRowId = TestUtilities.insertNorthPoleLocationValues(mContext);

        // Test the basic content provider query
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.buildMoviesByCategory(MovieContract.Category.highest_rated),
                null,
                null,
                null,
                null
        );

        String tit = "";
        try {
            if (movieCursor.moveToFirst()) {
                do {
                    int pos = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
                    tit = movieCursor.getString(pos);
                    tit = "1 + " + tit;
                } while (movieCursor.moveToNext());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        movieCursor.close();

        // Test the basic content provider query
        Uri movieUri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, TestUtilities.createStarWarsMovieValues2());
        movieCursor = mContext.getContentResolver().query(
                movieUri,
                null,
                null,
                null,
                null
        );

        try {
            if (movieCursor.moveToFirst()) {
                do {
                    int pos = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
                    tit = movieCursor.getString(pos);
                    tit = "1 + " + tit;
                } while (movieCursor.moveToNext());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        movieCursor.close();


        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicLocationQueries, movie query", movieCursor, TestUtilities.createStarWarsMovieValues2());

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Movie Query did not properly set NotificationUri",
                    movieCursor.getNotificationUri(), MovieContract.MovieEntry.buildMovieById(locationRowId));
        }


    }*/

    // Test Update
/*    public void testUpdate() {
        // Delete all rows
        new MovieDbHelper(mContext).getWritableDatabase().delete(MovieContract.MovieEntry.TABLE_NAME,null,null);
        // Insert
        Uri uri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,TestUtilities.createStarWarsMovieValues2());
        Long id = Long.valueOf(MovieContract.MovieEntry.getIDSettingFromUri(uri));
        // Get the URI back and test the value of favorie
        Cursor c = mContext.getContentResolver().query(MovieContract.MovieEntry.buildMovieById(id),
                new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_IS_FAVORITE},
                null, null, null);
        c.moveToFirst();
        String sid = c.getString(0);
        String cat = c.getString(1);
        c.close();
        // Update
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, "1");
        int rowsUpdated = mContext.getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                cv,
                MovieContract.MovieEntry._ID + " = ? ",
                new String[]{sid});
        c = mContext.getContentResolver().query(MovieContract.MovieEntry.buildMovieById(id),
                new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_IS_FAVORITE},
                null, null, null);
        c.moveToFirst();
        sid = c.getString(0);
        cat = c.getString(1);
        c.close();

    }*/

    // Test Delete
    public void testDelete() {
        // Delete all rows
        new MovieDbHelper(mContext).getWritableDatabase().delete(MovieContract.MovieEntry.TABLE_NAME,null,null);
        // Insert three rows, two of them which are favories
        // Insert
        Uri uri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,TestUtilities.createStarWarsMovieValues());
        uri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,TestUtilities.createStarWarsMovieValues());
        uri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,TestUtilities.createStarWarsMovieValues2());
        // Now delete only the non-favories, rows 1 and 2.
        int rowsDeleted = mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, MovieContract.MovieEntry.COLUMN_IS_FAVORITE + " != ? ", new String[]{"1"});
        int x = 1;
        x++;

    }
}
