package net.raphaelbaron.remotebox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/*
 *   Messages sent to UI Thread:
 *     -1: debug info, string
 *   	0: error, string
 *   	1: success, string
 *      2: success, xmlliststring
 *      10: connection stablished
 */

/*
 *   Messages received from UI Thread:
 *     -1: debug info, string
 *   	0: error, string
 *   	1: send, string
 *   	2: get xmlliststring, ""
 *   	3: connect, string (address)
 *   	4: disconnect
 *   	5: get volume
 */


public class NetThread extends Thread {
	
	private boolean debug = true;
	
	//static public Handler handler;
	private Handler handler;
	
	//static private Handler UIHandler;
	private Handler UIHandler;
	
	private String ip;
	
	//Socket stuff
	Socket sock;
	BufferedReader in;
	OutputStream os;

	//Append Exception.toString on error popups?
	private String appendException(Exception e) {
		return debug ? " ("+e.toString()+")" : "";
	}
	
	public NetThread(String ipAddr) {
		//Log.d(null, "NetThread Constructor");
		this.ip = ipAddr;
	}
	
	public void setUIHandler(Handler UIhandler) {
		this.UIHandler = UIhandler;	
		//Log.d(null, "NetThread setUIHandler");
	}
	
	private void connect(String ipAddr) throws Exception {
		try {			
			sock = new Socket();
    		sock.connect(new InetSocketAddress(ipAddr, 30666), 2000);
			//sock.connect(new InetSocketAddress("192.168.0.1", 30666), 2000);
			//sock = new Socket("192.168.0.104", 30666);
			//sock = new Socket(ipAddr, 30666);
    		//sock.setReuseAddress(true);
    		os = sock.getOutputStream();
    		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    		
    	} catch(Exception e) {
    		throw new Exception(e); 
    	}
	}
	
	private void disconnect() {// throws Exception {
		try {
			os.close();
			in.close();
			sock.close();
		} catch(Exception e) {
    		//sendToUI(0,"Error on disconnect()"+e.toString());
    		//throw new Exception(e);
    	}
	}
	
	@Override
	public void run() {
				
		try {
			this.connect(this.ip);
			sendToUI(10, null);
			
			//Get volume so that seekbar is synch'ed with rhythmboxe's vol
			try {
				send("vol");
				sendToUI(5, recv().toString());
			}
			catch(Exception e) {}
			
		} catch(Exception e) {
			sendToUI(0, "Error while connecting: connection refused. Please make sure " +
					"the server is running on "+this.ip+".");// Error:"+appendException(e));
			//return;
			
		}
			 
		//Main loop: waits for a Message from a thread and sends and receive data through sockets to server
		Looper.prepare();
		
		this.handler = new Handler() {
			
			private String message;
			private String size;
			
			public void handleMessage(Message msg) {
	
				//1 => send message to server and get response
				if(msg.what == 1) {
						try {
							send(msg.obj.toString());
					
							try {
								message = recv();
								
								//Is it a disconnection?
								if(message == null) {
									sendToUI(0, "Connection was broken. " +
											"Click again to try to reconnect.");
									disconnect();
								}
								else if(message.equals("ok")) {
									sendToUI(1, "Everything worked fine.");
								}
								else {
									sendToUI(0, "Unrecognized server response ("+message+").");
								}
							} catch(Exception e) {
								sendToUI(0, "Something went wrong while receiving data. Click again to try to " +
										"reconnect.");//+appendException(e));
								
							}
						} catch(Exception e) {
							sendToUI(0, "Error while sending request: not connected. Click again to try to reconnect. " +
									"Please also make sure the server is running and your WiFi is on.");//+appendException(e));
							disconnect();
						}
				}
				
				//2 => get file list (xml)
				else if(msg.what == 2) {
					//Send a get request, receive 2 chunks: (int)length, (String) xml, and send it back to UI
					try {
						send("list");
						
						try {
							
							size = recv();
							
							sendToUI(1, "Size: "+size.toString());
							
							message = recv();
							
							sendToUI(2, message);

						} catch(Exception e) {
							sendToUI(0, "Error while receiving file list.");//+appendException(e));
							disconnect();
						}
					} catch(Exception e) {
						sendToUI(0, "Error while sending request: connection lost.");//+appendException(e));
						disconnect();
					}
				}
				
				//3 => Connect to server
				else if(msg.what == 3) {
					//Send a get request, receive length, xml string and send it back to UI
					try {
						connect(ip);
						if(sock != null && sock.isConnected()) {
							sendToUI(1, "Connected!");
						}
					} catch(Exception e) {
						sendToUI(0, "Error while connecting: connection refused. Please make " +
								"sure the server is running.");//+appendException(e));
					}
				}
				
				//4 => disconnect
				else if(msg.what == 4) {
					try {
						disconnect();
					} catch(Exception e) {
						//sendToUI(0, "Error while disconnecting: "+e.toString()); 
					}
				}
			}			
		};
		
		Looper.loop();
	}
	
	/*
	 * recv():
	 * 		behavior: 
	 * 			return String if everything is ok.
	 * 			throw Exception if there was a problem reading the BufferReader or received a null string (disconnection)
	 */
	
	private String recv() throws Exception {
		String message;
		try {
			message = in.readLine();
			
			return message;
		
		} catch(Exception e) {
			throw new Exception(e);
		}
	}
	
	/*
	 * send():
	 * 		behavior:
	 * 			if not connected, tries to connect before sending.
	 * 
	 * 			return 0 on error-free execution.
	 * 			throw Exception if the reconnection was not successful.
	 * 
	 * 
	 */
	
	private int send(String txt) throws Exception {
		//Is the socket connected? If not, try to connect

		if(sock == null || !sock.isConnected() || sock.isClosed()) {
			try {
				this.connect(this.ip);
			}
			catch(Exception e) {
				throw new Exception("Error while trying to reconnect.");//+e.toString());
			}
		}
		
		if(sock == null)
			throw new Exception("Error while sending command: socket is null!");
		else if(!sock.isConnected())
			throw new Exception("Error while sending command: socket is not connected!");
		else if(sock.isClosed())
			throw new Exception("Error while sending command: socket is closed!");
		
		try {

			this.os.write(txt.getBytes());
			this.os.flush();

			return 0;
		} catch(Exception e) {	
			throw new Exception("Error while writing to output buffer.");//+e.toString());
		}
	}
	
	private int sendToUI(int what, String txt) {
		try {
			this.UIHandler.sendMessage(this.UIHandler.obtainMessage(what,txt));
		} catch(Exception e)
		{
			//How to display this error?
			//Case: NetThread.UIHandler is not set
		}
		return 0;
	}
	
	public Handler getHandler() {
		return this.handler;
	}
	
	private void debug(String msg) {
		sendToUI(-1, msg);
	}
	
}
