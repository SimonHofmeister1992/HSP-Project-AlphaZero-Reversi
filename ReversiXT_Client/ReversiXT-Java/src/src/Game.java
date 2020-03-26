package src;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import GUI.IServerConn;

public class Game implements IServerConn{

	// ************************************************************************************
	// * Attributes *
	// ************************************************************************************
	public final static int NUM_OF_LOG_TYPES = 3;
	public final int NORMAL_TURN_REST_TIME = 40;
	public final int LOGGING_TURN_REST_TIME = 150;
	
	private ServerComm serverComm = new ServerComm(12);

	private Map map;
	private Heuristic heuristic;
	private int phase;
	private boolean online = true; //cmd-parameter?
	private int turnCount = 0;
	private boolean guiStart = false; //cmd-Parameter if guiStart wanted
	private boolean automatic = true; //cmd-Parameter if guiStart true and playmode manual preferred
	private boolean[] logging; //0: log endOfGame, 1: log everyTakenTurn, 2: logEveryPossibilityInParanoid (multiple Data, reason: iterative Deepening)
	private String ip = "127.0.0.1";
	private int port = 7777;
	private String mapSource;
	private boolean quietMode = false;
	private boolean useConfig = false;
	
	public boolean wait = false;

	// ************************************************************************************
	// * Constructor *
	// ************************************************************************************

	public Game(String args[]) 
	{	
		if(args.length == 0) //if no startup params
		{
			if(!quietMode)System.out.println("NO Startup Parameters detected, running default");
		}
		this.startupParameters(args);
		
		if(!quietMode)this.printParameters();
		//this.guiStart = true;
		//this.automatic = false;
		//online = false;
		if(online) //if online mode enabled, connects to server, handshakes and reads map
		{
			if(guiStart == false) automatic = true;
			serverInit(ip, port);
			
			this.heuristic = new Heuristic(map, logging);
			this.heuristic.getTimer().setMIN_REST_TIME(this.NORMAL_TURN_REST_TIME);
			if(logging != null){
				for(int i = 0; i < this.logging.length; i++){
					if(logging[i]){
						this.heuristic.getTimer().setMIN_REST_TIME(this.LOGGING_TURN_REST_TIME);
						break;
					}
			}
			}
			this.setPhase(1);
			play();
		}
		else //if online mode disabled reads map from input
		{	
			guiStart = false; // not possible yet
	     	automatic = true; //needed if guiStart = false
			String mapSource = "maps/castleWithBugs.txt";
			this.map = new Map(mapSource, false);
			this.heuristic = new Heuristic(map, logging);
			this.setPhase(1);
			this.map.printMap();
			startTurn(1);
			//this.map.printMap();
		}
	}
	// ************************************************************************************
	// * Getter & Setter *
	// ************************************************************************************
	
		public int getPhase(){
			return this.phase;
		}
		
		public void setPhase(int phase){
			this.phase = phase;
		}
		
		public int getTurnCount(){
			return this.turnCount;
		}

	// ************************************************************************************
	// * Public Functions *
	// ************************************************************************************
		@Override
		public void sendTurn(int row, int col, int choice){
			this.serverComm.sendOwnTurn(row, col, choice);
		}
		@Override
		public void toggleAutomatics(boolean bool){
			this.automatic = bool;
		}

// ************************************************************************************
// * Private Functions *
// ************************************************************************************
	
	private void serverInit(String IP, int port)
	{
		try {serverComm.connect(IP, port);}
		catch(IOException ServerError)
		{
			//if(!quietMode)System.out.println("Server error. Aborting");
			if(!quietMode)System.err.println("Server error. Aborting");
			//ServerError.printStackTrace();
			this.serverComm.cleanup();
		}
		int msgType = this.serverComm.waitOnServer();
		if (msgType == 2) //got map from server
		{
			if(!quietMode)System.out.println("Got Map from Server");
			if(!quietMode)System.out.println("everything seems good, starting game...");
			if(!guiStart) this.map = new Map(this.serverComm.getRawMap(), true);
			else this.map = new Map(this.serverComm.getRawMap(), true, this);
		}
		else //something went wrong!!
		{
			System.err.println("First message was not MAP");
			System.err.println("Aborting!!");
			this.serverComm.cleanup();
		}
	}
	
