using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ReversiXT_Client.Game;
using ReversiXT_Client.Constants;

namespace ReversiXT_Client.Algorithms
{
    class HeatMapValuation
    {
        public static int GetMapValuation(char player, char[,] map)
        {
            if (Rules.CountPlayers == 2)
            {
                int mapValue = 0;
                for (int row = 0; row < Rules.MapHeigth; row++)
                {
                    for (int column = 0; column < Rules.MapWidth; column++)
                    {
                        if (FieldChecks.IsFieldOurStone(map[row, column]))
                        {
                            mapValue = mapValue + Heatmap.heatMap[row, column];
                        }
                        else if (!FieldChecks.IsFieldFreeOrSpecial(map[row, column]))
                        {
                            mapValue = mapValue - Heatmap.heatMap[row, column];
                        }
                    }
                }
                return mapValue;
            }
            else
            {
                int mapValue = 0;
                for (int row = 0; row < Rules.MapHeigth; row++)
                {
                    for (int column = 0; column < Rules.MapWidth; column++)
                    {
                        if (FieldChecks.IsFieldOurStone(map[row, column]))
                        {
                            mapValue = mapValue + Heatmap.heatMap[row, column];
                        }
                        else if (Heatmap.heatMap[row, column] > PositionValues.LowestValue)
                        {
                            mapValue = mapValue - PositionValues.DecreaseValueEnemyStone;
                        }
                    }
                }
                return mapValue;
            }
        }
    }
}
