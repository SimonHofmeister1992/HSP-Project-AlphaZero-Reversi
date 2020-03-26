package configwriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ConfigBuilder {

	private ArrayList<String[]> configs;
	private ConfigWriter cfw;
	
	public ConfigBuilder(String configpath){
		configs = new ArrayList<String[]>();
		cfw = new ConfigWriter(configpath);
	}
	
	public void buildFromDefault(ArrayList<String> names, ArrayList<double[]> vals, int phase){
		System.out.println("start building configs");
		int position = 0;
		int tempPos;
		
		String configName;
		
		
		for(double[] row : vals){
			for(int col = 1; col < row.length; col++){
				if(row[col] == -999 || (col == 1 && position > 0)) configs.add(null);
				else {
					tempPos = 0;
					String[] config = new String[names.size()+1];
					configName = "";
					for(double[] values : vals){
						String line ="";
						if(tempPos != position) {
							line += names.get(tempPos) + values[1]; 
							configName += "_" + values[1];
						}
						else{
							line += names.get(tempPos) + values[col];
							configName += "_x" + values[col];
						}
//						System.out.println(line);
						config[tempPos] = line;
						tempPos++;
					}
					config[tempPos] = configName;
					configs.add(config);
//					System.out.println();
//					System.out.println();
				}
			
			}
			position++;
		}
		
		System.out.println("end building configs, numOfConfigs: " + configs.size());
		
		cfw.writeConfigs(this.configs);
	}
	
	public void buildFromSelected(ArrayList<String> names, ArrayList<double[]> vals, int phase, int row, String configname, String[] config){
		if(row == 0) System.out.println("start building configs");	

		String[] newConfig;	
		String tempName;
		
		for(int col = 1; col < vals.get(row).length; col++){
			newConfig = new String[names.size()+1];
			tempName = configname;
			
			for(int r = 0; r < row; r++){
				newConfig[r] = config[r];
			}
			newConfig[row] = names.get(row) + vals.get(row)[col];
			
			tempName += "_" + vals.get(row)[col];
//			System.out.println(tempName);
			if(row < names.size()-1){
				buildFromSelected(names, vals, phase, row+1, tempName, newConfig);
			}
			else{
				newConfig[names.size()-1+1] = tempName;
				configs.add(newConfig);
			}
		}
		if(row == 0){
			System.out.println("end building configs, numOfConfigs: " + configs.size());
			
			for(String[] str : configs){
				for(int k = 0; k < str.length; k++){
					System.out.println(str[k]);

				}
				System.out.println();
				System.out.println();
			}
			
			
			long seed = System.nanoTime();
			Collections.shuffle(configs, new Random(seed));
			cfw.writeConfigs(this.configs);
		}
	}
		
}
