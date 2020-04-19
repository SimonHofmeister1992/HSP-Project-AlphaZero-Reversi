package src;

import java.io.IOException;
import java.util.HashMap;

import GUI.Frame_Board;
import GUI.IMap;

public class Map implements IMap{

	// ************************************************************************************
	// * Attributes *
	// ************************************************************************************

	private int mapHeight;
	private int mapWidth;
	private int numOfPlayers;
	
	private int[] bombsOfPlayers;
	
	private int strengthOfBombs;
	private int[] overwriteStones;

	private char[][] map;
	private char playerIcon = '1';
	private boolean[] playerExists;
	private boolean playerChangesExist;
	
	private int maxTurns = 0;
	
	private HashMap<String, String> transitions;
	private Frame_Board observer;

	// ************************************************************************************
	// * Constructor *
	// ************************************************************************************

	
	public Map(String mapSource, boolean online, Game game) {

		try {
			//this.guiStart = true
			
			FileManager fm = new FileManager(mapSource, this, online);
			
			fm.getWholeMap();

			this.playerExists = new boolean[this.getNumOfPlayers()];
			this.overwriteStones = new int[this.getNumOfPlayers()];
			this.bombsOfPlayers = new int[this.getNumOfPlayers()];
			
			setAllOverwriteStones(fm.getMapInformation()[1]);
			setAllPlayerExists();
			setAllBombsOfPlayers(fm.getMapInformation()[2]);
			this.detectMaxTurns();
			//printMap();
			
			this.setObserver(new Frame_Board(this, game));
			
		} catch (IOException e) {
			System.out.println("Ooops, something went wrong");
		}
	}
	
	public Map(String mapSource, boolean online) {

		try {
			//this.guiStart = true
			
			FileManager fm = new FileManager(mapSource, this, online);
			
			fm.getWholeMap();

			this.playerExists = new boolean[this.getNumOfPlayers()];
			this.overwriteStones = new int[this.getNumOfPlayers()];
			this.bombsOfPlayers = new int[this.getNumOfPlayers()];
			this.detectMaxTurns();
			
			setAllOverwriteStones(fm.getMapInformation()[1]);
			setAllPlayerExists();
			setAllBombsOfPlayers(fm.getMapInformation()[2]);
				
			//printMap();
			
		} catch (IOException e) {
			System.out.println("Ooops, something went wrong");
		}
	}

	// ************************************************************************************
	// * Getter & Setter *
	// ************************************************************************************

	public boolean existPlayerChanges(){
		return this.playerChangesExist;
	}
	
	public void setMaxTurns(int num){
		this.maxTurns = num;
	}
	
	public int getMaxTurns(){
		return this.maxTurns;
	}
	
	public void setMapHeight(int height) {
		this.mapHeight = height;
	}

	public void setMapWidth(int width) {
		this.mapWidth = width;
	}

	public void setNumOfPlayers(int num) {
		this.numOfPlayers = num;
	}
	
	public void setNumOfBombs(char player, int num){
		this.bombsOfPlayers[player-'0' - 1] = num;
	}

	public void setStrengthOfBombs(int strength) {
		this.strengthOfBombs = strength;
	}

	public void setOverrideStones(char player, int num){
		overwriteStones[player-'0'-1] = num;
	}
	
	public void setAllOverwriteStones(int num){
		for(int p = 0; p < this.getNumOfPlayers(); p++) overwriteStones[p] = num;
	}
	
	public void setAllBombsOfPlayers(int num){
		for(int p = 0; p < this.getNumOfPlayers(); p++) bombsOfPlayers[p] = num;
	}
	
	public void setAllPlayerExists(){
		for(int p = 0; p < this.getNumOfPlayers(); p++) playerExists[p] = true;
	}
	
	public void setMap(char[][] map) {
		this.map = map;
	}

	public void setTransitions(HashMap<String, String> transitions) {
		this.transitions = transitions;
	}

	public void setPlayerIcon(char icon) {
		this.playerIcon = icon;
	}

	public int getMapHeight() {
		return this.mapHeight;
	}

	public int getMapWidth() {
		return this.mapWidth;
	}

	public int getNumOfPlayers() {
		return this.numOfPlayers;
	}

	public int getStrengthOfBombs() {
		return this.strengthOfBombs;
	}


