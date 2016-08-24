package namtran.lab.exchange;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import namtran.lab.adapter.ExchangeRecyclerAdapter;
import namtran.lab.entity.CurrencyParser;
import namtran.lab.entity.ExchangeItem;
import namtran.lab.entity.TransactionsItem;
import namtran.lab.revexpmanager.R;

/**
 * Created by namtr on 22/08/2016.
 */
public class RevExchangeFragment extends Fragment {
    private static ArrayList<ExchangeItem> mListExchange;
    private static ExchangeRecyclerAdapter mAdapter;
    private ArrayList<TransactionsItem> mListTrans;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_list_item, container, false);
        mListExchange = new ArrayList<>();
        mListTrans = ExchangeFragment.mListTransIn;
        setupRecyclerView(recyclerView);
        return recyclerView;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ExchangeRecyclerAdapter(new RecyclerListener(recyclerView), mListExchange, true);
        recyclerView.setAdapter(mAdapter);
    }


    public static void update(ArrayList<ExchangeItem> data) {
        Log.d("Update Exp", "doing");
        mListExchange.clear();
        mListExchange.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

    private class RecyclerListener implements View.OnClickListener {
        private RecyclerView mRecyclerView;
        private CurrencyParser mParser;

        public RecyclerListener(RecyclerView recyclerView) {
            this.mRecyclerView = recyclerView;
            mParser = new CurrencyParser();
        }

        @Override
        public void onClick(View view) {
            int position = mRecyclerView.getChildLayoutPosition(view);
            TransactionsItem transactionsItem = mListTrans.get(position);
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle("Thông Tin Khoản Thu").setCancelable(true);
            StringBuilder msg = new StringBuilder();
            msg.append("Tiền: ").append(mParser.format(transactionsItem.getCost()));
            msg.append("\nNhóm: ").append(transactionsItem.getType());
            msg.append("\nNgày: ").append(transactionsItem.getDate());
            msg.append("\nGhi Chú: ").append(transactionsItem.getNote().isEmpty() ? "Trống" : transactionsItem.getNote());
            dialog.setMessage(msg);
            dialog.setNegativeButton("OK", null);
            dialog.show();
        }
    }
}
