using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ReversiXT_Client.Enums
{
    public enum Directions : byte
    {
        Up = 0,
        UpRight,
        Right,
        DownRight,
        Down,
        DownLeft,
        Left,
        UpLeft
    }

    public class Direction
    {

        //Get right variables for direction
        public static int GetAddRow(Directions direction)
        {
            switch (direction)
            {
                case Directions.Up:
                case Directions.UpRight:
                case Directions.UpLeft:
                    return -1;
                case Directions.Down:
                case Directions.DownRight:
                case Directions.DownLeft:
                    return 1;
                case Directions.Right:
                case Directions.Left:
                    return 0;
            }
            return 0;
        }
        public static int GetAddColumn(Directions direction)
        {
            switch (direction)
            {
                case Directions.DownLeft:
                case Directions.Left:
                case Directions.UpLeft:
                    return -1;
                case Directions.UpRight:
                case Directions.Right:
                case Directions.DownRight:
                    return 1;
                case Directions.Up:
                case Directions.Down:
                    return 0;
            }
            return 0;
        }

        //Increase or decrease variable
        public static int UpdateVariable(int addRowOrAddColumn)
        {
            if (addRowOrAddColumn > 0) addRowOrAddColumn++;
            else if (addRowOrAddColumn < 0) addRowOrAddColumn--;
            return addRowOrAddColumn;
        }

        public static Directions GetInvertedDirection(Directions direction)
        {
            switch(direction)
            {
                case Directions.Up:
                    return Directions.Down;
                case Directions.UpRight:
                    return Directions.DownLeft;
                case Directions.Right:
                    return Directions.Left;
                case Directions.DownRight:
                    return Directions.UpLeft;
                case Directions.Down:
                    return Directions.Up;
                case Directions.DownLeft:
                    return Directions.UpRight;
                case Directions.Left:
                    return Directions.Right;
                case Directions.UpLeft:
                    return Directions.DownRight;

                default:
                    return Directions.Up;
            }
        }

        public Directions NextDirection(Directions direction)
        {
            if (direction == Directions.UpLeft)
            {
                return Directions.Up;
            }
            else
            {
                return direction++;
            }
        }
    }
}