	public int getNumOfOverrideStones(char player){
		return overwriteStones[player-'0'-1];
	}
	
	
	public int getNumOfBombs(char player){
		return this.bombsOfPlayers[player- '0' -1];
	}
	
	public char[][] getMap() {
		return this.map;
	}

	public HashMap<String, String> getTransitions() {
		return this.transitions;
	}

	public char getPlayerIcon() {
		return this.playerIcon;
	}

	public char getItem(int row, int col) {
		return this.map[row][col];
	}
	
	public void delPlayer(char player){
		this.playerExists[player - 1 - '0'] = false;  
	}
	
	public boolean playerExists(char player){
		return playerExists[player - 1 - '0'];
	}
	
	// ************************************************************************************
	// * Interface GUI *
	// ************************************************************************************
	
	
	@Override
	public void updateBoard() {
		this.observer.update();
	}

	@Override
	public void setObserver(Frame_Board board) {
		this.observer = board;
		
	}

	@Override
	public void removeObserver() {
		this.observer = null;
		
	}
	
	@Override
	public int getRowOffset() {
		return 1;
		
	}	
	
	@Override
	public int getColOffset() {
		return 1;
		
	}	
	
	// ************************************************************************************
	// * Public Functions *
	// ************************************************************************************


	
	public void printMap() {
		System.out.println("Map: ");
		for (int row = 0; row <= mapHeight + 1; row++) {
			for (int col = 0; col <= mapWidth + 1; col++) {
				System.out.print(map[row][col]);
				System.out.print(" ");
			}
			System.out.println();
		}
	}
		
	
	public int[] searchPathes(char player, int startY, int startX)
	{
		int[] info = new int[14]; //posy posx hvalue dirs[8] = takenStones, overrides used?, special 
		info[0] =startY;
		info[1] =startX;
		int dir = 0;
		int[] start = {startY, startX};
		int[] current = start;
		char nextStone;
		int[] next;
		char firstStone = this.map[startY][startX]; //remembers first stone
		if((firstStone >= '1' && firstStone <= '8')||firstStone == 'x') //checks if path needs overwritestone
		{
			info[11] = -1; //writes to info, overwrites used
		}
		this.map[startY][startX] = player; //sets first stone to stop algotythm
		//System.out.println("startY" + startY + "startX" +startX);
		
		int[] pathInfo = new int[8]; //saves info over path in given dir, is 0 if path invalid
		int newDir;
		while(dir < 8) //runs trough all dirs and saves the dirs with number of stones colorable
		{
			current = start;
			pathInfo[dir] = -1; //path standard is -1 because it always counts +1
			newDir = dir;
			do
			{
				//System.out.println(current[0] + " , " + current[1]);
				next = nextPlace(current[0], current[1], newDir); //gets coords of next stone
				nextStone = (char) next[2]; //sets next stone //is already a char in "int form" a bit hacky...
				if (nextStone == '-') //checks for wall
				{
					//System.out.println("wall found");
					int[] transition = this.getTransition(current[1], current[0], newDir); //gets transition for current stone
					if (!(transition[0] == 0 || transition[1] == 0)) //checks if transition exists
					{
						//System.out.println("transition");
						next[0] = transition[0]; //x is first
						next[1] = transition[1]; //y comes second
						newDir = (transition[2]+4)%8; //dir is last
						nextStone = this.getItem(next[0], next[1]);
						current=next;
					}
					else
					{
						pathInfo[dir] = 0; //path not valid
						break;
					}
				}
				if (nextStone == '0' || nextStone == 'i' || nextStone == 'c' || nextStone == 'b') //path invalid because it doesnt connect to own stones
				{
					pathInfo[dir] = 0;
					break;
				}
				//System.out.println("Stone found is " + nextStone + " at y" + current[0] + " x:" + current[1]);
				current = next;
				//System.out.println(pathInfo[dir]);
				pathInfo[dir]++; //walked one stone, can up stones by one
			} while(nextStone != player); //checking next stone for what it is
			if ((next[0] == start[0]) && (next[1] == start[1])) //ends in start!!!
			{
				//System.out.println("path invalid cause start is end" + next[0] + ", " + next[1] + "    " + start[0] + ", " + start[1]);
				pathInfo[dir] = 0;
			}
			//System.out.println("could color " + pathInfo[dir] + " stones in dir " + dir);
			dir++;
		}
		
		this.map[startY][startX] = firstStone; //resets first stone
		for(int i=0; i<8; i++)
		{
			info[3+i]=pathInfo[i]; //writes path info to info
		}
		return info;
	}
	
