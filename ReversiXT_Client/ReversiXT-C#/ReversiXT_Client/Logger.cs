using ReversiXT_Client.Game;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace ReversiXT_Client
{
    public class Logger
    {
        public static void Log(Exception e)
        {
            Print.WriteLine("[" + DateTime.Now + "]" + ": " + e.Message + " StackTrace: " + e.StackTrace);
        }

        public static void Log(string message)
        {
            Print.WriteLine("[" + DateTime.Now + "]" + ": " + message);
        }
    }
}
