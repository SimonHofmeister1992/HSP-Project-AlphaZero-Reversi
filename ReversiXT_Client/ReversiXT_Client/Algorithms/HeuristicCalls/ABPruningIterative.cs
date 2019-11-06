using ReversiXT_Client.Game;
using ReversiXT_Client.Array;
using ReversiXT_Client.Constants;
using System;

namespace ReversiXT_Client.Algorithms
{
    public class ABPruningIterative
    {
        public static DateTime startTimeHeuristic;
        public static bool TimeLimit = true;
        public static double timeLimitMs;
        public static int countExpansions;
        public static int cutOffs;
        public static bool IsHeatmapChanged = false;

        //Statistik - wie viel expandiert 
        //if timeLimit is given -> ignore search depth
        //---MAP-VALUATION-CALL-SIMON--------------------------------------------------------------------------------------------------------------------
        #region MAP-VALUATION-SIMON
        public static void SetValuationForNextMoveWithAspirationWindow(int timeLimitMS, int MaxSearchDepth)
        {
            Print.WriteLine("------With Aspiration Window---------");

            //TestActions.WriteMap(Heatmap.heatMap);
            //Print.WriteLine("OverriteStones: " + Rules.CountOverrideStones);
            if (timeLimitMS == 0)
            { TimeLimit = false; }
            else { timeLimitMs = (double)timeLimitMS; MaxSearchDepth = PositionValues.MaxDepth; }


            startTimeHeuristic = DateTime.Now;

            //Static Arrays initializing
            Map.CurrentPosition = Map.SetCurrentPositions(Rules.OurPlayer, Map.MapFields);
            PossibleMoves.PossibleMoveArray = PossibleMoves.SetPossibleMoves(Rules.OurPlayer, Map.CurrentPosition, Map.MapFields, true);
            countExpansions = 1;
            cutOffs = 0;
            int expansionsHigher = 1; //only the new expansions from higher level

            //for aspiration window
            int alpha = int.MinValue;
            int beta = int.MaxValue;

            for (int nextDepth = 1; nextDepth <= MaxSearchDepth; nextDepth++)
            {
                int cutOffsBefore = cutOffs;
                int expansionsBefore = countExpansions;

                ABPruning.SetValuationForNextMove(nextDepth, alpha, beta);

                #region TimeCheck
                //generates the time for all new Expansions from the next depth
                int allNewExpansion = countExpansions - expansionsBefore;
                DateTime end = DateTime.Now;
                TimeSpan totalTime = end - startTimeHeuristic;
                double timeForOneExpansion = (totalTime.TotalMilliseconds / countExpansions);
                double timeForNextDepth = allNewExpansion * (allNewExpansion / expansionsHigher) * timeForOneExpansion;

                Print.WriteLine("D(" + nextDepth + ") ms:" + totalTime.TotalMilliseconds + "; Forecast next:" + (totalTime.TotalMilliseconds + timeForNextDepth));

                if (TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed) break;
                    if (!((totalTime.TotalMilliseconds + timeForNextDepth + PositionValues.TimeSpace) < timeLimitMS)) break;
                }

                expansionsHigher = allNewExpansion;
                if (expansionsHigher == 0) expansionsHigher++;
                #endregion

                
                int newPruning = MoveValuation.GetBestMove().Value;
                if (newPruning == 0)
                {
                    Print.WriteLine("-------ASPIRATION WINDOW TO NARROW------- @" + nextDepth);
                    alpha = int.MinValue;
                    beta = int.MaxValue;
                    nextDepth--;
                }
                else
                {
                    alpha = newPruning - PositionValues.AspirationWindow;
                    beta = newPruning + PositionValues.AspirationWindow;
                }
            }

            //----DEBUG----
            Print.WriteLine("Expansions: " + countExpansions);
            Print.WriteLine("CutOffs: " + cutOffs);
            //Console.ReadLine();

            //-----DEBUG----
            //TestActions.WritePossibleMoves();
            //Console.ReadLine();
            //Position target = MoveValuation.GetBestMove();
            //Print.WriteLine("BestMove(" + target.ColumnPosition + "," + target.RowPosition + "): " + target.Value);

            //SetValuationTestMethod(timeLimitMS, MaxSearchDepth);

        }


        public static void SetValuationForNextMove(int timeLimitMS, int MaxSearchDepth)
        {
            //TestActions.WriteMap(Heatmap.heatMap);
            //Print.WriteLine("OverriteStones: " + Rules.CountOverrideStones);
            if (timeLimitMS == 0)
            { TimeLimit = false; }
            else { timeLimitMs = (double)timeLimitMS; MaxSearchDepth = PositionValues.MaxDepth; }


            startTimeHeuristic = DateTime.Now;

            //Change Heatmap for override stones and the last turns -> more stones better than edge/corner
            if ((!IsHeatmapChanged) && (Map.MaxPossibleTurns - Map.CountTurn < 10))
            {
                IsHeatmapChanged = true;
                Heatmap.InitHeatMapForBombPhase();
                Heatmap.SetSpecialStoneValuationAndDefault();                
            }

            //Static Arrays initializing
            Map.CurrentPosition = Map.SetCurrentPositions(Rules.OurPlayer, Map.MapFields);
            PossibleMoves.PossibleMoveArray = PossibleMoves.SetPossibleMoves(Rules.OurPlayer, Map.CurrentPosition, Map.MapFields, true);
            countExpansions = 1;
            cutOffs = 0;

            int expansionsHigher = 1; //only the new expansions from higher level
            int completeExpansionsHigher = 1; //all expansions
            int alpha = int.MinValue;
            int beta = int.MaxValue;
            for (int nextDepth = 1; nextDepth <= MaxSearchDepth; nextDepth++)
            {

                ABPruning.SetValuationForNextMove(nextDepth, alpha, beta);

                #region TimeCheck
                //generates the time for all new Expansions from the next depth
                int allNewExpansion = countExpansions - completeExpansionsHigher;
                DateTime end = DateTime.Now;
                TimeSpan totalTime = end - startTimeHeuristic;
                double timeForOneExpansion = (totalTime.TotalMilliseconds / countExpansions);
                double timeForNextDepth = allNewExpansion * (allNewExpansion / expansionsHigher) * timeForOneExpansion;

                Print.WriteLine("D(" + nextDepth + ") ms:" + totalTime.TotalMilliseconds + "; Forecast next:" + (totalTime.TotalMilliseconds + timeForNextDepth));

                if (TimeLimit)
                {
                    if (CustomTimer.IsTimerElapsed) break;
                    if (!((totalTime.TotalMilliseconds + timeForNextDepth + PositionValues.TimeSpace) < timeLimitMS)) break;
                }

                expansionsHigher = allNewExpansion;
                if (expansionsHigher == 0) expansionsHigher++;
                completeExpansionsHigher = countExpansions;
                #endregion
            }

            //----DEBUG----
            Print.WriteLine("Expansions: " + countExpansions);
            Print.WriteLine("CutOffs: " + cutOffs);
            //Console.ReadLine();

            //-----DEBUG----
            //TestActions.WritePossibleMoves();
            //Console.ReadLine();

            //Position target = MoveValuation.GetBestMove();
            //Print.WriteLine("BestMove(" + target.ColumnPosition + "," + target.RowPosition + "): " + target.Value);
        }

        #endregion
    }
}