	public int[] setBomb(char player, int y, int x, int distance, int[] numOfLostStones){
		if(this.getItem(y, x) == '-') return numOfLostStones;
		int startY = y, startX = x;
		int[] postTrans;
		
			int[] nextCoord;
			if(this.getItem(y, x) == this.getPlayerIcon()) numOfLostStones[this.getPlayerIcon()-'0']++;
			else if(this.getItem(y, x) >= '1' && this.getItem(y, x) <= this.getNumOfPlayers()+'0'){
				numOfLostStones[0]++;
				numOfLostStones[this.getItem(y, x) - '0']++;
			}
			if(this.getItem(y, x) != '-') this.map[y][x] = 't';
			if(x >= 1 && y >= 1 && x <= this.getMapWidth() + 1 && y <= this.getMapHeight() + 1 && distance > 0){
					for(int dir = 0; dir < 8; dir++){
						x = startX;
						y = startY;
						postTrans = getTransition(x, y, dir); // check Transition
						if (postTrans[0] != 0 && postTrans[1] != 0) //transition
						{
							y = postTrans[0];
							x = postTrans[1];
							
							numOfLostStones = setBomb('0', y, x, distance-1, numOfLostStones);
						}
						else{	//no transition
							if(x >= 1 && y >= 1 && x < this.getMapWidth() + 1 && y < this.getMapHeight() + 1){
								nextCoord = nextPlace(y, x, dir);
								y = nextCoord[0];
								x = nextCoord[1];
								numOfLostStones = setBomb('0', y, x, distance-1, numOfLostStones);
							}
						}
					}
			}
			else if(distance == 0){
				if(this.getItem(y, x) == this.getPlayerIcon()){
					numOfLostStones[this.getPlayerIcon() - '0']++;
				}
				else if(this.getItem(y, x) >= '1' && this.getItem(y, x) <= this.getNumOfPlayers()+'0') {
					numOfLostStones[this.getItem(y, x) - '0']++;
				}
				if(this.getItem(y, x) != '-') this.map[y][x] = 't';
			}
			
			return numOfLostStones;
	}
	
	public int activePlayers(){
		int number=0;
		number=getNumOfPlayers();
		for(int i=1;i<this.getNumOfPlayers();i++){
			if (playerExists((char)('0'+i))==false){
				number--;
			}
		}
		return number;
	}
	
	
	public int[] setBomb(char player, int y, int x, int distance){
			int[] defaultValue = new int[this.getNumOfPlayers()+1];
			defaultValue = setBomb(player, y, x, distance, defaultValue);
			for(int row = 0; row <= this.getMapHeight(); row++){
				for(int col = 0; col <= this.getMapWidth(); col++){
					if(this.getItem(row, col) == 't') this.map[row][col] = '-';
				}
			}
			return defaultValue; 
	}
	
	public int getPlayerStoneNumber(char player)
	{
		int stones = 0;
		for (int row = 1; row <= this.getMapHeight(); row++) {
			for (int col = 1; col <= this.getMapWidth(); col++) {
				if (this.getItem(row, col) == player) {
					stones++;
				}
			}
		}
		return stones;
	}
	
	public void setStone(char player, int startY, int startX)
	{
		this.setStone(player, startY, startX, null);
	}
	
