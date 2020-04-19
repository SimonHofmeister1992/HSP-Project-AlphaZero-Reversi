package GUI;

public interface IMap {
	
	public void setObserver(Frame_Board board);
	public void removeObserver();
	
	public int getMapHeight();
	public int getMapWidth();
	
	public int getNumOfOverrideStones(char player);
	public int getNumOfBombs(char player);
	public char getItem(int row, int col);
	public char getPlayerIcon();
	
	public int getRowOffset();
	public int getColOffset();
	
	public void updateBoard();
}
