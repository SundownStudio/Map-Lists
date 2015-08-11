# Map-Lists
This Android project allows you to associate schemaless data with Google Maps markers, essentially you can add your own list of data to any map marker (text, photos, ratings, emails, etc. including lists of lists). It is intended for anybody looking to manage a data-set upon a geographical interface.

## Requirements
To run this you will need to add your own Google Maps and Google Places API keys in strings.xml. Make sure your emulator (or phone) has Google-Play Services for geocoding. In addition this app uses Couchbase Mobile for backend data storage (NoSQL). minimum sdk: 15

Dependencies should look similar to this:

```
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:21.0.3'
    compile 'com.google.android.gms:play-services:7.5.0'
    compile 'com.android.support:recyclerview-v7:21.0.3'
    compile 'com.android.support:cardview-v7:21.0.+'
    compile 'com.couchbase.lite:couchbase-lite-android:1.0.4'
}

```

## Notes
This project is currently in development so updates will be forthcoming. Any contributors are always welcome.
