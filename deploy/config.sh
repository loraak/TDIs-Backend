# =====================================================================
#  Configuración del despliegue en Google Cloud. EDITA LA PRIMERA PARTE.
# =====================================================================

# ID del proyecto en Google Cloud (lo eliges al crearlo; es único en todo GCP)
PROJECT_ID="tdis-uteq-CAMBIA-ESTO"

# Región: us-central1 es de las más baratas y donde aplica el nivel gratuito
REGION="us-central1"

# URL del frontend en Vercel, SIN "/" al final (para CORS)
FRONTEND_URL="https://tdis-frontend.vercel.app"

# Correo del administrador inicial (la contraseña la pide el script 01)
ADMIN_EMAIL="admin@uteq.edu.mx"

# "si" = cargar las 12 actividades de ejemplo si el catálogo está vacío
CARGAR_CATALOGO_INICIAL="si"

# ---------------------------------------------------------------------
#  Normalmente no necesitas cambiar nada de aquí para abajo
# ---------------------------------------------------------------------
REPO="tdis"                          # Artifact Registry
SQL_INSTANCE="tdis-db"               # Cloud SQL
DB_USER="tdis"
SA_NAME="tdis-run"                   # cuenta de servicio de Cloud Run
BUCKET="${PROJECT_ID}-evidencias"    # Cloud Storage
