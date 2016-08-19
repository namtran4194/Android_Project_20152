package namtran.lab.revexpmanager;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;

import namtran.lab.adapter.OutAdapter;
import namtran.lab.database.InDb;
import namtran.lab.database.OutDb;
import namtran.lab.entity.Item;
import namtran.lab.entity.UserInfo;

/**
 * Created by namtr on 19/08/2016.
 */
public class ExpFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Firebase root;
    private SQLiteDatabase sqlOut, sqlIn;
    private Item item;
    private ArrayList<Item> out;
    private OutAdapter adapter;
    private ProgressDialog dialog;
    private UserInfo info;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_expenditure, container, false);
        getUserInfo();
        // Khởi tạo firebase
        Firebase.setAndroidContext(getContext());
        root = new Firebase("https://expenseproject.firebaseio.com");
        // Khởi tạo database
        InDb inDb = new InDb(getContext());
        OutDb outDb = new OutDb(getContext());
        sqlIn = inDb.getWritableDatabase();
        sqlOut = outDb.getWritableDatabase();
        // Listview
        out = new ArrayList<>(); // Lưu danh sách chi
        ListView list = (ListView) rootView.findViewById(R.id.list_item_exp);
        adapter = new OutAdapter(getActivity(), R.layout.custom_list_out, out);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        // Lấy dữ liệu hiển thị lên listview
        initDialog();
        if (isDbEmpty()) {
            new GetDataFromServer().execute();
        } else {
            new GetDataFromDb().execute();
        }
        return rootView;
    }

    // Lấy thông tin người dùng
    private void getUserInfo() {
        SharedPreferences pref = getContext().getSharedPreferences(UserInfo.PREF_NAME, Context.MODE_PRIVATE);
        String email = pref.getString(UserInfo.KEY_EMAIL, null);
        String uid = pref.getString(UserInfo.KEY_UID, null);
        info = new UserInfo(email, uid);
    }

    // Hiển thị dialog khi lấy dữ liệu
    private void initDialog() {
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Đang tải dữ liệu...");
        dialog.setCancelable(false);
    }

    // Kiểm tra database có dữ liệu hay chưa
    private boolean isDbEmpty() {
        Cursor cursorIn = sqlIn.rawQuery("select * from " + InDb.TABLE_NAME, null);
        Cursor cursorOut = sqlOut.rawQuery("select * from " + OutDb.TABLE_NAME, null);
        boolean isEmpty = cursorIn.moveToFirst() && cursorOut.moveToFirst();
        cursorIn.close();
        cursorOut.close();
        Log.d("Is database empty", !isEmpty + "");
        return !isEmpty;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = out.get(position);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setCancelable(false);
        dialog.setTitle("Thông Tin Khoản Chi");
        StringBuilder msg = new StringBuilder();
        msg.append("Tiền: ").append(format.format(Integer.parseInt(item.getCost())));
        msg.append("\nLoại: ").append(item.getType());
        msg.append("\nNgày: ").append(item.getDate());
        msg.append("\nGhi Chú: ").append(item.getNote().isEmpty() ? "Trống" : item.getNote());
        dialog.setMessage(msg);
        dialog.setNegativeButton("OK", null);
        dialog.show();
    }

    // Lấy dữ liệu từ Database rồi hiển thị lên listview
    private class GetDataFromDb extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Item item;
            int id;
            String queryOut = "select * from " + OutDb.TABLE_NAME;
            Cursor cursorOut = sqlOut.rawQuery(queryOut, null);
            if (cursorOut.moveToFirst()) { // Thu
                do {
                    id = Integer.parseInt(cursorOut.getString(0));
                    String cost = cursorOut.getString(1);
                    String type = cursorOut.getString(2);
                    String note = cursorOut.getString(3);
                    String date = cursorOut.getString(4);
                    item = new Item(cost, type, note, date, id);
                    out.add(item);
                    Log.d("OUT DATA - Local", item.toString());
                } while (cursorOut.moveToNext());
            }
            cursorOut.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        }
    }

    // Lấy dữ liệu từ server
    private class GetDataFromServer extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("Get From Server - Exp", "doing");
            // Tiền chi
            Firebase refExpense = root.child(info.getUid()).child("Expense");
            final Query outQuery = refExpense.orderByValue();
            outQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot listItem : dataSnapshot.getChildren()) {
                        item = listItem.getValue(Item.class);
                        ContentValues cv = new ContentValues();
                        cv.put(OutDb.COL_COST, item.getCost());
                        cv.put(OutDb.COL_TYPE, item.getType());
                        cv.put(OutDb.COL_NOTE, item.getNote());
                        cv.put(OutDb.COL_DATE, item.getDate());
                        sqlOut.insert(OutDb.TABLE_NAME, null, cv);
                        out.add(item);
                        Log.d("OUT DATA - Server", item.toString());
                    }
                    sqlOut.close();
                    outQuery.removeEventListener(this);
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                    Log.d("Get out data - Exp", "Done");
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }
    }
}
