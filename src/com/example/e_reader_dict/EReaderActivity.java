package com.example.e_reader_dict;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.*;
import java.util.ArrayList;

enum BookType {TXT, EPUB}

public class EReaderActivity extends Activity {
    private LinearLayout booksScreen, learnScreen, historyScreen, topageScreen, aboutScreen;
    private ImageButton booksButton, learnButton, historyButton, topageButton, aboutButton;
    private TextView pageData;
    private FileChooser fileChooser;
    private String programDirectory;
    private ListView booksList;
    private ArrayList<String> books;
    private ArrayList<String> booksPaths;
    private ArrayList<BookType> bookTypes;
    private Book currentEpub;
    private String currentTxt;
    private ArrayList<String> pages;
    private int currentPage;
    private int currentBookId;
    private TextView mainText;
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
        mainText = (TextView) findViewById(R.id.mainTextView);
        booksList = (ListView) findViewById(R.id.booksList);
        pages = new ArrayList<>();
        books = new ArrayList<>();
        booksPaths = new ArrayList<>();
        bookTypes = new ArrayList<>();
        createProgramDirectoryIfDoesntExist();
        addBook(null);
        currentEpub = null;
        currentTxt = null;
        currentBookId = -1;
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
        //Log.i("File Reading stuff", programDirectory);
        //fileChooser.
    }

    private boolean isTooLarge (TextView text, String newText) {
        float textWidth = text.getPaint().measureText(newText);
        return (textWidth >= text.getMeasuredWidth ());
    }

    private ArrayList<String> getPages(char [] buffer) {
        String rawText = new String(buffer);
        int curIndex = 0;
        ArrayList<String> pages = new ArrayList<>();
        while (curIndex < rawText.length()) {
            int curLen = 500;
            while (curIndex + curLen < rawText.length() &&  !isTooLarge(mainText, rawText.substring(curIndex, curIndex + curLen))) {
                curLen+=10;
            }
            pages.add(rawText.substring(curIndex, Math.min(curIndex + curLen, rawText.length()-1)));
            curIndex += curLen;
        }
        return pages;
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

    private void openTxt(int id) {
        currentTxt = "";
        currentEpub = null;
        pages.clear();
        try {
            FileInputStream fIn = new FileInputStream (booksPaths.get(currentBookId));
            InputStreamReader isr = new InputStreamReader(fIn);
            char[] inputBuffer = new char[fIn.available()];
            isr.read(inputBuffer);
            ArrayList<String> newPages = getPages(inputBuffer);
            for (String page : newPages) {
                pages.add(page);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPage = 0;
        mainText.setText(pages.get(currentPage));
        booksToggle(null);
    }

    private void flipPage() {
        gotoPage(currentPage+1);
    }

    private void backFlipPage() {
        gotoPage(currentPage-1);
    }

    private void gotoPage(int pageNumber) {
        if (pageNumber >= 0 && pageNumber < pages.size()) {
            currentPage = pageNumber;
            mainText.setText(pages.get(currentPage));
        } else {
            //Toast toast = new Toast("END");
        }
    }

    private void openEpub(int id) {
        currentTxt = null;
        currentEpub = new Book();
        EpubReader epubReader = new EpubReader();
        pages.clear();
        try {
            currentEpub = epubReader.readEpub(new FileInputStream(booksPaths.get(id)));
            Spine spine = new Spine(currentEpub.getTableOfContents());
            for (SpineReference bookSection : spine.getSpineReferences()) {
                Resource res = bookSection.getResource();
                InputStream is = res.getInputStream();
                char[] inputBuffer = new char[is.available()];
                InputStreamReader isr = new InputStreamReader(is);
                isr.read(inputBuffer);
                ArrayList<String> newPages = getPages(inputBuffer);
                for (String page : newPages) {
                    pages.add(page);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPage = 0;
        mainText.setText(pages.get(currentPage));
        booksToggle(null);
        //Book book = op
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
        booksList.setAdapter(new ArrayAdapter<String>(this, R.layout.book_entry_layout, books));
        booksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.i("book id", "id: " + id);
                currentBookId = (int)id;
                if (bookTypes.get((int)id) == BookType.TXT) {
                    openTxt((int)id);
                } else {
                    openEpub((int)id);
                }
            }
        });
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
