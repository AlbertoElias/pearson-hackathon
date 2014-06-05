package com.ft.ftwear;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.ft.ftwear.activities.MainActivity;
import com.ft.ftwear.models.ArticleModel;
import com.ft.ftwear.utils.PrefsHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by albertoelias on 05/06/2014.
 */
public class GcmIntentService extends IntentService {

    private final static String TAG = "FT Wear";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    PrefsHelper prefsHelper;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        prefsHelper = new PrefsHelper(this);
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        String json = intent.getStringExtra("data");

        if (!json.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                parseResult(json);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                parseResult(json);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                parseResult(json);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void parseResult(String msg) {
        try {
            JSONArray articlesJSON = new JSONArray(msg);
            for(int i=0;i<articlesJSON.length();i++) {
                ArticleModel article = getArticle(articlesJSON.getJSONObject(i));
                sendNotification(article);
            }
            getNewArticles();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private ArticleModel getArticle(JSONObject articleJSON) {
        try {
            String title = articleJSON.getString("title");
            String summary = articleJSON.getString("summary");
            String image = articleJSON.getString("image");
            return new ArticleModel(title, summary, image);
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendNotification(ArticleModel article) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(article.getTitle())
                .setSmallIcon(R.drawable.ic_activity)
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(article.getSummary()))
                .setContentText(article.getSummary());

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void getNewArticles() {
        JsonArrayRequest req = new JsonArrayRequest(ApplicationController.Url+"/latestNews",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        prefsHelper.storeArticles(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        ApplicationController.getInstance().addToRequestQueue(req);
    }
}
