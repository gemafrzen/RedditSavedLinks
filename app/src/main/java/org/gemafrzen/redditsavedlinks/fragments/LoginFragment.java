package org.gemafrzen.redditsavedlinks.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.gemafrzen.redditsavedlinks.MainActivity;
import org.gemafrzen.redditsavedlinks.R;
import org.gemafrzen.redditsavedlinks.db.AppDatabase;
import org.gemafrzen.redditsavedlinks.db.entities.UserSettings;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    private String TAG = LoginFragment.class.getSimpleName();

    private static final String ACCESS_TOKEN_URL =
            "https://www.reddit.com/api/v1/access_token";

    private static final String AUTH_URL =
            "https://www.reddit.com/api/v1/authorize.compact?client_id=%s" +
                    "&response_type=code&state=%s&redirect_uri=%s&" +
                    "duration=temporary&scope=history,identity";

    private static final String CLIENT_ID = "l2wLkGX9_udUbg";

    private static final String REDIRECT_URI =
            "redditsavedlinks://org/gemafrzen/uri";

    public static final String STATE = "GEMAFRZEN_REDDIT_SAVED_LINKS";

    private String accessToken;
    private String refreshToken;
    private String username;

    private OnFragmentInteractionListener mListener;

    public LoginFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SavedLinkListFragment.
     */
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessToken = "";
        refreshToken = "";
        username = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button button = (Button) view.findViewById(R.id.btn_login);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startSignIn(v);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * open a browser for reddit oAuth
     * @param view
     */
    public void startSignIn(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI))));
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getActivity().getIntent();

        if(intent != null &&
                intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = intent.getData();
            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Log.e(TAG, "An error has occurred : " + error);
            } else {
                String state = uri.getQueryParameter("state");
                if(state.equals(STATE)) {
                    String code = uri.getQueryParameter("code");
                    getAccessToken(code);
                }
            }
        }
    }

    /**
     * request access token from reddit
     * @param code
     */
    private void getAccessToken(String code) {
        OkHttpClient client = new OkHttpClient();

        String authString = CLIENT_ID + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + REDIRECT_URI))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                try {
                    JSONObject data = new JSONObject(json);
                    accessToken = data.optString("access_token");
                    refreshToken = data.optString("refresh_token");

                    getUsername(accessToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getUsername(String accessToken) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .addHeader("Authorization", "bearer " + accessToken)
                .url("https://oauth.reddit.com/api/v1/me")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                try {
                    JSONObject data = new JSONObject(json);
                    username = data.optString("name");
                    Log.e(TAG, "getusername: " + username);

                    saveUserSettingsInDB();
                    openSavedLinkListFragment();
                } catch (JSONException e) {
                    Log.e(TAG, "json response username: " + json);
                    e.printStackTrace();
                }
            }
        });
    }



    /**
     * send a message to the activity to open the SavedLinkListFragment
     */
    public void openSavedLinkListFragment(){
        Uri builtUri = Uri.parse("login")
                .buildUpon()
                .appendQueryParameter(MainActivity.ACCESS_TOKEN, accessToken)
                .appendQueryParameter(MainActivity.REFRESH_TOKEN, refreshToken)
                .appendQueryParameter(MainActivity.USERNAME, username)
                .build();
        mListener.onFragmentInteraction(builtUri);
    }

    /**
     * saves permanent tokens in db for continued access
     */
    private void saveUserSettingsInDB(){
        AppDatabase database = AppDatabase.getDatabase(getContext());

        database.UserSettingsModel().addUserSettings(UserSettings.builder()
                                                    .setUsername(username)
                                                    .setisCurrentUser(true)
                                                    .setAccesstoken(accessToken)
                                                    .setRefreshtoken(refreshToken)
                                                    .build());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
