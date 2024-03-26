# Custom AMI and Deploy the second Drupal instance

In this task you will update your AMI with the Drupal settings and deploy it in the second availability zone.

## Task 01 - Create AMI

### [Create AMI](https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/create-image.html)

Note : stop the instance before

|Key|Value for GUI Only|
|:--|:--|
|Name|AMI_DRUPAL_DEVOPSTEAM[XX]_LABO02_RDS|
|Description|Same as name value|

```bash
[INPUT]
aws ec2 create-image --instance-id i-09b1de9e5e3d92e56 --name AMI_DRUPAL_DEVOPSTEAM02_LABO02_RDS
[OUTPUT]
{
    "ImageId": "ami-0cc3b068ac3a8c290"
}
```

## Task 02 - Deploy Instances

* Restart Drupal Instance in Az1

* Deploy Drupal Instance based on AMI in Az2

|Key|Value for GUI Only|
|:--|:--|
|Name|EC2_PRIVATE_DRUPAL_DEVOPSTEAM[XX]_B|
|Description|Same as name value|

```bash
[INPUT]
aws ec2 run-instances --image-id ami-0cc3b068ac3a8c290 --instance-type t3.micro --key-name CLD_KEY_DRUPAL_DEVOPSTEAM02
--security-group-ids sg-05c4194274dc5d64c --subnet-id subnet-0de1d6edd623c2dd3 --private-ip-address 10.0.2.140 
--tag-specifications ResourceType=instance,Tags=[{Key=Name,Value=EC2_PRIVATE_DRUPAL_DEVOPSTEAM02_B}]
[OUTPUT]
709024702237    r-026b6d7fcc77e9f76
INSTANCES       0       x86_64  e9abcb19-9a5d-4eb8-9cbb-4a75deb7a228    legacy-bios     False   True    xen     ami-0cc3b068ac3a8c290   i-0a6a5f3b8993a8786     t3.micro        CLD_KEY_DRUPAL_DEVOPSTEAM02     2024-03-21T16:25:30+00:00       ip-10-0-2-140.eu-west-3.compute.internal        10.0.2.140              /dev/xvda       ebs     True            subnet-0de1d6edd623c2dd3        hvm     vpc-03d46c285a2af77ba
CAPACITYRESERVATIONSPECIFICATION        open
CPUOPTIONS      1       2
ENCLAVEOPTIONS  False
MAINTENANCEOPTIONS      default
METADATAOPTIONS enabled disabled        1       optional        disabled        pending
MONITORING      disabled
NETWORKINTERFACES               interface       0a:74:fe:31:7e:91       eni-0ae1e101ec29986e2   709024702237    10.0.2.140      True    in-use  subnet-0de1d6edd623c2dd3        vpc-03d46c285a2af77ba
ATTACHMENT      2024-03-21T16:25:30+00:00       eni-attach-063e9946b3612a871    True    0       0       attaching
GROUPS  sg-05c4194274dc5d64c    SG-PRIVATE-DRUPAL-DEVOPSTEAM02
PRIVATEIPADDRESSES      True    10.0.2.140
PLACEMENT       eu-west-3b              default
PRIVATEDNSNAMEOPTIONS   False   False   ip-name
SECURITYGROUPS  sg-05c4194274dc5d64c    SG-PRIVATE-DRUPAL-DEVOPSTEAM02
STATE   0       pending
STATEREASON     pending pending
TAGS    Name    EC2_PRIVATE_DRUPAL_DEVOPSTEAM02_B
```

## Task 03 - Test the connectivity

### Update your ssh connection string to test

* add tunnels for ssh and http pointing on the B Instance

```bash
//updated string connection

ssh devopsteam02@15.188.43.46 -i CLD_KEY_DMZ_DEVOPSTEAM02.pem -L 2223:10.0.2.10:22 -L 888:10.0.2.10:8080 -L 2224:10.0.2.140:22 -L 889:10.0.2.140:8080
```

## Check SQL Accesses

```sql
[INPUT]
//sql string connection from A

bitnami@ip-10-0-2-10:~$ mariadb -h dbi-devopsteam02.cshki92s4w5p.eu-west-3.rds.amazonaws.com -u bn_drupal -p

[OUTPUT]

Enter password:
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 71
Server version: 10.11.7-MariaDB managed by https://aws.amazon.com/rds/

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [(none)]>
```

```sql
[INPUT]
//sql string connection from B

bitnami@ip-10-0-2-140:~$ mariadb -h dbi-devopsteam02.cshki92s4w5p.eu-west-3.rds.amazonaws.com -u bn_drupal -p

[OUTPUT]

Enter password:
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 72
Server version: 10.11.7-MariaDB managed by https://aws.amazon.com/rds/

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [(none)]>
```

### Check HTTP Accesses

```bash
//connection string updated

curl localhost:888

curl localhost:889
```

### Read and write test through the web app

* Login in both webapps (same login)

* Change the users' email address on a webapp... refresh the user's profile page on the second and validated that they are communicating with the same db (rds).

* Observations ?

```
//TODO

I only needed to connect on one machine, and it left me connected on the other machine.

Changed the email address on port webapp accessible from port 888 and it changed as well on the webapp accessible from port 889.

```

### Change the profil picture

* Observations ?

```
//TODO

While the email address information was shared, the image wasn't. It's not visible on the second instance. But the information that there is an image is present on the second instance, it's just that the image data isn't accessible.
```