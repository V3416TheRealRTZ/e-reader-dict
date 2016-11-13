package com.example.e_reader_dict;

import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Kraft on 13.11.2016.
 */
public class TranslateDialog extends Dialog {

    EReaderActivity activity;
    Button translateButton, cancelButton, okButton;
    TextView word1Text, word2Text, translationText;
    LinearLayout translateScreen, resultScreen, translatingScreen;
    //String word = "";

    public TranslateDialog(EReaderActivity a, String word) {
        super(a);
        setContentView(R.layout.translate_dialog);
        this.activity = a;
        translateButton = (Button) findViewById(R.id.translateButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        okButton = (Button) findViewById(R.id.okButton);
        word1Text = (TextView) findViewById(R.id.word1Text);
        word2Text = (TextView) findViewById(R.id.word2Text);
        translationText = (TextView) findViewById(R.id.translationText);
        translateScreen = (LinearLayout) findViewById(R.id.translateScreen);
        translatingScreen = (LinearLayout) findViewById(R.id.translatingScreen);
        resultScreen = (LinearLayout) findViewById(R.id.resultScreen);
        final String wordd = word;
        word1Text.setText(word);
        word2Text.setText(word);
        translateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                translateScreen.setVisibility(View.GONE);
                translatingScreen.setVisibility(View.VISIBLE);
                new WordTranslateAsyncTask(activity, translationText, translatingScreen, resultScreen).execute(activity.getString(R.string.yandex_url), wordd);
                Log.d("tapped on: ", "translateButton");
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d("tapped on: ", "cancelButton");
                dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d("tapped on: ", "okButton");
                dismiss();
            }
        });
    }
}
