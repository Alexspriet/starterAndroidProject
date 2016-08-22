package com.challenge.quapital.qapitalchallenge.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.challenge.quapital.qapitalchallenge.BR;
import com.challenge.quapital.qapitalchallenge.R;
import com.challenge.quapital.qapitalchallenge.model.SavingEntry;

import java.util.ArrayList;
import java.util.List;

import static com.challenge.quapital.qapitalchallenge.activity.GoalActivity.convertToMillis;
import static com.challenge.quapital.qapitalchallenge.activity.GoalActivity.getRelativeTime;

public class ListSavingsAdapter extends RecyclerView.Adapter<ListSavingsAdapter.BindingHolder> {
    private  List<SavingEntry> mItems;

    public ListSavingsAdapter() {
        super();
        mItems = new ArrayList<>();
    }

    public void addData(SavingEntry savingEntry) {
        mItems.add(savingEntry);
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.savings_item, viewGroup, false);
        BindingHolder viewHolder = new BindingHolder(v);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(BindingHolder holder, int i) {
        final SavingEntry savingEntry = mItems.get(i);
        savingEntry.displayRelativeTime = getRelativeTime(convertToMillis(savingEntry.timestamp));
        holder.getBinding().setVariable(BR.savingEntry, savingEntry);
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class BindingHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public BindingHolder(View rowView) {
            super(rowView);
            binding = DataBindingUtil.bind(rowView);
        }
        public ViewDataBinding getBinding() {
            return binding;
        }
    }

}