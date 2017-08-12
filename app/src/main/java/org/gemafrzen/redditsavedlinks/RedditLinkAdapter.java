package org.gemafrzen.redditsavedlinks;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.gemafrzen.redditsavedlinks.db.AppDatabase;
import org.gemafrzen.redditsavedlinks.db.entities.RedditLink;
import org.gemafrzen.redditsavedlinks.db.entities.UserSettings;
import org.gemafrzen.redditsavedlinks.exceptions.NoCurrentUserFoundException;
import org.gemafrzen.redditsavedlinks.exceptions.NoRefreshOfTokenException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RedditLinkAdapter extends RecyclerView.Adapter<RedditLinkAdapter.MyViewHolder> implements Filterable{

    private String TAG = RedditLinkAdapter.class.getSimpleName();
    private Context mContext;
    private List<RedditLink> originalLinkList;
    private List<RedditLink> filteredLinkList;
    private RedditLinkFilter mFilter = new RedditLinkFilter();

    public RedditLinkAdapter(Context mContext, List<RedditLink> linkList) {
        this.mContext = mContext;
        this.originalLinkList = linkList;
        this.filteredLinkList = linkList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reddit_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        RedditLink link = filteredLinkList.get(position);
        holder.title.setText(link.getTitle());
        holder.subreddit.setText(link.getSubreddit() );
        holder.comments.setText("" + link.getNumberOfComments());
        holder.domain.setText(link.getDomain());
    }

    @Override
    public int getItemCount() {
        return filteredLinkList.size();
    }


    private void openInBrowser(int position){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(filteredLinkList.get(position).getUrl()));
        mContext.startActivity(intent);
    }


    private String getAccesstoken(){
        String accesstoken = "";
        //TODO async
        AppDatabase db = AppDatabase.getDatabaseOnUIThread(mContext);

        List<UserSettings> userSettingsList = db.UserSettingsModel().getUserSettings(true);

        if(userSettingsList != null && userSettingsList.size() > 0){
            UserSettings userSettings = userSettingsList.get(0);

            if(userSettings.accesstokenExpiresIn <= (Calendar.getInstance().getTimeInMillis() + 2000)){
                RefreshAccessToken refresher = new RefreshAccessToken();
                try{
                    accesstoken = refresher.refresh(mContext);
                }catch(NoCurrentUserFoundException e){
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG);
                }catch(NoRefreshOfTokenException e){
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG);
                }
            }else{
                accesstoken = userSettings.accesstoken;
            }

        }

        return accesstoken;
    }

    private void unsavesaveLink(final int position){
        String accesstoken = getAccesstoken();
        String command = "unsave";

        if(!filteredLinkList.get(position).isSaved())
            command = "save";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .addHeader("Authorization", "bearer " + accesstoken)
                .url("https://oauth.reddit.com/api/" + command)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "id=" + filteredLinkList.get(position).getFullname()
                                ))
                .build();

        client.newCall(request).enqueue(new Callback() {
            //TODO error handling
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
                // TODO runOnUIThread
                //Toast.makeText(mContext, " failed", Toast.LENGTH_LONG);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "unsave response = " + response.toString());
                // TODO runOnUIThread
                // Toast.makeText(mContext, "success", Toast.LENGTH_LONG);

                filteredLinkList.get(position).setSaved(false);
            }
        });

    }


    private void deleteLink(int position){
        /*AppDatabase database = AppDatabase.getDatabase(mContext.getApplicationContext());
        filteredLinkList.get(position)
        database.LinkModel().deleteLink
        */
        Log.e(TAG, "deleteLink not yet implemented");

        Toast.makeText(mContext.getApplicationContext(), "not yet implemented", Toast.LENGTH_LONG);
    }


    @Override
    public Filter getFilter() {
        return mFilter;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, subreddit, comments, domain;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            subreddit = (TextView) view.findViewById(R.id.subreddit);
            domain = (TextView) view.findViewById(R.id.domain);
            comments = (TextView) view.findViewById(R.id.comments);

            View cardView = view.findViewById(R.id.card_view);

            if(cardView != null){
                cardView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        int position =  getAdapterPosition();
                        openInBrowser(position);
                    }
                });

                cardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                            onCardClickOpenPopup(v, getAdapterPosition());
                           return true;
                        }
                });
            }
        }

    }

    public void onCardClickOpenPopup(View anchorView, final int adapterPosition) {
        if(filteredLinkList.size() <= adapterPosition) return;

        LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = vi.inflate(R.layout.popup_main, null);

        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // Example: If you have a TextView inside `popup_layout.xml`
        TextView tv = (TextView) popupView.findViewById(R.id.textOpenBrowser);
        tv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openInBrowser(adapterPosition);
            }
        });

        tv = (TextView) popupView.findViewById(R.id.textDeleteLink);
        tv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                deleteLink(adapterPosition);
                popupWindow.dismiss();
            }
        });

        tv = (TextView) popupView.findViewById(R.id.textUnsaveSave);
        tv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                unsavesaveLink(adapterPosition);
                popupWindow.dismiss();
            }
        });

        if(!filteredLinkList.get(adapterPosition).isSaved())
            tv.setText("save redditLink in reddit");

        tv = (TextView) popupView.findViewById(R.id.textDeleteUnsaveLink);
        tv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //unsavesaveLink(adapterPosition);
                deleteLink(adapterPosition);
                popupWindow.dismiss();
            }
        });

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);

        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());


        int location[] = new int[2];

        // Get the View's(the one that was clicked in the Fragment) location
        anchorView.getLocationOnScreen(location);

        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY,
                        location[0], location[1]);
    }


    private class RedditLinkFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            final List<RedditLink> list = originalLinkList;

            int count = list.size();
            final ArrayList<RedditLink> newFilteredlist = new ArrayList<>(count);

            String filterString = constraint.toString().toLowerCase();
            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getSubreddit();
                if (filterableString.toLowerCase().contains(filterString)) {
                    newFilteredlist.add(list.get(i));
                }
            }

            results.values = newFilteredlist;
            results.count = newFilteredlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredLinkList = (ArrayList<RedditLink>) results.values;
            notifyDataSetChanged();
        }

    }
}