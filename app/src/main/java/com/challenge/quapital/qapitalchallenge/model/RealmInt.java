package com.challenge.quapital.qapitalchallenge.model;

import io.realm.RealmObject;

public class RealmInt extends RealmObject {
    public int value;

    public RealmInt() {
    }

    public RealmInt(int value) {
        this.value = value;
    }

}