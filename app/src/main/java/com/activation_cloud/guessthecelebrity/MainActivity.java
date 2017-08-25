package com.activation_cloud.guessthecelebrity;


import android.Manifest;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();

    int chosenCeleb = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];

    ImageView imgView;
    WebView webView;
    Button button00;
    Button button01;
    Button button02;
    Button button03;
    ProgressBar prBar;

    String RESULT="";
    DownloadTask taskUrls = null;
    DownloadImage imTask = null;

    boolean isNotConnection = false;

    public class DownloadTask extends AsyncTask<String,Integer,String>
    {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                int count =0;
                while(data != -1)
                {
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                    count++;
                    publishProgress(count);
                }
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i("INFO","Bad url convertion");

            } catch (IOException e) {
                e.printStackTrace();
                Log.i("INFO","Fail open connection");
                isNotConnection = true;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

            //Log.i("PROGRESS",progress[0].toString());
            super.onProgressUpdate(progress);

            prBar.setProgress(progress[0]);
        }

        @Override
        protected  void onPreExecute()
        {
            super.onPreExecute();
            webView.setVisibility(View.VISIBLE);
            prBar.setProgress(0);
            prBar.setVisibility(View.VISIBLE);
            imgView.setVisibility(View.INVISIBLE);
            button00.setVisibility(View.INVISIBLE);
            button01.setVisibility(View.INVISIBLE);
            button02.setVisibility(View.INVISIBLE);
            button03.setVisibility(View.INVISIBLE);
            Log.i("INFO", "onPreExecute TaskDownloader");
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(!isNotConnection) {
                InitUrlsArray();
                DownLoadImage();
                webView.setVisibility(View.INVISIBLE);
                prBar.setVisibility(View.INVISIBLE);
                imgView.setVisibility(View.VISIBLE);
                Log.i("INFO", "onPostExecute TaskDownloader");
            }
            else
                Toast.makeText(getApplicationContext(),"Please, check your access to the Internet and restart application",Toast.LENGTH_LONG).show();
        }

    }

    protected class DownloadImage extends AsyncTask<String,Integer,Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream inp = connection.getInputStream();
                Bitmap bm = BitmapFactory.decodeStream(inp);
                return bm;
            } catch (MalformedURLException e) {
                e.printStackTrace();

                isNotConnection = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("INFO","Fail open connection");
                isNotConnection = true;
            }

            return null;
        }

        @Override
        protected  void onPreExecute()
        {
            super.onPreExecute();
            webView.setVisibility(View.VISIBLE);
            imgView.setVisibility(View.INVISIBLE);
            button00.setVisibility(View.INVISIBLE);
            button01.setVisibility(View.INVISIBLE);
            button02.setVisibility(View.INVISIBLE);
            button03.setVisibility(View.INVISIBLE);

            Log.i("INFO", "onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if(!isNotConnection) {
                createNewQuestion();
                webView.setVisibility(View.INVISIBLE);
                imgView.setVisibility(View.VISIBLE);
                button00.setVisibility(View.VISIBLE);
                button01.setVisibility(View.VISIBLE);
                button02.setVisibility(View.VISIBLE);
                button03.setVisibility(View.VISIBLE);
                Log.i("INFO", "onPostExecute");
            }
            else
                Toast.makeText(getApplicationContext(),"Please, check your access to the Internet and restart application",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.activation_cloud.guessthecelebrity.R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(canMakeSmores()) {
            int RESULT = 105;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //requestPermissions(new String[]{Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE}, RESULT);
                ActivityCompat.requestPermissions (this,
                        new String [] {Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE},RESULT);
                //Toast.makeText(this,"Add permision",Toast.LENGTH_LONG).show();
            }
            //

        }

        webView = (WebView)findViewById(com.activation_cloud.guessthecelebrity.R.id.webView);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadUrl("file:///android_res/raw/loader.html");

        imgView = (ImageView)findViewById(com.activation_cloud.guessthecelebrity.R.id.imageView);
        imgView.setVisibility(View.INVISIBLE);
        button00 = (Button)findViewById(com.activation_cloud.guessthecelebrity.R.id.button00);
        button00.setOnClickListener(this);
        button01 = (Button)findViewById(com.activation_cloud.guessthecelebrity.R.id.button01);
        button01.setOnClickListener(this);
        button02 = (Button)findViewById(com.activation_cloud.guessthecelebrity.R.id.button02);
        button02.setOnClickListener(this);
        button03 = (Button)findViewById(com.activation_cloud.guessthecelebrity.R.id.button03);
        button03.setOnClickListener(this);

        prBar = (ProgressBar)findViewById(R.id.progressBar);
        prBar.setMax(61885);
        InitUrlsResult();

    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();

    }

    private void InitUrlsArray()
    {
        if(taskUrls == null)  return;
        try {
            RESULT = taskUrls.get();
            //System.out.println(result);
            if(RESULT =="") return;
            String[] splitR = RESULT.split("<div class=\"sidebarContainer\">");
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitR[0]);

            while (m.find())
            {
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitR[0]);

            while(m.find())
            {
                celebNames.add(m.group(1));
            }

            Log.i("INFO",celebURLs.toString());
            Log.i("INRO",celebNames.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
    private void InitUrlsResult() {
        taskUrls = new DownloadTask();
        taskUrls.execute("http://posh24.se/kandisar");
    }

    private void DownLoadImage()
    {
        imTask = new DownloadImage();
        Random random = new Random();
        chosenCeleb = random.nextInt(celebURLs.size());
        imTask.execute(celebURLs.get(chosenCeleb));
    }

    private void createNewQuestion()
    {
        Random random = new Random();
        //chosenCeleb = random.nextInt(celebURLs.size());
        //DownloadImage imTask = new DownloadImage();
        Bitmap celebIm;
        HashSet<Integer> tmp_set =  new HashSet<Integer>();

        try {
            Log.i("INFO",celebURLs.get(chosenCeleb));
            celebIm = imTask.get();
            imgView.setImageBitmap(celebIm);
            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation = 0;

            for(int i = 0; i < 4; i++)
            {
                if(i == locationOfCorrectAnswer)
                {
                    answers[i]= celebNames.get(chosenCeleb);
                }
                else
                {
                    boolean find = false;
                    int attempt = 0;
                    while (!find && attempt <100) {
                        Random tmp_rand = new Random();

                        incorrectAnswerLocation = tmp_rand.nextInt(celebURLs.size());

                        if (incorrectAnswerLocation != chosenCeleb) {
                            if (tmp_set.add(incorrectAnswerLocation)) {
                                find = true;

                            }
                        }
                        attempt++;
                    }

                    if(attempt >= 100)
                    {
                        Random tmp_rand = new Random();
                        incorrectAnswerLocation = tmp_rand.nextInt(celebURLs.size());
                        while(incorrectAnswerLocation == chosenCeleb) {
                            incorrectAnswerLocation = tmp_rand.nextInt(celebURLs.size());
                        }
                    }
                    answers[i]=celebNames.get(incorrectAnswerLocation);
                }
            }

            button00.setText(answers[0]);
            button01.setText(answers[1]);
            button02.setText(answers[2]);
            button03.setText(answers[3]);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    private boolean canMakeSmores() {
        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @Override
    public void onClick(View view) {

        if(view == null)
            return;

        int id = view.getId();

        switch (id)
        {
            case com.activation_cloud.guessthecelebrity.R.id.button00:
            case com.activation_cloud.guessthecelebrity.R.id.button01:
            case com.activation_cloud.guessthecelebrity.R.id.button02:
            case com.activation_cloud.guessthecelebrity.R.id.button03:
                Button tmp = (Button)view;
                if(answers[locationOfCorrectAnswer] == tmp.getText().toString())
                {
                    Toast.makeText(this,"Correct! Is "+ answers[locationOfCorrectAnswer],Toast.LENGTH_SHORT).show();

                }
                else {
                    Toast.makeText(this,"Incorrect! Is "+ answers[locationOfCorrectAnswer],Toast.LENGTH_SHORT).show();

                }
                break;
        }

        DownLoadImage();

    }
}
