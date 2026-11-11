# Spring Backend

Backend de **Spring Calendario** (seguimiento del ciclo menstrual y bienestar).
Frontend (Flutter): https://github.com/evelinpulsara/Spring_Calendario

## Asistente Spring (IA con Gemini)

La app Flutter pide consejos al backend, y el backend consulta a **Google Gemini**.
La clave de la API se queda solo en el backend: nunca llega al navegador.

```
App Flutter  --POST /api/ai/insight-->  Spring Boot  --generateContent-->  Gemini
```

### Cómo encenderlo

1. Saca una clave gratis en https://aistudio.google.com/apikey
2. Copia `.env.example` como `.env` y pega tu clave en `GEMINI_API_KEY`.
3. Enciende el backend:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Comprueba que está encendido: http://localhost:8080/api/health

## Base de datos

El backend guarda usuarias y registros en **PostgreSQL**. Las tablas las crea
**Flyway** automáticamente al arrancar (`src/main/resources/db/migration`):

| Tabla | Qué guarda |
|-------|------------|
| `usuarios` | Cuenta (contraseña cifrada con BCrypt) y respuestas del cuestionario del ciclo |
| `dias_periodo` | Días de menstruación y su flujo |
| `sintomas` | Síntomas por día (intensidad 1–5) |
| `relaciones` | Relaciones registradas y tipo de protección |

- **En tu computadora** (sin `DB_URL`): usa una base local en el archivo `./data/spring-db`
  (no se sube a GitHub). No hay que instalar nada.
- **En producción**: PostgreSQL de **Supabase** (variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

Al arrancar se crea la cuenta de prueba **demo@spring.app / Spring1234** con datos de
ejemplo (se desactiva con `DEMO_ENABLED=false`).

> La carpeta `database/` contiene el diseño original (diagrama y scripts) como
> referencia; el esquema que usa la app es el de `db/migration`.

## API

| Método | Ruta | Sesión |
|--------|------|--------|
| GET | `/api/health` | No |
| POST | `/api/auth/register`, `/api/auth/login` | No (devuelven el token) |
| GET / PUT | `/api/me` | Sí |
| DELETE | `/api/me/data` (borra todos los registros) | Sí |
| GET, PUT `/{fecha}`, DELETE `/{fecha}` | `/api/period-days`, `/api/intimacy` | Sí |
| GET, PUT `/{fecha}` | `/api/symptoms` | Sí |
| POST | `/api/ai/insight`, `/api/ai/prediction` | Sí |

La sesión se envía como `Authorization: Bearer <token>`. Cada usuaria solo puede ver y
modificar sus propios datos.

## Despliegue

### 1. Base de datos en Supabase
1. Entra a https://supabase.com, crea una cuenta y un **New project**.
   Guarda la **contraseña de la base de datos** que eliges.
2. En el proyecto: botón **Connect** → pestaña **Session pooler**
   (funciona con Render). Verás algo como:
   `postgresql://postgres.abcd1234:[YOUR-PASSWORD]@aws-0-us-east-1.pooler.supabase.com:5432/postgres`
3. De ahí salen las variables (no hace falta crear tablas, Flyway lo hace):
   - `DB_URL` = `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require`
   - `DB_USERNAME` = `postgres.abcd1234`
   - `DB_PASSWORD` = tu contraseña
