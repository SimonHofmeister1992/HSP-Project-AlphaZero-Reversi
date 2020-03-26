using System;
using System.Collections.Generic;
using ReversiXT_Client.Constants;
using ReversiXT_Client.Enums;
using ReversiXT_Client.Game;
using System.Linq;

namespace ReversiXT_Client.Algorithms
{
    public class PossibleMoves
    {
        #region PossiblePositions

        public static Position[] PossibleMoveArray;

        public static Position[] SetPossibleMoves(char player, Position[] currentPosition, char[,] map, bool withOverrideStones)
        {
            Position[] PossibleMoveArray = new Position[0];
            int insertIndex = 0;
            //for EACH of our stones
            foreach (Position Position in currentPosition)
            {
                //in EACH direction
                foreach (Directions direction in Enum.GetValues(typeof(Directions)))
                {
                    Position newCurPosition = Position;
                    newCurPosition.Direction = direction;
                    newCurPosition = TransitionCheckAtPositionOrIncreasePosition(newCurPosition);

                    //check if index is in the map-range
                    if (!Map.IndexValidation(newCurPosition.RowPosition, newCurPosition.ColumnPosition))
                    { continue; }
                    //if index is in map-range -> it copys the fieldsymbol in position
                    newCurPosition.SpecialAttribute = map[newCurPosition.RowPosition, newCurPosition.ColumnPosition];

                    //FirstStones
                    if (FieldChecks.IsFieldFreeOrSpecialStoneWithoutExpand(newCurPosition.SpecialAttribute)
                        || FieldChecks.IsFieldHole(newCurPosition.SpecialAttribute)
                        || newCurPosition.SpecialAttribute == player)
                    {
                        continue;
                    }

                    PossibleMoveArray = AddFirstEmptyFieldToArray(player, newCurPosition, map, PossibleMoveArray, ref insertIndex);

                }//foreach(directions)
            }//foreach(positions);

            //ToDo: Algorithm for OverrideStones "When will it aktivated"
            if (insertIndex == 0 && Rules.CountOverrideStones > 0 && withOverrideStones)
            {
                //we must take an OverrideStone!
                Rules.CountOverrideStones--;

                Position[] PossibleStones = Map.GetAllPositionsForOverrideStones(map);

                foreach (Position Position in PossibleStones)
                {
                    //'X' Stones can always overwritten
                    if(Position.SpecialAttribute == SpecialStones.Expansion)
                    {
                        PossibleMoveArray = InsertNextPossibleMove(Position, PossibleMoveArray, ref insertIndex);
                        continue;
                    }

                    //in EACH direction
                    foreach (Directions direction in Enum.GetValues(typeof(Directions)))
                    {
                        Position newCurPosition = Position;
                        newCurPosition.Direction = direction;

                        if (Map.ValidTurnCheck(player, newCurPosition, map))
                            PossibleMoveArray = InsertNextPossibleMove(newCurPosition, PossibleMoveArray, ref insertIndex);

                    }//foreach(directions)
                }//foreach(positions);
            }
            return PossibleMoveArray;
        }
        
        //List of all PossibleMoves for Bombs
        public static Position[] SetPossibleMovesForBombs(char[,] map)
        {
            Position[] BombPlaces = new Position[0];

            int indexCurrentPos = 0;
            for (int row = 0; row < Rules.MapHeigth; row++)
            {
                for (int column = 0; column < Rules.MapWidth; column++)
                {
                    if (!FieldChecks.IsFieldHole(map[row, column]))
                    {
                        var position = new Position()
                        {
                            SpecialAttribute = map[row, column],
                            RowPosition = row,
                            ColumnPosition = column
                        };
                        if (indexCurrentPos == BombPlaces.Length)
                        { BombPlaces = Array.ArrayMethods<Position>.NewLength(BombPlaces); }

                        BombPlaces[indexCurrentPos] = position;
                        indexCurrentPos++;
                    }
                }
            }
            return BombPlaces;
        }

        /// <summary>
        ///Transition -> return: transition-end-position
        ///No Transition -> return: updated position in direction
        ///!!!!NOTE: no special attribute changes!!!! 
        /// </summary>
        /// <param name="position">this position will checked - including position.direction</param>
        /// <returns>updated position</returns>
        public static Position TransitionCheckAtPositionOrIncreasePosition(Position position)
        {
            Position newPosition = position;
            if (Transition.IsTransitionAtCurrentPosition(position.RowPosition, position.ColumnPosition, position.Direction))
            {
                newPosition = Transition.GetEndTransitionPosition(position.RowPosition, position.ColumnPosition, position.Direction);
                newPosition.Direction = Direction.GetInvertedDirection(newPosition.Direction);
            }
            else
            {
                newPosition.RowPosition += Direction.GetAddRow(newPosition.Direction);
                newPosition.ColumnPosition += Direction.GetAddColumn(newPosition.Direction);
            }
            return newPosition;
        }

        private static Position[] AddFirstEmptyFieldToArray(char player, Position position, char[,] map, Position[] curPosMoves, ref int insertIndex)
        {
            Position newCurPosition = position;
            
            while (true)
            {
                newCurPosition = TransitionCheckAtPositionOrIncreasePosition(newCurPosition);

                //check if index is in the map-range
                if (!Map.IndexValidation(newCurPosition.RowPosition, newCurPosition.ColumnPosition))
                { return curPosMoves; }
                //if index is in map-range -> it copys the fieldsymbol in position
                newCurPosition.SpecialAttribute = map[newCurPosition.RowPosition, newCurPosition.ColumnPosition];

                if (FieldChecks.IsFieldFreeOrSpecialStoneWithoutExpand(newCurPosition.SpecialAttribute))
                {
                    curPosMoves = InsertNextPossibleMove(newCurPosition, curPosMoves, ref insertIndex);
                    return curPosMoves;
                }

                if (FieldChecks.IsFieldHole(newCurPosition.SpecialAttribute)
                    || newCurPosition.SpecialAttribute == player)
                {
                    return curPosMoves;
                }
            }
        }

        private static Position[] InsertNextPossibleMove(Position position, Position[] curPosMoves, ref int insertIndex)
        {
            Position[] tmpArray = curPosMoves;
            if (!IsNewPositionInList(position, tmpArray))
            {
                if (insertIndex <= tmpArray.Length)
                    tmpArray = Array.ArrayMethods<Position>.NewLength(tmpArray);
                tmpArray[insertIndex] = position;
                insertIndex++;
                return tmpArray;
            }
            return tmpArray;
        }

        private static bool IsNewPositionInList(Position move, Position[] curPosMoves)
        {
            for (int moveIndex = 0; moveIndex < curPosMoves.Length; moveIndex++)
            {
                if (curPosMoves[moveIndex].RowPosition == move.RowPosition)
                {
                    if (curPosMoves[moveIndex].ColumnPosition == move.ColumnPosition)
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        #endregion

    }

}

