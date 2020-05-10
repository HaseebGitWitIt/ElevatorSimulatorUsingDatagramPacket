import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class TestHost implements Runnable {    
    // The port to receive packets on
	private int RECEIVE_PORT; 
	
	// The destination port for all messages
	private int DEST_PORT;

	private InetAddress address; // The address to receive and send packets on

	// The DatagramSockets and DatagramPackets used for UDP
	private DatagramSocket sendSocket, receiveSocket;

	// Expected number of messages to receive
	private int expectedNumMessages;
	
	private boolean response;

	/**
	 * TestHost
	 * 
	 * Constructor
	 * 
	 * Creates a new TestHost object.
	 * Initializes the port to receive on to the given port number.
	 * Initializes the port and address for receiving.
	 * This object acts as a simple EchoServer.
	 * 
	 * @param expectedNumMessages  The expected number of messages to receive
	 * @param portNumber           The port number to receive packets from
	 * 
	 * @return None
	 */
	public TestHost(int expectedNumMessages, 
        	        int receivePortNumber,
        	        int destPort) {
	    // Set the port to the given port
		RECEIVE_PORT = receivePortNumber;
		DEST_PORT = destPort;
		
		// Initialize the send and receive sockets
		try {
			sendSocket = new DatagramSocket();

			receiveSocket = new DatagramSocket(RECEIVE_PORT);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		// Initialize the address to send information on
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.expectedNumMessages = expectedNumMessages;
		
		response = true;

	}

	/**
	 * teardown
	 * 
	 * Teardown this TestHost object.
	 * Set the packets to null and close the sockets.
	 * 
	 * @param  None
	 * 
	 * @return None
	 */
	public void teardown() {
		// Close the socket
		sendSocket.close();
		receiveSocket.close();
	}

	/**
	 * sendPacket
	 * 
	 * Send a DatagramPacket containing the given message
	 * to the given port at the given address. Prints out
	 * information about the packet being sent.
	 * 
	 * @param msg      The message to place in the DatagramPacket
	 * @param address  The address to send the message to
	 * @param portNum  The port number to send the message to
	 * 
	 * @return void
	 */
	public void sendPacket(byte[] msg, InetAddress address, int portNum) {
		// Create the DatagramPacket to send
		// Send the created response to the given port number
		DatagramPacket sendPacket = new DatagramPacket(msg, msg.length, address, portNum);

		// Print out information about the packet being sent
		System.out.println("Test: Sending packet:");
		System.out.println("Test: To address: " + sendPacket.getAddress());
		System.out.println("Test: Destination port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		System.out.println("Test: Length: " + len);
		System.out.print("Test: Containing (as bytes): ");
		System.out.println(Arrays.toString(sendPacket.getData()));

		// Send the packet
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Test: packet sent");
	}

	/**
	 * receivePacket
	 * 
	 * Waits on the port for a DatagramPacket containing
	 * a message of the given expected length. Prints out
	 * information about the packet received.
	 * 
	 * @param expectedLen  Expected length of the DatagramPacket being sent
	 * 
	 * @return void
	 */
	public DatagramPacket receivePacket(int expectedLen) {
		// Initialize the DatagramPacket used to receive requests
		byte data[] = new byte[expectedLen];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Test: Waiting for Packet.\n");

		// Wait on the DatagramSocket to receive a request
		try {
			System.out.println("Test: Waiting...");
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("Test: IO Exception: likely:");
			System.out.println("Test: Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		// Print out information about the received packet
		System.out.println("Test: Packet received:");
		System.out.println("Test: From address: " + receivePacket.getAddress());
		System.out.println("Test: From port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		System.out.println("Test: Length: " + len);
		System.out.print("Test: Containing (as bytes): ");
		System.out.println(Arrays.toString(data) + "\n");
		
		return(receivePacket);
	}

	/**
	 * getPortNum
	 * 
	 * Return the current port number used to
	 * receive information
	 * 
	 * @param  None
	 * 
	 * @return The port number information is received on
	 */
	public int getPortNum() {
		return (RECEIVE_PORT);
	}

	/**
	 * getAddress
	 * 
	 * Return the address currently used to send data
	 * through.
	 * 
	 * @param  None
	 * 
	 * @return The address that data is received through
	 */
	public InetAddress getAddress() {
		return (address);
	}

	/**
	 * setExpectedNumMessages
	 * 
	 * Set the expected number of messages to receive
	 * before closing the TestHost.
	 * 
	 * @param expectedNum  The expected number of messages to receive
	 * 
	 * @return void
	 */
	public void setExpectedNumMessages(int expectedNum) {
		expectedNumMessages = expectedNum;
	}
	
	/**
	 * disableResponse
	 * 
	 * Disables this TestHost from sending responses to received messages.
	 * 
	 * @param  None
	 * 
	 * @return None
	 */
	public void disableResponse() {
	    response = false;
	}
	
	/**
     * enableResponse
     * 
     * Enables this TestHost to send responses to received messages.
     * 
     * @param  None
     * 
     * @return None
     */
    public void enableResponse() {
        response = true;
    }

	/**
	 * run
	 * 
	 * Overridden
	 * 
	 * Run the TestHost when called in a Thread object.
	 * Repeats the following for the number of expected messages:
	 *     Receive packet
	 *     Send packet with the same contents back to the sender.
	 *     
	 * @param  None
	 * 
	 * @return void
	 */
	@Override
	public void run() {
		int expectedLen = 100;
		
		DatagramPacket temp;

		// Receive and echo the expected number of messages
		for (int i = 0; i < expectedNumMessages; i++) {
			temp = receivePacket(expectedLen);
			if (response) {
			    sendPacket(temp.getData(), temp.getAddress(), DEST_PORT);
			}
		}

	}
}
