package com.challenge.quapital.qapitalchallenge.adapter;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.challenge.quapital.qapitalchallenge.R;
import com.challenge.quapital.qapitalchallenge.databinding.ListItemBinding;
import com.challenge.quapital.qapitalchallenge.handler.GoalClickHandler;
import com.challenge.quapital.qapitalchallenge.model.SavingsGoals;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CardGoalAdapter extends RecyclerView.Adapter<CardGoalAdapter.BindingHolder> {
    private  List<SavingsGoals> mItems;
    private OnClickCardListener mListener;

    public CardGoalAdapter(OnClickCardListener listener) {
        super();
        mItems = new ArrayList<>();
        mListener = listener;
    }

    public void addData(SavingsGoals goal) {
        mItems.add(goal);
        notifyDataSetChanged();
    }

    public void addAll(List<SavingsGoals> list) {
        mItems.addAll(list);
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        BindingHolder viewHolder = new BindingHolder(v);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(BindingHolder holder, int i) {
        final SavingsGoals savingsGoal = mItems.get(i);
        holder.getBinding().setVariable(com.challenge.quapital.qapitalchallenge.BR.savingsGoal, savingsGoal);

        final View v = holder.getBinding().getRoot();
        holder.getBinding().setHandler(new GoalClickHandler() {
            @Override
            public void onClick(final View view) {
                mListener.OnClickCard(v, savingsGoal);
            }
        });
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class BindingHolder extends RecyclerView.ViewHolder {
        private ListItemBinding binding;

        public BindingHolder(View rowView) {
            super(rowView);
            binding = DataBindingUtil.bind(rowView);
        }
        public ListItemBinding getBinding() {
            return binding;
        }
    }

    @BindingAdapter("bind:imageUrl")
    public static void loadImage(ImageView imageView, String url) {
        Picasso.with(imageView.getContext()).load(url).into(imageView);
    }

    public interface OnClickCardListener {
        void OnClickCard(View v, SavingsGoals goal);
    }

}