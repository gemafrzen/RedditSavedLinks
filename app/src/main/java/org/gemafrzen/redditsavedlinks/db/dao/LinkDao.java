package org.gemafrzen.redditsavedlinks.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.gemafrzen.redditsavedlinks.db.entities.Link;

import java.util.List;

@Dao
public interface LinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addLink(Link link);

    @Query("select * from link")
    public List<Link> getAllLinks();

    @Query("select * from link where id = :id")
    public List<Link> getLink(long id);

    @Query("select * from link where subreddit = :subreddit")
    public List<Link> getLinkFromSubreddit(String subreddit);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateLink(Link link);

    @Query("delete from link")
    void removeAllLinks();
}