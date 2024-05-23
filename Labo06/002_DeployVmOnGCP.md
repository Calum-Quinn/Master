# Task 2: Create a cloud infrastructure on Google Compute Engine with Terraform

In this task you will create a simple cloud infrastructure that consists of a single VM on Google Compute Engine. It will be
managed by Terraform.

This task is highly inspired from the following guide: [Get started with Terraform](https://cloud.google.com/docs/terraform/get-started-with-terraform).

Create a new Google Cloud project. Save the project ID (`aerobic-copilot-424214-k9`), it will be used later.

* Name: __labgce__

As we want to create a VM, you need to enable the Compute Engine API:

* [Navigate to google enable api page](https://console.cloud.google.com/flows/enableapi?apiid=compute.googleapis.com)

![EnableAPI](./img/enableAPI.png)

Terraform needs credentials to access the Google Cloud API. Generate and download the Service Account Key:

* Navigate to __IAM & Admin__ > __Service Accounts__. 
* Click on the default service account > __Keys__ and __ADD KEY__ > __Create new key__ (JSON format). 
* On your local machine, create a directory for this lab. In it, create a subdirectory named `credentials` and save the key under the name `labgce-service-account-key.json`, it will be used later.

Generate a public/private SSH key pair that will be used to access the VM and store it in the `credentials` directory:

    ssh-keygen \
      -t ed25519 \
      -f labgce-ssh-key \
      -q \
      -N "" \
      -C ""

At the root of your lab directory, create a `terraform` directory and get the [backend.tf](./appendices/backend.tf), [main.tf](./appendices/main.tf), [outputs.tf](./appendices/outputs.tf) and [variables.tf](./appendices/variables.tf) files. 

These files allow you to deploy a VM, except for a missing file, which you have to provide. Your task is to explore the provided files and using the [Terraform documentation](https://www.terraform.io/docs) understand what these files do. 

The missing file `terraform.tfvars` is supposed to contain values for variables used in the `main.tf` file. Your task is to find out what these values should be. You can freely choose the user account name and the instance name (only lowercase letters, digits and hyphen allowed).

You should have a file structure like this:

    .
    ├── credentials
    │   ├── labgce-service-account-key.json
    │   ├── labgce-ssh-key
    │   └── labgce-ssh-key.pub
    └── terraform
        ├── backend.tf
        ├── main.tf
        ├── outputs.tf
        ├── terraform.tfvars
        └── variables.tf

There are two differences between Google Cloud and AWS that you should know about:

1. Concerning the default Linux system user account created on a VM: In AWS, newly created VMs have a user account that is always named the same for a given OS. For example, Ubuntu VMs have always have a user account named `ubuntu`, CentOS VMs always have a user account named `ec2-user`, and so on. In Google Cloud, the administrator can freely choose the name of the user account.

2. Concerning the public/private key pair used to secure access to the VM: In AWS you create the key pair in AWS and then download the private key. In Google Cloud you create the key pair on your local machine and upload the public key to Google Cloud.

The two preceding parameters are configured in Terraform in the `metadata` section of the `google_compute_instance` resource description. For example, a user account named `fred` with a public key file located at `/path/to/file.pub` is configured as

    metadata = {
      ssh-keys = "fred:${file("/path/to/file.pub")}"
    }
    
This is already taken care of in the provided `main.tf` file.

You can now initialize the Terraform state:

    cd terraform
    terraform init

//TODO
[OUTPUT]
```bash
Initializing the backend...

Successfully configured the backend "local"! Terraform will automatically
use this backend unless the backend configuration changes.

Initializing provider plugins...
- Finding latest version of hashicorp/google...
- Installing hashicorp/google v5.30.0...
- Installed hashicorp/google v5.30.0 (signed by HashiCorp)

Terraform has created a lock file .terraform.lock.hcl to record the provider
selections it made above. Include this file in your version control repository
so that Terraform can guarantee to make the same selections by default when
you run "terraform init" in the future.

Terraform has been successfully initialized!

You may now begin working with Terraform. Try running "terraform plan" to see
any changes that are required for your infrastructure. All Terraform commands
should now work.

If you ever set or change modules or backend configuration for Terraform,
rerun this command to reinitialize your working directory. If you forget, other
commands will detect it and remind you to do so if necessary.
```
    
* What files were created in the `terraform` directory? Make sure to look also at hidden files and directories (`ls -a`).

//TODO
[OUTPUT]
```bash
Directory: 	.terraform
File:		.terraform.lock.hcl
```

* What are they used for?

//TODO
|File/FolderName	|Explanation|
|:--				|:--		|
|.terraform			|Contains the state and the providers|
|.terraform.lock.hcl|Contains the version of the provider|


* Check that your Terraform configuration is valid:

```bash
terraform validate
```

//TODO
[OUTPUT]
```bash
Success! The configuration is valid.
```

* Create an execution plan to preview the changes that will be made to your infrastructure and save it locally:

```bash
terraform plan -input=false -out=.terraform/plan.cache
```

```
//TODO - copy the command result in a file named "planCache.json" and add it to your lab repo.
```

* If satisfied with your execution plan, apply it:

```bash
    terraform apply -input=false .terraform/plan.cache
```

```
//TODO - copy the command result in a file name "planCacheApplied.txt
```

* Test access via ssh

//TODO
[INPUT]
```bash
sudo ssh basic-user@34.65.88.136 -i ./credentials/labgce-ssh-key
```

[OUTPUT]
```
Welcome to Ubuntu 20.04.6 LTS (GNU/Linux 5.15.0-1060-gcp x86_64)

 * Documentation:  https://help.ubuntu.com
 * Management:     https://landscape.canonical.com
 * Support:        https://ubuntu.com/pro

 System information as of Thu May 23 15:30:59 UTC 2024

  System load:  0.0               Processes:             93
  Usage of /:   19.1% of 9.51GB   Users logged in:       0
  Memory usage: 34%               IPv4 address for ens4: 10.172.0.2
  Swap usage:   0%

Expanded Security Maintenance for Applications is not enabled.

0 updates can be applied immediately.

Enable ESM Apps to receive additional future security updates.
See https://ubuntu.com/esm or run: sudo pro status



The programs included with the Ubuntu system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Ubuntu comes with ABSOLUTELY NO WARRANTY, to the extent permitted by
applicable law.
```

If no errors occur, you have successfully managed to create a VM on Google Cloud using Terraform. You should see the IP of the Google Compute instance in the console. Save the instance IP, it will be used later.

After launching make sure you can SSH into the VM using your private
key and the Linux system user account name defined in the `terraform.tfvars` file.

Deliverables:

* Explain the usage of each provided file and its contents by directly adding comments in the file as needed (we must ensure that you understood what you have done). In the file `variables.tf` fill the missing documentation parts and link to the online documentation. Copy the modified files to the report.

```
//TODO
```

* Explain what the files created by Terraform are used for.

```
The .tfstate files are used to summarise the current state of the deployed instance. This can then be used to figure out the necessary steps to take to reach the desired configuration the user provides.
```

* Where is the Terraform state saved? Imagine you are working in a team and the other team members want to use Terraform, too, to manage the cloud infrastructure. Do you see any problems with this? Explain.

```
As explained before, the state is saved in the terraform.tfstate file.

Conflicts could occur between several people attempting to make changes to the same instance.
```

* What happens if you reapply the configuration (1) without changing `main.tf` (2) with a change in `main.tf`? Do you see any changes in Terraform's output? Why? Can you think of examples where Terraform needs to delete parts of the infrastructure to be able to reconfigure it?

```
1)	"No changes. Your infrastructure matches the configuration.

	Terraform has compared your real infrastructure against your configuration and found no differences, so no
	changes are needed."
	
2) "Terraform used the selected providers to generate the following execution plan. Resource actions are
	indicated with the following symbols:
	  ~ update in-place

	Terraform will perform the following actions:

	  # google_compute_firewall.ssh will be updated in-place
	  ~ resource "google_compute_firewall" "ssh" {
			id                      = "projects/aerobic-copilot-424214-k9/global/firewalls/allow-ssh"
			name                    = "allow-ssh"
		  ~ source_ranges           = [
			  + "1.0.0.0/0",
				# (1 unchanged element hidden)
			]
			# (13 unchanged attributes hidden)

			# (1 unchanged block hidden)
		}

	Plan: 0 to add, 1 to change, 0 to destroy."
	
If you remove things from the configuration, for example tighten security on the allowed communications. Terraform will most certainly have to delete some rules/things.
```

* Explain what you would need to do to manage multiple instances.

```
To deploy multiple instances of the same base, you can add a "count" attribute that specifies the quantity.
```

* Take a screenshot of the Google Cloud Console showing your Google Compute instance and put it in the report.


![Instance in Console](./img/Instance.png)


* Deliver a folder "terraform" with your configuration.