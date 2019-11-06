
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Timers;

namespace ReversiXT_Client.Game
{
    public static class CustomTimer
    {
        private static int MaxTime;
        private static bool IsAlreadyWritten;
        private const int tolerance = 450;
        #region Properties

        private static Timer Timer { get; set; }
        private static DateTime? StartTime { get; set; }
        private static DateTime? EndTime { get; set; }
        public static bool IsTimerElapsed { get; private set; }

        #endregion

        //WHY??
        //
        //public static void StartTimer(TimeSpan timespan)
        //{
        //    Timer = new Timer(timespan.Milliseconds - tolerance);
        //    Timer.Elapsed += Timer_Elapsed;

        //    Timer.Start();
        //    StartTime = DateTime.Now;
        //}

        public static void StartTimer(int milliseconds)
        {
            IsTimerElapsed = false;

            if (milliseconds != 0)
            {
                MaxTime = milliseconds;
                StartTime = DateTime.Now;
                Timer = new Timer(milliseconds - tolerance);
                Timer.Elapsed += Timer_Elapsed;

                Print.WriteLine("TIMER STARTED----------------LIMIT:" + milliseconds +"---------------");

                Timer.Start();
            }
        }

        public static string GetNeededTime()
        {
            EndTime = DateTime.Now;

            if (EndTime != null && StartTime != null)
                return (EndTime - StartTime).Value.Milliseconds.ToString();

            return "unknown";
        }

        private static void Timer_Elapsed(object sender, ElapsedEventArgs e)
        {
            Print.WriteLine("Timer elapsed (" + GetNeededTime() + " ms)" + " - committing ...");
            IsTimerElapsed = true;
        }

        public static void StopTimer()
        {
            EndTime = DateTime.Now;
            Timer.Elapsed -= Timer_Elapsed;
            Timer.Close();
        }

        public static void DisposeTimer()
        {
            Timer.Dispose();
        }


    }
}
