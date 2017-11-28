package edu.tjhsst.apilab;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import static android.net.Uri.encode;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private TextView timeDisplay;
    private EditText search;
    private Button button;
    private String url;
    private String sunset_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BackgroundService.class));

        timeDisplay = (TextView)findViewById(R.id.time_text);
        search = (EditText)findViewById(R.id.search_edit);
        button = (Button)findViewById(R.id.search_btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = search.getText().toString();
                String YQL = String.format("select astronomy.sunset from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\")", city);
                //String YQL = "select%20astronomy.sunset%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22"+ city +"%22)&format=json";
                String endpoint = String.format("http://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));

                url = endpoint;
                /*
                try {
                    url = new URL(endpoint);
                }catch (IOException e){
                    throw new RuntimeException(e);
                }*/
                new getTime().execute();

            }
        });

    }

    private void createNotification(int nId, String title, String body) {
        NotificationCompat.Builder mBuilder =
                (android.support.v7.app.NotificationCompat.Builder)new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(title)
                        .setContentText(body);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(nId, mBuilder.build());
    }

    private int[] getHours(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC−4:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("KK:mm");
        int currentHour = cal.get(Calendar.HOUR);
        int currentMinute = cal.get(Calendar.MINUTE);
        int[] toRet = {currentHour, currentMinute};
        return toRet;
    }

    private class getTime extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);
            //System.out.print(jsonStr);
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONObject queryResults = jsonObj.optJSONObject("query");
                JSONObject channelJSON = queryResults.optJSONObject("results").optJSONObject("channel");
                JSONObject time = channelJSON.getJSONObject("astronomy");
                sunset_time = time.getString("sunset");
                System.out.println();
                System.out.println(sunset_time);
                System.out.println();
            }catch (JSONException e){
                throw new RuntimeException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String text = "Sunset time: " + sunset_time;
            timeDisplay.setText(text);
        }

    };
    public class BackgroundService extends Service
    {
        public Context context = this;
        public Handler handler = null;
        public Runnable runnable = null;

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();

            handler = new Handler();
            runnable = new Runnable() {
                public void run() {

                    Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                    handler.postDelayed(runnable, 10000);
                }
            };

            handler.postDelayed(runnable, 15000);
        }

        @Override
        public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
            //handler.removeCallbacks(runnable);
            Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStart(Intent intent, int startid) {
            Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        }
    }



}
