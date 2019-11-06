using System;
using ReversiXT_Client.Game;

namespace ReversiXT_Client.Array
{
    public class ArrayMethods<T>
    {
        public static T[] NewLength(T[] Array)
        {
            int arrayLenght = Array.Length + 1;

            T[] newArray = new T[arrayLenght];
            for (int index = 0; index < Array.Length; index++)
            {
                newArray[index] = Array[index];
            }

            return newArray;
        }

        /// <summary>
        /// bad performance, allocated new memory
        /// </summary>
        /// <returns>cloned array</returns>
        public static T[,] CloneArray(T[,] Array)
        {
            T[,] cloneArray = new T[Array.GetLength(0), Array.GetLength(1)];
            for (int i = 0; i < Array.GetLength(0); i++)
            {
                for (int j = 0; j < Array.GetLength(1); j++)
                {
                    cloneArray[i, j] = Array[i, j];
                }
            }
            return cloneArray;
        }
    }
}
