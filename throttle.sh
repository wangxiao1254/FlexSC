tc qdisc del dev $1 root
tc qdisc add dev $1 root handle 1:0 tbf rate $2kbit buffer 1600 limit 3000
tc qdisc add dev $1 parent 1:0 handle 10: netem delay 100ms loss 0%
