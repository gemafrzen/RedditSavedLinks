package org.gemafrzen.redditsavedlinks.db;

import android.content.Context;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import org.gemafrzen.redditsavedlinks.db.dao.CategoryDao;
import org.gemafrzen.redditsavedlinks.db.dao.RedditLinkDao;
import org.gemafrzen.redditsavedlinks.db.dao.SubredditDao;
import org.gemafrzen.redditsavedlinks.db.dao.UserSettingsDao;
import org.gemafrzen.redditsavedlinks.db.entities.Category;
import org.gemafrzen.redditsavedlinks.db.entities.RedditLink;
import org.gemafrzen.redditsavedlinks.db.entities.Subreddit;
import org.gemafrzen.redditsavedlinks.db.entities.UserSettings;

@Database(entities = {Category.class, RedditLink.class, Subreddit.class, UserSettings.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;
    private static AppDatabase INSTANCE_ON_UI_THREAD;

    public abstract SubredditDao SubredditModel();
    public abstract UserSettingsDao UserSettingsModel();
    public abstract CategoryDao CategoryModel();
    public abstract RedditLinkDao LinkModel();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "redditSavedLinks-database")
                    .build();
        }
        return INSTANCE;
    }
    public static AppDatabase getDatabaseOnUIThread(Context context) {
        if (INSTANCE_ON_UI_THREAD == null) {
            INSTANCE_ON_UI_THREAD = Room.databaseBuilder(context, AppDatabase.class, "redditSavedLinks-database")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE_ON_UI_THREAD;
    }

    public static void destroyInstance() {
        INSTANCE = null;
        INSTANCE_ON_UI_THREAD = null;
    }
}