package org.gemafrzen.redditsavedlinks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class RedditLinkAdapter extends RecyclerView.Adapter<RedditLinkAdapter.MyViewHolder> {

    private String TAG = RedditLinkAdapter.class.getSimpleName();
    private Context mContext;
    private List<RedditLink> redditLinkList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, subreddit, comments, domain;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            subreddit = (TextView) view.findViewById(R.id.subreddit);
            domain = (TextView) view.findViewById(R.id.domain);
            comments = (TextView) view.findViewById(R.id.comments);

            View cardView = view.findViewById(R.id.card_view);

            if(cardView != null) cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position =  getAdapterPosition();
                    openInBrowser(position);
                    return true;
                }

            });
        }
    }

    public RedditLinkAdapter(Context mContext, List<RedditLink> linkList) {
        this.mContext = mContext;
        this.redditLinkList = linkList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reddit_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        RedditLink link = redditLinkList.get(position);
        holder.title.setText(link.getTitle());
        holder.subreddit.setText(link.getSubreddit() );
        holder.comments.setText("" + link.getNumberOfComments());
        holder.domain.setText(link.getDomain());
    }

    @Override
    public int getItemCount() {
        return redditLinkList.size();
    }

    private void openInBrowser(int position){
        Log.e(TAG, "redditLinkList.get(position).getUrl() = " + redditLinkList.get(position).getUrl());

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redditLinkList.get(position).getUrl()));
        mContext.startActivity(intent);
    }
}