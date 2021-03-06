package com.namtran.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.namtran.entity.TransactionsItem;
import com.namtran.main.R;

import java.util.ArrayList;

/**
 * Created by namtr on 20/08/2016.
 */
public class TransactionsRecyclerAdapter extends RecyclerView.Adapter<TransactionsViewHolder> {
    private final View.OnClickListener mOnClickListener;
    private ArrayList<TransactionsItem> mListTrans;
    private boolean isReveneus;

    public TransactionsRecyclerAdapter(ArrayList<TransactionsItem> data, boolean isReveneus, View.OnClickListener mOnClickListener) {
        this.mListTrans = data;
        this.isReveneus = isReveneus;
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public TransactionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.custom_list_recycler, parent, false);
        view.setOnClickListener(mOnClickListener);
        return new TransactionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionsViewHolder holder, int position) {
        TransactionsItem transactionsItem = mListTrans.get(position);
        setItemText(transactionsItem, holder);
    }

    private void setItemText(TransactionsItem item, TransactionsViewHolder holder) {
        String money;
        if (isReveneus) {
            money = "+" + holder.mParser.format(item.getCost());
            holder.cost.setTextColor(Color.BLUE);
        } else {
            money = "-" + holder.mParser.format(item.getCost());
            holder.cost.setTextColor(Color.RED);
        }
        holder.cost.setText(money);
        holder.type.setText(item.getType());
        String notes = item.getNote();
        if (!TextUtils.isEmpty(notes)) {
            holder.note.setVisibility(View.VISIBLE);
            holder.note.setText(notes);
        } else {
            holder.note.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mListTrans == null ? 0 : mListTrans.size();
    }

}
