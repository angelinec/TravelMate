package com.example.test.TravelMate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Angeline Cheah on 2/4/2015.
 */
public class NearbyPlaces extends ActionBarActivity  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private int foodIcon, drinkIcon, shopIcon, otherIcon, selPlaceIcon;
    private GoogleMap googleMap;
    private Marker[] placeMarkers;
    private Marker marker;
    private MarkerOptions[] places;
    private Button mapTypeBtn;
    private Button diningBtn;
    private Button shopMallBtn;
    private Button allPlacesBtn;
    private double latitude;
    private double longitude;
    private GoogleApiClient mGoogleApiClient;
    private MarkerOptions placeSelectedMarker;
    private static final int REQUEST_PLACE_PICKER = 1;

    private TextView selPlaceName;
    private TextView selPlaceAddress;
    private TextView selPlacePhone;

    private String placeName = "";
    private String vicinity = "";

    LocationRequest mLocationRequest=new LocationRequest();
    boolean requestLocUpdate=true;
    private NetworkInfo activeNetworkInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby_places);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        foodIcon = R.drawable.orange_dot;
        drinkIcon = R.drawable.ltblue_dot;
        shopIcon = R.drawable.green_dot;
        otherIcon = R.drawable.purple_dot;
        selPlaceIcon = R.drawable.red_dot;
        mapTypeBtn = (Button) findViewById(R.id.mapTypeBtn);
        diningBtn = (Button) findViewById(R.id.diningBtn);
        shopMallBtn = (Button) findViewById(R.id.shopMallBtn);
        allPlacesBtn = (Button) findViewById(R.id.allPlaces);
        selPlaceName = (TextView) findViewById(R.id.placeName);
        selPlaceAddress = (TextView) findViewById(R.id.placeAddress);
        selPlacePhone = (TextView) findViewById(R.id.placePhone);

        mapTypeBtn.setTag(1);
        int MAX_PLACES = 20;
        placeMarkers = new Marker[MAX_PLACES];


        try { // verify we can interact with the Google Map
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }

            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //set map type
            googleMap.setMyLocationEnabled(true); // Place dot on current location
            googleMap.setTrafficEnabled(true); // Turns traffic layer on
            googleMap.setIndoorEnabled(true);  // Enables indoor maps
            googleMap.setBuildingsEnabled(true); // Turns on 3D buildings
            googleMap.getUiSettings().setZoomControlsEnabled(true); // Show Zoom buttons
            googleMap.getUiSettings().setCompassEnabled(true); //enables compass

            mGoogleApiClient = new GoogleApiClient.Builder(this) //set up client before connecting
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .build();

            mLocationRequest.setInterval(5000); //request location every 5 seconds
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //toggle map mode
        mapTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int btnTag = (Integer) view.getTag();
                if (btnTag == 1) {
                    mapTypeBtn.setTag(2);
                    mapTypeBtn.setText("NORMAL MODE");
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                } else if (btnTag == 2) {
                    mapTypeBtn.setTag(1);
                    mapTypeBtn.setText("SATELLITE MODE");
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });

        //retrieve dining places
        diningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkNetworkAvailability(); //check if Internet is enabled
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) { //if enabled

                    String diningSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/"
                            + "json?location=" + latitude + "," + longitude + "&radius=5000&sensor=true" +
                            "&types=food%7Crestaurants%7Ccafe" +
                            "&key=AIzaSyB8QO1QLj_5wnYa1viVEUjBbX-4UAioFj8"; //string HTTP query to send

                    new GetPlaces().execute(diningSearchStr); //execute HTTP request

                }else { //if disabled, inform user
                    alertInternetDialog();
                }
            }
        });

        //retrieve malls
        shopMallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkNetworkAvailability(); //check if Internet is enabled
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) { //if enabled

                    String shopMallSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/"
                            + "json?location=" + latitude + "," + longitude + "&radius=5000&sensor=true" +
                            "&types=shopping_mall" +
                            "&key=AIzaSyB8QO1QLj_5wnYa1viVEUjBbX-4UAioFj8"; //string HTTP query to send

                    new GetPlaces().execute(shopMallSearchStr); //execute HTTP request

                }else { //if disabled, inform user
                    alertInternetDialog();
                }
            }
        });

        //retrieve all places nearby
        allPlacesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkNetworkAvailability(); //check if Internet is enabled
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) { //if enabled
                    try {
                        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                        Intent intent = intentBuilder.build(getApplicationContext()); //prepare Intent

                        // Start the Intent by requesting a result, identified by a request code.
                        startActivityForResult(intent, REQUEST_PLACE_PICKER);

                    } catch (GooglePlayServicesRepairableException e) { //catch and show error message
                        e.printStackTrace();
                        Toast.makeText(NearbyPlaces.this, "ServicesRepairableException", Toast.LENGTH_SHORT).show();

                    } catch (GooglePlayServicesNotAvailableException e) { //catch and show error message
                        e.printStackTrace();
                        Toast.makeText(NearbyPlaces.this, "Service not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    alertInternetDialog();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect(); //connect Client
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect(); //disconnect Client
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect(); //connect Client
    }

    @Override
    public void onConnected(Bundle bundle) {
        //get location service
        final LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );

        if ( !locManager.isProviderEnabled(LocationManager.GPS_PROVIDER ) ) { //if GPS is disabled
            alertGPSDialog(); //inform user

        } else { //if GPS enabled, continue process
            updatePlace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) { //listen to suspended connection
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { //listen to failed connection
    }

    private void alertGPSDialog() { //dialog to inform user on GPS service

        AlertDialog.Builder dBox = new AlertDialog.Builder(this);
        dBox.setMessage("GPS service is disabled. Enable it?");
        dBox.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {
                //open Settings intent
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        dBox.setNegativeButton("Cancel", null);
        dBox.show();
    }

    private void updatePlace() {

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (requestLocUpdate) { //trying to get the update location
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

            try { //get LatLng to animate camera on map
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(latitude, longitude))      // Sets the center of the map to user's location
                        .zoom(14)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition)); //move map view

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
    }

    private void alertInternetDialog() { //dialog to user on Internet service
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Internet connection is disabled. Enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //open Settings intent
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    private boolean checkNetworkAvailability() { //check for Internet service
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //retrieved data from intent
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this); //get place selected from Place Picker intent
                selPlaceName.setText(place.getName().toString());
                selPlaceAddress.setText(place.getAddress().toString());
                selPlacePhone.setText(place.getPhoneNumber().toString());
                LatLng placeSelected = place.getLatLng();

                placeSelectedMarker = new MarkerOptions() //prepare marker to plot on map
                        .position(placeSelected)
                        .title(place.getName().toString())
                        .icon(BitmapDescriptorFactory.fromResource(selPlaceIcon))
                        .snippet(place.getAddress().toString());

                if (placeMarkers != null) { //if other markers are on map, remove all; check array
                    for (int pm = 0; pm < placeMarkers.length; pm++) {
                        if (placeMarkers[pm] != null)
                            placeMarkers[pm].remove();
                    }
                }

                marker = googleMap.addMarker(placeSelectedMarker); //place marker on map
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) { //set info window listener
                        selPlaceName.setText(marker.getTitle());
                        selPlaceAddress.setText(marker.getSnippet());
                    }
                });
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) { //check for location change
        Location locationUpdate = location;
    }

    private class GetPlaces extends AsyncTask<String, Void, String> { //get list of places from server
        @Override
        protected String doInBackground(String... placesURL) {

            StringBuilder placesBuilder = new StringBuilder(); //create and store places

            //process search parameter string(s)
            for (String placeSearchURL : placesURL) {
                //execute search
                HttpClient placesClient = new DefaultHttpClient();

                try {
                    //try to fetch the data
                    HttpGet placesGet = new HttpGet(placeSearchURL);
                    HttpResponse placesResponse = placesClient.execute(placesGet);
                    StatusLine placeSearchStatus = placesResponse.getStatusLine();
                    if (placeSearchStatus.getStatusCode() == 200) {
                        //OK response received
                        HttpEntity placesEntity = placesResponse.getEntity();

                        InputStream placesContent = placesEntity.getContent(); //retrieved content
                        InputStreamReader placesInput = new InputStreamReader(placesContent);
                        BufferedReader placesReader = new BufferedReader(placesInput);

                        String lineIn;
                        while ((lineIn = placesReader.readLine()) != null) { //if reader not null
                            placesBuilder.append(lineIn); //add in placebuilder
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return placesBuilder.toString();
        }
//fetch and parse place data

        protected void onPostExecute(String result) {
            //parse place data returned from Google Places API for Android

            if (placeSelectedMarker != null) { //remove red marker plotted by place picker
                marker.remove();

            }else if (placeMarkers != null) { //if place array containing colour markers not null
                for (int pm = 0; pm < placeMarkers.length; pm++) {
                    if (placeMarkers[pm] != null) //if LatLng not null, remove all
                        placeMarkers[pm].remove();
                }
            }
            try {
                //parse JSON
                JSONObject resultObject = new JSONObject(result);
                JSONArray placesArray = resultObject.getJSONArray("results"); //retrieve "results" array
                places = new MarkerOptions[placesArray.length()]; //set places MarkerOptions array size
                //loop through places array
                for (int p = 0; p < placesArray.length(); p++) {
                    //parse each place
                    boolean missingValue = false;
                    LatLng placeLatLong = null;
                    int currIcon = otherIcon;

                    try {
                        //attempt to retrieve place data values
                        missingValue = false;
                        JSONObject placeObject = placesArray.getJSONObject(p); //retrieve pbject from position
                        JSONObject geoLoc = placeObject.getJSONObject("geometry").getJSONObject("location");
                        placeLatLong = new LatLng(
                                Double.valueOf(geoLoc.getString("lat")),
                                Double.valueOf(geoLoc.getString("lng"))); //create a LatLng object

                        JSONArray types = placeObject.getJSONArray("types");
                        for (int t = 0; t < types.length(); t++) {
                            //identify type of place returned
                            String thisType = types.get(t).toString();
                            if (thisType.contains("food")) {
                                currIcon = foodIcon;
                                break;
                            } else if (thisType.contains("restaurant")) {
                                currIcon = foodIcon;
                                break;
                            } else if (thisType.contains("shopping_mall")) {
                                currIcon = shopIcon;
                                break;
                            } else if (thisType.contains("cafe")) {
                                currIcon = drinkIcon;
                                break;
                            }
                        }
                        vicinity = placeObject.getString("vicinity"); //get place's vicinity
                        placeName = placeObject.getString("name"); //get place's name

                    } catch (JSONException jse) {
                        missingValue = true;
                        jse.printStackTrace();
                    }
                    if (missingValue) places[p] = null; //no values retrieved
                    else
                        places[p] = new MarkerOptions() //prepare marker for each place
                                .position(placeLatLong)
                                .title(placeName)
                                .icon(BitmapDescriptorFactory.fromResource(currIcon))
                                .snippet(vicinity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (places != null && placeMarkers != null) { //if arrays not null
                for (int p = 0; p < places.length && p < placeMarkers.length; p++) {
                    //will be null if a value was missing
                    if (places[p] != null) {
                        placeMarkers[p] = googleMap.addMarker(places[p]); //add markers on map
                        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) { //shows place details when
                                selPlaceName.setText(marker.getTitle());    //info window is clicked
                                selPlaceAddress.setText(marker.getSnippet());
                            }
                        });
                    }
                }
            }
        }
    }
}