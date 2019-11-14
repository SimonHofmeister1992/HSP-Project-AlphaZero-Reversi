using System;
using System.Collections.Generic;
using ReversiXT_Client.Constants;
using ReversiXT_Client.Enums;
using ReversiXT_Client.Array;
using ReversiXT_Client.Algorithms;

namespace ReversiXT_Client.Game
{
    public static class Map
    {
        //documentation
        public static int CountOurTurn { get; set; }
        public static int CountTurn { get; set; }
        public static int MaxPossibleTurns { get; set; }

        public static char[,] MapFields { get; set; }

        public static Position[] CurrentPosition;

        //Initilize
        public static void SetMap(string[] inputArray)
        {
            int countHoles = 0;
            MapFields = new char[Rules.MapHeigth, Rules.MapWidth];


            for (int i = 0; i < Rules.MapHeigth; i++)
            {
                var columns = inputArray[Rules.MapHeightWidthLine + i + 1].Split(' ');
                

                for (int j = 0; j < Rules.MapWidth; j++)
                {
                    if (columns[j].ToString().Contains("\r"))
                        columns[j] = columns[j].ToString().Replace("\r", "");

                    MapFields[i, j] = char.Parse(columns[j]);

                    if (MapFields[i, j] == SpecialStones.Hole) 
                        countHoles++;
                }
            }

            //PositionValues.SetHoleValuation(Rules.MapHeigth * Rules.MapWidth, countHoles);
            //Initialize HeatMap
            Heatmap.CreateHeatMap();
            //TestActions.WriteMap(Heatmap.heatMap);
        }

        //List of CurrentPositions from player
        public static Position[] SetCurrentPositions(char player, char[,] map)
        {
            Position[] currentPos = new Position[0];

            int indexCurrentPos = 0;
            for (int row = 0; row < Rules.MapHeigth; row++)
            {
                for (int column = 0; column < Rules.MapWidth; column++)
                {
                    if (map[row, column] == player)
                    {
                        var position = new Position()
                        {
                            SpecialAttribute = map[row, column],
                            RowPosition = row,
                            ColumnPosition = column
                        };
                        if (indexCurrentPos == currentPos.Length)
                        { currentPos = ArrayMethods<Position>.NewLength(currentPos); }

                        currentPos[indexCurrentPos] = position;
                        indexCurrentPos++;
                    }
                }
            }
            return currentPos;
        }

        //List of all Positions for OverrideStone
        public static Position[] GetAllPositionsForOverrideStones(char[,] map)
        {
            Position[] OverridePlaces = new Position[0];

            int indexCurrentPos = 0;
            for (int row = 0; row < Rules.MapHeigth; row++)
            {
                for (int column = 0; column < Rules.MapWidth; column++)
                {
                    if (!FieldChecks.IsFieldHole(map[row, column]) && 
                        !FieldChecks.IsFieldFreeOrSpecialStoneWithoutExpand(map[row, column]))
                    {
                        var position = new Position()
                        {
                            SpecialAttribute = map[row, column],
                            RowPosition = row,
                            ColumnPosition = column
                        };
                        if (indexCurrentPos == OverridePlaces.Length)
                        { OverridePlaces = Array.ArrayMethods<Position>.NewLength(OverridePlaces); }

                        OverridePlaces[indexCurrentPos] = position;
                        indexCurrentPos++;
                    }
                }
            }
            return OverridePlaces;
        }

        //Check if index out of Map
        public static bool IndexValidation(int row, int column)
        {
            if ((row >= Rules.MapHeigth) || (column >= Rules.MapWidth)
                || (row < 0) || (column < 0))
                return false;
            return true;
        }

        //---Methods------------------------------------------------------------

        public static bool SetStone(char player, Position stonePosition)
        {
            //ValidTurnCheck if the server sends us invalid turn (at the moment we don't know)
            //here Check

            //fieldSymbol temp save
            char fieldSymbol = MapFields[stonePosition.RowPosition, stonePosition.ColumnPosition];

            //change all stones
            MakeAllChanges(player, stonePosition, MapFields);

            //set last stone
            MapFields[stonePosition.RowPosition, stonePosition.ColumnPosition] = player;


            return true;
        }

        public static char[,] SetStoneInTempMapWithoutSpecialMoves(char player, Position stonePosition, char[,] mapChange)
        {
            char fieldSymbol = mapChange[stonePosition.RowPosition, stonePosition.ColumnPosition];

            MakeAllChanges(player, stonePosition, mapChange);

            mapChange[stonePosition.RowPosition, stonePosition.ColumnPosition] = player;

            //Special move handling for temp maps.. in heuristic
            if(fieldSymbol == SpecialStones.Inversion)
            {
                for (int i = 0; i < mapChange.GetLength(0); i++)
                {
                    for (int j = 0; j < mapChange.GetLength(1); j++)
                    {
                        int tmpPlayer = -1;
                        if (int.TryParse(mapChange[i, j].ToString(), out tmpPlayer) && mapChange[i, j] != SpecialStones.FreeField)
                        {
                            tmpPlayer = (tmpPlayer % Rules.CountPlayers) + 1;
                            mapChange[i, j] = char.Parse(tmpPlayer.ToString());
                        }
                    }
                }
            }
            return mapChange;
        }


