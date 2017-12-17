package twitu.mywallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import twitu.mywallet.transaction.walletTransaction;

public class wallet extends AppCompatActivity {

    TransactionDbhelper transactionDbhelper = new TransactionDbhelper(this);
    private WalletsDbHelper walletsDbHelper;

    private Toolbar toolbar;
    private SharedPreferences flag;
    public static final int REQUEST_EXTERNAL_PERMISSION_CODE = 666;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static final String[] PERMISSIONS_EXTERNAL_STORAGE = {
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE
    };

    private String lastRowID;

    private SQLiteDatabase walletDb;
    private ListView listView;

    private Spinner walletsSpinner;
    private TextView display ;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        walletsDbHelper = new WalletsDbHelper(wallet.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("MyWallet");
        setSupportActionBar(toolbar);

        display = (TextView) findViewById(R.id.cash);

        listView = (ListView) findViewById(R.id.listView);
        View v = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.recent_transaction_view, null, false);
        listView.addHeaderView(v);

        walletsSpinner = (Spinner) findViewById(R.id.walletsNameSpinner);

        List<String> walletsNameList = updateSpinner();

        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, walletsNameList);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        walletsSpinner.setAdapter(spinnerAdapter);
        walletsSpinner.setSelection(0);
        walletsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 spinnerOnItemClick(parent, view, position, id,display);
             }

             @Override
             public void onNothingSelected(AdapterView<?> parent) {

             }
         });
        Log.d("TAG", "spinner selected item: " + walletsSpinner.getSelectedItem());
        walletDb = transactionDbhelper.getWritableDatabase();

        Button pay = (Button) findViewById(R.id.pay);
        Button receive = (Button) findViewById(R.id.receive);
        final EditText info = (EditText) findViewById(R.id.info);
        final EditText amount = (EditText) findViewById(R.id.amount);

        populateRecentsList();
        long row=0;
//        SQLiteDatabase transactionDb = transactionDbhelper.getReadableDatabase();
        Cursor cursor = walletDb.rawQuery("SELECT * FROM moneyTransaction WHERE "+walletTransaction.COLUMN_TRANSACTION_WALLET_NAME+" = '"+walletsSpinner.getSelectedItem().toString()+"'"+" ORDER BY _id DESC LIMIT 1",null);
        if(cursor.moveToFirst()){
            row= Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction._ID)));
            Log.d("TAG","Last row id :"+row);
        }
//        Cursor cursor = transactionDb.rawQuery("SELECT TOP 1 * FROM moneyTransaction WHERE walletName = wallet ORDER BY "+walletTransaction._ID+" DESC",null);
        flag = getSharedPreferences("flag", Context.MODE_PRIVATE);
//        SharedPreferences flag= PreferenceManager.getDefaultSharedPreferences(this);
//        final SharedPreferences.Editor flagEditor = flag.edit();

//        Log.d("TAG", "Last row ID: " + flag.getString("lastRow", null));
        updateDisplay(display,row);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = 0;
                int walletTransactionAmount = 0;
                try {
                    current = Integer.parseInt((String) display.getText());
                    walletTransactionAmount = Integer.parseInt(amount.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(wallet.this, "Please put in valid input !", Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentValues newvalues = new ContentValues();
                if (walletTransactionAmount > current) {
                    Toast.makeText(wallet.this, "You do not have the required cash", Toast.LENGTH_SHORT).show();
                    return;
                }
                SQLiteDatabase walletsDb =walletsDbHelper.getReadableDatabase();
                Cursor cursor1 = walletsDb.rawQuery("SELECT * FROM "+transaction.wallets.TABLE_NAME+" WHERE "+transaction.wallets.COLUMN_WALLET_NAME+" = '"+walletsSpinner.getSelectedItem().toString()+"'",null);
                cursor1.moveToFirst();
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_BALANCE, String.valueOf(current - walletTransactionAmount));
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_DESCRIPTION, info.getText().toString());
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_TYPE, "pay");
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_WALLET_NAME,walletsSpinner.getSelectedItem().toString());
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_PARENT_WALLET_NAME,cursor1.getString(cursor1.getColumnIndexOrThrow(transaction.wallets.COLUMN_WALLET_PARENT_NAME)));
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_AMOUNT, String.valueOf(walletTransactionAmount));
                Log.d("TAG","Entering : "+newvalues);
                long newRowID = walletDb.insert(walletTransaction.TABLE_NAME, null, newvalues);
                cursor1.close();
                walletsDb.close();
                populateRecentsList();
                Log.d("TAG", "new row id: " + newRowID);
