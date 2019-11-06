using ReversiXT_Client.Algorithms;
using ReversiXT_Client.Array;
using ReversiXT_Client.Constants;
using ReversiXT_Client.Enums;
using ReversiXT_Client.Game;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ReversiXT_Client.Connection
{
    public static class MessageBuilder
    {

        public static byte GroupNumber = 12; // server can't count to 12...loser :P


        #region Setting bytes

        private static void SetReceivedBytes(int startByte, int endByte, byte[] byteMessage, ref byte[] bytesToInsert)
        {
            int j = bytesToInsert.Length - 1;
            for (int i = startByte; i <= endByte; i++)
            {
                bytesToInsert[j]=byteMessage[i];
                j--;
            }
        }

        private static void SetBytesToSend(int startByte, int endByte, byte[] bytesToInsert, byte[] bytesToModify)
        {
            int j = bytesToInsert.Length - 1;
            for (int i = startByte; i <= endByte; i++)
            {
                bytesToModify[i] = bytesToInsert[j];
                j--;
            }
        }

        private static byte[] SetMessageLengthBytes(byte[] bytes, byte[] bytesToInsert)
        {
            int j = 1;
            for (int i = bytesToInsert.Length - 1; i >= 0; i--)
            {
                bytes[j] = bytesToInsert[i];
                j++;
            }

            return bytes;
        }

        #endregion


        #region Building Message

        public static byte[] BuildMessage(ServerMessageTypes type, Position position = new Position(), byte player = 0)
        {
            byte[] bytes = new byte[0];
            byte[] array = new byte[0];
            uint length = 0;

            switch (type)
            {
                case ServerMessageTypes.GroupNumber:
                    length = ServerMessageTypeLengths.MinLength + ServerMessageTypeLengths.GroupNumberLength;
                    array = InitializeAndSetMessageType(length, ServerMessageTypes.GroupNumber);
                    array = SetMessageLengthBytes(array, BitConverter.GetBytes(ServerMessageTypeLengths.GroupNumberLength));
                    bytes = GetGroupNumberBytes(array);

                    break;

                case ServerMessageTypes.TurnAnswer:
                    if (ServerComponent.PhaseOne)
                    {
                        length = ServerMessageTypeLengths.MinLength + ServerMessageTypeLengths.TurnAnswerLength;
                        array = InitializeAndSetMessageType(length, ServerMessageTypes.TurnAnswer);
                        SetMessageLengthBytes(array, BitConverter.GetBytes(ServerMessageTypeLengths.TurnLength - 1));

                        position = Valuation();

                        //OnlyTestAction
                        //TestActions.WritePossibleMoves();

                        //here special move handling
                        bytes = GetPositionToByte(position, array);
                    }
                    else
                    {
                        length = ServerMessageTypeLengths.MinLength + ServerMessageTypeLengths.TurnAnswerLength;
                        array = InitializeAndSetMessageType(length, ServerMessageTypes.TurnAnswer);
                        SetMessageLengthBytes(array, BitConverter.GetBytes(ServerMessageTypeLengths.TurnLength - 1));

                        position = ValuationBombPhase();

                        //no special moves in bombphase (should be always 0)
                        bytes = GetPositionToByte(position, array);
                    }

                    break;

                default:
                    bytes = null;
                    break;
            }

            return bytes;
        }

        private static byte[] InitializeAndSetMessageType(uint length, ServerMessageTypes serverMessageType)
        {
            var array = new byte[length];
            array[0] = Convert.ToByte(serverMessageType);
            return array;
        }

        #endregion


        #region PositionToByte
        private static byte[] GetPositionToByte(Position position, byte[] refBytes)
        {
            var bytes = refBytes;
            var columnbytes = BitConverter.GetBytes(Convert.ToUInt16(position.ColumnPosition));
            var rowBytes = BitConverter.GetBytes(Convert.ToUInt16(position.RowPosition));
            byte specialStone;

            //TODO Ändern ... korregieren
            specialStone = SpecialMoves.SpecialMoveHandlingToSend(position);
            
            SetBytesToSend(5, 6, columnbytes, bytes); // setting columns
            SetBytesToSend(7, 8, rowBytes, bytes); // setting rows
            bytes[9] = specialStone;

            return bytes;
        }

        private static byte[] GetPositionToByteWithPlayer(Position position, byte[] refBytes, byte player)
        {
            byte[] bytes = GetPositionToByte(position, refBytes);
            ArrayMethods<byte>.NewLength(bytes);

            bytes[11] = player;

            return bytes;
        }

        #endregion

        #region Setting Message Bytes
        internal static void SetMessagesMessage(ref Message message)
        {
            message.ByteMessage = new byte[message.Length];
            int b = ServerComponent.Stream.Read(message.ByteMessage, 0, message.Length);
        }

        internal static void SetMessageLength(ref Message message, byte[] initializeBytes)
        {
            var extractedBytes = ExtractBytes(1, 4, initializeBytes);

            message.Length = BitConverter.ToInt32(extractedBytes, 0);
        }

        internal static void SetMessageType(ref Message message, byte[] initializeBytes)
        {
            message.Type = (ServerMessageTypes)initializeBytes[0];
        }

        #endregion

        private static Position Valuation()
        {

            Position position;
            ABPruningIterative.SetValuationForNextMove(ServerComponent.TimeLimit, ServerComponent.Depth);
            //ABPruningIterative.SetValuationForNextMoveWithAspirationWindow(ServerComponent.TimeLimit, ServerComponent.Depth);
            position = MoveValuation.GetBestMove();
            Print.WriteLine("BestMoveValue: " + position.Value);

            return position;
        }

        private static Position ValuationBombPhase()
        {
            Position position;
            BombPhase.SetValuationForNextMove(ServerComponent.TimeLimit, ServerComponent.Depth);
            position = MoveValuation.GetBestMove();
            return position;
        }

        private static byte[] GetGroupNumberBytes(byte[] groupNumberBytes)
        {
            groupNumberBytes[5] = GroupNumber;

            return groupNumberBytes;
        }

        internal static Position AnalyseReceivedMessage(Message receivedMessage, ref Position position)
        {
            var columnBytes = new byte[2];
            var rowBytes = new byte[2];

            SetReceivedBytes(0, 1, receivedMessage.ByteMessage, ref columnBytes);
            SetReceivedBytes(2, 3, receivedMessage.ByteMessage, ref rowBytes);

            position = new Position()
            {
                ColumnPosition = BitConverter.ToInt16(columnBytes, 0),
                RowPosition = BitConverter.ToInt16(rowBytes, 0)
            };

            return position;
        }

        internal static string[] AnalyseReceivedMessage(Message receivedMessage)
        {
            string[] message = null;
            string mapString = Encoding.ASCII.GetString(receivedMessage.ByteMessage);
            message = mapString.Split('\n');

            return message;
        }

        private static byte[] ExtractBytes(int startByte, int endByte, byte[] bytes)
        {
            byte[] returnBytes = new byte[endByte - (startByte - 1)];

            int j = endByte - startByte;
            for (int i = startByte; i <= endByte; i++)
            {
                returnBytes[j] = bytes[i];
                j--;
            }

            return returnBytes;
        }

        private static void Disqualification()
        {
            Rules.CountPlayers--;
        }
    }
}