        #region methods for SetStone() (ValidTurnCheck, ChangeStones)

        private static void MakeAllChanges(char player, Position stonePosition, char[,] map)
        {
            //this array saves all stone that will be changed
            Position[] stoneChanges = new Position[0];
            //generates the array
            foreach (Directions directions in Enum.GetValues(typeof(Directions)))
            {
                stonePosition.Direction = directions;
                if (ValidCheckInOneDirection(player, stonePosition, map))
                {
                    stoneChanges = GetAllStonesInOneDirection(player, stonePosition, stoneChanges, map);
                }
            }

            //here all stones will be changed
            for(int index = 0; index < stoneChanges.Length; index++)
            {
                map[stoneChanges[index].RowPosition, stoneChanges[index].ColumnPosition] = player;
            }
        }

        //---VALID--CHECKS-------------
        public static bool ValidTurnCheck(char player, Position stonePosition, char[,] map)
        {
            Position newCurPosition = stonePosition;
            newCurPosition = PossibleMoves.TransitionCheckAtPositionOrIncreasePosition(newCurPosition);

            //check if index is in the map-range
            if (!IndexValidation(newCurPosition.RowPosition, newCurPosition.ColumnPosition))
            { return false; }
            //if index is in map-range -> it copys the fieldsymbol in position
            newCurPosition.SpecialAttribute = map[newCurPosition.RowPosition, newCurPosition.ColumnPosition];

            //FirstStones
            if (FieldChecks.IsFieldFreeOrSpecialStoneWithoutExpand(newCurPosition.SpecialAttribute)
                || FieldChecks.IsFieldHole(newCurPosition.SpecialAttribute)
                || newCurPosition.SpecialAttribute == Rules.OurPlayer)
            {
                return false;
            }

            if (newCurPosition.ColumnPosition == stonePosition.ColumnPosition &&
                newCurPosition.RowPosition == stonePosition.RowPosition)
            {
                return false;
            }

            return ValidCheckInOneDirection(Rules.OurPlayer, newCurPosition, map);
        }
        //next from Position
        //X [1] [1] [2] -> [3] [4] ...Valid for player 4
        public static bool ValidCheckInOneDirection(char player, Position position, char[,] map)
        {
            Position newCurPosition = position;

            while (true)
            {
                newCurPosition = PossibleMoves.TransitionCheckAtPositionOrIncreasePosition(newCurPosition);

                //check if index is in the map-range
                if (!IndexValidation(newCurPosition.RowPosition, newCurPosition.ColumnPosition))
                { return false; }
                //if index is in map-range -> it copys the fieldsymbol in position
                newCurPosition.SpecialAttribute = map[newCurPosition.RowPosition, newCurPosition.ColumnPosition];

                if (newCurPosition.ColumnPosition == position.ColumnPosition
                    && newCurPosition.RowPosition == position.RowPosition)
                {
                    return false;
                }

                if (FieldChecks.IsFieldFreeOrSpecialStoneWithoutExpand(newCurPosition.SpecialAttribute)
                    || FieldChecks.IsFieldHole(newCurPosition.SpecialAttribute))
                {
                    return false;
                }

                if (newCurPosition.SpecialAttribute == player)
                {
                    return true;
                }
            }
        }
        //---END---VALID--CHECKS-------

        //---GET--STONE--CHANGE--Array------------
        private static Position[] GetAllStonesInOneDirection(char player, Position stonePosition, Position[] stoneChangeArray, char[,] map)
        {
            Position[] newChangeArray = stoneChangeArray;
            Position newCurPosition = stonePosition;

            while (true)
            {
                newCurPosition = PossibleMoves.TransitionCheckAtPositionOrIncreasePosition(newCurPosition);

                //check if index is in the map-range
                if (!IndexValidation(newCurPosition.RowPosition, newCurPosition.ColumnPosition))
                { return newChangeArray; }

                //if index is in map-range -> it copys the fieldsymbol in position
                newCurPosition.SpecialAttribute = map[newCurPosition.RowPosition, newCurPosition.ColumnPosition];

                if (newCurPosition.SpecialAttribute == player)
                    return newChangeArray;

                newChangeArray = InsertNextStoneInArray(newCurPosition, newChangeArray);
            }
        }

        private static Position[] InsertNextStoneInArray(Position position, Position[] stoneChanges)
        {
            Position[] newChangeArray = stoneChanges;
            int insertIndex = stoneChanges.Length;

            //Check if stone is already in array
            for (int moveIndex = 0; moveIndex < insertIndex; moveIndex++)
            {
                if (stoneChanges[moveIndex].RowPosition == position.RowPosition)
                {
                    if (stoneChanges[moveIndex].ColumnPosition == position.ColumnPosition)
                    {
                        return stoneChanges;
                    }
                }
            }

            //increase Array -> insert new Position
            newChangeArray = Array.ArrayMethods<Position>.NewLength(newChangeArray);
            newChangeArray[insertIndex] = position;
            return newChangeArray;
        }
        //---END---GET--STONE--CHANGE--ARRAY------
        #endregion


    }
}
