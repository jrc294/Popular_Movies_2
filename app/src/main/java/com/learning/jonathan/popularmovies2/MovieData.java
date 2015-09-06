package com.learning.jonathan.popularmovies2;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.learning.jonathan.popularmovies2.data.MovieContract;

/**
 * Created by Jonathan.Cook on 8/6/2015.
 */
public class MovieData implements Parcelable {
    private int ID;
    private String title;
    private String overview;
    private String voteAverage;
    private String releaseDate;
    private Bitmap posterW185;
    private MovieContract.Category category;
    private String reviews;
    private String videos;

    public MovieData(Parcel source) {
        setTitle(source.readString());
        setOverview(source.readString());
        setVoteAverage(source.readString());
        setReleaseDate(source.readString());
        Bitmap bmp185 = source.readParcelable(Bitmap.class.getClassLoader());
        setPosterW185(bmp185);
        setID(source.readInt());
        String cat = source.readString();
        MovieContract.Category category;
        if (cat.equals("popular")) {
            category = MovieContract.Category.popular;
        } else if (cat.equals("highest_rated")) {
            category = MovieContract.Category.highest_rated;
        } else {
            category = MovieContract.Category.favorite;
        }
        setCategory(category);
    }

    public MovieData() {}

    public String getOverview() {
        if (this.overview.equals("null")) {
            this.overview = "";
        }
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Bitmap getPosterW185() {
        return posterW185;
    }

    public void setPosterW185(Bitmap poster) {
        this.posterW185 = poster;
    }

    public MovieContract.Category getCategory() {
        return category;
    }

    public void setCategory(MovieContract.Category category) {
        this.category = category;
    }

    public static final Parcelable.Creator<MovieData> CREATOR = new Parcelable.Creator<MovieData>() {

        @Override
        public MovieData createFromParcel(Parcel source) {
            return new MovieData(source);
        }

        @Override
        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };

    public String getVideos() {
        return videos;
    }

    public void setVideos(String videos) {
        this.videos = videos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getTitle());
        dest.writeString(getOverview());
        dest.writeString(getVoteAverage());
        dest.writeString(getReleaseDate());
        dest.writeParcelable(getPosterW185(), 0);
        dest.writeInt(getID());
        dest.writeString(category.toString());
        dest.writeString(getReviews());
    }

    public String getReviews() {
        return reviews;
    }

    public void setReviews(String reviews) {
        this.reviews = reviews;
    }
}
