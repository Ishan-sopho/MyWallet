package twitu.mywallet;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateWallet extends AppCompatActivity {

    private EditText walletNameEditText ;
    private EditText walletBalance ;
    private Button createWalletButton ;

    private WalletsDbHelper walletsDbHelper;
    private TransactionDbhelper transactionDbhelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);

        final Long currentWalletBalance = Long.parseLong(getIntent().getStringExtra("currentBalance"));
        walletsDbHelper=new WalletsDbHelper(CreateWallet.this);
        transactionDbhelper=new TransactionDbhelper(CreateWallet.this);

        walletNameEditText=(EditText)findViewById(R.id.walletNameEditText);
        walletBalance=(EditText)findViewById(R.id.newWalletBalanceEditText);
        createWalletButton=(Button)findViewById(R.id.createNewWallet);

        final SQLiteDatabase walletsDb = walletsDbHelper.getWritableDatabase();
        final SQLiteDatabase walletTransactionDb=transactionDbhelper.getWritableDatabase();

        createWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(walletNameEditText.getText().toString()) && !TextUtils.isEmpty(walletBalance.getText().toString()) && currentWalletBalance-Long.parseLong(walletBalance.getText().toString()) >0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(transaction.wallets.COLUMN_WALLET_PARENT_NAME, "MainWallet");
                    contentValues.put(transaction.wallets.COLUMN_WALLET_NAME,walletNameEditText.getText().toString());
                    walletsDb.insertWithOnConflict(transaction.wallets.TABLE_NAME,null,contentValues,SQLiteDatabase.CONFLICT_REPLACE);

                    Long newWalletBalance= Long.parseLong(walletBalance.getText().toString());

                    ContentValues newvalues = new ContentValues();
                    newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_BALANCE, String.valueOf(-newWalletBalance+currentWalletBalance));
                    newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_DESCRIPTION, "Wallet paid to "+walletNameEditText.getText().toString());
                    newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
                    newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_TYPE, "pay");
                    newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_WALLET_NAME,"MainWallet");
                    newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_PARENT_WALLET_NAME,"MainWallet");
                    newvalues.put(transaction.walletTransaction.COLUMN_TRANSACTION_AMOUNT, walletBalance.getText().toString());
                    walletTransactionDb.insert(transaction.walletTransaction.TABLE_NAME,null,newvalues);

                    ContentValues newvalues1 = new ContentValues();
                    newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_BALANCE, String.valueOf(newWalletBalance));
                    newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_DESCRIPTION, "Received from Wallet ");
                    newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_TIME, System.currentTimeMillis() / 1000);
                    newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_TYPE, "receive");
                    newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_WALLET_NAME,walletNameEditText.getText().toString());
                    newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_PARENT_WALLET_NAME,"MainWallet");
                    newvalues1.put(transaction.walletTransaction.COLUMN_TRANSACTION_AMOUNT, walletBalance.getText().toString());
                    walletTransactionDb.insert(transaction.walletTransaction.TABLE_NAME,null,newvalues1);

                    walletTransactionDb.close();
                    Intent intent = new Intent (CreateWallet.this,wallet.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else if (TextUtils.isEmpty(walletNameEditText.getText().toString())){
                    Toast.makeText(CreateWallet.this, "Enter a name!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(walletBalance.getText().toString())){
                    Toast.makeText(CreateWallet.this,"Please put some money in the wallet",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(CreateWallet.this, "Some error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
