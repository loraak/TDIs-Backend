#!/usr/bin/env bash
# =====================================================================
#  Compila, construye las imágenes y despliega los 6 servicios en Cloud Run.
#  Se corre cada vez que quieras publicar cambios.
#    bash deploy/02-desplegar.sh              -> compila y despliega
#    bash deploy/02-desplegar.sh --sin-build  -> solo redespliega (mismas imágenes)
# =====================================================================
set -euo pipefail
cd "$(dirname "$0")"
source ./config.sh

gcloud config set project "$PROJECT_ID" >/dev/null
PROJECT_NUMBER=$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')
SA="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
INSTANCE=$(gcloud sql instances describe "$SQL_INSTANCE" --format='value(connectionName)')
IMG="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}"

# URL que Cloud Run le da a cada servicio (se conoce antes de desplegar)
url()    { echo "https://$1-${PROJECT_NUMBER}.${REGION}.run.app"; }
# Conexión a Cloud SQL con el conector oficial (sin IPs ni certificados)
db_url() { echo "jdbc:postgresql:///$1?cloudSqlInstance=${INSTANCE}&socketFactory=com.google.cloud.sql.postgres.SocketFactory"; }

if [ "${1:-}" != "--sin-build" ]; then
  echo "==> Compilando y construyendo imágenes en Cloud Build (5-10 min)"
  (cd .. && gcloud builds submit --config cloudbuild.yaml \
     --substitutions=_REGION="$REGION",_REPO="$REPO")
fi

URLS="USUARIOS_URL=$(url usuarios-service),CATALOGO_URL=$(url catalogo-service),TRAMITES_URL=$(url tramites-service),PROGRESO_URL=$(url progreso-service),DOCUMENTOS_URL=$(url documentos-service)"
BASE="SPRING_PROFILES_ACTIVE=prod,$URLS"
CATALOGO_MODO=$([ "$CARGAR_CATALOGO_INICIAL" = "si" ] && echo always || echo never)

# Común a todos: 1 vCPU, 1 GiB, arranque acelerado, se apagan sin uso,
# máximo 2 instancias (controla costo y conexiones a la BD)
COMUNES=(--region="$REGION" --service-account="$SA" --cpu=1 --memory=1Gi
         --cpu-boost --min-instances=0 --max-instances=2 --quiet)

desplegar() { # servicio [flags...]
  local svc=$1; shift
  echo "==> Desplegando $svc"
  gcloud run deploy "$svc" --image="$IMG/$svc:latest" "${COMUNES[@]}" "$@"
}

# ---- Servicios internos: PRIVADOS (solo los llama otro servicio con ID token)
desplegar usuarios-service --no-allow-unauthenticated \
  --set-env-vars="$BASE,DATABASE_URL=$(db_url tdis_usuarios),DB_USERNAME=$DB_USER,ADMIN_EMAIL=$ADMIN_EMAIL" \
  --set-secrets="DB_PASSWORD=db-password:latest,JWT_SECRET=jwt-secret:latest,ADMIN_PASSWORD=admin-password:latest"

desplegar catalogo-service --no-allow-unauthenticated \
  --set-env-vars="$BASE,DATABASE_URL=$(db_url tdis_catalogo),DB_USERNAME=$DB_USER,CATALOGO_CARGA_INICIAL=$CATALOGO_MODO" \
  --set-secrets="DB_PASSWORD=db-password:latest"

desplegar tramites-service --no-allow-unauthenticated \
  --set-env-vars="$BASE,DATABASE_URL=$(db_url tdis_tramites),DB_USERNAME=$DB_USER,IA_PROVEEDOR=${IA_PROVEEDOR:-ninguno},IA_MODO=${IA_MODO:-recomendar},IA_CONFIANZA_MINIMA=${IA_CONFIANZA_MINIMA:-0.85},GCP_PROJECT_ID=$PROJECT_ID" \
  --set-secrets="DB_PASSWORD=db-password:latest"

# El bucket se monta como carpeta /mnt/evidencias (solo hace falta la primera vez)
VOLUMEN=()
if ! gcloud run services describe documentos-service --region="$REGION" >/dev/null 2>&1; then
  VOLUMEN=(--execution-environment=gen2
           --add-volume="name=evidencias,type=cloud-storage,bucket=$BUCKET"
           --add-volume-mount="volume=evidencias,mount-path=/mnt/evidencias")
fi
desplegar documentos-service --no-allow-unauthenticated "${VOLUMEN[@]}" \
  --set-env-vars="$BASE,DATABASE_URL=$(db_url tdis_documentos),DB_USERNAME=$DB_USER,UPLOAD_DIR=/mnt/evidencias" \
  --set-secrets="DB_PASSWORD=db-password:latest"

desplegar progreso-service --no-allow-unauthenticated \
  --set-env-vars="$BASE"

# ---- Gateway: el ÚNICO servicio público
desplegar api-gateway --allow-unauthenticated \
  --set-env-vars="$BASE,CORS_ALLOWED_ORIGINS=$FRONTEND_URL" \
  --set-secrets="JWT_SECRET=jwt-secret:latest"

echo
echo "=================================================================="
echo " Listo. URL pública del backend:"
echo "   $(url api-gateway)"
echo
echo " En el frontend, environment.prod.ts debe tener:"
echo "   apiUrl: '$(url api-gateway)/api'"
echo "=================================================================="
