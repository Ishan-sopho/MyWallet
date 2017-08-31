package twitu.mywallet;

import android.provider.BaseColumns;

import java.security.PublicKey;

/**
 * Created by Ishan on 24-08-2017.
 */

public final class transaction implements BaseColumns{
    //TABLE NAME
    public static final String TABLE_NAME = "moneyTransaction";

    //TABLE COLUMNS
    public static final String COLUMN_TRANSACTION_TYPE = "type";
    public static final String COLUMN_TRANSACTION_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTION_BALANCE = "balance";
    public static final String COLUMN_TRANSACTION_TIME = "time";
    public static final String COLUMN_TRANSACTION_DESCRIPTION = "description";
}
