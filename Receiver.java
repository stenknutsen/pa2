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
        FileOutputStream toFile;
        String host = "localhost";
		int port = 3000;
		
		DatagramSocket socket = new DatagramSocket(port);
		
        InetAddress address;
        InetAddress senderAddress= InetAddress.getByName(host);
        
        
        boolean done = false;
        System.out.println("Ready to receive packets!");
        while(!done){
        	byte[] msg = new byte[1124];
        	DatagramPacket receivedPacket = new DatagramPacket(msg, msg.length);
        	
        	socket.receive(receivedPacket);
        	msg = receivedPacket.getData();
        	
        	int index = ByteBuffer.wrap(msg).getInt();
            //System.out.println(ByteBuffer.wrap(msg).getInt());
        	
        	if(index==0){
        		byte [] tmp = new byte[100];
        		for(int i =0;i<100;i++){
        			tmp[i]=msg[i+8];
        		}
        		
        		String str = new String(tmp, "UTF-8");
        		str = str.split("\n")[0].trim();
        		System.out.println(str);
        	}
        	
        	
        	
        	
        }
        
        
        
        

	}

}
