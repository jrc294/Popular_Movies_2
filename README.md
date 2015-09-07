# Popular_Movies_2
Popular Movies part II

Please add the API key API_KEY in the static variables in MainFragment.java before running the app.

GENERAL APPROACH
================

I decided to cache all the information required by the app, including movie posters, in a SQLiteDtabase. The app
would download the data from the network for 'popular' movies when the app first starts, and would retrieve data
over the network for the 'highest-rated' movies, when the user chooses to view by 'highest-rated'. The list of 
movies for either 'popular' of 'highest-rated' would then not be updated until the chooses to 'refresh' from the menu.

The advantage of this approach is that the app would not be making duplicate calls to the internet which will save
on data usage. Also, all the data the app requires will be available when the app is off-line.

When the app is refreshing from the internet, I decided to lock the screen orientation for the duration of the 
refresh. This prevented the app from stopping / starting mid refresh which might cause problems.

I also used a content provider for accessing the data in the database.

DATABASE
========

I added everything for the movie cache in a single table in the database. Additional tables should probably have been 
used for both the reviews and movie trailer links, as there is a many-to-one relationship there. However, I wanted
to get the project finished in order to get my application in for the Android Developer Summit, so I decided
to include the movie trailers and reviews in a single column using delimiters to separate out the different 
items. These entries would later be parsed out when adding to the detail view screen. This was done for brevity.

Also, I added a unique constraint on the movie title and release date columns to prevent duplicate movies
being added when the data was refreshed. This could happen when a movie was flagged as a favorite. Any favorite
movies would not be removed as part of the data refresh so the unique index would prevent a duplicate from
occuring. For right now, I let the database prevent the insertion and did not check for it in code. This should
probably need to change, but again, I wanted to get the project submitted.

GRID VIEW ADAPTER
=================

When loading the grid view with data coming back from the database, I decided to use an ArrayAdapter in
preference to a CursorAdapter. i.e. The data comes back from the database via a cusror initiated by the loader, 
and the data is stored in an arrayList of MovieStore objects which contain one entry per movie. This
arrayList is used in the array adapter for the gridView.

The reason I did it this way instead to using a CursorAdapter, is that I found that there was a notable
pause each time the returned Blob was decoded into the bitmap in the bind view method, making for an unsatisfactory
user experience. I found that if I decoded the Blob before adding the array to the arrayadapter, the grid view 
would scroll perfectly.

There's probably a better way to do this using a CursorAdapter. Not sure....

SUMMING UP
=========

I lifted quite a bit of the code from the sunshine app, especially for the master / detail layout design for the
tablet. I found this quite a challenging project. I wasn't sure how to add the additonal layout items for the 
detail page for the movie trailers, so there was probably a better way to do that also.


