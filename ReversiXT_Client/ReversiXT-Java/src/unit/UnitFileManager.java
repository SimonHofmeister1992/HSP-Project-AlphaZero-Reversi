package unit;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;
import src.FileManager;

public class UnitFileManager {
	private FileManager fm;
	private int[] mapInformation;
	private char[][] map;
	private HashMap<String, String> transitions;
	
	@Test
	public void test() {
		//fail("Not yet implemented");
		String fileName = "maps/test.txt";
		try{
			this.fm = new FileManager(fileName, null, false);
			testMapInformation();
			testMap();
			testTransitions();
		}
		catch(IOException io){
			fail("FileManager couldn´t load file, please check the filename and directory of the map 'maps/test.txt'");
		}
		catch(Exception e){
			fail("Problems in loading map");
		}
	}
	
	private void testMapInformation(){
		this.mapInformation = fm.getMapInformation();
		int countCorrectNumbers = 0;
		
		for(int index = 0; index < 6; index++){
			switch(index){
				case 0: if(mapInformation[index] == 2) countCorrectNumbers++; break;
				case 1: if(mapInformation[index] == 3) countCorrectNumbers++; break;
				case 2: if(mapInformation[index] == 2) countCorrectNumbers++; break;
				case 3: if(mapInformation[index] == 1) countCorrectNumbers++; break;
				case 4: if(mapInformation[index] == 4) countCorrectNumbers++; break;
				case 5: if(mapInformation[index] == 4) countCorrectNumbers++; break;
				default: fail("testing map information: problems with test itself");
			}
			if(countCorrectNumbers != index + 1){
				fail("testing map information: position " + index + "/5 is not correct");
			}
		}
	}
	
	private void testMap(){
		this.map = fm.getMap();
		int rows = this.mapInformation[4];
		int columns = this.mapInformation[5];
		for(int row = 1; row <= rows; row++){
			for(int col = 1; col <= columns; col++){
				switch((row-1)*columns+(col-1)){
					case 0: if(this.map[row][col] != '-') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 1: if(this.map[row][col] != '0') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 2: if(this.map[row][col] != '0') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 3: if(this.map[row][col] != '-') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 4: if(this.map[row][col] != '-') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 5: if(this.map[row][col] != '1') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 6: if(this.map[row][col] != '2') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 7: if(this.map[row][col] != '-') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 8: if(this.map[row][col] != '0') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 9: if(this.map[row][col] != '2') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 10: if(this.map[row][col] != '1') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 11: if(this.map[row][col] != '0') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 12: if(this.map[row][col] != '0') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 13: if(this.map[row][col] != '0') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 14: if(this.map[row][col] != '0') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					case 15: if(this.map[row][col] != '0') fail("testing map: the element in row " + row + " and column " + col + " is not correct"); break;
					default: fail("testing map: problem with test itself");
				}
			}
		}
	}
	
	private void testTransitions(){
		this.transitions = fm.getTransitions();
		int fails = 0, pos = 0;
		
		for (String key : transitions.keySet()) {
			switch(pos){
				case 0: if(key == "2 4 4" && transitions.get(key) != "1 0 0") fails++; break;
				case 1: if(key == "2 1 0" && transitions.get(key) != "1 3 4") fails++; break;
				case 2: if(key == "3 4 4" && transitions.get(key) != "2 0 0") fails++; break;
				case 3: if(key == "3 1 0" && transitions.get(key) != "2 3 4") fails++; break;
				default: fail("testing transitions: problems with test itself");
			}
			if(fails != 0){
				fail("testing transitions: key " + key + " is not correct");
			}
			pos++;
		}
	}
}


