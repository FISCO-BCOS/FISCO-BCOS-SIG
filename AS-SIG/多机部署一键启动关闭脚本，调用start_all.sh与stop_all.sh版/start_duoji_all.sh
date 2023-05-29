#!/usr/bin/expect -f                  
 
set timeout 10 

set ssh_machine1_ip "192.168.37.131"
set ssh_machine1_user "fisco"
set ssh_machine1_passwd "fisco"

set ssh_machine2_ip "192.168.37.132"
set ssh_machine2_user "fisco"
set ssh_machine2_passwd "fisco"

set ssh_machine3_ip "192.168.37.133"
set ssh_machine3_user "fisco"
set ssh_machine3_passwd "fisco"

set ssh_machine4_ip "192.168.37.135"
set ssh_machine4_user "fisco"
set ssh_machine4_passwd "fisco"

spawn ssh $ssh_machine1_user@$ssh_machine1_ip

expect {
    "(yes/no)?" { send "yes\r"; exp_continue }

    "*password*" {send "$ssh_machine1_passwd\r"}


}

expect "*$ssh_machine1_user*" {send "bash ~/fisco/$ssh_machine1_ip/start_all.sh\r"}

expect eof


spawn ssh $ssh_machine2_user@$ssh_machine2_ip
expect {
    "(yes/no)?" { send "yes\r"; exp_continue }

    "*password*" {send "$ssh_machine2_passwd\r"}


}

expect "*$ssh_machine2_user*" {send "bash ~/fisco/$ssh_machine2_ip/start_all.sh\r"}

expect eof

spawn ssh $ssh_machine3_user@$ssh_machine3_ip
expect {
    "(yes/no)?" { send "yes\r"; exp_continue }

    "*password*" {send "$ssh_machine3_passwd\r"}


}

expect "*$ssh_machine3_user*" {send "bash ~/fisco/$ssh_machine3_ip/start_all.sh\r"}

expect eof

spawn ssh $ssh_machine4_user@$ssh_machine4_ip
expect {
    "(yes/no)?" { send "yes\r"; exp_continue }

    "*password*" {send "$ssh_machine4_passwd\r"}


}

expect "*$ssh_machine4_user*" {send "bash ~/fisco/$ssh_machine4_ip/start_all.sh\r"}

expect eof

echo 所有节点启动完成