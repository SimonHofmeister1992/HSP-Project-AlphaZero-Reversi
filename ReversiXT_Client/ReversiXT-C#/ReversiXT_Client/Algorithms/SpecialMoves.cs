using System;
using ReversiXT_Client.Constants;
using ReversiXT_Client.Enums;
using ReversiXT_Client.Game;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ReversiXT_Client.Algorithms
{
    class SpecialMoves
    {
        #region SpecialMoveHandling
        public static byte SpecialMoveHandlingToSend(Position position)
        {
            switch (position.SpecialAttribute)
            {
                case SpecialStones.Bonus:
                    {

                        //we always take an override stone xD!
                        return MoveValuation.ChooseBombOrOverrideStone();
                    }

                case SpecialStones.Choice:
                    {
                        DateTime start = DateTime.Now;
                        
                        //create nextMap
                        Position target = MoveValuation.GetBestMove();
                        //delete valuation in heatmap for choice stone
                        Heatmap.heatMap[target.RowPosition, target.ColumnPosition] -= PositionValues.ChoiceStone;

                        char[,] tmpMap = Array.ArrayMethods<char>.CloneArray(Map.MapFields);
                        tmpMap = Map.SetStoneInTempMapWithoutSpecialMoves(Rules.OurPlayer, target, tmpMap);

                        int stonesFromPlayer = 0;
                        char chosenPlayer = Rules.OurPlayer;

                        char targetPlayer = Rules.OurPlayer;
                        for(int count = 0; count < Rules.CountPlayers; count++)
                        {
                            int mapValuation = HeatMapValuation.GetMapValuation(targetPlayer, tmpMap);
                            if(stonesFromPlayer < mapValuation)
                            {
                                chosenPlayer = targetPlayer;
                                stonesFromPlayer = mapValuation;
                            }
                            targetPlayer = HelpMethods.NextPlayer(targetPlayer);
                        }

                        DateTime end = DateTime.Now;
                        TimeSpan total = end - start;
                        Console.WriteLine("CHOICE STONE VALUATION TIME: " + total.TotalMilliseconds);

                        //increase stone value in heatmap (that no conflict in SpecialMovesIncoming)
                        Heatmap.heatMap[target.RowPosition, target.ColumnPosition] += PositionValues.ChoiceStone;

                        //we always take our stones!
                        var curPlayer = byte.Parse(chosenPlayer.ToString());
                        return curPlayer;
                    }

                default:
                    {
                        return 0;
                    }
            }
        }

        // 9 players are not allowed, so you MUST choose a player for example "Choice"
        public static void SpecialMoveHandlingIncoming(Position position, char currentPlayer, char chosenPlayer = '9')
        {
            // TODO: implement
            switch (position.SpecialAttribute)
            {
                case SpecialStones.Bonus:
                    {
                        //reset valuation
                        Heatmap.ResetPositionForSpecialStones(position);
                        return;
                    }

                case SpecialStones.Choice:
                    {
                        //change colors
                        for (int i = 0; i < Map.MapFields.GetLength(0); i++)
                        {
                            for (int j = 0; j < Map.MapFields.GetLength(1); j++)
                            {
                                if (Map.MapFields[i, j] == currentPlayer)
                                {
                                    Map.MapFields[i, j] = chosenPlayer;
                                }
                                else if (Map.MapFields[i, j] == chosenPlayer)
                                {
                                    Map.MapFields[i, j] = currentPlayer;
                                }
                            }
                        }
                        //reset valuation
                        Heatmap.ResetPositionForSpecialStones(position);
                        return;
                    }
                case SpecialStones.Inversion:
                    {
                        //change colors
                        for (int i = 0; i < Map.MapFields.GetLength(0); i++)
                        {
                            for (int j = 0; j < Map.MapFields.GetLength(1); j++)
                            {
                                int player = -1;
                                if (int.TryParse(Map.MapFields[i, j].ToString(), out player) && Map.MapFields[i, j] != SpecialStones.FreeField)
                                {
                                    player = (player % Rules.CountPlayers) + 1;
                                    Map.MapFields[i, j] = char.Parse(player.ToString());
                                }
                            }
                        }
                        //reset valuation
                        Heatmap.ResetPositionForSpecialStones(position);
                        return;
                    }
                default:
                    {
                        return;
                    }
            }
        }

        #endregion
    }
}
