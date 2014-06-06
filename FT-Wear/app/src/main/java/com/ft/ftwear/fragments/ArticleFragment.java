package com.ft.ftwear.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.ft.ftwear.R;
import com.ft.ftwear.models.ArticleModel;
import com.ft.ftwear.utils.PrefsHelper;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by albertoelias on 05/06/2014.
 */
public class ArticleFragment extends Fragment {

    private ArticleModel article;
    private TextView titleText;
    private WebView bodyText;

    PrefsHelper prefsHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        prefsHelper = new PrefsHelper(getActivity());
        Bundle args = getArguments();
        Gson gson = new Gson();
        article = gson.fromJson(args.getString("article"), ArticleModel.class);

        if (article.getBody().isEmpty()) {
            List<ArticleModel> articles = prefsHelper.getArticles();
            for (ArticleModel a : articles) {
                if (article.getTitle().equalsIgnoreCase(a.getTitle())) {
                    article = a;
                    break;
                }
            }
        }

        View view = inflater.inflate(R.layout.fragment_article, container, false);
        titleText = (TextView)view.findViewById(R.id.articleTitle);
        bodyText = (WebView)view.findViewById(R.id.articleBody);

        titleText.setText(article.getTitle());
        bodyText.loadDataWithBaseURL(null, article.getBody(), "text/html", "utf-8", null);
        return view;
    }
}
