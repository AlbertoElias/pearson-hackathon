package com.ft.ftwear.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ft.ftwear.ApplicationController;

/**
 * Created by arjun on 06/06/2014.
 */
public class SearchReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        StringRequest req = new StringRequest(
                ApplicationController.Url + "/search/" + intent.getStringExtra("search_reply"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        }
        );

        ApplicationController.getInstance().addToRequestQueue(req);
    }
}
