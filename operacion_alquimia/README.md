# Parcial_Final_Cs2
# Operación Alquimia — Protocolo Savitar
## Diseño e Implementación de Solución de Software (DDD + Arquitectura Hexagonal)
Proyecto de parcial de construccion de software II

**Operación Alquimia - Protocolo Savitar**, diseñada e implementada bajo **Domain-Driven Design (DDD)** y **Arquitectura Hexagonal (Puertos y Adaptadores)** en **Java 17 / Spring Boot**.

---

## 1. Dispositivos de Vigilancia (Coordenadas Fijas)

| Dispositivo | Coordenada X | Coordenada Y |
|---|---|---|
| **STAR Labs** | `-500` | `-200` |
| **CCPD** | `100` | `-100` |
| **S.T.A.R. Satellite** | `500` | `100` |

---

## 2. Requerimientos Funcionales (F1 a F6)

| Req. | Operación | Método HTTP | Endpoint | Descripción |
|---|---|---|---|---|
| **F1** | Procesamiento Completo | `POST` | `/alquimia` | Recibe la información simultánea de los 3 dispositivos, calcula la ubicación mediante trilateración y reconstruye el mensaje. |
| **F2** | Registro Individual | `POST` | `/alquimia/{device}` | Registra independientemente la distancia y mensaje de un dispositivo. |
| **F3** | Consulta Acumulada | `GET` | `/alquimia` | Si se han registrado los 3 dispositivos, calcula la posición y mensaje; si faltan datos, retorna HTTP 404. |
| **F4** | Reemplazo Completo | `PUT` | `/alquimia/{device}` | Sustituye completamente los datos anteriores de ese dispositivo sin afectar a los demás. |
| **F5** | Actualización Parcial | `PATCH` | `/alquimia/{device}` | Modifica solo la distancia, el mensaje o ambos; conserva los valores no enviados. |
| **F6** | Eliminación | `DELETE` | `/alquimia/{device}` | Elimina el dispositivo de la memoria temporal sin alterar los demás. |

---

## 3. Fundamento Matemático y Algorítmico

### A. Trilateración 2D (`TrilaterationService`)
Dadas las 3 ecuaciones circulares $(x - x_i)^2 + (y - y_i)^2 = r_i^2$, restando la ecuación 2 de la 1, y la 3 de la 2, se obtiene un sistema lineal de $2 \times 2$:
$$A x + B y = C$$
$$D x + E y = F$$
Donde:
* $A = 2(x_2 - x_1)$
* $B = 2(y_2 - y_1)$
* $C = r_1^2 - r_2^2 - x_1^2 - y_1^2 + x_2^2 + y_2^2$
* $D = 2(x_3 - x_2)$
* $E = 2(y_3 - y_2)$
* $F = r_2^2 - r_3^2 - x_2^2 - y_2^2 + x_3^2 + y_3^2$

Resolviendo por regla de Cramer con determinante $\Delta = AE - BD \neq 0$:
$$x = \frac{CE - BF}{\Delta}, \quad y = \frac{AF - CD}{\Delta}$$

### B. Reconstrucción de Mensaje (`MessageReconstructionService`)
* Se descartan cadenas vacías `""`.
* Se alinean los desfases en listas de longitudes desiguales tomando el sufijo común.
* Se valida que no existan contradicciones léxicas entre dispositivos en una misma posición.

---

## 4. Estructura Hexagonal del Código

```text
src/main/java/application/alquimia/
├── domain/                                  <-- NÚCLEO DE DOMINIO (Puro Java)
│   ├── models/
│   │   ├── Position.java                    (Value Object: x, y)
│   │   ├── KnownDevice.java                 (Catálogo de dispositivos y posiciones)
│   │   ├── DeviceData.java                  (Entidad: distancia, mensaje)
│   │   └── AlchemyResult.java               (Resultado: posición y mensaje)
│   ├── ports/out/
│   │   └── DeviceDataRepositoryPort.java    (Contrato de persistencia volátil)
│   ├── services/
│   │   ├── TrilaterationService.java        (Matemática de trilateración)
│   │   ├── MessageReconstructionService.java(Algoritmo de mezcla y limpieza)
│   │   └── SavitarProtocolService.java      (Casos de uso F1 a F6)
│   └── exceptions/
│       ├── InsufficientDataException.java
│       ├── LocationCalculationException.java
│       ├── MessageReconstructionException.java
│       └── InvalidDeviceException.java
└── infrastructure/                          <-- ADAPTADORES EXTERNOS
    ├── adapters/in/rest/
    │   ├── AlquimiaRestController.java      (Controlador REST F1 a F6)
    │   ├── AlquimiaExceptionHandler.java    (Mapeo de códigos HTTP 400 y 404)
    │   └── dtos/                            (DTOs de entrada y salida)
    └── adapters/out/memory/
        └── InMemoryDeviceDataRepository.java(Persistencia volátil en ConcurrentHashMap)
```
