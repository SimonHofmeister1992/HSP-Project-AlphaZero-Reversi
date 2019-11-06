using System;
using ReversiXT_Client.Constants;
using ReversiXT_Client.Game;

namespace ReversiXT_Client.Algorithms
{
    public class HelpMethods
    {
        //---NEXT-PLAYER----------------------------------------------------------------------------------------------------------------------------------------
        public static char NextPlayer(char player)
        {
            int curPlayer = Convert.ToInt32(player);
            curPlayer = curPlayer - 49 + 1;
            curPlayer = curPlayer % Rules.CountPlayers;
            curPlayer = curPlayer + 49;
            return (char)curPlayer;
        }
        //---END-NEXT-PLAYER----------------------------------------------------------------------------------------------------------------------------------------

        //---COUNT-STONES--TEST-HEURISTIC-------------------------------------------------------------------------------------------------------------------------
        public static int CountStones(char player, char[,] map)
        {
            Position[] StonesFromPlayer = Map.SetCurrentPositions(player, map);
            return StonesFromPlayer.Length;
        }
       //---END-COUNT-STONES--TEST-HEURISTIC-------------------------------------------------------------------------------------------------------------------------

        /// <summary>
        /// Count all stones from player - seperate
        /// Output "Player: StoneCount"
        /// </summary>
        /// <param name="map">target map</param>
        public static void CountPlayerStones(char[,] map)
        {
            int[] count = new int[Rules.CountPlayers + 1];
            for (int index = 0; index < count.Length; index++)
            { count[index] = 0; }

            foreach (char value in map)
            {
                if (FieldChecks.IsFieldFreeOrSpecial(value))
                    continue;
                int curPlayer = Convert.ToInt32(value);
                curPlayer = curPlayer - 48;
                count[curPlayer]++;
            }

            for(int player = 0; player < count.Length; player++)
            {
                Print.WriteLine("Player: " + player + " Stones: " + count[player]);
            }
        }
    }
}
