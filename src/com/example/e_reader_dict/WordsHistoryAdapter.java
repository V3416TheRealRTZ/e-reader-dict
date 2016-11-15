package com.example.e_reader_dict;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

/**
 * Created by Kraft on 15.11.2016.
 */
public class WordsHistoryAdapter extends BaseAdapter {

    ArrayList<String> engWords, rusWords;
    ArrayList<Integer> learnIds;
    Context mContext;

    public WordsHistoryAdapter(Context mContext, ArrayList<String> eng, ArrayList<String> rus, ArrayList<Integer> learns) {
        engWords = eng;
        rusWords = rus;
        learnIds = learns;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return engWords.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.history_entry, parent, false);

        TextView engWordText = (TextView) row.findViewById(R.id.engWordText);
        TextView rusWordText = (TextView) row.findViewById(R.id.rusWordText);
        Button startLearningButton = (Button) row.findViewById(R.id.startLearningButton);

        engWordText.setText(engWords.get(position));
        rusWordText.setText(rusWords.get(position));

        if (learnIds.contains(position)) {
            startLearningButton.setText(mContext.getString(R.string.already_learning));
            startLearningButton.setEnabled(false);
        } else {
            startLearningButton.setText(mContext.getString(R.string.start_learning));
        }

        return row;
    }
}
