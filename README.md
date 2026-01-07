# HaramMusic – Guía de despliegue

Este documento describe los pasos necesarios para configurar, compilar y ejecutar la aplicación **HaramMusic** en un entorno local de desarrollo Android.

La aplicación se ejecuta desde Android Studio y utiliza servicios de Spotify para la reproducción de música.

---

## Requisitos previos

Antes de desplegar la aplicación es necesario disponer de:

- Android Studio (versión recomendada: Hedgehog o superior)
- SDK de Android instalado
    - SDK mínimo: API 26
- Un emulador Android o un dispositivo físico
- Cuenta de Spotify
- Aplicación oficial de Spotify instalada en el dispositivo

Nota: para la reproducción completa de canciones se recomienda una cuenta de Spotify Premium.

---

## Clonar el proyecto

Clonar el repositorio desde GitHub:

```bash
git clone https://github.com/victorgopas/HaramMusic.git
```
## Configuración de Spotify (obligatoria)

La aplicación utiliza **Spotify Web API** y **Spotify App Remote**, por lo que es necesario configurar credenciales propias antes de ejecutar la aplicación.

### 1. Crear una aplicación en Spotify Developer Dashboard

1. Acceder a la siguiente URL:  
   https://developer.spotify.com/dashboard
2. Crear una nueva aplicación
3. Obtener el **Client ID**
4. Añadir el siguiente **Redirect URI** en la configuración de la aplicación:com.bicheator.harammusic://callback

5. Guardar los cambios antes de continuar

---

### 2. Configurar el Client ID en el proyecto

En el archivo: core/di/AppContainer.kt

Sustituir el valor:

```kotlin
private val spotifyClientId = "TU_CLIENT_ID"
```

## Ejecución de la aplicación

- Seleccionar un emulador o un dispositivo físico desde Android Studio
- Ejecutar el proyecto utilizando el botón Run
- La aplicación se instalará automáticamente en el dispositivo
- Al iniciar la aplicación:
  - Registrarse o iniciar sesión
  - Acceder a la pestaña Explorar
  - Conectar con Spotify cuando se solicite
Una vez conectado Spotify, la aplicación estará lista para reproducir canciones.


