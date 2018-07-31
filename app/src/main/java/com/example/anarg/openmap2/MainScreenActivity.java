package com.example.anarg.openmap2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
This class controls the welcome screen of the app.
@author Anarghya Das
 */
public class MainScreenActivity extends AppCompatActivity implements AsyncResponse { //AppCompatActivity
    //TMS URL from which the data is fetched in the app
    private static final String tmsURL = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
//    private static final String backEndServer= "http://192.168.0.106/railway/senddevicelocations.cgi";
//    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";

    //Autocomplete widget used to display train name, train number and track name respectively
    private AutoCompleteTextView autocompleteView, autocompleteView2, autoCompleteTextView3;
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
    private boolean restart, fileIOPermission;
    private static final String folderPath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/.FogSignal";
    private static final long dayThreshhold=259200000;//in ms


    /**
     * The first function which runs after the activity has started
     * Initializes the the instance variables declared above
     */
    @SuppressLint({"HardwareIds", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Loading Time", "initialStarted ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        restart = false;
        fileIOPermission = false;
        askPermission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
        if (getIntent().getBooleanExtra("Exit", false)) {
            finish();
        } else if (connectivityCheck()) {
            if (fileIOPermission) {
                try {
                    deleteOldFolders();
                } catch (ParseException e) {
                    Log.d("FileTest", "error");
                }
            }
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
            requestTaskPost.execute(tmsURL);
            SharedPreferences pref = getSharedPreferences("myPref", MODE_PRIVATE);
            long n = pref.getLong("number", 0);
            if (n != 0) {
                editText.setText(Long.toString(n));
            }
            if (requestTaskPost.getStatus() == RequestTaskPost.Status.RUNNING) {
                load("Please wait while the data loads...");
            }
        } else {
            exceptionRaised("Connectivity Error", "Enable mobile data or WiFi to use this app.");
        }
    }

    /**
     * If the activity is restarted then refreshes the screen and loads new data
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (restart && dialog != null) {
            restart();
            restart = false;
        }
    }

    /**
     * Sets restart activity to true
     */
    @Override
    protected void onPause() {
        super.onPause();
        restart = true;
    }

    /**
     * This method runs after the async task is complete and executes proper functions based on the
     * result received.
     *
     * @param asyncOutput Stores the result of the async task after completion.
     */
    @Override
    public void processFinish(String asyncOutput) {
        dialog.dismiss();
        switch (asyncOutput) {
            case "null":
                exceptionRaised("Server Problem", "Cannot connect to the Server.Please try again Later.");
                break;
            case "null2":
                exceptionRaised("Network Problem", "Please check your network Connection and Try again Later.");
                break;
            case "error":
                exceptionRaised("Multiple Devices Error", "Currently a device is already logged into this train.\n" +
                        "Please check your information and try again.");
                break;
            case "error2":
                exceptionRaised("Connection Error", "Please check your network connection and try again!");
                break;
        }
    }

    /**
     * Setter method to populate the array list of trains.
     *
     * @param t Array list of trains
     */
    public void setTrains(ArrayList<Train> t) {
        trains = t;
    }

    /**
     * @return Autocomplete text view containing the track names.
     */
    public AutoCompleteTextView getAutoCompleteTextView3() {
        return autoCompleteTextView3;
    }

    /**
     * @return Autocomplete text view containing the train names.
     */
    public AutoCompleteTextView getAutocompleteView() {
        return autocompleteView;
    }

    /**
     * @return Autocomplete text view containing the train ids.
     */
    public AutoCompleteTextView getAutocompleteView2() {
        return autocompleteView2;
    }

    /**
     * Creates the autocomplete text view for train names.
     *
     * @param arr       Array which stores the train names in autocomplete text view.
     * @param allTrains Array list of the trains received from the server
     */
    public void createTrainNameView(String[] arr, ArrayList<Train> allTrains) {
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autocompleteView.setAdapter(adapter);
        autocompleteView.setOnItemClickListener(new onItemClickListener(allTrains, direction, "adapter 1", this));
    }

    /**
     * Creates the autocomplete text view for train IDs.
     *
     * @param arr       Array which stores the train IDs in autocomplete text view.
     * @param allTrains Array list of the trains received from the server
     */
    public void createTrainIDView(String[] arr, ArrayList<Train> allTrains) {
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autocompleteView2.setAdapter(adapter);
        autocompleteView2.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        autocompleteView2.setOnItemClickListener(new onItemClickListener(allTrains, direction, "adapter 2", this));
    }

    /**
     * Creates the autocomplete text view for track names.
     *
     * @param arr       Array which stores the track names in autocomplete text view.
     * @param allTrains Array list of the trains received from the server
     */
    public void createTrackNameView(String[] arr, ArrayList<Train> allTrains) {
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> trainList = Arrays.asList(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutItemId, trainList);
        autoCompleteTextView3.setAdapter(adapter);
        autoCompleteTextView3.setOnItemClickListener(new onItemClickListener(allTrains, direction, "adapter 3", this));
    }

    /**
     * Shows the dropdown list of all the train names.
     */
    public void dropDown1(View view) {
        autocompleteView.showDropDown();
    }

    /**
     * Shows the dropdown list of all the train IDs.
     */
    public void dropDown2(View view) {
        autocompleteView2.showDropDown();
    }

    /**
     * Shows the dropdown list of all the track names.
     */
    public void dropdown3(View view) {
        autoCompleteTextView3.showDropDown();
    }

    /**
     * Clears the autocomplete text view widgets of the train names, IDs and track names.
     *
     * @param view Takes the widget reference which needs to be cleared
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void clear(View view) {
        if (view == findViewById(R.id.clear1)) {
            autocompleteView.setText("", false);
            direction.setVisibility(View.INVISIBLE);
        } else if (view == findViewById(R.id.clear2)) {
            autocompleteView2.setText("", false);
            direction.setVisibility(View.INVISIBLE);
        } else if (view == findViewById(R.id.clear3)) {
            autoCompleteTextView3.setText("", false);
            direction.setVisibility(View.INVISIBLE);
        } else if (view == findViewById(R.id.clear4)) {
            editText.setText("");
        }
    }

    /**
     * OnClick button handler of the Enter button, checks whether all the input is correct or not
     * and then starts the new intent to the next activity.
     *
     * @param view The reference of the button widget
     */
    public void Start(View view) {
        String param = autocompleteView.getText().toString();
        String param2 = autocompleteView2.getText().toString();
        String param3 = autoCompleteTextView3.getText().toString();
        TextView direction = findViewById(R.id.directionView);
        try {
            long num = Long.parseLong(editText.getText().toString());
            SharedPreferences preferences = getSharedPreferences("myPref", MODE_PRIVATE);
            SharedPreferences.Editor prefeditor = preferences.edit();
            prefeditor.putLong("number", num);
            prefeditor.apply();
            if (param.isEmpty() || param2.isEmpty() || param3.isEmpty()) {
                Toast.makeText(this, "Enter Valid Train Info!", Toast.LENGTH_SHORT).show();
            } else if (String.valueOf(num).length() != 10) {
                editText.setError("Enter Valid Phone Number!");
//                Toast.makeText(this, "Enter Valid Phone Number!", Toast.LENGTH_SHORT).show();
            } else if (backEnd.checkTrainName(param, trains) && backEnd.checkTrainNumber(param2, trains)
                    && backEnd.checkTrackName(param3, trains)) {
//                load("Please wait, Checking your inputs!");

                Intent i = new Intent(this, SignalActivity.class);
                i.putExtra("Signal", param);
                i.putExtra("TrainNumber", Integer.parseInt(param2));
                i.putExtra("TrackName", param3);
                i.putExtra("Phone", num);
                i.putExtra("id", android_id);
                i.putExtra("Direction", direction.getText());
                this.startActivity(i);

//                new ServerPost(this, param3, param, param2, num,android_id,this).execute(backEndServer,
//                        jsonPost("active", Integer.parseInt(param2), num, param, param3));
            } else {
                Toast.makeText(this, "Enter Valid Train Info!", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter Valid Phone Number!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a json object of all the user inputs along with android device ID to send it to the
     * server
     *
     * @return The json string to be sent to the server
     */
    public String jsonPost(String status, int trainNo, long phone, String trainName, String trackName) {
        JsonObject o = new JsonObject();
        o.add("deviceId", android_id);
        JsonObject o2 = new JsonObject();
        o2.add("trainNo", trainNo);
        o2.add("phone", phone);
        o2.add("trainName", trainName);
        o2.add("trackName", trackName);
        o.add("info", o2);
        JsonObject o3 = new JsonObject();
        o3.add("latitude", 0);
        o3.add("longitude", 0);
        o.add("coordinate", o3);
        o.add("status", status);
//        Log.d("worksend", o.toString());
        return o.toString();
    }

    /**
     * Checks if the user is connected to Wifi or mobile data or not
     *
     * @return true is connected and false otherwise
     */
    private boolean connectivityCheck() {
        boolean result = false;
        ConnectivityManager connectivitymanager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivitymanager.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                if (netInfo.isConnected()) {
                    result = true;
                }
            }
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (netInfo.isConnected()) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Method which creates a custom dialog box to show if the program encountered an error
     *
     * @param title Title of the dialog box
     * @param body  Body of text for the dialog box
     */
    public void exceptionRaised(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(body)
                .setTitle(title);
        builder.setNegativeButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restart();
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
     * Method which creates the loading dialog when the data takes time to load
     */
    private void load(String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(body)
                .setTitle("Loading");
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Helper method to restart this activity
     */
    private void restart() {
        finish();
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void askPermission(String permission,String permission2, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission,permission2}, requestCode);
        } else {
            fileIOPermission = true;
            Log.d("FIle", Boolean.toString(fileIOPermission));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!",Toast.LENGTH_SHORT).show();
                    try {
                        deleteOldFolders();
                    } catch (ParseException e) {
                        Log.d("FileTest", "error");
                    }
                }else{
                    Toast.makeText(this,"Enable it!",Toast.LENGTH_SHORT).show(); //change it later
//                    startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
                }
        }
    }

    private void deleteOldFolders() throws ParseException {
        HashMap<Date,File> nameFile=new HashMap<>();
        File root=new File(folderPath);
        File[] allFiles=root.listFiles();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df=new SimpleDateFormat("y-MM-d");
        for (File folder: allFiles){
            if (folder.isDirectory()){
                String name=folder.getName().substring(1,folder.getName().length());
                Date date= df.parse(name);
                nameFile.put(date,folder);
            }
        }
        Log.d("FileTest", nameFile.toString());
        Date currentDate=new Date();
        for (Date d: nameFile.keySet()){
//            Log.d("FileTest", Long.toString(currentDate.getTime()-d.getTime()));
//            Log.d("FileTest","."+d.toString());
            if (currentDate.getTime()-d.getTime()>=dayThreshhold) {
                String date = df.format(d);
                date = "." + date;
                String path=folderPath+"/"+date;
                File folder=new File(path);
                deleteFolder(folder);
                Log.d("FileTest", "deleted");
            }
        }
    }
    private void deleteFolder(File f){
        if (f.isDirectory()){
            boolean result=f.delete();
            if (!result&&deleteAllFilesinFolder(f)){
                f.delete();
            }
        }
    }
    private boolean deleteAllFilesinFolder(File f){
        File[] allFiles=f.listFiles();
        boolean res=false;
        for (File fo: allFiles){
            res=fo.delete();
        }
        return res;
    }
}
