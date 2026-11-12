
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

resource "aws_instance" "portfolio" {
  ami                    = var.ami_id
  instance_type          = "t3.micro"
  key_name               = "portfolio-key"
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [var.security_group_id]

  monitoring   = false
  ebs_optimized = true

  root_block_device {
    volume_size           = 8
    volume_type           = "gp3"
    encrypted             = false
    iops                  = 3000
    throughput            = 125
    delete_on_termination = true
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  tags = {
    Name = "portfolio"
  }

  lifecycle {
    prevent_destroy = true
  }
}
