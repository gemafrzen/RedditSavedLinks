package org.gemafrzen.redditsavedlinks.db.entities;

/**
 * Created by Erik on 15.07.2017.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Category {

    @PrimaryKey(autoGenerate = true)
    public final long id;
    public String category;
    public String subreddit;


    public Category(long id, String category, String subreddit) {
        this.id = id;
        this.category = category;
        this.subreddit = subreddit;
    }

    public static CategoryBuilder builder(){
        return new CategoryBuilder();
    }

    public static class CategoryBuilder {
        private long id;
        private String category = "";
        private String subreddit = "";

        public CategoryBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public CategoryBuilder setCategory(String category) {
            this.category = category;
            return this;
        }

        public CategoryBuilder setSubreddit(String subreddit) {
            this.subreddit = subreddit;
            return this;
        }

        public Category build() {
            return new Category(id, category, subreddit);
        }
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", category='" + category +
                ", subreddit='" + subreddit +
                '}';
    }
}
