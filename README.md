# api-create-user

API RESTful para la creación y consulta de usuarios. Microservicio implementado con Spring Boot WebFlux y R2DBC (H2 en memoria). Proporciona endpoints para crear usuarios, listar usuarios y obtener un usuario por UUID. Al crear un usuario se genera un token JWT y se registran teléfonos asociados.

## Contenido
- Descripción del servicio
- Cómo desplegar (Windows)
- Cómo probar (Swagger-UI)
- Diagramas
- Estructura / paquetería

## Descripción
Este microservicio expone una API reactiva (WebFlux) para gestionar usuarios. Usa una base de datos H2 en memoria con R2DBC para persistencia durante la ejecución, y genera un token JWT por usuario. Los datos de configuración principales están en `src/main/resources/application.yml`.

## Requisitos
- JDK 21
- Maven wrapper incluido (mvnw.cmd)

## Despliegue (Windows)
1. Abrir una consola en la carpeta raíz del proyecto.
2. Construir el paquete con el wrapper de Maven:

   mvnw.cmd -DskipTests clean package

   Esto generará el JAR en `target/api-create-user-0.0.1-SNAPSHOT.jar`.

3. Ejecutar la aplicación:

   java -jar target/api-create-user-0.0.1-SNAPSHOT.jar

   Alternativamente se puede ejecutar directamente con el wrapper:

   mvnw.cmd spring-boot:run

4. La aplicación se expondrá por defecto en http://localhost:8080 (configurable en `application.yml`). La base de datos es en memoria y los scripts de creación están en `src/main/resources/scripts/h2/`.

## Probar el microservicio (Swagger-UI)
El proyecto integra springdoc OpenAPI; la interfaz Swagger-UI permite probar los endpoints de forma interactiva.

1. Arrancar la aplicación (ver pasos anteriores).
2. Abrir en el navegador:
   - Swagger UI: http://localhost:8080/swagger-ui/index.html  (también puede ser /swagger-ui.html)
   - Esquema OpenAPI: http://localhost:8080/v3/api-docs

3. Endpoints principales:
   - POST /users → Crear usuario
   - GET /users → Listar todos los usuarios
   - GET /users/id/{uuid} → Obtener usuario por UUID

4. Ejemplo de body (POST /users):
```json
{
  "name": "Juan Pérez",
  "email": "juan.perez@example.com",
  "password": "Passw0rd!",
  "phones": [
    { "number": "123456789", "cityCode": "01", "countryCode": "57" }
  ]
}
```
Notas útiles:
- Si el email ya existe o el formato no cumple la expresión regular configurada, la creación fallará con un error descriptivo.
- La respuesta de creación incluye: `uuid`, `userInformation`, `created`, `lastLogin`, `token` y `isActive`.
- Para pruebas manuales use Swagger UI: seleccione el endpoint, haga clic en "Try it out", pegue el JSON y ejecute.

## Diagramas
En la carpeta `docs/` se incluyen los diagramas relacionados:
- `docs/diagram.drawio` → archivo editable con Draw.io
- `docs/diagram.png` → diagrama general (modelo de datos / entidades)
- `docs/diagram-sequence.png` → diagrama de secuencia para el flujo de creación de usuario

Breve explicación:
- Diagrama de base de datos: muestra las tablas `User` y `Phone` y la relación entre ellas.
- Diagrama de secuencia: ilustra la invocación desde `UserController` → `UserServiceImpl` → repositorios y `JwtService` durante la creación de un usuario.

## Paquetería (estructura del proyecto)
El código está organizado bajo `com.evaluation.project` con las siguientes capas:

- controller: controladores REST (entrada HTTP)
- service.impl: implementación de la lógica de negocio (servicios)
- repository: repositorios R2DBC para persistencia
- model.dto: objetos DTO (UserRequest, UserResponse, Phone)
- model.entity: entidades mapeadas a la base de datos
- config: configuración de la aplicación (propiedades, configuración R2DBC)
- util: utilidades, excepciones y manejo de errores

Los scripts SQL iniciales están en `src/main/resources/scripts/h2/` (schema.sql, data.sql).

## Tests
El proyecto incluye pruebas unitarias con JUnit y Mockito. Para ejecutarlas:

mvnw.cmd test

(Opción: ejecutar `mvnw.cmd -DskipTests clean package` para omitir pruebas durante el empaquetado.)

## Notas finales
- Configuración del JWT y expresiones regulares están en `application.yml` bajo la sección `app.jwt` y `app.regex`.
- La base de datos H2 es en memoria; al reiniciar la aplicación los datos se perderán a menos que se configure una persistencia diferente.

