package twitu.mywallet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import twitu.mywallet.transaction;

/**
 * Created by Ishan on 24-08-2017.
 */

public class Dbhelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "transactions.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + transaction.TABLE_NAME + " (" +
                    transaction._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    transaction.COLUMN_TRANSACTION_TYPE + " TEXT," +
                    transaction.COLUMN_TRANSACTION_AMOUNT + " TEXT," +
                    transaction.COLUMN_TRANSACTION_TIME + " TEXT," +
                    transaction.COLUMN_TRANSACTION_DESCRIPTION + " TEXT," +
                    transaction.COLUMN_TRANSACTION_BALANCE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE  " + transaction.TABLE_NAME;

    public Dbhelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
//        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void delete(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
