variable "gcp_project_id" {
  description = "Id for the google cloud project"
  type        = string
  nullable    = false
}

variable "gcp_service_account_key_file_path" {
  description = "Path to the service account JSON created in the google console"
  type        = string
  nullable    = false
}

variable "gce_instance_name" {
  description = "Name of the instance we are creating"
  type        = string
  nullable    = false
}

variable "gce_instance_user" {
  description = "Name of the user we want to connect with"
  type        = string
  nullable    = false
}

variable "gce_ssh_pub_key_file_path" {
  description = "Path to our public SSH key with which will authenticate connections to the instance"
  type        = string
  nullable    = false
}
