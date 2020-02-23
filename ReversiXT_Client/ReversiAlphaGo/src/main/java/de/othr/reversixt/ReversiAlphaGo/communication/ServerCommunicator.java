package de.othr.reversixt.ReversiAlphaGo.communication;

import de.othr.reversixt.ReversiAlphaGo.environment.Turn;
import de.othr.reversixt.ReversiAlphaGo.general.IMsgType;
import de.othr.reversixt.ReversiAlphaGo.general.Main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class ServerCommunicator {


    // ************************************************************************************
    // * Attributes *
    // ***********************************************************************************

    private Socket socket;

    //Streaminfo
    private DataInputStream inStream;
    private DataOutputStream outStream;

    //Data
    private int groupNumber;
    private String rawMap;
    private char playerIcon;
    private int phase;
    private int maxDepth;
    private int timeLimit;
    private Turn enemyTurn;
    private char disqPlayer;


    // ************************************************************************************
    // * Constructor *
    // ************************************************************************************

    public ServerCommunicator(int grNumber) {
        this.groupNumber = grNumber;
        this.phase = 1;
        if (!Main.QUIET_MODE) System.out.println("The used group number for this game is: " + groupNumber);
    }


    // ************************************************************************************
    // * Getter & Setter *
    // ************************************************************************************

    public String getRawMap() {
        return this.rawMap;
    }

    public int getPhase() {
        return this.phase;
    }

    // own player icon on map
    public char getPlayerIcon() {
        return this.playerIcon;
    }

    // max number of turns which may be pre-calculated by rules
    public int getMaxDepth() //
    {
        return this.maxDepth;
    }

    public Turn getEnemyTurn() {
        return this.enemyTurn;
    }

    // get info about disqualified players, but not oneself
    public char getDisqualifiedPlayer() {
        return this.disqPlayer;
    }

    // remaining time for turn in ms
    public int getTimeLimit() {
        return timeLimit;
    }

    // ************************************************************************************
    // * Public Functions *
    // ************************************************************************************

    public void connect(String address, int port) throws IOException {
        //network
        //establishing connection
        //System.out.println("Connecting to Server on IP: " + this.IP + " on Port: " + this.portNumber);
        for (int tries = 0; ; tries++) {
            try {
                this.socket = new Socket(address, port);
                if (!Main.QUIET_MODE) System.out.println("Server found");
                outStream = new DataOutputStream(socket.getOutputStream());
                outStream.flush();
                inStream = new DataInputStream(socket.getInputStream());

                break;
            } catch (IOException e) {
                if (!Main.QUIET_MODE) System.out.println("Server not found. Retrying " + (3 - tries) + " time(s)");
                if (tries >= 3) {
                    throw e;
                }
            }
        }
        //sending group number
        byte[] data = {((byte) groupNumber)};
        byte[] message = encodeMessage(1, data);
        sendMessage(message);
    }

    public int waitOnServer() //returns msgType
    {
        return decodeMessage();
    }

    public void sendOwnTurn(Turn turn) //sends own turn to server
    {
        sendTurn(turn.getRow(), turn.getColumn(), turn.getSpecialFieldInfo());
    }

    public void cleanup() {
        if (!Main.QUIET_MODE) System.out.println("Closing Socket");
        if (this.socket != null) {
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

    // ************************************************************************************
    // * Private Functions *
    // ************************************************************************************

    private int decodeMessage()  //decodes the message and relays the information
    {
        int msgType = 0;
        int msgLength;

        try {
            msgType = this.inStream.read();
            msgLength = this.inStream.readInt();
            if (msgType == IMsgType.INITIAL_MAP) //map
            {
                byte[] data = new byte[msgLength];
                this.inStream.read(data, 0, msgLength);

                this.rawMap = new String(data);

                if (!Main.QUIET_MODE) System.out.println(this.rawMap);
            } else if (msgType == IMsgType.PLAYER_ICON) //playericon
            {
                int icon = this.inStream.read();
                this.playerIcon = Character.forDigit(icon, 10);
            } else if (msgType == IMsgType.TURN_REQUEST) //turnrequest
            {
                this.timeLimit = this.inStream.readInt();
                if (this.timeLimit <= 0) this.timeLimit = Integer.MAX_VALUE;
                this.maxDepth = this.inStream.read();
            } else if (msgType == IMsgType.ENEMY_TURN) //enemyTurn
            {

                int col = this.inStream.readUnsignedShort();
                int row = this.inStream.readUnsignedShort();
                int specialInfo = this.inStream.read();
                char playerIcon = (char) (this.inStream.read() + 48);

                this.enemyTurn = new Turn(playerIcon, row, col, specialInfo);

            } else if (msgType == IMsgType.DISQUALIFIED_PLAYER) //disqualified
            {
                int player = this.inStream.read();
                this.disqPlayer = Character.forDigit(player, 10);
            } else if (msgType == IMsgType.END_OF_FIRST_PHASE) //end of first phase
            {
                this.phase = 2;
            } else if (msgType == IMsgType.END_OF_GAME) {
                //end of game
            }

        } catch (EOFException e) {
            // do nothing
        } catch (SocketException e) {
            System.err.println("Socket Exception");
            //e.printStackTrace();
            this.cleanup();
        } catch (IOException e) {
            System.err.println("IO Exception");
            //e.printStackTrace();
            this.cleanup();
        }
        return msgType;
    }

    private byte[] encodeMessage(int msgType, byte[] messageData) //encodes the message and sends it
    {
        //setting other values
        int messageLength = messageData.length;
        byte[] message;
        byte[] lenght = ByteBuffer.allocate(4).putInt(messageLength).array(); //converts messageLenght to byte array
        message = new byte[5 + messageLength];
        message[0] = (byte) msgType;
        message[1] = lenght[0];
        message[2] = lenght[1];
        message[3] = lenght[2];
        message[4] = lenght[3];
        for (int i = 0; i < (message.length - 5); i++) //copying messageData to data for sending
        {
            message[i + 5] = messageData[i];
        }
        return message;
    }

    private void sendMessage(byte[] data) {
        try {
            this.outStream.write(data);
            this.outStream.flush();
        } catch (IOException e) {
            System.err.println("Communication to Server failed");
            //e.printStackTrace();
            this.cleanup();
        }
    }

    private void sendTurn(int y, int x, int special) {
        byte[] data = new byte[5];
        byte[] buff = ByteBuffer.allocate(2).putShort((short) x).array(); //put x in data (because of transitioned coordinates)
        data[0] = buff[0];
        data[1] = buff[1];
        buff = ByteBuffer.allocate(2).putShort((short) y).array(); //put y - 1 in data (because of transitioned coordinates)
        data[2] = buff[0];
        data[3] = buff[1];
        data[4] = (byte) special; //put special in data
        byte[] message = encodeMessage(5, data);
        sendMessage(message);
    }

}

