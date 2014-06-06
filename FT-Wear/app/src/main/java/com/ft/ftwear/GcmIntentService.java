package com.ft.ftwear;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.preview.support.wearable.notifications.WearableNotifications;
import android.support.v4.app.NotificationCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.ft.ftwear.activities.MainActivity;
import com.ft.ftwear.models.ArticleModel;
import com.ft.ftwear.utils.PrefsHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * Created by albertoelias on 05/06/2014.
 */
public class GcmIntentService extends IntentService {

    private final static String TAG = "FT Wear";
    private final static String GROUP_KEY_ARTICLES = "group_key_articles";
    public static int NOTIFICATION_ID;
    public static int NOTIFICATION_SUMMARY_ID = 1;
    private NotificationManagerCompat mNotificationManager;

    PrefsHelper prefsHelper;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        prefsHelper = new PrefsHelper(this);
        NOTIFICATION_ID = 2;
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
                sendNotification(article, articlesJSON.length());
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
            ArticleModel article = new ArticleModel(title, summary, image);
            article.setTags(articleJSON.getJSONArray("tags"));
            article.setAuthors(articleJSON.getJSONArray("authors"));
            article.setOrganisations(articleJSON.getJSONArray("organisations"));
            article.setTopics(articleJSON.getJSONArray("topics"));
            article.setSections(articleJSON.getJSONArray("sections"));

            return article;
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendNotification(ArticleModel article, int articles) {
        mNotificationManager = NotificationManagerCompat.from(this.getApplicationContext());

        if (NOTIFICATION_ID > 2) {
            Intent summaryIntent = new Intent(this, MainActivity.class);
            PendingIntent listIntent = PendingIntent.getActivity(this, 0,
                    summaryIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder mSummaryBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_activity)
                    .setContentText("You have "+String.valueOf(articles)+" new articles")
                    .setContentIntent(listIntent);

            Notification summaryNotif = new WearableNotifications.Builder(mSummaryBuilder)
                    .setGroup(GROUP_KEY_ARTICLES, WearableNotifications.GROUP_ORDER_SUMMARY)
                    .build();

            mNotificationManager.notify(NOTIFICATION_SUMMARY_ID, summaryNotif);
        }

        Intent intent = new Intent(this, MainActivity.class);
        Gson gson = new Gson();
        String json = gson.toJson(article, ArticleModel.class);
        intent.putExtra("article", json);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)

                .setContentTitle(article.getTitle())
                .setSmallIcon(R.drawable.ic_activity)
                .setLargeIcon(getBitmap(article.getImage()))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(article.getSummary()))
                .setContentText(article.getSummary())
                .addAction(R.drawable.ic_activity, "Open", contentIntent);

        Notification secondPage = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .setBigContentTitle(getResources().getString(R.string.authors))
                    .bigText(article.getAuthors()))
                    .build();

        Notification thirdPage = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(getResources().getString(R.string.tags))
                        .bigText(article.getTags()))
                .build();

        Notification fourthPage = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(getResources().getString(R.string.organisations))
                        .bigText(article.getOrganisations()))
                .build();

        Notification fifthPage = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(getResources().getString(R.string.topics))
                        .bigText(article.getTopics()))
                .build();

        Notification sixthPage = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(getResources().getString(R.string.sections))
                        .bigText(article.getSections()))
                .build();

        WearableNotifications.Builder wBuilder = new WearableNotifications.Builder(mBuilder)
                        .addPage(secondPage)
                        .addPage(thirdPage)
                        .addPage(fourthPage)
                        .addPage(fifthPage)
                        .addPage(sixthPage);

        if (articles > 1) {
            wBuilder.setGroup(GROUP_KEY_ARTICLES);
        }

        mNotificationManager.notify(NOTIFICATION_ID, wBuilder.build());
        NOTIFICATION_ID++;
    }

    private Bitmap getBitmap(String src) {
        try {
            URL url = new URL(src);
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
