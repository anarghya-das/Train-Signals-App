package com.example.anarg.openmap2;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainScreenActivity extends AppCompatActivity {
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
    private AutoCompleteTextView autocompleteView;
    private AutoCompleteTextView autocompleteView2;
    private TextView direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
//        String[] dogArr = getResources().getStringArray(R.array.dogs_list);
        autocompleteView = findViewById(R.id.autocompleteView);
        autocompleteView2 = findViewById(R.id.autocompleteView2);
        direction= findViewById(R.id.directionView);
        new RequestTaskPost(this).execute(govURl);
    }

    public void createTrainNameView(String[] arr, ArrayList<Train> allTrains){
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autocompleteView.setAdapter(adapter);
        autocompleteView.setOnItemClickListener(new onItemClickListener(allTrains,autocompleteView2,direction,"adapter 1"));
    }

    public void createTrainIDView(String[] arr,ArrayList<Train> allTrains){
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autocompleteView2.setAdapter(adapter);
        autocompleteView2.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        autocompleteView2.setOnItemClickListener(new onItemClickListener(allTrains,autocompleteView,direction,"adapter 2"));
    }

    public void dropDown1(View view) {
        autocompleteView.showDropDown();
    }

    public void dropDown2(View view) {
        autocompleteView2.showDropDown();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void clear(View view) {
        autocompleteView2.setText("",false);
        autocompleteView.setText("",false);
        direction.setVisibility(View.INVISIBLE);
    }
}
