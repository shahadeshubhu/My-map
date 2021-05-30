# My-map
an app that does the following: </br>
1) displays a Google map and allow users to switch between different views; 
2) contains a floating action button (FAB) that will display the current location when the user clicks it. 
3) has an SQLite database and a content provider so that: </br>
  • On clicking the Map, a marker will be drawn at the taped location, and the corresponding latitude longitude coordinates with map zoom level will be saved in the database in background. </br>
  • On restarting the app/changing orientation, the saved locations are retrieved from the database in background and redrawn on the map. </br>
  • On long-clicking, all makers will be cleared from the map, and all data will be removed from the database in background.
