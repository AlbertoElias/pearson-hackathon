package com.ft.ftwear.fragments;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.ft.ftwear.ApplicationController;
import com.ft.ftwear.R;
import com.ft.ftwear.adapters.ArticlesAdapter;
import com.ft.ftwear.models.ArticleModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class ArticleListFragment extends ListFragment {

    SharedPreferences prefs;
    List<ArticleModel> articles;
    ArticlesAdapter articlesAdapter;
    Gson gson;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getActivity().getSharedPreferences(ApplicationController.PREFS_NAME,
                                                    Context.MODE_PRIVATE);

        articles = new ArrayList<ArticleModel>();
        gson = new Gson();
        ArrayList<String> jsonArray = new ArrayList<String>();
        jsonArray.addAll(prefs.getStringSet("articles", null));
        for (String json : jsonArray) {
            ArticleModel article = gson.fromJson(json, ArticleModel.class);
            articles.add(article);
        }
        articlesAdapter = new ArticlesAdapter(getActivity(), articles);
        setListAdapter(articlesAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Bundle bundle = new Bundle();
        String json = gson.toJson(articles.get(position), ArticleModel.class);
        bundle.putString("article", json);
        ArticleFragment articleFragment = new ArticleFragment();
        articleFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, articleFragment)
                .addToBackStack(null)
                .commit();

    }

}
