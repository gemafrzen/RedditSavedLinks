package org.gemafrzen.redditsavedlinks.db.entities;

/**
 * Created by Erik on 15.07.2017.
 */

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index("subreddit")})
public class Link {

    @PrimaryKey(autoGenerate = true)
    public final long id;
    public String domain;
    public String subreddit;
    public String title;
    public String link;
    public long utc;
    public int numberOfComments;
    public String image;
    public int score;
    public boolean isDeleted;

    public Link(long id, String domain, String subreddit, String title, String link, long utc,
                int numberOfComments, String image, int score, boolean isDeleted) {
        this.id = id;
        this.domain = domain;
        this.subreddit = subreddit;
        this.title = title;
        this.link = link;
        this.utc = utc;
        this.numberOfComments = numberOfComments;
        this.image = image;
        this.score = score;
        this.isDeleted = isDeleted;
    }

    public static LinkBuilder builder(){
        return new LinkBuilder();
    }

    public static class LinkBuilder {
        private long id;
        private String domain = "";
        private String subreddit = "";
        private String title = "";
        private String link = "";
        private long utc = 0;
        private int numberOfComments = 0;
        private String image  = "";
        private int score  = 0;
        private boolean isDeleted = false;

        public LinkBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public LinkBuilder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public LinkBuilder setSubreddit(String subreddit) {
            this.subreddit = subreddit;
            return this;
        }

        public LinkBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public LinkBuilder setLink(String link) {
            this.link = link;
            return this;
        }

        public LinkBuilder setUtc(long utc) {
            this.utc = utc;
            return this;
        }

        public LinkBuilder setNumberOfComments(int numberOfComments) {
            this.numberOfComments = numberOfComments;
            return this;
        }

        public LinkBuilder setImage(String image) {
            this.image = image;
            return this;
        }

        public LinkBuilder setScore(int score) {
            this.score = score;
            return this;
        }

        public LinkBuilder setIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Link build() {
            return new Link(id, domain, subreddit, title, link, utc, numberOfComments, image, score, isDeleted);
        }
    }

    @Override
    public String toString() {
        return "Subreddit{" +
                "id=" + id +
                ", domain='" + domain +
                ", subreddit='" + subreddit +
                ", title='" + title +
                ", link='" + link +
                ", utc='" + utc +
                ", numberOfComments='" + numberOfComments +
                ", image='" + image +
                ", score='" + score +
                ", isDeleted='" + isDeleted +
                '}';
    }
}
