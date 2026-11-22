#!/usr/bin/env bash
# =====================================================================
#  Crea la infraestructura (se corre UNA vez; si falla, se puede repetir):
#  APIs, Artifact Registry, Cloud SQL, bucket, secretos y permisos.
#  Uso (en Cloud Shell, desde la raíz del repo):  bash deploy/01-infraestructura.sh
# =====================================================================
set -euo pipefail
cd "$(dirname "$0")"
source ./config.sh

if [[ "$PROJECT_ID" == *CAMBIA-ESTO* ]]; then
  echo "Primero edita deploy/config.sh (PROJECT_ID, FRONTEND_URL, ADMIN_EMAIL)."; exit 1
fi

gcloud config set project "$PROJECT_ID" >/dev/null
PROJECT_NUMBER=$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')
SA="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

existe() { "$@" >/dev/null 2>&1; }

echo "==> 1/7 Habilitando APIs (1-2 min)"
gcloud services enable run.googleapis.com sqladmin.googleapis.com \
  artifactregistry.googleapis.com cloudbuild.googleapis.com \
  secretmanager.googleapis.com storage.googleapis.com \
  iam.googleapis.com compute.googleapis.com

echo "==> 2/7 Repositorio de imágenes (Artifact Registry)"
existe gcloud artifacts repositories describe "$REPO" --location="$REGION" || \
  gcloud artifacts repositories create "$REPO" --repository-format=docker --location="$REGION"

echo "==> 3/7 Base de datos Cloud SQL (la primera vez tarda ~10 min)"
if ! existe gcloud sql instances describe "$SQL_INSTANCE"; then
  gcloud sql instances create "$SQL_INSTANCE" \
    --database-version=POSTGRES_16 --edition=enterprise --tier=db-f1-micro \
    --region="$REGION" --storage-size=10 --backup-start-time=09:00
fi
for db in tdis_usuarios tdis_catalogo tdis_tramites tdis_documentos; do
  existe gcloud sql databases describe "$db" --instance="$SQL_INSTANCE" || \
    gcloud sql databases create "$db" --instance="$SQL_INSTANCE"
done

echo "==> 4/7 Usuario de la base de datos y secretos"
crear_secreto() { # nombre valor
  if existe gcloud secrets describe "$1"; then
    echo "    secreto '$1' ya existe, se conserva"
  else
    printf '%s' "$2" | gcloud secrets create "$1" --data-file=- --replication-policy=automatic
  fi
}
if ! existe gcloud secrets describe db-password; then
  DB_PASS=$(openssl rand -base64 32 | tr -d '/+=' | cut -c1-32)
  if gcloud sql users list --instance="$SQL_INSTANCE" --format='value(name)' | grep -qx "$DB_USER"; then
    gcloud sql users set-password "$DB_USER" --instance="$SQL_INSTANCE" --password="$DB_PASS"
  else
    gcloud sql users create "$DB_USER" --instance="$SQL_INSTANCE" --password="$DB_PASS"
  fi
  crear_secreto db-password "$DB_PASS"
fi
crear_secreto jwt-secret "$(openssl rand -base64 48)"
if ! existe gcloud secrets describe admin-password; then
  while true; do
    read -r -s -p "    Contraseña para el administrador ($ADMIN_EMAIL), mínimo 12 caracteres: " ADMIN_PASS; echo
    [ ${#ADMIN_PASS} -ge 12 ] && break
    echo "    Muy corta, intenta de nuevo."
  done
  crear_secreto admin-password "$ADMIN_PASS"
fi

echo "==> 5/7 Bucket para las evidencias (Cloud Storage, privado)"
existe gcloud storage buckets describe "gs://$BUCKET" || \
  gcloud storage buckets create "gs://$BUCKET" --location="$REGION" \
    --uniform-bucket-level-access --public-access-prevention

echo "==> 6/7 Cuenta de servicio de Cloud Run y sus permisos"
if ! existe gcloud iam service-accounts describe "$SA"; then
  gcloud iam service-accounts create "$SA_NAME" --display-name="TDIs Cloud Run"
  sleep 15 # dar tiempo a que la cuenta nueva sea visible para IAM
fi
for role in roles/cloudsql.client roles/secretmanager.secretAccessor roles/run.invoker; do
  gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:$SA" --role="$role" --condition=None >/dev/null
done
gcloud storage buckets add-iam-policy-binding "gs://$BUCKET" \
  --member="serviceAccount:$SA" --role=roles/storage.objectUser >/dev/null

echo "==> 7/7 Permisos para Cloud Build"
BUILD_SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"
for role in roles/artifactregistry.writer roles/logging.logWriter roles/storage.objectViewer; do
  gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:$BUILD_SA" --role="$role" --condition=None >/dev/null || \
    echo "    (no se pudo asignar $role a $BUILD_SA; si el build falla por permisos, revisa la guía)"
done

echo
echo "Infraestructura lista. Siguiente paso:  bash deploy/02-desplegar.sh"
