using ReversiXT_Client.Game;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ReversiXT_Client.Constants;
using ReversiXT_Client.Enums;


namespace ReversiXT_Client.Algorithms
{
    public class MoveValuation
    {
        
        
        public static Position GetBestMove()
        {
            Position pos = new Position();

            if (PossibleMoves.PossibleMoveArray.Length != 0)
            {
                // -1 to ensure that at least one position will be selected
                pos = PossibleMoves.PossibleMoveArray[0];

                foreach (var position in PossibleMoves.PossibleMoveArray)
                {
                    //Problem: there can be position which have the same value - what then?!
                    if (position.Value > pos.Value)
                    {
                        pos = position;
                    }
                }
            }
            return pos;
        }


        // for server-connection
        internal static byte ChooseBombOrOverrideStone()
        {
            // we have to choose --> valuation
            // we chose overwriteStone --> so good at the end

            //OverwriteStone-----------------
            Rules.CountOverrideStones++;
            return 21;
            //-------------------------------


            //Extra Bomb--------------------
            //return 20;
            //------------------------------
        }
    }
}
