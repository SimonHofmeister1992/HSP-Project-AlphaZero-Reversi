#!/bin/bash

rm *.txt
rm server/*.txt
rm logs_best_model/*.txt
rm logs_learning_model/*.txt

for P in {1..500} ; do

nohup ./../../ServerNoGL/Linux64_ServerNOGL/Linux/server_nogl -d 8 -m ../../Maps/actualLearningMaps/lmap1_2p.map > logs_server/${P}_1_server.txt &

nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo2/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar > logs_best_model/${P}_1_best.txt &

sleep 5

nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --l > logs_learning_model/${P}_1_learner.txt &


# use own user instead of ws1920-hsp-g03-reversiml
PID=`/bin/ps -fu ws1920-hsp-g03-reversiml | grep "ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --l" | grep -v "grep" | awk '{print $2}'`
while s=`ps -p $PID -o s=` && [[ "$s" && "$s" != 'Z' ]]; do
    sleep 1
done


nohup ./../../ServerNoGL/Linux64_ServerNOGL/Linux/server_nogl -d 8 -m ../../Maps/actualLearningMaps/lmap1_2p.map > logs_server/${P}_2_server.txt &

nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --l > logs_learning_model/${P}_2_learner.txt &

sleep 3

nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo2/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar > logs_best_model/${P}_2_best.txt &

# use own user instead of ws1920-hsp-g03-reversiml
PID=`/bin/ps -fu ws1920-hsp-g03-reversiml | grep "ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --l" | grep -v "grep" | awk '{print $2}'`
while s=`ps -p $PID -o s=` && [[ "$s" && "$s" != 'Z' ]]; do
    sleep 1
done




done

