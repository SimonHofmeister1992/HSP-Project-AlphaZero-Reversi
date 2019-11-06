
namespace ReversiXT_Client.Constants
{
    public class PositionValues
    {
        // Configuration of Gameplay

        //25 =: standard valuation (other are changed by function below)
        public static int CountHolesAtSide = 1;  //this is vor edge/corner stones

        public const int CornerStone = 710;
        public const int EdgeStone = 50;

        public const int ChoiceStone = 3000;
        public const int BonusStone = 15000;
        public const int InversionStone = 2500;
        public const int LowestValue = InversionStone;
        public const int DecreaseValueEnemyStone = 50000;

        public const int NormalStone = 1; //(last game 07.06.16 - 6)

        // Configuration for Aspiration Window size
        public const int AspirationWindow = 200; //more than corner stone + buffer

        // Configuration of Time and Depth
        public const int MaxDepth = 25;
        public const double TimeSpace = 600;

        // Phase two
        public const int BombPhaseStone = 2;
        public const int OurStonesValue = 10;

        
        public static int GetValueForStone(char FieldSymbol)
        {
            switch (FieldSymbol)
            {
                case SpecialStones.Choice:
                    return ChoiceStone;
                case SpecialStones.Inversion:
                    return InversionStone;
                case SpecialStones.Bonus:
                    return BonusStone;
            }
            return 0;
        }
    }
}
