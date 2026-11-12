
output "instance_id" {
  description = "Existing portfolio EC2 instance ID"
  value       = aws_instance.portfolio.id
}

output "public_ip" {
  description = "Portfolio EC2 public IP"
  value       = aws_instance.portfolio.public_ip
}

output "public_dns" {
  description = "Portfolio EC2 public DNS"
  value       = aws_instance.portfolio.public_dns
}

output "website_url" {
  description = "Portfolio website"
  value       = "http://${aws_instance.portfolio.public_ip}"
}
