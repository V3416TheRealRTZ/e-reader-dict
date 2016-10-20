package com.example.e_reader_dict;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class EReaderActivity extends Activity {
    LinearLayout booksScreen, learnScreen, historyScreen, topageScreen, aboutScreen;
    ImageButton booksButton, learnButton, historyButton, topageButton, aboutButton;
    TextView pageData;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        booksScreen = (LinearLayout)findViewById(R.id.booksScreen);
        booksButton = (ImageButton) findViewById(R.id.booksButton);
        learnScreen = (LinearLayout)findViewById(R.id.learnScreen);
        learnButton = (ImageButton) findViewById(R.id.learnButton);
        historyScreen = (LinearLayout)findViewById(R.id.historyScreen);
        historyButton = (ImageButton) findViewById(R.id.historyButton);
        topageScreen = (LinearLayout)findViewById(R.id.topageScreen);
        topageButton = (ImageButton) findViewById(R.id.topageButton);
        aboutScreen = (LinearLayout)findViewById(R.id.aboutScreen);
        aboutButton = (ImageButton) findViewById(R.id.aboutButton);
        pageData = (TextView) findViewById(R.id.pageData);
    }

    private void updateMenu(LinearLayout screen, ImageButton button, Boolean value) {
        if (value) {
            screen.setVisibility(View.VISIBLE);
            button.setAlpha(0.5f);
        } else {
            screen.setVisibility(View.GONE);
            button.setAlpha(1.0f);
        }
    }

    private void updateMenus(ArrayList<Boolean> menus) {
        updateMenu(booksScreen, booksButton, menus.get(0));
        updateMenu(learnScreen, learnButton, menus.get(1));
        updateMenu(historyScreen, historyButton, menus.get(2));
        updateMenu(topageScreen, topageButton, menus.get(3));
        updateMenu(aboutScreen, aboutButton, menus.get(4));
        for (int i =0; i < menus.size(); ++i) {
            if (menus.get(i)) {
                pageData.setVisibility(View.GONE);
                return;
            }
        }
        pageData.setVisibility(View.VISIBLE);
    }

    private ArrayList<Boolean> formMenusArray(int size, int id, boolean value) {
        ArrayList<Boolean> result = new ArrayList<>();
        for (int i =0; i < size; ++i) {
            if (i == id) {
                result.add(value);
            } else {
                result.add(false);
            }
        }
        return result;
    }

    public void booksToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(5, 0, booksButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
    public void learnToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(5, 1, learnButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
    public void historyToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(5, 2, historyButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
    public void topageToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(5, 3, topageButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
    public void settingsToggle(View v) {

    }
    public void aboutToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(5, 4, aboutButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
}
