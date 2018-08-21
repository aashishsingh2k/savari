package com.example.aashishsingh.savari;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.LoginStatusCallback;
import com.facebook.Profile;
import com.facebook.ProfileManager;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.places.PlaceManager;
import com.facebook.places.model.PlaceFields;
import com.facebook.places.model.PlaceSearchRequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.Permissions;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    public CallbackManager callbackManager;
    public String accountName = "no name entered yet!";
    public final int ACCESS_CODE = 1;
    public ProfileTracker profileTracker;
    public String accountLocation = "No City yet!";
    public String ID = "no ID set yet!";
    private LoginManager lm;
    private AccessToken accessToken;
    //private LoginButton loginButton;
    //private boolean isLoggedIn = false;
    private boolean comingFromLogout = false;
    private ArrayList<String> list = null;

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

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);
        TextView mainText = findViewById(R.id.fullscreen_content);
        mainText.setTextColor(Color.WHITE);
        //mainText.setTextSize(25);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        accessToken = AccessToken.getCurrentAccessToken();

        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        callbackManager = CallbackManager.Factory.create();
        Intent it = getIntent();
        if(it != null) {
            comingFromLogout = true;
        }
        if(isLoggedIn) {
            Log.v("alreadyLoggedIn", "user already logged in...");
            ID = accessToken.getUserId();
            goToMainMenu(accessToken);
        }


        else if(!comingFromLogout) {
            Log.v("NOTalreadyLoggedIn", "user not already logged in...");
            loginCode();
        }

        if(comingFromLogout) {
            comingFromLogout = false;
            LoginButton loginButton = findViewById(R.id.login_button);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginCode();
                }
            });


        }

    }

    public void loginCode() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_likes"));
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(
                "user_likes"));

        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.v("Recent permission onSuccess",
                                loginResult.getRecentlyGrantedPermissions().toString());
                        accessToken = loginResult.getAccessToken();
                        Button b = findViewById(R.id.login_button);
                        b.setVisibility(View.INVISIBLE);
                        ID = accessToken.getUserId();
                        goToMainMenu(accessToken);
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "Logged Out!",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.v("ERROR:", exception.toString());
                        Toast.makeText(getApplicationContext(), "Error: 101: " +
                                        "Please try again later...",
                                Toast.LENGTH_LONG).show();

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void goToMainMenu(AccessToken accessToken) {
        /*LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(
                "pubic_profile"));*/
        //ID = "100026294557350";
        Log.v("Permissions from goToMainMenu", accessToken.getPermissions().toString());

        GraphRequest mGraphRequest = GraphRequest.newMeRequest(
                accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject me, GraphResponse response) {
                        if (response.getError() != null) {
                            // handle error
                        } else {
                            //String email = me.optString("email");
                            String s = response.toString();
                            Log.v("full response", s);
                            String name = me.optString("name");
                            String id = me.optString("id");
                            Log.v("Login Activity NAME", name);
                            JSONObject obj = null;
                            try {
                                obj = me.getJSONObject("likes");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            list = new ArrayList<String>();
                            JSONArray likeArray = null;
                            try {
                                likeArray = obj.getJSONArray("data");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            for(int i = 0 ; i < likeArray.length() ; i++) {
                                try {
                                    list.add(likeArray.getJSONObject(i).getString("name"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            Iterator<String> iter = list.iterator();
                            while(iter.hasNext()) {
                                Log.v("USER__LIKES", iter.next());
                            }
                            accountName = name;
                            passIntent();

                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,likes");
        mGraphRequest.setParameters(parameters);
        mGraphRequest.executeAsync();
        Log.v("Login Activity", "End Limit");
    }


    public void passIntent() {
        Intent i = new Intent(this, MainMenuActivity.class);
        i.putExtra("accountName", accountName);
        i.putExtra("accountLocation", accountLocation);
        i.putStringArrayListExtra("list", list);
        startActivityForResult(i, ACCESS_CODE);
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

}
