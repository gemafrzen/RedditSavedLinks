package org.gemafrzen.redditsavedlinks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.gemafrzen.redditsavedlinks.db.entities.RedditLink;

import java.util.ArrayList;
import java.util.List;

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
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(filteredLinkList.get(position).getLink()));
        mContext.startActivity(intent);
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
            }
        }
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