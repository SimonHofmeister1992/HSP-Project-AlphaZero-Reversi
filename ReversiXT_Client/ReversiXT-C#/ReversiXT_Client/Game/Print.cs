using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReversiXT_Client.Game
{
    class Print
    {
        public static void WriteLine(String value = "")
        {
            if (Program._quietMode) return;
            Console.WriteLine(value);
        }

        public static void Write(String value = "")
        {
            if (Program._quietMode) return;
            Console.Write(value);
        }
    }
}
