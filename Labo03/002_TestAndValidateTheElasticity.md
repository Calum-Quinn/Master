# Task 003 - Test and validate the elasticity

![Schema](./img/CLD_AWS_INFA.PNG)


## Simulate heavy load to trigger a scaling action

* [Install the package "stress" on your Drupal instance](https://www.geeksforgeeks.org/linux-stress-command-with-examples/)

* [Install the package htop on your Drupal instance](https://www.geeksforgeeks.org/htop-command-in-linux-with-examples/)

* Check how many vCPU are available (with htop command)

```
[INPUT]
htop

[OUTPUT]
![htop output](./img/htop.png)
```

### Stress your instance

```
[INPUT]
stress --cpu 2

[OUTPUT]
![htop output](./img/htop2.png)
```

* (Scale-IN) Observe the autoscaling effect on your infa


```
[INPUT]
![cloud Watch](./img/cloudWatch.png)
```
[Sample](./img/CLD_AWS_CLOUDWATCH_CPU_METRICS.PNG)

```
![instance list](./img/instanceList.png)
```
[Sample](./img/CLD_AWS_EC2_LIST.PNG)

```
//TODO Validate that the various instances have been distributed between the two available az.
[INPUT]
aws ec2 describe-instances

[OUTPUT]

```

```
//TODO screenshot of the activity history
![activity history](./img/activity.png)
```
[Sample](./img/CLD_AWS_ASG_ACTIVITY_HISTORY.PNG)

```
//TODO screenshot of the cloud watch alarm target tracking
![alarms](./img/alarms.png)
```
[Sample](./img/CLD_AWS_CLOUDWATCH_ALARMHIGH_STATS.PNG)


* (Scale-OUT) As soon as all 4 instances have started, end stress on the main machine.

[Change the default cooldown period](https://docs.aws.amazon.com/autoscaling/ec2/userguide/ec2-auto-scaling-scaling-cooldowns.html)

```
//TODO screenshot from cloud watch metric
![cloud watch](./img/cloudWatch2.png)
```

```
//TODO screenshot of ec2 instances list (terminated state)
![instance list](./img/instanceList2.png)
```

```
//TODO screenshot of the activity history
![activity history](./img/activity2.png)
```

## Release Cloud resources

Once you have completed this lab release the cloud resources to avoid
unnecessary charges:

* Terminate the EC2 instances.
    * Make sure the attached EBS volumes are deleted as well.
* Delete the Auto Scaling group.
* Delete the Elastic Load Balancer.
* Delete the RDS instance.

(this last part does not need to be documented in your report.)

## Delivery

Inform your teacher of the deliverable on the repository (link to the commit to retrieve)