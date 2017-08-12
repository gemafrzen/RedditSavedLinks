package org.gemafrzen.redditsavedlinks.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.gemafrzen.redditsavedlinks.R;
import org.gemafrzen.redditsavedlinks.RedditLinkAdapter;
import org.gemafrzen.redditsavedlinks.RefreshAccessToken;
import org.gemafrzen.redditsavedlinks.db.AppDatabase;
import org.gemafrzen.redditsavedlinks.db.entities.RedditLink;
import org.gemafrzen.redditsavedlinks.db.entities.Subreddit;
import org.gemafrzen.redditsavedlinks.exceptions.NoCurrentUserFoundException;
import org.gemafrzen.redditsavedlinks.exceptions.NoRefreshOfTokenException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SavedLinkListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SavedLinkListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavedLinkListFragment extends Fragment {
    private String TAG = SavedLinkListFragment.class.getSimpleName();

    private String mAccesstoken;
    private String mRefreshToken;
    private String mUsername;
    private String mFilterSubreddit;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RedditLinkAdapter adapter;
    private List<RedditLink> redditLinkList;

    private boolean reloadIsRunning = false;

    private OnFragmentInteractionListener mListener;

    public SavedLinkListFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SavedLinkListFragment.
     */
    public static SavedLinkListFragment newInstance(String accesstoken, String refreshToken, String username) {
        SavedLinkListFragment fragment = new SavedLinkListFragment();

        Bundle args = new Bundle();
        args.putString("mAccesstoken", accesstoken);
        args.putString("mRefreshToken", refreshToken);
        args.putString("mUsername", username);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        redditLinkList = new ArrayList<>();
        adapter = new RedditLinkAdapter(this.getContext(), redditLinkList);

        if(savedInstanceState != null){
            mAccesstoken = savedInstanceState.getString("mAccesstoken");
            mRefreshToken = savedInstanceState.getString("mRefreshToken");
            mUsername = savedInstanceState.getString("mUsername");
            mFilterSubreddit = savedInstanceState.getString("mFilterSubreddit");
        }else{
            Bundle bundle = getArguments();
            if(bundle != null){
                mAccesstoken = bundle.getString("mAccesstoken");
                mRefreshToken = bundle.getString("mRefreshToken");
                mUsername = bundle.getString("mUsername");
            }

            new FillRedditLinks().execute();
        }
    }


    private class FillRedditLinks extends AsyncTask<Void, Void, Void> {

        List<RedditLink> tmpList;

        public FillRedditLinks() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tmpList = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            redditLinkList.clear();
            redditLinkList.addAll(tmpList);
            adapter.notifyDataSetChanged();
        }

        protected Void doInBackground(Void... arg0) {
            AppDatabase database = AppDatabase.getDatabase(getContext());
            tmpList.addAll(database.LinkModel().getAllRedditLinks());

            return null;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_link_list, container, false);

        recyclerView  = (RecyclerView) view.findViewById(R.id.rv);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startGettingLinks();
            }
        });

        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("mAccesstoken", mAccesstoken);
        outState.putString("mRefreshToken", mRefreshToken);
        outState.putString("mUsername", mUsername);
        outState.putString("mFilterSubreddit", mFilterSubreddit);
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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    /**
     * Filters the links - shows only those which are in the specific subreddit
     * @param filter
     */
    public void setFilter(String filter){
        mFilterSubreddit = filter;
        adapter.getFilter().filter(filter);
    }


    public void setTokensAndUsername(String accesstoken, String refreshToken, String username){
        mAccesstoken = accesstoken;
        mRefreshToken = refreshToken;
        mUsername = username;
    }


    private void startGettingLinks(){
        // TODO remove and implement adding newer than last
        if(!reloadIsRunning) {
            redditLinkList.clear();
            reloadIsRunning = true;
            new LoadRedditSavedLinks().execute();
        }
    }

    private void finishGettingLinks(String errorMessage){
        reloadIsRunning = false;

        if(!errorMessage.isEmpty())
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        else{
            Toast.makeText(getContext(), "Downloading of the saved links from reddit completed!", Toast.LENGTH_LONG).show();
            new FillRedditLinks().execute();
        }

        mListener.onFragmentInteraction(Uri.parse("refillNavigationView"));
        mSwipeRefreshLayout.setRefreshing(false);
    }


    private class LoadRedditSavedLinks extends AsyncTask<Void, Void, Void> {

        private String errorMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            errorMessage = "";
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            finishGettingLinks(errorMessage);
        }

        protected Void doInBackground(Void... arg0) {
            String responseString = "";

            try{
                RefreshAccessToken refresher = new RefreshAccessToken();
                refresher.refresh(getContext());

                do{
                    responseString = getSavedLinks(responseString);
                }while(!responseString.isEmpty());

            }catch(NoRefreshOfTokenException e){
                errorMessage = e.getErrormessage();
            }catch(NoCurrentUserFoundException e){
                errorMessage = "No active user available!";
            }


            return null;
        }

        private String getSavedLinks(String afterItem){
            String responseString = "";

            Log.e(TAG, "showLinks for " + mUsername + " with token " + mAccesstoken + " & afterItem = " + afterItem);

            if(!mUsername.equals("")) {
                OkHttpClient client = new OkHttpClient();

                if(!afterItem.isEmpty())
                    afterItem = "&after=" + afterItem;

                Request request = new Request.Builder()
                        .addHeader("Authorization", "bearer " + mAccesstoken)
                        .url("https://oauth.reddit.com/user/" + mUsername + "/saved/.json?limit=30" + afterItem)
                        .build();

                try(Response response = client.newCall(request).execute()) {

                    if (!response.isSuccessful())
                        errorMessage = "General I/O response exception: " + response.code();
                    else {
                        responseString = readJsonResponse(response.body().string());
                    }
                }catch(IOException e){
                    errorMessage = "General I/O exception: " + e.getMessage();
                }
            }

            Log.e(TAG, errorMessage);

            if(responseString.equals("null"))
                responseString = "";

            return responseString;
        }

        /**
         * show saved links in textView
         * @param jsonStr JSON response as a String
         * @return afterItem indicates that the list of links continues starting with this item
         */
        private String readJsonResponse(String jsonStr){
            String afterItem = "";
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                if(jsonObj.has("error")){
                    errorMessage = "Error: " + jsonObj.getString("error");
                    if (jsonObj.has("message"))
                        errorMessage += "-" + jsonObj.getString("message");
                }else if(jsonObj.has("data")){
                    afterItem = readJsonData(jsonObj.getJSONObject("data"));
                }
            }catch(JSONException e){
                errorMessage = "General JSON exception: " + e.getMessage();
            }

            return afterItem;
        }

        /**
         * reads the children of the JSON Object and returns an after item if available
         * @param jsonObj
         * @return afterItem indicates that the list of links continues starting with this item
         * @throws JSONException exception while reading a json String into an object
         */
        private String readJsonData(JSONObject jsonObj) throws JSONException{
            JSONArray children = jsonObj.getJSONArray("children");

            for (int i = 0; i < children.length(); i++) {
                addToLinkList(readJsonChild(children.getJSONObject(i).getJSONObject("data")));
            }

            return ( jsonObj.has("after") ? jsonObj.getString("after") : "" );
        }

        /**
         *
         * @param jsonData Has to be the data value of a child
         * @return RedditLink object
         * @throws JSONException exception while reading a json String into an object
         */
        private RedditLink readJsonChild(JSONObject jsonData) throws JSONException{
            RedditLink redditlink = RedditLink.builder().build();

            if (jsonData.has("title"))
                redditlink.setTitle(jsonData.getString("title"));
            else if (jsonData.has("body"))
                redditlink.setTitle(jsonData.getString("body"));
            else redditlink.setTitle("no title");

            if (jsonData.has("subreddit_name_prefixed")) {
                redditlink.setSubreddit(jsonData.getString("subreddit_name_prefixed"));
            } else redditlink.setSubreddit("NA");

            if (jsonData.has("num_comments"))
                redditlink.setNumberOfComments(jsonData.getInt("num_comments"));

            if (jsonData.has("score"))
                redditlink.setScore(jsonData.getInt("score"));

            if (jsonData.has("domain"))
                redditlink.setDomain(jsonData.getString("domain"));

            if (jsonData.has("url"))
                redditlink.setUrl(jsonData.getString("url"));

            if (jsonData.has("link_url"))
                redditlink.setUrl(jsonData.getString("link_url"));

            if (jsonData.has("name"))
                redditlink.setFullname(jsonData.getString("name"));

            return redditlink;
        }

        /**
         * Save reddit link into the database
         * @param redditlink Redditlink TODO change to RedditLink class
         */
        private void addToLinkList(RedditLink redditlink){

            AppDatabase database = AppDatabase.getDatabase(getContext().getApplicationContext());

            database.LinkModel().addLink(redditlink);

            database.SubredditModel().addSubreddit(
                    Subreddit.builder().setSubredditname(redditlink.getSubreddit()).build());
        }

    }
}
