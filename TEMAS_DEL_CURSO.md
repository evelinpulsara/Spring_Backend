# Temas del curso aplicados — Backend (Spring_Backend)

Curso: Programación Orientada a la Web. El frontend (repo Spring_Calendario) tiene
su propio `TEMAS_DEL_CURSO.md` con la otra mitad.

## Tema 5 — Tiempo real en la web y escalabilidad

**Qué hace:** si la usuaria cambia un registro en el celular, la computadora (u otra
pestaña) se actualiza sola al instante, sin recargar ni hacer polling.

| Concepto del curso | Dónde está |
|---|---|
| **SSE (Server-Sent Events)**, `text/event-stream` | `tiemporeal/EventsController.java` → `GET /api/events/stream` |
| **¿Por qué SSE y no WebSockets?** | La comunicación es de una sola vía (servidor → app). La app guarda con REST normal; solo necesita *enterarse* de cambios. SSE va sobre HTTP normal, se reconecta solo y no necesita el *Upgrade* a TCP full-duplex. |
| **¿Por qué no Short/Long Polling?** | Polling pregunta cada X segundos aunque no haya cambios (sobrecarga de headers); long polling ocupa un hilo por petición. SSE solo envía cuando hay un cambio. |
| **Patrón Fan-Out** | `tiemporeal/InMemoryEventBus.java#publish`: un cambio se envía a TODAS las conexiones abiertas de esa usuaria (y a nadie más). |
| **Connection State / C10K** | Cada conexión SSE ocupa memoria y un descriptor de archivo. Por eso: máximo 5 conexiones por usuaria, limpieza al cerrar y *heartbeat* cada 25 s (`@Scheduled`) para que los proxies no corten la conexión. |
| **Escalado horizontal: Backplane Pub/Sub vs Sticky Sessions** | `tiemporeal/EventBus.java` es una interfaz. Hoy hay 1 instancia (Render gratis) → `InMemoryEventBus`. Con varias instancias se cambia por una implementación con **Redis Pub/Sub o Kafka** (cada servidor reenvía a sus propias conexiones). Se descartan *sticky sessions* (desbalanceo y pérdida de conexiones si cae un nodo). |
| Evento después del commit | `tiemporeal/RecordChangePublisher.java`: el aviso sale *after commit*, así nadie recarga datos viejos. |
| Seguridad del stream | `EventSource` no puede mandar el header `Authorization`. En vez de poner el JWT en la URL, la app pide un **ticket de un solo uso que dura 30 s** (`POST /api/events/ticket`, `StreamTicketService.java`). |

**Pruebas:** `RealtimeTest.java` (fan-out a 2 conexiones de la misma usuaria, aislamiento
entre usuarias, ticket de un solo uso y vencimiento).

## Tema 2 — Paradigmas de comunicación y diseño de APIs

| Concepto | Dónde está |
|---|---|
| **REST** orientado a recursos | `/api/period-days/{date}`, `/api/symptoms/{date}`, `/api/intimacy/{date}` con GET/PUT/DELETE semánticos. |
| **Contract-First / SSOT con OpenAPI** | `src/main/resources/static/openapi.yaml` es la fuente única de verdad de la API. Se ve con Swagger UI en **`/docs.html`** (ej. https://spring-backend-veif.onrender.com/docs.html). |
| El contrato se hace cumplir | `ContractTest.java` falla si el código tiene un endpoint que no está en el contrato o al revés. |
| **Validación en runtime** | Bean Validation (`@Valid`, `@Pattern`, `@Min`/`@Max`, `@Size`) en los DTOs: lo que no cumple el contrato responde 400. El frontend además *parsea* las respuestas (ver su documento). |
| **¿Por qué no gRPC / gRPC-Web?** | gRPC en el navegador necesita un proxy (Envoy) que traduzca a gRPC-Web; Render gratis y Vercel no lo permiten fácilmente, y la app no tiene alta carga ni streaming bidireccional. REST + contrato OpenAPI da contratos estrictos sin esa infraestructura. |
| **¿Por qué no GraphQL / tRPC?** | GraphQL complica la caché HTTP y agrega riesgo N+1 para pocos recursos fijos. tRPC exige TypeScript en ambos lados y monorepo; aquí son Java + Dart en repos separados. |

## Cómo mostrarlo en clase

1. Abre `/docs.html` del backend → el contrato completo, con el apartado "Tiempo real".
2. Abre la app en dos pestañas (o celular + computadora) con la misma cuenta.
3. Registra un síntoma en una → aparece solo en la otra. En **Perfil → Rendimiento y
   tiempo real** sube el contador "Cambios recibidos".
4. En DevTools → Network → `stream?ticket=...` → pestaña **EventStream** se ven los eventos.
