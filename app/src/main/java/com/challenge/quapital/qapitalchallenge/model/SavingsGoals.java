package com.challenge.quapital.qapitalchallenge.model;

import java.text.DecimalFormat;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SavingsGoals extends RealmObject {
    @PrimaryKey
    public int id;
    public String goalImageURL;
    public int userId;
    public double targetAmount;
    public double currentBalance;
    public String status;
    public String name;
    public RealmList<RealmInt> created;

    public String getAmountToDisplay(){
        DecimalFormat decimalFormat = new DecimalFormat("#");
        String tmp = (this.targetAmount > 0) ? " of " + decimalFormat.format(this.targetAmount)  : "";
        return "$"+decimalFormat.format(this.currentBalance)+ tmp;
    }

    public String getDateToDisplay(){
        return (created.size() > 2) ?
                created.get(0).value + "-" +
                created.get(1).value + "-" +
                created.get(2).value : "";
    }

}
