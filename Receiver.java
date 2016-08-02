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
        socket.setSoTimeout(10*1000);
        
        
        
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
    				socket.close();
    				System.exit(0);
    				//continue;
    			}
        	
        	
        	//"throws away" predetermined percentage of packets
        	//
        	if(Math.random()<=percentLoss){
        		
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
/*
 * WHAT, ME WORRY?
                                                                                       . . ..              .                     ..... .  .     ..   .....       
                                                                                      .I. ..                                     ..                    ...      
                                                                            ..... ....MM....                     ...             ..                   ....      
                                                                        . ........=DNNMM~...      .               ..                              .   ....      
                                                             . . ..........~OND8DNNDDNMNN...                                                          ....      
                                                             .....$DMNDDNNNNDDDDNNNDDNNNN.. .....                                                     ....      
                                                           ..:ODNDNNNND888OOOO8ONNNNNNNNNMN...... ..... ..                                            ....      
                                           .. .. ...  . ..,DNNNNNNNNNNNDNNNNNND8OOON8DNNNNNMMNM?.,ZMMMMNN. .                         .                ....      
                                        . ........... . .MNNNNNNNNNNNNNNNZ8DDNNNND88OOOZONDNMNNODNNNNN=.....                       ..  ..             ....      
                                        ...ZD8DNNNNNNNO$NMNNNNNNNNNNNNDNNNN8ZZZOZDNNZOZODNOOZDDDNDNNMNMM..... ..                       ..             ....      
                                        .ODDZDDDNNNNNNNNNN88DNNDDNNDD8NNNNNNDDD8OOZOZZ$$88D8ONDODDNNNMNMNDDODO......                 .                ....      
                                       .7......:NNNNNNDDN88ODNNNNNNNDNNNNNNNNNNDNDDZZ$ZZZOZZZZODMNNNNNNNNN8DNN8NN?..  .              .                ....      
                                       ......DDNNNNNNNDNOOOMNDDDNMND8NNNNNDNNNMNNNN8DDD8OZZOZOZOZ8Z88NNMND8ODD8MDDMN....                               ..       
                                        ..DNNNNNNDD8DNNDDONZONNNNNND8NNNNNNDNNNNODO8D88OZ8DDNNDND8888ND88NDZNOZNDMDNN:..                               ..       
                                       .,NNNNNNODDNDNNNN8N$$8DNNNNNN$ZDDNNNNNDNDNDN8DD88ODNNNNNNDOOZDD$NDNN88DN8ZDNDNNM..                              ..       
                                      ..NNNNNDDNZNN8NNDN8N$$NDNNNNNNNN8ZONNDD8DNDNNNN8DDONDDD8DZNDDOOOD8ZD8NNODDNNZNNDMNI...                           ..       
                                    ...NNNNMDDNDND8DNNNNDDOZDDNNNMNNNNNNZ8$ODODNNDDNNNDNNO8N8DDNDNDDZDD$OOO88N8OZDDZNNONNM..                   ..      ..       
                                   ...INNNNNNN8N8NOOOONNNOOOD8NNDNNDNNDDNDNN8OZONNO8DND8DNO$DND8ODZO8O$8O8$8O$N88ZZNODNO8NM.....                       ...      
                                     .NNNNDNDO8DND8O$ZDNODN8ODDNNNNNNNDDNDO8NN$$ONNOO88DONNODI$N$ZZZZ$$$Z7ZZOZDODN8$8DDDO8NN....  .                    ..       
                                    ..NNDNDNOOZNNDN8ZZNDONDNNND88$8NNMM8NNZ$NON7ZNNN8ZZ7Z$ODNZ87$D$D7$$ZZ$$$$$8OIDDDZ888DOONM... .                     ..       
                                    .,NNNNDNO8NNDNOD8DNDNND88DDO8D8NN8NDONMZ7NDDI8DNNO7$OZZONNZN88$$OO77$$$$$O$DDONNNN8OODZDN$..   .        .          ..       
                                    ..NMNNNNOONNNNND8NNNNNNNDNNNZODN87NDDONNOONDNNOONN8NZO88$ONNDND8DDDZ$$ZD8OOON8OONDDDZDZDD8+...          ..         ..       
                                    ..8NNNNNNZNNNNNNNNNNNNNNNODNN8OOZO$OZND8NNNNNNDN8NNN8NDZ8ZZZ8NNNNZD8O$ZZ88DDNN8D78NN8ONOONND....                  ....      
                                    ...:NNNDDN8DNNNNNNNNNNNNNNDNNNNM8OOO8ONNNNNNDNNDDODNNMDNN88ZOZ$ZN8NDZDZZZNDDD8ON$8$NZ8ZDN8NNN....                 ....      
                                    ..DNNNNDODNDDNNNNNNDNNNNND?$8NNNNMMMNNNNNNNNNNNNNNNDDNDNNDNNOOD8NDDDOODODZDOZDZ8NN7DNOD88DONMD..               .  ....      
                                   ..7NNNNDDD88NNNNNNNNNN8NNNI?IIIII7DNDNNNNNNNNNNNNNDNNNNDDDDDDNO8ND8DND8D8ODODN8NDZ$ND88DDDZDODM..               .  ....      
                                   .:NNNNNDNDNNNNNMNDDNN$8NN$IIII7IIIIIII?INDNN??I?I????I+?=++++++??ONDODNNDDOD8NNND8NN8NNNND8ONNZN.. ...     .       ....      
                                  .INMNNNNN8NDNMNNNN$$8D$7DZ7IIIII7IIIIIIIIII?I?II?IIIIII???+?+++???????+8NDDDNDN8NDDNNN8DND$8ZNDZOI. ...     .       ....      
                                ..DNNNNNNNNNDNNNNNMDZ$Z$$$77777IIIIII7II7III7III7IIIIIIIIIII??????IIII??????8DDNDNNNDNNOZOO8ND8N88O$....            . ....      
                                .7NNNNNNNNDDNDNNNNDZZZZZ$7$7777I7I77I7I7I7777II7II7IIIIII?II??????II???III????+$$?I?I?I??78ODN8DDNO8D...            . ....      
                               ..NNNNNNNNNDDDNNNNDZZZZZZZZ$$$777777I7777II77II7IIII7III?I?II?????III?IIIIIIIIIIIIIIIII??IND8ZDNONNN8D~..               ..       
                                .MNMNDNNNNNDDNNDDDZZZZZ$Z$$$777III77777I7I7III7I7I7IIIIIIIII??I?IIII??III7777III7IIIIIIII?8OONDNDODN8?.                ..       
                                ,NNMN8NNNDNNNDNNND$ZOZZZZZZ$$7I7IIIII7777III77I777IIIII7I?I???IIIIIIIIIII7IIIIIIII7IIIIII?ZOOZDOOZONDO..               ..       
                                .ZNNNN8DNNNNNNNDNOZZZOZZZZ$$$777IIII7I77IIII7II7IIIIIIIII?IIII?????I?III7IIIIIIIII7IIIIIIIDO$7NNOZODND..              ....      
                                ..NNNND8NDNNNDNND$$ZZOOZZZZ$777IIIII7I7IIIIIIIIIII7IIIIIII????I??????IIIII7777I77IIIIIIII?OD8ZON$OD88N..              ....      
                            .....MMNNNOONNNNNNNND$$$$ZZZZZZ$777III7I7$$IIIIIIIII77IIIIII??II?????II??III7$7777777IIIIIIIII77NDOD$$DDDN,.             ....       
                            . ...NMNNNNNNNNNNNNN8$Z$$ZZZZZZ7DNNNNNNNNNN8OZ777III7II7IIIIII?I?+???????I777$$$7$7IIII7IIIIII7O$8DDZDDN8N?.             ....       
                             ..ZNMNNDNNNDNNNNNND$$Z$$$ZOOZODD8DOOOOOD8DD8O$777III7IIIIIIII?I?+?+?+I?7?I7Z$$$77IIIIIIIIIII77DZ$NN8N8NNN..               ..       
                            ..DNNNNDNNDNNNNNNNZ$$$$$ZZOOO88ZIIIIII7II7ZOOZZ$77IIIIIIIIIIIII7DDDNDD8DDDNDZ$Z$$77IIIIIII?II7ZNONO88$N8NN....            ....      
                            ..8DNNNNNDNNDNNNND$7$$$$ZOOOD8Z$IIIIIIII7I7ZOOO$77I777I7I77II$8NDNDD8????++8DO$Z$77I7?I?IIII77ONODD88ONDN8..              ....      
                            ...8MNNNNNNNNNNNNZ$$$$$ZZOOZZZ$I7IIIII7IIIIZO8ZZ77I77777III77ZDD8O7I????????I?O$$Z$7II?IIIIIIZZNOZ8D8D8DN=..              ....      
                            ...DNNNNNNNNNNNNOZ$$$7$ZOOOZOZZZ$$O8D8DNNNDD88OZ$$77II7III77$$ZZZ7I??I?????I??I7$$777IIIIIIII7ZODZZ$DDNND=..              ....      
                              .8NMNNNNNNNNNNOZ$$$$$$ZZOOZZ8N88DNNNNNND$7D8OOZZ77777I?I$ZZ$777I?IIIIIIIIIIIII777$$7IIIIII?IZDDDOD$8NNDN..   .          ....      
                        ........NNNNNMMNNNNOOZ$$$77II$OZ8DDDD$~NINNO8+OD?II8OZZ$ZI7I?IOZ$$$8DDDD8O$7II7I777II777$7IIIIII??+O8NO8NDNNNN..              ....      
      .                 ..D88888DNNNNNNNNN8OOZ$$77II??I778NNDZ7?Z$$7I77DD+ZOOZZ$7IIII7ZZ$$$$$ZDNDNNDNNZD8Z$7III7II777IIIII?7OZD$DNDNND..              ....      
       .             ...888ZZOOZO8O8ZNNNNN88OZ$$$$IIII$$$$Z$$Z$7I77Z$Z7??OOOZZZ$OII7I7O$$7?DN++N7DD88+7NDD$$I?IIIIII7IIII7I?8O$NNNNDNN....           .....      
                   ....88$$$$Z$ZZOOZ$7INNN888ZZ$7II?III777788D88888IIIIIO8OZZOO77IIZ?88$77?I?I77??++?$I7Z++???I?IIIIIIIII7I?ZZDNNDNDNN... ..         .....      
                   . .ONDNOZ$$$$O$ZZZ$7NNNN88OZ$ZZZ$$$7$77$7II77IIII7$$OOZZZZZZ7IIIII7$$O77$ZOOZ$IIII?7$I?I?I???I777777I777?O88NNDDOND.....           ....      
                   ...ODDD8Z$$7777$OZZ7NNNN88OZZZZ$$$7777777I77777777Z$Z$ZZZ$Z7787I7II$ZZ$$$$$IO888887I7IIIII77I7III7777777?DNDDNNND8OOOO8=.....      ....      
                    ..=8NDD8Z7777Z8DOO$8NNNO8OOZ$Z$$7O7$77777778I7I7I$7ZZZZZ$$$777777I$$$$77$7$77I7II7I7II7I7I7I77777777777IDN8OD77$$7777I77:..       ....      
            ..     ....DODNDO$$$ODDN8OZZDNNO8OOOZZZ7$77777I7I77I777III7777ZZOZ$7O777777$$$II77777$I77III77$II7II777I7777777ZDND$7777III$OI7?7,..      ....      
                   .....ZOND8Z$O8DDDO8O7I$8OO8OOZZ$777777I7II7I7777I7IIII78O8Z$7I77III7ZZZ$?IIII7777II7IIIIIIIIIIII777777$$NNN?7$7I?$77II77IOI..      ....      
      .              . ..Z8D8OZZ88DOZ$$7I$8OOOOOZ$77$777III??II777I7III?$888OZ7II?II??I7ZZZZI???IIIIII7III?II?I7I7II77777$ZNNNI$77D7+??I7$$I8?..      ....      
                       ...Z88OZ$888$77$7I7$O8OOZ$$$$7I7II???III7IIIII?ND888OZZ7I7III??II$77$7I???II7IIIIII??????IIII7777$$ZO77Z$7NDD8II7$Z$I8I..      ....      
                       ....O8D8Z$O8OO7$$$7?D8OOZZ$$$$7I7IIII7777IIII?NN88OZZ8OZ$7$$77777$$77I77III?77IIIIII????IIII7I777$ZZZ77Z$7?8D8ZI7ZO7DI,..      ....      
       .                ....O8DO77OD8DD$$7$D88OOZ$$$$$777$$$$$$$$$7??ND8ZZZZ$8O$7$7$77$Z$$777ZZII77IZZ$77II7III7III7777$$ZO77IZ$I+?8O$7$ZO$77...      ....      
                            .8ZD8Z7OO$ODZZ$8D88OOZ$$$$$$$$$$ZZZZO$II7DN8O$DNNDZO$7777$ZZ8D8Z$O?+?III$OZZ$77$77777777777$ZOO$IIII77IZOZ77OO87....      ....      
                          .....$ZD8DO$777$$7888OZOZ$$ZZZZZZOOO88IIIII7777778DDNDND8ZI?I777777IIIII777I8OOZ$$$77$777777$ZOOI7I?Z?77ZO7?IO8ZI.....      ....      
                               ..O8D8OZ$$ZZ$Z88OOZZZOZOOOOO8ODZ77I7I77$ZZ$$$$Z7I?I7I??I7I7777I777II77II88OOOZZZO$$$$$$ZZO7I77I??7O8OIIZ8$I,            ..       
        ..            ..       ...888O$Z$$$$ZOOOZZ$Z$$ZZOOO8OO8Z$$$7777$Z$$ZZ$7I?I77I?III77777IIIII7I777I888O8OO$$Z$$$ZOOIII7DZ$IIIIIOOI$...          ....      
                                ...88OZ$Z77IOOOOZ$$$ZZOZZZO8OO8O$$$$777$$$$$$$II?I77IIIIII777I7IIIIIII7777$8$ZO$Z$$$$$ZZIII78IOO7ZZ88I7I....           ...      
                                    ZOOZ$77$8OZOOZ$$$OOOOO8DOOOOZZ$7777777$777I?I777II?IIIII7IIIIIIII7777I7O77IZ$$I$Z$$$II7??IOZII?IZ+..              ....      
                                   ...8OO88~.ZOOOZ7$7ZOOO88NNNDOZZ$7777777$777III7777I?IIIIIIIIIIIIII77777Z7777Z$IIZ$$$I$7$7II$ZO78... .              ....      
                                      . .....OOOOO$7I7O7II??I??8NDZ$7777777IIIII?I7$II?I?IIIIIIIIIIII$$$$$Z777$$7$$ZZZ$I$$77$$$$O... ....              ..       
         ..  .              .           ......8OOZZ7I7$77IIIIII7IOOONNNDZ7III???I?7II???????I??IIII7ONDO??IIII77$$$$ZZOIII?I$$$....    .               ..       
                                            ..~88OOZ7I777777I7$$$IOOZ$$DN8DDD888O8$$Z$Z$ZOO88DNDDDOI?????IIIIIII$Z$$ZZ:888$ZO,......                   ..       
      .                                    ....Z88OOZ7777777777$ZZ$OOZZ$$$7?O++Z7$ODDD$7I$=?Z87$$$??IIIIIIIIII7$$7$ZZ:......                          ....      
                 ..                         ....?8O8OO$$Z$777777$O$$ZOOZZI$$$$IN?++DDD==IDZ$$$$Z$?IIIIII7I7777Z$7$$$$.......                          ....      
                                             .. ..888OOZZZ$777777$ZZ7I8OOO$7II7$$7$I$$$$$7?I$$$I?IIII?II7777$$$I$$O7........                          ....      
                                                .. ~88OOZZOZ$77777$ZZ$7O8ZZ$$7II??IIII????$ZZOIIII??II77777$$77$ZO$. .                                ....      
      .                                        . .. .,OOOZOZZ$$777$$ZZ$7788OOZZZZ$7I7II$$ZOO7IIIIIIIII7$7$7777$OO,..                                  ....      
      .                                              ...OOZZOOOZ$77Z$ZZ$Z7788ZOOOZ$$$$$$ZO$I7III?III777$$II7$ZO7....            .                     ....      
      .          ..                                  .. ...OZOZZZ$$$7$$$$$$$7IZ88OOOOOOZI7IIIIIIII777$II7$$ZO... ...                                  ....      
       ..                      ..                    ........ZOOOZZ$II7$$$$$7$II77777IIIIIIIIII77777777ZZZI.. ......                                  ....      
                         .                                .....ZOOZ$Z77I$777IIIII?????+??III?I777777$7$O,...                                          ....      
               ..                                          ......8OOZ$$77IIIIIIIIIIII?????II7I7777$$ZI. .. .                                          ....      
      .  ..                 .     .. ..                         ...$ZZ$$$777IIIII7I7II????II777$$Z7..       ...                                        .        
      .. ..                                                    . ....?ZZZ$77I77777II7I?IIII77777~...                                                            
      ..                               .       .               ... ....+ZZZ$777777IIIIII777I7$,.....  .. .                                              ..      
      ...                                               .       .       .=$ZZOZ$$Z$77$$777I:....                                                                
       ..                                 ..                           .......=77$$$77,.........                                                                
      ...   ..     ..  .     .   . .   .          ..     ..   ......  .  .  ......  ..    .             .     ..      .      ...   .....   ......      
 
 */
