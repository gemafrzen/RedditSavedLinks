package org.gemafrzen.redditsavedlinks.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.gemafrzen.redditsavedlinks.db.entities.Subreddit;

import java.util.List;

@Dao
public interface SubredditDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addSubreddit(Subreddit subreddit);

    @Query("select * from subreddit")
    public List<Subreddit> getAllSubreddits();

    @Query("select * from subreddit order by subredditname")
    public List<Subreddit> getAllSubredditsSorted();

    @Query("select * from subreddit where subredditname = :subreddit")
    public List<Subreddit> getSubredditWithName(String subreddit);

    @Query("delete from subreddit")
    void removeAllSubreddits();
}