package org.gemafrzen.redditsavedlinks.db.entities;

/**
 * Created by Erik on 15.07.2017.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index("subreddit")})
public class RedditLink {

    @PrimaryKey(autoGenerate = true)
    private final long id;
    private String domain;
    private String subreddit;
    private String title;
    private String url;
    private String permaLink; // in reddit
    private String fullname;
    private String selftext;
    private long utc;
    private int numberOfComments;
    private String image;
    private int score;
    private boolean isDeleted;
    private boolean isSaved;
    private boolean isNsfw;

    public RedditLink(long id, String domain, String subreddit, String title, String url,
                      String permaLink, String fullname, String selftext, long utc,
                      int numberOfComments, String image, int score, boolean isDeleted,
                      boolean isSaved, boolean isNsfw) {
        this.id = id;
        this.setDomain(domain);
        this.setSubreddit(subreddit);
        this.setTitle(title);
        this.setUrl(url);
        this.setPermaLink(permaLink);
        this.setFullname(fullname);
        this.setSelftext(selftext);
        this.setUtc(utc);
        this.setNumberOfComments(numberOfComments);
        this.setImage(image);
        this.setScore(score);
        this.setDeleted(isDeleted);
        this.setSaved(isSaved);
        this.setNsfw(isNsfw);
    }

    public static RedditLinkBuilder builder(){
        return new RedditLinkBuilder();
    }

    public long getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPermaLink() {
        return permaLink;
    }

    public void setPermaLink(String permaLink) {
        this.permaLink = permaLink;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getSelftext() {
        return selftext;
    }

    public void setSelftext(String selftext) {
        this.selftext = selftext;
    }

    public long getUtc() {
        return utc;
    }

    public void setUtc(long utc) {
        this.utc = utc;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public boolean isNsfw() {
        return this.isNsfw;
    }

    public void setNsfw(boolean isNsfw) {
        this.isNsfw = isNsfw;
    }

    /**
     * Builder class for this class
     */
    public static class RedditLinkBuilder {
        private long id;
        private String domain = "";
        private String subreddit = "";
        private String title = "";
        private String url = "";
        private String permaLink = "";
        private String fullname = "";
        private String selftext = "";
        private long utc = 0;
        private int numberOfComments = 0;
        private String image  = "";
        private int score  = 0;
        private boolean isDeleted = false;
        private boolean isSaved = true;
        private boolean isNsfw = false;

        public RedditLinkBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public RedditLinkBuilder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public RedditLinkBuilder setSubreddit(String subreddit) {
            this.subreddit = subreddit;
            return this;
        }

        public RedditLinkBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public RedditLinkBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public RedditLinkBuilder setPermaLink(String permaLink) {
            this.permaLink = permaLink;
            return this;
        }

        public RedditLinkBuilder setFullname(String fullname) {
            this.fullname = fullname;
            return this;
        }

        public RedditLinkBuilder setSelftext(String selftext) {
            this.selftext = selftext;
            return this;
        }

        public RedditLinkBuilder setUtc(long utc) {
            this.utc = utc;
            return this;
        }

        public RedditLinkBuilder setNumberOfComments(int numberOfComments) {
            this.numberOfComments = numberOfComments;
            return this;
        }

        public RedditLinkBuilder setImage(String image) {
            this.image = image;
            return this;
        }

        public RedditLinkBuilder setScore(int score) {
            this.score = score;
            return this;
        }

        public RedditLinkBuilder setIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public RedditLinkBuilder setIsSaved(boolean isSaved) {
            this.isSaved = isSaved;
            return this;
        }

        public RedditLinkBuilder setNsfw(boolean isNsfw) {
            this.isNsfw = isNsfw;
            return this;
        }

        public RedditLink build() {
            return new RedditLink(id, domain, subreddit, title, url, permaLink, fullname, selftext,
                                  utc, numberOfComments, image, score, isDeleted, isSaved, isNsfw);
        }
    }

    @Override
    public String toString() {
        return "Redditlink{" +
                "id=" + getId() +
                ", domain=" + getDomain() +
                ", subreddit=" + getSubreddit() +
                ", title='" + getTitle() +
                "', url=" + getUrl() +
                ", fullname=" + getFullname() +
                ", utc=" + getUtc() +
                ", numberOfComments=" + getNumberOfComments() +
                ", image=" + getImage() +
                ", score=" + getScore() +
                ", isDeleted=" + isDeleted() +
                ", isSaved=" + isSaved() +
                ", isNsfw=" + isNsfw() +
                '}';
    }
}