	public void setStone(char player, int startY, int startX, int[] pathInfo)
	{
		if((this.getItem(startY, startX) >= '1' && this.getItem(startY, startX) <= this.getNumOfPlayers() + '0') || this.getItem(startY, startX) == 'x')
		{
			this.setOverrideStones(player, this.getNumOfOverrideStones(player)-1);
		}
		int dir = 0;
		int[] start = {startY, startX};
		int[] current = start;
		char nextStone;
		int[] next;
		this.map[startY][startX] = player; //sets first stone
		int[] transition;
		if (pathInfo == null)
		{
			pathInfo = new int[8];
			//saves info over path in given dir, is 0 if path invalid
			while(dir < 8) //runs trough all dirs and saves the dirs with number of stones colorable
			{
				current = start;
				pathInfo[dir] = -1; //path standard is -1 because it always counts +1
				int newDir = dir;
				do
				{
					//System.out.println(current[0] + " , " + current[1]);
					next = nextPlace(current[0], current[1], newDir); //gets coords of next stone
					nextStone = (char) next[2]; //sets next stone //is already a char in "int form" a bit hacky...
					if (nextStone == '-') //checks for wall
					{
						//System.out.println("wall found");
						transition = this.getTransition(current[1], current[0], newDir); //gets transition for current stone
						if (!(transition[0] == 0 || transition[1] == 0)) //checks if transition exists
						{
							//System.out.println("transition");
							next[0] = transition[0]; //x is first
							next[1] = transition[1]; //y comes second
							newDir = (transition[2]+4)%8; //dir is last
							nextStone = this.getItem(next[0], next[1]);
							current=next;
						}
						else
						{
							pathInfo[dir] = 0; //path not valid
							break;
						}
					}
					if (nextStone == '0' || nextStone == 'i' || nextStone == 'c' || nextStone == 'b') //path invalid because it doesnt connect to own stones
					{
						pathInfo[dir] = 0;
						break;
					}
					//System.out.println("Stone found is " + nextStone + " at y" + current[0] + " x:" + current[1]);
					current = next;
					//System.out.println(pathInfo[dir]);
					pathInfo[dir]++; //walked one stone, can up stones by one
				} while(nextStone != player); //checking next stone for what it is
				if ((next[0] == start[0]) && (next[1] == start[1])) //ends in start!!!
				{
					//System.out.println("path invalid cause start is end" + next[0] + ", " + next[1] + "    " + start[0] + ", " + start[1]);
					pathInfo[dir] = 0;
				}
				//System.out.println("could color " + pathInfo[dir] + " stones in dir " + dir);
				dir++;
			}
		}
		dir = 0;
		
		while (dir < 8) //painting dirs
		{
			current = start;
			int newDir = dir;
			while (pathInfo[dir] > 0) //walks path til end
			{
				next = nextPlace(current[0], current[1], newDir); //gets coords of next stone
				nextStone = (char) next[2]; //sets next stone is already a char in "int form" a bit hacky...
				if (nextStone == '-') //checks for wall
				{
					transition = this.getTransition(current[1], current[0], newDir); //gets transition for current stone
					if (!(transition[0] == 0 || transition[1] == 0)) //checks if transition exists
					{
						next[0] = transition[0]; //x is first
						next[1] = transition[1]; //y comes second
						newDir = (transition[2]+4)%8; //dir is last
					}
				}

				current = next;
				this.map[current[0]][current[1]] = player; //colores current stone
				pathInfo[dir]--; //walked one stone
			}
			dir++;
		}
	}
	
	public boolean existTransition(int x, int y, int d) {
		String key = x + " " + y + " " + d;
		if (this.getTransitions().containsKey(key)) {
			return true;
		} else {
			return false;
		}
	}
	
	public char[][] doubleMap(char[][] map) {
		this.playerChangesExist = false;
		char[][] doubledMap;
		int rows = map.length, cols = map[0].length;
		doubledMap = new char[rows][cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				doubledMap[row][col] = map[row][col];
				if(this.getItem(row, col) == 'c' || this.getItem(row, col) == 'i') this.playerChangesExist = true;;
			}
		}

