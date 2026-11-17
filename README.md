# Spring — Backend

API de **Spring**, una app para seguir el ciclo menstrual. Maneja las cuentas, guarda los
registros y consulta a la IA (Gemini) sin exponer su clave al navegador.

- **App en línea:** https://spring-calendario.vercel.app
- **Cuenta demo:** `demo@spring.app` / `Spring1234`
- **Contrato de la API (Swagger):** https://spring-backend-veif.onrender.com/docs.html
- **Frontend:** https://github.com/evelinpulsara/Spring_Calendario

> La primera petición puede tardar unos 50 s mientras Render enciende el servidor.

## Cómo funciona

Spring Boot 4 con Java 21, PostgreSQL en Supabase (tablas creadas por Flyway) y sesión con
JWT. Cada usuaria solo puede ver y cambiar sus propios datos.

| Paquete | Responsabilidad |
|---------|-----------------|
| `usuarios` | Registro, inicio de sesión y perfil |
| `registros` | Días de periodo, síntomas y relaciones |
| `ai` | Consejos y predicción con Gemini; corrige fechas poco realistas |
| `tiemporeal` | Avisos en vivo por SSE |
| `seguridad` | BCrypt, JWT y CORS |

## Temas del curso

| Tema | Qué se implementó | Dónde |
|------|-------------------|-------|
| 2. Contract-first | `openapi.yaml` es la fuente de verdad; una prueba falla si el código y el contrato no coinciden | `static/openapi.yaml`, `ContractTest` |
| 5. Tiempo real y escalabilidad | SSE con ticket de un solo uso, fan-out a todos los dispositivos de la usuaria, latido cada 25 s y máximo 5 conexiones; preparado para Redis Pub/Sub | `tiemporeal/` |

## POO y patrones de diseño

| Patrón | Dónde |
|--------|-------|
| Herencia | `RegistroDiario` → `DiaPeriodo`, `Sintoma`, `Relacion` |
| Template Method | `AiJsonService` → `InsightService`, `PredictionService` |
| Builder | `PromptBuilder` |
| Facade (capa de servicios) | `RegistrosService`, `AuthService`, `PerfilService` |
| Repository genérico | `RegistroDiarioRepository<T>` |
| Strategy | `AiTextGenerator` (implementado por `GeminiClient`) |
| Observer | `EventBus` / `InMemoryEventBus` |

Más detalle en [TEMAS_DEL_CURSO.md](TEMAS_DEL_CURSO.md).

## Ejecutar

```bash
./mvnw spring-boot:run   # sin DB_URL usa una base local, no hay que instalar nada
./mvnw test              # pruebas
```
