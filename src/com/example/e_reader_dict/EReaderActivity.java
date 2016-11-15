package com.example.e_reader_dict;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.*;
import nl.siegmann.epublib.domain.Book;

import java.io.*;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;

enum BookType {TXT, EPUB}

public class EReaderActivity extends Activity {
    private LinearLayout booksScreen, learnScreen, historyScreen, topageScreen, aboutScreen, settingsScreen, mainScreen;
    private ImageButton booksButton, learnButton, historyButton, topageButton, settingsButton;
    Button aboutButton, goButton;
    private TextView pageData;
    private FileChooser fileChooser;
    private String programDirectory;
    private ListView booksList, historyList;
    private ArrayList<String> books;
    private ArrayList<String> engWords;
    private ArrayList<String> rusWords;
    private ArrayList<Integer> learnIds;
    private ArrayList<Integer> learnLevels;
    private ArrayList<Integer> learnDates;
    private ArrayList<String> booksPaths;
    private ArrayList<BookType> bookTypes;
    private Book currentEpub;
    private String currentTxt;
    public ArrayList<String> pages;
    protected int currentPage;
    private int currentBookId;
    private TextView mainText, topageText;
    private ProgressDialog progressDialog;
    private Handler handler;
    private int mainLines;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mainScreen = (LinearLayout)findViewById(R.id.mainScreen);
        booksScreen = (LinearLayout)findViewById(R.id.booksScreen);
        booksButton = (ImageButton) findViewById(R.id.booksButton);
        learnScreen = (LinearLayout)findViewById(R.id.learnScreen);
        learnButton = (ImageButton) findViewById(R.id.learnButton);
        historyScreen = (LinearLayout)findViewById(R.id.historyScreen);
        historyButton = (ImageButton) findViewById(R.id.historyButton);
        topageScreen = (LinearLayout)findViewById(R.id.topageScreen);
        topageButton = (ImageButton) findViewById(R.id.topageButton);
        topageText = (TextView) findViewById(R.id.topageText);
        goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = topageText.getText().toString();
                Integer pageNumber = Integer.parseInt(str);
                if (pageNumber != null) {
                    gotoPage(pageNumber - 1);
                    topageToggle(null);
                }
            }
        });
        settingsScreen = (LinearLayout)findViewById(R.id.settingsScreen);
        aboutScreen = (LinearLayout)findViewById(R.id.aboutScreen);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        aboutButton = (Button) findViewById(R.id.aboutButton);
        pageData = (TextView) findViewById(R.id.pageData);
        mainText = (TextView) findViewById(R.id.mainTextView);
        booksList = (ListView) findViewById(R.id.booksList);
        historyList = (ListView) findViewById(R.id.historyList);
        pages = new ArrayList<>();
        books = new ArrayList<>();
        booksPaths = new ArrayList<>();
        bookTypes = new ArrayList<>();
        createProgramDirectoryIfDoesntExist();
        addBook(null);
        engWords = new ArrayList<>();
        rusWords = new ArrayList<>();
        learnIds = new ArrayList<>();
        addWord(null, null);
        currentEpub = null;
        currentTxt = null;
        currentBookId = -1;
        mainText.setMovementMethod(LinkMovementMethod.getInstance());
        mainText.setOnTouchListener(new OnSwipeTouchListener(EReaderActivity.this) {
            public void onSwipeTop() {
                //Toast.makeText(EReaderActivity.this, "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
                backFlipPage();
            }
            public void onSwipeLeft() {
                flipPage();
            }
            public void onSwipeBottom() {
                //Toast.makeText(EReaderActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });
        mainLines = 1;
        pageData.setText("");
        //Log.i("File Reading stuff", programDirectory);
        //fileChooser.

    }

    private ClickableSpan getClickableSpan(final String word) {
        return new ClickableSpan() {
            final String mWord;
            {
                mWord = word;
            }

            @Override
            public void onClick(View widget) {
                Log.d("tapped on:", mWord);
                TranslateDialog translateDialog = new TranslateDialog(EReaderActivity.this, mWord);
                translateDialog.show();
                //Toast.makeText(widget.getContext(), mWord, Toast.LENGTH_SHORT).show();
            }

            public void updateDrawState(TextPaint ds) {
                //super.updateDrawState(ds);
            }
        };
    }

    private void updateMenu(LinearLayout screen, View v, Boolean value) {
        if (value) {
            screen.setVisibility(View.VISIBLE);
            v.setAlpha(0.5f);
        } else {
            screen.setVisibility(View.GONE);
            v.setAlpha(1.0f);
        }
    }

    private void updateMenus(ArrayList<Boolean> menus) {
        updateMenu(booksScreen, booksButton, menus.get(0));
        updateMenu(learnScreen, learnButton, menus.get(1));
        updateMenu(historyScreen, historyButton, menus.get(2));
        updateMenu(topageScreen, topageButton, menus.get(3));
        updateMenu(aboutScreen, aboutButton, menus.get(4));
        updateMenu(settingsScreen, settingsButton, menus.get(5));
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
                    booksPaths.add(line);
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
                if (!booksPaths.contains(newBookFilePath)) {
                    osw.write(readString + newBookFilePath + '\n');
                    booksPaths.add(newBookFilePath);
                } else {
                    osw.write(readString);
                    Toast.makeText(EReaderActivity.this, "already added", Toast.LENGTH_SHORT).show();
                }
                Log.i("File Reading stuff", "read string: " + readString + newBookFilePath + '\n');
                osw.flush();
                osw.close();
            }
        } catch (IOException ioe)
        {ioe.printStackTrace();}
        updateBooks();
    }

    protected void addWord(String engWord, String rusWord) {
        File wordsFile = new File(programDirectory + File.separator + "words");
        String readString = "";
        engWords.clear();
        rusWords.clear();
        learnIds.clear();
        try {
            if (wordsFile.exists()) {
                FileInputStream fIn = new FileInputStream (wordsFile);//openFileInput("books.txt");
                InputStreamReader isr = new InputStreamReader(fIn);
                BufferedReader reader = new BufferedReader(isr);
                String line = reader.readLine();
                do {
                    readString += line + '\n';
                    engWords.add(line);
                    line = reader.readLine();
                    readString += line + '\n';
                    rusWords.add(line);
                    line = reader.readLine();
                    readString += line + '\n';
                    if (line.contains("+")) {
                        learnIds.add(engWords.size()-1);
                    }
                    line = reader.readLine();
                } while (line != null);

            }
            if (engWord != null && rusWord != null) {
                FileOutputStream fOut = new FileOutputStream(new File(wordsFile.getPath()));
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                if (!engWords.contains(engWord)) {
                    osw.write(readString + engWord + '\n' + rusWord + '\n' + "-" + '\n');
                    engWords.add(engWord);
                    rusWords.add(rusWord);
                } else {
                    String isLearning = "-";
                    if (learnIds.contains(engWords.indexOf(engWord))) {
                        isLearning = "+";
                    }
                    osw.write(readString + engWord + '\n' + rusWord + '\n' + isLearning + '\n');
                }
                osw.flush();
                osw.close();
            }
        } catch (IOException ioe)
        {ioe.printStackTrace();}
        updateWords();
    }

    protected void updatePageNumber() {
        pageData.setText(getString(R.string.page) + " " + (currentPage + 1) + "/" + pages.size());
    }

    private void flipPage() {
        gotoPage(currentPage+1);
    }

    private void backFlipPage() {
        gotoPage(currentPage-1);
    }

    protected void gotoPage(int pageNumber) {
        if (pageNumber >= 0 && pageNumber < pages.size()) {
            currentPage = pageNumber;
            mainText.setText(pages.get(currentPage), TextView.BufferType.SPANNABLE);
            updatePageNumber();

            Spannable spans = (Spannable) mainText.getText();
            BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
            iterator.setText(pages.get(currentPage));
            int start = iterator.first();
            for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                    .next()) {
                String possibleWord = pages.get(currentPage).substring(start, end);
                if (Character.isLetter(possibleWord.charAt(0))) {
                    ClickableSpan clickSpan = getClickableSpan(possibleWord);
                    spans.setSpan(clickSpan, start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

        } else {
            Toast.makeText(EReaderActivity.this, getString(R.string.nopage), Toast.LENGTH_SHORT).show();
            //Toast toast = new Toast("END");
        }
    }

    private void updateBooks() {
        books.clear();
        bookTypes.clear();
        for (String bookPath : booksPaths) {
            if (bookPath != null) {
                String words[] = bookPath.split(File.separator);
                String name = words[words.length - 1];
                if (name.endsWith(".txt")) {
                    bookTypes.add(BookType.TXT);
                    //name = name.substring(0, name.length() - 4);
                } else {
                    bookTypes.add(BookType.EPUB);
                    //name = name.substring(0, name.length() - 5);
                }
                books.add(name);
            }
        }
        booksList.setAdapter(new ArrayAdapter<String>(this, R.layout.book_entry, books));
        booksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.i("book id", "id: " + id);
                currentBookId = (int)id;
                BookLoadAsyncTask bookLoadAsyncTask = new BookLoadAsyncTask(EReaderActivity.this, booksPaths.get((int)id), mainText, booksScreen, getString(R.string.loadtxt));
                bookLoadAsyncTask.execute();
            }
        });
    }

    private void updateWords() {
        historyList.setAdapter(new WordsHistoryAdapter(this, engWords, rusWords, learnIds));
        /*historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                learnIds.add(position);
                addWord(null, null);
            }
        });*/

    }

    public void booksToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(6, 0, booksButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
    public void learnToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(6, 1, learnButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
    public void historyToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(6, 2, historyButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
    public void topageToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(6, 3, topageButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
    public void settingsToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(6, 5, settingsButton.getAlpha() != 0.5f);
        updateMenus(menus);
    }
    public void aboutToggle(View v) {
        ArrayList<Boolean> menus = formMenusArray(6, 4, aboutScreen.getVisibility() != View.VISIBLE);
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

    public void chooseWord(View v) {

    }
}
