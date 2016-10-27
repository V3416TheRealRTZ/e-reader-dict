package com.example.e_reader_dict;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.util.ArrayList;

public class EReaderActivity extends Activity {
    private LinearLayout booksScreen, learnScreen, historyScreen, topageScreen, aboutScreen;
    private ImageButton booksButton, learnButton, historyButton, topageButton, aboutButton;
    private TextView pageData;
    private FileChooser fileChooser;
    private String programDirectory;
    private ListView booksList;
    private ArrayList<String> books;
    private ArrayList<String> booksPaths;
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
        booksList = (ListView) findViewById(R.id.booksList);
        books = new ArrayList<>();
        booksPaths = new ArrayList<>();
        createProgramDirectoryIfDoesntExist();
        addBook(null);
        //Log.i("File Reading stuff", programDirectory);
        //fileChooser.
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

    private void createProgramDirectoryIfDoesntExist() {
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "e-reader-dict");
        directory.mkdirs();
        programDirectory = directory.getPath();
    }

    /*private void resetBooksFile() {

    }*/

    private void addBook(String newBookFilePath) {
        File booksFile = new File(programDirectory + File.separator + "books");
        String readString = "";
        booksPaths.clear();
        try {
            if (booksFile.exists()) {

                FileInputStream fIn = new FileInputStream (booksFile);//openFileInput("books.txt");
                InputStreamReader isr = new InputStreamReader(fIn);
                BufferedReader reader = new BufferedReader(isr);
                String line = reader.readLine();
                do {
                    readString += line + '\n';
                    Log.i("Path", "path: " + line + '\n');
                    if (!booksPaths.contains(line)) {
                        booksPaths.add(line);
                    }
                    line = reader.readLine();
                    /**/
                } while (line != null);
                /*
                char[] inputBuffer = new char[10000];
                isr.read(inputBuffer);
                readString = new String(inputBuffer);*/
            }
            if (newBookFilePath != null) {
                FileOutputStream fOut = new FileOutputStream(new File(booksFile.getPath()));//openFileOutput("books.txt", MODE_WORLD_READABLE);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                osw.write(readString + newBookFilePath + '\n');
                booksPaths.add(newBookFilePath);
                Log.i("File Reading stuff", "read string: " + readString + newBookFilePath + '\n');
                osw.flush();
                osw.close();
            }
        } catch (IOException ioe)
        {ioe.printStackTrace();}
        updateBooks();
    }

    private void updateBooks() {
        books.clear();
        for (String bookPath : booksPaths) {
            if (bookPath != null) {
                String words[] = bookPath.split(File.separator);
                String name = words[words.length - 1];
                /*if (name.endsWith(".txt")) {
                    name = name.substring(0, name.length() - 4);
                } else {
                    name = name.substring(0, name.length() - 5);
                }*/
                books.add(name);
            }
        }
        booksList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, books));
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

    public void chooseBookFile(View v) {
        fileChooser = new FileChooser(this);
        ArrayList<String> okExtensions = new ArrayList<>();
        okExtensions.add(".txt");
        okExtensions.add(".epub");
        fileChooser.setExtensions(okExtensions);
        fileChooser.showDialog();
        fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                if (file != null) {
                    addBook(file.getPath());
                }
            }
        });
    }
}
