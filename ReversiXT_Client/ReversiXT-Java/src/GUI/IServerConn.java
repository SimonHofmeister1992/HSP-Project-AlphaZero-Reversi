package GUI;

public interface IServerConn {
	public void sendTurn(int row, int col, int choice);
	public void toggleAutomatics(boolean bool);
}
