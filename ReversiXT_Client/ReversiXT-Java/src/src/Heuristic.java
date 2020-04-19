package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class Heuristic {
	//###STARTOFGONFIGVALUES###
	final String EXAMPLE = "EXAMPLESTRING";
	final int USE_ASP_WINDOWS = 1;
	final int ASP_MIN_SIZE = 20;
	final double ASP_FACTOR = 1.0;
	final double TURNTIME_FACTOR = 0.3;
	final int ITERATIVE_JUMPWIDTH = 2;
	final int HEURISTICPLAYERDISTANCE = 16;
	final double BREAKEVEN = 0.8;
	final double VALUE_HEURISTIK = 1.0;
	final double VALUE_STONES = 1;
	//###ENDOFCONFIGVALUES###
	final boolean ASCENDING = true;
	final boolean DESCENDING = false;
	final int NEG_INFINITY = Integer.MIN_VALUE;
	final int POS_INFINITY = Integer.MAX_VALUE;
	final int OVERRIDE_RATING_CHANGE = 5000;
	// ************************************************************************************
	// * Attributes *
	// ************************************************************************************

	private int[][] strategicMap;
	private Map map;
	private int maxSearchDeepness;
	private int heuristicValueOfActualMap;
	private int numOfStatesInTurn;
	private char[][] oldMap;
	private int[][] playerRank;
	private int[][] playerRankPhase2;
	private int[] specialStones;
	private int[][] specialStonesMap;
	private int[][] obstructionMap;
	private boolean turnSort = true;
	private Timer turnTimer;
	private boolean searchDepthComplete = false;
	private int turn = 0;
	private boolean[] logging;
	private LoggerManager lm;
	private ArrayList<String>[] dataset;
	private boolean playerChangesOnMap = true;
	private int phase;
	private int activePlayers;
	private int numOfActivePlayers;
	// ************************************************************************************
	// * Constructor *
	// ************************************************************************************

	// getbestPath via Paranoid! (perhaps also some others in combination later
	// on)
	@SuppressWarnings("unchecked")
	public Heuristic(Map map, boolean[] logging) {
		this.logging = logging;
		
		if(logging != null){	
			
			this.dataset = new ArrayList[Game.NUM_OF_LOG_TYPES];
			for(int i = 0; i < Game.NUM_OF_LOG_TYPES; i++){
				dataset[i] = new ArrayList<String>();
			}
			this.lm = new LoggerManager();
		}
		this.turnTimer = new Timer();
		this.turnTimer.setTurnFactor(this.TURNTIME_FACTOR);
		this.numOfStatesInTurn = 0;
		this.map = map;
		buildObstructionMap();
		buildStratMap();
		playerRank = new int[map.getNumOfPlayers()][1];
		specialStones = new int[2];		
		this.rateSpecialStones();
//		this.printStratMap();
	}

	// ************************************************************************************
	// * Getter & Setter *
	// ************************************************************************************
	
	public void setTurnSort(boolean value)
	{
		this.turnSort = value;
	}

	public Timer getTimer(){
		return this.turnTimer;
	}
	
	public void setMaxSearchDeepness(int value) {
		this.maxSearchDeepness = value;
	}

	public void setActualHeuristicValueOfMap(int value) {
		this.heuristicValueOfActualMap = value;
	}

	public int setActualHeuristicValueOfMap() {
		return this.heuristicValueOfActualMap;
	}

	public void incNumOfStates() {
		this.numOfStatesInTurn++;
	}
	
	public void incNumOfStatesBy(int value) {
		this.numOfStatesInTurn += value;
	}

	public int getMaxSearchDeepness() {
		return this.maxSearchDeepness;
	}

	public int getNumOfStates() {
		return this.numOfStatesInTurn;
	}
	
	public int getPhase(){
		return this.phase;
	}
	
	public void setPhase(int phase){
		this.phase = phase;
	}

	public void resetNumOfStates(){
		this.numOfStatesInTurn = 0;
	}
	// ************************************************************************************
	// * Public Functions *
	// ************************************************************************************

	public void printNumOfStates(){
		System.out.println("States:  " + getNumOfStates());
	}
	public void iniPlayerRank(){
		
	}
	public int[][] getStratMap() {
		return this.strategicMap;
	}
	public void rateSpecialStones(){
		//Rating of bombs
		specialStones[0]=(map.getStrengthOfBombs()*2+1)*(map.getStrengthOfBombs()*2+1)*2;
		//Rating of overwrite-stones
		specialStones[1]=((map.getMapHeight()*map.getMapWidth())/2);
	}
	public void rateSpecialStones(int i){
		//Rating of bombs
		specialStones[0]=(map.getStrengthOfBombs()*2+1)*(map.getStrengthOfBombs()*2+1)*2*i;
		//Rating of overwrite-stones
		specialStones[1]=((map.getMapHeight()*map.getMapWidth())/2)*i;
	}
	public void setPlayerRank(){
		int x,max=0;
		int playernumber;
		this.playerRank = new int[map.getNumOfPlayers()+1][2];
		int[] stones= new int[map.getNumOfPlayers()+1];
		
		int mapHeight = this.map.getMapHeight(), mapWidth = this.map.getMapWidth();
		char item;
		
		playernumber=this.map.getNumOfPlayers();
		
		this.numOfActivePlayers = this.map.getNumOfPlayers();
		
		
		if(playernumber>=2){
			//initialize the playerRank with zeros
			for(int y=1;y<=playernumber;y++){
				playerRank[y][1]=0;
				playerRank[y][0]=0;
				stones[y]=0;
			}
			for (int row = 1; row < (mapHeight + 1); row++) {
				for (int col = 1; col < (mapWidth + 1); col++) {
					//if the position has a value between 1 and the number of players 
					item = map.getItem(row,col);
					if((item >='1')&&(item<='0'+(char)playernumber)){
						x=Character.getNumericValue(item);
						//Add the strategicMap Values to the Rating
						playerRank[x][1]+=strategicMap[row][col];
						stones[x]++;
					}
				}
			}
			//Checks if a player is disqualified
			//If a player is disqualified he get's a rank of -1
			for(int y=1;y<=playernumber;y++){
				if(map.playerExists((char)('0'+y))==false){
					playerRank[y][0]=-1;
					playerRank[y][1]= 0;
					if(this.map.playerExists((char)(y+'0'))) this.map.delPlayer((char)(y+'0'));
					this.numOfActivePlayers--;
				}
				//If a player has no stones in the map he get's a rank of -1
				if(stones[y]==0){
					playerRank[y][0]=-1;
					playerRank[y][1]= 0;
//					if(this.map.playerExists((char)(y+'0'))) this.map.delPlayer((char)(y+'0'));
					this.numOfActivePlayers--;
				}
			}
			//Run through the array and give the remaining players a rank
			int maxold=0,numberold=0;
			for(int i=1;i<=playernumber;i++){
				max=-100000;
				int number=1;
				for(int y=1;y<=playernumber;y++){
					if(playerRank[y][0]==0){
						if(playerRank[y][1]>max){
							max=playerRank[y][1];
							number=y;
						}
					}
				}
				//Check if players have the same rating and give them the same position
				if(playerRank[number][0]==1){
					if(maxold==max){
						playerRank[number][0]=numberold;
						i--;
					}
					else{
						playerRank[number][0]=i;
						numberold=i;
					}
				}
				maxold=max;
			}							
		}	
	}
	
	public void setPlayerRankPhase2(){
		int x,max=0;
		int playernumber;
		int[] stones= new int[map.getNumOfPlayers()];
		playernumber=this.map.getNumOfPlayers();
		playerRankPhase2 = new int[this.map.getNumOfPlayers()][2];
		
		int mapHeight = this.map.getMapHeight(), mapWidth = this.map.getMapWidth();
		char item;
		

			//initialize the playerRank with zeros
			for(int y=0;y<playernumber-1;y++){
				
				playerRankPhase2[y][1]=0;
				playerRankPhase2[y][0]=0;
				stones[y]=0;
			}
			for (int row = 1; row < (mapHeight + 1); row++) {
				for (int col = 1; col < (mapWidth + 1); col++) {
					//if the position has a value between 1 and the number of players 
					item = map.getItem(row,col);
					if((item>='1')&&(item<='0'+playernumber)){
						x=Character.getNumericValue(item);
						//Add the stone to the Rating
						playerRankPhase2[x-1][1]+=1;
						stones[x-1]++;
					}
				}
			}
			//Checks if a player is disqualified
			//If a player is disqualified he get's a rank of -1
			for(int y=1;y<playernumber-1;y++){
				if(map.playerExists((char)(y+'0'))==false){
					playerRankPhase2[y-1][0]=-1;
//					playerRankPhase2[y-1][1]= 0;
				}
				//If a player has no stones in the map he get's a rank of -1
				if(stones[y-1]==0){
					playerRankPhase2[y-1][0]=-1;
					playerRankPhase2[y-1][1]= 0;
				}
			}
			//Run through the array and give the remaining players a rank
			int maxold=0,numberold=0;
			for(int i=1;i<=playernumber -1;i++){
				max=-100000;
				int number=0;
				for(int y=0;y<playernumber - 1;y++){
					if(playerRankPhase2[y][0]==0){
						if(playerRankPhase2[y][1]>max){
							max=playerRankPhase2[y][1];
							number=y;
						}
					}
				}
				//Check if players have the same rating and give them the same position
				if(playerRankPhase2[number][0]==0){
					if(maxold==max){
						playerRankPhase2[number][0]=numberold;
						i--;
					}
					else{
						playerRankPhase2[number][0]=i;
						numberold=i;
					}
				}
				maxold=max;
			}										
	}
	
	public void printStratMap() {

		System.out.println();
		System.out.println();
		System.out.println("strategical map: ");
		System.out.println();

		for (int row = 0; row <= map.getMapHeight() + 1; row++) {
			for (int col = 0; col <= map.getMapWidth() + 1; col++) {
				System.out.print(strategicMap[row][col] + " ");
			}
			System.out.println();
		}
	}
	
	public void singleDatasetWriter(int num)
	{
		for(String s : this.dataset[num]){
			lm.noticeData(num, s);
		}
		try{
			lm.writeData();
			this.dataset[num] = new ArrayList<String>();
		}
		catch(Exception e){
		}
	}
	
	public void addToDataset(int num, Object... o){
		for(Object x : o){
				this.dataset[num].add(x.toString());
		}
	}
	
	


	public int[] getBestPossibleTurn(int turn)
	{
		this.turn = turn;	
		this.resetNumOfStates();
		
		int[] bestTurn = new int[13];
		int[] lastBestTurn = new int[13];
		lastBestTurn[2] = this.NEG_INFINITY;
		bestTurn[2] = this.NEG_INFINITY;
		
		int maxDepth = this.getMaxSearchDeepness();
		int maxDepthReached = 0;
		boolean aspirationWindows = false;

		int alpha = this.NEG_INFINITY, beta = this.POS_INFINITY;
		int aspWindowSize = 0;
		
		this.activePlayers = this.map.activePlayers();
		if(this.activePlayers>2)
		{
			this.oldMap = this.map.doubleMap(this.map.getMap());
			this.setPlayerRank(); //only sets player rank if more than 2 players are active
		}
		
		if(logging != null){
			for(int index = 1; index < Game.NUM_OF_LOG_TYPES; index++){
				if(this.turn == 0 && logging[index]) {
					this.addToDataset(index, "Anzahl Spieler", "Zugnummer", "y" , "x",  "MaxSuchtiefe", "Suchtiefe",  "Beta", "Alpha", "Zugwertung vor Ueberschreib/Bombe" , "Zugwertung", "Anzahl Ueberschreibsteine Spieler", "Differenz Ueberschreibsteine" ,"Wertung Ueberschreibsteine im Value", "Verbleibende Zeit");
					this.singleDatasetWriter(index);
				}
			}
		}
		
		int saveNumOfOurOverrides = this.map.getNumOfOverrideStones(this.map.getPlayerIcon());
		for(int i = 0; i <= maxDepth; i+=this.ITERATIVE_JUMPWIDTH)
		{
			
			//RE_INITIALIZE ALPHA, BETA for ASPIRATION WINDOWS
			
			int alpha_save = alpha, beta_save = beta;
			
			if(!this.getTimer().mayContinueGame())
			{
				bestTurn = lastBestTurn;
				break;
			}
			
			maxDepthReached = i;
			this.resetNumOfStates();
			this.searchDepthComplete = false;
			
			if(aspirationWindows){
				int mapFactor = (map.getMapHeight()*map.getMapWidth())/(map.getMapHeight()+map.getMapWidth());
				
				int numMaxTurns = this.map.getMaxTurns();
				double turnFactor = Math.sin((turn * 180) / numMaxTurns);	//turn-variability: window biggest in middle of game with move that we don't get factor 0 (only one value ok)
				
				
				if(aspWindowSize < this.ASP_MIN_SIZE) aspWindowSize = this.ASP_MIN_SIZE;
				aspWindowSize = (int) Math.abs(turnFactor * mapFactor * this.ASP_FACTOR);
				
				alpha = bestTurn[2] - aspWindowSize;
				beta = bestTurn[2] + aspWindowSize;
				
				if(alpha + 2 >= beta || (alpha == alpha_save && beta == beta_save)) {
					bestTurn = lastBestTurn;
					break;
				}
			}
			else if(bestTurn[2] != this.NEG_INFINITY && bestTurn[2] != this.POS_INFINITY && this.USE_ASP_WINDOWS != 0) aspirationWindows = true;

			bestTurn = this.goKindOfParanoid(this.map.getMap(), this.map.getPlayerIcon(), 0, i, alpha, beta, 0); //map, player, current SearchDepth, maxSearchDepth, alpha, beta
			if(lastBestTurn[2] != this.NEG_INFINITY && lastBestTurn[2] != this.POS_INFINITY){
				if(bestTurn[0] != 0 && bestTurn[1] != 0 && (lastBestTurn[2] <= bestTurn[2] || this.searchDepthComplete)) lastBestTurn = bestTurn;
			}
			else {
				if(bestTurn[0] != 0 && bestTurn[1] != 0) lastBestTurn = bestTurn;
			}
			
//			System.out.println(this.turn + "_" + i + "_" + this.getNumOfStates() + "_" + alpha + "_" + beta);	
		}
		
		
//		System.out.println(turn + "_SearchDepth of Stop: " + (maxDepthReached-1));
//		System.out.println();
//		this.map.printMap();
		
		if(logging != null && logging[1]) {
			int numOfOurOverrideStones = this.map.getNumOfOverrideStones(this.map.getPlayerIcon());
			char stone = this.map.getItem(bestTurn[0], bestTurn[1]);
			if(stone == 'x'|| (stone>='1' && stone <= '8')) numOfOurOverrideStones--;
			
			this.addToDataset(1, this.map.getNumOfPlayers(), turn, bestTurn[0]-1 , bestTurn[1]-1,  this.maxSearchDeepness, maxDepthReached,  beta, alpha, "x" , bestTurn[2], this.map.getNumOfOverrideStones(this.map.getPlayerIcon()), saveNumOfOurOverrides-numOfOurOverrideStones ,this.map.existPlayerChanges()?500:1 , this.turnTimer.getFullTime()-this.turnTimer.getCurrentTurnTime());
			this.singleDatasetWriter(1);
		}
		bestTurn = lastBestTurn;
		return bestTurn;
	}
	
	
	public int[] getBestBomb()
	{	
		int[] bestBomb = {0, 0};
		
		char mostHatedEnemy = '2'; //function!
		
		char[][] mapSave;
		
		int[] numOfBombedStones; //all enemy-stones deleted, all stones lost in dependence of player number = position;
		int[] bestBombed = {this.NEG_INFINITY + this.map.getMapHeight()*this.map.getMapWidth(), 0, 0, 0, 0, 0, 0, 0, 0}; //num of bombed positions of all enemies (sum), num of bombed positions of every single player
		char higherRankedPlayer = 'n', lessRankedPlayer = 'n'; //placeholder
		int rankActualUpperHatedEnemy = 0, rankActualLowerHatedEnemy = this.POS_INFINITY; //negative ranking: disqualified, no stones left
//		bestBombed[this.map.getPlayerIcon()-'0'] = 0; //set worst possible turn for us to compare with better turns
		
		this.setPlayerRankPhase2(); //this.playerRank[0]: Rank, this.playerRank[1]: numStones
		
		for(int player = 0; player < this.map.getNumOfPlayers(); player++){
			if(player == this.map.getPlayerIcon()-'0') continue;
			if(this.playerRankPhase2[player][0] <= this.playerRankPhase2[this.map.getPlayerIcon() -1 - '0'][0] && this.playerRankPhase2[player][0] >= rankActualUpperHatedEnemy){
				rankActualUpperHatedEnemy = this.playerRankPhase2[player][0];
				higherRankedPlayer = (char)(player + 1 + '0');
			}
			if(higherRankedPlayer == 'n' && this.playerRankPhase2[player][0] >= this.playerRankPhase2[this.map.getPlayerIcon() -1 - '0'][0] && this.playerRankPhase2[player][0] <= rankActualLowerHatedEnemy){
				rankActualLowerHatedEnemy = this.playerRankPhase2[player][0];
				lessRankedPlayer  = (char)(player + 1 + '0');
			}
		}
		
		//looks if higher ranked or lower ranked player is near enough
		int normallyDeletedFields = (int) Math.pow(2*this.map.getStrengthOfBombs() + 1, 2);
		int reachable = 0; // 1: higher one is reachable, 2 less one is reachable, if both: 3, higher one will be attacked
		if(higherRankedPlayer != 'n' && this.playerRankPhase2[higherRankedPlayer-1-'0'][1] - 0.5 * this.map.getNumOfBombs(this.map.getPlayerIcon()) * normallyDeletedFields >= this.playerRankPhase2[this.map.getPlayerIcon()-1 -'0'][1]) reachable += 1;
		if(lessRankedPlayer != 'n' && this.playerRankPhase2[lessRankedPlayer-1-'0'][1] <= this.playerRankPhase2[this.map.getPlayerIcon()-1 -'0'][1] - 0.75 * this.map.getNumOfBombs(this.map.getPlayerIcon()) * normallyDeletedFields) reachable += 2;
		
			
		//bomb next best enemy
		if(higherRankedPlayer != 'n' && (reachable == 1 || reachable == 3)){ //there is a player with more stones than us
			mostHatedEnemy = higherRankedPlayer;
		}
		else if(lessRankedPlayer != 'n'&& (reachable == 2)){ //we are the best player, destroy next best player
			mostHatedEnemy = lessRankedPlayer;
		}
		else{ // can only be if we are the only remaining players, will be searching for the less loss because of difference of stones lost of Player himself should be minimized
			mostHatedEnemy = this.map.getPlayerIcon();
		}	
		
		//try to send bomb on every possible position
		for(int row = 1; row <= this.map.getMapHeight(); row++){
			for(int col = 1; col <= this.map.getMapWidth(); col++){
				if(this.map.getItem(row, col) == '-') continue;	
				if(!this.turnTimer.mayContinueGamePhase2()) return bestBomb;
				
				mapSave = this.map.doubleMap(this.map.getMap());
				
				numOfBombedStones = this.map.setBomb(this.map.getPlayerIcon(), row, col, this.map.getStrengthOfBombs()); //returns loss of all enemy stones (ind = 0), lost stones of every player (ind = playericon - '0')				

				//our preferred enemy should loose as much stones as possible 
				if(numOfBombedStones[mostHatedEnemy - '0'] >= bestBombed[mostHatedEnemy - '0']){				
					//we should loose as few stones as possible while enemies loose much more than us
					if(numOfBombedStones[0] - numOfBombedStones[this.map.getPlayerIcon() - '0'] >=  bestBombed[0] - bestBombed[this.map.getPlayerIcon() - '0']){
						bestBombed = numOfBombedStones; //remember actual array of setBomb for later comparations
						bestBomb[0] = row; //y-coord of bomb to remember
						bestBomb[1] = col; //x-coord of bomb to remember
					}
				}
				
				
				this.map.setMap(mapSave);
			}
		}
		return bestBomb;
	}
	
	public void buildSpecialStoneMap(){
		int mapHeight = this.map.getMapHeight(), mapWidth = this.map.getMapWidth();
		specialStonesMap = new int[mapHeight+2][mapWidth+2];
		boolean bonus=false,choice=false,inversion=false;
		int bonusValue=0,choiceValue=0,inversionValue=0;
		bonusValue=200;
		char item;
		choiceValue=((mapHeight*mapWidth)/(mapHeight+mapWidth));
		inversionValue = choiceValue;
		for (int row = 1; row < (mapHeight+1); row++) {
			for (int col = 1; col < (mapWidth+1); col++) {
				item = this.map.getItem(row, col);				
				if ((bonus=(item == 'b'))||(choice=(item == 'c'))||(inversion=(item == 'i'))){
					for(int r = -1; r <= 1; r++){
						for(int c = -1; c <= 1; c++){
							if(row+r>=1 && row+r<=mapHeight && col+c>=1 && col+c<=mapWidth && !(r==0 && c==0)){
								if(bonus){
									specialStonesMap[row + r][col + c]-=bonusValue/2;
								}
								else if(choice){
									specialStonesMap[row + r][col + c]-=choiceValue;
								}
								else if(inversion){
									specialStonesMap[row + r][col + c]-=inversionValue;
								}
							}							
						}
					}
					if(bonus){
						specialStonesMap[row][col]+=bonusValue;
					}
					else if(choice){
						specialStonesMap[row][col]+=choiceValue*2;
					}
					else if(inversion){
						specialStonesMap[row][col]+=inversionValue*2;
					}
					
					bonus=false;
					choice=false;
					inversion=false;
				}
			}
		}
	}
		
	public void buildStratMap() {
		int row, col, counter;
		int positionOfTransition[] ={0,0,0};

		// Create the strategicMap
		this.strategicMap = new int[this.map.getMapHeight() + 2][this.map.getMapWidth() + 2];

		// Run through the map
		for (row = 0; row < (this.map.getMapHeight() + 2); row++) {
			for (col = 0; col < (this.map.getMapWidth() + 2); col++) {
				counter = 0;
				if (this.map.getItem(row, col) != '-') {
					//Check if there is no passage through the point in all 4 possible directions
					if(this.map.getItem(row, col-1)=='-'){
						if((map.existTransition(row, col,6)==false)){
							counter++;
							addObstruction(row,col,6);
						}
						else if(this.map.getItem(row, col+1)=='-'){
							if((map.existTransition(row, col,2)==false)){
								counter++;
								addObstruction(row,col,2);
							}
						}
					}
					else if(this.map.getItem(row, col+1)=='-'){
						if((map.existTransition(row, col,2)==false)){
							counter++;
							addObstruction(row,col,2);
						}						
					}
					if(this.map.getItem(row+1, col+1)=='-'){
						if((map.existTransition(row, col,3)==false)){
							counter++;
							addObstruction(row,col,3);
						}
						else if(this.map.getItem(row-1, col-1)=='-'){
							if((map.existTransition(row, col,7)==false)){
								counter++;
								addObstruction(row,col,7);
							}
						}
					}
					else if(this.map.getItem(row-1, col-1)=='-'){
						if((map.existTransition(row, col,7)==false)){
							counter++;
							addObstruction(row,col,7);
						}						
					}
					if(this.map.getItem(row-1, col+1)=='-'){
						if((map.existTransition(row, col,1)==false)){
							counter++;
							addObstruction(row,col,1);
						}
						else if(this.map.getItem(row+1, col-1)=='-'){
							if((map.existTransition(row, col,5)==false)){
								counter++;
								addObstruction(row,col,5);
							}
						}
					}
					else if(this.map.getItem(row+1, col-1)=='-'){
						if((map.existTransition(row, col,5)==false)){
							counter++;
							addObstruction(row,col,5);
						}						
					}
					if(this.map.getItem(row-1, col)=='-'){
						if((map.existTransition(row, col,0)==false)){
							counter++;
							addObstruction(row,col,0);
						}
						else if(this.map.getItem(row+1, col)=='-'){
							if((map.existTransition(row, col,4)==false)){
								counter++;
								addObstruction(row,col,4);
							}
						}
					}
					else if(this.map.getItem(row+1, col)=='-'){
						if((map.existTransition(row, col,4)==false)){
							counter++;
							addObstruction(row,col,4);
						}						
					}					
//					Set the value of the positions
					if(counter==4){
						//System.out.println(col+" " +row);
						int value = 0;
						int newcol =col;
						int newrow =row;
						//Check if neighbour is '-', count until '-' or until the end of a transition
						while(this.map.getItem(newrow, newcol-1)!='-'){
							value++;
							newcol--;
						}
						newcol=col;
						while((this.map.getItem(newrow, newcol+1)!='-')&&(newcol<map.getMapWidth())){
							value++;
							newcol++;	
						}
						newcol=col;
						while((this.map.getItem(newrow+1, newcol+1)=='-')&&(newrow<map.getMapHeight())&&(newcol<map.getMapWidth())){
							value++;
							newcol++;
							newrow++;
						}
							newcol=col;
							newrow=row;
						while((this.map.getItem(newrow-1, newcol-1)=='-')&&(newrow>1)&&(newcol>1)){
							value++;
							newrow--;
							newcol--;
						}
							newcol=col;
							newrow=row;
						while((this.map.getItem(newrow-1, newcol+1)=='-')&&(newrow>1)&&(newcol<map.getMapWidth())){
							value++;
							newrow--;
							newcol++;										
						}
						newcol=col;
						newrow=row;
						while((this.map.getItem(newrow+1, newcol-1)=='-')&&(newrow<map.getMapHeight())&&(newcol>1)){
							value++;
							newrow++;
							newcol--;						
						}
						newcol=col;
						newrow=row;
						while((this.map.getItem(newrow-1, newcol)=='-')&&(newrow>1)){
							value++;
							newrow--;
						}
						newrow=row;
						while((this.map.getItem(newrow+1, newcol)=='-')&&(newrow<map.getMapHeight())){
							value++;
							newrow++;					
						}
						newrow=row;
						//Change Values
						this.strategicMap[row][col]+=4*value;	
						if(this.map.getItem(row-1, col)!='-'){
							this.strategicMap[row-1][col]-=value/2;
						}
						else if(this.map.existTransition(row, col, 0)){
							positionOfTransition=this.map.getTransition(row, col, 0);
							this.strategicMap[positionOfTransition[0]][positionOfTransition[1]]-=value/2;
						}
						if(this.map.getItem(row-1, col-1)!='-'){
							this.strategicMap[row-1][col-1]-=value/2;
						}
						else if(this.map.existTransition(row, col, 7)){
							positionOfTransition=this.map.getTransition(row, col, 7);
							this.strategicMap[positionOfTransition[0]][positionOfTransition[1]]-=value/2;
						}
						if(this.map.getItem(row-1, col+1)!='-'){
							this.strategicMap[row-1][col+1]-=value/2;
						}
						else if(this.map.existTransition(row, col, 1)){
							positionOfTransition=this.map.getTransition(row, col, 1);
							this.strategicMap[positionOfTransition[0]][positionOfTransition[1]]-=value/2;
						}
						if(this.map.getItem(row+1, col+1)!='-'){
							this.strategicMap[row+1][col+1]-=value/2;
						}
						else if(this.map.existTransition(row, col, 3)){
							positionOfTransition=this.map.getTransition(row, col, 3);
							this.strategicMap[positionOfTransition[0]][positionOfTransition[1]]-=value/2;
						}
						if(this.map.getItem(row+1, col-1)!='-'){
							this.strategicMap[row+1][col-1]-=value/2;
						}
						else if(this.map.existTransition(row, col, 5)){
							positionOfTransition=this.map.getTransition(row, col, 5);
							this.strategicMap[positionOfTransition[0]][positionOfTransition[1]]-=value/2;
						}
						if(this.map.getItem(row+1, col)!='-'){
							this.strategicMap[row+1][col]-=value/2;
						}
						else if(this.map.existTransition(row, col, 4)){
							positionOfTransition=this.map.getTransition(row, col, 4);
							this.strategicMap[positionOfTransition[0]][positionOfTransition[1]]-=value/2;
						}
						if(this.map.getItem(row, col-1)!='-'){
							this.strategicMap[row][col-1]-=value/2;
						}
						else if(this.map.existTransition(row, col, 6)){
							positionOfTransition=this.map.getTransition(row, col, 6);
							this.strategicMap[positionOfTransition[0]][positionOfTransition[1]]-=value/2;
						}
						if(this.map.getItem(row, col+1)!='-'){
							this.strategicMap[row][col+1]-=value/2;
						}
						else if(this.map.existTransition(row, col, 2)){
							positionOfTransition=this.map.getTransition(row, col, 2);
							this.strategicMap[positionOfTransition[0]][positionOfTransition[1]]-=value/2;
						}
					}
					else if(counter==3){
						int value = 0;
						int newcol =col;
						int newrow =row;
						//Check if neighbour is '-', count until '-'
						//Don't count the neighbours where both sites of one direction have values
						if(this.map.getItem(row, col-1)=='-'||this.map.getItem(row, col+1)=='-'||col-1==0||col+1==this.map.getMapWidth()+2){
							while((this.map.getItem(newrow, newcol-1)!='-')&&(newcol>1)){
								value++;
								newcol--;
							}
							newcol=col;
							while((this.map.getItem(newrow, newcol+1)!='-')&&(newcol<map.getMapWidth()+1)){
								value++;
								newcol++;	
							}
							newcol=col;
						}
						if(this.map.getItem(row+1, col+1)=='-'||this.map.getItem(row-1, col-1)=='-'||((row+1==this.map.getMapHeight()+2)&&(col+1==this.map.getMapWidth()+2))||((row-1==0)&&(col-1==0))){
							while((this.map.getItem(newrow+1, newcol+1)!='-')&&(newrow<map.getMapHeight()+1)&&(newcol<map.getMapWidth()+1)){
								value++;
								newcol++;
								newrow++;
						}
							newcol=col;
							newrow=row;
							while((this.map.getItem(newrow-1, newcol-1)!='-')&&(newrow>1)&&(newcol>1)){
									value++;
									newrow--;
									newcol--;
							}
							newcol=col;
							newrow=row;
						}
						if(this.map.getItem(row-1, col+1)=='-'||this.map.getItem(row+1, col-1)=='-'||((row-1==0)&&(col+1==this.map.getMapWidth()+2))||((row+1==this.map.getMapHeight()+2)&&(col-1==0))){
							while((this.map.getItem(newrow-1, newcol+1)!='-')&&(newrow>1)&&(newcol<this.map.getMapWidth()+1)){
								value++;
								newrow--;
								newcol++;										
							}
							newcol=col;
							newrow=row;
							while((this.map.getItem(newrow+1, newcol-1)!='-')&&(newrow<this.map.getMapHeight()+1)&&(newcol>1)){
								value++;
								newrow++;
								newcol--;						
							}
							newcol=col;
							newrow=row;
						}
						if(this.map.getItem(row-1, col)=='-'||this.map.getItem(row+1, col)=='-'||row-1==0||row+1==this.map.getMapHeight()+2){
							while((this.map.getItem(newrow-1, newcol)!='-')&&(newrow>1)){
								value++;
								newrow--;
							}
							newrow=row;
							while((this.map.getItem(newrow+1, newcol)!='-')&&(newrow<map.getMapHeight()+1)){
								value++;
								newrow++;					
							}
							newrow=row;
						}
						//Return the value to the field
						this.strategicMap[row][col]+=value/3;
					}
					else if(counter==2){
						this.strategicMap[row][col]+=3;
					}
					else if(counter==1){
						this.strategicMap[row][col]+=2;
					}
					else if(counter==0){
						this.strategicMap[row][col]+=1;
					}
					
					
				}
			}
		}
					
		//printStratMap();
	}
	
	
	// ************************************************************************************
	// * Private Functions *
	// ************************************************************************************

	//ONLY SIMPLE METHOD
	private Comparator<int[]> sortListWithOrder(boolean order){ //orderAscending = true => ascending, else: descending
		
		Comparator<int[]> comp = new Comparator<int[]>(){ //places item if return is negative

			@Override
			public int compare(int[] firstItem, int[] secondItem) {
				int firstItemValue = firstItem[2];
				int secondItemValue = secondItem[2];
				
				if(order == DESCENDING){ //i.e. 9 7 5 3 1
					return secondItemValue - firstItemValue; 
				}
				return firstItemValue - secondItemValue;
			}			
		};
	
		return comp;
	}
	
	private int getMapValueViaHeuristic() {

		int heuristicValue = 0, value = 0;
		int col, row;
		this.buildSpecialStoneMap();
		//run through this valuation when there are 2 active players
		int stones = 0;
		
		if(this.activePlayers==2){
			rateSpecialStones();
			for (row = 1; row <= this.map.getMapHeight(); row++) {
				for (col = 1; col <= this.map.getMapWidth(); col++) {
					if (this.map.getItem(row, col) == this.map.getPlayerIcon()) 
					{					
						heuristicValue += (this.strategicMap[row][col]+specialStonesMap[row][col]);
						stones++;
					}
				}
			}
			int numMaxTurns = this.map.getMaxTurns();
			double turnFactor = Math.abs(Math.sin((turn * 180) / numMaxTurns));	//turn-variability: window biggest in middle of game with move that we don't get factor 0 (only one value ok)
			
			if(turnFactor < this.BREAKEVEN && numMaxTurns-turn <= 10 * numOfActivePlayers){ //normal game, stones not much influence
				heuristicValue -= (Math.pow(heuristicValue, turnFactor)*(1-turnFactor))*this.VALUE_HEURISTIK; //heuristic importance decreases in time
				heuristicValue += stones * this.VALUE_STONES * turnFactor; //stone importance increases in time
			}
			else{ //endgame, try to get as many stones as possible
				heuristicValue = stones;
			}
		}
		
		//run through this valuation when there are more than 2 active players
		else if(this.activePlayers>2){
			int ourRank;
			
//			this.map.setMap(oldmap);
//			setPlayerRank();
			rateSpecialStones(this.HEURISTICPLAYERDISTANCE);
			ourRank=playerRank[Character.getNumericValue(this.map.getPlayerIcon())][0];			
//			this.map.setMap(map);
			
			for (row = 1; row <= this.map.getMapHeight(); row++) {
				for (col = 1; col <= this.map.getMapWidth(); col++) {				
					if (this.map.getItem(row, col) == this.map.getPlayerIcon()) 
					{	
						int rating=0;
						if (oldMap[row][col] >='1' && oldMap[row][col] <=(char)(this.map.getNumOfPlayers()+'0')) 
						{	
							int enemyRank;
							enemyRank=playerRank[Character.getNumericValue(oldMap[row][col])][0];
							//If this player has a lower ranking than us
							if(ourRank>enemyRank){
								rating=this.HEURISTICPLAYERDISTANCE-(ourRank-enemyRank)/2;
							}
							//If this player is disqualified (or player has no stones: no position with a changed value)
							else if(enemyRank==-1){ 
								rating=this.HEURISTICPLAYERDISTANCE-(this.map.getNumOfPlayers()+1)/2;
							}
							//If this player has a higher rating or the same rating than us
							else{
								rating=this.HEURISTICPLAYERDISTANCE-(enemyRank-ourRank)/2;
							}
						}
						//If the position don't belong to a player
						else{
							rating=this.HEURISTICPLAYERDISTANCE;
						}

						value += (this.strategicMap[row][col]+specialStonesMap[row][col])*rating;
					}
				}
			}
			heuristicValue = value;
			
			int numMaxTurns = this.map.getMaxTurns();
			double turnFactor = Math.abs(Math.sin((turn * 180) / numMaxTurns));	//turn-variability: window biggest in middle of game with move that we don't get factor 0 (only one value ok)
			
			if(turnFactor < this.BREAKEVEN && numMaxTurns-turn <= 10 * numOfActivePlayers){	 //normal game, stones not much influence
				heuristicValue -= (Math.pow(heuristicValue, turnFactor)*(1-turnFactor))*this.VALUE_HEURISTIK; //heuristic importance decreases in time
				heuristicValue += stones * this.VALUE_STONES * turnFactor; //stone importance increases in time
			}
			else{ //if endgame: try to get as many stones as possible
				heuristicValue = stones;
			}
		}
		return heuristicValue; //-heuristicValueOld;
	}
	
	private ArrayList<int[]> getAllPathes(char player)
	{
		ArrayList<int[]> ways = new ArrayList<int[]>(); //arraylist of paths, dirs, then overwrite stone needed?
		int[] path;
		int row = 1, col = 1;  
		for(row = 1; row < this.map.getMapHeight() + 1; row++)
		{
			for(col = 1; col < this.map.getMapWidth() + 1; col++)
			{
				if(map.getItem(row, col) != '-' && (map.getNumOfOverrideStones(player)>0 || "0bic".contains(Character.toString(this.map.getItem(row, col))))) //if player has overwrite or player can go to field
				{
					path = this.map.searchPathes(player, row, col);
					int value = 0;
					for(int dir = 0; dir < 8; dir++)
					{ 
						value += path[3+dir];
					}
					if(value > 0) //ads path only if path valid (if stones are more than 0
					{
						path[2] = value; //adds number of overwritten stones to path "heuristic value" as sort of simple heuristic for turn sorting
						//System.out.println("y" + row + "x" +col + "player" + player);
						if(this.map.getItem(row, col) == 'c') //if stone is choice stone add possibility for all players
						{                              
							for(int currentPlayer = 1; currentPlayer <= this.map.getNumOfPlayers(); currentPlayer++) //tests choice stone for all players, adds them to possible turn
							{
								int[] choicePath = new int[path.length];
								for(int i = 0; i < path.length; i++)
								{
									choicePath[i] = path[i];
								}
 //                                                              {
                                                                      //path[2] = this.POS_INFINITY; //sets stones taken to positive infinity because it can be very beneficial
								choicePath[12] = currentPlayer;
								ways.add(choicePath);
  
  //                                                              }
							}
						}
						else //if not choice stone then continue normally
						{
							ways.add(path);
						}
					}
					if(this.map.getItem(row, col) == 'x') //if path is x add it anyways
					{
						path[0] = row;
						path[1] = col;
						
						path[11] = -1;
						
						ways.add(path);
					}
				}
 //                              }
			}
		}
		return ways;
	}
	
	
	private int[] goKindOfParanoid(char[][] prevMap, char player, int actualSearchDepth, int maxSearchDepth, int alpha, int beta, int actuallyUsedOverridesThisPath) //returns x, y, hvalue, dirs, overwriteUsedThisTurn, special, maxOverridesThisPath
	{		

		int overWriteStones = this.map.getNumOfOverrideStones(player);
		int[] v = new int[14];
		v[2] = this.NEG_INFINITY;
		boolean order = DESCENDING; //if player is self (maximiser)
		boolean maximiser = true;
		char nextPlayer = (char) ((((player-'0')%this.map.getNumOfPlayers())+1)+'0');
		int overrideFactor;
		
		boolean enableOverrides = (this.turn/this.map.getMaxTurns() >= 0.9999) && !this.map.existPlayerChanges();
		
		ArrayList<int[]> possibleTurns = this.getAllPathes(player); //gets all possible turns
		char[][] actualMap;
		
		
		if(player != this.map.getPlayerIcon()) //if player is not self (minimiser)
		{
			maximiser = false;
			v[2] = this.POS_INFINITY;
			order = ASCENDING;
		}
		else if (!enableOverrides){ //if player is us and we aren't in end of phase 1 as defined above
			ArrayList<int[]> tempTurns = new ArrayList<int[]>();
			tempTurns = getNormalTurns(possibleTurns); //remove Override-Possibilities
			if(tempTurns.size() > 0){ //if there is at least one possible turn left which needs no override, try only these
				possibleTurns = tempTurns;
			}
			else{} //overrides needed, leave everything as it is and test all overrides

		}
		
		if(!possibleTurns.isEmpty() && this.map.playerExists(player))
		{
			
			v[0] = possibleTurns.get(0)[0]; //initializes v values
			v[1] = possibleTurns.get(0)[1]; //initializes v values
		
			if ((possibleTurns.size() > 1 && actualSearchDepth < maxSearchDepth - 2) && this.turnSort) //if there are more turns than 1 and if we are deeper than search depth 2 and if turn sort is enabled
				Collections.sort(possibleTurns, sortListWithOrder(order)); //sort turns in order (ascending if minimiser, descending if maximiser
			
			int temp = actuallyUsedOverridesThisPath;
			for(int[] turn : possibleTurns)
			{	
				if(!this.turnTimer.mayContinueGame()) break;
				
				int bomb = 0;				
				actuallyUsedOverridesThisPath = temp;
//				int[] pathInfo = new int[8];	unused
				overrideFactor = 50;
				actualMap = this.map.doubleMap(prevMap); //copies map from last player/turn
				this.map.setMap(actualMap);
				
				if(playerChangesOnMap) playerChangesOnMap = this.map.existPlayerChanges();
				if(playerChangesOnMap) overrideFactor = this.OVERRIDE_RATING_CHANGE; //check if i or c exist on map

				int bombFactor = overrideFactor;
				
				char stone = this.map.getItem(turn[0], turn[1]); //remembers coordinate stone
				this.map.setStone(player, turn[0], turn[1]); //does turn
				
				//this.map.printMap();
				if(stone == 'b') //needs to decide if should pick bomb or override
				{
					//System.out.println("dis is da bomb");
					if(this.specialStones[0] > this.specialStones[1]) //chooses bomb
					{
						bomb++; //give bonus for taking a bomb!
						turn[12] = 20;
					}
					else //chooses overwrite
					{
						turn[11] += 1; //give bonus for taking a overwrite Stone!
						turn[12] = 21;
					}
				}
				else if(stone == 'i') //inversion Stone!
				{
					this.map.inversionStone();
				}
				else if(stone == 'c') //has to run through every players position
				{
					this.map.choiceStone(player, (char) (turn[12] + '0'));
//					System.out.print("Choice:" +  turn[12] + " value: ");
				}
				
				if(maximiser) actuallyUsedOverridesThisPath += turn[11];
		
				int saveAfterTurn2 = 0;
				
				if(maxSearchDepth>actualSearchDepth) //if not last need to go recursive
				{
					//System.out.println("recursive " + actualSearchDepth);
					
					int[] values = this.goKindOfParanoid(actualMap, nextPlayer, actualSearchDepth + 1, maxSearchDepth, alpha, beta, actuallyUsedOverridesThisPath);
					turn[2] = values[2];
					saveAfterTurn2 = values[2];
					turn[13] = values[13];
				}
				else //get hValue
				{
					turn[2] = this.getMapValueViaHeuristic() + getMobilityValue();// + 2*overrideFactor*turn[11]*this.specialStones[1] + bombFactor*bomb*this.specialStones[0];
					saveAfterTurn2 = turn[2];
					turn[13] = actuallyUsedOverridesThisPath;
				}
				
				if(maximiser)
				{
					saveAfterTurn2 = turn[2] + 2*overrideFactor*turn[13]*this.specialStones[1] + bombFactor*bomb*this.specialStones[0];
//					System.out.println("max: " + this.turn + "_" + actualSearchDepth + "_" + v[2] + "_" + saveAfterTurn2 + "_alpha: " + alpha + "_" + beta);
					if(v[2] < saveAfterTurn2)
					{
						v = turn;			
					}
					if(v[2] > alpha)
					{
						alpha = v[2];
						
						//if((v[2]>beta) || (v[2]<alpha))
						if(v[2] >= beta)
						{
							if(actualSearchDepth == 0){
								this.searchDepthComplete = true;
								v[2] = saveAfterTurn2;
							}
							this.incNumOfStates();
							this.map.setMap(prevMap);
							this.map.setOverrideStones(player, overWriteStones);
							//System.out.println("cutof maxi");
							return v;
						}
					}

				}
				else
				{
					saveAfterTurn2 = turn[2] - 2*overrideFactor*turn[11]*this.specialStones[1];
					if(v[2]>saveAfterTurn2)
					{
						v = turn;
					}
					if(v[2] < beta)
					{
						beta = v[2];
						
						//if((v[2]>beta) || (v[2]<alpha))
						if(v[2] <= alpha)
						{
							this.incNumOfStates();
							this.map.setMap(prevMap);
							this.map.setOverrideStones(player, overWriteStones);
							//System.out.println("cutof mini");
							return v;
						}
					}
					
				}
				this.map.setOverrideStones(player, overWriteStones); //resets overwrite stones
				if(logging != null && logging[2]) {
					this.addToDataset(2, this.map.getNumOfPlayers(), this.turn, turn[0]-1, turn[1]-1,  maxSearchDepth, actualSearchDepth, beta, alpha, turn[2], saveAfterTurn2, this.map.getNumOfOverrideStones(this.map.getPlayerIcon()), turn[11], 2*overrideFactor*turn[11]*this.specialStones[1], turnTimer.getFullTime()-turnTimer.getCurrentTurnTime());
					this.singleDatasetWriter(2);
				}
				this.incNumOfStates();
			}
			if(actualSearchDepth == 0 && possibleTurns.indexOf(turn) == possibleTurns.size() - 1) this.searchDepthComplete = true;
		}
		else //player cant do turn!
		{
			//System.out.println("player" + player + "cant do turn!");
			if(maxSearchDepth>actualSearchDepth) //need to go recursive
			{
				int[] temp = this.goKindOfParanoid(prevMap, nextPlayer, actualSearchDepth + 1, maxSearchDepth, alpha, beta, actuallyUsedOverridesThisPath);
				//System.out.println("recursive " + actualSearchDepth);
				v[2] = temp[2];
				v[13] = temp[13];
				
			}
			else //get hValue
			{
				v[2] = this.getMapValueViaHeuristic() + getMobilityValue();
				v[13] = actuallyUsedOverridesThisPath;
			}
		}
		this.incNumOfStatesBy(possibleTurns.size());
		this.map.setMap(prevMap);
		this.map.setOverrideStones(player, overWriteStones); //resets overwrite stones
		return v;
	}
	
	private ArrayList<int[]> getNormalTurns(ArrayList<int[]> possibleTurns){
		ArrayList<int[]> turns = new ArrayList<int[]>();
		boolean bonusDetected = false;
		boolean choiceDetected = false;
		boolean inversionDetected = false;
		
		for(int[] turn : possibleTurns){
			if(turn[11] == 0){
				if(!bonusDetected && !choiceDetected && !inversionDetected) turns.add(turn);
				
				if(this.map.getItem(turn[0], turn[1]) == 'b'){
					if(!bonusDetected) turns.clear();
					turns.add(turn);
					bonusDetected = true;
				}
				else if(!bonusDetected && this.map.getItem(turn[0], turn[1]) == 'c'){
					if(!choiceDetected) turns.clear();
					turns.add(turn);
					choiceDetected = true;
				}
				else if(!bonusDetected && !choiceDetected && this.map.getItem(turn[0], turn[1]) == 'i'){
					if(!inversionDetected) turns.clear();
					turns.add(turn);
					inversionDetected = true;
				}
				
			}

		}
		return turns;
	}
	
	private void buildObstructionMap()
	{
		this.obstructionMap = new int[this.map.getMapHeight() + 2][this.map.getMapWidth() + 2];
	}
	
	private void addObstruction(int y, int x, int dir)
	{
		int passage = 0;
		if(dir == 0 || dir == 4) //vertical
			passage = 1;
		else if(dir == 1 || dir == 5) //raising
			passage = 2;
		else if(dir == 2 || dir == 6) //horizontal
			passage = 4;
		else if(dir == 3 || dir == 7) //falling
			passage = 8;
		this.obstructionMap[y][x] = this.obstructionMap[y][x] | passage;
	}
	
	private boolean getObstruction(int y, int x, int dir)
	{
		int passage = 0;
		if(dir == 0 || dir == 4) //vertical
			passage = 1;
		else if(dir == 1 || dir == 5) //raising
			passage = 2;
		else if(dir == 2 || dir == 6) //horizontal
			passage = 4;
		else if(dir == 3 || dir == 7) //falling
			passage = 8;
		int value = this.obstructionMap[y][x] & passage;
		if (value > 0)
			return true;
		else 
			return false;
	}
	
	public int getMobilityValue(){ //returns mobilityValue in dependence to possible ways
		int value=0; //arraylist of paths, dirs, then overwrite stone needed?
		int row = 1, col = 1;  
		char player = this.map.getPlayerIcon();
		boolean path = false;
		
		for(row = 1; row < this.map.getMapHeight() + 1; row++)
		{
			for(col = 1; col < this.map.getMapWidth() + 1; col++)
			{
				path = false;
				if("0bic".contains(Character.toString(this.map.getItem(row, col)))) //if player has overwrite or player can go to field
				{
					path = this.map.isPathValid(player, row, col);
					if(path){
						value++;
						if(value >= this.numOfActivePlayers) break;
					}
				}
			}
			if(value >= this.map.activePlayers()) break;
		}
		if(value >= this.numOfActivePlayers) return (value + 120 + bonusOnEnemiesDeath(this.numOfActivePlayers));
		switch(value){
		case 0: value = -2000;
		case 1: value = 0;
		case 2: value = 5;
		case 3: value = 10;
		case 4: value = 20 + bonusOnEnemiesDeath(this.numOfActivePlayers);
		case 5: value = 25 + bonusOnEnemiesDeath(this.numOfActivePlayers);
		case 6: value = 30 + bonusOnEnemiesDeath(this.numOfActivePlayers);
		case 7: value = 35 + bonusOnEnemiesDeath(this.numOfActivePlayers);
		default: value = 0 + bonusOnEnemiesDeath(this.numOfActivePlayers);
		}
		return value;
	}
	
	private int bonusOnEnemiesDeath(int numOfActivePlayers){
		int bonusValue = 0;
		int[] activePlayers = {0, 0, 0, 0, 0, 0, 0, 0};
		int sum = 0;
		for(int row = 0; row < this.map.getMapHeight() + 1; row++){
			for(int col = 0; col < this.map.getMapWidth() + 1; col++){
				char item = this.map.getItem(row, col);
				if(item >= '1' && item <= '8' && activePlayers[item-1-'0'] != 1){
					activePlayers[item-1-'0'] = 1;
					sum++;
					if(numOfActivePlayers > sum){
						bonusValue += 50;
						return bonusValue;
					}
				}
			}
		}
		return bonusValue;
	}
}
