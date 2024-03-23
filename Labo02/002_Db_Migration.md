# Database migration

In this task you will migrate the Drupal database to the new RDS database instance.

![Schema](./img/CLD_AWS_INFA.PNG)

## Task 01 - Securing current Drupal data

### [Get Bitnami MariaDb user's password](https://docs.bitnami.com/aws/faq/get-started/find-credentials/)

```bash
[INPUT]
//help : path /home/bitnami/bitnami_credentials
cat bitnami_credentials
[OUTPUT]
Welcome to the Bitnami package for Drupal

******************************************************************************
The default username and password is 'user' and 'eikzWruLc2S!'.
******************************************************************************

You can also use this password to access the databases and any other component the stack includes.

Please refer to https://docs.bitnami.com/ for more details.
```

### Get Database Name of Drupal

```bash
[INPUT]
//add string connection
mariadb -u admin -h dbi-devopsteam02.cshki92s4w5p.eu-west-3.rds.amazonaws.com -p
Enter password:
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 23
Server version: 10.11.7-MariaDB managed by https://aws.amazon.com/rds/

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [(none)]>
show databases;

[OUTPUT]
+--------------------+
| Database           |
+--------------------+
| information_schema |
| innodb             |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
5 rows in set (0.003 sec)
```

### [Dump Drupal DataBases](https://mariadb.com/kb/en/mariadb-dump/)

```bash
[INPUT]
mariadb-dump bitnami_drupal -u root --host 127.0.0.1 -p > dump.sql
[OUTPUT]
Aucune mais on voit ensuite le fichier d'output.
```

### Create the new Data base on RDS

```sql
[INPUT]
CREATE DATABASE bitnami_drupal;
```

### [Import dump in RDS db-instance](https://mariadb.com/kb/en/restoring-data-from-dump-files/)

Note : you can do this from the Drupal Instance. Do not forget to set the "-h" parameter.

```sql
[INPUT]
mysql -h <rds-end-point> -u <rds_admin_user> -p <db_target> < <pathToDumpFileToImport>.sql
mysql -h dbi-devopsteam02.cshki92s4w5p.eu-west-3.rds.amazonaws.com -u admin -p bitnami_drupal < dump.sql
[OUTPUT]
Aucun output.
```

### [Get the current Drupal connection string parameters](https://www.drupal.org/docs/8/api/database-api/database-configuration)

```bash
[INPUT]
//help : same settings.php as before
tail -n 15 /opt/bitnami/drupal/sites/default/settings.php
[OUTPUT]
#   include $app_root . '/' . $site_path . '/settings.local.php';
# }
$databases['default']['default'] = array (
  'database' => 'bitnami_drupal',
  'username' => 'bn_drupal',
  'password' => 'c9aca165f2696b1e3aed5e80ee16ae768f8a5acdf1eedb6dad9be4931a7080dc',
  'prefix' => '',
  'host' => '127.0.0.1',
  'port' => '3306',
  'isolation_level' => 'READ COMMITTED',
  'driver' => 'mysql',
  'namespace' => 'Drupal\\mysql\\Driver\\Database\\mysql',
  'autoload' => 'core/modules/mysql/src/Driver/Database/mysql/',
);
$settings['config_sync_directory'] = 'sites/default/files/config_7pnqmC4bdlHfrU0Ynir2PgF_c5HSzW5lvSs1Geu2JSBl683lVzip_NHTK31AUZTprOg5LU5Yyg/sync';
```

### Replace the current host with the RDS FQDN

```
//settings.php

$databases['default']['default'] = array (
   [...] 
  'host' => 'dbi-devopsteam02.cshki92s4w5p.eu-west-3.rds.amazonaws.com',
   [...] 
);
```

### [Create the Drupal Users on RDS Data base](https://mariadb.com/kb/en/create-user/)

Note : only calls from both private subnets must be approved.
* [By Password](https://mariadb.com/kb/en/create-user/#identified-by-password)
* [Account Name](https://mariadb.com/kb/en/create-user/#account-names)
* [Network Mask](https://cric.grenoble.cnrs.fr/Administrateurs/Outils/CalculMasque/)

```sql
[INPUT]
CREATE USER bn_drupal@'10.0.2.%' IDENTIFIED BY '<redacted>';

GRANT ALL PRIVILEGES ON bitnami_drupal.* TO bn_drupal@'10.0.2.%';

FLUSH PRIVILEGES;
```

```sql
//validation
[INPUT]
SHOW GRANTS for bn_drupal@'10.0.2.%';

[OUTPUT]
+-----------------------------------------------------------------------------------------------------------------+
| Grants for bn_drupal@10.0.2.%
                                      |
+-----------------------------------------------------------------------------------------------------------------+
| GRANT USAGE ON *.* TO `bn_drupal`@`10.0.2.%` IDENTIFIED BY PASSWORD '*4BB39F5A212B499B241DF843A14C34F0C5F7B16A' |
| GRANT ALL PRIVILEGES ON `bitnami_drupal`.* TO `bn_drupal`@`10.0.2.%`                                            |
+-----------------------------------------------------------------------------------------------------------------+
```

### Validate access (on the drupal instance)

```sql
[INPUT]
mariadb -h dbi-devopsteam02.cshki92s4w5p.eu-west-3.rds.amazonaws.com -u bn_drupal -p

[INPUT]
show databases;

[OUTPUT]
+--------------------+
| Database           |
+--------------------+
| bitnami_drupal     |
| information_schema |
+--------------------+
2 rows in set (0.001 sec)
```

* Repeat the procedure to enable the instance on subnet 2 to also talk to your RDS instance.