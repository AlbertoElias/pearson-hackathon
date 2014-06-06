package com.ft.ftwear.models;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by albertoelias on 05/06/2014.
 */
public class ArticleModel {

    private String title;
    private String summary;
    private String body;
    private String image;
    private ArrayList<String> tags;
    private ArrayList<String> authors;
    private ArrayList<String> organisations;
    private ArrayList<String> topics;
    private ArrayList<String> sections;

    public ArticleModel(String title, String summary, String image) {
        this(title, summary, image, "");
    }

    public ArticleModel(String title, String summary, String image, String body) {
        this.title = title;
        this.summary = summary;
        this.body = body;
        this.image = image;
        this.tags = new ArrayList<String>();
        this.authors = new ArrayList<String>();
        this.organisations = new ArrayList<String>();
        this.topics = new ArrayList<String>();
        this.sections = new ArrayList<String>();
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

    public String getTags() {
        return tags.toString();
    }

    public void setTags(JSONArray tags) {
        for (int i=0;i<tags.length();i++) {
            try {
                this.tags.add(tags.getString(i));
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAuthors() {
        return authors.toString();
    }

    public void setAuthors(JSONArray authors) {
        for (int i=0;i<authors.length();i++) {
            try {
                this.authors.add(authors.getString(i));
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getOrganisations() {
        return organisations.toString();
    }

    public void setOrganisations(JSONArray organisations) {
        for (int i=0;i<organisations.length();i++) {
            try {
                this.organisations.add(organisations.getString(i));
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getTopics() {
        return topics.toString();
    }

    public void setTopics(JSONArray topics) {
        for (int i=0;i<topics.length();i++) {
            try {
                this.topics.add(topics.getString(i));
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSections() {
        return sections.toString();
    }

    public void setSections(JSONArray sections) {
        for (int i=0;i<sections.length();i++) {
            try {
                this.sections.add(sections.getString(i));
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
