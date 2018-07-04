package com.example.anarg.openmap2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainScreenActivity extends AppCompatActivity implements AsyncResponse{ //AppCompatActivity
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
//    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";
    private AutoCompleteTextView autocompleteView,autocompleteView2,autoCompleteTextView3;
    private EditText editText;
    private TextView direction;
    private BackEnd backEnd;
    private String android_id;
    private ArrayList<Train> trains;
    private AlertDialog dialog;
    private boolean restart;

    @SuppressLint({"HardwareIds", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        restart=false;
        if (getIntent().getBooleanExtra("Exit", false))
        {
            finish();
        }else if (connectivityCheck()) {
            backEnd = new BackEnd();
            trains = new ArrayList<>();
            android_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            autocompleteView = findViewById(R.id.autocompleteView);
            autocompleteView2 = findViewById(R.id.autocompleteView2);
            autoCompleteTextView3 = findViewById(R.id.autocompleteView3);
            editText = findViewById(R.id.editText);
            direction = findViewById(R.id.directionView);
            RequestTaskPost requestTaskPost = new RequestTaskPost(this, this);
            requestTaskPost.execute(govURl);
            SharedPreferences pref = getSharedPreferences("myPref", MODE_PRIVATE);
            long n = pref.getLong("number", 0);
            if (n != 0) {
                editText.setText(Long.toString(n));
            }
            if (requestTaskPost.getStatus()==RequestTask.Status.RUNNING){
                load();
            }
        }else{
            exceptionRaised("Connectivity Error","Enable mobile data or WiFi to use this app.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (restart&&dialog!=null){
            this.recreate();
            restart=false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        restart=true;
    }

    @Override
    public void processFinish(String output) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (output.equals("null")){
            exceptionRaised("Server Problem","Cannot connect to the Server.Please try again Later.");

        }else if (output.equals("null2")){
            exceptionRaised("Network Problem","Please check your network Connection and Try again Later.");
        }
    }

    public void setTrains(ArrayList<Train> t){ trains=t; }

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
        autoCompleteTextView3.setOnItemClickListener(new onItemClickListener(allTrains,autocompleteView,direction,"adapter 3",this));
    }

    public void dropDown1(View view) { autocompleteView.showDropDown(); }

    public void dropDown2(View view) { autocompleteView2.showDropDown(); }

    public void dropdown3(View view){ autoCompleteTextView3.showDropDown(); }

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
        }else if (view==findViewById(R.id.clear4)){
            editText.setText("");
        }
    }

    public void Start(View view) {
        String param=autocompleteView.getText().toString();
        String param2=autocompleteView2.getText().toString();
        String param3=autoCompleteTextView3.getText().toString();
        try {
            long num = Long.parseLong(editText.getText().toString());
            SharedPreferences preferences=getSharedPreferences("myPref",MODE_PRIVATE);
            SharedPreferences.Editor prefeditor= preferences.edit();
            prefeditor.putLong("number",num);
            prefeditor.apply();
            if (param.isEmpty() || param2.isEmpty() || param3.isEmpty()) {
                Toast.makeText(this, "Enter Valid Train Info!", Toast.LENGTH_SHORT).show();
            } else if (String.valueOf(num).length() != 10) {
                editText.setError("Enter Valid Phone Number!");
//                Toast.makeText(this, "Enter Valid Phone Number!", Toast.LENGTH_SHORT).show();
            } else if (backEnd.checkTrainName(param, trains) && backEnd.checkTrainNumber(param2, trains)
                    && backEnd.checkTrackName(param3, trains)) {
                Intent i = new Intent(this, SignalActivity.class);
                i.putExtra("Signal", param);
                i.putExtra("TrainNumber",Integer.parseInt(param2));
                i.putExtra("TrackName",param3);
                i.putExtra("Phone",num);
                i.putExtra("id",android_id);
                this.startActivity(i);
//                new ServerPost(this, param3, param, param2, editText, trains, num,android_id).execute(backEndServer,
//                        jsonPost("active", Integer.parseInt(param2), num, param, param3));
            } else{
                Toast.makeText(this, "Enter Valid Train Info!", Toast.LENGTH_SHORT).show();
            }
        }catch (NumberFormatException e){
            Toast.makeText(this, "Enter Valid Phone Number!", Toast.LENGTH_SHORT).show();
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

    private boolean connectivityCheck(){
        boolean result=false;
        ConnectivityManager connectivitymanager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivitymanager.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                if (netInfo.isConnected()) {
                    result=true;
                }
            }
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (netInfo.isConnected()) {
                    result=true;
                }
            }
        }
        return result;
    }

    public void exceptionRaised(String title,String body) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(body)
                .setTitle(title);
        builder.setNegativeButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
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

    private void load(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Please wait while the data loads...")
                .setTitle("Loading");
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

}
