package com.challenge.quapital.qapitalchallenge.model;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import java.text.DecimalFormat;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class SavingEntry extends RealmObject {
    @PrimaryKey
    public String id;
    public String type;
    public String timestamp;
    @Ignore
    public String displayRelativeTime;
    public String message;
    public double amount;
    public int userId;
    public int savingsRuleId;

    public Spanned getMessageText() {
        return Html.fromHtml(this.message);
    }

    public String getAmountToDisplay(){
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return "$"+decimalFormat.format(this.amount);
    }
}
