package com.example.anarg.openmap2;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * onClick listeners of the AutoCompleteTextView widgets
 * @author Anarghya Das
 */
public class onItemClickListener implements AdapterView.OnItemClickListener {
    //Stores all trains
    private ArrayList<Train> allTrains;
    //Stores the direction of the train
    private TextView textView;
    //Stores the adapter which was clicked
    private String adapterCheck;
    //Stores reference to the MainScreenActivity class
    private MainScreenActivity m;
    /**
     * Initializes the instance variables.
     * @param allTrains all train objects
     * @param textView direction of the train
     * @param s current adapter selected
     * @param m MainScreenActivity reference
     */
    public onItemClickListener(ArrayList<Train> allTrains, TextView textView, String s, MainScreenActivity m) {
        this.allTrains = allTrains;
        this.textView = textView;
        this.adapterCheck = s;
        this.m = m;
    }
    /**
     * Changes the UI on the respective selection from the user.
     * @param position the index of the value selected in the adapter
     */
    @SuppressLint("NewApi")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (adapterCheck.equals("adapter 1")) {
            String s = (String) parent.getItemAtPosition(position);
            m.getAutocompleteView2().setText(findId(s));
            m.getAutoCompleteTextView3().setText(findTrackName(s));
            textView.setText("Direction: " + findDirection(s));
            textView.setVisibility(View.VISIBLE);
        } else if (adapterCheck.equals("adapter 2")) {
            String s = (String) parent.getItemAtPosition(position);
            m.getAutocompleteView().setText(findTrainame(s));
            m.getAutoCompleteTextView3().setText(findTrackName(findTrainame(s)));
            textView.setText("Train Direction: " + findDirection(findTrainame(s)));
            textView.setVisibility(View.VISIBLE);
        } else if (adapterCheck.equals("adapter 3")) {
            String s = (String) parent.getItemAtPosition(position);
            m.getAutocompleteView().setText(findTrainName2(s));
            m.getAutocompleteView2().setText(findId(findTrainName2(s)));
            textView.setText("Train Direction: " + findDirection(findTrainName2(s)));
            textView.setVisibility(View.VISIBLE);
        }
    }
    /**
     * Helper method to find the direction of the train using train name.
     * @param s train name
     * @return direction of the train
     */
    private String findDirection(String s) {
        for (Train t : allTrains) {
            if (t.getTrainName().equals(s)) {
                return t.getDirection();
            }
        }
        return "";
    }
    /**
     * Helper method to find the train name of the train using train ID.
     * @param s train ID
     * @return train name
     */
    private String findTrainame(String s) {
        for (Train t : allTrains) {
            if (Integer.toString(t.getTrainId()).equals(s)) {
                return t.getTrainName();
            }
        }
        return "";
    }
    /**
     * Helper method to find the train name of the train using track Name.
     * @param s track Name
     * @return train name
     */
    private String findTrainName2(String s){
        for (Train t: allTrains){
            if (t.getTrackName().equals(s)){
                return t.getTrainName();
            }
        }
        return "";
    }
    /**
     * Helper method to find the train ID of the train using train Name.
     * @param s train Name
     * @return train ID
     */
    private String findId(String s) {
        for (Train t : allTrains) {
            if (t.getTrainName().equals(s)) {
                return Integer.toString(t.getTrainId());
            }
        }
        return "";
    }
    /**
     * Helper method to find the track name of the train using train Name.
     * @param s train Name
     * @return track name
     */
    private String findTrackName(String s) {
        for (Train t : allTrains) {
            if (t.getTrainName().equals(s)){
                return t.getTrackName();
            }
        }
        return "";
    }
}
