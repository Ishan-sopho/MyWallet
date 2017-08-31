package twitu.mywallet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    SharedPreferences flag ;
//    SharedPreferences flag = PreferenceManager.getDefaultSharedPreferences(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flag=getSharedPreferences("flag", Context.MODE_PRIVATE);
        if (flag.getString("walletInitialize", "False").matches("True")){
            Intent next = new Intent(MainActivity.this, wallet.class);
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
                    Dbhelper dbhelper = new Dbhelper(MainActivity.this);
                    final SQLiteDatabase walletDb = dbhelper.getWritableDatabase();

                    final ContentValues values = new ContentValues();
                    values.put(transaction.COLUMN_TRANSACTION_BALANCE, initial);
                    values.put(transaction.COLUMN_TRANSACTION_DESCRIPTION, "Initialized wallet");
                    values.put(transaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
                    values.put(transaction.COLUMN_TRANSACTION_TYPE, "receive");
                    values.put(transaction.COLUMN_TRANSACTION_AMOUNT, initial);
                    Log.d("TAG", "value :" + values);
                    long newRowID = walletDb.insert(transaction.TABLE_NAME, null, values);
                    Log.d("TAG", "new row id: " + newRowID);
                    SharedPreferences.Editor flagEditor = flag.edit();
                    flagEditor.putString("lastRow", String.valueOf(newRowID));
                    flagEditor.apply();

                    Log.d("TAG","Last row ID: "+flag.getString("lastRow",null));

                    Intent next = new Intent(MainActivity.this, wallet.class);
                    next.putExtra("balance", balance.getText().toString().trim());
                    next.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    flagEditor.putString("walletInitialize", "True");
                    flagEditor.apply();
                    startActivity(next);
                }
            });
        }

    }
}
