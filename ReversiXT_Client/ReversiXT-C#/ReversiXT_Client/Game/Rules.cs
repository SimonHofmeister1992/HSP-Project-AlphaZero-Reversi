using ReversiXT_Client.Connection;
using ReversiXT_Client.Enums;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ReversiXT_Client.Game
{
    public static class Rules
    {
        #region Constants
        // ToDo: make variable and include public set-method for bombs and overwrite-stones
        public const int MapHeightWidthLine = 3;
        public const int MapHeightColumn = 0;
        public const int MapWidthColumn = 1;
        public const int BombLine = 2;
        public const int BombCountColumn = 0;
        public const int BombStrengthColumn = 1;
        public const int CountPlayersLine = 0;
        public const int OverrideStonesLine = 1;
        public const int StartMapPositionsLine = 4;
        #endregion

        //this should be a better performance "list"
        //public static Transition[] Transitions = new Transition[1];
        public static Transition[] Transitions { get; set; }

        //Initialize
        public static void SetGameRules(string[] gameInfo)
        {
            CountPlayers = int.Parse(gameInfo[CountPlayersLine]);
            CountOverrideStones = int.Parse(gameInfo[OverrideStonesLine]);
            CountBombs = int.Parse(gameInfo[BombLine].Split(' ')[BombCountColumn]);
            BombStrength = int.Parse(gameInfo[BombLine].Split(' ')[BombStrengthColumn]);
            MapHeigth = int.Parse(gameInfo[MapHeightWidthLine].Split(' ')[MapHeightColumn]);
            MapWidth = int.Parse(gameInfo[MapHeightWidthLine].Split(' ')[MapWidthColumn]);
            Transitions = GetTransitions(gameInfo);
            BombsLeft = CountBombs;
        }

        //Initialize - Transistions
        private static Transition[] GetTransitions(string[] gameInfo)
        {
            var transitions = new Transition[gameInfo.Length - StartMapPositionsLine - MapHeigth-1];

            var mapHeight = int.Parse(gameInfo[MapHeightWidthLine].Split(' ')[0]);

            int currentTransitionIndex = 0;
            for (int i = StartMapPositionsLine + mapHeight; i < gameInfo.Length - 1; i++)
            {
                var transitionInfo = gameInfo[i].Split(' ');

                transitions[currentTransitionIndex] = new Transition()
                {
                    // See Specifications: x , y , direction <-> x ,y, direction

                    StartColumn = int.Parse(transitionInfo[0]),
                    StartRow = int.Parse(transitionInfo[1]),
                    StartDirection = (Directions)byte.Parse(transitionInfo[2]),
                    EndColumn = int.Parse(transitionInfo[4]),
                    EndRow = int.Parse(transitionInfo[5]),
                    EndDirection = (Directions)byte.Parse(transitionInfo[6])
                };

                currentTransitionIndex++;

            }

            return transitions;
        }

        public static int CountPlayers { get; set; }
        public static int BombsLeft { get; set; }
        public static int CountOverrideStones { get; set; }
        public static int CountBombs { get; set; }
        public static int BombStrength { get; private set; }
        public static int MapHeigth { get; private set; }
        public static int MapWidth { get; private set; }
        public static char OurPlayer { get; set; }
    }
}
