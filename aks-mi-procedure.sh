# list of commands used to enable workload identity
# taken from https://learn.microsoft.com/en-us/azure/aks/workload-identity-deploy-cluster

# If the cluster already exists:
az aks update -n banchioaks2 -g aks --enable-oidc-issuer --enable-workload-identity

export AKS_OIDC_ISSUER="$(az aks show -n banchioaks2 -g aks --query "oidcIssuerProfile.issuerUrl" -otsv)"

# Confirm oidc issuer is present
echo $AKS_OIDC_ISSUER 

export SUBSCRIPTION_ID="$(az account show --query id --output tsv)"
export USER_ASSIGNED_IDENTITY_NAME="banchiospringapp-aks"
export RG_NAME="aks"
export LOCATION="eastus"

# UAMI to be created
az identity create --name "${USER_ASSIGNED_IDENTITY_NAME}" --resource-group "${RG_NAME}" --location "${LOCATION}" --subscription "${SUBSCRIPTION_ID}"

# then get UAMI Client Id
export USER_ASSIGNED_CLIENT_ID="$(az identity show --resource-group "${RG_NAME}" --name "${USER_ASSIGNED_IDENTITY_NAME}" --query 'clientId' -otsv)"

# assegnare permesso al key vault
export RG_NAME="shared"
export KEYVAULT_NAME="keyvaultstd"

az keyvault set-policy --name "${KEYVAULT_NAME}" --secret-permissions get --spn "${USER_ASSIGNED_CLIENT_ID}"


# create service account
export SERVICE_ACCOUNT_NAME="banchiospringapp-aks-sa"
export SERVICE_ACCOUNT_NAMESPACE="default"

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  annotations:
    azure.workload.identity/client-id: "${USER_ASSIGNED_CLIENT_ID}"
  labels:
    azure.workload.identity/use: "true"
  name: "${SERVICE_ACCOUNT_NAME}"
  namespace: "${SERVICE_ACCOUNT_NAMESPACE}"
EOF

az identity federated-credential create --name banchiospringapp-aks-federated --identity-name "${USER_ASSIGNED_IDENTITY_NAME}" --resource-group "${RG_NAME}" --issuer "${AKS_OIDC_ISSUER}" --subject system:serviceaccount:"${SERVICE_ACCOUNT_NAMESPACE}":"${SERVICE_ACCOUNT_NAME}"