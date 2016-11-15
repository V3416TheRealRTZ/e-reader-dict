package com.example.e_reader_dict;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WordTranslateAsyncTask extends AsyncTask<String, Void, String> {
    EReaderActivity context;
    String input = "";
    String result = "";
    TextView resultText;
    LinearLayout translatingScreen, resultScreen;

    public WordTranslateAsyncTask(EReaderActivity context, TextView resultText, LinearLayout translatingScreen, LinearLayout resultScreen) {
        super();
        this.context = context;
        this.resultText = resultText;
        this.translatingScreen = translatingScreen;
        this.resultScreen = resultScreen;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strs) {
        input = strs[1];
        JSONObject json = loadJSON(strs[0], strs[1]);
        try {
            result = json.getJSONArray("text").getString(0);
            //URLEncoder.encode(result, "UTF-8");
            //result.replace(" ", "%20");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public JSONObject loadJSON(String url, String word) {

        JSONParser jParser = new JSONParser();
        // здесь параметры необходимые в запрос добавляем
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("key", context.getString(R.string.yandex_key)));
        params.add(new BasicNameValuePair("lang", "en-ru"));
        params.add(new BasicNameValuePair("text", word));
        // посылаем запрос методом GET
        JSONObject json = jParser.makeHttpRequest(url, "GET", params);

        return json;
    }

    @Override
    protected void onPostExecute(String str) {
        // если какой-то фейл, проверяем на null
        // фейл может быть по многим причинам: сервер сдох, нет сети на устройстве и т.д.
        Log.d("result: ", result);
        resultText.setText(result);
        translatingScreen.setVisibility(View.GONE);
        resultScreen.setVisibility(View.VISIBLE);
        context.addWord(input, result);
    }

}