# pa2
##Sender Class:
The Sender Class opens files from the *sample_testcases* folder and transmits them to the Receiver. Along with the Receiver Class, it employs a Back-to-N protocol in order to ensure complete files are transmitted. In order to simulate network packet loss, a command-line argument of percentage loss may be entered; otherwise a default value of 30% is used.
	The method *packetizeFile()* reads the targeted file and stores it in a buffer in packet form, header information included. The method *sendFile()*  then uses a “sliding window” to transmit packets to the Receiver.
	A separate thread with the sole purpose of receiving ACKs from Receiver is used. ACKs received in this thread mediate the speed with which the sliding window in *sendfile()* advances. *Synchronized* blocks are used when writing to shared data structures, and the socket is set to timeout and shut the Sender program down after seven seconds of not receiving any ACKs. 

##Receiver Class:
The Receiver Class accepts packets from Sender and writes them to the folder receiver_output. Like Sender, Receiver takes an optional command-line argument for percentage packets lost; otherwise a default value of 30% is used.
	Receiver extracts the file name from the first packet sent and creates a new “copy” file in the *receiver_output* folder. Then, using a Back-to-N protocol, Receiver sends acknowledgment of having received packets in sequential order. As packets are received, the original data is extracted written directly to the correct file. After seven seconds of not receiving packets from Sender, the  socket times out and the program terminates. 
