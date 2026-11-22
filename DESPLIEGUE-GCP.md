# Despliegue de TDIs en Google Cloud

```
                    Internet
                       │
   Vercel (Angular) ───┤
                       ▼
              ┌──────────────────┐   Cloud Run (público)
              │   api-gateway    │   valida JWT y roles
              └────────┬─────────┘
       ID token de Google (X-Serverless-Authorization)
   ┌──────────┬────────┼─────────┬────────────┐   Cloud Run (PRIVADOS)
   ▼          ▼        ▼         ▼            ▼
usuarios  catalogo  tramites  progreso   documentos ──► Cloud Storage
   │          │        │                      │          (evidencias)
   └──────────┴────────┴──────────────────────┘
                       ▼
            Cloud SQL Postgres (4 bases)
```

- **Solo el gateway es público.** Los demás servicios rechazan cualquier petición
  que no traiga un ID token de Google de la cuenta de servicio `tdis-run`.
- **Sin Eureka:** en producción cada servicio se llama por su URL de Cloud Run.
  En local todo sigue igual (Eureka + `start-all.ps1`).
- **Se apagan solos** cuando no hay uso. La primera petición después de un rato
  puede tardar 10–30 s (arranque en frío).
- **Secretos** (JWT, contraseña de la BD, contraseña del admin) en Secret Manager.

Costo esperado con poco uso: **~10–20 USD/mes**, casi todo de Cloud SQL.

---

## 0. Antes de empezar (una sola vez)

1. Entra a https://console.cloud.google.com con tu cuenta de Google.
2. Crea un **proyecto** (arriba a la izquierda → *Nuevo proyecto*). Anota su **ID**
   (ej. `tdis-uteq-482913`); no es lo mismo que el nombre.
3. Activa la **facturación** en ese proyecto (los clientes nuevos reciben crédito
   gratis; aun así pide tarjeta).
4. **Crea una alerta de presupuesto** para no llevarte sorpresas:
   *Facturación → Presupuestos y alertas → Crear presupuesto* → por ejemplo 20 USD,
   con avisos al 50 %, 90 % y 100 %.

## 1. Abrir Cloud Shell y subir el código

Cloud Shell es una terminal Linux en el navegador con `gcloud` ya instalado.
Botón **`>_`** arriba a la derecha de la consola.

Sube el backend de una de estas dos formas:

- **Con git** (si está en GitHub):
  ```bash
  git clone https://github.com/TU_USUARIO/TDIs-Backend.git
  cd TDIs-Backend
  ```
- **Con zip**: en Cloud Shell, menú `⋮` → *Subir* → elige el zip, y luego
  ```bash
  unzip TDIs-Backend.zip && cd TDIs-Backend
  ```

## 2. Configurar

Abre el editor (botón *Abrir editor* en Cloud Shell) y edita **`deploy/config.sh`**:

| Variable | Qué poner |
|---|---|
| `PROJECT_ID` | El ID del proyecto del paso 0 |
| `FRONTEND_URL` | La URL de tu frontend en Vercel, sin `/` al final |
| `ADMIN_EMAIL` | Correo con el que entrará el administrador |
| `CARGAR_CATALOGO_INICIAL` | `si` para empezar con las 12 actividades de ejemplo |

## 3. Crear la infraestructura (una sola vez, ~15 min)

```bash
bash deploy/01-infraestructura.sh
```

Crea: Artifact Registry, Cloud SQL (Postgres 16, `db-f1-micro`, respaldo diario),
las 4 bases, el bucket privado de evidencias, los secretos y la cuenta de
servicio con sus permisos. Te pedirá la **contraseña del administrador**
(mínimo 12 caracteres; guárdala en un lugar seguro).

Si se corta a la mitad, vuelve a correrlo: se salta lo que ya existe.

## 4. Desplegar el backend (~10 min)

```bash
bash deploy/02-desplegar.sh
```

Compila en Cloud Build, publica las imágenes y despliega los 6 servicios.
Al final imprime la **URL pública del gateway**, algo como
`https://api-gateway-123456789.us-central1.run.app`.

Para publicar cambios después: haz `git pull` (o sube el zip nuevo) y vuelve a
correr el mismo comando.

## 5. Conectar el frontend

1. En `tdis_frontend`, edita `src/environments/environment.prod.ts`:
   ```ts
   export const environment = {
     production: true,
     apiUrl: 'https://api-gateway-XXXXXXXX.us-central1.run.app/api',
   };
   ```
2. Haz commit y push: Vercel vuelve a desplegar solo.

## 6. Probar

1. Abre la URL del gateway + `/api/catalogo` en el navegador. Debe responder
   **401** (sin token): significa que el gateway está vivo y protegiendo.
2. Entra al frontend con `ADMIN_EMAIL` y la contraseña que pusiste.
   *La primera vez puede tardar ~30 s* mientras arrancan los servicios.
3. Registra un alumno de prueba, sube una evidencia y apruébala como admin.
4. Verifica que la imagen siga ahí después de redesplegar (está en el bucket).

## Comandos útiles

```bash
source deploy/config.sh && gcloud config set project $PROJECT_ID

# Ver logs de un servicio (los últimos 100)
gcloud run services logs read tramites-service --region=$REGION --limit=100

# Ver la URL y estado de todos los servicios
gcloud run services list --region=$REGION

# Cambiar la contraseña del admin: se usa solo si NO existe ningún admin
echo -n "NuevaContraseñaLarga" | gcloud secrets versions add admin-password --data-file=-
```

## Si algo falla

| Síntoma | Causa probable / solución |
|---|---|
| El build falla con `PERMISSION_DENIED` | La cuenta de Cloud Build no tiene permisos. En *IAM*, dale a `PROJECT_NUMBER-compute@developer.gserviceaccount.com` los roles *Artifact Registry Writer*, *Logs Writer* y *Storage Object Viewer*. |
| Un servicio no arranca: `Could not resolve placeholder` | Falta una variable de entorno. El nombre exacto viene en el mensaje; revisa `deploy/02-desplegar.sh`. |
| Error de conexión a la BD | Revisa que la cuenta `tdis-run` tenga *Cloud SQL Client* y que la instancia `tdis-db` esté encendida. |
| `403` / `401` en llamadas entre servicios (aparece en logs del gateway o de tramites) | La cuenta `tdis-run` necesita *Cloud Run Invoker*. Si persiste, cambia las URLs de `url()` en `02-desplegar.sh` por las que da `gcloud run services list`. |
| El navegador marca error de **CORS** | `FRONTEND_URL` no coincide exactamente con la URL de Vercel (con `https://`, sin `/` final). Corrige y corre `bash deploy/02-desplegar.sh --sin-build`. |
| La primera carga tarda mucho | Normal: arranque en frío. Si molesta, `--min-instances=1` en el gateway (cuesta más, ~10–15 USD/mes extra). |

## Costos y cómo apagar todo

- Cloud Run: dentro del nivel gratuito con poco uso.
- Cloud SQL `db-f1-micro`: unos 8–10 USD/mes + disco, **aunque no haya usuarios**.
- Cloud Storage, Secret Manager, Artifact Registry: centavos.

Para pausar la base sin borrarla:
```bash
gcloud sql instances patch tdis-db --activation-policy=NEVER    # apagar
gcloud sql instances patch tdis-db --activation-policy=ALWAYS   # encender
```
Para borrar todo y dejar de pagar: eliminar el proyecto en *IAM y administración → Configuración → Cerrar*.
