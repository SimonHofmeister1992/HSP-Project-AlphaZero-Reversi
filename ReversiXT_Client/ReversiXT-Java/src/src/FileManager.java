package src;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;

public class FileManager {

	// ************************************************************************************
	// * Attributes *
	// ************************************************************************************

	private Map playground;
	private char[][] map;
	private String fileName;
	private String rawMap;
	private int[] mapInformation;
	private HashMap<String, String> transitions;
	private boolean online;

	// ************************************************************************************
	// * Constructor *
	// ************************************************************************************

	public FileManager(String input, Map playground, boolean online) throws IOException {
		this.playground = playground;
		this.online = online;
		if (online)
		{
			this.rawMap = input;
		}
		else
		{
			this.fileName = input;
		}
		this.mapInformation = new int[6];
		this.transitions = new HashMap<String, String>();
		do {
			loadMap();
		} while (map == null);
	}

	// ************************************************************************************
	// * Public Functions *
	// ************************************************************************************

	public void getWholeMap() {
		this.playground.setNumOfPlayers(this.mapInformation[0]);
		this.playground.setStrengthOfBombs((this.mapInformation[3]));
		this.playground.setMapHeight(this.mapInformation[4]);
		this.playground.setMapWidth(this.mapInformation[5]);

		this.playground.setMap(this.map);
		this.playground.setTransitions(this.transitions);
	}

	public char[][] getMap() {
		return this.map;
	}

	public int[] getMapInformation() {
		return this.mapInformation;
	}

	public HashMap<String, String> getTransitions() {
		return this.transitions;
	}

	public void printReadedFile() {
		System.out.println(mapInformation[0]);
		System.out.println(mapInformation[1]);
		System.out.println(mapInformation[2] + " " + mapInformation[3]);
		System.out.println(mapInformation[4] + " " + mapInformation[5]);

		for (int row = 1; row < mapInformation[4]; row++) {
			for (int col = 1; col < mapInformation[5]; col++) {
				System.out.print(map[row][col] + " ");
			}
			System.out.println();
		}

		int pos = 0;
		for (String key : transitions.keySet()) {
			if (pos % 3 == 0)
				System.out.println(key + " <-> " + transitions.get(key));
			pos++;
		}
	}

	// ************************************************************************************
	// * Private Functions *
	// ************************************************************************************
	private void loadMap() throws IOException {
		BufferedReader br = null;
		if (online)
		{
			InputStream stream = new ByteArrayInputStream(this.rawMap.getBytes());
			br = new BufferedReader(new InputStreamReader(stream));
		}
		else
		{
			br = new BufferedReader(new FileReader(this.fileName));
		}
		StreamTokenizer st = new StreamTokenizer(br);
		st.whitespaceChars(' ', ' ');
		st.ordinaryChar('-');
		st.ordinaryChar('<');
		st.ordinaryChar('>');
		st.wordChars('-', '-');
		while (st.nextToken() != StreamTokenizer.TT_EOF) {

			this.mapInformation[0] = (int) st.nval; // number of players on map
			st.nextToken();
			this.mapInformation[1] = (int) st.nval; // number of override-stones
													// per player
			st.nextToken();
			this.mapInformation[2] = (int) st.nval; // number of bomb-stones per
													// player
			st.nextToken();
			this.mapInformation[3] = (int) st.nval; // strength of bomb-stones
			st.nextToken();
			this.mapInformation[4] = (int) st.nval; // height of map
			st.nextToken();
			this.mapInformation[5] = (int) st.nval; // width of map

			int mapHeight = this.mapInformation[4];
			int mapWidth = this.mapInformation[5];
			this.map = new char[mapHeight + 2][mapWidth + 2];

			// Load Map
			for (int row = 0; row < mapHeight + 2; row++) {
				for (int col = 0; col < mapWidth + 2; col++) {
					if (row == 0 || row == mapHeight + 1 || col == 0 || col == mapWidth + 1) {
						this.map[row][col] = '-';
					} else {
						st.nextToken();
						if (st.sval != null)
							this.map[row][col] = st.sval.charAt(0);
						else
							this.map[row][col] = (char) (st.nval + 48);
					}
				}
			}

			// Load Transitions
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				String value1 = "", value2 = "";
				value1 = (int) (st.nval + 1) + " ";
				st.nextToken();
				value1 += (int) (st.nval + 1) + " ";
				st.nextToken();
				value1 += (int) st.nval + "";
				st.nextToken(); // don´t take <-> in Array
				st.nextToken();
				st.nextToken();
				st.nextToken();
				value2 = (int) (st.nval + 1) + " ";
				st.nextToken();
				value2 += (int) (st.nval + 1) + " ";
				st.nextToken();
				value2 += (int) st.nval + "";

				this.transitions.put(value1, value2);
				this.transitions.put(value2, value1);
			}
		}
		br.close();
	}

}
