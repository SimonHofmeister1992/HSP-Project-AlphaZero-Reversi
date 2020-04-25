#!/bin/bash

rm *.txt
rm server/*.txt
rm logs_best_model/*.txt
rm logs_learning_model/*.txt

for P in {1..3} ; do

nohup ./../../ServerNoGL/Linux64_ServerNOGL/Linux/server_nogl -d 8 -m ../../Maps/actualLearningMaps/lmap1_2p.map > logs_server/${P}_server.txt &

nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --q > logs_best_model/${P}_best.txt &
sleep 5
nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --q --l > logs_learning_model/${P}_learner.txt &

PID=`/bin/ps -fu ws1920-hsp-g03-reversiml | grep "ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --q --l" | grep -v "grep" | awk '{print $2}'`
while s=`ps -p $PID -o s=` && [[ "$s" && "$s" != 'Z' ]]; do
    sleep 1
done

nohup ./../../ServerNoGL/Linux64_ServerNOGL/Linux/server_nogl -d 8 -m ../../Maps/actualLearningMaps/lmap1_2p.map > logs_server/${P}_server.txt &

nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --q > logs_best_model/${P}_best.txt &
sleep 5
nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --q --l > logs_learning_model/${P}_learner.txt &

PID=`/bin/ps -fu ws1920-hsp-g03-reversiml | grep "ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --q --l" | grep -v "grep" | awk '{print $2}'`
while s=`ps -p $PID -o s=` && [[ "$s" && "$s" != 'Z' ]]; do
    sleep 1
done

done

