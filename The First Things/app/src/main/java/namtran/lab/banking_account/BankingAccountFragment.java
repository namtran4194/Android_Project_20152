package namtran.lab.banking_account;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import namtran.lab.adapter.ListAccountAdapter;
import namtran.lab.database.InterestDb;
import namtran.lab.entity.BankingItem;
import namtran.lab.revexpmanager.R;

/**
 * Created by namtr on 23/08/2016.
 */
public class BankingAccountFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemLongClickListener {
    private Spinner mSpinner;
    private EditText mMoneyField, mRateField;
    private TextView mDateTextView;
    private SQLiteDatabase mSQLiteRate;
    private ListAccountAdapter mAdapter;
    private ArrayList<BankingItem> mListAccount = new ArrayList<>();
    private Calendar mCalendar = Calendar.getInstance();
    private View view;
    private ProgressDialog mProDialog;
    private static final String[] BANK_NAME = {"VietinBank", "CBBank", "OceanBank", "GPBank", "AgriBank", "ACBank", "TPBank", "DongABank",
            "SeABank", "ABBank", "TechcomBank", "VPBank", "SHBank", "VietABank", "PGBank", "VCBank", "BIDV", "HSBC", "CitiBank"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_banking_account, container, false);
        initControl(view);
        return view;
    }

    private void initControl(View rootView) {
        InterestDb interestDb = new InterestDb(getContext());
        mSQLiteRate = interestDb.getWritableDatabase();
        mProDialog = new ProgressDialog(getContext());
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner_banking);
        mMoneyField = (EditText) rootView.findViewById(R.id.et_money_banking);
        mRateField = (EditText) rootView.findViewById(R.id.et_rate_banking);
        mDateTextView = (TextView) rootView.findViewById(R.id.tv_date_banking);
        ImageButton btnSelectDate = (ImageButton) rootView.findViewById(R.id.btn_select_date_banking);
        ImageButton btnSave = (ImageButton) rootView.findViewById(R.id.btn_save_banking);
        btnSelectDate.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        ListView listAccount = (ListView) rootView.findViewById(R.id.list_account);
        listAccount.setOnItemLongClickListener(this);
        mAdapter = new ListAccountAdapter(getContext(), R.layout.custom_list_account, mListAccount);
        listAccount.setAdapter(mAdapter);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, BANK_NAME);
        mSpinner.setAdapter(adapter);
        showDefaultDate();
        new GetDataFromDb().execute();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_select_date_banking:
                showDatePicker();
                break;
            case R.id.btn_save_banking:
                saveAccount();
                break;
        }
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Tuỳ chỉnh tài khoản");
        dialog.setMessage("Chọn chức năng mà bạn muốn?");
        dialog.setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAccount(position);
            }
        });
        dialog.show();
        return false;
    }

    private void saveAccount() {
        String bankName = mSpinner.getSelectedItem().toString();
        String money = mMoneyField.getText().toString();
        String date = mDateTextView.getText().toString();
        String rate = mRateField.getText().toString();
        if (money.isEmpty() || checkZero(money)) {
            mMoneyField.requestFocus();
            Snackbar.make(view, "Bạn chưa nhập số tiền", Snackbar.LENGTH_SHORT).show();
        } else if (rate.isEmpty() || checkZero(rate)) {
            mRateField.requestFocus();
            Snackbar.make(view, "Bạn chưa nhập lãi xuất", Snackbar.LENGTH_SHORT).show();
        } else {
            view.setEnabled(false);
            BankingItem item = new BankingItem(bankName, money, rate, date);
            new SaveToDatabase().execute(item);
        }
    }

    private void deleteAccount(int position) {
        String query = "select * from " + InterestDb.TABLE_NAME;
        Cursor cursor = mSQLiteRate.rawQuery(query, null);
        cursor.moveToPosition(position);
        String rowId = cursor.getString(0);
        mSQLiteRate.delete(InterestDb.TABLE_NAME, "_id=?", new String[]{rowId});
        cursor.requery();
        cursor.close();
        mListAccount.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    public boolean checkZero(String s) {
        try {
            Float f = Float.parseFloat(s);
            if (f == 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e("BankingAccountFragment", e.getMessage());
        }
        return false;
    }

    private void showDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.getDefault());
        mDateTextView.setText(format.format(calendar.getTime()));
    }

    private void showDatePicker() {
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        DatePickerFragment pickerFragment = new DatePickerFragment(getContext(), callback, year, month, day);
        pickerFragment.show();
    }


    private DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendar.set(year, monthOfYear, dayOfMonth);
            monthOfYear = monthOfYear + 1;
            String date, day, month;
            if (dayOfMonth < 10) {
                day = "0" + dayOfMonth;
            } else {
                day = dayOfMonth + "";
            }
            if (monthOfYear < 10) {
                month = "0" + monthOfYear;
            } else {
                month = monthOfYear + "";
            }
            date = day + "/" + month + "/" + year;
            mDateTextView.setText(date);
        }
    };

    private class DatePickerFragment extends DatePickerDialog {

        public DatePickerFragment(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
        }

        @Override
        public void setTitle(CharSequence title) {
            super.setTitle("Chọn ngày gửi");
        }
    }

    private class SaveToDatabase extends AsyncTask<BankingItem, Void, Void> {

        @Override
        protected Void doInBackground(BankingItem... params) {
            BankingItem item = params[0];
            ContentValues values = new ContentValues();
            values.put(InterestDb.COL_NAME, item.getName());
            values.put(InterestDb.COL_MONEY, item.getMoney());
            values.put(InterestDb.COL_RATE, item.getRate());
            values.put(InterestDb.COL_DATE, item.getDate());
            long id = mSQLiteRate.insert(InterestDb.TABLE_NAME, null, values);
            item.setId(String.valueOf(id));
            mListAccount.add(item);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            view.setEnabled(true);
            mMoneyField.setText("");
            mRateField.setText("");
            mAdapter.notifyDataSetChanged();
            Snackbar.make(view, "Thêm thành công", Snackbar.LENGTH_SHORT).show();
        }
    }

    private class GetDataFromDb extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProDialog.setMessage("Đang tải...");
            mProDialog.setCancelable(false);
            mProDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            BankingItem item;
            String query = "select * from " + InterestDb.TABLE_NAME;
            Cursor cursor = mSQLiteRate.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(0);
                    String name = cursor.getString(1);
                    String money = cursor.getString(2);
                    String rate = cursor.getString(3);
                    String date = cursor.getString(4);
                    item = new BankingItem(name, money, rate, date, id);
                    mListAccount.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.notifyDataSetChanged();
            if (mProDialog.isShowing()) {
                mProDialog.dismiss();
            }
        }
    }
}
