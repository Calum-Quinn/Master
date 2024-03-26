### Deploy the elastic load balancer

In this task you will create a load balancer in AWS that will receive
the HTTP requests from clients and forward them to the Drupal
instances.

![Schema](./img/CLD_AWS_INFA.PNG)

## Task 01 Prerequisites for the ELB

* Create a dedicated security group

|Key|Value|
|:--|:--|
|Name|SG-DEVOPSTEAM[XX]-LB|
|Inbound Rules|Application Load Balancer|
|Outbound Rules|Refer to the infra schema|

```bash
[INPUT]

aws ec2 create-security-group --group-name SG-DEVOPSTEAM02-LB --description "Security group for team 2 load balancer" --vpc-id vpc-03d46c285a2af77ba

[OUTPUT]

sg-0979882d4e5089f30

Inbound rules

[INPUT]

aws ec2 authorize-security-group-ingress --group-id sg-0979882d4e5089f30 --protocol tcp --port 8080 --cidr 10.0.0.0/28

[OUTPUT]

True
SECURITYGROUPRULES      10.0.0.0/28     8080    sg-0979882d4e5089f30    709024702237    tcp     False   sgr-08dd0082c202a15bd   8080

```

* Create the Target Group

|Key|Value|
|:--|:--|
|Target type|Instances|
|Name|TG-DEVOPSTEAM[XX]|
|Protocol and port|Refer to the infra schema|
|Ip Address type|IPv4|
|VPC|Refer to the infra schema|
|Protocol version|HTTP1|
|Health check protocol|HTTP|
|Health check path|/|
|Port|Traffic port|
|Healthy threshold|2 consecutive health check successes|
|Unhealthy threshold|2 consecutive health check failures|
|Timeout|5 seconds|
|Interval|10 seconds|
|Success codes|200|

```bash
[INPUT]

// left default value for http code of 200, see doc here: https://docs.aws.amazon.com/cli/latest/reference/elbv2/create-target-group.html
// left default value for --health-check-path of "/", see doc above.

aws elbv2 create-target-group --target-type instance --name TG-DEVOPSTEAM02 --protocol HTTP --port 8080 --ip-address-type ipv4 --vpc-id vpc-03d46c285a2af77ba --protocol-version HTTP1 --health-check-protocol HTTP --health-check-port traffic-port --healthy-threshold-count 2 --unhealthy-threshold-count 2 --health-check-interval-seconds 10 --health-check-timeout-seconds 5

[OUTPUT]

TARGETGROUPS    True    10      /       traffic-port    HTTP    5       2       ipv4    8080    HTTP    HTTP1   arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM02/d6f0f0c87fbf6200    TG-DEVOPSTEAM02 instance        2       vpc-03d46c285a2af77ba
MATCHER 200

[INPUT]

aws elbv2 register-targets --target-group-arn arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM02/d6f0f0c87fbf6200 --targets Id=i-09b1de9e5e3d92e56 Id=i-0a6a5f3b8993a8786

[OUTPUT]

No output, but we have two instances unused targets registered now.
```


## Task 02 Deploy the Load Balancer

[Source](https://aws.amazon.com/elasticloadbalancing/)

* Create the Load Balancer

|Key|Value|
|:--|:--|
|Type|Application Load Balancer|
|Name|ELB-DEVOPSTEAM99|
|Scheme|Internal|
|Ip Address type|IPv4|
|VPC|Refer to the infra schema|
|Security group|Refer to the infra schema|
|Listeners Protocol and port|Refer to the infra schema|
|Target group|Your own target group created in task 01|

Provide the following answers (leave any
field not mentioned at its default value):

```bash
[INPUT]

aws elbv2 create-load-balancer --name ELB-DEVOPSTEAM02 --scheme internal --ip-address-type ipv4 --subnets 	subnet-0318cbafbe9e9e49a subnet-0de1d6edd623c2dd3 --security-group sg-0979882d4e5089f30

[OUTPUT]

LOADBALANCERS   Z3Q77PNBQS71R4  2024-03-26T08:57:31.180000+00:00        internal-ELB-DEVOPSTEAM02-1681402979.eu-west-3.elb.amazonaws.com        ipv4    arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM02/19b37b562c5e2155      ELB-DEVOPSTEAM02        internal        application     vpc-03d46c285a2af77ba
AVAILABILITYZONES       subnet-0318cbafbe9e9e49a        eu-west-3a
AVAILABILITYZONES       subnet-0de1d6edd623c2dd3        eu-west-3b
SECURITYGROUPS  sg-0979882d4e5089f30
STATE   provisioning

```

* Get the ELB FQDN (DNS NAME - A Record)

```bash
[INPUT]

 aws elb describe-load-balancers --load-balancer-names ELB-DEVOPSTEAM02 --region eu-west-3

[OUTPUT]

An error occurred (LoadBalancerNotFound) when calling the DescribeLoadBalancers operation: There is no ACTIVE Load Balancer named 'ELB-DEVOPSTEAM02'

// Strange because it is active, and it exists, and I'm searching in the right region. Might be a question of rights.
// Right answer: internal-ELB-DEVOPSTEAM02-1681402979.eu-west-3.elb.amazonaws.com
```

* Get the ELB deployment status

Note : In the EC2 console select the Target Group. In the
       lower half of the panel, click on the **Targets** tab. Watch the
       status of the instance go from **unused** to **initial**.

* Ask the DMZ administrator to register your ELB with the reverse proxy via the private teams channel

* Update your string connection to test your ELB and test it

```bash
//connection string updated

ssh devopsteam02@15.188.43.46 -i CLD_KEY_DMZ_DEVOPSTEAM02.pem -L 2223:10.0.2.10:22 -L 888:10.0.2.10:8080 -L 2224:10.0.2.140:22 -L 889:10.0.2.140:8080 -L 1234:internal-ELB-DEVOPSTEAM02-1681402979.eu-west-3.elb.amazonaws.com:8080
```

* Test your application through your ssh tunneling

```bash
[INPUT]
curl localhost:1234

[OUTPUT]

// J'ai des connections refused etc.
// Il y a des soucis encore.

// J'ai aussi des problèmes avec l'état du target group qui part pas de unused. Je sais pas trop pourquoi.

```

#### Questions - Analysis

* On your local machine resolve the DNS name of the load balancer into
  an IP address using the `nslookup` command (works on Linux, macOS and Windows). Write
  the DNS name and the resolved IP Address(es) into the report.

```
//TODO
```

* From your Drupal instance, identify the ip from which requests are sent by the Load Balancer.

Help : execute `tcpdump port 8080`

```
//TODO
```

* In the Apache access log identify the health check accesses from the
  load balancer and copy some samples into the report.

```
//TODO
```
