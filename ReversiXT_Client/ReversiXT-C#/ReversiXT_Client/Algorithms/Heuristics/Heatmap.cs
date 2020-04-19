using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ReversiXT_Client.Game;
using ReversiXT_Client.Enums;
using ReversiXT_Client.Constants;

namespace ReversiXT_Client.Algorithms
{
    public class Heatmap
    {
        public static int[,] heatMap;

        public static void ResetPositionForSpecialStones(Position pos)
        {
            switch (pos.SpecialAttribute)
            {
                case SpecialStones.Choice:
                    heatMap[pos.RowPosition, pos.ColumnPosition] -= PositionValues.ChoiceStone;
                    return;
                case SpecialStones.Inversion:
                    heatMap[pos.RowPosition, pos.ColumnPosition] -= PositionValues.InversionStone;
                    return;
                case SpecialStones.Bonus:
                    heatMap[pos.RowPosition, pos.ColumnPosition] -= PositionValues.BonusStone;
                    return;
                default:
                    return;
            }
        }

        public static void CreateHeatMap()
        {
            //don't change the order------
            InitHeatMap();
            SetEdgeCornerValuation();
            //----------------------------
            SetSpecialStoneValuationAndDefault();
        }

        public static void SetSpecialStoneValuationAndDefault()
        {
            for (int row = 0; row < Rules.MapHeigth; row++)
            {
                for (int column = 0; column < Rules.MapWidth; column++)
                {
                    if (heatMap[row, column] > 4*PositionValues.CountHolesAtSide)
                    {
                        heatMap[row, column] = PositionValues.CornerStone;
                    }
                    else if(heatMap[row, column] == 3*PositionValues.CountHolesAtSide)
                    {
                        heatMap[row, column] = PositionValues.EdgeStone;
                    }

                    if (FieldChecks.IsFieldChoiceInversBonus(Map.MapFields[row, column]))
                    {
                        heatMap[row, column] += PositionValues.GetValueForStone(Map.MapFields[row, column]);
                    }
                    heatMap[row, column] += PositionValues.NormalStone;
                }
            }
        }

        /// <summary>
        /// Initialise all "-" Holes with position values
        /// This is needed to generate the corner/edge value
        /// </summary>
        private static void InitHeatMap()
        {
            Map.MaxPossibleTurns = 0;
            heatMap = new int[Rules.MapHeigth, Rules.MapWidth];
            //initialize
            for (int row = 0; row < Rules.MapHeigth; row++)
            {
                for (int column = 0; column < Rules.MapWidth; column++)
                {
                    //count all possible moves
                    Map.MaxPossibleTurns++;
                    heatMap[row, column] = 0;
                }
            }
        }

        public static void InitHeatMapForBombPhase()
        {
            //initialize
            for (int row = 0; row < Rules.MapHeigth; row++)
            {
                for (int column = 0; column < Rules.MapWidth; column++)
                {
                    heatMap[row, column] = PositionValues.BombPhaseStone;
                }
            }
        }

        private static void SetEdgeCornerValuation()
        {

            //set position valuation
            Position nextValuation = new Position();
            for (int row = 0; row < Rules.MapHeigth; row++)
            {
                for (int column = 0; column < Rules.MapWidth; column++)
                {
                    if (Map.MapFields[row, column] == SpecialStones.Hole) continue;
                    foreach (Directions direction in Enum.GetValues(typeof(Directions)))
                    {
                        nextValuation.RowPosition = row;
                        nextValuation.ColumnPosition = column;
                        nextValuation.Direction = direction;
                        nextValuation = PossibleMoves.TransitionCheckAtPositionOrIncreasePosition(nextValuation);
                        if (!Map.IndexValidation(nextValuation.RowPosition, nextValuation.ColumnPosition))
                        {
                            heatMap[row, column] = heatMap[row, column] + PositionValues.CountHolesAtSide;
                        }
                        else
                        {
                            if (Map.MapFields[nextValuation.RowPosition, nextValuation.ColumnPosition] == SpecialStones.Hole)
                            {
                                heatMap[row, column] = heatMap[row, column] + PositionValues.CountHolesAtSide;
                            }
                        }
                    }
                }
            }
            
        }
    }
}
