package configwriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class ConfigWriter {
	
	private String configPath;
	
	public ConfigWriter(String configpath){
		this.configPath = configpath;
	}
	
	public void writeConfigs(ArrayList<String[]> configs){
		System.out.println("start writing configs");
		
		int count = 0;
		int folderNum = 0;
		
		for(String[] config : configs){
			if(config != null) writeConfig(config, folderNum);
			count++;
			if(count % 8 == 0) folderNum++;
		}
		
		
		System.out.println("end writing configs");
	}
	
	private void writeConfig(String[] config, int folderNum){
		String configName;
		
		String folder = this.configPath + File.separator + folderNum;
		
		File actualFolder = new File(folder);
		File file;
		
		try{
			if(!actualFolder.exists()) actualFolder.mkdirs();
			
			configName = folderNum + config[config.length - 1];
			System.out.println(configName);
			file = new File(folder + File.separator + configName + ".txt");
			file.createNewFile();
			
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			String[] searchImportantVar = configName.split("_");
			
			for(int line = 0; line < config.length - 1; line++){				
				
				if(searchImportantVar[line+1].startsWith("x")){
					bw.write(config[line] + ";" + " //x");
					bw.newLine();
				}
				else{
					bw.write(config[line] + ";");
					bw.newLine();
				}

			}
			bw.close();
		}
		catch(Exception e){
				System.out.println("Folder couldn't be created: " + actualFolder.getAbsolutePath());
		}
	}
	
}
