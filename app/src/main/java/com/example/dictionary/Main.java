package com.example.dictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ch09_dictionary1.R;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.toolbox.Loger;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main extends AppCompatActivity implements OnClickListener, TextWatcher
{//数据库
    private String DATABASE_PATH;
    private AutoCompleteTextView actvWord;
    private final String DATABASE_FILENAME = "dictionary.db";
    private SQLiteDatabase database;
    private Button btnSelectWord;
    private TextView mean;
    private EditText in;
    private Button sure;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        DATABASE_PATH = this.getFilesDir() + "/dictionary";
        database = openDatabase();
        btnSelectWord = (Button) findViewById(R.id.btnSelectWord);
        actvWord = (AutoCompleteTextView) findViewById(R.id.actvWord);
        btnSelectWord.setOnClickListener(this);
        actvWord.addTextChangedListener(this);
        System.out.println(DATABASE_PATH);
      //  Log（"sss","ss"）;
        initView();
    }


    private void initView() {
        mean = (TextView) findViewById(R.id.mean);
        in = (EditText) findViewById(R.id.in);
        sure = (Button) findViewById(R.id.sure);

        sure.setOnClickListener(new View.OnClickListener() {


                                    @Override
                                    public void onClick(View v) {
                                        String s = in.getText().toString();
                                        if (s == null) {
                                            Toast.makeText(getApplicationContext(), "输入框不能为空", Toast.LENGTH_LONG).show();
                                        }
                                        String a = "http://fanyi.youdao.com/openapi.do?keyfrom=cyy123456&key=953341210&type=data&doctype=json&version=1.1&q=" + s;
                                        RxVolley.get(a, new HttpCallback() {
                                            public void onSuccess(String t) {
                                                Loger.debug("请求到的数据：" + t);
                                                Log.e("aaa", "bbb");
                                                eJson(t);
                                                Log.e("aaa", "bbb");
                                            }
                                        });
                                    }
                                }
        );


    }


    private void eJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject object = jsonObject.getJSONObject("basic");
            String s = "美式发音" + object.getString("us-phonetic") + "\n" + "英式发音" + object.getString("uk-phonetic") + "\n" + "释义" + "\n" + object.getString("explains") + "\n" + "网络释义" + "\n";
            JSONArray ja = jsonObject.getJSONArray("web");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jsonObject3 = (JSONObject) ja.get(i);
                s = s + jsonObject3.getString("value") + "\n";
                s = s + jsonObject3.getString("key") + "\n";
            }
            mean.setText(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public class DictionaryAdapter extends CursorAdapter//s
    {
        private LayoutInflater layoutInflater;

        @Override
        public CharSequence convertToString(Cursor cursor)
        {
            return cursor == null ? "" : cursor.getString(cursor
                    .getColumnIndex("_id"));
        }

        private void setView(View view, Cursor cursor)
        {
            TextView tvWordItem = (TextView) view;
            tvWordItem.setText(cursor.getString(cursor.getColumnIndex("_id")));
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor)
        {
            setView(view, cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            View view = layoutInflater.inflate(R.layout.word_list_item, null);
            setView(view, cursor);
            return view;
        }

        public DictionaryAdapter(Context context, Cursor c, boolean autoRequery)
        {
            super(context, c, autoRequery);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    @Override
    public void afterTextChanged(Editable s)
    {

        Cursor cursor = database.rawQuery(
                "select english as _id from t_words where english like ?",
                new String[]
                        { s.toString() + "%" });

        DictionaryAdapter dictionaryAdapter = new DictionaryAdapter(this,
                cursor, true);
        actvWord.setAdapter(dictionaryAdapter);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View view)//s
    {
        String sql = "select chinese from t_words where english=?";
        Cursor cursor = database.rawQuery(sql, new String[]
                { actvWord.getText().toString() });
        String result = "未找到该单词.";
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            result = cursor.getString(cursor.getColumnIndex("chinese"));
        }
        new AlertDialog.Builder(this).setTitle("查询结果").setMessage(result)
                .setPositiveButton("关闭", null).show();

    }

    private SQLiteDatabase openDatabase()//s
    {
        try
        {
            String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
            File dir = new File(DATABASE_PATH);
            if (!dir.exists())
                dir.mkdir();
            if (!(new File(databaseFilename)).exists())
            {
                InputStream is = getResources().openRawResource(
                        R.raw.dictionary);
                FileOutputStream fos = new FileOutputStream(databaseFilename);
                byte[] buffer = new byte[8192];
                int count = 0;
                while ((count = is.read(buffer)) > 0)
                {
                    fos.write(buffer, 0, count);
                }

                fos.close();
                is.close();
            }
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
                    databaseFilename, null);
            return database;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}