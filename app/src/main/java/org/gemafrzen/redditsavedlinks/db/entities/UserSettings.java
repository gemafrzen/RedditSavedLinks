package org.gemafrzen.redditsavedlinks.db.entities;

/**
 * Created by Erik on 15.07.2017.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class UserSettings {

    @PrimaryKey(autoGenerate = true)
    public final long id;
    public String username;
    public String accesstoken;
    public String refreshtoken;
    public boolean isCurrentUser;


    public UserSettings(long id, String username, String accesstoken, String refreshtoken, boolean isCurrentUser) {
        this.id = id;
        this.username = username;
        this.accesstoken = accesstoken;
        this.refreshtoken = refreshtoken;
        this.isCurrentUser = isCurrentUser;
    }

    public static UserSettingsBuilder builder(){
        return new UserSettingsBuilder();
    }

    public static class UserSettingsBuilder {
        private long id;
        private String username = "";
        private String accesstoken = "";
        private String refreshtoken = "";
        private boolean isCurrentUser = false;

        public UserSettingsBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public UserSettingsBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public UserSettingsBuilder setAccesstoken(String accesstoken) {
            this.accesstoken = accesstoken;
            return this;
        }

        public UserSettingsBuilder setRefreshtoken(String refreshtoken) {
            this.refreshtoken = refreshtoken;
            return this;
        }

        public UserSettingsBuilder setisCurrentUser(boolean isCurrentUser) {
            this.isCurrentUser = isCurrentUser;
            return this;
        }

        public UserSettings build() {
            return new UserSettings(id, username, accesstoken, refreshtoken, isCurrentUser);
        }
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "id=" + id +
                ", username='" + username +
                ", accesstoken='" + accesstoken +
                ", refreshtoken='" + refreshtoken +
                ", isCurrentUser='" + isCurrentUser +
                '}';
    }
}
