package com.example.e_reader_dict;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Kraft on 15.11.2016.
 */
public class BooksAdapter extends BaseAdapter {

    ArrayList<String> books;
    EReaderActivity mContext;

    public BooksAdapter(EReaderActivity mContext, ArrayList<String> books) {
        this.books = books;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.book_entry, parent, false);

        TextView bookText = (TextView) row.findViewById(R.id.bookText);
        Button removeBookButton = (Button) row.findViewById(R.id.removeBookButton);

        removeBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mContext.removeBook(position);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        bookText.setText(books.get(position));

        return row;
    }
}
