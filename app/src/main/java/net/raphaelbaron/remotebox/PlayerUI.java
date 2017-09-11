package net.raphaelbaron.remotebox;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

import net.raphaelbaron.remotebox.data.RemoteboxData;
import net.raphaelbaron.remotebox.fragments.MyListFragment;
import net.raphaelbaron.remotebox.models.Track;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ImageView;

public class PlayerUI extends FragmentActivity {
	
	public final int ICON_OK = 0x0;
	public final int ICON_ERROR = 0x1;
	public final int ICON_LOADING = 0x2;

	//Handler to pass to network thread
	//private TextView textView;
	private NetThread nt;
	
	private String ip;
	
	//private ProgressDialog dialog;
	private AlertDialog dialog;
	
	//ControlFragment's volumeBar
	private SeekBar volumeBar;
	
	//Fragments
	private MyListFragment myArtistsListFragment, myAlbumsListFragment, myTracksListFragment;
	
	//Figure out a way of making this public static while still getting access to the app's context
	private Handler handler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			/*
	   		*   Messages received from net thread:
	   		*     	-1: debug info, string
	   		*   	0: error, string
	   		*   	1: success, string
	   		*     	2: success, xml string. Tells AsyncTask (XMLStringParser) to parse the XML string
	   		*     	3: success, list is ready (done parsing)
	   		*     
	   		*     	5: success, got volume (string float)
	   		*       6: success, set(ted) volume
	   		*/
	   		if(msg.what == -1) {
	   			//textView.append("\nDEBUG: "+msg.obj); 
	   		}
	   		else if(msg.what == 0) {
	   			//errorMsg("\nERROR: "+msg.obj);
	   			errorMsg(msg.obj.toString());
	   			setStatusIcon(ICON_ERROR);
	   		}
	   		else if(msg.what == 1) {
	   			//textView.append("\nSUCCESS: "+msg.obj);
	   			setStatusIcon(ICON_OK);
	   		}
	   		else if(msg.what == 2) {
	   			setStatusIcon(ICON_LOADING);
	   			
	   			showDialog("Loading", "Generating list and ordering by name. It might take a few seconds...",false);
	   			
	   			//Pass xml string to AsyncTask ParseTrackList
	   			XMLStringParser p = new XMLStringParser();
	   			p.execute(msg.obj.toString());
	   		}
	   		else if(msg.what == 3) {
	   			hideDialog();
	   			setStatusIcon(ICON_OK);
	   			
	   			try {
	   				myArtistsListFragment = new MyListFragment();

		   			//Set root node
		   			Bundle b = new Bundle();
		   				
		   			//Tells MyListFragment that root is RemoteboxData.root
		   			b.putInt("listType", MyListFragment.ARTISTSLIST);
		   			myArtistsListFragment.setArguments(b);
		   			
		   			hideDialog();
		   				
		   			//Show up MyListFragment
		   			try {
		   				//Fix: do _not_ add to back stack
		   				//getSupportFragmentManager().beginTransaction().replace(R.id.list_container, myArtistsListFragment).addToBackStack("myArtistsListFragment").commit();
		   				getSupportFragmentManager().beginTransaction().replace(R.id.list_container, myArtistsListFragment).commit();

			   			setStatusIcon(ICON_OK);
		   			}
		   			catch(Exception e) {
		   				debug("Error while commiting fragment transaction: "+e.toString());
			   			setStatusIcon(ICON_ERROR);
		   			}
	   				
	   			}
	   			catch(Exception e) {
	   				
	   			}
	   			
	   		}
	   		else if(msg.what == 5) {
	   			debug("Got volume: "+msg.obj.toString());
	   			
	   			Float percentage = 100*Float.parseFloat(msg.obj.toString());
	   			
	   			volumeBar.setProgress(percentage.intValue());
	   		}
	   		
