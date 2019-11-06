using ReversiXT_Client.Algorithms;
using ReversiXT_Client.Array;
using ReversiXT_Client.Enums;
using System;

namespace ReversiXT_Client.Game
{
    public struct Position
    {
        public int ColumnPosition { get; set; }
        public int RowPosition { get; set; }
        public char SpecialAttribute { get; set; }

        //Performnce - static map creating
        public int MapIndex { get; set; }

        //Direction for Transitions
        public Directions Direction { get; set; }

        //Selection criterion -> our next move?
        public int Value { get; set; }
    }
}