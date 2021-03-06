package pa2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/*
*
*
*
*
*	
*
*	
*
*
*
*/

public class Sender implements Runnable {
	
	//Array list to store packet buffer
	//
	//
	public static ArrayList<byte[]> allPackets = new ArrayList();
	public static int ACKnum = 0;
	public static boolean endOfFile = false;
	public static float percentLoss = (float).30;
	
	
	//returns 4-byte array representation of int
	//
	public static byte[] intToByte(int i ){
		
		byte [] arr = new byte [4];
		arr[0] = (byte) (i >> 24);
		arr[1] = (byte) (i >> 16);
		arr[2] = (byte) (i >> 8);
		arr[3] = (byte) (i);
		return arr;
	}
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		
		
		//percent loss a CLA, otherwise 30%
		if(args.length==0){
			System.out.println("Default percentage loss: 30%");
		}else{
			String str = args[0];
			int result = Integer.parseInt(str);
			percentLoss = (float)result/100;
			
		}
		
		
		
		
		
		
		
		//start ACKListener thread
		//
		Sender ACKListener = new Sender();
        Thread thread = new Thread(ACKListener);
        thread.start();
        
        
        
        //grab list of files from a specified folder
        //
        File folder = new File("/Users/stenknutsen/Desktop/IO_folder/");
        File[] listOfFiles = folder.listFiles();
       
        
        //send each file name in list to send
        //
        for (File file : listOfFiles) {
            
        	if (file.isFile()) {
                if(file.getName().equals(".DS_Store")){//I think this is just a 'Mac thing'
                	continue;
                }
                try {
					sendFile(file.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}    
            }
        	
     
        }
        
        
        
       System.out.println("All files sent. Shutting down sender. . . . "); 
       System.exit(0);

	}//end main
	
	

	//takes filename, "packetizes" data and stores in allPackets buffer
	//
	//	
	public static void packetizeFile(String fileName) throws IOException{
		
		System.out.println("Opening: " + fileName);
		
		    File file = new File("/Users/stenknutsen/Desktop/IO_folder/"+fileName);
		    FileInputStream is = new FileInputStream(file);
		    byte[] chunk = new byte[1024];
		    fileName = fileName+"\n";
		    byte[] name = fileName.getBytes();
		    fileName = fileName.trim();
		    
		    
		    int totalFileLength = (int)file.length();
			System.out.println(fileName + " is " + totalFileLength+" bytes.");
			
			
			int  numPackets = (int) Math.ceil((float)totalFileLength/1024);
			System.out.println("Total pkts needed to xmit: "+ numPackets +"\n");//this will be the length of the packet array
		    
		    
		    int chunkLen = 0;
		    int sequenceNum = 0;
		    
		    while ((chunkLen = is.read(chunk)) != -1) {
		        
		    	byte[] payload = new byte[1124];
		    	byte[] seq = intToByte(sequenceNum);
		    	
		    	//copy seq numbers to payload
		    	for(int i=0;i<4;i++){
		    		payload[i] = seq[i];
		    	}
		    	
		    	//copy file name to payload
		    	for(int i=0;i<name.length;i++){
		    		payload[i+8] = name[i];
		    	}
		    	
		    	//copy data tp payload
		    	for(int i=0;i<1024;i++){
		    		payload[i+100] = chunk[i];
		    	}
		    	
		    	
		    	
		    	//add end byte
		    	//
		    	if(sequenceNum == numPackets-1){
		    		payload[4] = (byte)0x1;
		    	}
		    	
		    
		    	
		    	
		    	
		    	allPackets.add(payload);
		    	
		    	
		    	
		    	sequenceNum++;
		    	
		    }		
	}
	
	
	
	//"packetize" file and send to receiver
	//
	public static void sendFile(String fileName) throws IOException{
		
		String host = "localhost";
		int port = 3000;
		DatagramSocket socket = new DatagramSocket(3500);
        InetAddress address = InetAddress.getByName(host);
		int startIndex = 0;
		int window = 10;
		
		packetizeFile(fileName);
		
		
		
		
		
		
		
		while(!endOfFile){
		
		//System.out.println("Start index: " +ACKnum);
		for(startIndex=ACKnum;startIndex<(Math.min(ACKnum+window, allPackets.size()));startIndex++){
			
			
			
			DatagramPacket sendPacket = new DatagramPacket(allPackets.get(startIndex), allPackets.get(startIndex).length, address, port);
			socket.send(sendPacket);
			
		}
		
		//throttle back on sender
		//
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		}//end while
		
		System.out.println("File Sent!");
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        //clear for next file
        //
		allPackets.clear();
		//close port
		socket.close();
		//reset acknum
		ACKnum = 0;
		//reset EOF
		endOfFile = false;
		
	}
	
	
	
		
	
	
	
	
	
	
	//ACKListener
	//
	//
	public void run() {
			
	       
		
		while(true){ 
		        int senderPort = 3501;
		        //I picked size 8 for ACK packet. . . 
		        //
		        byte[] message = new byte[8];
		        DatagramPacket packet = new DatagramPacket(message, message.length);
		        DatagramSocket socket = null;
		        
			        try{
			           socket = new DatagramSocket(senderPort);
			        }catch (SocketException e) {
			            e.printStackTrace();
			            System.out.println("Socket excep");
			        }
			        try {
						socket.setSoTimeout(10*1000);
					} catch (SocketException e1) {
						e1.printStackTrace();
					}
			        
			        
	        
			        try{
						socket.receive(packet);
						}catch (IOException e) {
							System.out.println("Timeout. . . terminating Sender");
							socket.close();
							System.exit(0);
							
						}
	        
			        
			        //extract ACK info here
					//       
			        message = packet.getData();
			        if(Math.random()<=percentLoss){
		        		socket.close();
			        	continue;
		        	}
			       
			       
			        if(message[4]==2){
			        	synchronized(this){
					        endOfFile = true;
					        }
			        }else{
			       
			        	
			        synchronized(this){
			        ACKnum = ByteBuffer.wrap(message).getInt();
			        }
			        }
			        
	        socket.close();
			
			}
		
		

		}//end run (ACKlistener)

}
