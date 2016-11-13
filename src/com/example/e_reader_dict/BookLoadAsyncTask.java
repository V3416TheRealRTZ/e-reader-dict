package com.example.e_reader_dict;

import android.app.ProgressDialog;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.widget.LinearLayout;
import android.widget.TextView;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Kraft on 01.11.2016.
 */
class BookLoadAsyncTask extends AsyncTask<Void, Integer, Void> {
    private ProgressDialog dialog;
    private String bookPath;
    private ArrayList<String> pages;
    private EReaderActivity activity;
    private LinearLayout booksScreen;
    private TextView mainText;
    private int chapter, chapterNum;
    private String loadMessage;

    public BookLoadAsyncTask(EReaderActivity activity, String bookPath, TextView mainText, LinearLayout booksScreen, String loadMessage) {
        dialog = new ProgressDialog(activity);
        this.bookPath = bookPath;
        this.mainText = mainText;
        this.booksScreen = booksScreen;
        pages = new ArrayList<>();
        this.activity = activity;
        this.loadMessage = loadMessage;
        chapter = -1;
        chapterNum = -1;
    }

    @Override
    protected void onPreExecute() {
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage(loadMessage);
        dialog.setIndeterminate(false);
        dialog.setMax(100);
        dialog.setProgress(0);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Void result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        activity.pages = pages;
        activity.currentPage = 0;
        if (pages.size() > 0) {
            activity.gotoPage(activity.currentPage);
            //mainText.setText(pages.get(activity.currentPage), TextView.BufferType.SPANNABLE);
        }
        activity.booksToggle(null);
        activity.updatePageNumber();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (bookPath.endsWith(".txt")) {
            openTxt();
        } else if (bookPath.endsWith(".epub")) {
            openEpub();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        dialog.setProgress(progress[0]);
        if (progress[1] != -1 && progress[2] != -1) {
            dialog.setMessage(activity.getString(R.string.loadepub) + " " + progress[1] + " " + activity.getString(R.string.of) + " " + progress[2] + " " + activity.getString(R.string.topages));
        }
    }

    private void openTxt() {
        try {
            FileInputStream fIn = new FileInputStream (bookPath);
            InputStreamReader isr = new InputStreamReader(fIn);
            char[] inputBuffer = new char[fIn.available()];
            isr.read(inputBuffer);
            String str = new String(inputBuffer);
            ArrayList<String> newPages = getPages(str);
            for (String page : newPages) {
                pages.add(page);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openEpub() {
        Book currentEpub = new Book();
        EpubReader epubReader = new EpubReader();
        pages.clear();
        try {
            currentEpub = epubReader.readEpub(new FileInputStream(bookPath));
            Spine spine = new Spine(currentEpub.getTableOfContents());
            chapterNum = spine.getSpineReferences().size();
            chapter = 1;
            for (SpineReference bookSection : spine.getSpineReferences()) {
                Resource res = bookSection.getResource();
                InputStream is = res.getInputStream();
                char[] inputBuffer = new char[is.available()];
                InputStreamReader isr = new InputStreamReader(is);
                isr.read(inputBuffer);
                String nohtml = (new String(inputBuffer)).replaceAll("\\<.*?>","");
                ArrayList<String> newPages = getPages(nohtml);
                for (String page : newPages) {
                    pages.add(page);
                }
                chapter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getPages(String rawText) throws InterruptedException {
        ArrayList<String> pages = new ArrayList<>();
        int curIndex = 0;
        int maxLines = booksScreen.getHeight()/mainText.getLineHeight() - 1;
        int maxWidth = 200;
        Paint paint = new Paint();
        while (curIndex < rawText.length()) {
            int curLen = 0;
            String curPage ="";
            for (int i = 0; i < maxLines; ++i) {
                int curLineWidth = 0;
                if (curIndex + curLen >= rawText.length()) break;
                String curLine = rawText.substring(curIndex + curLen, Math.min(curIndex + curLen + curLineWidth, rawText.length() - 1));
                while (!curLine.endsWith("\n") && paint.measureText(curLine) < maxWidth && curIndex + curLen + curLineWidth < rawText.length()) {
                    curLineWidth++;
                    curLine = rawText.substring(curIndex + curLen, Math.min(curIndex + curLen + curLineWidth, rawText.length() - 1));
                    //Log.i("max", maxWidth + " " + paint.measureText(curLine));
                }
                if (curLine.contains(" ") || curLine.contains("\n")) {
                    while (!curLine.endsWith(" ") && !curLine.endsWith("\n")) {
                        curLineWidth--;
                        curLine = rawText.substring(curIndex + curLen, Math.min(curIndex + curLen + curLineWidth, rawText.length() - 1));
                    }
                }
                curLen += curLineWidth;
                publishProgress((int)(100*((float)(curIndex + curLen)/ (float)rawText.length())), chapter, chapterNum);
                if (curLine.length() > 2) curPage += curLine.substring(0, curLine.length()-1) + "\n";
                else curPage += "\n";
                if (curLine.length() == 0) break;
                //Log.i("max", maxWidth + " " + paint.measureText(curLine));
            }

            if (curPage.length() == 0) {
                break;
            }
            pages.add(curPage);
            //Log.i("Page read", pages.size() + " page size: " + pages.get(pages.size()-1).length());
            curIndex+= curLen;
        }
        return pages;
    }
}