		return doubledMap;
	}

	public void inversionStone() //gives every player the stones of the next existing player
	{
		int maxplayer = this.getNumOfPlayers();
		int rows = this.map.length;
		int cols = this.map[0].length;
		for (int row = 0; row < rows; row++) 
		{
			for (int col = 0; col < cols; col++) 
			{
				if (this.map[row][col] >= '1' && this.map[row][col] <= (maxplayer + '0'))// && this.playerExists(this.map[row][col])) //if field is a playerStone and exists
				{
					char player = this.map[row][col];

						player++; //increase player by one
						if(player > maxplayer + '0') //if player number is now bigger than maxplayer, new number must be 1
						{
							player = '1';
						}

					this.map[row][col] = player;
				}
			}
		}
	}
	
	public void choiceStone(char player1, char player2) //switches stones of player 1 and 2
	{
		int rows = this.map.length;
		int cols = this.map[0].length;
		for (int row = 0; row < rows; row++) 
		{
			for (int col = 0; col < cols; col++) 
			{
				if (this.map[row][col] == player1) this.map[row][col] = player2; //replaces player1 with player2
				else if (this.map[row][col] == player2) this.map[row][col] = player1; //replaces player2 with player1
			}
		}
	}
	
	// ************************************************************************************
	// * Private Functions *
	// ************************************************************************************

	private void detectMaxTurns(){
		
		int numOfPossibleTurns = 0;
		for(int row = 0; row < this.getMapHeight(); row++){
			for(int col = 0; col < this.getMapWidth(); col++){
				if(this.getItem(row, col) != '-') numOfPossibleTurns++;
			}
		}
		this.setMaxTurns(numOfPossibleTurns);
	}
	
	private int[] nextPlace(int row, int col, int direction) {

		if (direction == 0) {
			row--;
		} else if (direction == 1) {
			row--;
			col++;
		} else if (direction == 2) {
			col++;
		} else if (direction == 3) {
			row++;
			col++;
		} else if (direction == 4) {
			row++;
		} else if (direction == 5) {
			row++;
			col--;
		} else if (direction == 6) {
			col--;
		} else if (direction == 7) {
			row--;
			col--;
		}

		char nextPlace = this.getItem(row, col);

		int[] changes = new int[3];
		changes[0] = row;
		changes[1] = col;
		changes[2] = nextPlace;

		return changes;
	}

	public boolean isPathValid(char player, int row, int col){ //returns if a path is valid or not (adjusted function which was originally in coloring function)
		boolean isValid = false;
			int dir = 0;
			int[] start = {row, col};
			int[] current = start;
			char nextStone;
			int[] next;
			this.map[row][col] = player; //sets first stone
			int[] transition;

				while(dir < 8) //runs trough all dirs and saves the dirs with number of stones colorable
				{
					current = start;
					int newDir = dir;
					do
					{
						//System.out.println(current[0] + " , " + current[1]);
						next = nextPlace(current[0], current[1], newDir); //gets coords of next stone
						nextStone = (char) next[2]; //sets next stone //is already a char in "int form" a bit hacky...
						if (nextStone == '-') //checks for wall
						{
							//System.out.println("wall found");
							transition = this.getTransition(current[1], current[0], newDir); //gets transition for current stone
							if (!(transition[0] == 0 || transition[1] == 0)) //checks if transition exists
							{
								//System.out.println("transition");
								next[0] = transition[0]; //x is first
								next[1] = transition[1]; //y comes second
								newDir = (transition[2]+4)%8; //dir is last
								nextStone = this.getItem(next[0], next[1]);
								current=next;
							}
							else
							{
								break; //path is not valid
							}
						}
						if (nextStone == '0' || nextStone == 'i' || nextStone == 'c' || nextStone == 'b') //path invalid because it doesnt connect to own stones
						{
							break; //not valid because there's no connection to other own stones
						}
						//System.out.println("Stone found is " + nextStone + " at y" + current[0] + " x:" + current[1]);
						current = next;
						//System.out.println(pathInfo[dir]);
					} while(nextStone != player); //checking next stone for what it is
					if ((next[0] == start[0]) && (next[1] == start[1])) //ends in start!!!
					{
						//System.out.println("path invalid cause start is end" + next[0] + ", " + next[1] + "    " + start[0] + ", " + start[1]);
						break; //not valid because startPos is endPos
					}
					else if(this.getItem(next[0], next[1]) == player) return true; //another own stone reached => valid path
					
					//System.out.println("could color " + pathInfo[dir] + " stones in dir " + dir);
					dir++;
				}
		return isValid;
	}
	
	public int[] getTransition(int x, int y, int d) // returns transition Data
														// or 0 0 0 if invalid
														// Transition
	{
		String key = x + " " + y + " " + d;
		int[] newData = { 0, 0, 0 };

		if (transitions.containsKey(key)) // checks if transition exists
		{
			String value = transitions.get(key);
			String[] values = value.split(" ");
			newData[0] = Integer.parseInt(values[0]); // column
			newData[1] = Integer.parseInt(values[1]); // row
			newData[2] = Integer.parseInt(values[2]); // direction
			
			int temp = newData[1]; // change order to convention row column
			newData[1] = newData[0];
			newData[0] = temp;
		}
		return newData;
	}
}
