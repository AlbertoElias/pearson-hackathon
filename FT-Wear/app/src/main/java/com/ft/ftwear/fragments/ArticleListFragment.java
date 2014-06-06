package com.ft.ftwear.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.ft.ftwear.R;
import com.ft.ftwear.adapters.ArticlesAdapter;
import com.ft.ftwear.models.ArticleModel;
import com.ft.ftwear.utils.PrefsHelper;
import com.google.gson.Gson;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class ArticleListFragment extends ListFragment {

    public static final String FRAGMENT_TAG = "ARTICLE_LIST";
    List<ArticleModel> articles;
    ArticlesAdapter articlesAdapter;
    Gson gson;
    PrefsHelper prefsHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsHelper = new PrefsHelper(getActivity());
        gson = new Gson();

        articles = prefsHelper.getArticles();
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

    public void updateList() {
        articles = prefsHelper.getArticles();
        articlesAdapter.setList(articles);
        articlesAdapter.notifyDataSetChanged();
    }

}
