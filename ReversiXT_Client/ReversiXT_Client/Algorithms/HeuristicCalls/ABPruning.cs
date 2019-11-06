using ReversiXT_Client.Game;
using ReversiXT_Client.Array;
using ReversiXT_Client.Constants;
using System;

namespace ReversiXT_Client.Algorithms
{
    public class ABPruning
    {

        //---MAP-VALUATION-CALL-SIMON--------------------------------------------------------------------------------------------------------------------
        #region MAP-VALUATION-SIMON
        public static void SetValuationForNextMove(int MaxSearchDepth, int alpha, int beta)
        {

            #region alpha-beta-pruning init
            //int alpha = int.MinValue;
            int v = int.MinValue;
            //int beta = int.MaxValue;
            //Print.WriteLine("Initial Highest: alpha: " + alpha + " v: " + v + " beta: " + beta);
            #endregion

            //for performance - create map for each possible move
            int mapLength = Map.MapFields.Length;
            char[][,] mapArray = new char[PossibleMoves.PossibleMoveArray.Length][,];

            for (int sortingIndex = 0; sortingIndex < PossibleMoves.PossibleMoveArray.Length; sortingIndex++)
            {
                mapArray[sortingIndex] = new char[Rules.MapHeigth, Rules.MapWidth];
                System.Array.Copy(Map.MapFields, mapArray[sortingIndex], mapLength);

                //mapArray[sortingIndex] = ArrayMethods<char>.CloneArray(Map.MapFields);
                mapArray[sortingIndex] = Map.SetStoneInTempMapWithoutSpecialMoves(Rules.OurPlayer, PossibleMoves.PossibleMoveArray[sortingIndex], mapArray[sortingIndex]);
                PossibleMoves.PossibleMoveArray[sortingIndex].Value = HeatMapValuation.GetMapValuation(Rules.OurPlayer, mapArray[sortingIndex]);
                PossibleMoves.PossibleMoveArray[sortingIndex].MapIndex = sortingIndex;
                #region check time
                if (ABPruningIterative.TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed)
                    {
                        Print.WriteLine("Time is running out ... committing (CreateMaps) @" + PossibleMoves.PossibleMoveArray.Length + "moves");
                        return;
                    }
                }
                #endregion
            }

            ABPruningIterative.countExpansions += PossibleMoves.PossibleMoveArray.Length;

            #region moveSort
            if (Program._moveSort)
            {
                MoveSorting(PossibleMoves.PossibleMoveArray, Rules.OurPlayer);
            }
            #endregion

            //foreach static possibleMove
            for (int moveIndex = 0; moveIndex < PossibleMoves.PossibleMoveArray.Length; moveIndex++)
            {
                //give our possibleMove a MapValue
                int MapValuationForMove = SetValuationRecursive(Rules.OurPlayer,
                    mapArray[PossibleMoves.PossibleMoveArray[moveIndex].MapIndex],
                    1, MaxSearchDepth, alpha, beta);

                #region check time
                if (ABPruningIterative.TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed)
                    {
                        Print.WriteLine("Time is running out ... committing");
                        return;
                    }
                }
                #endregion

                //save the MapValuation in the static possibleMoves
                PossibleMoves.PossibleMoveArray[moveIndex].Value = MapValuationForMove;

