package com.ft.ftwear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.ft.ftwear.ApplicationController;
import com.ft.ftwear.models.ArticleModel;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by albertoelias on 05/06/2014.
 */
public class PrefsHelper {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_TWITTER_KEY = "consumer_key";
    private static final String PROPERTY_TWITTER_SECRET = "consumer_secret_key";
    private final static String TAG = "FT Wear";

    SharedPreferences prefs;
    Context context;
    Gson gson;

    public PrefsHelper(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(ApplicationController.PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void storeArticles(JSONArray content) {
        try {
            SharedPreferences.Editor prefsEditor = prefs.edit();
            int length = content.length();
            Set<String> articles = prefs.getStringSet("articles", null);
            ArrayList<String> articlesList = new ArrayList<String>();

            for (int i=0;i<length;i++) {
                JSONObject articleJSON = content.getJSONObject(i);
                String title = articleJSON.getString("title");
                String summary = articleJSON.getString("summary");
                String body = articleJSON.getString("body");
                String image = articleJSON.getString("image");

                ArticleModel article = new ArticleModel(title, summary, image, body);
                article.setTags(articleJSON.getJSONArray("tags"));
                article.setAuthors(articleJSON.getJSONArray("authors"));
                article.setOrganisations(articleJSON.getJSONArray("organisations"));
                article.setTopics(articleJSON.getJSONArray("topics"));
                article.setSections(articleJSON.getJSONArray("sections"));

                String json = gson.toJson(article, ArticleModel.class);
                articlesList.add(json);
            }

            if (articles != null) {
                articlesList.addAll(articles);

                int l = articlesList.size() - articles.size();
                for (int i=0;i<l;i++) {
                    articlesList.remove(articlesList.size()-1);
                }
            }

            articles = new HashSet<String>();
            articles.addAll(articlesList);
            prefsEditor.putStringSet("articles", articles);
            prefsEditor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public List<ArticleModel> getArticles() {
        List<ArticleModel>  articles = new ArrayList<ArticleModel>();
        ArrayList<String> jsonArray = new ArrayList<String>();
        jsonArray.addAll(prefs.getStringSet("articles", null));
        for (String json : jsonArray) {
            ArticleModel article = gson.fromJson(json, ArticleModel.class);
            articles.add(article);
        }

        return articles;
    }

    public String getRegistrationId() {
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

   public void storeRegistrationId(String regId) {
        int appVersion = getAppVersion();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public void storeTwitterCredentials(String key, String secret) {
        SharedPreferences.Editor editor = prefs.edit();
        if (getTwitterConsumerKey().isEmpty()) {
            editor.putString(PROPERTY_TWITTER_KEY, key);
        }
        if (getTwitterSecretKey().isEmpty()) {
            editor.putString(PROPERTY_TWITTER_SECRET, secret);
        }
        editor.commit();
    }

    public String getTwitterConsumerKey() {
        return prefs.getString(PROPERTY_TWITTER_KEY, "");
    }

    public String getTwitterSecretKey() {
        return prefs.getString(PROPERTY_TWITTER_SECRET, "");
    }
}
