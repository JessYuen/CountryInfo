package com.example.countryinfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class CountryDetails extends AppCompatActivity {

    private TextView name;
    private TextView capital;
    private TextView code;
    private TextView population;
    private TextView area;
    private Button btnWiki;
    private ImageView flag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_details);
        Log.i("INFO", "onCreate: got UI");

        getSupportActionBar().setTitle("Country Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = findViewById(R.id.tvLoadCN);
        capital = findViewById(R.id.tvLoadCap);
        code = findViewById(R.id.tvLoadCC);
        population = findViewById(R.id.tvLoadPop);
        area = findViewById(R.id.tvLoadArea);
        flag = findViewById(R.id.imageView);

        final String selectedCountry = getIntent().getStringExtra("country");
        new GetCountryDetails().execute(selectedCountry);

        btnWiki = findViewById(R.id.btnWiki);
        btnWiki.setText("WIKI " + selectedCountry);
        btnWiki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnWiki.setText("WIKI " + selectedCountry);
                Intent wikiIntent = new Intent(CountryDetails.this, WikiPage.class);
                wikiIntent.putExtra("countryWiki", selectedCountry);
                startActivity(wikiIntent);
            }
        });

    }

    private class GetCountryDetails extends AsyncTask<String, String, CountryInfo> {
        @Override
        protected CountryInfo doInBackground(String... params) {
            CountryInfo countryInfo = null;
            try {
                // Create URL
                String selectedCountry = params[0];
                URL webServiceEndPoint = new URL("https://restcountries.eu/rest/v2/name/" + selectedCountry);

                HttpsURLConnection myConnection = (HttpsURLConnection) webServiceEndPoint.openConnection();

                if (myConnection.getResponseCode() == 200) {
                    //JSON data has arrived successful, now we need to open a stream to it and get a reader
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");

                    // now use a JSON parser to decode data
                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginArray(); // consume array's opening JSON brace
                    String keyName;
                    countryInfo = new CountryInfo(); // nested class (see below) to carry Country Data around in
                    boolean countryFound = false;

                    while (jsonReader.hasNext() && !countryFound) { // process array of objects
                        jsonReader.beginObject(); // consume object's opening JSON brace
                        while (jsonReader.hasNext()) {
                            keyName = jsonReader.nextName();
                            if (keyName.equals("name")) {
                                countryInfo.setName(jsonReader.nextString());
                                if (countryInfo.getName().equalsIgnoreCase(selectedCountry)) {
                                    countryFound = true;
                                }
                            } else if (keyName.equals("alpha2Code")) {
                                countryInfo.setAlpha2Code(jsonReader.nextString());
                            }else if (keyName.equals("capital")) {
                                countryInfo.setCapital(jsonReader.nextString());
                            }else if (keyName.equals("population")) {
                                countryInfo.setPopulation(jsonReader.nextInt());
                            }else if (keyName.equals("area")) {
                                countryInfo.setArea(jsonReader.nextDouble());
                            } else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();

                } else {
                    Log.i("INFO", "Error: No response");
                }
            } catch (Exception e) {
                Log.i("INFO", "Error: " + e.toString());
            }

            return countryInfo;
        }

        @Override
        protected void onPostExecute(CountryInfo countryInfo) {
            super.onPostExecute(countryInfo);
            String flagUrl = "https://www.countryflags.io/" + countryInfo.getAlpha2Code() + "/flat/64.png";
            new GetFlag().execute(flagUrl);
            name.setText(countryInfo.getName());
            capital.setText(countryInfo.getCapital());
            code.setText(countryInfo.getAlpha2Code());
            population.setText(Integer.toString(countryInfo.getPopulation()));
            area.setText(Double.toString(countryInfo.getArea()));
        }
    }

    private class GetFlag extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... flagUrl) {
            try {
                java.net.URL url = new java.net.URL(flagUrl[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            flag.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class CountryInfo {
        private String name;
        private String alpha2Code;
        private String capital;
        private int population;
        private double area;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlpha2Code() {
            return alpha2Code;
        }

        public void setAlpha2Code(String alpha2Code) {
            this.alpha2Code = alpha2Code;
        }

        public String getCapital() {
            return capital;
        }

        public void setCapital(String capital) {
            this.capital = capital;
        }

        public int getPopulation() {
            return population;
        }

        public void setPopulation(int population) {
            this.population = population;
        }

        public double getArea() {
            return area;
        }

        public void setArea(double area) {
            this.area = area;
        }

    }
}
