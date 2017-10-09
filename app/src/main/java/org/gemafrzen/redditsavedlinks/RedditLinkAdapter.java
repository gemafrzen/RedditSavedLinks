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
import java.text.SimpleDateFormat;
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

public class RedditLinkAdapter extends RecyclerView.Adapter<RedditLinkAdapter.CardViewHolder> implements Filterable{

    private String TAG = RedditLinkAdapter.class.getSimpleName();

    public static final int VIEWHOLDER_TYPE_THREAD = 1;
    public static final int VIEWHOLDER_TYPE_COMMENT = 2;

    private Context mContext;
    private List<RedditLink> originalLinkList;
    private List<RedditLink> filteredLinkList;
    private RedditLinkFilter mFilter;
    private SimpleDateFormat mFormat;

    public RedditLinkAdapter(Context mContext, List<RedditLink> linkList) {
        this.mContext = mContext;
        this.originalLinkList = linkList;
        this.filteredLinkList = linkList;
        mFormat = new SimpleDateFormat("dd.MM.yyyy");
        mFilter = new RedditLinkFilter();
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        if(viewType == VIEWHOLDER_TYPE_COMMENT)
            return new CardCommentViewHolder(layoutInflater, parent);
        else // VIEWHOLDER_TYPE_THREAD
            return new CardThreadViewHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(final CardViewHolder holder, int position) {
        holder.bind(filteredLinkList.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredLinkList.size();
    }


    @Override
    public int getItemViewType(int position) {
        if(filteredLinkList.get(position).getDomain().isEmpty())
            return VIEWHOLDER_TYPE_COMMENT;
        else
            return VIEWHOLDER_TYPE_THREAD;
    }

    private void openInBrowser(int position){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(filteredLinkList.get(position).getUrl()));
        mContext.startActivity(intent);
    }

    private void openInReddit(int position){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com" +
                filteredLinkList.get(position).getPermaLink()));
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
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }catch(NoRefreshOfTokenException e){
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
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


    public abstract class CardViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public CardViewHolder(View view){
            super(view);

            View cardView = itemView.findViewById(R.id.card_view);

            if(cardView != null){
                cardView.setOnClickListener(this);

                cardView.setOnLongClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            openInBrowser(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            onCardClickOpenPopup(view, getAdapterPosition());
            return true;
        }

        public abstract void bind(RedditLink redditLink);
    }


    public class CardThreadViewHolder extends CardViewHolder {
        public TextView title, subreddit, comments, domain, createdUtc, selftext;

        public CardThreadViewHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.reddit_card, parent, false));

            title = (TextView) itemView.findViewById(R.id.title);
            subreddit = (TextView) itemView.findViewById(R.id.subreddit);
            domain = (TextView) itemView.findViewById(R.id.domain);
            comments = (TextView) itemView.findViewById(R.id.comments);
            createdUtc = (TextView) itemView.findViewById(R.id.utc);
            selftext = (TextView) itemView.findViewById(R.id.selftext);
        }

        public void bind(RedditLink redditLink){
            title.setText(redditLink.getTitle());
            subreddit.setText(redditLink.getSubreddit() );
            comments.setText("" + redditLink.getNumberOfComments());
            domain.setText(redditLink.getDomain());
            createdUtc.setText("" + redditLink.getUtc());
            selftext.setText(redditLink.getSelftext());

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(redditLink.getUtc() * 1000);
            createdUtc.setText(mFormat.format(cal.getTime()));
        }
    }


    public class CardCommentViewHolder extends CardViewHolder {
        public TextView title, subreddit, createdUtc, selftext;

        public CardCommentViewHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.reddit_card_comment, parent, false));
            title = (TextView) itemView.findViewById(R.id.title);
            subreddit = (TextView) itemView.findViewById(R.id.subreddit);
            createdUtc = (TextView) itemView.findViewById(R.id.utc);
            selftext = (TextView) itemView.findViewById(R.id.selftext);
        }

        public void bind(RedditLink redditLink){
            title.setText(redditLink.getTitle());
            subreddit.setText(redditLink.getSubreddit() );
            createdUtc.setText("" + redditLink.getUtc());
            selftext.setText(redditLink.getSelftext());

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(redditLink.getUtc() * 1000);
            createdUtc.setText(mFormat.format(cal.getTime()));
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

        tv = (TextView) popupView.findViewById(R.id.textOpenReddit);
        tv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openInReddit(adapterPosition);
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

            String filterString = constraint.toString().toLowerCase();

            if(filterString.isEmpty()){
                results.values = originalLinkList;
                results.count = originalLinkList.size();
            }else {
                final List<RedditLink> list = originalLinkList;
                int count = list.size();
                final ArrayList<RedditLink> newFilteredlist = new ArrayList<>(count);

                String filterableString;

                for (int i = 0; i < count; i++) {
                    filterableString = list.get(i).getSubreddit();
                    if (filterableString.equalsIgnoreCase(filterString)) {
                        newFilteredlist.add(list.get(i));
                    }
                }

                results.values = newFilteredlist;
                results.count = newFilteredlist.size();
            }

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