package net.raphaelbaron.remotebox;


import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public final static String IP_ADDR = "com.example.myfirstapp.ipAddr";
	public final static String FILENAME = "remotebox_ip_addr";

	
	//private Handler handler;
	private TextView textView;
	private EditText editText;
	
	private Intent intent;
	
	private boolean isNetworkUsable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText1);
        
        if(savedInstanceState != null) {
        	//Restoring a saved instance
        	debug("MainActivity: Restoring a saved instance.");
        }
        else {
        	debug("MainActivity: Creating a new instance. ");
        }
        
        //Try to restore the saved ip address
        try {
        	FileInputStream db = openFileInput(FILENAME);
        	debug("Reading ip address from database file");
        	
        	byte[] ip = new byte[16];
        	int n = db.read(ip);
        	
        	debug("Read "+n+ "bytes");
        	String ipStr = new String(ip, 0, n);      
        	
        	editText.setText(ipStr);
        	
        	db.close();
        } catch(Exception e) {
        	debug("Database file not found.");
        }
             
    }
    
    public void debug(String txt) {
    	Log.d("DEBUG", txt);
    }
    
    @Override
    public void onResume() {
    	
        super.onResume();          
    }
    
    public void onClickConnect(View view) {
    	
    	//Write ip address to a file so it is persisted
    	try {
    		FileOutputStream db = openFileOutput(FILENAME, Context.MODE_PRIVATE);
    		db.write(editText.getText().toString().getBytes());
    		db.close();
    	} catch(Exception e) {
    		//Do nothing
    	}
    	
    	
    	if(!isNetworkUsable()) {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("No connection found. Please turn your WiFi on!").setTitle("Connection error");
	    	
	    	builder.setPositiveButton("Close",
				new DialogInterface.OnClickListener()
				{
					//@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				} 
	    	);

			AlertDialog dialog = builder.create();
			 
			dialog.show();
    	}
    	else {
            
    		intent = new Intent(this, PlayerUI.class);
        	String message = editText.getText().toString();
        	intent.putExtra(IP_ADDR, message);
        	startActivity(intent);	    		 
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public boolean isNetworkUsable() {
		ConnectivityManager connMgr = (ConnectivityManager) 
                getSystemService(Context.CONNECTIVITY_SERVICE);
        
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        if (networkInfo != null && networkInfo.isConnected()) {
        	return true;
        } else {
        	return false;
        }
	}
}
