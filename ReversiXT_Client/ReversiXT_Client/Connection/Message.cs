using ReversiXT_Client.Enums;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ReversiXT_Client.Connection
{
    public struct Message
    {
        public ServerMessageTypes Type { get; set; }
        public int Length { get; set; }
        public byte[] ByteMessage { get; set; }

        public override string ToString()
        {
            return "Type: " + Enum.GetName(typeof(ServerMessageTypes), Type) +
                "   Length: " + Length + "   Message in Byte: " + GetMessageToString();
        }

        private string GetMessageToString()
        {
            var message = string.Empty;

            for(int i=0; i <= ByteMessage.Length-1; i++)
            {
                message += ByteMessage[i];
            }

            return message;
        }
    }
}
