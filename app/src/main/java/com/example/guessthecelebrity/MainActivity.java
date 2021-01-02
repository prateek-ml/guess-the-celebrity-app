package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    ImageView imageView;
    String[] answers = new String[4];
    int correctOption;
    ArrayList<Button> optionButtons = new ArrayList<Button>();

    private static final int[] BUTTON_IDS = {
            R.id.button0,
            R.id.button1,
            R.id.button2,
            R.id.button3
    };

    public void celebChosen(View view) {
        if(view.getTag().toString().equals(Integer.toString(correctOption))) {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Incorrect! Correct answer is " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }

        newQuestion();
    }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                return image;

            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();

                while(data != -1) {
                    char current  = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void newQuestion() {
        try {
            Random rand = new Random();
            chosenCeleb = rand.nextInt(celebNames.size());

            DownloadImage imageDownloader = new DownloadImage();

            Bitmap celebImage = imageDownloader.execute(celebURLs.get(chosenCeleb)).get();

            imageView.setImageBitmap(celebImage);

            correctOption = rand.nextInt(4);

            int incorrectOptions;
            for (int i = 0; i < 4; i++) {
                optionButtons.add(findViewById(BUTTON_IDS[i]));

                if (i == correctOption)
                    answers[i] = celebNames.get(chosenCeleb);
                else {
                    incorrectOptions = rand.nextInt(celebURLs.size());

                    while (incorrectOptions == chosenCeleb) {
                        rand.nextInt(celebURLs.size());
                    }
                    answers[i] = celebNames.get(incorrectOptions);
                }

                optionButtons.get(i).setText(answers[i]);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        String result = null;
        imageView = findViewById(R.id.imageView);

        try {
            Log.i("HI", "Im here");

            result = task.execute("https://www.imdb.com/list/ls052283250/").get();

            String[] splitResult = result.split("<div id=\"sidebar\">");

            Pattern p = Pattern.compile("height=\"209\"\n" +
                    "src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while(m.find()) {
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("img alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while(m.find()) {
                celebNames.add(m.group(1));
            }

            newQuestion();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}