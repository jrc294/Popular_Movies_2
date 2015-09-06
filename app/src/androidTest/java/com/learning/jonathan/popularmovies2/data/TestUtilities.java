package com.learning.jonathan.popularmovies2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by Jonathan.Cook on 8/22/2015.
 */
public class TestUtilities extends AndroidTestCase {

    static ContentValues createStarWarsMovieValues() {

        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Soooo Star Wars");
        testValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, "Long, long ago in a galaxy far, far away");
        testValues.put(MovieContract.MovieEntry.COLUMN_RATING, "10/10");
        testValues.put(MovieContract.MovieEntry.COLUMN_DATE, 7234);
        testValues.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, 0);
        testValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "path to image");
        testValues.put(MovieContract.MovieEntry.COLUMN_CATEGORY,"highest_rated");
        return testValues;
    }

    static ContentValues createStarWarsMovieValues2() {

        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Empire Strikes Back");
        testValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, "Long, long ago in a galaxy far, far away");
        testValues.put(MovieContract.MovieEntry.COLUMN_RATING, "10/10");
        testValues.put(MovieContract.MovieEntry.COLUMN_DATE, 7234);
        testValues.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, 1);
        testValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "path to image");
        testValues.put(MovieContract.MovieEntry.COLUMN_CATEGORY,"highest_rated");
        return testValues;
    }

    static long insertNorthPoleLocationValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createStarWarsMovieValues();

        long locationRowId;
        locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }


}
