resource "aws_iam_role" "digitalhippo-eks-service-role" {
  name = "digitalhippo-eks-service-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = "sts:AssumeRole",
        Principal = {
          Service = "eks.amazonaws.com"
        }
      }
    ]
  })
}

# attach required IAM policy to the EKS cluster IAM Role
resource "aws_iam_role_policy_attachment" "digitalhippo-AmazonEKSClusterPolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.digitalhippo-eks-service-role.name
}

# EKS cluster digitalhippo-backend-cluster
resource "aws_eks_cluster" "digitalhippo-backend-cluster" {
  name = "digitalhippo-backend"
  # IAM role
  role_arn = aws_iam_role.digitalhippo-eks-service-role.arn

  vpc_config {
    subnet_ids = [
      aws_subnet.private-subnet-ca-central-1a.id,
      aws_subnet.private-subnet-ca-central-1b.id,
      aws_subnet.public-subnet-ca-central-1a.id,
      aws_subnet.public-subnet-ca-central-1b.id
    ]
  }

  depends_on = [aws_iam_role_policy_attachment.digitalhippo-AmazonEKSClusterPolicy]
}