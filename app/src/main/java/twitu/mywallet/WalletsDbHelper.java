package twitu.mywallet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Nishchay on 13-12-2017.
 */

public class WalletsDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "wallets.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + transaction.wallets.TABLE_NAME + " (" +
                    transaction.wallets._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    transaction.wallets.COLUMN_WALLET_PARENT_NAME + " TEXT," +
                    transaction.wallets.COLUMN_WALLET_NAME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE  " + transaction.wallets.TABLE_NAME;

    public WalletsDbHelper(Context context) {
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
