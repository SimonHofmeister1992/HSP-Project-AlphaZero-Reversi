using ReversiXT_Client.Enums;
using System.Linq;

namespace ReversiXT_Client.Game
{
    public class Transition
    {
        public int StartColumn { get; set; }
        public int StartRow { get; set; }
        public Directions StartDirection { get; set; }

        public int EndColumn { get; set; }
        public int EndRow { get; set; }
        public Directions EndDirection { get; set; }


        public static Position GetEndTransitionPosition(int startRow, int startColumn, Directions direction)
        {
            Position position = new Position();
            var startTransitions = Rules.Transitions.Where(i => (i.StartColumn == startColumn && i.StartRow == startRow && i.StartDirection == direction));

            if (startTransitions.Count(i => (i.StartColumn == startColumn && i.StartRow == startRow && i.StartDirection == direction)) > 0)
            {
                var startTransition = Rules.Transitions.Where(i => (i.StartColumn == startColumn && i.StartRow == startRow && i.StartDirection == direction)).First();

                position.ColumnPosition = startTransition.EndColumn;
                position.RowPosition = startTransition.EndRow;
                position.Direction = startTransition.EndDirection;
                position.SpecialAttribute = Map.MapFields[startTransition.EndRow, startTransition.EndColumn]; // Position where Transition ends
            }
            else
            {
                var endTransitions = Rules.Transitions.Where(i => (i.EndColumn == startColumn && i.EndRow == startRow && i.EndDirection == direction));

                if (endTransitions.Count(i => (i.EndColumn == startColumn && i.EndRow == startRow && i.EndDirection == direction)) > 0)
                {
                    var endTransition = Rules.Transitions.Where(i => (i.EndColumn == startColumn && i.EndRow == startRow && i.EndDirection == direction)).First();

                    position.ColumnPosition = endTransition.StartColumn;
                    position.RowPosition = endTransition.StartRow;
                    position.Direction = endTransition.StartDirection;
                    position.SpecialAttribute = Map.MapFields[endTransition.StartRow, endTransition.StartColumn]; // Position where Transition ends - inverted
                }
            }

            return position;
        }

        public static bool IsTransitionAtCurrentPosition(int row, int column)
        {
            if (Rules.Transitions.Any(i => i.StartColumn == column && i.StartRow == row))
            {
                return true;
            }
            else if (Rules.Transitions.Any(i => i.EndColumn == column && i.EndRow == row))
            {
                return true;
            }


            return false;
        }

        public static bool IsTransitionAtCurrentPosition(int row, int column, Directions direction)
        {
            if (Rules.Transitions.Any(i => i.StartColumn == column && i.StartRow == row && i.StartDirection == direction))
            {
                return true;
            }
            else if (Rules.Transitions.Any(i => i.EndColumn == column && i.EndRow == row && i.EndDirection == direction))
            {
                return true;
            }

            return false;
        }

    }
}