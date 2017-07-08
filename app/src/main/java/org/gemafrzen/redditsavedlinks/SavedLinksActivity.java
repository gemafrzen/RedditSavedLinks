package org.gemafrzen.redditsavedlinks;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SavedLinksActivity extends Activity {

    public static String JSON_STRING = "org.gemafrzen.redditsavedlinks.JSONSTRING";
    private String TAG = SavedLinksActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private RedditLinkAdapter adapter;
    private List<RedditLink> redditLinkList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_links);

        redditLinkList = new ArrayList<>();
        adapter = new RedditLinkAdapter(this, redditLinkList);

        recyclerView  = (RecyclerView) findViewById(R.id.rv);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        if(this.getIntent() != null)
            showJson(this.getIntent().getStringExtra(JSON_STRING));
    }

    /**
     * show saved links in textView
     * @param jsonStr
     */
    private void showJson(String jsonStr){
        Log.e(TAG,jsonStr);
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray children = jsonObj.getJSONObject("data").getJSONArray("children");
            RedditLink redditlink;
            redditLinkList.clear();
            for(int i = 0 ; i < 25; i++){
                Log.e(TAG, "" + i);
                JSONObject jsonData = children.getJSONObject(i).getJSONObject("data");
                redditlink = new RedditLink();

                if(jsonData.has("title"))
                    redditlink.setTitle(jsonData.getString("title"));
                else if(jsonData.has("body"))
                    redditlink.setTitle(jsonData.getString("body"));
                else redditlink.setTitle("no title");

                if(jsonData.has("subreddit_name_prefixed"))
                    redditlink.setSubreddit(jsonData.getString("subreddit_name_prefixed"));
                else redditlink.setSubreddit("NA");

                if(jsonData.has("comments"))
                    redditlink.setNumberOfComments(jsonData.getInt("num_comments"));

                if(jsonData.has("score"))
                    redditlink.setNumberOfComments(jsonData.getInt("score"));

                if(jsonData.has("domain"))
                    redditlink.setSubreddit(jsonData.getString("domain"));

                redditLinkList.add(redditlink);
            }

            adapter.notifyDataSetChanged();
        }catch(JSONException e){
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
    }
}
