namespace ReversiXT_Client.Enums
{
    public enum ServerMessageTypes : byte
    {
        GroupNumber = 1,
        Initialize = 2,
        PlayerNumber = 3,
        TurnRequest = 4,
        TurnAnswer = 5,
        Turn = 6,
        Disqualification = 7,
        EndTurnPhase = 8,
        EndBombPhase = 9
    }
}
