package com.challenge.quapital.qapitalchallenge.service;

import com.challenge.quapital.qapitalchallenge.model.FeedSavings;
import com.challenge.quapital.qapitalchallenge.model.GlobalRules;
import com.challenge.quapital.qapitalchallenge.model.GlobalSavings;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface QapitalService {
    String SERVICE_ENDPOINT = "http://qapital-ios-testtask.herokuapp.com";

    @GET("/savingsgoals")
    Observable<GlobalSavings> getGlobalSavings();

    @GET("/savingsgoals/{id}/feed")
    Observable<FeedSavings> getFeedSavings(@Path("id") int id);

    @GET("/savingsrules")
    Observable<GlobalRules> getGlobalRules();
}