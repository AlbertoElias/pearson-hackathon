package com.ft.ftwear.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ft.ftwear.ApplicationController;
import com.ft.ftwear.R;
import com.ft.ftwear.fragments.ArticleListFragment;
import com.ft.ftwear.models.ArticleModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends Activity {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "363005271366";
    private final static String TAG = "FT Wear";

    GoogleCloudMessaging gcm;
    String regid;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(ApplicationController.PREFS_NAME, Context.MODE_PRIVATE);
        if (savedInstanceState == null) {
            if (checkPlayServices()) {
                gcm = GoogleCloudMessaging.getInstance(this);
                regid = getRegistrationId(this);

                if (regid.isEmpty()) {
                    sendRegistrationIdToBackend(this);
                } else {
                    getFragmentManager().beginTransaction()
                            .add(R.id.container, new ArticleListFragment())
                            .commit();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void sendRegistrationIdToBackend(final Context context) {
        new AsyncTask<Void,Void,Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Boolean success = true;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                } catch (IOException ex) {
                    success = false;
                    ex.printStackTrace();
                }
                return success;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("registrationID", regid);

                    JsonObjectRequest req = new JsonObjectRequest(ApplicationController.Url + "/register", new JSONObject(params),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    storeArticles(response);
                                    new Handler().post(new Runnable() {
                                        public void run() {
                                            storeRegistrationId(context, regid);
                                            getFragmentManager().beginTransaction()
                                                    .add(R.id.container, new ArticleListFragment())
                                                    .commitAllowingStateLoss();
                                        }
                                    });
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.e("Error: ", error.getMessage());
                        }
                    }
                    );

                    ApplicationController.getInstance().addToRequestQueue(req);
                } else {
                    sendRegistrationIdToBackend(context);
                }
            }
        }.execute();


    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void storeArticles(JSONObject res) {
        try {
            SharedPreferences.Editor prefsEditor = prefs.edit();
            Gson gson = new Gson();

            JSONArray content = res.getJSONArray("content");
            Set<String> articles = new HashSet<String>();
            for (int i=0;i<content.length();i++) {
                JSONObject articleJSON = content.getJSONObject(i);
                String title = articleJSON.getString("title");
                String summary = articleJSON.getString("summary");
                String body = articleJSON.getString("body");
                String image = articleJSON.getString("image");

                ArticleModel article = new ArticleModel(title, summary, body, image);
                String json = gson.toJson(article, ArticleModel.class);
                articles.add(json);
            }
            prefsEditor.putStringSet("articles", articles);
            prefsEditor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
