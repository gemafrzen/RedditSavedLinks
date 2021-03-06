package org.gemafrzen.redditsavedlinks;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.gemafrzen.redditsavedlinks.db.AppDatabase;
import org.gemafrzen.redditsavedlinks.db.entities.Subreddit;
import org.gemafrzen.redditsavedlinks.db.entities.UserSettings;
import org.gemafrzen.redditsavedlinks.fragments.LoginFragment;
import org.gemafrzen.redditsavedlinks.fragments.SavedLinkListFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    SavedLinkListFragment.OnFragmentInteractionListener,
                    LoginFragment.OnFragmentInteractionListener{

    public static String ACCESS_TOKEN = "org.gemafrzen.redditsavedlinks.ACCESS_TOKEN";
    public static String REFRESH_TOKEN = "org.gemafrzen.redditsavedlinks.REFRSH_TOKEN";
    public static String USERNAME = "org.gemafrzen.redditsavedlinks.USERNAME";

    private String TAG = MainActivity.class.getSimpleName();
    String accessToken = "";
    String refreshToken = "";
    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //TODO save user settings
        AppDatabase database = AppDatabase.getDatabaseOnUIThread(getApplicationContext());
        List<UserSettings> us = database.UserSettingsModel().getUserSettings(true);

        Fragment fragment;

        if(us.isEmpty()) fragment = LoginFragment.newInstance();
        else {
            accessToken = us.get(0).accesstoken;
            refreshToken = us.get(0).refreshtoken;
            username = us.get(0).username;
            fragment = SavedLinkListFragment.newInstance(accessToken, refreshToken, username);
        }

        getFragmentManager().beginTransaction().replace(R.id.fragmentframe, fragment).commit();

        new FillMenu().execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Log.e(TAG, "ID = " + id + " and Title = " + item.getTitle());


        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentframe);

        if(fragment instanceof LoginFragment){
            Toast.makeText(getApplicationContext(), "Please login first!", Toast.LENGTH_SHORT).show();
        }else if(fragment instanceof SavedLinkListFragment){

            if (id == R.id.nav_all)
                ((SavedLinkListFragment) fragment).setFilter("");
            else
                ((SavedLinkListFragment) fragment).setFilter(item.getTitle().toString());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        Log.e(TAG, "onFragmentInteraction=" + uri.toString());

        if(uri.getEncodedPath().equals("login")){
            Log.e(TAG, "path=" + uri.getQueryParameter(MainActivity.ACCESS_TOKEN));
            Log.e(TAG, "path=" + uri.getQueryParameter(MainActivity.USERNAME));

            SavedLinkListFragment fragment = new SavedLinkListFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentframe, fragment)
                    .addToBackStack(null)
                    .commit();

            fragment.setTokensAndUsername(uri.getQueryParameter(MainActivity.ACCESS_TOKEN),
                    uri.getQueryParameter(MainActivity.REFRESH_TOKEN),
                    uri.getQueryParameter(MainActivity.USERNAME));
        }else if(uri.toString().equals("refillNavigationView")){
            new FillMenu().execute();
        }
    }

    @Override
    protected void onDestroy() {
        AppDatabase.destroyInstance();
        super.onDestroy();
    }


    private void refillNavigationView(List<Subreddit> subreddits){
        if(!subreddits.isEmpty()) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            Menu menu = navigationView.getMenu();
            menu.removeGroup(1);

            for (Subreddit subreddit : subreddits) {
                menu.add(1, 0, 0, subreddit.subredditname);
            }
        }
    }

    private class FillMenu extends AsyncTask<Void, Void, Void> {

        List<Subreddit> subreddits;

        public FillMenu() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(!subreddits.isEmpty()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refillNavigationView(subreddits);
                    }
                });
            }
        }

        protected Void doInBackground(Void... arg0) {
            AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
            subreddits = database.SubredditModel().getAllSubredditsSorted();

            return null;
        }
    }
}
