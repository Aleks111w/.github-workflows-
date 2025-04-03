package com.example.militaryaircraft;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ListView aircraftListView;
    private EditText searchInput;
    private List<String> aircraftData;
    private ArrayAdapter<String> adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustScreenSettings();
        setContentView(R.layout.activity_main);
        
        setupFilters();
        setupAircraftList();
        setupSearch();
        new FetchAircraftData().execute();
    }
    
    private void adjustScreenSettings() {
        // Автоматическое переключение ориентации экрана
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        if (width > height) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        // Подстройка под разрешение экрана
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
    
    private void setupFilters() {
        Spinner yearSpinner = findViewById(R.id.yearSpinner);
        Spinner countrySpinner = findViewById(R.id.countrySpinner);
        Spinner typeSpinner = findViewById(R.id.typeSpinner);
        
        String[] years = {"1900-1950", "1951-2000", "2001-2050"};
        String[] countries = {"США", "Россия", "Китай", "Франция", "Германия"};
        String[] types = {"Истребитель", "Бомбардировщик", "Штурмовик", "Разведчик"};
        
        yearSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
        countrySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries));
        typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
    }
    
    private void setupAircraftList() {
        aircraftListView = findViewById(R.id.aircraftListView);
        aircraftData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, aircraftData);
        aircraftListView.setAdapter(adapter);
    }
    
    private void setupSearch() {
        searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterAircraftList(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }
    
    private void filterAircraftList(String query) {
        List<String> filteredList = new ArrayList<>();
        for (String aircraft : aircraftData) {
            if (aircraft.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                filteredList.add(aircraft);
            }
        }
        adapter.clear();
        adapter.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
    
    private class FetchAircraftData extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> result = new ArrayList<>();
            try {
                URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&titles=F-22_Raptor|Su-57|B-2_Spirit|Tu-160");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();
                
                JSONObject jsonObject = new JSONObject(json.toString());
                JSONObject pages = jsonObject.getJSONObject("query").getJSONObject("pages");
                for (String key : pages.keySet()) {
                    JSONObject aircraft = pages.getJSONObject(key);
                    result.add(aircraft.getString("title"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
        
        @Override
        protected void onPostExecute(List<String> result) {
            aircraftData.clear();
            aircraftData.addAll(result);
            adapter.notifyDataSetChanged();
        }
    }
}
