package org.gemafrzen.redditsavedlinks.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.gemafrzen.redditsavedlinks.db.entities.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addCategory(Category category);

    @Query("select * from category")
    public List<Category> getAllCategories();

    @Query("select * from category where id = :id")
    public List<Category> getCategory(long id);

    @Query("select * from category where category = :category")
    public List<Category> getCategoryWithName(String category);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCategory(Category category);

    @Query("delete from category")
    void removeAllCategories();
}