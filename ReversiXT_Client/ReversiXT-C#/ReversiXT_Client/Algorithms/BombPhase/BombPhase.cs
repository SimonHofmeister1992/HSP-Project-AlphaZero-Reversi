using ReversiXT_Client.Constants;
using ReversiXT_Client.Game;
using System;
using ReversiXT_Client.Enums;
using ReversiXT_Client.Array;

namespace ReversiXT_Client.Algorithms
{
    public class BombPhase
    {
        //initialize BombPhase---------------------

        /// <summary>
        /// Call for incoming Turn
        /// </summary>
        /// <param name="position">Position for bomb</param>
        #region SetBomb
        public static void SetBomb(Position position)
        {
            Map.MapFields = SetBombTemp(Map.MapFields, position);
        }
        #endregion

        //Call for next move in BombPhase
        #region BombPhase
        public static bool TimeLimit = true;
        public static int timeLimitMs;
        public static void SetValuationForNextMove(int timeLimitMS, int MaxSearchDepth)
        {
            if (timeLimitMS == 0)
            { TimeLimit = false; }


            DateTime startTimeHeuristic = DateTime.Now;

            //Static Arrays initializing
            PossibleMoves.PossibleMoveArray = PossibleMoves.SetPossibleMovesForBombs(Map.MapFields);

            //performance
            char[,] newMapWithBomb = new char[Rules.MapHeigth, Rules.MapWidth];
            for (int index = 0; index < PossibleMoves.PossibleMoveArray.Length; index++)
            {
                System.Array.Copy(Map.MapFields, newMapWithBomb, Map.MapFields.Length);
                newMapWithBomb = SetBombTemp(newMapWithBomb, PossibleMoves.PossibleMoveArray[index]);

                //PossibleMoves.PossibleMoveArray[index].Value = HeatMapValuation.GetMapValuation(Rules.OurPlayer, newMapWithBomb);
                PossibleMoves.PossibleMoveArray[index].Value = GetMapValuationForBombPhase(Rules.OurPlayer, newMapWithBomb);
   
            
                //checks Time
                DateTime end = DateTime.Now;
                TimeSpan totalTime = end - startTimeHeuristic;
                if (TimeLimit)
                {
                    if (!((totalTime.TotalMilliseconds + PositionValues.TimeSpace) < timeLimitMS)) break;
                }
            }

            //----DEBUG----
            DateTime complete = DateTime.Now;
            TimeSpan finish = complete - startTimeHeuristic;
            Print.WriteLine("Bomb valuation finished: " + finish.TotalMilliseconds);
            //Console.ReadLine();

            //foreach(Position target in PossibleMoves.PossibleMoveArray)
            //{
            //    Print.WriteLine("Position (X:" + target.ColumnPosition + ", Y:" + target.ColumnPosition + ") Value: " + target.Value);
            //}
        }
        #endregion

        //Help-methods------------------------------------

        #region SetBombTemp (using recursion)
        //needed for postition to set holes
        private static Position[] PositionsToDelete;
        private static int InsertIndexToDelete;
        /// <summary>
        /// Set a bomb at their position in the tmpMap
        /// </summary>
        /// <param name="tmpMap">This map will be changed and returned</param>
        /// <param name="position">Bomb position</param>
        private static char[,] SetBombTemp(char[,] tmpMap, Position position)
        {
            if(Rules.BombStrength == 0)
            {
                tmpMap[position.RowPosition, position.ColumnPosition] = SpecialStones.Hole;
                return tmpMap;
            }

            PositionsToDelete = new Position[1];
            InsertIndexToDelete = 0;
            PositionsToDelete[InsertIndexToDelete] = position;
            InsertIndexToDelete++;

            SetBombRecursive(position, 1);
            
            foreach(Position target in PositionsToDelete)
            {
                tmpMap[target.RowPosition, target.ColumnPosition] = SpecialStones.Hole;
            }

            return tmpMap;
        }

        /// <summary>
        /// This method should not be called - used in SetBombTemp
        /// </summary>
        private static void SetBombRecursive(Position position, int recursion)
        {
            if (recursion <= Rules.BombStrength)
            {
                recursion++;
                foreach (Directions direction in Enum.GetValues(typeof(Directions)))
                {
                    Position curPosition = position;
                    curPosition.Direction = direction;
                    curPosition = PossibleMoves.TransitionCheckAtPositionOrIncreasePosition(curPosition);

                    if (!Map.IndexValidation(curPosition.RowPosition, curPosition.ColumnPosition))
                    { continue; }

                    if (FieldChecks.IsFieldHole(Map.MapFields[curPosition.RowPosition, curPosition.ColumnPosition]))
                    { continue; }

                    //setze bombe hier
                    SetBombRecursive(curPosition, recursion);

                    PositionsToDelete = ArrayMethods<Position>.NewLength(PositionsToDelete);
                    PositionsToDelete[InsertIndexToDelete] = curPosition;
                    InsertIndexToDelete++;
                }
            }
            return;
        }
        #endregion

        //Valuation-method--------------------------------
        private static int GetMapValuationForBombPhase(char player, char[,] map)
        {
            int mapValue = 0;
            foreach (char target in map)
            {
                if (!FieldChecks.IsFieldFreeOrSpecial(target))
                {
                    if (target == player)
                    {
                        mapValue = mapValue + PositionValues.OurStonesValue;
                    }
                    else if(!Connection.ServerComponent.DisqualifiedPlayers.Contains(target))
                    {
                        mapValue = mapValue - PositionValues.BombPhaseStone;
                    }
                }
            }
            return mapValue;
        }
    }
}
