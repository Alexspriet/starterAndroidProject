package com.challenge.quapital.qapitalchallenge.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bartoszlipinski.recyclerviewheader2.RecyclerViewHeader;
import com.challenge.quapital.qapitalchallenge.R;
import com.challenge.quapital.qapitalchallenge.adapter.ListSavingsAdapter;
import com.challenge.quapital.qapitalchallenge.model.FeedSavings;
import com.challenge.quapital.qapitalchallenge.model.GlobalRules;
import com.challenge.quapital.qapitalchallenge.model.SavingEntry;
import com.challenge.quapital.qapitalchallenge.model.SavingRule;
import com.squareup.picasso.Picasso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Observable;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import static com.challenge.quapital.qapitalchallenge.activity.MainActivity.serviceRequest;


public class GoalActivity extends AppCompatActivity {

    private String mUrl, mName, mState;
    private int mId;
    private RecyclerView mRecyclerView;
    private TextView mTextViewTotal, mTextViewRules;
    private ListSavingsAdapter listSavingsAdapter;

    private Realm realm;
    private RealmConfiguration realmConfig;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPersistentData();
        getDataFromIntent();
        configureLayout();
        createObservables();
    }

    public void getDataFromIntent() {
        Intent intent = getIntent();
        mUrl = intent.getStringExtra(MainActivity.INTENT_IMAGE_URL);
        mName = intent.getStringExtra(MainActivity.INTENT_NAME);
        mState = intent.getStringExtra(MainActivity.INTENT_STATE);
        mId = intent.getIntExtra(MainActivity.INTENT_ID, -1);
    }

    public void configureLayout() {
        setContentView(R.layout.activity_goal);

        // get image from previous activity
        ImageView picture_view = (ImageView) findViewById(R.id.picture_view);
        Picasso.with(this).load(mUrl).into(picture_view);

        // get name from previous activity
        TextView name_text = (TextView) findViewById(R.id.name_text);
        name_text.setText(mName);

        // get balance state
        TextView state_text = (TextView) findViewById(R.id.state_text);
        state_text.setText(mState);

        RecyclerViewHeader header = (RecyclerViewHeader) findViewById(R.id.header);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        header.attachTo(mRecyclerView);
        mRecyclerView.setAdapter(listSavingsAdapter);

        mTextViewTotal = (TextView) findViewById(R.id.total_amount_text);
        mTextViewRules = (TextView) findViewById(R.id.rules_text);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private  void getPersistentData() {
        realmConfig = new RealmConfiguration
                .Builder(GoalActivity.this)
                .build();
        realm = Realm.getInstance(realmConfig);

        List<SavingEntry> allSavedEntry = realm.where(SavingEntry.class).findAll();
        listSavingsAdapter = new ListSavingsAdapter();

        for(SavingEntry goal : allSavedEntry) {
            listSavingsAdapter.addData(goal);
        }
    }


    private void createObservables() {
        if (mId != -1 && isNetworkAvailable()) {
            Observable
                    .zip(serviceRequest.getFeedSavings(mId),
                            serviceRequest.getGlobalRules(), new Func2<FeedSavings, GlobalRules, Boolean>() {
                                @Override
                                public Boolean call(FeedSavings feedSavings, GlobalRules rules) {
                                    displayFeedSavings(feedSavings);
                                    displayRules(rules);
                                    return true;
                                }
                            }).subscribeOn(Schedulers.newThread())
                    .subscribeOn(Schedulers.io())
                    .toBlocking()
                    .first();
        }
    }

    private void displayFeedSavings(FeedSavings feedSavings){
        double tmpTotal = 0;
        for (SavingEntry entry : feedSavings.feed) {
            listSavingsAdapter.addData(entry);
            tmpTotal += entry.amount;
            storeEntry(entry.id, entry);
        }
        mTextViewTotal.setText(getString(R.string.total_amount, tmpTotal));
    }

    public void storeEntry(String Id, final SavingEntry newGoal) {
        realm = Realm.getInstance(realmConfig);
        if (realm.where(SavingEntry.class).equalTo("id", Id).count() == 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(newGoal);
                }
            });
        }
    }

    private void displayRules(GlobalRules rules){
        String rulesStr = "";
        for (SavingRule rule : rules.savingsRules) {
            rulesStr += rule.type + ' ';
        }
        mTextViewRules.setText(rulesStr);
    }

    public static String getRelativeTime(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static long convertToMillis(String srcDate) {
        SimpleDateFormat format;
        if (srcDate.endsWith("Z")) {
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        } else {
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }

        long dateInMillis = 0;
        try {
            Date date = format.parse(srcDate);
            dateInMillis = date.getTime();
            return dateInMillis;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


}