//                flagEditor.putString("lastRow", String.valueOf(newRowID));
//                flagEditor.apply();
                updateDisplay(display, newRowID);
                info.setText("");
                amount.setText("");
                hideKeyboard(wallet.this);
            }
        });

        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = 0;
                int walletTransactionAmount = 0;
                try {
                    current = Integer.parseInt((String) display.getText());
                    walletTransactionAmount = Integer.parseInt(amount.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(wallet.this, "Please put in valid input!", Toast.LENGTH_SHORT).show();
                    return;
                }
                SQLiteDatabase walletsDb =walletsDbHelper.getReadableDatabase();
                Cursor cursor1 = walletsDb.rawQuery("SELECT * FROM "+transaction.wallets.TABLE_NAME+" WHERE "+transaction.wallets.COLUMN_WALLET_NAME+" = '"+walletsSpinner.getSelectedItem().toString()+"'",null);
                cursor1.moveToFirst();

                ContentValues newvalues = new ContentValues();
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_BALANCE, String.valueOf(current + walletTransactionAmount));
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_DESCRIPTION, info.getText().toString());
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_TYPE, "receive");
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_WALLET_NAME,walletsSpinner.getSelectedItem().toString());
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_PARENT_WALLET_NAME,cursor1.getString(cursor1.getColumnIndexOrThrow(transaction.wallets.COLUMN_WALLET_PARENT_NAME)));
                newvalues.put(walletTransaction.COLUMN_TRANSACTION_AMOUNT, String.valueOf(walletTransactionAmount));
                Log.d("TAG","Entering : "+newvalues);
                long newRowID = walletDb.insert(walletTransaction.TABLE_NAME, null, newvalues);
                cursor1.close();
                walletsDb.close();
                populateRecentsList();
//                flagEditor.putString("lastRow", String.valueOf(newRowID));
//                flagEditor.apply();
                Log.d("TAG", "new row id: " + newRowID);
                updateDisplay(display, newRowID);
                info.setText("");
                amount.setText("");
                hideKeyboard(wallet.this);
            }
        });
    }

    private void populateRecentsList() {
        final SQLiteDatabase recentsDb = transactionDbhelper.getReadableDatabase();
        Cursor cursor1 = recentsDb.rawQuery("SELECT * FROM moneyTransaction where walletName = '"+walletsSpinner.getSelectedItem().toString()+"' order by " + walletTransaction._ID + " DESC limit 3", null);
        cursor1.moveToNext();
        lastRowID = cursor1.getString(cursor1.getColumnIndexOrThrow(walletTransaction._ID));
        while (cursor1.isLast()) {
            Log.d("TAG", "row id: " + cursor1.getString(cursor1.getColumnIndexOrThrow(walletTransaction._ID)));
            cursor1.moveToNext();
        }

        RecentTransactionsAdapter adapter = new RecentTransactionsAdapter(this, cursor1);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
                if (position == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(wallet.this);
                    builder.setMessage("Do you want to remove this walletTransaction ?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String idToBeDeleted = String.valueOf(Integer.parseInt(lastRowID));
                            Log.d("TAG", "Row to be deleted: " + idToBeDeleted);
                            String tableName = "moneyTransaction";
                            String whereClause = walletTransaction._ID + "=?";
                            String[] whereArgs = new String[]{idToBeDeleted};
                            int deleteID = recentsDb.delete(tableName, whereClause, whereArgs);
                            Log.d("TAG", "Delete id= " + deleteID);
                            populateRecentsList();
                            lastRowID = String.valueOf(Integer.parseInt(lastRowID) - 1);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create().show();

                } else if (position != 1) {
                    Toast.makeText(wallet.this, "Can only delete last walletTransaction!", Toast.LENGTH_SHORT).show();
                }

                return false;
            }

            ;
        });
//        recentsDb.close();
        if (cursor1.isLast())
            cursor1.close();


    }

    private void updateDisplay(TextView display, long rowId) {
//        SQLiteDatabase data = transactionDbhelper.getReadableDatabase();
        Cursor cursor = walletDb.rawQuery("SELECT * FROM moneyTransaction WHERE " + walletTransaction._ID + " = " + String.valueOf(rowId), null);
        cursor.moveToNext();
        String currentBalance = cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction.COLUMN_TRANSACTION_BALANCE));
        Log.d("TAG", "Current Balance: " + currentBalance);
        display.setText(currentBalance);
        cursor.close();
