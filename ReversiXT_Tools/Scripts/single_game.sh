#!/bin/bash
read -p "Map: " map
rm *.txt
rm logs/*
chmod u+x single_game/start_server.sh
chmod u+x single_game/start_ai_alphazero.sh
#chmod u+x single_game/start_ai_trivial.sh
echo $map | single_game/start_server.sh &
single_game/start_ai_alphazero.sh &
single_game/start_ai_alphazero.sh &
#single_game/start_ai_trivial.sh &

read -p "Press [AnyKey] to end program"