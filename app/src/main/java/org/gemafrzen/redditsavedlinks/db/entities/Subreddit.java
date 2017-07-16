package org.gemafrzen.redditsavedlinks.db.entities;

/**
 * Created by Erik on 15.07.2017.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Subreddit {

    @PrimaryKey
    public String subredditname;


    public Subreddit(String subredditname) {
        this.subredditname = subredditname;
    }

    public static SubredditBuilder builder(){
        return new SubredditBuilder();
    }

    public static class SubredditBuilder {
        private String subredditname = "";

        public SubredditBuilder setSubredditname(String subredditname) {
            this.subredditname = subredditname;
            return this;
        }

        public Subreddit build() {
            return new Subreddit(subredditname);
        }
    }

    @Override
    public String toString() {
        return "Subreddit{" +
                "subredditname='" + subredditname +
                '}';
    }
}
