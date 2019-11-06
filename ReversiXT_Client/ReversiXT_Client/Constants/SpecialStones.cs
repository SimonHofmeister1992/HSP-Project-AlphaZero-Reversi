using ReversiXT_Client.Game;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ReversiXT_Client.Constants
{
    public class SpecialStones
    {
        public const char Hole = '-';
        public const char Choice = 'c';
        public const char Inversion = 'i';
        public const char Bonus = 'b';
        public const char Expansion = 'x';
        public const char FreeField = '0';
    }


    public class FieldChecks
    {
        public static bool IsFieldSpecialStone(char FieldSymbol)
        {
            switch (FieldSymbol)
            {
                case SpecialStones.Choice:
                case SpecialStones.Inversion:
                case SpecialStones.Expansion:
                case SpecialStones.Bonus:
                    return true;
            }
            return false;
        }
        public static bool IsFieldChoiceInversBonus(char FieldSymbol)
        {
            switch (FieldSymbol)
            {
                case SpecialStones.Choice:
                case SpecialStones.Inversion:
                case SpecialStones.Bonus:
                    return true;
            }
            return false;
        }

        public static bool IsFieldFreeOrSpecialStone(char FieldSymbol)
        {
            switch (FieldSymbol)
            {
                case SpecialStones.FreeField:
                case SpecialStones.Choice:
                case SpecialStones.Inversion:
                case SpecialStones.Expansion:
                case SpecialStones.Bonus:
                    return true;
            }
            return false;
        }

        public static bool IsFieldFreeOrSpecial(char FieldSymbol)
        {
            switch (FieldSymbol)
            {
                case SpecialStones.Hole:
                case SpecialStones.FreeField:
                case SpecialStones.Choice:
                case SpecialStones.Inversion:
                case SpecialStones.Expansion:
                case SpecialStones.Bonus:
                    return true;
            }
            return false;
        }

        public static bool IsFieldFreeOrSpecialStoneWithoutExpand(char FieldSymbol)
        {
            switch (FieldSymbol)
            {
                case SpecialStones.FreeField:
                case SpecialStones.Choice:
                case SpecialStones.Inversion:
                case SpecialStones.Bonus:
                    return true;
            }
            return false;
        }

        public static bool IsFieldOurStone(char FieldSymbol)
        {
            if (FieldSymbol == Rules.OurPlayer)
                return true;
            return false;
        }

        public static bool IsFieldHole(char FieldSymbol)
        {
            if (FieldSymbol == SpecialStones.Hole)
                return true;
            return false;
        }

    }
}