                #region alpha-beta-pruning- update
                if (MapValuationForMove > alpha) alpha = MapValuationForMove;
                if (MapValuationForMove > v) v = MapValuationForMove;
                //Print.WriteLine("Update Highest: alpha: " + alpha + " v: " + v + " beta: " + beta);
                #endregion
            }
        }

        private static int SetValuationRecursive(char player, char[,] newMap, int recursionDepth, int MaxSearchDepth, int alpha, int beta)
        {
            if (recursionDepth < MaxSearchDepth)
            {

                char targetPlayer = HelpMethods.NextPlayer(player);
                //while(targetPlayer == disqualified) { NextPlayer }

                #region alpha-beta-pruning
                //alpha-beta-pruning-----------------------------------------------------
                int v = int.MinValue;
                if (targetPlayer != Rules.OurPlayer) v = int.MaxValue;
                //Print.WriteLine("(" + recursionDepth + ")Init  : alpha: " + alpha + " v: " + v + " beta: " + beta);
                //-----------------------------------------------------------------------
                #endregion

                Position[] newCurPosition = Map.SetCurrentPositions(targetPlayer, newMap);
                Position[] newPossibleMoves = PossibleMoves.SetPossibleMoves(targetPlayer, newCurPosition, newMap, false);

                #region time check
                if (ABPruningIterative.TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed)
                    {
                        Print.WriteLine("Time is running out ... committing (befor CreateMaps)");
                        return int.MinValue;
                    }
                }
                #endregion

                //for performance - create map for each possible move
                int mapLength = Map.MapFields.Length;
                char[][,] mapArray = new char[newPossibleMoves.Length][,];
                for (int Index = 0; Index < newPossibleMoves.Length; Index++)
                {
                    mapArray[Index] = new char[Rules.MapHeigth, Rules.MapWidth];
                    System.Array.Copy(newMap, mapArray[Index], mapLength);

                    //mapArray[Index] = ArrayMethods<char>.CloneArray(newMap);
                    mapArray[Index] = Map.SetStoneInTempMapWithoutSpecialMoves(targetPlayer, newPossibleMoves[Index], mapArray[Index]);
                    newPossibleMoves[Index].Value = HeatMapValuation.GetMapValuation(Rules.OurPlayer, mapArray[Index]);
                    newPossibleMoves[Index].MapIndex = Index;

                    #region check time
                    if (ABPruningIterative.TimeLimit)
                    {
                        if (CustomTimer.IsTimerElapsed)
                        {
                            Print.WriteLine("Time is running out ... committing (CreateMaps Rekursion) @" + PossibleMoves.PossibleMoveArray.Length + "moves");
                            return int.MinValue;
                        }
                    }
                    #endregion
                }

                ABPruningIterative.countExpansions += newPossibleMoves.Length;

                #region check if Player haven't any moves -> skip the player
                if (newPossibleMoves.Length == 0)
                {
                    if (targetPlayer == Rules.OurPlayer)
                    {
                        return HeatMapValuation.GetMapValuation(Rules.OurPlayer, newMap);
                    }
                    //check the next Player in next depth
                    //normaly the server will skip the player
                    recursionDepth++;
                    return SetValuationRecursive(targetPlayer, newMap, recursionDepth, MaxSearchDepth, alpha, beta);
                }
                #endregion

                #region moveSort
                if (Program._moveSort)
                {
                    MoveSorting(newPossibleMoves, targetPlayer);
                }
                #endregion

                #region time check
                if (ABPruningIterative.TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed)
                    {
                        Print.WriteLine("Time is running out ... committing (MoveSort)");
                        return int.MinValue;
                    }
                }
                #endregion

                for (int indexMove = 0; indexMove < newPossibleMoves.Length; indexMove++)
                {
                    if (indexMove == 0) recursionDepth++;

                    #region alpha-beta-pruning ---CUT OFF---
                    if (targetPlayer != Rules.OurPlayer)
                    {
                        if (alpha >= v)
                        {
                            //Print.WriteLine("---CUT@MIN---");
                            ABPruningIterative.cutOffs++;
                            break;
                        }
                    }
                    else
                    {
                        if (beta <= v)
                        {
                            //Print.WriteLine("---CUT@MAX---");
                            ABPruningIterative.cutOffs++;
                            break;
                        }
                    }
                    #endregion

                    //-----RECURSION-----
                    int mapValue = SetValuationRecursive(targetPlayer,
                        mapArray[newPossibleMoves[indexMove].MapIndex],
                        recursionDepth, MaxSearchDepth, alpha, beta);
                    //Print.WriteLine("NewValue: " + mapValue );

                    #region time check
                    if (ABPruningIterative.TimeLimit)
                    {
                        if (CustomTimer.IsTimerElapsed)
                        {
                            Print.WriteLine("Time is running out ... committing");
                            return int.MinValue;
                        }
                    }
                    #endregion

                    #region alpha-beta-pruning update
                    if (targetPlayer != Rules.OurPlayer)
                    {
                        if (mapValue < v) v = mapValue;
                        if (mapValue < beta) beta = mapValue;
                    }
                    else
                    {
                        if (mapValue > v) v = mapValue;
                        if (mapValue > alpha) alpha = mapValue;
                    }
                    //Print.WriteLine("(" + recursionDepth + ")Update: alpha: " + alpha + " v: " + v + " beta: " + beta);
                    #endregion
                }

                return v; //alpha-beta-pruning
            }
            return HeatMapValuation.GetMapValuation(Rules.OurPlayer, newMap);
        }

        #endregion
        //---END-MAP-VALUATION-CALL-SIMON--------------------------------------------------------------------------------------------------------------------

        #region moveSort-Algorithm
        private static void MoveSorting(Position[] targetArray, char targetPlayer)
        {
            //MAX sorting
            if (targetPlayer == Rules.OurPlayer)
            {
                HeapSortMax(targetArray);
            }
            else //MIN sorting
            {
                HeapSortMin(targetArray);
            }
        }

        #region MaxSorting
        //Sorting lowest value first postiion
        private static void HeapSortMin(Position[] input)
        {
            //Build-Max-Heap
            int heapSize = input.Length;
            for (int p = (heapSize - 1) / 2; p >= 0; p--)
            {
                MaxHeapify(input, heapSize, p);

                #region check time
                if (ABPruningIterative.TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed)
                    {
                        Print.WriteLine("Time is running out ... committing (while Sorting)");
                        return;
                    }
                }
                #endregion
            }

            for (int i = input.Length - 1; i > 0; i--)
            {
                //Swap
                Position temp = input[i];
                input[i] = input[0];
                input[0] = temp;

                heapSize--;
                MaxHeapify(input, heapSize, 0);

                #region check time
                if (ABPruningIterative.TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed)
                    {
                        Print.WriteLine("Time is running out ... committing (while Sorting)");
                        return;
                    }
                }
                #endregion
            }
        }

        private static void MaxHeapify(Position[] input, int heapSize, int index)
        {
            int left = (index + 1) * 2 - 1;
            int right = (index + 1) * 2;
            int largest = 0;

            if (left < heapSize && input[left].Value > input[index].Value)
                largest = left;
            else
                largest = index;

            if (right < heapSize && input[right].Value > input[largest].Value)
                largest = right;

            if (largest != index)
            {
                Position temp = input[index];
                input[index] = input[largest];
                input[largest] = temp;

                MaxHeapify(input, heapSize, largest);
            }
        }
        #endregion

        //---------

        #region MaxSorting
        //Sorting biggest value first position
        public static void HeapSortMax(Position[] input)
        {
            //Build-Max-Heap
            int heapSize = input.Length;
            for (int p = (heapSize - 1) / 2; p >= 0; p--)
            {
                MinHeapify(input, heapSize, p);

                #region check time
                if (ABPruningIterative.TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed)
                    {
                        Print.WriteLine("Time is running out ... committing (while Sorting)");
                        return;
                    }
                }
                #endregion
            }

            for (int i = input.Length - 1; i > 0; i--)
            {
                //Swap
                Position temp = input[i];
                input[i] = input[0];
                input[0] = temp;

                heapSize--;
                MinHeapify(input, heapSize, 0);

                #region check time
                if (ABPruningIterative.TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed)
                    {
                        Print.WriteLine("Time is running out ... committing (while Sorting)");
                        return;
                    }
                }
                #endregion
            }
        }

        private static void MinHeapify(Position[] input, int heapSize, int index)
        {
            int left = (index + 1) * 2 - 1;
            int right = (index + 1) * 2;
            int minimal = 0;

            if (left < heapSize && input[left].Value < input[index].Value)
                minimal = left;
            else
                minimal = index;

            if (right < heapSize && input[right].Value < input[minimal].Value)
                minimal = right;

            if (minimal != index)
            {
                Position temp = input[index];
                input[index] = input[minimal];
                input[minimal] = temp;

                MaxHeapify(input, heapSize, minimal);
            }
        }
        #endregion

        #endregion
    }
}
