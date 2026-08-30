#!/bin/bash
set -e # Stops the script if any command fails

aws --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name patient-management \
    --template-file "./cdk.out/localstack.template.json"

ELB_DNS=""
if ! ELB_DNS=$(aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" --output text 2>/dev/null); then
    ELB_DNS=""
fi

if [ -n "$ELB_DNS" ] && [ "$ELB_DNS" != "None" ]; then
    echo "API gateway URL: http://${ELB_DNS}"
else
    echo "LocalStack ELBv2 is not licensed in this environment; using the direct localhost endpoint."
    echo "API gateway URL: http://localhost:4004"
fi