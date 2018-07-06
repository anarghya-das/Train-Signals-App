package com.example.anarg.openmap2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.eclipsesource.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
This class controls the welcome screen of the app.
@author Anarghya Das
 */
public class MainScreenActivity extends AppCompatActivity implements AsyncResponse{ //AppCompatActivity
    //Government URL from which the data is fetched in the app
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
//    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";

    //Autocomplete widget used to display train name, train number and track name respectively
    private AutoCompleteTextView autocompleteView,autocompleteView2,autoCompleteTextView3;
    //Edit Text widget used to enter the phone number of the driver
    private EditText editText;
    //Text view widget used to show the direction of the train selected (Invisible by default)
    private TextView direction;
    //Variable stores the reference of backEnd class
    private BackEnd backEnd;
    //Stores the unique id of each android device
    private String android_id;
    //Stores all the train object received from the server
    private ArrayList<Train> trains;
    //Dialog widget which displays the loading and the error messages
    private AlertDialog dialog;
    //Boolean variable which ensures that activity is not recreated during the first run
    private boolean restart;
    /**
     * The first function which runs after the activity has started
     * Initializes the the instance variables declared above
     */
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
    /**
     * If the activity is restarted then refreshes the screen and loads new data
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (restart&&dialog!=null){
            this.recreate();
            restart=false;
        }
    }
    /**
     * Sets restart activity to true
     */
    @Override
    protected void onPause() {
        super.onPause();
        restart=true;
    }
    /**
     * This method runs after the async task is complete and executes proper functions based on the
     * result received.
     * @param asyncOutput Stores the result of the async task after completion.
     */
    @Override
    public void processFinish(String asyncOutput) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (asyncOutput.equals("null")){
            exceptionRaised("Server Problem","Cannot connect to the Server.Please try again Later.");

        }else if (asyncOutput.equals("null2")){
            exceptionRaised("Network Problem","Please check your network Connection and Try again Later.");
        }
    }

    /**
     * Setter method to populate the array list of trains.
     * @param t Array list of trains
     */
    public void setTrains(ArrayList<Train> t){ trains=t; }
    /**
     * @return Autocomplete text view containing the track names.
     */
    public AutoCompleteTextView getAutoCompleteTextView3() { return autoCompleteTextView3; }
    /**
     * @return Autocomplete text view containing the train names.
     */
    public AutoCompleteTextView getAutocompleteView() { return autocompleteView; }
    /**
     * @return Autocomplete text view containing the train ids.
     */
    public AutoCompleteTextView getAutocompleteView2() { return autocompleteView2; }

    /**
     * Creates the autocomplete text view for train names.
     * @param arr Array which stores the train names in autocomplete text view.
     * @param allTrains Array list of the trains received from the server
     */
    public void createTrainNameView(String[] arr, ArrayList<Train> allTrains){
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autocompleteView.setAdapter(adapter);
        autocompleteView.setOnItemClickListener(new onItemClickListener(allTrains,direction,"adapter 1",this));
    }
    /**
     * Creates the autocomplete text view for train IDs.
     * @param arr Array which stores the train IDs in autocomplete text view.
     * @param allTrains Array list of the trains received from the server
     */
    public void createTrainIDView(String[] arr,ArrayList<Train> allTrains){
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autocompleteView2.setAdapter(adapter);
        autocompleteView2.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        autocompleteView2.setOnItemClickListener(new onItemClickListener(allTrains,direction,"adapter 2",this));
    }
    /**
     * Creates the autocomplete text view for track names.
     * @param arr Array which stores the track names in autocomplete text view.
     * @param allTrains Array list of the trains received from the server
     */
    public void createTrackNameView(String[] arr,ArrayList<Train> allTrains){
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autoCompleteTextView3.setAdapter(adapter);
        autoCompleteTextView3.setOnItemClickListener(new onItemClickListener(allTrains,direction,"adapter 3",this));
    }
    /**
     * Shows the dropdown list of all the train names.
     */
    public void dropDown1(View view) { autocompleteView.showDropDown(); }
    /**
     * Shows the dropdown list of all the train IDs.
     */
    public void dropDown2(View view) { autocompleteView2.showDropDown(); }
    /**
     * Shows the dropdown list of all the track names.
     */
    public void dropdown3(View view){ autoCompleteTextView3.showDropDown(); }
    /**
     * Clears the autocomplete text view widgets of the train names, IDs and track names.
     * @param view Takes the widget reference which needs to be cleared
     */
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

    /**
     * OnClick button handler of the Enter button, checks whether all the input is correct or not
     * and then starts the new intent to the next activity.
     * @param view The reference of the button widget
     */
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
    /**
     * Creates a json object of all the user inputs along with android device ID to send it to the
     * server
     * @return The json string to be sent to the server
     */
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
    /**
     * Checks if the user is connected to Wifi or mobile data or not
     * @return true is connected and false otherwise
     */
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

    /**
     * Method which creates a custom dialog box to show if the program encountered an error
     * @param title Title of the dialog box
     * @param body Body of text for the dialog box
     */
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
    /**
     * Method which creates a custom dialog box to show if the program encountered an error
     */
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
    /**
     * Method which creates the loading dialog when the data takes time to load
     */
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
