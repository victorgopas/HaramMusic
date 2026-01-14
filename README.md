# HaramMusic – Guía de despliegue

Este documento describe los pasos necesarios para configurar, compilar y ejecutar la aplicación **HaramMusic** en un entorno local de desarrollo Android.

La aplicación se ejecuta desde Android Studio y utiliza archivos locales de musica del usuario para su funcionamiento.

---

## Requisitos previos

Antes de desplegar la aplicación es necesario disponer de:

- Android Studio (versión recomendada: Hedgehog o superior)
- SDK de Android instalado
    - SDK mínimo: API 26
- Un emulador Android o un dispositivo físico
  
---

## Clonar el proyecto

Clonar el repositorio desde GitHub:

```bash
git clone https://github.com/victorgopas/HaramMusic.git
```
---

## Ejecución utilizando Android Studio

- Seleccionar un emulador o un dispositivo físico desde Android Studio
- Ejecutar el proyecto utilizando el botón Run
- La aplicación se instalará automáticamente en el dispositivo
- Al iniciar la aplicación:
  - Registrese o inicie sesión
  - Acceder a la seccion "Perfil"
  - Pulse "Elegir carpeta"
  - Escoja la carpeta donde tenga canciones guardadas o donde vaya a almacenar musica.
  - Otorge permisos a la aplicación para acceder a dicha carpeta.
  - Pulse "Importar"
  - La aplicación obtendra las canciones de la carpeta y creará fichas para cada artista, álbum y canción.


