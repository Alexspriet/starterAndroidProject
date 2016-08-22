package com.challenge.quapital.qapitalchallenge.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.challenge.quapital.qapitalchallenge.R;
import com.challenge.quapital.qapitalchallenge.adapter.CardGoalAdapter;
import com.challenge.quapital.qapitalchallenge.model.GlobalSavings;
import com.challenge.quapital.qapitalchallenge.model.RealmInt;
import com.challenge.quapital.qapitalchallenge.model.SavingsGoals;
import com.challenge.quapital.qapitalchallenge.service.QapitalService;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity implements CardGoalAdapter.OnClickCardListener{

    final static String TAG = "QAPITAL";
    final static String INTENT_IMAGE_URL = "INTENT_IMAGE_URL";
    final static String INTENT_NAME = "INTENT_TEXT";
    final static String INTENT_STATE = "INTENT_STATE";
    final static String INTENT_ID = "INTENT_ID";

    private CardGoalAdapter mCardAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;

    private Realm realm;
    private RealmConfiguration realmConfig;
    public static QapitalService serviceRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPersistentData();
        configureLayout();
        createService();
        createObservables();
    }

    private void createService() {
        serviceRequest = createRetrofitService(QapitalService.class, QapitalService.SERVICE_ENDPOINT);
    }

    private void createObservables() {
        serviceRequest.getGlobalSavings()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GlobalSavings>() {
                    @Override
                    public final void onCompleted() {
                        endRefreshUI();
                    }

                    @Override
                    public final void onError(Throwable e) {
                        endRefreshUI();
                    }

                    @Override
                    public final void onNext(GlobalSavings response) {
                        for(SavingsGoals goal : response.savingsGoals) {
                            mCardAdapter.addData(goal);
                            storeGoals(goal.id, goal);
                        }
                        endRefreshUI();
                    }
                });
    }

    private void endRefreshUI() {
        if(mSwipeContainer != null) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeContainer.setRefreshing(false);
                }
            }, 1800);
        }
    }

    // get persist data with Realm
    private  void getPersistentData() {
        realmConfig = new RealmConfiguration
                .Builder(this)
                .build();
        realm = Realm.getInstance(realmConfig);

        List<SavingsGoals> allSavedGoals = realm.where(SavingsGoals.class).findAll();
        mCardAdapter = new CardGoalAdapter(this);

        for(SavingsGoals goal : allSavedGoals) {
            mCardAdapter.addData(goal);
        }
    }

    private void configureLayout() {
        setContentView(R.layout.activity_main);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.goals));
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mCardAdapter);

        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                createObservables();
            }
        });
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    // persist and store data with Realm if Id is new
    public void storeGoals(int Id, final SavingsGoals newGoal) {
        if (realm.where(SavingsGoals.class).equalTo("id", Id).count() == 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(newGoal);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static <T> T createRetrofitService(final Class<T> clazz, final String endPoint) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endPoint)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String msg) {
                        Log.i(TAG, msg);
                    }
                })
                .setConverter(new GsonConverter(new GsonBuilder()
                        .registerTypeAdapter(new TypeToken<RealmList<RealmInt>>() {}.getType(), new TypeAdapter<RealmList<RealmInt>>() {

                            @Override
                            public void write(JsonWriter out, RealmList<RealmInt> value) throws IOException {
                            }

                            @Override
                            public RealmList<RealmInt> read(JsonReader in) throws IOException {
                                RealmList<RealmInt> list = new RealmList<RealmInt>();
                                in.beginArray();
                                while (in.hasNext()) {
                                    list.add(new RealmInt(in.nextInt()));
                                }
                                in.endArray();
                                return list;
                            }
                        })
                        .create()))
                .build();
        T service = restAdapter.create(clazz);

        return service;
    }

    @Override
    public void OnClickCard(View v, SavingsGoals goal) {
        Intent intent = new Intent(this, GoalActivity.class);
        intent.putExtra(INTENT_IMAGE_URL, goal.goalImageURL);
        intent.putExtra(INTENT_NAME, goal.name);
        intent.putExtra(INTENT_STATE, goal.getAmountToDisplay());
        intent.putExtra(INTENT_ID, goal.id);

        if (Build.VERSION.SDK_INT >= 21) {

            View picture_view = v.findViewById(R.id.picture_view);
            View name_text = v.findViewById(R.id.name_text);
            View state_text = v.findViewById(R.id.state_text);
            View background_view = v.findViewById(R.id.background_view);

            Pair<View, String> pair1 = Pair.create(picture_view, picture_view.getTransitionName());
            Pair<View, String> pair2 = Pair.create(name_text, name_text.getTransitionName());
            Pair<View, String> pair3 = Pair.create(state_text, state_text.getTransitionName());
            Pair<View, String> pair4 = Pair.create(background_view, background_view.getTransitionName());

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair1, pair2, pair3, pair4);
            startActivity(intent, options.toBundle());

        } else {
            startActivity(intent);
        }
    }
}
