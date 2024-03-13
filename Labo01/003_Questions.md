* What is the smallest and the biggest instance type (in terms of
  virtual CPUs and memory) that you can choose from when creating an
  instance?

```
The smallest type would be the t2.nano with 1 vCPU and 0.5 GiB of memory.

Tha largest type would be the u-6tb1.112xlarge with 448 vCPU's and 6144 GiB of memory.
```

* How long did it take for the new instance to get into the _running_
  state?

```
After clicking the "launch instance" button, roughly 6 seconds later the instance had the `Running` state. This seems very short though.
```

* Using the commands to explore the machine listed earlier, respond to
  the following questions and explain how you came to the answer:

  * What's the difference between time here in Switzerland and the time set on
      the machine?
```
There is a one hour difference.
```

  * What's the name of the hypervisor?
```
HVM is the used hypervisor, this information can be found on the console under the details about the instance.
```

  * How much free space does the disk have?
```
After running the "df -h" command we got this output:

Filesystem       Size  Used Avail Use% Mounted on
udev             464M     0  464M   0% /dev
tmpfs             95M  472K   95M   1% /run
/dev/nvme0n1p1   9.7G  3.5G  5.8G  38% /
tmpfs            475M     0  475M   0% /dev/shm
tmpfs            5.0M     0  5.0M   0% /run/lock
/dev/nvme0n1p15  124M   12M  113M  10% /boot/efi
tmpfs             95M     0   95M   0% /run/user/1000

As we can see on the line for /dev/nvme0n1p1 there are 5.8G available.
```


* Try to ping the instance ssh srv from your local machine. What do you see?
  Explain. Change the configuration to make it work. Ping the
  instance, record 5 round-trip times.

```
There is no response from the ping when pinging the SSH server from our instance.
There is no response from the ping when pinging the instance from the server either.

After adding an inbound rule to the SSH server security group to allow ICMP traffic from our subnet (10.0.2.10/28), we got the following times when pinging the server from our instance:
bitnami@ip-10-0-2-10:~$ ping 10.0.0.5
PING 10.0.0.5 (10.0.0.5) 56(84) bytes of data.
64 bytes from 10.0.0.5: icmp_seq=1 ttl=64 time=0.306 ms
64 bytes from 10.0.0.5: icmp_seq=2 ttl=64 time=0.388 ms
64 bytes from 10.0.0.5: icmp_seq=3 ttl=64 time=0.323 ms
64 bytes from 10.0.0.5: icmp_seq=4 ttl=64 time=0.383 ms
64 bytes from 10.0.0.5: icmp_seq=5 ttl=64 time=0.323 ms
```

* Determine the IP address seen by the operating system in the EC2
  instance by running the `ifconfig` command. What type of address
  is it? Compare it to the address displayed by the ping command
  earlier. How do you explain that you can successfully communicate
  with the machine?

```


The "ifconfig" command does not seem to exist. When using the "ip addr show" command we get the server's private address 10.0.0.5.
We can communicate with the machine because we made sure the SSH server could accept ICMP traffic from our specific subnet and that the server can transmit ICMP messages into our subnet.

devopsteam02@ip-10-0-0-5:~$ ifconfig
-bash: ifconfig: command not found
devopsteam02@ip-10-0-0-5:~$ ip addr
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host noprefixroute
       valid_lft forever preferred_lft forever
2: enX0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 9001 qdisc fq_codel state UP group default qlen 1000
    link/ether 06:d9:0b:3d:1a:85 brd ff:ff:ff:ff:ff:ff
    inet 10.0.0.5/28 metric 100 brd 10.0.0.15 scope global dynamic enX0
       valid_lft 3584sec preferred_lft 3584sec
    inet6 fe80::4d9:bff:fe3d:1a85/64 scope link
       valid_lft forever preferred_lft forever
```
