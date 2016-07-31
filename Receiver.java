package pa2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class Receiver {
	
	
	
	//sends ACKS back to Sender
	//
	public static void sendACK(int missedPkt, DatagramSocket socket, InetAddress address, int port) throws IOException {
        
        byte[] ACKet = intToByte(missedPkt);
        
        DatagramPacket acknowledgement = new  DatagramPacket(ACKet, ACKet.length, address, port);
        socket.send(acknowledgement);
        
    }

	//converts ints to bytes
	//
	public static byte[] intToByte(int i ){
		byte [] arr = new byte [4];
		arr[0] = (byte) (i >> 24);
		arr[1] = (byte) (i >> 16);
		arr[2] = (byte) (i >> 8);
		arr[3] = (byte) (i);
		
		return arr;
	 }
	
	
	

	public static void main(String[] args) throws IOException {
		
		float percentLoss = (float).30;
		
		
		//percent loss a CLA, otherwise 30%
		if(args.length==0){
			System.out.println("Default percentage loss: 30%");
			}else{
				String str = args[0];
				int result = Integer.parseInt(str);
				percentLoss = (float)result/100;
					
		}
		
		File file;
        FileOutputStream toFile = null;
        String host = "localhost";
		int port = 3000;
		DatagramSocket socket = new DatagramSocket(port);
        InetAddress address;
        InetAddress senderAddress= InetAddress.getByName(host);
        int senderPort = 0;
        int expectedSeqNum = 0;
        boolean gotFileName = false;
        boolean done = false;
        socket.setSoTimeout(7*1000);
        
        
        
        System.out.println("Ready to receive packets!");
        
        
        
        while(!done){
        	
        	
        	byte[] msg = new byte[1124];
        	DatagramPacket receivedPacket = new DatagramPacket(msg, msg.length);
        	
        	
        	//if longer than 7 seconds and no communication with Sender,
        	//program terminates.
        	//
        	try{
    			socket.receive(receivedPacket);
    			}catch (SocketTimeoutException e) {
    				System.out.println("Timeout. . . terminating Receiver program");
    				System.exit(0);
    				//continue;
    			}
        	
        	
        	//"throws away" predetermined percentage of packets
        	//
        	if(Math.random()<percentLoss){
        		continue;
        	}
        	
        	
        	msg = receivedPacket.getData();
        	senderAddress = receivedPacket.getAddress();
            senderPort = receivedPacket.getPort();
        	int index = ByteBuffer.wrap(msg).getInt();
           
        	
        	//sends ACK if not in sequence
        	//
        	if(expectedSeqNum!=index){
        		//System.out.println("Expected: "+expectedSeqNum + ", Received: "+index);
        		sendACK(expectedSeqNum, socket, senderAddress, 3501);
        		continue;
        	}
        
        	//strips filename and creates new files
        	//
        	if(index==0 && gotFileName == false){
        		gotFileName = true;
        		byte [] tmp = new byte[100];
        		for(int i =0;i<100;i++){
        			tmp[i]=msg[i+8];
        		}
        		
        		String str = new String(tmp, "UTF-8");
        		str = str.split("\n")[0].trim();
        		System.out.println("Creating file: copy_"+ str);
        		file = new File("/Users/stenknutsen/Desktop/output/copy_"+str);
        		toFile = new FileOutputStream(file); 
        	}
        	
        	
        	
        	//extract data and write to file
        	//
        	byte[] payload =new byte[1024];
			for(int i=0;i<payload.length;i++){	
				payload[i]=msg[i+100];
			}
			toFile.write(payload);
        	
			
			//advance sequence
			//
			expectedSeqNum++;
        	
        	
        	
        	//end of file flag
        	//
        	if(msg[4] == 1){
        		//System.out.println("End found at: "+ByteBuffer.wrap(msg).getInt() );
        		toFile.close();
        		gotFileName = false;
        		byte[] ACKet = new byte[8];
        		ACKet[4] = (byte)0x2;
        		//send reset ACKs to Sender
        		//
        		for(int i =0;i<150;i++){
        	        DatagramPacket acknowledgement = new  DatagramPacket(ACKet, ACKet.length, senderAddress, 3501);
        	        socket.send(acknowledgement);	
        		}
        		
        		//reset sequence
        		//
        		expectedSeqNum = 0;
       
        	}
        	
  	
        
        }//end while     

	}

}
