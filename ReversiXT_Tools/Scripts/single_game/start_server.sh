#!/bin/bash
read -p "Map: " map
#nohup ../ServerNoGL/LinuxVersion_ServerNOGL/server_nogl -m ../Maps/actualLearningMaps/lmap1_2p.map > logs/server.out
nohup ../ServerNoGL/LinuxVersion_ServerNOGL/server_nogl -m ../Maps/actualLearningMaps/${map} > logs/server.out
