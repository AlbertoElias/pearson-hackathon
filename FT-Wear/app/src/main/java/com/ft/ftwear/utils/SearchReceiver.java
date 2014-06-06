package com.ft.ftwear.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ft.ftwear.ApplicationController;
import com.ft.ftwear.R;
import com.ft.ftwear.fragments.ArticleListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arjun on 06/06/2014.
 */
public class SearchReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        JsonArrayRequest req = new JsonArrayRequest(ApplicationController.Url + "/search/" + intent.getStringExtra("search_reply"),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {

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
    }
}
