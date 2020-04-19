using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ReversiXT_Client.Connection
{
    public static class TextFileReader
    {
        public static string[] GetTextFileContent(string filePath)
        {
            if (File.Exists(filePath))
            {
                var inputStringArray = File.ReadAllLines(filePath);

                return inputStringArray;
            }

            return null;
        }
    }
}
