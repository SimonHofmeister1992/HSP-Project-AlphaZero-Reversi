package src;

import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class ServerComm {
	
	
	// ************************************************************************************
	// * Attributes *
	// ***********************************************************************************
	
	//network
	private String IP;
	private int portNumber;
	private Socket socket;
	//Streaminfo
	private DataInputStream inStream;
	private DataOutputStream outStream;
	//Data
	private int groupNumber = 69;
	private String rawMap;
	private char playerIcon;
	private int phase;
	private int maxDepth;
	private int timeLimit;
	private int[] enemyTurn = new int[4]; //enemy turn info (player number, y, x coordinate, special field info)
	private char disqPlayer;
	
	
	
	// ************************************************************************************
	// * Constructor *
	// ************************************************************************************
	
	public ServerComm(int grNumber) 
	{
		this.groupNumber=grNumber;
		this.phase=1;
	}

	
	// ************************************************************************************
	// * Getter & Setter *
	// ************************************************************************************
	
	public String getRawMap()
	{
		return this.rawMap;
	}
	
	public int getPhase()
	{
		return this.phase;
	}
	
	public char getPlayerIcon()
	{
		return this.playerIcon;
	}
	
	public int getMaxDepth() //
	{
		return this.maxDepth;
	}
	
	public int[] getEnemyTurn()
	{
		return this.enemyTurn;
	}
	
	public char getDisqualifiedPlayer()
	{
		return this.disqPlayer;
	}
	
	public int getTimeLimit(){
		return this.timeLimit;
	}

	// ************************************************************************************
	// * Public Functions *
	// ************************************************************************************
	
	public void connect(String adress, int port) throws IOException
	{
		this.IP = adress;
		this.portNumber = port;
		//establishing connection
		//System.out.println("Connecting to Server on IP: " + this.IP + " on Port: " + this.portNumber);
		for (int tries = 0;; tries++)
		{
			try
			{
				this.socket = new Socket(this.IP, this.portNumber);
				//System.out.println("Server found");
				outStream = new DataOutputStream(socket.getOutputStream());
				outStream.flush();
				inStream = new DataInputStream(socket.getInputStream());
				
				break;
			}
			catch(IOException e)
			{
				//System.out.println("Server not found. Retrying " + (3-tries) + " time(s)");
				if (tries<3)
				{
					continue;
				}
				else
				{
					throw e;
				}
			}
		}
		//sending group number
		byte[] data = {((byte)groupNumber)};
		byte[] message = encodeMessage(1, data);
		sendMessage(message);
	}
	
	public void cleanup()
	{
		//System.out.println("Closing Socket");
		if (this.socket != null)
		{
			try {
				this.socket.close();
			} catch (IOException e) {
				System.err.println("Couldn't close Socket. Must be closed already...");
				System.err.println("Or something else is afield!!...");
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
	public int waitOnServer() //returns msgType
	{
		return decodeMessage();
	}	
	
	public void sendOwnTurn(int y, int x) //sends own turn to server
	{
		sendTurn(y, x, 0);
	}
	
	public void sendOwnTurn(int y, int x, int special) //sends own turn to server, special is special stone, num of player to switch, 20 for bomb, 21 for overwrite
	{
		sendTurn(y, x, special);
	}
	
	
	// ************************************************************************************
	// * Private Functions *
	// ************************************************************************************
	
    private int decodeMessage()  //decodes the message and relays the information
	{
		int msgType = 0;
		int msgLength;
		
		try 
		{
			msgType = this.inStream.read();
			msgLength = this.inStream.readInt();
			if (msgType == 2) //map
			{
				byte[] data = new byte[msgLength];
				this.inStream.read(data, 0, msgLength);
				
				this.rawMap = new String(data);
				
				// System.out.println(this.rawMap);
			}
			else if (msgType == 3) //playericon
			{
				int icon = this.inStream.read();
				this.playerIcon = Character.forDigit(icon, 10);
			}
			else if (msgType == 4) //turnrequest
			{
				this.timeLimit = this.inStream.readInt();
				this.maxDepth = this.inStream.read();
			}
			else if (msgType == 6) //enemyTurn
			{
				this.enemyTurn[2] = this.inStream.readUnsignedShort() + 1; //enemy x 
				this.enemyTurn[1] = this.inStream.readUnsignedShort() + 1; //enemy y
				this.enemyTurn[3] = this.inStream.read(); //enemy special
				this.enemyTurn[0] = this.inStream.read(); //enemy icon
			}
			else if (msgType == 7) //disqualified
			{
				int player = this.inStream.read();
				this.disqPlayer = Character.forDigit(player, 10);
			}
			else if (msgType == 8) //end of first phase
			{
				this.phase=2;
			}
			else if (msgType == 9) //end of game
			{
				//System.out.println("Game is over");
			}
			
		} 
		catch (EOFException e) 
		{
			
		}
		catch (SocketException e)
		{
			System.err.println("Socket Exception");
			//e.printStackTrace();
			this.cleanup();
		}
		catch (IOException e) {
			System.err.println("IO Exception");
			//e.printStackTrace();
			this.cleanup();
		}
		return msgType;
	}
	
	private byte[] encodeMessage(int msgType, byte[] messageData) //encodes the message and sends it
	{
		//setting other values
		int messageLength=messageData.length;
		byte[] message = new byte[5+messageLength];
		byte[] lenght = ByteBuffer.allocate(4).putInt(messageLength).array(); //converts messageLenght to byte array
		message = new byte[5+messageLength];
		message[0] = (byte)msgType;
		message[1] = lenght[0];
		message[2] = lenght[1];
		message[3] = lenght[2];
		message[4] = lenght[3];
		for (int i = 0; i < (message.length-5); i++) //copying messageData to data for sending
		{
			message[i+5]=messageData[i];
		}
		return message;
	}
	
	private void sendMessage(byte[] data)
	{
		try {
			this.outStream.write(data);
			this.outStream.flush();
		} catch (IOException e) {
			System.err.println("Communication to Server failed");
			//e.printStackTrace();
			this.cleanup();
		}
	}
	
	private void sendTurn(int y, int x, int special)
	{
		byte[] data = new byte[5];
		x--;
		byte[] buff = ByteBuffer.allocate(2).putShort((short) x).array(); //put x in data (because of transitioned coordinates)
		data[0] = buff[0];
		data[1] = buff[1];
		y--;
		buff = ByteBuffer.allocate(2).putShort((short) y).array(); //put y - 1 in data (because of transitioned coordinates)
		data[2] = buff[0];
		data[3] = buff[1];
		data[4] = (byte) special; //put special in data
		byte[] message = encodeMessage(5, data);
		sendMessage(message);
	}
}

