using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ReversiXT_Client.Game
{
    public static class TestActions
    {
        //Write-Map-Methods-------------------------------------------------------------------------------------
        #region WriteMap Methods
        public static void WriteMap()
        {
            if (Program._mapPrint)
            {
                WriteMap(Map.MapFields);
            }
        }

        public static void WriteMap(char[,] map)
        {
            for (int layout = 0; layout < Rules.MapWidth; layout++)
            {
                if (layout == 0)
                { Print.Write("    "); }
                Print.Write(layout + " ");
            }
            Print.WriteLine();

            for (int i = 0; i < Rules.MapHeigth; i++)
            {
                //Row-Number output
                if (i < 10)
                { Print.Write(i + " | "); }
                else
                { Print.Write(i + "| "); }

                //Draw Map
                for (int j = 0; j < Rules.MapWidth; j++)
                {
                    Print.Write(map[i, j] + " ");
                }

                Print.Write("\n");
            }
        }

        public static void WriteMap(int[,] map)
        {
            Print.WriteLine();

            for (int i = 0; i < Rules.MapHeigth; i++)
            {
                //Draw Map
                for (int j = 0; j < Rules.MapWidth; j++)
                {
                    if (map[i, j] < 10)
                        Print.Write("  " + map[i, j] + " ");
                    else if (map[i, j] < 100)
                        Print.Write(" " + map[i, j] + " ");
                    else Print.Write(map[i, j] + " ");
                }

                Print.WriteLine();
            }
        }
        #endregion
        //END---------------------------------------------------------------------------------------------------

        //Write-Arrays------------------------------------------------------------------------------------------
        #region Write Array Methods
        public static void WritePossibleMoves()
        {
            foreach (Position target in Algorithms.PossibleMoves.PossibleMoveArray)
            {
                Print.WriteLine("Move: (" + target.ColumnPosition + "," + target.RowPosition + ") Value: " + target.Value);
            }
        }

        public static void WriteCurrentPositions()
        {
            foreach (Position target in Map.CurrentPosition)
            {
                Print.WriteLine("Position: (" + target.ColumnPosition + "," + target.RowPosition + ")");
            }
        }
        
        public static void WriteTransitions()
        {
            foreach(var Transition in Rules.Transitions)
            {
                Print.WriteLine("Start (" + Transition.StartColumn + "," + Transition.StartRow + "," + Transition.StartDirection + ") End ("
                    + Transition.EndColumn + "," + Transition.EndRow + "," + Transition.EndDirection + ")");
            }
        }

        public static void WritePositionArray(Position[] array)
        {
            foreach(Position target in array)
            {
                Print.WriteLine("Move: (" + target.ColumnPosition + "," + target.RowPosition + ") Value: " + target.Value);
            }
        }
        #endregion
        //END---------------------------------------------------------------------------------------------------
    }
}
