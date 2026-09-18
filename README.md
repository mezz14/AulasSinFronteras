# Aulas Sin Fronteras 🎓

App Android nativa (Kotlin + Jetpack Compose) para coordinar la agenda
académica, avisos en tiempo real y la presencia docente en el campus,
usando **Firebase** (Auth, Firestore, Cloud Messaging) como backend.

Este proyecto implementa la especificación simplificada: sin Room ni
WorkManager, MVVM estándar (UI ↔ ViewModel ↔ Repository ↔ Firebase) y
una sola interfaz adaptativa según el rol (Administrador / Profesor / Alumno).

## 📂 Estructura del proyecto

```
app/src/main/java/com/aulasinfronteras/app/
├── data/
│   ├── model/          Usuario, Evento, Aviso, Presencia
│   └── repository/     AuthRepository, EventoRepository, AvisoRepository, PresenciaRepository
├── di/                  AppModule (Hilt: provee FirebaseAuth/Firestore/Messaging)
├── service/             AulasFirebaseMessagingService (push FCM)
├── ui/
│   ├── theme/           Theme.kt, Color.kt, Type.kt
│   ├── navigation/      NavGraph.kt (bottom bar + rutas)
│   └── screens/
│       ├── login/
│       ├── calendario/  (3.1 – Calendario de Campus)
│       ├── presencia/   (3.2 – Live Status Lite)
│       ├── avisos/      (3.3 – Notificaciones Push e In-App)
│       └── perfil/
├── AulasApplication.kt  (PersistentCacheSettings + canales de notificación)
└── MainActivity.kt
```

## ⚙️ Pasos para abrir y ejecutar el proyecto

### 1. Abrir en Android Studio
Descomprime el `.zip` y ábrelo con **Android Studio Koala (2024.1) o
más reciente** — `File > Open` y selecciona la carpeta `AulasSinFronteras`.
Al abrirlo, Android Studio detectará que falta el binario
`gradle/wrapper/gradle-wrapper.jar` (no se puede distribuir binario en este
paquete) y te ofrecerá **regenerarlo automáticamente** al sincronizar
("Sync Project with Gradle Files"). Si prefieres generarlo tú mismo con
Gradle instalado localmente, ejecuta una vez:
```bash
gradle wrapper --gradle-version 8.7
```

### 2. Crear el proyecto en Firebase
1. Ve a [Firebase Console](https://console.firebase.google.com/) y crea un proyecto nuevo.
2. Agrega una app Android con el **applicationId**: `com.aulasinfronteras.app`.
3. Descarga el archivo `google-services.json` generado.
4. Cópialo dentro de `app/` (junto a `build.gradle.kts`), es decir:
   `app/google-services.json`.
   > Este archivo **no** se incluye en el .zip por seguridad; el proyecto
   > no compilará sin él (lo excluye `.gitignore`).

### 3. Habilitar servicios de Firebase
En la consola de Firebase, habilita:
- **Authentication** → método "Correo electrónico/contraseña".
- **Firestore Database** → crear en modo producción o prueba.
- **Cloud Messaging** → no requiere configuración adicional para empezar.

Sube las reglas de seguridad de ejemplo incluidas en `firestore.rules`
(Firestore Database → pestaña "Reglas").

### 4. Estructura de datos sugerida en Firestore
- `usuarios/{uid}` → `{ nombre, email, rol: "ADMINISTRADOR"|"PROFESOR"|"ALUMNO", materiasMatriculadas: [], materiasImpartidas: [] }`
- `eventos/{id}` → `{ titulo, materiaId, materiaNombre, aula, enlaceVirtual, fechaInicio, fechaFin, creadoPorUid }`
- `avisos/{id}` → `{ titulo, contenido, canal: "NOTICIAS"|"CAMBIOS_AULA"|"URGENTE", materiaId, creadoPorUid, creadoPorNombre, fechaCreacion }`
- `presencia/{profesorUid}` → `{ profesorNombre, materiaId, materiaNombre, estado, ubicacion, enlaceVirtual, actualizadoEn }`

Puedes crear manualmente el primer usuario **Administrador** en la
consola de Firestore (colección `usuarios`, documento con el mismo
`uid` que en Authentication) o usar la función `registrar()` del
`LoginViewModel` desde una pantalla temporal de pruebas.

### 5. Envío real de notificaciones push (opcional, recomendado)
El cliente Android se suscribe a los topics de FCM
(`materia_{id}`, `noticias`, `cambios_aula`, `urgente`), pero **el envío
del push en sí** normalmente se dispara desde el backend con una
**Cloud Function** `onCreate`/`onWrite` sobre las colecciones `avisos` y
`presencia`, usando el Admin SDK. Este paquete no incluye las Cloud
Functions (quedan fuera del alcance de la app Android), pero el modelo
de datos y los topics ya están listos para conectarlas.

### 6. Compilar y ejecutar
Con `google-services.json` en su lugar, sincroniza Gradle y ejecuta la
app en un emulador o dispositivo físico (▶ Run 'app').

## 🧩 Tecnologías usadas
| Capa | Tecnología |
|---|---|
| Lenguaje / UI | Kotlin + Jetpack Compose |
| Arquitectura | MVVM (UI ↔ ViewModel ↔ Repository ↔ Firebase) |
| Backend | Firebase Auth + Firestore + Cloud Messaging |
| DI | Hilt |
| Persistencia offline | `PersistentCacheSettings` de Firestore |

## 📝 Notas
- `minSdk 24`, `targetSdk 34`, `compileSdk 34`.
- Los tres roles comparten la misma UI; los botones de edición se
  muestran u ocultan según `usuario.isAdmin` / `usuario.isTeacher`.
- El ícono de la app es un placeholder simple (birrete estilizado);
  reemplázalo en `app/src/main/res/drawable/ic_launcher_*.xml` o usa el
  Image Asset Studio de Android Studio (clic derecho en `res` > `New` >
  `Image Asset`) para generar uno definitivo.
