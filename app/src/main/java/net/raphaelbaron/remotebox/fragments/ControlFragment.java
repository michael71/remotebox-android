package net.raphaelbaron.remotebox.fragments;

import net.raphaelbaron.remotebox.PlayerUI;
import net.raphaelbaron.remotebox.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;


public class ControlFragment extends Fragment {
	private ImageButton playButton;
	private ImageButton pauseButton;
	private ImageButton stopButton;
	private ImageButton getButton;
	private ImageButton prevButton;
	private ImageButton nextButton;
	private SeekBar volumeBar;
	
	private PlayerUI act;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	
    	act = (PlayerUI) this.getActivity();
    	
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.control_fragment, container, false);       
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	//Set up callbacks. XML won't work here, because it would take the Activity's methods
    	playButton = (ImageButton) getActivity().findViewById(R.id.playButton);
    	pauseButton = (ImageButton) getActivity().findViewById(R.id.pauseButton);
    	stopButton = (ImageButton) getActivity().findViewById(R.id.stopButton);
    	getButton = (ImageButton) getActivity().findViewById(R.id.getButton);
    	prevButton = (ImageButton) getActivity().findViewById(R.id.previousButton);
    	nextButton = (ImageButton) getActivity().findViewById(R.id.nextButton);
    	volumeBar = (SeekBar) getActivity().findViewById(R.id.volumeBar);
    	
    	playButton.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			act.sendToNetThread(1, "play");
			}
		});
    	pauseButton.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			act.sendToNetThread(1, "pause");
			}
		});
    	stopButton.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			act.sendToNetThread(1, "stop");
			}
		});
    	getButton.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			act.showDialog("Loading", "Downloading file list...", true);
    			act.sendToNetThread(2, "");
			}
		});
    	prevButton.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			act.sendToNetThread(1, "prev");
			}
		});
    	nextButton.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			act.sendToNetThread(1, "next");
			}
		});
    	
    	volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    		
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				float percentage = (float) volumeBar.getProgress()/100;
				
				act.sendToNetThread(1, "vol "+percentage);
				Log.d(null, "sending vol "+percentage);
				
			}
    		
    	});    	
    	
    }
}