	   		else if(msg.what == 10) {
	   			hideDialog();
	   			setStatusIcon(ICON_OK);
	   			
	   			if(myArtistsListFragment == null) {
	   				showInfoDialog("Connected!", "Press Menu > Refresh File List to download your file list!");
	   			}
	   		}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_ui);
        
        debug("PlauerUI: onCreate().");
        
        //New or has savedInstanceState?
        if(savedInstanceState != null) {
        	//Restoring a saved instance
        	debug("PlauerUI: Restoring a saved instance.");

        	//Get ip address
        	ip = savedInstanceState.getString("ipAddr");
        	
        	//Already has a list. Set it.
        	
        } else {
        	//New instance
        	debug("PlauerUI: Creating a new instance.");
        	
        	//Get the IP address from intent 
            Intent intent = getIntent();
            ip = intent.getStringExtra(MainActivity.IP_ADDR);
        
            
            //Find list container
            if (findViewById(R.id.list_container) != null) {
                //Allocate myListFragment
                //myArtistsListFragment = new MyListFragment();
            }
         
        }
               	
        //Get the text view
        //textView = (TextView) findViewById(R.id.textView1);
                
    }
    
    @Override 
    public void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
    	
    	if(savedInstanceState != null) {
    		//Instance already exists
    		((TextView) findViewById(R.id.textView1)).setText("remotebox");
    	}
    	else {
    		//New instance
    		((TextView) findViewById(R.id.textView1)).setText("remotebox");
    	}
    	
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	//textView.append("\nonSaveInstanceState()");
    	

    	debug("PlauerUI: onSaveInstanceState().");
    	
    	//Save ip
    	outState.putString("ipAddr", this.ip);
    	
    	//Save current list
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);

    	debug("PlauerUI: onRestoreInstanceState().");
    }
	    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	//Disconnect
        sendToNetThread(4, "");
        
        //Make sure current NetThread wont be used (otherwise nullpointer exception)
        nt = null; 
	}
    
    //Connects
    @Override
	public void onResume() {
    	super.onResume();
    	debug("PlauerUI: onResume().");
    	
    	//Set dialog and wait for a message from network thread to close it
    	this.showDialog("Connecting", "Connecting to server...", false);
    	
    	volumeBar = (SeekBar) findViewById(R.id.volumeBar);
    	    	 
    	//Start net thread
        try {
        	debug("IP: "+ip);
        	nt = new NetThread(ip);
        	nt.setUIHandler(handler);
        	nt.start();
        	debug("Aqui");

       	} catch(Exception e) {
       		errorMsg("\nError while creating the networking thread: "+e.toString());
       		e.printStackTrace();
       	}       
	}
     
    //Disconnects
    @Override
    public void onStop() {
    	super.onStop();
    	debug("PlauerUI: onStop().");
	}
    
    //End net thread
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	debug("PlauerUI: onDestroy().");
    }
        
    @Override
    public void onStart() {
    	super.onStart();
    	debug("PlauerUI: onStart().");
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_player_ui, menu);
        return true;
    }
    
    public int sendToNetThread(int what, String txt) {
    	try {
    		nt.getHandler().sendMessage(nt.getHandler().obtainMessage(what,txt));
    	} catch(Exception e) {
    		errorMsg("\nError on sendToNetThread: "+e.toString());
    	}
    	return 0;
    }
    
    public void askForFileList(MenuItem menuItem) {
    	showDialog("Loading", "Downloading file list...", true);
    	this.sendToNetThread(2, "");
    }
    
    public void errorMsg(String error) {
    	//Log.d(null, error);
    	showErrorDialog(error);
    	
    }
    public void debug(String txt) {
    	Log.d("DEBUG", txt);
    }
    
    public void setHeader(String txt) {
    	((TextView) findViewById(R.id.textView1)).setText(txt);
    }
    
    public void startMyAlbumsListFragment(int artistID) {
    	myAlbumsListFragment = new MyListFragment();
    	
    	//Log.d("wat", "Starting albums list - artistID: "+artistID);

		Bundle b = new Bundle();
			
		//Tells MyListFragment which root to use
		b.putInt("listType", MyListFragment.ALBUMSLIST);
		b.putInt("artistID", artistID);
		myAlbumsListFragment.setArguments(b);
			
		//Show up MyListFragment (after adding the transaction to backstack
		try {
			getSupportFragmentManager().beginTransaction().replace(R.id.list_container, myAlbumsListFragment).addToBackStack(null).commit();
		}
		catch(Exception e) {
			debug("Error while commiting fragment transaction: "+e.toString());
		}
    }
    
    public void startMyTracksListFragment(int artistID, int albumID) {
    	myTracksListFragment = new MyListFragment();
    	
    	//Log.d("wat", "Starting tracks list - artistID: "+artistID+" albumID: "+albumID);
    	
		Bundle b = new Bundle();
			
		//Tells MyListFragment which root to use
		b.putInt("listType", MyListFragment.TRACKSLIST);
		b.putInt("artistID", artistID);
		b.putInt("albumID", albumID);
		
		myTracksListFragment.setArguments(b);
			
		//Show up MyListFragment (after adding the transaction to backstack
		try {
			getSupportFragmentManager().beginTransaction().replace(R.id.list_container, myTracksListFragment).addToBackStack(null).commit();
		}
		catch(Exception e) {
			debug("Error while commiting fragment transaction: "+e.toString());
		}
    }
    
    private void clearBackStack() {
    	FragmentManager fm = getSupportFragmentManager();
    	
    	while(fm.getBackStackEntryCount()>0){
    	    fm.popBackStack();
    	}
    }
    
    public void setStatusIcon(int iconCode) {
    	Drawable myIcon = null;
    	try {
    		
    		if(iconCode == ICON_OK) {
        		myIcon = getResources().getDrawable(R.drawable.navigation_accept_green);
    		}
    		else if(iconCode == ICON_ERROR) {
        		myIcon = getResources().getDrawable(R.drawable.navigation_cancel_red);
    		}
    		else if(iconCode == ICON_LOADING) {
        		myIcon = getResources().getDrawable(R.drawable.navigation_accept);
    		}
    		
			if(myIcon != null) ((ImageView) findViewById(R.id.imageView1)).setImageDrawable(myIcon);
			
			((ImageView) findViewById(R.id.imageView1)).invalidate();
    	}
    	catch(Exception e) {
    		
    	}
    }
    
    public void showErrorDialog(String message) {
    	//Close any open dialog
    	hideDialog();
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
        .setTitle("Error")
        		/*
               .setPositiveButton("Positive", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                   }
               })
               */
               .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        
        builder.create().show();
    }
    
    public void showInfoDialog(String title, String message) {
    	//Close any open dialog
    	hideDialog();
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
        .setTitle(title);
        		/*
               .setPositiveButton("Positive", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                   }
               })
               
               .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });*/
        
        builder.create().show();
    }
    
    public void showDialog(String title, String content, boolean cancelable) {
    	//Is there a dialog already showing?
    	hideDialog();
    	
    	//Context, title, content, indeterminate, cancelable
    	dialog = ProgressDialog.show(this, title, content, false, cancelable);
    }
    
    public void hideDialog() {
    	try {
    		dialog.dismiss();
    	}
    	catch(Exception e) {
    		//Log.d("Dialog", "error while dismiss()'ing dialog: "+e.toString());
    	}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      //Back button: just check in case we are showing the artists' list
      if(myArtistsListFragment != null && myArtistsListFragment.isVisible()) {
    	  
	      if (keyCode == KeyEvent.KEYCODE_BACK) {
	        //Ask the user if they want to quit
	        new AlertDialog.Builder(this)
	          //.setIcon(android.R.drawable.ic_dialog_alert)
	          .setTitle("Disconnect")
	          .setMessage("Are you sure you want to go back? You might have to re-download your track list. ")
	          .setNegativeButton("No", null)
	          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    PlayerUI.this.finish();
	                }
	            }).show();
	        
	        return true;
	      }
      }
      
      //Default back operation
      return super.onKeyDown(keyCode, event);
    } 
    
    private class XMLStringParser extends AsyncTask<String, Void, Void> {
    	private List<Track> tracks;
    	private MyXMLParser XMLParser;
    	
		@Override
		protected Void doInBackground(String... XMLString) {
			try {
				XMLParser = new MyXMLParser();
				tracks = XMLParser.parse(new StringReader(XMLString[0]));
				//Log.d(null, "\nParsed! Size: "+tracks.size());
					
				//Try to parse into tree structure
				RemoteboxData.parseTrackList(tracks);
				
				//Tell PlayerUI that the list is done parsing
				sendToUI(3, "");
			}
			catch(Exception e) {
				sendToUI(0, "Error while parsing track list.");
			}
			
			return null;
		}
		
		private int sendToUI(int what, String txt) {
			try {
				handler.sendMessage(handler.obtainMessage(what,txt));
			} catch(Exception e)
			{
				//Log.d(null, "AsyncTask (XMLStringParser): Error while sending message to UI");
			}
			return 0;
		}
    }
}
