using ReversiXT_Client.Algorithms;
using ReversiXT_Client.Connection;
using ReversiXT_Client.Enums;
using ReversiXT_Client.Game;
using System;

namespace ReversiXT_Client
{
    class Program
    {

        //input parameter
        public static string _connectionIp = "127.0.0.1";
        public static int _port = 7777;
        public static bool _moveSort = true;
        public static bool _quietMode = false;
        public static bool _mapPrint = false;

        #region Initialize
        static void Main(string[] args)
        {
            #region console-parameter

            //Console Parameter
            //args  [-i] [new-server-ip]    : change server adress (default 127.0.0.1)
            //      [-p] [new-server-port]  : change server port (default 7777)
            //      [-n]                    : turn off/on "Zugsortierung"
            //      [-q]                    : turn off/on quiet mode
            //      [-g] [number]           : change group number
            //      [-wm]                   : with map print
            //      [-h]                    : print help
            if (args.Length > 0)
            {
                for (int index = 0; index < args.Length; index++)
                {
                    switch (args[index])
                    {
                        case "-i":
                            index++;
                            _connectionIp = args[index];
                            break;
                        case "-p":
                            index++;
                            try
                            {
                                _port = Convert.ToInt32(args[index]);
                            }
                            catch (Exception e)
                            {
                                Console.WriteLine("Port Failure - wrong convention");
                            }
                            break;
                        case "-n":
                            _moveSort = false;
                            break;
                        case "-q":
                            _quietMode = true;
                            break;
                        case "-wm":
                            _mapPrint = true;
                            break;
                        case "-g":
                            index++;
                            try
                            {
                                MessageBuilder.GroupNumber = Convert.ToByte(args[index]);
                            }
                            catch (Exception e)
                            {
                                Console.WriteLine("Groupnumber Failure - wrong convention");
                            }
                            break;
                        case "-h":
                            PrintHelp();
                            return;
                    }
                }
                Initialize();
                Run();
                return;
            }
            PrintHelp();
            return;

            #endregion

        }
        private static void PrintHelp()
        {
            Console.WriteLine("[-i] [new-server-ip] : change server adress");
            Console.WriteLine("[-p] [new-server-port] : change server port");
            Console.WriteLine("[-n] : turn off/on \"Zugsortierung\"");
            Console.WriteLine("[-q] : turn off/on quiet mode");
            Console.WriteLine("[-wm] : with map print");
            Console.WriteLine("[-g] [number] : change group number");
        }

        private static void Initialize()
        {
            Map.CountTurn = 0;
            Map.CountOurTurn = 0;
            //Todo connect exception wenn kein server da ist
            ServerComponent.Connect();
            ServerComponent.Commit(ServerMessageTypes.GroupNumber);
        }

        private static void ExitApplication()
        {
            ServerComponent.Dispose();
            CustomTimer.DisposeTimer();
            Environment.Exit(0);
        }

        #endregion

        #region Run
        private static void Run()
        {      
            while (true)
            {
                ServerComponent.HandleReceivedMessage();
            }
        }
        #endregion
        
    }
}
