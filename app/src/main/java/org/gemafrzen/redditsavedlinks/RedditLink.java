package org.gemafrzen.redditsavedlinks;

/**
 * Created by Erik on 05.07.2017.
 */

public class RedditLink {
    private String title; //title or body (if comment)
    private String subreddit; //subreddit_name_prefixed
    private String domain; //e.g.: 'self.Fitness'
    private int numberOfComments; //num_comments
    private int created_utc; //in unix time
    private int score;


    public RedditLink() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public int getCreated_utc() {
        return created_utc;
    }

    public void setCreated_utc(int created_utc) {
        this.created_utc = created_utc;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}