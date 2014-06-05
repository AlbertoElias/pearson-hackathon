package com.ft.ftwear.models;

/**
 * Created by albertoelias on 05/06/2014.
 */
public class ArticleModel {

    private String title;
    private String summary;
    private String body;
    private String image;

    public ArticleModel(String title, String summary, String image) {
        this(title, summary, image, "");
    }
    public ArticleModel(String title, String summary, String image, String body) {
        this.title = title;
        this.summary = summary;
        this.body = body;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
