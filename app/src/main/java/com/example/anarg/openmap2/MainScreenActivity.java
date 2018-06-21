package com.example.anarg.openmap2;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eclipsesource.json.JsonObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainScreenActivity extends AppCompatActivity { //AppCompatActivity
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";
//    private static final String govURl = "http://anarghya321.pythonanywhere.com/static/railwaysignalapi_2018-06-09T10.27.37.000Z.json";
    private AutoCompleteTextView autocompleteView,autocompleteView2,autoCompleteTextView3;
    private TextView direction;
    private BackEnd backEnd;
    private String json,android_id;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
//        setTraceLifecycle(true);
//        String[] dogArr = getResources().getStringArray(R.array.dogs_list);
        backEnd=new BackEnd();
        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        autocompleteView = findViewById(R.id.autocompleteView);
        autocompleteView2 = findViewById(R.id.autocompleteView2);
        autoCompleteTextView3= findViewById(R.id.autocompleteView3);
        direction= findViewById(R.id.directionView);
        RequestTaskPost requestTaskPost=new RequestTaskPost(this);
        requestTaskPost.execute(govURl);
        try {
            json=requestTaskPost.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public AutoCompleteTextView getAutoCompleteTextView3() { return autoCompleteTextView3; }

    public AutoCompleteTextView getAutocompleteView() { return autocompleteView; }

    public AutoCompleteTextView getAutocompleteView2() { return autocompleteView2; }

    public void createTrainNameView(String[] arr, ArrayList<Train> allTrains){
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autocompleteView.setAdapter(adapter);
        autocompleteView.setOnItemClickListener(new onItemClickListener(allTrains,autocompleteView2,direction,"adapter 1",this));
    }

    public void createTrainIDView(String[] arr,ArrayList<Train> allTrains){
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autocompleteView2.setAdapter(adapter);
        autocompleteView2.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        autocompleteView2.setOnItemClickListener(new onItemClickListener(allTrains,autocompleteView,direction,"adapter 2",this));
    }

    public void createTrackNameView(String[] arr,ArrayList<Train> allTrains){
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autoCompleteTextView3.setAdapter(adapter);
        autoCompleteTextView3.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        autoCompleteTextView3.setOnItemClickListener(new onItemClickListener(allTrains,autocompleteView,direction,"adapter 3",this));
    }

    public void dropDown1(View view) { autocompleteView.showDropDown(); }

    public void dropDown2(View view) {
        autocompleteView2.showDropDown();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void clear(View view) {
        if (view==findViewById(R.id.clear1)){
            autocompleteView.setText("",false);
            direction.setVisibility(View.INVISIBLE);
        }else if (view==findViewById(R.id.clear2)){
            autocompleteView2.setText("",false);
            direction.setVisibility(View.INVISIBLE);
        }else if (view==findViewById(R.id.clear3)){
            autoCompleteTextView3.setText("",false);
            direction.setVisibility(View.INVISIBLE);
        }
    }

    public void Start(View view) {
        ServerPost sp=new ServerPost();
        String param=autocompleteView.getText().toString();
        String param2=autocompleteView2.getText().toString();
        String param3=autoCompleteTextView3.getText().toString();
        EditText et= findViewById(R.id.editText);
        try {
            long num = Long.parseLong(et.getText().toString());
            sp.execute(backEndServer,jsonPost("active",Integer.parseInt(param2),num,param,param3));
            String key=sp.get().trim();
            Log.d("key", key);
            if (param.isEmpty() || param2.isEmpty() || param3.isEmpty()) {
                Toast.makeText(this, "Enter Valid Train Info!", Toast.LENGTH_SHORT).show();
            } else if (String.valueOf(num).length() != 10) {
                Toast.makeText(this, "Enter Valid Phone Number!", Toast.LENGTH_SHORT).show();
            } else if (backEnd.checkTrainName(param, json) && backEnd.checkTrainNumber(param2, json) && backEnd.checkTrackName(param3, json)&&key.equals("good")) {
                Intent i = new Intent(this, MainActivity.class);
                i.putExtra("Signal", param);
                i.putExtra("TrainNumber",Integer.parseInt(param2));
                i.putExtra("TrackName",param3);
                i.putExtra("Phone",num);
                startActivity(i);
            } else {
                if (key.equals("error")){
                    exceptionRaised();
                }
                else {
                    Toast.makeText(this, "Enter Valid Train Info!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (NumberFormatException e){
            Toast.makeText(this, "Enter Valid Phone Number!", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            Toast.makeText(this, "Something was wrong!", Toast.LENGTH_SHORT).show();
        } catch (ExecutionException e) {
            Toast.makeText(this, "Something was wrong!", Toast.LENGTH_SHORT).show();
        }
    }
    public String jsonPost(String status,int trainNo, long phone, String trainName, String trackName){
        JsonObject o=new JsonObject();
        o.add("deviceId",android_id);
        JsonObject o2=new JsonObject();
        o2.add("trainNo",trainNo);
        o2.add("phone",phone);
        o2.add("trainName",trainName);
        o2.add("trackName",trackName);
        o.add("info",o2);
        JsonObject o3=new JsonObject();
        o3.add("latitude",0);
        o3.add("longitude",0);
        o.add("coordinate",o3);
        o.add("status", status);
//        Log.d("worksend", o.toString());
        return o.toString();
    }


    public void exceptionRaised() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("There was some problem connecting to the Server!\nPlease try again later.")
                .setTitle("Error");
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
