# Map-Lists
This Android project allows you to associate schemaless data with Google Maps markers, essentially you can add your own list of data to any map marker (text, photos, ratings, etc. including lists of lists). It is intended for anybody looking to manage a data-set upon a geographical interface. A full synopsis of the project is available [here](http://matthewgusella.com/maplists/)

## Requirements
To run this you will need to add your own Google Maps and Google Places API keys to strings.xml. Make sure your device has both Google Maps and Google-Play Services installed. If you're using Genymotion instructions are available [here](https://github.com/codepath/android_guides/wiki/Genymotion-2.0-Emulators-with-Google-Play-support)
In addition this app uses Couchbase Mobile for backend data storage (NoSQL). minimum sdk: 15


## Notes  
Activities behave as parent-controllers for their respective Fragments hosted between upper and lower Toolbars.
Activities handle all Toolbar clicks and swap fragments as needed.
Each Fragment is a controller for its own views and models.
I've been developing this project on my own so comments are sparse but the code should hopefully be straightforward.

## Contributions
Contributions are always welcome, the big jobs left to do are:    

* setting up CouchBase server and Sync Gateway on a cloud instance to handle multiple users, the sync function should support individual and group channels    

* enabling sort functionality for list items (sort alphabetically, by date, or through some index the user defines on a variable within their list)  

* graphics art and stylizing

* testing!     

If you're interested in helping out please feel free to open an issue and/or shoot me an email. Thanks!
