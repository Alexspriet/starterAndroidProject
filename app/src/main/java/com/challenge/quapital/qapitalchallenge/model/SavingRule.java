package com.challenge.quapital.qapitalchallenge.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SavingRule extends RealmObject {
    @PrimaryKey
    public int id;
    public String type;
    public double amount;

}