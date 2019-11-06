using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Threading.Tasks;
using System.IO;
using ReversiXT_Client.Game;
using ReversiXT_Client.Enums;
using ReversiXT_Client.Constants;
using ReversiXT_Client.Algorithms;
using ReversiXT_Client.Array;
using System.Net;
using System.Threading;

namespace ReversiXT_Client.Connection
{
    public class ServerComponent
    {
        #region MessageBuilt

        // Nachrichtenaufbau:
        // 1. Typ (8-Bit Integer) / z.b. Zugaufforderung
        // 2. Länge der Nachricht (32-Bit Integer)
        // 3. Nachricht (in Bytes)

        //Typ 2: Spielfeld als String (ASCII)
        //Typ 3: Zuweisung Spielernummber (8-Bit Ingeger)
        //Typ 4: Zugaufforderung (Zeitlimit: 32-Bit-Integer, Suchtiefe: 8-Bit-Integer)
        //Typ 5: Zugantwort(x,y,: 16-Bit-Ingegers, Sonderfelder: 8-Bit-Integer)
        //Typ 6: Spielzug (x,y, 16-Bit-Integer, Sonderfelder: 8-Bit-Integer, Spielernr: 8-Bit-Integer) //TODO nachschauen
        //Typ 7: Disqualifikation (Spielernr: 8-Bit-Integer)
        //Typ 8: Ende der Zugphase (Steine)
        //Typ 9: Ende der Bombenphase -> Ende des Spiels

        #endregion

        #region Fields

        private static TcpClient _tcpClient;
        internal static NetworkStream Stream;
        internal static int TimeLimit;
        internal static int Depth;

        public static List<char> DisqualifiedPlayers = new List<char>();

        public static bool PhaseOne = true;

        #endregion

        #region Connect

        public static void Connect()
        {
            bool isConnected = false;

            while (!isConnected)
            {
                Print.WriteLine("Connecting.....");
                if (_tcpClient == null)
                {
                    _tcpClient = new TcpClient();
                }
                try
                {
                    _tcpClient.Connect(Program._connectionIp, Program._port);
                    isConnected = true;
                }
                catch (Exception e)
                {
                    Print.WriteLine("Error (log)");
                    Logger.Log(e);
                    Thread.Sleep(4000);
                }
            }

            if (Stream == null && _tcpClient.Connected)
            {
                Stream = _tcpClient.GetStream();
            }
        }

        #endregion

        #region Commit

        private static void CommitBytes(byte[] bytesToCommit)
        {
            try
            {
                ASCIIEncoding asen = new ASCIIEncoding();
                //byte[] data = Encoding.ASCII.GetBytes(bytesToCommit);
                Print.WriteLine("Transmitting.....");
                Stream.Write(bytesToCommit, 0, bytesToCommit.Length);
                Print.WriteLine("Sent: ");
                foreach (var committedbyte in bytesToCommit)
                {
                    Print.Write(committedbyte.ToString());
                }

                Print.WriteLine("\nTransmitted.");
            }
            catch (Exception e)
            {
                Logger.Log(e);
                Print.WriteLine("Error (log)");
            }
        }

        public static void Commit(ServerMessageTypes type)
        {
            var bytes = MessageBuilder.BuildMessage(type);
            CommitBytes(bytes);
            //CustomTimer.StopTimer();
            // Print.WriteLine(CustomTimer.GetNeededTime() + " ms needed");
        }

        #endregion

        #region Receive
        private static Message Receive()
        {
            try
            {
                Message message = new Message();
                byte[] initializeBytes = new byte[ServerMessageTypeLengths.MinLength];
                int k = Stream.Read(initializeBytes, 0, initializeBytes.Length);

                MessageBuilder.SetMessageType(ref message, initializeBytes);
                MessageBuilder.SetMessageLength(ref message, initializeBytes);
                MessageBuilder.SetMessagesMessage(ref message);

                return message;
            }
            catch (Exception e)
            {
                Print.WriteLine("Error (log)");
                Logger.Log(e);
                return new Message();
            }
        }

