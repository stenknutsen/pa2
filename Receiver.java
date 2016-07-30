package pa2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Receiver {

	public static void main(String[] args) throws IOException {
		
		
		File file;
        FileOutputStream toFile = null;
        String host = "localhost";
		int port = 3000;
		
		DatagramSocket socket = new DatagramSocket(port);
		
        InetAddress address;
        InetAddress senderAddress= InetAddress.getByName(host);
        
        int expectedSeqNum = 0;
        boolean gotFileName = false;
        boolean done = false;
        System.out.println("Ready to receive packets!");
       
        
        
        
        
        
        while(!done){
        	byte[] msg = new byte[1124];
        	DatagramPacket receivedPacket = new DatagramPacket(msg, msg.length);
        	
        	socket.receive(receivedPacket);
        	msg = receivedPacket.getData();
        	
        	int index = ByteBuffer.wrap(msg).getInt();
            //System.out.println(ByteBuffer.wrap(msg).getInt());
        	
        	if(index==0 && gotFileName == false){
        		gotFileName = true;
        		byte [] tmp = new byte[100];
        		for(int i =0;i<100;i++){
        			tmp[i]=msg[i+8];
        		}
        		String str = new String(tmp, "UTF-8");
        		str = str.split("\n")[0].trim();
        		System.out.println(str);
        		file = new File("/Users/stenknutsen/Desktop/output/copy_"+str);
        		toFile = new FileOutputStream(file); 
        	}//else continue?
        	
        	
        	
        	
        	
        	
        	//extract data and write to file
        	byte[] payload =new byte[1024];
			for(int i=0;i<payload.length;i++){	
				payload[i]=msg[i+100];
			}
        	toFile.write(payload);
        
        	//end of file flag
        	//
        	if(msg[4] == 1){
        		System.out.println("End found at: "+ByteBuffer.wrap(msg).getInt() );
        		toFile.close();
        		gotFileName = false;
        		expectedSeqNum = 0;
        		//break;
        	}
        	
        	
        	
        	
        	
        
        }//end while
        
        
        
        

	}

}