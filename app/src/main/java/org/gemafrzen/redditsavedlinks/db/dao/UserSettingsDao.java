package org.gemafrzen.redditsavedlinks.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.gemafrzen.redditsavedlinks.db.entities.UserSettings;

import java.util.List;

@Dao
public interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addUserSettings(UserSettings userSettings);

    @Query("select * from userSettings")
    public List<UserSettings> getAllUserSettings();

    @Query("select * from userSettings where id = :id")
    public List<UserSettings> getUserSettings(long id);

    @Query("select * from userSettings where isCurrentUser = :isCurrentUser")
    public List<UserSettings> getUserSettings(boolean isCurrentUser);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateUserSettings(UserSettings userSettings);

    @Query("delete from userSettings")
    void removeAllUserSettings();
}