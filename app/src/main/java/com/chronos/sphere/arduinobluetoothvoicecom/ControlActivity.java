package com.chronos.sphere.arduinobluetoothvoicecom;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ControlActivity extends AppCompatActivity {

    private ImageButton micRed;

    // bluetooth section
    private ListView devicelist;
    private BluetoothAdapter myBluetooth = null;
    Button btnPaired;
    private MyBTconnector conBT;
    private String btAddress;

    private TextView statusTxtView;
    private static final int SPEECH_REQUEST_CODE = 100;
    private String statusStr;

    private ProgressDialog progress;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        initParams();

    }

    private void initParams(){
        devicelist = (ListView)findViewById(R.id.devices_list);
        statusTxtView = (TextView) findViewById(R.id.status_txt);
        statusStr = getString(R.string.status_txt);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        btnPaired = (Button) findViewById(R.id.bt_button);
        micRed = (ImageButton) findViewById(R.id.img_bttn);
        conBT = new MyBTconnector();
        String txtV = String.format("Status: ");
        statusTxtView.setText(txtV);


        micRed.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //change background here
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        micRed.setAlpha(0.6f);
                        break;

                    case MotionEvent.ACTION_UP:

                        micRed.setAlpha(1.0f);
                        if (conBT.getbtIsConnected()) {
                            displaySpeechRecognizer();//toggle speech recognizer
                        }
                        else {
                            Toast.makeText(ControlActivity.this, "Connect to device first", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return false;
            }
        });

        // buttons
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (conBT.getbtIsConnected()) {
                    //do nothing
                }
                else {
                    devicelist.setVisibility(View.VISIBLE);
                    pairedDevicesList();
                }
            }
        });


    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        btInitializer();
    }


    private void btInitializer() {
        if(myBluetooth == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //exit app
            finish();
        }
        if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }

    }

    private void pairedDevicesList()
    {
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                //noinspection unchecked
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        @SuppressWarnings("unchecked") final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }

    private final AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            btAddress = info.substring(info.length() - 17);

            // Display progress
            new progConnectBT().execute();
        }
    };


    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        if (conBT.getbtIsConnected()) {
            conBT.closeConnection();
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2500);
    }


    void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak command");
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            String lwrCaseSpokenText = spokenText.toLowerCase();
            // Do something with spokenText
            String fStatus = String.format("%s%s",statusStr,lwrCaseSpokenText);
            statusTxtView.setText(fStatus);
            //statusView.setText(lwrCaseSpokenText);
            if (lwrCaseSpokenText.equals("lights on") || lwrCaseSpokenText.equals("let there be light") || lwrCaseSpokenText.equals("give me some light") || lwrCaseSpokenText.equals("light please") || lwrCaseSpokenText.equals("why so dark")) {
                //turnOnLed();
                conBT.sendBytes("on");
            }
            else if (lwrCaseSpokenText.equals("go dark") || lwrCaseSpokenText.equals("lights off") || lwrCaseSpokenText.equals("i don't want light") || lwrCaseSpokenText.equals("i'm a vampire") || lwrCaseSpokenText.equals("no light")) {
                //turnOffLed();
                conBT.sendBytes("off");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private class progConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(ControlActivity.this, "", "Connecting...", true);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (conBT.getbtIsConnected()) {
                Toast.makeText(ControlActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                devicelist.setVisibility(View.GONE);
            }
            else {
                Toast.makeText(ControlActivity.this, "Try again", Toast.LENGTH_SHORT).show();
            }
            progress.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Connecting device
            try {
                conBT.conBTexpress(btAddress);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(ControlActivity.this, "Error Connecting to " + btAddress, Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }
}
