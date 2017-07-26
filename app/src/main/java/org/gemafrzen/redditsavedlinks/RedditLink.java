package org.gemafrzen.redditsavedlinks;

/**
 * Created by Erik on 05.07.2017.
 */

public class RedditLink {
    private String title; //title or body (if comment)
    private String subreddit; //subreddit_name_prefixed
    private String domain; //e.g.: 'self.Fitness'
    private String url;
    private int numberOfComments; //num_comments
    private long created_utc; //in unix time
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public long getCreated_utc() {
        return created_utc;
    }

    public void setCreated_utc(long created_utc) {
        this.created_utc = created_utc;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
