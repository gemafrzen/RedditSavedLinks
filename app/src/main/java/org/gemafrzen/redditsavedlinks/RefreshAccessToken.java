package org.gemafrzen.redditsavedlinks;

import android.content.Context;
import android.util.Base64;

import org.gemafrzen.redditsavedlinks.db.AppDatabase;
import org.gemafrzen.redditsavedlinks.db.entities.UserSettings;
import org.gemafrzen.redditsavedlinks.exceptions.NoCurrentUserFoundException;
import org.gemafrzen.redditsavedlinks.exceptions.NoRefreshOfTokenException;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created on 05.08.2017.
 */

public class RefreshAccessToken {
    private String TAG = RefreshAccessToken.class.getSimpleName();

    /**
     * refresh accesstoken if necessary (it expired)
     * @param context
     * @throws NoCurrentUserFoundException
     * @throws NoRefreshOfTokenException
     */
    public void refresh(Context context)
            throws NoCurrentUserFoundException, NoRefreshOfTokenException{

        AppDatabase database = AppDatabase.getDatabase(context);

        List<UserSettings> userSettingsList = database.UserSettingsModel().getUserSettings(true);

        if(userSettingsList == null || userSettingsList.isEmpty())
            throw new NoCurrentUserFoundException();
        else{
            UserSettings currentUserSettings = userSettingsList.get(0);

            if(currentUserSettings.accesstokenExpiresIn <=
                    Calendar.getInstance().getTimeInMillis() + 2000){
                getNewAccesstoken(context, currentUserSettings);
            }
        }
    }

    /**
     * requests new access token
     * @param context
     * @param userSettings
     * @throws NoRefreshOfTokenException
     */
    private void getNewAccesstoken(Context context, UserSettings userSettings)
            throws NoRefreshOfTokenException{

        OkHttpClient client = new OkHttpClient();
        String CLIENT_ID = "l2wLkGX9_udUbg"; //TODO refactor client id
        String errorMessage = "";

        String authString = CLIENT_ID + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url("https://www.reddit.com/api/v1/access_token")
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=refresh_token&refresh_token=" + userSettings.refreshtoken))
                .build();

        try(Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful())
                errorMessage = "General I/O response exception: " + response.code();
            else
                errorMessage = readJsonResponse(context, response.body().string(), userSettings);

        }catch(IOException e){
            errorMessage = "General I/O exception: " + e.getMessage();
        }

        if(!errorMessage.isEmpty())
            throw new NoRefreshOfTokenException(errorMessage);
    }


    /**
     * Reads new accesstoken and expired_in from the json string
     * @param context
     * @param response
     * @param userSettings
     * @return error message
     */
    private String readJsonResponse(Context context, String response, UserSettings userSettings){
        String errorMessage = "";

        try {
            JSONObject jsonObj = new JSONObject(response);

            if(jsonObj.has("error")){
                errorMessage = "Error: " + jsonObj.getString("error");

                if (jsonObj.has("message"))
                    errorMessage += "-" + jsonObj.getString("message");
            }else{
                if(jsonObj.has("access_token")) {
                    userSettings.accesstoken = jsonObj.getString("access_token");

                    if (jsonObj.has("expires_in")){
                        userSettings.accesstokenExpiresIn = Calendar.getInstance().getTimeInMillis()
                                                            + (jsonObj.getLong("expires_in") * 1000);
                    }

                    AppDatabase database = AppDatabase.getDatabase(context);
                    database.UserSettingsModel().updateUserSettings(userSettings);
                }
            }
        }catch(JSONException e){
            errorMessage = "General JSON exception: " + e.getMessage();
        }

        return errorMessage;
    }
}
