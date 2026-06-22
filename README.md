# FS3_Ms_Sync - Microservicio de Sincronización

Microservicio de sincronización de datos del ropero a la nube desarrollado con Spring Boot 3.5.13, Java 17 y Maven. Proporciona funcionalidades de almacenamiento y recuperación de datos de prendas de vestir con integración AWS S3.

## Características

- **Sincronización Cloud**: Almacenamiento de datos del ropero en la nube
- **Integración AWS S3**: Subida y descarga de imágenes a S3
- **API REST**: Endpoints para exportar/importar datos
- **Gestión de Imágenes**: Subida, almacenamiento y serving de imágenes
- **JWT Integration**: Compatible con el microservicio de autenticación
- **Base de Datos MySQL**: Almacenamiento de metadatos en `sync_db`
- **Docker ready**: Configurado para despliegue con Docker y docker-compose

## Tecnologías

- **Java 17**
- **Spring Boot 3.5.13**
- **Spring Data JPA** para acceso a datos
- **MySQL** como base de datos
- **AWS SDK** para integración con S3
- **Spring Web** para endpoints REST
- **Lombok** para reducir código boilerplate
- **Docker** para contenedorización

## Metadatos del Proyecto

- **Group**: duoc.fs3
- **Artifact**: ms-sync
- **Package**: duoc.fs3.ms_sync
- **Version**: 0.0.1-SNAPSHOT

## Requisitos Previos

- Java 17 o superior
- Maven 3.8+
- MySQL Server (configurado para desarrollo local o AWS RDS)
- AWS Account con S3 bucket (para producción)
- Docker y docker-compose (para despliegue con contenedores)
- IDE compatible con Java (IntelliJ IDEA, Eclipse, VS Code)

## Configuración de Base de Datos

### Desarrollo Local

1. Crear la base de datos en MySQL:
   ```sql
   CREATE DATABASE sync_db;
   ```

### Docker / AWS EC2

La aplicación usa variables de entorno para la configuración de base de datos:

- `DB_HOST`: IP del servidor MySQL (AWS EC2 o localhost para Docker local)
- `DB_PORT`: Puerto MySQL (default 3306)
- `DB_USER`: Usuario MySQL
- `DB_PASSWORD`: Contraseña MySQL

Ver sección **Despliegue con Docker** para más detalles.

## Configuración de AWS S3

Para producción, configura las siguientes variables de entorno:

- `AWS_ACCESS_KEY`: Clave de acceso de AWS
- `AWS_SECRET_KEY`: Clave secreta de AWS
- `AWS_REGION`: Región de AWS (ej: us-east-1)
- `S3_BUCKET_NAME`: Nombre del bucket S3

Para desarrollo local, puedes deshabilitar S3 configurando `aws.s3.enabled=false` en application.properties.

## Ejecución de la Aplicación

### Desde Maven

```bash
cd FS3_Ms_Sync
mvn clean install
mvn spring-boot:run
```

### Desde IDE

1. Importar el proyecto como proyecto Maven
2. Ejecutar la clase `MsSyncApplication.java`
3. La aplicación estará disponible en `http://localhost:8083`

### Despliegue con Docker

#### Construir imagen Docker
```bash
docker build -t fs3-ms-sync .
```

#### Ejecutar con docker-compose
```bash
# Copiar .env.example a .env y configurar variables
cp .env.example .env
# Editar .env con tus valores de DB y AWS

# Iniciar contenedor
docker-compose up -d
```

#### Ejecutar contenedor directamente
```bash
docker run -d -p 8083:8083 \
  -e DB_HOST=tu-db-host \
  -e DB_PORT=3306 \
  -e DB_USER=root \
  -e DB_PASSWORD=tu-password \
  -e AWS_ACCESS_KEY=tu-aws-access-key \
  -e AWS_SECRET_KEY=tu-aws-secret-key \
  -e AWS_REGION=us-east-1 \
  -e S3_BUCKET_NAME=tu-bucket-name \
  --name fs3-ms-sync \
  fs3-ms-sync
```

#### Variables de Entorno Requeridas
- `DB_HOST`: IP del servidor MySQL
- `DB_PORT`: Puerto MySQL (default 3306)
- `DB_USER`: Usuario MySQL
- `DB_PASSWORD`: Contraseña MySQL
- `AWS_ACCESS_KEY`: Clave de acceso AWS (opcional para desarrollo)
- `AWS_SECRET_KEY`: Clave secreta AWS (opcional para desarrollo)
- `AWS_REGION`: Región AWS (opcional para desarrollo)
- `S3_BUCKET_NAME`: Nombre del bucket S3 (opcional para desarrollo)

Ver archivo `.env.example` para template de configuración.

## Endpoints de la API

### Sincronización

#### Exportar/Sincronizar datos
- **POST** `/api/v1/sync/export`

#### Subir imagen
- **POST** `/api/v1/sync/upload-image`

#### Eliminar prenda
- **DELETE** `/api/v1/sync/item/{itemId}`

#### Descargar datos de la nube
- **GET** `/api/v1/sync/download`

#### Servir imagen
- **GET** `/api/v1/sync/images/{filename}`

## Configuración

### application.properties
Configuración principal incluyendo conexión a base de datos, AWS S3 y CORS. Ahora usa variables de entorno para configuración flexible.

### Clave JWT
La aplicación usa la misma clave JWT que el microservicio de autenticación para validar tokens. Asegúrate de mantener la misma clave en ambos servicios.

## Estructura del Proyecto

```
src/main/java/duoc/fs3/ms_sync/
|-- MsSyncApplication.java          # Clase principal
|-- config/                         # Configuraciones
|   |-- AwsConfig.java             # Configuración AWS S3
|   |-- WebConfig.java              # Configuración CORS
|-- controller/
|   |-- SyncController.java         # Endpoints de sincronización
|-- model/
|   |-- ClothingItem.java           # Entidad de prenda
|-- repository/
|   |-- ClothingItemRepository.java # Repositorio JPA
|-- service/
|   |-- SyncService.java            # Servicio de sincronización
|   |-- S3Service.java              # Servicio AWS S3
```

## Desarrollo

### Agregar nuevas funcionalidades

1. Crear las clases necesarias en los paquetes correspondientes
2. Agregar pruebas unitarias
3. Actualizar este README si es necesario

### Estándares de Código

- Todo el código está documentado con JavaDoc en español
- Se siguen las convenciones de nomenclatura de Java
- Las pruebas unitarias deben cubrir al menos los casos principales

## Licencia

MIT License - Ver archivo LICENSE para más detalles.

## Autor

Desarrollado por Duoc UC - Fullstack III (2026) 
