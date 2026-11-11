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

