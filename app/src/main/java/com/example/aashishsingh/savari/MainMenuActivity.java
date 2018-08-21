package com.example.aashishsingh.savari;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.places.PlaceManager;
import com.facebook.places.model.PlaceFields;
import com.facebook.places.model.PlaceSearchRequestParams;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainMenuActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 5;
    public final int LOGOUT_CODE = 0;
    private DrawerLayout mDrawerLayout;
    protected GeoDataClient mGeoDataClient;
    public final int PLACE_PICKER_REQUEST = 4;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private String currentPlace = "No place set yet!";
    private Geocoder mGeocoder;
    private String name = "no name set yet";
    private FusedLocationProviderClient mFusedLocationClient;
    private Location l;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;
    private boolean mLocationPermissionGranted;
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayList<String> Category = new ArrayList<String>();
    private String mRestaurantType = "getting global restuarant type";
    static final int REQUEST_IMAGE_CAPTURE = 5;
    private ImageView mImageView;
    Bitmap imageBitmap = null;
    private String mCurrentPhotoPath = "shitShit";
    private Uri myURI;
    private File myFile;
    private String mPhotoPath = "figuring path...";
    private Place mPlace;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls_MainMenu);
        mContentView = findViewById(R.id.fullscreen_content_mainMenu);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });


        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        if (menuItem.getItemId() == R.id.sign_out) {
                            OnLogOut();
                        } else if (menuItem.getItemId() == R.id.change_city) {
                            OnChangeCityClick();

                        } else if (menuItem.getItemId() == R.id.discover) {
                            createRestaurantCategoryArray();
                            getCustomRestaurants();
                            getCustomEntertainment();
                            Log.v("DISCOVER PRESSED", "RESTAURANTS SHOULD HAVE BEEN PRINTED!");

                        } else {
                            //organize photos
                            dispatchTakePictureIntent();
                            String toastMessage = "Image saved to " + mPhotoPath;
                            Toast.makeText(MainMenuActivity.this,
                                    toastMessage, Toast.LENGTH_LONG).show();

                            Log.v("ORGANIZE PHOTOS:", "all tasks complete!");

                        }

                        return true;
                    }
                });

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        TextView tv = findViewById((R.id.fullscreen_content_mainMenu));
        name = getIntent().getStringExtra("accountName");
        list.addAll(getIntent().getStringArrayListExtra("list"));
        String location = getIntent().getStringExtra("accountLocation");
        tv.setText("Welcome " + name + ", Lets explore " + currentPlace);
        tv.setTextSize(25);


    }

    public void createText() {
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        TextView tv = findViewById((R.id.fullscreen_content_mainMenu));
        tv.setText("Welcome " + name + ", Lets explore " + currentPlace + "!");
        tv.setTextSize(25);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("REQUESTCODE:", requestCode + ", result code: " + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.v("ONACTIVITYRESULT:", "reached camera's on activity result");
        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                mPlace = place;
                //String toastMsg = String.format("Place: %s", place.getName());
                currentPlace = place.getName().toString();
                mGeocoder = new Geocoder(this, Locale.getDefault());
                try {
                    currentPlace = getCityNameByCoordinates(place.getLatLng().latitude, place.getLatLng().longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                mPlace = place;
                mGeocoder = new Geocoder(this, Locale.getDefault());


                try {
                    currentPlace = getCityNameByCoordinates(place.getLatLng().latitude, place.getLatLng().longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("Getting place name:", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("Getting place name:", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        createText();
    }

    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return "city not found";
    }

    public void logOutIntent() {
        Log.v("LOGOUT", "logging out...");
        Intent loginIntent = new Intent(this, MainActivity.class);
        startActivityForResult(loginIntent, LOGOUT_CODE);
    }

    public void OnLogOut() {
        LoginManager.getInstance().logOut();
        logOutIntent();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void OnChangeCityClick() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }


    }

    private boolean checkSubstring(String str) {
        int x = 0;
        String s = "";
        while(x < list.size()) {
            boolean contains = list.get(x).toLowerCase().contains(str.toLowerCase());
            if(contains) {
                return true;
            }
            x++;
        }
        return false;
    }

    private void createRestaurantCategoryArray() {
        if(checkSubstring("chinese")) {
            Log.v("USER___LIKES", "chinese present!");
            Category.add("chinese");
        }
        if(checkSubstring("indian")) {
            Log.v("USER___LIKES", "indian present!");
            Category.add("indian");
        }
        if(checkSubstring("mexican")) {
            Log.v("USER___LIKES", "mexican present!");
            Category.add("mexican");
        }
        if(checkSubstring("italian")) {
            Log.v("USER___LIKES", "italian present!");
            Category.add("italian");
        }
        if(checkSubstring("halal")) {
            Log.v("USER___LIKES", "halal present!");
            Category.add("halal");
        }
        if(checkSubstring("vegetarian")) {
            Log.v("USER___LIKES", "vegetarian present!");
            Category.add("vegetarian");
        }
        if(checkSubstring("kosher")) {
            Log.v("USER___LIKES", "kosher present!");
            Category.add("kosher");
        }
        if(checkSubstring("middle eastern") || checkSubstring("middle-eastern") || checkSubstring("mideast")
                || (checkSubstring("east") && checkSubstring("middle")) || checkSubstring("Persian")
                || checkSubstring("iranian") || checkSubstring("persian")) {
            Log.v("USER___LIKES", "middle eastern present!");
            Category.add("middle eastern");
            Category.add("middle-eastern");
            Category.add("mideast");
            Category.add("iranian");
            Category.add("persian");
            Category.add("arabian");
        }

        if(checkSubstring("english")) {
            Log.v("USER___LIKES", "english");
            Category.add("english");
        }

    }


    private void getCustomRestaurants() {
        int count = 0;
        while(count < Category.size()) {
            PlaceSearchRequestParams.Builder builder =
                    new PlaceSearchRequestParams.Builder();
            String restaurantType = Category.get(count);
            setRestaurant(restaurantType);

            builder.setSearchText(restaurantType);
            builder.setDistance(30000); //in meters
            builder.setLimit(10);
            builder.addField(PlaceFields.NAME);
            builder.addField(PlaceFields.LOCATION);
            builder.addField(PlaceFields.PHONE);
            builder.addCategory("FOOD_BEVERAGE");


// Get the current location from LocationManager or FusedLocationProviderApi
            Location location = getLastLocation();
            location.setLatitude(mPlace.getLatLng().latitude);
            location.setLongitude(mPlace.getLatLng().longitude);
            Log.v("LAST LOCATION: ", location.toString());
            GraphRequest request =
                    PlaceManager.newPlaceSearchRequestForLocation(builder.build(), location);

            request.setCallback(new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    Log.v("OnCompleted reached!", "YAAY");
                    Log.v("RESTAURANTS NEARBY", "" + getRestaurantGlobal() + response.toString());
                }
            });

            request.executeAsync();
            count++;
        }
    }

    private void getCustomEntertainment() {
        int count = 0;
        while(count < Category.size()) {
            PlaceSearchRequestParams.Builder builder =
                    new PlaceSearchRequestParams.Builder();
            String EntertainmentType = Category.get(count);
            setRestaurant(EntertainmentType);

            builder.setSearchText(EntertainmentType);
            builder.setDistance(30000); //in meters
            builder.setLimit(10);
            builder.addField(PlaceFields.NAME);
            builder.addField(PlaceFields.LOCATION);
            builder.addField(PlaceFields.PHONE);
            builder.addCategory("ARTS_ENTERTAINMENT");


// Get the current location from LocationManager or FusedLocationProviderApi
            Location location = getLastLocation();
            location.setLatitude(mPlace.getLatLng().latitude);
            location.setLongitude(mPlace.getLatLng().longitude);
            Log.v("LAST LOCATION: ", location.toString());
            GraphRequest request =
                    PlaceManager.newPlaceSearchRequestForLocation(builder.build(), location);

            request.setCallback(new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    Log.v("OnCompleted reached!", "YAAY");
                    Log.v("ARTS NEARBY", "" + getRestaurantGlobal() + response.toString());
                }
            });

            request.executeAsync();
            count++;
        }
    }

    private void setRestaurant(String st){
        mRestaurantType = st;
    }

    private String getRestaurantGlobal() {
        return mRestaurantType;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    public Location getLastLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            getLocationPermission();
        }
        Task<Location> t = null;
        if (mLocationPermissionGranted) {
            t = mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                            }
                        }
                    });
        }
        while(!t.isSuccessful()) {

        }
        l = t.getResult();
        return l;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.v("DISPATCHPIC", "Threw IO Exception");
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                myURI = photoURI;
                myFile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

        Log.v("DISPATCHPIC:", "completed all tasks!");

    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = currentPlace + timeStamp + "_" + ".jpg";
        String FolderPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() +
                "/" + currentPlace + " Photos" + "/";
        mPhotoPath = FolderPath;
        File path = new File(FolderPath);
        if(!path.exists()) {
            Log.v("CREATEIMAGEFILE:", "creating new path...");
            path.mkdirs();
        }

        File image = File.createTempFile(imageFileName, ".jpg", path);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Log.v("GALLERYADDPIC:", mCurrentPhotoPath);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }



}


