Quorum - Foro de Ciencia
Quorum es una aplicación móvil nativa desarrollada en Kotlin y Jetpack Compose, diseñada para fomentar la divulgación y discusión científica. La plataforma permite a los usuarios compartir artículos, noticias y preguntas sobre ciencia en tiempo real.

Integrantes del Equipo:
-Karla Hoch
-Sebastian Huaiquimilla

Funcionalidades Principales.
La aplicación implementa una arquitectura MVVM (Model-View-ViewModel) estricta y cuenta con las siguientes características:

Autenticación y Usuarios:
Registro e Inicio de Sesión: Sistema seguro validado en tiempo real (correo y contraseña).
Perfil de Usuario: Visualización de datos del usuario y listado filtrado de sus propias publicaciones.

Foro y Gestión de Contenido (CRUD):
Lectura en Tiempo Real: Los posts se actualizan instantáneamente gracias a SnapshotListeners.
Creación de Posts: Formulario validado para nuevos temas.
Edición y Borrado: Sistema de permisos que permite modificar o eliminar solo los posts creados por el usuario actual (validación de authorId).
Favoritos: Funcionalidad para guardar/marcar posts de interés personal.


Recursos Nativos y APIs:
API Externa (NASA): Integración con la API Astronomy Picture of the Day para mostrar contenido científico destacado en el inicio.
Compartir (Share Sheet): Uso de Intent.ACTION_SEND para compartir el contenido de los posts a través de otras aplicaciones (WhatsApp, Gmail, etc.).
Notificaciones Locales: Alertas al dispositivo cuando se realiza una publicación exitosa.

Endpoints y Servicios Utilizados.

Backend (Microservicios)
El proyecto utiliza Firebase como Backend-as-a-Service (BaaS):
Firebase Authentication: Gestión de identidad y sesiones.
Cloud Firestore: Base de datos NoSQL para persistencia remota de posts y usuarios en tiempo real.

API Externa
NASA APOD API: Se consume el servicio público de la NASA para obtener la imagen astronómica del día.
Endpoint: GET https://api.nasa.gov/planetary/apod
Parámetro: api_key=DEMO_KEY


Pasos para Ejecutar:
Opción A: Instalación Directa (Para Evaluación)
Si solo deseas probar la aplicación funcional en un dispositivo Android:
1. Descarga el archivo app-release.apk que se encuentra en la carpeta raíz de este repositorio.
2. Transfiere el archivo a tu dispositivo Android.
3. Instala la aplicación (asegúrate de permitir la instalación de fuentes desconocidas si es necesario).
4. ¡Listo! La aplicación ya está configurada y conectada a los servicios de Firebase.

Opción B: Ejecutar Código Fuente (Para Desarrollo)
Si deseas abrir el proyecto en Android Studio:
1. Abrir Android Studio.
2. Clonar el Repositorio: Descargar el código fuente desde este repositorio.
3. Sincronizar el proyecto con Gradle(Tu Android Studio puede tener una versión distinta que la App)
4. Ejecutar: Seleccionar un emulador (API 24+) o dispositivo físico y presionar "Run".