	private void play()
	{
		long maxTurnTime = 0;
		
		while(true)
		{
			int msgType = this.serverComm.waitOnServer();
			if (msgType == 3) //gets player icon from server
			{
				this.map.setPlayerIcon(this.serverComm.getPlayerIcon());
			}
			else if (msgType == 4) //its our turn
			{
				
				this.heuristic.getTimer().setStartTime();
				this.heuristic.getTimer().setTurnTime((long)this.serverComm.getTimeLimit());
	//			this.heuristic.getTimer().setTurnTime(1000);
				if(automatic == true) startTurn(this.serverComm.getMaxDepth());
				
				long tempTurnTime = heuristic.getTimer().getCurrentTurnTime();
				if(tempTurnTime > maxTurnTime) maxTurnTime = tempTurnTime;
			}
			else if (msgType == 6) // turn received
			{
				int[] turn = this.serverComm.getEnemyTurn();
				char turnPlayerIcon = Character.forDigit(turn[0], 10);
				doTurn(turn[1], turn[2], turn[3], turnPlayerIcon);
				if(this.getPhase() == 2) map.setNumOfBombs(turnPlayerIcon, this.map.getNumOfBombs(turnPlayerIcon) - 1);
				if(guiStart) map.updateBoard();
			}
			else if (msgType == 7) //disqualification
			{
				char player = this.serverComm.getDisqualifiedPlayer();
				this.map.delPlayer(player); //removes said player from our game
				if (player == this.map.getPlayerIcon()) System.exit(1); //ends program if game is over for us
				//if all enemys are disqualified.. server may bug :O
			}
			else if (msgType == 8) //end of phase 1
			{
				this.setPhase(this.serverComm.getPhase());
			}
			else if (msgType == 9) //end of game
			{
				if(logging!= null && logging[0]) {
					this.heuristic.addToDataset(0, this.map.getNumOfPlayers(), this.getTurnCount(), this.heuristic.getTimer().getWholeGameTime());
				}
				char winner = '0';
				int stones = 0;
				for (int player = 1; player <= this.map.getNumOfPlayers(); player++) //go over every player
				{
					int oldStones = stones;
					stones = this.map.getPlayerStoneNumber((char) (player + '0'));
					if(logging!= null && logging[0]) this.heuristic.addToDataset(0, stones);
					if (oldStones<stones) winner = ((char) (player + '0'));
					if(!quietMode)System.out.println("Player " + player + " has " + stones + " Points");
				}
				if(logging!= null && logging[0]) this.heuristic.singleDatasetWriter(0);
				if (winner == this.map.getPlayerIcon() && !quietMode)
				{
					System.out.println("max. Zugdauer: " + maxTurnTime +" ms");
					System.out.println("We are the Champions my Friend!! (maybe)");
				}
				else
				{	if(!quietMode){
					System.out.println("max. Zugdauer: " + maxTurnTime + " ms");
					System.out.println("Player " + winner + " wins.");
					System.out.println("Sadly we didn't win...");
				}
				}
				break;
			}
		}

	}
	
	private void doTurn (int y, int x, int special, char player)
	{	
		this.wait = true;
		if(this.getPhase() == 1){
			char stone = this.map.getItem(y, x) ;
			this.map.setStone(player, y, x);
			if ((special < 20) &&(0 < special))
			{
				char switchPlayer = (char) (special + '0');
				if(!quietMode)System.out.print("Choice stone..." );
				if(!quietMode)System.out.println("Player " + player + " switches with Player " + switchPlayer);
				this.map.choiceStone(player, switchPlayer); //executes choice stone
			}
			else if (special == 20)
			{
				this.map.setNumOfBombs(player, this.map.getNumOfBombs(player)+1);
				if(!quietMode)System.out.println("Player " + player + "chose an overwrite Stone");
			}
			else if (special == 21)
			{
				this.map.setOverrideStones(player, this.map.getNumOfOverrideStones(player)+1);//player gets extra overwriteStone
				if(!quietMode)System.out.println("Player " + player + "chose a Bomb");
			}
			else if (stone == 'i')
			{
				this.map.inversionStone(); //executes inversion
				if(!quietMode)System.out.println("Invserion Stone triggered!!");
			}
		}
		else if(this.getPhase() == 2){
			map.setBomb(player,y, x, this.map.getStrengthOfBombs());
		}
		if(!quietMode){
		map.printMap();
		System.out.println("Player " + player + " turn to y: " + y + ", x: " + x);
		System.out.println("He now has " + this.map.getNumOfBombs(player) + " bombs, and " + this.map.getNumOfOverrideStones(player) + " overwrite Stones!");
		System.out.println("Turn: " + this.turnCount);
		System.out.println();
		System.out.println();
		}
		this.turnCount++;
		this.wait = false;
	}


	private void startTurn(int searchDeepness)
	{
		if(this.serverComm.getTimeLimit() == 0){
			if(this.getPhase() == 1) this.heuristic.getTimer().setTurnTime(300*1000);
			else this.heuristic.getTimer().setTurnTime(2);
			if(searchDeepness == 0){
				searchDeepness = 1;
			}
		}
		else{
			searchDeepness = 42;
		}
		while(this.wait == true){}
		Timer turnTime = new Timer();
		char[][] mapSave = map.doubleMap(map.getMap());
		heuristic.setMaxSearchDeepness(searchDeepness);
		int[] bestTurn = new int[13];
		
		if(this.getPhase() == 1)
		{
			bestTurn = heuristic.getBestPossibleTurn(this.getTurnCount());
		}
		else
		{
			int[]bestBomb = heuristic.getBestBomb();
			bestTurn[0] = bestBomb[0];
			bestTurn[1] = bestBomb[1];
			bestTurn[12] = 0;
		}
		
		map.setMap(mapSave);
		if (online)
		{
			this.serverComm.sendOwnTurn(bestTurn[0], bestTurn[1], bestTurn[12]); //normal
			//this.serverComm.sendOwnTurn(3, 7); //boesesLevel7
			//this.serverComm.sendOwnTurn(3, 4); //Reflect
			//this.serverComm.sendOwnTurn(bestTurn[0], bestTurn[1], 1); //boesesLevel8 - Type 1
			//this.serverComm.sendOwnTurn(bestTurn[0], bestTurn[1], 2); //boesesLevel8 - Type 2
		}		
		if(!quietMode){
			System.out.println("Phase: " + this.getPhase());
			System.out.println("After Own Turn with ");
			System.out.println("ParanoidValue: " + bestTurn[2]);
			turnTime.printTimeDifference();
			heuristic.printNumOfStates();
		}
		//System.out.println("y" + bestTurn[0] + "x" + bestTurn[1]);

	}
	
