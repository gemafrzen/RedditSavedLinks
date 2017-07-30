package org.gemafrzen.redditsavedlinks.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.gemafrzen.redditsavedlinks.db.entities.RedditLink;

import java.util.List;

@Dao
public interface RedditLinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addLink(RedditLink redditLink);

    @Query("select * from redditlink")
    public List<RedditLink> getAllRedditLinks();

    @Query("select * from redditlink where id = :id")
    public List<RedditLink> getRedditLink(long id);

    @Query("select * from redditlink where link = :link")
    public List<RedditLink> getRedditLink(String link);

    @Query("select * from redditlink where subreddit = :subreddit")
    public List<RedditLink> getRedditLinkFromSubreddit(String subreddit);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateLink(RedditLink redditLink);

    @Query("delete from redditlink")
    void removeAllLinks();

    @Delete
    void deleteLink(RedditLink redditLink);
}