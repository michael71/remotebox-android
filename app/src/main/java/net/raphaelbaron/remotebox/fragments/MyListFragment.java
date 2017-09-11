package net.raphaelbaron.remotebox.fragments;

import net.raphaelbaron.remotebox.PlayerUI;
import net.raphaelbaron.remotebox.R;
import net.raphaelbaron.remotebox.data.RemoteboxData;
import net.raphaelbaron.remotebox.models.Node;
import net.raphaelbaron.remotebox.models.TrackNode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MyListFragment extends Fragment {

	public static final int ARTISTSLIST = 0x0;
	public static final int ALBUMSLIST = 0x1;
	public static final int TRACKSLIST = 0x2;
	
	//private PlayerUI act;

	private ListView listView;
	private ArrayAdapter<Node> arrayAdapter;
	private Object[] items;
	
	private int scrollPosition, top;
	
	/*
	 * listType:
	 * 		0: RemoteboxData.root (list artists)
	 * 		1: RemoteboxData.root.children.get() (list albums)
	 * 		2: RemoteboxData.root.children.get().children.get() (list tracks)
	 * 		 
	 */
	int listType;
	
	private Node rootNode;
	
	public void setListView() {
		
		items = rootNode.children.toArray();
		
		//arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, items);
		arrayAdapter = new ArrayAdapter(getActivity(), R.layout.my_list_item, R.id.my_list_item_text, items);
		 
		listView.setAdapter(arrayAdapter);
		
		//Set onClick callback
		if(listType == ARTISTSLIST) {
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            	PlayerUI act = (PlayerUI) getActivity();
	            	//act.debug("\nCliqued on artists position: "+position);
	            	act.startMyAlbumsListFragment(position);
	            }
	        } );
		}
		
		else if(listType == ALBUMSLIST) {
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		          	PlayerUI act = (PlayerUI) getActivity();
		           	act.startMyTracksListFragment(getArguments().getInt("artistID"), position);
		        }
			} );
		}
		
		else if(listType == TRACKSLIST) {
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		          	PlayerUI act = (PlayerUI) getActivity();
		           	
		           	//Send play
		           	act.sendToNetThread(1, "goto "+((TrackNode) rootNode.children.get(position)).url);
		        }
			} );
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Is there already a list?
		if(savedInstanceState != null) {
        	//Restoring a saved instance
			Log.d(null, "MyListFragment: restoring a saved instance");
			//Log.d(null, "MyListFragment: restored scroll position: "+savedInstanceState.getInt("ScrollPosition"));
			
			scrollPosition = savedInstanceState.getInt("ScrollPosition");
			
        } else {
        	//New instance
        	Log.d(null, "MyListFragment: starting a new instance");
        	
			scrollPosition = 0;
        }
		
		//Get root node
		listType = getArguments().getInt("listType");
		
		//None: show artists
		if(listType == ARTISTSLIST) {
			rootNode = RemoteboxData.root;
		}
		
		//Artist: show albums
		else if(listType == ALBUMSLIST) {
			rootNode = RemoteboxData.root.children.get(getArguments().getInt("artistID"));
		}
		
		//Album: show tracks
		else if(listType == TRACKSLIST) {
			rootNode = RemoteboxData.root.children.get(getArguments().getInt("artistID")).children.get(getArguments().getInt("albumID"));
		}
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	//Save scroll position
    	outState.putInt("ScrollPosition", listView.getFirstVisiblePosition());
    	
    }
	
	@Override
	public void onPause() {
		super.onPause();
		
    	//Save scroll position in case fragment is not destroyed but added to backstack
    	scrollPosition = listView.getFirstVisiblePosition();
    	Log.d(null, "Saved scroll position: "+scrollPosition);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		listView = (ListView) getActivity().findViewById(R.id.listView1);
		setListView();
		
    	Log.d(null, "Restoring scroll position: "+scrollPosition);
    	listView.setSelectionFromTop(scrollPosition, 0);
    	
    	//Set PlayerUI header text
    	if(listType == ARTISTSLIST) {
    		((PlayerUI) getActivity()).setHeader("Artists");
    	}
    	else {
    		((PlayerUI) getActivity()).setHeader(rootNode.name);
    	}
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.mylist_fragment, container, false);
		
		// Inflate the layout for this fragment
		return v;
	}
}