        #endregion

        #region HandleReceivedMessage

        public static void HandleReceivedMessage()
        {
            Message message = Receive();
            byte[] array = new byte[0];

            switch (message.Type)
            {
                case ServerMessageTypes.TurnRequest:
                    Map.CountOurTurn++;

                    Print.WriteLine(message.ToString());

                    // Set TimeLimit
                    TimeLimit = GetTimeLimit(message.ByteMessage);
                    if(TimeLimit > 0) { CustomTimer.StartTimer(TimeLimit); }

                    // Set Depth
                    Depth = message.ByteMessage[4];

                    // Commit
                    Commit(ServerMessageTypes.TurnAnswer);

                    // Stop Timer
                    if (TimeLimit > 0) { CustomTimer.StopTimer(); }

                    // Print used Time
                    TimeSpan total = DateTime.Now - ABPruningIterative.startTimeHeuristic;
                    Print.WriteLine("----------TIME USED:" + total.TotalMilliseconds + "------------------");
                    Print.WriteLine("TURN (" + Map.CountOurTurn + ")");
                    Print.WriteLine();
                    break;

                case ServerMessageTypes.Turn:
                    
                    Map.CountTurn++;
                    Print.WriteLine(message.ToString());
                    Position position = new Position();
                    MessageBuilder.AnalyseReceivedMessage(message, ref position);

                    if (PhaseOne)
                    {
                        char player = Convert.ToChar(message.ByteMessage[5].ToString());

                        char targetPlayer = '0';
                        position.SpecialAttribute = Map.MapFields[position.RowPosition, position.ColumnPosition];
                        Map.SetStone(player, position);

                        if (position.SpecialAttribute == SpecialStones.Choice)
                        {
                            targetPlayer = Convert.ToChar(message.ByteMessage[4].ToString());
                        }

                        SpecialMoves.SpecialMoveHandlingIncoming(position, player, targetPlayer);
                    }
                    else
                    {
                        BombPhase.SetBomb(position);
                    }
                    
                    //for documentation
                    //HelpMethods.CountPlayerStones(Map.MapFields);
                    TestActions.WriteMap();
                    Print.WriteLine("TURN (" + Map.CountOurTurn + ") - GAMETURN (" + Map.CountTurn + ")");
                    Print.WriteLine();

                    break;

                case ServerMessageTypes.Initialize:
                    //No printing -> better performance
                    //Print.WriteLine(message.ToString());
                    string[] lines = MessageBuilder.AnalyseReceivedMessage(message);
                    Rules.SetGameRules(lines);
                    Map.SetMap(lines);
                    TestActions.WriteMap();
                    break;

                case ServerMessageTypes.PlayerNumber:
                    Print.WriteLine(message.ToString());
                    Rules.OurPlayer = char.Parse(message.ByteMessage[0].ToString());
                    break;

                case ServerMessageTypes.Disqualification:
                    Print.WriteLine(message.ToString());
                    char disqualifiedPlayer = char.Parse(message.ByteMessage[0].ToString());

                    DisqualifiedPlayers.Add(disqualifiedPlayer);

                    if (disqualifiedPlayer == Rules.OurPlayer)
                        Environment.Exit(1);

                    break;

                case ServerMessageTypes.EndTurnPhase:
                    PhaseOne = false;
                    Heatmap.InitHeatMapForBombPhase();
                    break;

                case ServerMessageTypes.EndBombPhase:
                    Environment.Exit(0);

                    break;

                default:
                    message = new Message();
                    break;
            }
        }

        private static int GetTimeLimit(byte[] byteMessage)
        {
            var timeBytes = new byte[4];
            int j = timeBytes.Length - 1;
            for (int i = 0; i < timeBytes.Length; i++)
            {
                timeBytes[j] = byteMessage[i];
                j--;
            }

            return BitConverter.ToInt32(timeBytes, 0);
        }

        #endregion

        public static void Dispose()
        {
            Stream.Close();
            Stream = null;
            _tcpClient.Close();
            _tcpClient = null;
        }
    }
}