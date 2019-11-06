namespace ReversiXT_Client.Constants
{
    public class ServerMessageTypeLengths
    {
        public const uint GroupNumberLength = 1;
        public const uint PlayerNumberLength = 1;
        public const uint TurnRequestTimeLength = 4;
        public const uint TurnRequestDepthLength = 1; // will be removed
        public const uint TurnAnswerLength = 5;
        public const uint TurnLength = 6;
        public const uint DiqualificationLength = 1;
        public const uint MinLength = 5;
    }
}
