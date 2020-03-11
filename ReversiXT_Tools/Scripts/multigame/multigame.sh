#!/bin/bash

for P in {1..200} ; do

rm *.txt

nohup ./../../ServerNoGL/Linux64_ServerNOGL/Linux/server_nogl -d 8 -m ../../Maps/actualLearningMaps/lmap1_2p.map > server.txt &

nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --q > best.txt &

nohup java -jar ../../../ReversiXT_Client/ReversiAlphaGo/target/ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --q --l > learner.txt &

PID=`/bin/ps -fu ws1920-hsp-g03-reversiml | grep "ReversiAlphaGo-0.0.1-SNAPSHOT-jar-with-dependencies.jar --q --l" | grep -v "grep" | awk '{print $2}'`
while s=`ps -p $PID -o s=` && [[ "$s" && "$s" != 'Z' ]]; do
    sleep 1
done

done

