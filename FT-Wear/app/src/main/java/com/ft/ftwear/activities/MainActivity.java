package com.ft.ftwear.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.ft.ftwear.fragments.ArticleFragment;
import com.ft.ftwear.fragments.ArticleListFragment;
import com.ft.ftwear.utils.PrefsHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;


public class MainActivity extends Activity {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "363005271366";
    private final static String TAG = "FT Wear";

    GoogleCloudMessaging gcm;
    String regid;
    SharedPreferences prefs;
    PrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        prefsHelper = new PrefsHelper(this);
        prefsHelper.storeTwitterCredentials(ApplicationController.CONSUMER_KEY,
                ApplicationController.CONSUMER_SECRET_KEY);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            ArticleFragment articleFragment = new ArticleFragment();
            articleFragment.setArguments(intent.getExtras());
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, articleFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            if (checkPlayServices()) {
                gcm = GoogleCloudMessaging.getInstance(this);
                regid = prefsHelper.getRegistrationId();

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
                                    try {
                                        JSONArray content = response.getJSONArray("content");
                                        prefsHelper.storeArticles(content);
                                        new Handler().post(new Runnable() {
                                            public void run() {
                                                prefsHelper.storeRegistrationId(regid);
                                                getFragmentManager().beginTransaction()
                                                        .add(R.id.container, new ArticleListFragment())
                                                        .commitAllowingStateLoss();
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

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


}