	private void startupParameters(String argsString[])
	{
		//evaluating content of args
		List<String> args = Arrays.asList(argsString);
		if(args.contains("-help") || args.contains("-h"))
		{
			printHelp();
			System.exit(0);
		} //exit after printing help
		else //do actual evaluation
		{
			int i;
			
			if((i=args.indexOf("-log")) > -1) //activates logging
			{
				logging = new boolean[NUM_OF_LOG_TYPES];
				boolean anyLogging = false;
				for(int j = 0; j < Game.NUM_OF_LOG_TYPES; j++){
					if(args.indexOf("+l" + j) > -1){
						logging[j] = true;
						anyLogging = true;
					}
					else{
						logging[j] = false;
					}
				}
				if(!anyLogging){
					this.logging = null;
				}
			}
			if((i=args.indexOf("-c")) > -1)
			{
				this.useConfig = true;
			}
			if((i=args.indexOf("-gui")) > -1) //activates gui
			{
				this.guiStart = true;
			}
			if((i=args.indexOf("-manual")) > -1) //disables automatic mode
			{
				if((i=args.indexOf("-gui")) > -1)
				{
					this.automatic = false;
					this.guiStart = true;
				}
				else
				{
					if(!quietMode)System.out.println("-gui not set, ignoring -manual");
				}
			}
			if((i=args.indexOf("-offline")) > -1) //disables server connection
			{
				this.online = false;
				if("-map".equals(args.get(i+1)))
				{
					this.mapSource = args.get(i+2);
				}
				else
				{
					System.out.println("Map not provided, aborting!!");
					System.exit(0);
				}
			}
			if(((i=args.indexOf("-ip")) > -1) || ((i=args.indexOf("-i")) > -1)) //reads ip
			{
				this.ip = args.get(i+1);
			}
			if(((i=args.indexOf("-port")) > -1) || ((i=args.indexOf("-p")) > -1)) //reads port
			{
				try 
				{
					this.port = Integer.parseInt(args.get(i+1)); //checks if port is a number
				}
				catch (NumberFormatException e)
				{
					System.out.println("Specified port is not a number, aborting...");
					System.exit(0);
				}
			}
			if(args.contains("-n"))
			{
				this.heuristic.setTurnSort(false);
			}
			if(args.contains("-q"))
			{
				this.quietMode = true;
			}
			
		}
	}
	
	private void printParameters()
	{ 
		System.out.println("Online mode:" + this.online);
		System.out.println("IP: " + this.ip);
		System.out.println("Port: " + this.port);
		System.out.println("GUI: " + this.guiStart);
		System.out.println("Manual mode: " + !this.automatic);
		if(logging!=null){
			for(int i = 0; i < Game.NUM_OF_LOG_TYPES; i++){
				if(this.logging[i])System.out.println("loggingmode " + i + ": " + this.logging[i]);
			}
		}

		if(!this.online)
		{
			System.out.println("Map: " + this.mapSource);
		}
		
		System.out.println();
	}
	
	private void printHelp()
	{
		System.out.println("This program can have startup parameters");
		System.out.println("The following parameters are accepted:");
		System.out.println("");
		System.out.println("-ip <IP of Server> or -i <IP of Server>");
		System.out.println("standart is loopback ip");
		System.out.println("");
		System.out.println("-port <Port of Server> or -p <Port of Server>");
		System.out.println("standart is 7777");
		System.out.println("");
		System.out.println("-gui");
		System.out.println("activates gui");
		System.out.println("");
		System.out.println("-c");
		System.out.println("");
		System.out.println("-manual");
		System.out.println("disables AI play, only works if -gui is set");
		System.out.println("");
		System.out.println("-log");
		System.out.println("turns on normal logging");
		System.out.println("--e");
		System.out.println("turns on extensive logging");
		System.out.println("");
		System.out.println("-offline");
		System.out.println("sets program to offline mode");
		System.out.println("sneeds --map <Path to Mapfile> parameter");
		System.out.println("ignores -ip -port -gui -manual");
		System.out.println("");
		System.out.println("-help or -h");
		System.out.println("prints this help text");
		
	}
}