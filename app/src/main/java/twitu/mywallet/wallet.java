package twitu.mywallet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class wallet extends AppCompatActivity {
    Dbhelper dbhelper = new Dbhelper(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        final SQLiteDatabase walletDb = dbhelper.getWritableDatabase();
        final TextView display = (TextView) findViewById(R.id.cash);
        Button pay = (Button) findViewById(R.id.pay);
        Button receive = (Button) findViewById(R.id.receive);
        final EditText info = (EditText) findViewById(R.id.info);
        final EditText amount = (EditText) findViewById(R.id.amount);

        SharedPreferences flag = getSharedPreferences("flag", Context.MODE_PRIVATE);
//        SharedPreferences flag= PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor flagEditor = flag.edit();

        Log.d("TAG","Last row ID: "+flag.getString("lastRow",null));
        updateDisplay(display, Long.parseLong(flag.getString("lastRow", null)));

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = Integer.parseInt((String) display.getText());
                int transactionAmount = Integer.parseInt(amount.getText().toString());
                ContentValues newvalues = new ContentValues();
                if (transactionAmount > current){
                    Toast.makeText(wallet.this, "You do not have the required cash", Toast.LENGTH_SHORT).show();
                    return;
                }
                newvalues.put(transaction.COLUMN_TRANSACTION_BALANCE, String.valueOf(current - transactionAmount));
                newvalues.put(transaction.COLUMN_TRANSACTION_DESCRIPTION, info.getText().toString());
                newvalues.put(transaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
                newvalues.put(transaction.COLUMN_TRANSACTION_TYPE, "pay");
                newvalues.put(transaction.COLUMN_TRANSACTION_AMOUNT, String.valueOf(transactionAmount));
                long newRowID = walletDb.insert(transaction.TABLE_NAME, null, newvalues);
                Log.d("TAG", "new row id: " + newRowID);
                flagEditor.putString("lastRow", String.valueOf(newRowID));
                flagEditor.apply();
                updateDisplay(display, newRowID);
            }
        });

        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = Integer.parseInt((String) display.getText());
                int transactionAmount = Integer.parseInt(amount.getText().toString());
                ContentValues newvalues = new ContentValues();
                newvalues.put(transaction.COLUMN_TRANSACTION_BALANCE, String.valueOf(current + transactionAmount));
                newvalues.put(transaction.COLUMN_TRANSACTION_DESCRIPTION, info.getText().toString());
                newvalues.put(transaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
                newvalues.put(transaction.COLUMN_TRANSACTION_TYPE, "receive");
                newvalues.put(transaction.COLUMN_TRANSACTION_AMOUNT, String.valueOf(transactionAmount));
                long newRowID = walletDb.insert(transaction.TABLE_NAME, null, newvalues);
                flagEditor.putString("lastRow", String.valueOf(newRowID));
                flagEditor.apply();
                Log.d("TAG", "new row id: " + newRowID);
                updateDisplay(display, newRowID);
            }
        });
    }

    private void updateDisplay(TextView display,long rowId) {
        SQLiteDatabase data = dbhelper.getReadableDatabase();
        Cursor cursor=data.rawQuery("SELECT * FROM moneyTransaction WHERE "+transaction._ID+" = "+String.valueOf(rowId),null);
        cursor.moveToNext();
        String currentBalance=cursor.getString(cursor.getColumnIndexOrThrow(transaction.COLUMN_TRANSACTION_BALANCE));
        Log.d("TAG","Current Balance: "+currentBalance);
        display.setText(currentBalance);
    }


}
