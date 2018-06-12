package com.example.anarg.openmap2;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class onItemClickListener implements AdapterView.OnItemClickListener {
    private ArrayList<Train> allTrains;
    private AutoCompleteTextView autoCompleteTextView;
    private TextView textView;
    private String adapterCheck;

    public onItemClickListener(ArrayList<Train> allTrains,AutoCompleteTextView autoCompleteTextView,TextView textView, String s){
        this.allTrains=allTrains;
        this.autoCompleteTextView=autoCompleteTextView;
        this.textView=textView;
        this.adapterCheck=s;
    }

    @SuppressLint("NewApi")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(adapterCheck.equals("adapter 1")) {
            String s = (String) parent.getItemAtPosition(position);
            autoCompleteTextView.setText(findId(s));
            textView.setText("Direction: " + findDirection(s));
            textView.setVisibility(View.VISIBLE);
        }
        else if (adapterCheck.equals("adapter 2")){
            String s = (String) parent.getItemAtPosition(position);
            autoCompleteTextView.setText(findTrainame(s));
            textView.setText("Direction: " + findDirection(findTrainame(s)));
            textView.setVisibility(View.VISIBLE);
        }
    }

    private String findDirection(String s){
        for (Train t: allTrains){
            if (t.getTrainName().equals(s)){
                return t.getDirection();
            }
        }
        return "";
    }

    private String findTrainame(String s){
        for (Train t: allTrains){
            if (Integer.toString(t.getTrainId()).equals(s)){
                return t.getTrainName();
            }
        }
        return "";
    }
    private String findId(String s) {
        for (Train t: allTrains){
            if(t.getTrainName().equals(s)){
                return Integer.toString(t.getTrainId());
            }
        }
        return "";
    }
}
