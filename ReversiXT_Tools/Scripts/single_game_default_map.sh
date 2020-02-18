#!/bin/bash
rm *.txt
rm logs/*
chmod u+x single_game/start_server.sh
chmod u+x single_game/start_ai_alphazero.sh
#chmod u+x single_game/start_ai_trivial.sh
echo $map | single_game/start_server_default_map.sh &
echo "client1" | single_game/start_ai_alphazero.sh &
echo "client2" | single_game/start_ai_alphazero.sh &
#echo "client2" | single_game/start_ai_trivial.sh &

read -p "Press [AnyKey] to end program"