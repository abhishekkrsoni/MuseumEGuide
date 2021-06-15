package com.example.abhishek.museumeguide;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Global variables declaration
    EditText etname, etpass;
    ImageButton btlogin;
    TextView ttsignup;
    Spinner spinnerLanguage;
    MySQLiteQuery mySQLiteQuery;
    public static String language="English";
    ArrayAdapter<CharSequence> adapterLanguage;
    int a_id;
    String a_name,a_desc,gal_id,m_id;
    public static boolean PARSE_FLAG=false,ENGLISH_FLAG=false,HINDI_FLAG=false, JAPANESE_FLAG=false;

    String[] column = {"artical_id", "gallery_id", "artical_name", "artical_description","media_id"};

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        etname = (EditText) findViewById(R.id.etname);
        etpass = (EditText) findViewById(R.id.etpass);
        btlogin = (ImageButton) findViewById(R.id.btlogin);
        ttsignup = (TextView) findViewById(R.id.ttsignup);
        btlogin.setOnClickListener(this);
        mySQLiteQuery=new MySQLiteQuery(this);
        spinnerLanguage = (Spinner) findViewById(R.id.spinner_lang);

        adapterLanguage = ArrayAdapter.createFromResource(this, R.array.array_language_name, android.R.layout.simple_spinner_item);//category_names is array define strings.xml
        adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        try {
            spinnerLanguage.setAdapter(adapterLanguage);  //setting the array adapter to sppiner
            //spinnerLanguage.setBackground(R.drawable.regimg);
            spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Toast.makeText(getBaseContext(),parent.getItemAtPosition(position)+" is selected",Toast.LENGTH_LONG);
                    language = parent.getItemAtPosition(position).toString();

                    //Toast.makeText(getApplicationContext(), "Selected language is " + language, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Exception e) {
        }

    }

    //for language spinner
    private boolean validateEmailPass(String email, String pass) {

        matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            Toast.makeText(this, "Please Enter Name / Email", Toast.LENGTH_LONG).show();
            return false;
        } else if (pass.length() < 8) {
            Toast.makeText(this, "Password should be of minimum 8 characters", Toast.LENGTH_LONG).show();
            return false;
        } /*else if (name.indexOf('@') == -1) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_LONG).show();
            return false;
        } else if (name.indexOf('.') == -1) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_LONG).show();
            return false;
        }*/
        return true;
    }

    public void OnSignup(View view) {
        //Toast.makeText(this, "Signup clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {

        //Toast.makeText(this, "Logging In Clicked....", Toast.LENGTH_SHORT).show();
        String email = etname.getText().toString();
        // String email = etname.getText().toString();
        String pass = etpass.getText().toString();
        //Invoking the Email Validation
        boolean flag = validateEmailPass(email, pass);
        //Toast.makeText(this,"Logging In....",Toast.LENGTH_LONG).show();

        //If validated
        if (flag == true) {
            //Toast.makeText(getApplicationContext(),language,Toast.LENGTH_LONG).show();
            if(language.equalsIgnoreCase("English")){
                ENGLISH_FLAG=true;
                HINDI_FLAG=false;
                JAPANESE_FLAG=false;
                jsonDecoder("http://"+MainActivity.IPV4_URL+"/myapp/artical_in_english.php", "artical_table_eng","English");

            }else if(language.equalsIgnoreCase("Hindi")){
                ENGLISH_FLAG=false;
                HINDI_FLAG=true;
                JAPANESE_FLAG=false;
                jsonDecoder("http://"+MainActivity.IPV4_URL+"/myapp/artical_in_hindi.php", "artical_table_hindi","hindi");

            }else if(language.equalsIgnoreCase("Japanese")){
                ENGLISH_FLAG=false;
                HINDI_FLAG=false;
                JAPANESE_FLAG=true;
                jsonDecoder("http://"+MainActivity.IPV4_URL+"/myapp/artical_in_japanese.php", "artical_table_japanese","japanese");

            }
            String type = "login";
            BackgroundWorker backgroundWorker = new BackgroundWorker(this);
            backgroundWorker.execute(type, email, pass);
            Intent toMainActivity=new Intent(this,MenuActivity.class);
            startActivity(toMainActivity);

        }

    }

    //JsonDecoder is used to parse the json file from server

    public void jsonDecoder(String phpFile, final String arrayName,final String language){

        final RequestQueue rque;
        rque = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.GET, phpFile , null, new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                try {
                    SQLiteDatabase writeDatabase = mySQLiteQuery.getWritableDatabase();
                    ContentValues cntval = new ContentValues();
                    JSONArray ja = response.getJSONArray(arrayName);
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = ja.getJSONObject(i);
                        a_id = jo.getInt("artical_id");
                        gal_id = jo.getString("gallery_id");
                        a_name = jo.getString("artical_name");
                        a_desc = jo.getString("artical_description");
                        m_id = jo.getString("media_id");
                        cntval.put("artical_id", a_id);
                        cntval.put("gallery_id", gal_id);
                        cntval.put("artical_name", a_name);
                        cntval.put("artical_description", a_desc);
                        cntval.put("media_id", m_id);
                        //writeDatabase = mySQLiteQuery.getWritableDatabase();


                        if(language.equalsIgnoreCase("English")){
                            ENGLISH_FLAG=true;
                            HINDI_FLAG=false;
                            JAPANESE_FLAG=false;
                            writeDatabase.insert("e_artical_table", null, cntval);
                        }else if(language.equalsIgnoreCase("Hindi")){
                            ENGLISH_FLAG=false;
                            HINDI_FLAG=true;
                            JAPANESE_FLAG=false;
                            writeDatabase.insert("h_artical_table", null, cntval);
                        } else if(language.equalsIgnoreCase("Japanese")){
                            ENGLISH_FLAG=false;
                            HINDI_FLAG=false;
                            JAPANESE_FLAG=true;
                            writeDatabase.insert("j_artical_table", null, cntval);
                        }
                        //PARSE_FLAG=true;
                        // Toast.makeText(getApplicationContext(),"Eflag:"+ENGLISH_FLAG+"HFlag:"+HINDI_FLAG+"language:"+language, Toast.LENGTH_SHORT).show();
                    }
                    writeDatabase.close();

                } catch (JSONException e) {
                    //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "ERROR to connecting volley " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        );rque.add(jsonObjRequest);
    }

}