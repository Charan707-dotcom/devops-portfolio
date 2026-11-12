
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-south-1"
}

variable "ami_id" {
  description = "Existing Ubuntu AMI"
  type        = string
  default     = "ami-094adff9b24048c5c"
}

variable "subnet_id" {
  description = "Existing EC2 subnet"
  type        = string
  default     = "subnet-09182f919a75da040"
}

variable "security_group_id" {
  description = "Existing portfolio security group"
  type        = string
  default     = "sg-00aa77905547ce38a"
}
