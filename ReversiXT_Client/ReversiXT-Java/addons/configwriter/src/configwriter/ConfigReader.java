package configwriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigReader {
	
	private ConfigBuilder configBuilder;
	private ArrayList<String> fileEntries;
	
	
	ConfigReader (int phase) {
		String path;
		switch(phase){
		case 1: path = "."+ File.separator + "configs" + File.separator + "newConfigs1"; break;
		case 2: path = "configs" + File.separator + "configsTemp2"; break;
		default: path = "."+ File.separator + "configs" + File.separator + "newConfigs1";
		}
		
		this.configBuilder = new ConfigBuilder(path);
		if(phase == 1) phase1();
		else{
			phase2();
		}
	}
	
	private void phase1(){
		File defaultConfig = new File("."+ File.separator + "configs" + File.separator + "default" + File.separator + "defaultconfig.txt");
		this.fileEntries = new ArrayList<String>();
		
		try{
			FileReader fr = new FileReader(defaultConfig);
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			while ((line = br.readLine()) != null) {
		        fileEntries.add(line);
		    }
			br.close();
			fileEntryPreprocessing(1);
		}
		catch(IOException e){
			System.out.println("Oooops, something went wrong while reading defaultconfig");
		}
		
		
	}
	
	private void phase2(){
		File selectedFilesFolder = new File("."+ File.separator + "configs" + File.separator + "selectedConfigs2");		

		ArrayList<String> newDefaultConfig = new ArrayList<String>();


		
		try{
			HashMap<String, String> hm = new HashMap<String, String>();		
			for(File selectedFile : selectedFilesFolder.listFiles()){
				
				FileReader fr = new FileReader(selectedFile);
				BufferedReader br = new BufferedReader(fr);
				
				String line;
				String[] content;
				
				int lineNum = 0;
				String actualString;
				
				while ((line = br.readLine()) != null) {
					if(line.endsWith("x")){
						

			        	line = line.replaceFirst("; //x", "");
			        	content = line.split(" = ");
			        	
			        	if(!hm.containsKey(String.valueOf(lineNum))){
			        		hm.put(String.valueOf(lineNum), content[0] + " = // ");
			        	}
			        	
			        	actualString = hm.get(String.valueOf(lineNum));
			        	actualString += content[1] + " ";
			        				        	
			        	hm.remove(String.valueOf(lineNum));
			        	hm.put(String.valueOf(lineNum), actualString);
			        	
			        	
			        }
					lineNum++;
			    }
				br.close();
			}
//			System.out.println(hm.size());
        	for(int i = 0; i < hm.size(); i++){
 //       		System.out.println(hm.get(String.valueOf(i)));
        		newDefaultConfig.add(hm.get(String.valueOf(i)));
        	}
			fileEntries = newDefaultConfig;
			
			fileEntryPreprocessing(2);
		}
		catch(IOException e){
			System.out.println("Couldn't read Files in Folder selectedConfigs2");
		}

	}
	
	private void fileEntryPreprocessing(int phase){
		System.out.println("start preprocessing");
		ArrayList<String> varnames = new ArrayList<String>();
		ArrayList<double[]> vals = new ArrayList<double[]>();
		
		String[] contents;
		String var;
		String[] textVals;
		double[] values;
		
		for(String line : fileEntries){
			contents = line.split("//");
			var = contents[0];
			textVals = contents[1].split(" ");			
			values = new double[textVals.length];
			for(int i = 0; i < textVals.length; i++){
				if(i!=0) values[i] = Double.parseDouble(textVals[i]);
			}
			
			varnames.add(var);
			vals.add(values);
		}
		System.out.println("end preprocessing");
		
		String[] config = new String[varnames.size()];
		
		if(phase == 1)configBuilder.buildFromDefault(varnames, vals, phase);
		else if(phase == 2) configBuilder.buildFromSelected(varnames, vals, phase, 0, "", config);
	}
}
