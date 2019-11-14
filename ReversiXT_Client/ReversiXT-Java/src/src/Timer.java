package src;

import java.util.Date;

public class Timer {
	private int MIN_REST_TIME = 80;
	private int MIN_REST_TIME_PHASE2 = 250;
	
	private Date gameStart;
	private Date startTime;
	private Date endTime;
	private long turnTime;
	private double FACTOR = 1.0;
	
	public Timer(){
		gameStart = new Date();
		setStartTime();
	}
	
	public void setMIN_REST_TIME(int value){
		if(MIN_REST_TIME > 40){
			this.MIN_REST_TIME = value;
		}
		else{
			this.MIN_REST_TIME = 40;
		}
	}
	
	public void setTurnFactor(double FACTOR){
		this.FACTOR = FACTOR;
	}
	
	public String getWholeGameTime(){
		Date now = new Date();
		long timeDiff = now.getTime() - gameStart.getTime();
		String timeNeeded = String.valueOf(((timeDiff/(1000*60*60)))) + ":" + String.valueOf(((timeDiff/(1000*60)%60))) + ":" + String.valueOf((timeDiff/(1000)%60));
		return timeNeeded;
	}
	
	public void setTurnTime(long turntime){
		this.turnTime = turntime;
	}
	
	public void setEndTime(){
		this.endTime = new Date();
	}
	
	public void setStartTime(){
		this.startTime = new Date();
	}
	
	public long getCurrentTurnTime(){
		Date tempTime = new Date();
		long timeDiff = tempTime.getTime() - this.startTime.getTime() + this.MIN_REST_TIME; //min_rest_Time simulates, we would have needed more time, to abort the right time
		return timeDiff;
	}
	
	public long getFullTime()
	{
		return turnTime;
	}
	
	public boolean mayContinueGame(){
		if(this.turnTime == 0 || (FACTOR*this.turnTime - this.getCurrentTurnTime()) > this.MIN_REST_TIME) return true;
		return false;
	}
	
	public boolean mayContinueGamePhase2(){
		if(this.turnTime == 0 || (this.turnTime - this.getCurrentTurnTime()) > MIN_REST_TIME_PHASE2) return true;
		return false;
	}
	
	public void printTimeDifference(){
		setEndTime();
		long timeDiff = endTime.getTime() - startTime.getTime();

		double sec = timeDiff / 1000;
		double mSec = timeDiff % 1000;

		System.out.print("Time needed: sec: " + sec + " msec: " + mSec + " ");
	}
}
