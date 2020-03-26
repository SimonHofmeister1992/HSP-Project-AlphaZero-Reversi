using ReversiXT_Client.Game;
using ReversiXT_Client.Array;
using System;

namespace ReversiXT_Client.Algorithms
{
    public class Paranoid
    {
        public static int ExpansionsParanoid;
        //---MAP-VALUATION-CALL-SIMON--------------------------------------------------------------------------------------------------------------------
        #region MAP-VALUATION-SIMON
        public static void SetValuationForNextMove(int MaxSearchDepth = 3)
        {
            ABPruningIterative.startTimeHeuristic = DateTime.Now;
            ExpansionsParanoid = 0;

            //Static Arrays initializing
            Map.CurrentPosition = Map.SetCurrentPositions(Rules.OurPlayer, Map.MapFields);
            PossibleMoves.PossibleMoveArray = PossibleMoves.SetPossibleMoves(Rules.OurPlayer, Map.CurrentPosition, Map.MapFields, true);

            //Check if possibleMoves are empty
            if (PossibleMoves.PossibleMoveArray.Length < 1)
            { Print.WriteLine("We do not have any moves!!"); return; }

            //foreach possibleMove in the static map
            for (int moveIndex = 0; moveIndex < PossibleMoves.PossibleMoveArray.Length; moveIndex++)
            {
                //create temp map
                char[,] newMapForMove = ArrayMethods<char>.CloneArray(Map.MapFields);
                newMapForMove = Map.SetStoneInTempMapWithoutSpecialMoves(Rules.OurPlayer, PossibleMoves.PossibleMoveArray[moveIndex], newMapForMove);
                ExpansionsParanoid++;

                //give our possibleMove a MapValue
                int MapValuationForMove = SetValuationRecursive(Rules.OurPlayer, newMapForMove, 1, MaxSearchDepth);
                PossibleMoves.PossibleMoveArray[moveIndex].Value = MapValuationForMove;

            }
            Print.WriteLine("Expansions: " + ExpansionsParanoid);
        }

        private static int SetValuationRecursive(char player, char[,] newMap, int recursionDepth, int MaxSearchDepth)
        {

            if (recursionDepth < MaxSearchDepth)
            {
                char targetPlayer = HelpMethods.NextPlayer(player);

                //next recursive preparation
                char[,] newMapForMove = ArrayMethods<char>.CloneArray(newMap);
                Position[] newCurPosition = Map.SetCurrentPositions(targetPlayer, newMapForMove);
                Position[] newPossibleMoves = PossibleMoves.SetPossibleMoves(targetPlayer, newCurPosition, newMapForMove, false);

                //TODO: Check if newPossibleMoves are empty! -> no recursion 
                if (newPossibleMoves.Length == 0)
                {
                    if (targetPlayer == Rules.OurPlayer) { return -1000; }
                    else { return 1000; }
                }

                //for each move in newPossibleMoves
                for (int indexMove = 0; indexMove < newPossibleMoves.Length; indexMove++)
                {
                    //update map for next depth
                    char[,] possibleMoveMap = ArrayMethods<char>.CloneArray(newMapForMove);
                    possibleMoveMap = Map.SetStoneInTempMapWithoutSpecialMoves(targetPlayer, newPossibleMoves[indexMove], possibleMoveMap);
                    ExpansionsParanoid++;
                    if (indexMove == 0) recursionDepth++;
                    //-----RECURSION-----
                    int mapValue = SetValuationRecursive(targetPlayer, possibleMoveMap, recursionDepth, MaxSearchDepth);
                    //-------------------
                    newPossibleMoves[indexMove].Value = mapValue;
                }

                //-----Search algorithms-----
                return MinMaxSearch(targetPlayer, newPossibleMoves); //Paranoid-Algorithm
            }

            //Valuation for map
            //return HelpMethods.CallValuations(Rules.OurPlayer, newMap);
            return HelpMethods.CountStones(Rules.OurPlayer, newMap);
        }
        #endregion
        //---END-MAP-VALUATION-CALL-SIMON--------------------------------------------------------------------------------------------------------------------


        private static int MinMaxSearch(char player, Position[] movesWithValuation)
        {

            //Maximum algorithm for our player
            if (player == Rules.OurPlayer)
            {
                int bestPos = movesWithValuation[0].Value;
                foreach (Position pos in movesWithValuation)
                {
                    if (pos.Value > bestPos)
                        bestPos = pos.Value;
                }
                return bestPos;
            }
            //Minimum algorithm for other player
            else
            {
                int bestPos = movesWithValuation[0].Value;
                foreach (Position pos in movesWithValuation)
                {
                    if (pos.Value < bestPos)
                        bestPos = pos.Value;
                } 
                return bestPos;
            }

        }
    }
}
