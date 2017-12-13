package twitu.mywallet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import twitu.mywallet.transaction.walletTransaction;
import twitu.mywallet.transaction.wallets;

public class MainActivity extends AppCompatActivity {
    SharedPreferences flag ;
    private WalletsDbHelper walletsDbHelper;
    private SQLiteDatabase walletsDb;
//    SharedPreferences flag = PreferenceManager.getDefaultSharedPreferences(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        walletsDbHelper=new WalletsDbHelper(MainActivity.this);
        walletsDb=walletsDbHelper.getWritableDatabase();
        flag=getSharedPreferences("flag", Context.MODE_PRIVATE);
        if (flag.getString("walletInitialize", "False").matches("True")){
            Intent next = new Intent(MainActivity.this, wallet.class);
            next.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(next);
        }
        else {
            setContentView(R.layout.activity_main);
            final EditText balance = (EditText) findViewById(R.id.balance);
            Button start = (Button) findViewById(R.id.start);

            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String initial = balance.getText().toString();
                    ContentValues contentValues= new ContentValues();
                    contentValues.put(wallets.COLUMN_WALLET_NAME,"Wallet");
                    contentValues.put(wallets.COLUMN_WALLET_PARENT_NAME,"Wallet");
                    long rowID=walletsDb.insertWithOnConflict(wallets.TABLE_NAME,null,contentValues,SQLiteDatabase.CONFLICT_ROLLBACK);
                    Log.d("TAG","new root wallet created with rowID: "+rowID);
                    walletsDb.close();
                    TransactionDbhelper transactionDbhelper = new TransactionDbhelper(MainActivity.this);

                    SQLiteDatabase walletDb = transactionDbhelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put(walletTransaction.COLUMN_TRANSACTION_BALANCE, initial);
                    values.put(walletTransaction.COLUMN_TRANSACTION_DESCRIPTION, "Initialized wallet");
                    values.put(walletTransaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
                    values.put(walletTransaction.COLUMN_TRANSACTION_TYPE, "receive");
                    values.put(walletTransaction.COLUMN_TRANSACTION_PARENT_WALLET_NAME,"Wallet");
                    values.put(walletTransaction.COLUMN_TRANSACTION_WALLET_NAME,"Wallet");
                    values.put(walletTransaction.COLUMN_TRANSACTION_AMOUNT, initial);
                    Log.d("TAG", "value :" + values);
                    long newRowID = walletDb.insert(walletTransaction.TABLE_NAME, null, values);
                    Log.d("TAG", "new row id: " + newRowID);
                    SharedPreferences.Editor flagEditor = flag.edit();
                    flagEditor.putString("lastRow", String.valueOf(newRowID));
                    flagEditor.apply();

                    Log.d("TAG","Last row ID: "+flag.getString("lastRow",null));

                    Intent next = new Intent(MainActivity.this, wallet.class);
                    next.putExtra("balance", balance.getText().toString().trim());
                    next.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    flagEditor.putString("walletInitialize", "True");
                    flagEditor.apply();
                    startActivity(next);
                }
            });
        }

    }
}
