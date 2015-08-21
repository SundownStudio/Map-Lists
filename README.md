# Map-Lists
This Android project allows you to associate schemaless data with Google Maps markers, essentially you can add your own list of data to any map marker (text, photos, ratings, emails, etc. including lists of lists). It is intended for anybody looking to manage a data-set upon a geographical interface.

## Requirements
To run this you will need to add your own Google Maps and Google Places API keys to strings.xml. Make sure your emulator (or phone) has Google-Play Services for geocoding. In addition this app uses Couchbase Mobile for backend data storage (NoSQL). minimum sdk: 15


## Notes  
I've been developing this on my own so comments are kinda sparse.  
This project is currently in development so updates will be forthcoming. 

## Contributions
Contributions are always welcome, the big jobs left to do are:    

* setting up CouchBase server and Sync Gateway on a cloud instance to handle multiple users, the sync function should support individual and group channels    

* enabling sort functionality for list items (sort alphabetically, by date, or through some index the user defines on a variable within their list)  

* graphics art and stylizing

* testing!     

If you're interested in helping out please feel free to open an issue and/or shoot me an email. Thanks!
