package twitu.mywallet;

import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Nishchay on 01-09-2017.
 */

public class RecentTransactionsAdapter extends CursorAdapter {
    private Context context;

    public RecentTransactionsAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.recent_transaction_list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView amountTextView= (TextView) view.findViewById(R.id.amountTextView);
        TextView descriptionTextView=(TextView)view.findViewById(R.id.descriptionTextView);
        TextView timeTextView=(TextView)view.findViewById(R.id.timeAdded);

        String amount=cursor.getString(cursor.getColumnIndexOrThrow(transaction.COLUMN_TRANSACTION_AMOUNT));
        String balance=cursor.getString(cursor.getColumnIndexOrThrow(transaction.COLUMN_TRANSACTION_BALANCE));
        String description=cursor.getString(cursor.getColumnIndexOrThrow(transaction.COLUMN_TRANSACTION_DESCRIPTION));
        String time=cursor.getString(cursor.getColumnIndexOrThrow(transaction.COLUMN_TRANSACTION_TIME));
        String transactionType=cursor.getString(cursor.getColumnIndexOrThrow(transaction.COLUMN_TRANSACTION_TYPE));

        if(transactionType.matches("pay")) {
            amountTextView.setText("Paid: Rs." + amount);
        }else if(transactionType.matches("receive")){
            amountTextView.setText("Received: Rs." + amount);
        }
        timeTextView.setText("Time: "+convertTimeStampToDate(time));
        if(description!=null)
            descriptionTextView.setText("Description: "+description);
        else {
            descriptionTextView.setGravity(Gravity.CENTER);
            descriptionTextView.setText("No description available !");
        }
    }


    private String convertTimeStampToDate(String stamp){
        long timeStamp=Long.parseLong(stamp);
        DateFormat objFormatter = new SimpleDateFormat("dd MMM yyyy hh:mm aa", Locale.getDefault());
        objFormatter.setTimeZone(TimeZone.getDefault());

        Calendar objCalendar =
                Calendar.getInstance(TimeZone.getDefault());
        objCalendar.setTimeInMillis(timeStamp * 1000);//edit
        String result = objFormatter.format(objCalendar.getTime());
        objCalendar.clear();
        return result;
    }
}
