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

# IA para revisar evidencias (Gemini en Vertex AI)
#   IA_PROVEEDOR: "gemini" o "ninguno" (todo lo revisa el admin)
#   IA_MODO:      "recomendar" -> la IA opina y el admin decide
#                 "automatico" -> la IA aprueba/rechaza sola si su confianza >= IA_CONFIANZA_MINIMA
IA_PROVEEDOR="gemini"
IA_MODO="recomendar"
IA_CONFIANZA_MINIMA="0.85"

# ---------------------------------------------------------------------
#  Normalmente no necesitas cambiar nada de aquí para abajo
# ---------------------------------------------------------------------
REPO="tdis"                          # Artifact Registry
SQL_INSTANCE="tdis-db"               # Cloud SQL
DB_USER="tdis"
SA_NAME="tdis-run"                   # cuenta de servicio de Cloud Run
BUCKET="${PROJECT_ID}-evidencias"    # Cloud Storage