//        data.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export:
                checkExternalStoragePermission(this);
                if (checkExternalStoragePermission(this))
                    exportToCSV();
                return true;
            case R.id.deleteWallet:
                deleteCurrentWallet();
                return true;
            case R.id.createWallet:
                if(!walletsSpinner.getSelectedItem().toString().matches("MainWallet")){
                    Toast.makeText(this, "Can't create subwallet of this wallet currently", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(wallet.this,CreateWallet.class);
                intent.putExtra("currentBalance",display.getText().toString());
                startActivity(intent);
                return true;
            case R.id.mergeWallet:
                mergeWallet();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.add(1,3,Menu.NONE,"Merge Wallet");
        if(walletsSpinner.getSelectedItem().toString().matches("MainWallet")){
            menu.getItem(3).setEnabled(false);
        }else{
            menu.getItem(3).setEnabled(true);
        }
        return true;
    }

    private void deleteCurrentWallet() {
//        String dbpath=getDatabasePath("moneyTransaction.db").getPath();
//        final SQLiteDatabase db=SQLiteDatabase.openDatabase(dbpath,null,Context.MODE_PRIVATE);
        final SQLiteDatabase db = transactionDbhelper.getWritableDatabase();
        final SQLiteDatabase walletsDb = walletsDbHelper.getWritableDatabase();
        AlertDialog.Builder builder = new AlertDialog.Builder(wallet.this);
        builder.setMessage("Are you sure you want to delete this wallet ?");
        builder.setCancelable(false);
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                finish();
                dialogInterface.dismiss();
            }
        }).setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                transactionDbhelper.delete(db);
                walletsDbHelper.delete(walletsDb);
                SharedPreferences.Editor editor = flag.edit();
                editor.putString("walletInitialize", "False");
                editor.apply();
                Intent intent = new Intent(wallet.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean checkExternalStoragePermission(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            return true;
        }

        int readStoragePermissionState = ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE);
        int writeStoragePermissionState = ContextCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);
        boolean externalStoragePermissionGranted = readStoragePermissionState == PackageManager.PERMISSION_GRANTED &&
                writeStoragePermissionState == PackageManager.PERMISSION_GRANTED;
        if (!externalStoragePermissionGranted) {
            requestPermissions(PERMISSIONS_EXTERNAL_STORAGE, REQUEST_EXTERNAL_PERMISSION_CODE);
        }

        return externalStoragePermissionGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_EXTERNAL_PERMISSION_CODE) {
                if (checkExternalStoragePermission(this))
                    exportToCSV();
            }
        }
    }

    private void exportToCSV() {
        Log.d("TAG", "Inside export to CSV");
        SQLiteDatabase db = transactionDbhelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from moneyTransaction", null);
//        String rowID = flag.getString("lastRow", null);
//        int numberOfRows = Integer.valueOf(rowID);
        String path = Environment.getExternalStorageDirectory() + "/Download/" + "wallet.csv";
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String[]> data = new ArrayList<String[]>();
        data.add(new String[]{"TimeStamp", "Transaction type", "Amount", "Current Balance", "Description","walletName","ParentWalletName"});
        while (cursor.moveToNext()) {
            String timeStamp = cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction.COLUMN_TRANSACTION_TIME));
            String walletTransactionType = cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction.COLUMN_TRANSACTION_TYPE));
            String amount = cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction.COLUMN_TRANSACTION_AMOUNT));
            String balance = cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction.COLUMN_TRANSACTION_BALANCE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction.COLUMN_TRANSACTION_DESCRIPTION));
            String walletName =cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction.COLUMN_TRANSACTION_WALLET_NAME));
            String parentWalletName = cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction.COLUMN_TRANSACTION_PARENT_WALLET_NAME));
