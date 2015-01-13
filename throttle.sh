tc qdisc add dev eth1 root handle 1:0 netem delay 400ms loss 0.03%
tc qdisc add dev eth1 parent 1:1 handle 10: tbf rate 200kbit buffer 1600 limit 3000
