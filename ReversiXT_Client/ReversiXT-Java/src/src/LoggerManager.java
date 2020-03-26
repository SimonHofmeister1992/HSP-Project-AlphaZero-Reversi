package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LoggerManager {
	
	private String logFileName = "newFile";
	private String logDirectory = "";
	private boolean isLogging = false;
	private ArrayList<String> dataList;
	
	public LoggerManager(){
		try{
			fetchLogDirectory();
			if(getIsLogging()){
				this.dataList = new ArrayList<String>();
				this.logFileName = this.createFileName();
			}	
		}
		catch (IOException e){
			
		}
	}
	
	private void setIsLogging(boolean isLogging){
		this.isLogging = isLogging;
	}
	
	private boolean getIsLogging(){
		return this.isLogging;
	}
	
	private String getFileName(){
		return this.logFileName;
	}
	
	private void setLogDirectory(String dir){
		dir.replace("\\", File.separator);
		
		File file = new File(dir);
		if(file.isDirectory()){
			this.logDirectory = dir;
			setIsLogging(true);
		}
	}
	
	public void noticeData(int num, Object o){
		dataList.add(String.valueOf(num));
		String data = o.toString();
		dataList.add(data);
	}
	
	private String getLogDirectory(){
		return this.logDirectory;
	}
	
	private void fetchLogDirectory() throws IOException{
			File logConfigFile = new File("."+ File.separator + "configs" + File.separator + "logconfig.txt");

			BufferedReader br = new BufferedReader(new FileReader(logConfigFile));
			StreamTokenizer st = new StreamTokenizer(br);
			st.whitespaceChars(',', ',');

			String logDirectory = "";
			int numOfNull = 0;

			while(st.nextToken() != StreamTokenizer.TT_EOF){
				if(st.sval == null){
					if(numOfNull == 0) {
						logDirectory += ':';
						numOfNull++;
					}
					else{
						logDirectory += File.separator;
					}
				}
				else{
					logDirectory += st.sval;
				}
				
			}
			
			setLogDirectory(logDirectory);
			br.close();
	}
	
	private String createFileName(){
		Date now = new Date();		
		int numOfLog = this.getLastLogFileName();
		numOfLog++;
		String newLogNumber = String.valueOf(numOfLog);
		String logNum="";
		for(int i = newLogNumber.length(); i <= 6; i++){
			logNum += "0";
		}
		logNum += newLogNumber;
		SimpleDateFormat df = new SimpleDateFormat( "yyMMdd_HHmmss");
		String newLogFileName = logNum + "_log_" + df.format(now) + ".txt";

		return newLogFileName;
	}
	
	private int getLastLogFileName(){
		int numOfOldLogFile = findFiles(new File(this.logDirectory), ".txt");
		return numOfOldLogFile;
	}
	
	private static int findFiles(File file, String extension) {
		String tempCurrentFileName ="";
		String tempCurrentDigitsOfLog ="";
		int tempCurrentNumOfLog = 0, newFileNumOfLog = 0;
		
		if (file.isHidden());
		if (file.isFile() && file.getName().endsWith(extension)){
			tempCurrentFileName = file.getName();
			tempCurrentDigitsOfLog = tempCurrentFileName.substring(0, 7);
			
			tempCurrentNumOfLog = Integer.parseInt(tempCurrentDigitsOfLog);
			if(tempCurrentNumOfLog > newFileNumOfLog) newFileNumOfLog = tempCurrentNumOfLog;
		}
		if (!file.isDirectory() || file.listFiles() == null){}
		else{
			for (File subfile : file.listFiles()) {
					tempCurrentNumOfLog = findFiles(subfile, extension);
					if(tempCurrentNumOfLog > newFileNumOfLog) newFileNumOfLog = tempCurrentNumOfLog;
			
 		}

			
		}
		return newFileNumOfLog;
		}
	
	public void writeData() throws IOException{
		
		int num = Integer.parseInt(this.dataList.get(0));
		FileWriter fw, fw2;
		BufferedWriter bw;
		PrintWriter pw;
		String fileName = this.getFileName().substring(0, this.getFileName().indexOf("g")+1) + num + this.getFileName().substring(this.getFileName().indexOf("g")+1);
		System.out.println(this.getLogDirectory() + File.separator + fileName);
		if(num != 0){
			fw = new FileWriter(this.getLogDirectory() + File.separator + fileName, true);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
		}
		else{
			fw2 = new FileWriter(this.getLogDirectory() + File.separator + "0000000_log0_gamelog.txt", true);
			bw = new BufferedWriter(fw2);
			FileReader fr2 = new FileReader(this.getLogDirectory() + File.separator + "0000000_log0_gamelog.txt");
			BufferedReader br = new BufferedReader(fr2);
			pw = new PrintWriter(bw);
			if(br.readLine() == null){
				pw.append("AnzahlSpieler,AnzahlZuege,VerbleibendeZeit,Spieler1,Spieler2,Spieler3,Spieler4,Spieler5,Spieler6,Spieler7,Spieler8"+System.lineSeparator());
			}
			br.close();
		}


		int numData = 0, valueIndex = 0;
		for (String data : this.dataList) {
			if(valueIndex > 0 && valueIndex % 2 == 1){
				if(numData > 0) {
					pw.append(',');
				}
				pw.append(data);
				numData++;
			}
			valueIndex++;
		}
		pw.append(System.lineSeparator());
		pw.close();
		this.dataList = new ArrayList<String>();
	}
}