//            Log.d("TAG","Transaction detected !");
            data.add(new String[]{convertTimeStampToDate(timeStamp), walletTransactionType, amount, balance, description,walletName,parentWalletName});
        }
        if (writer != null) {
            writer.writeAll(data);
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Successfully made CSV !!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Some error occured!", Toast.LENGTH_SHORT).show();
        }
        if (cursor.isLast()) {
            cursor.close();
        }

        db.close();
    }

    private String convertTimeStampToDate(String stamp) {
        long timeStamp = Long.parseLong(stamp);
        DateFormat objFormatter = new SimpleDateFormat("dd MMM yyyy hh:mm aa", Locale.getDefault());
        objFormatter.setTimeZone(TimeZone.getDefault());

        Calendar objCalendar =
                Calendar.getInstance(TimeZone.getDefault());
        objCalendar.setTimeInMillis(timeStamp * 1000);//edit
        String result = objFormatter.format(objCalendar.getTime());
        objCalendar.clear();
        return result;
    }

    public static void hideKeyboard(Activity activity) {

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View v = activity.getCurrentFocus();

        if (v == null) {
            v = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public List<String> updateSpinner() {
        List<String> walletsName = new ArrayList<String>();
        SQLiteDatabase walletsDb = walletsDbHelper.getReadableDatabase();
        Cursor cursor = walletsDb.rawQuery("SELECT * FROM " + transaction.wallets.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            walletsName.add(cursor.getString(cursor.getColumnIndexOrThrow(transaction.wallets.COLUMN_WALLET_NAME)));
        }
        cursor.close();
        walletsDb.close();
        return walletsName;
    }

    public void spinnerOnItemClick(AdapterView parent, View view, int position, long id,TextView display) {
        long row=0;
        Cursor cursor = walletDb.rawQuery("SELECT * FROM moneyTransaction WHERE "+walletTransaction.COLUMN_TRANSACTION_WALLET_NAME+" = '"+walletsSpinner.getSelectedItem().toString()+"'"+" ORDER BY _id DESC LIMIT 1",null);
        if(cursor.moveToNext()){
            row= Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction._ID)));
            Log.d("TAG","Last row id :"+row);
        }
        updateDisplay(display,row);
        cursor.close();

        populateRecentsList();
    }

    public void mergeWallet(){
        SQLiteDatabase walletsDb =walletsDbHelper.getWritableDatabase();
        Cursor cursor1 = walletsDb.rawQuery("SELECT * FROM "+transaction.wallets.TABLE_NAME+" WHERE "+transaction.wallets.COLUMN_WALLET_NAME+" = '"+walletsSpinner.getSelectedItem().toString()+"'",null);
        cursor1.moveToFirst();
        String parentWallet =cursor1.getString(cursor1.getColumnIndexOrThrow(transaction.wallets.COLUMN_WALLET_PARENT_NAME));
        Log.d("TAG","Parent wallet name: "+parentWallet);

        Long currentBalance= Long.parseLong(display.getText().toString());
        ContentValues newvalues = new ContentValues();
        newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_BALANCE, String.valueOf(0));
        newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_DESCRIPTION, walletsSpinner.getSelectedItem().toString()+" paid to Wallet");
        newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
        newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_TYPE, "pay");
        newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_WALLET_NAME,walletsSpinner.getSelectedItem().toString());
        newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_PARENT_WALLET_NAME,parentWallet);
        newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_AMOUNT, display.getText().toString());
        cursor1.close();

        walletDb.insert(transaction.walletTransaction.TABLE_NAME,null,newvalues);
        Cursor cursor2 = walletsDb.rawQuery("SELECT * FROM "+transaction.wallets.TABLE_NAME+" WHERE "+transaction.wallets.COLUMN_WALLET_NAME+" = '"+parentWallet+"'",null);
        cursor2.moveToNext();
        Cursor cursor = walletDb.rawQuery("SELECT * FROM moneyTransaction WHERE "+walletTransaction.COLUMN_TRANSACTION_WALLET_NAME+" = '"+walletsSpinner.getSelectedItem().toString()+"'"+" ORDER BY _id DESC LIMIT 1",null);
        cursor.moveToNext();
        Long currentMainBalance = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(walletTransaction.COLUMN_TRANSACTION_BALANCE)));

        cursor.close();

        ContentValues newvalues1 = new ContentValues();
        newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_BALANCE, String.valueOf(currentBalance+currentMainBalance));
        newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_DESCRIPTION, "Received from "+walletsSpinner.getSelectedItem().toString());
        newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
        newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_TYPE, "receive");
        newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_WALLET_NAME,parentWallet);
        newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_PARENT_WALLET_NAME,cursor2.getString(cursor2.getColumnIndexOrThrow(transaction.wallets.COLUMN_WALLET_PARENT_NAME)));
        newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_AMOUNT,currentBalance);
        walletDb.insert(transaction.walletTransaction.TABLE_NAME,null,newvalues1);
        cursor2.close();
        walletsDb.delete(transaction.wallets.TABLE_NAME,transaction.wallets.COLUMN_WALLET_NAME+" = '"+walletsSpinner.getSelectedItem().toString()+"'",null);
        walletsDb.close();

        List<String> walletsNameList = updateSpinner();

        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, walletsNameList);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        walletsSpinner.setAdapter(spinnerAdapter);
        walletsSpinner.setSelection(0);
        walletsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerOnItemClick(parent, view, position, id,display);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}

