package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class Frame_Board extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final int BUTTON_HEIGHT = 35;
	private final int BUTTON_WIDTH = 45;
	
	private IMap map;
	private IServerConn serverConn;
	private JPanel frame;
	private JPanel board;
	private JMenuBar menubar;
	
	private int numOfRows;
	private int numOfCols;
	private int rowOffset, colOffset;
	private char ownPlayer;
	
	public int actualRow, actualCol;
	
	public Frame_Board(IMap map, IServerConn serverConn){
		this.map = map;
		this.serverConn = serverConn;
		
		initialize();
		
		setTitle("ReversiXT");
//		setSize(500, 500);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		createMenuBar();
		createPlayboard();
		
		createUsability();
		
		add(this.frame);
			
		pack();
		super.setAlwaysOnTop(true);
		setVisible(true);		
	}
	
	public void update(){

		this.remove(this.board);
//		repaint();
//		requestFocus();
		createPlayboard();
//		System.out.println("I am here");

		
	}
	
	private void initialize(){
		this.numOfCols = this.map.getMapWidth();
		this.numOfRows = this.map.getMapHeight();
		this.rowOffset = this.map.getRowOffset();
		this.colOffset = this.map.getColOffset();
		this.ownPlayer = this.map.getPlayerIcon();
		
		this.frame = new JPanel(true);
		this.frame.setLayout(new BorderLayout());
	}
	
	
	private void createUsability(){
		JPanel usability = new JPanel();
		JButton automatic = new JButton("Manuell spielen");
		automatic.addActionListener(new ActionListener(){
			boolean automatics = true;
			@Override
			public void actionPerformed(ActionEvent arg0) {
				automatics = !automatics;	
				Frame_Board.this.serverConn.toggleAutomatics(automatics);
				if(automatics) automatic.setText("Manuell spielen");
				else automatic.setText("Auto-Modus");
			}
			
		});
		automatic.setPreferredSize(new Dimension(200,80));
		usability.add(automatic);
		this.frame.add(BorderLayout.EAST, usability);
	}
	
	
	private void createPlayboard(){
		this.board = new JPanel(true);
		Dimension dim = new Dimension(BUTTON_WIDTH * this.numOfCols, BUTTON_HEIGHT * this.numOfRows);
		board.setPreferredSize(dim);
		this.board.setLayout(new GridLayout(this.numOfCols, this.numOfRows));
		
		drawBoard();
		frame.add(BorderLayout.WEST, this.board);
		
		board.updateUI();
	}
	
	private void drawBoard(){
		Dimension dim = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
		char item;
		JButton temp;
		for(int row = 0 + rowOffset; row < numOfRows + rowOffset; row++){
			for(int col = 0 + colOffset; col < numOfCols + colOffset; col++){
				
				item = map.getItem(row, col);
				if(item != '-' && item != '0'){
						if(item == 'b' || item == 'c' || item == 'i') item = (char)(item - 'a' + 'A');
			            if(item >= '1' & item <= '8') temp = getColoredButton(item);
			            else{
			            	temp = new JButton(String.valueOf(item));
			            }
			    }
			    else{
			          temp = new JButton();
			    }
				this.actualCol = col;
				this.actualRow = row;
				temp.addActionListener(new ActionListener(){
					int col = Frame_Board.this.actualCol;
					int row = Frame_Board.this.actualRow;	
					@Override
					public void actionPerformed(ActionEvent arg0) {
						int choice = 0;
						if(map.getItem(row, col) == 'b') choice = 21;
						if(map.getItem(row, col) == 'c') choice = map.getPlayerIcon();

						serverConn.sendTurn(row, col, choice);
						
					}
					
				});
				temp.setRolloverEnabled(false);
				temp.setPreferredSize(dim);
				temp.setBackground(getBackgroundColor(item)); 
				this.board.add(temp, dim);
				
			}
		}
	}
	
	private void createMenuBar(){
		this.menubar = new JMenuBar();
		
		JMenu menu = new JMenu("Datei");
		JMenuItem exit = new JMenuItem("Beenden");
		exit.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();		
			}		
		});
		
		menu.add(exit);
		menubar.add(menu);
		this.setJMenuBar(menubar);
	}
	
	private Color getBackgroundColor(char item){
		Color color = Color.WHITE;

		switch(item){
			case 'I': color = Color.lightGray; break;
			case 'C': color = Color.CYAN; break;
			case 'x': break;
			case 'B': color = Color.YELLOW; break;
			case '0': color = Color.WHITE; break;
			case '1': break;
			case '2': break;
			case '3': break;
			case '4': break;
			case '5': break;
			case '6': break;
			case '7': break;
			case '8': break;
			default: color = Color.BLACK;
		}
		if(item == ownPlayer){
			color = Color.ORANGE;
		}
		return color;
	}
	
	private JButton getColoredButton(char item){
		JButton temp = new JButton(); 
		
			temp = new JButton() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;


				char field = item;
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					int nXPosition = 13;
					int nYPosition = 8;
					int nWidth = getWidth() - nXPosition * 2;
					int nHeight = getHeight() - nYPosition * 2;

					Color color = null;
					switch(field){
					case '1': color = Color.RED; break;
					case '2': color = Color.BLUE; break;
					case '3': color = Color.GREEN; break;
					case '4': color = Color.YELLOW; break;
					case '5': color = new Color(0,139,139); break;
					case '6': color = Color.MAGENTA; break;
					case '7': color = Color.ORANGE; break;
					case '8': color = Color.DARK_GRAY; break;
					}
					g.setColor(color);

					g.drawOval(nXPosition, nYPosition, nWidth, nHeight);
					g.fillOval(nXPosition, nYPosition, nWidth, nHeight);

				}

			};
	return temp;	
	}
}
