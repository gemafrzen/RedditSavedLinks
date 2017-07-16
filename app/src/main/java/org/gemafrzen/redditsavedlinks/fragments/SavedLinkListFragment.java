package org.gemafrzen.redditsavedlinks.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.gemafrzen.redditsavedlinks.R;
import org.gemafrzen.redditsavedlinks.RedditLink;
import org.gemafrzen.redditsavedlinks.RedditLinkAdapter;
import org.gemafrzen.redditsavedlinks.db.AppDatabase;
import org.gemafrzen.redditsavedlinks.db.entities.Subreddit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
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

    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RedditLinkAdapter adapter;
    private List<RedditLink> redditLinkList;

    private int sumLinks = 0;

    private OnFragmentInteractionListener mListener;

    public SavedLinkListFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SavedLinkListFragment.
     */
    public static SavedLinkListFragment newInstance() {
        SavedLinkListFragment fragment = new SavedLinkListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        redditLinkList = new ArrayList<>();
        adapter = new RedditLinkAdapter(this.getContext(), redditLinkList);
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void startGettingLinks(String accesstoken, String refreshToken, String username){
        mAccesstoken = accesstoken;
        mRefreshToken = refreshToken;
        mUsername = username;
        //startGettingLinks();
    }

    private void startGettingLinks(){
        // TODO remove and implement adding newer than last
        Thread thread = new Thread() {
            @Override
            public void run() {
                AppDatabase database = AppDatabase.getDatabase(getContext().getApplicationContext());

                database.SubredditModel().removeAllSubreddits();
            }
        };

        thread.start();

        redditLinkList.clear();
        getSavedLinks("");
    }


    /**
     *
     * @param redditlink
     * @param informActivity
     */
    private void addToLinkList(RedditLink redditlink, final boolean informActivity){
        redditLinkList.add(redditlink);

        final String subredditname = redditlink.getSubreddit();

        Thread thread = new Thread() {
            @Override
            public void run() {
                AppDatabase database = AppDatabase.getDatabase(getContext().getApplicationContext());

                Subreddit sub = Subreddit.builder().setSubredditname(subredditname).build();
                database.SubredditModel().addSubreddit(sub);

                if(informActivity)
                    mListener.onFragmentInteraction(Uri.parse("refillNavigationView"));
            }
        };

        thread.start();
    }


    /**
     * show saved links in textView
     * @param jsonStr
     */
    private void adaptJson(String jsonStr){
        try {
            JSONObject jsonObj = new JSONObject(jsonStr).getJSONObject("data");
            JSONArray children = jsonObj.getJSONArray("children");
            RedditLink redditlink = null;
            JSONObject jsonData = null;

            sumLinks += children.length();

            for(int i = 0 ; i < children.length(); i++){
                jsonData = children.getJSONObject(i).getJSONObject("data");
                redditlink = new RedditLink();

                if(jsonData.has("title"))
                    redditlink.setTitle(jsonData.getString("title"));
                else if(jsonData.has("body"))
                    redditlink.setTitle(jsonData.getString("body"));
                else redditlink.setTitle("no title");

                if(jsonData.has("subreddit_name_prefixed")) {
                    redditlink.setSubreddit(jsonData.getString("subreddit_name_prefixed"));
                }
                else redditlink.setSubreddit("NA");

                if(jsonData.has("num_comments"))
                    redditlink.setNumberOfComments(jsonData.getInt("num_comments"));

                if(jsonData.has("score"))
                    redditlink.setScore(jsonData.getInt("score"));

                if(jsonData.has("domain"))
                    redditlink.setDomain(jsonData.getString("domain"));

                if(jsonData.has("url"))
                    redditlink.setUrl(jsonData.getString("url"));

                if(jsonData.has("link_url"))
                    redditlink.setUrl(jsonData.getString("link_url"));

                //TODO change back to 30
                addToLinkList(redditlink,
                        children.length() < 40 && i + 1 == children.length());
            }

            if(children.length() < 40) { //TODO change back to 30
                Log.e(TAG,"getting Links finished with " + sumLinks + " Links");
                mSwipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }
            else{
                if(jsonObj != null && jsonObj.has("after"))
                    getSavedLinks(jsonObj.getString("after"));
            }

        }catch(JSONException e){
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
    }


    private void getSavedLinks(String afterItem){
        Log.e(TAG, "showLinks for " + mUsername + " with token " + mAccesstoken + " & afterItem = " + afterItem);

        if(!mUsername.equals("")) {
            OkHttpClient client = new OkHttpClient();

            if(!afterItem.isEmpty())
                afterItem = "&after=" + afterItem;

            Request request = new Request.Builder()
                    .addHeader("Authorization", "bearer " + mAccesstoken)
                    .url("https://oauth.reddit.com/user/" + mUsername + "/saved/.json?limit=30" + afterItem) //sr=fitness
                    .build();


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "ERROR: " + e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String jsonString = response.body().string();
                    Log.e(TAG, "onResponse success" + jsonString);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adaptJson(jsonString);
                        }
                    });
                }
            });
        }
    }
}
