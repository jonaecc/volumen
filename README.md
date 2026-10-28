# Límite de Volumen (Android TV / Google TV)

App que corre en segundo plano dentro de la TV y baja el volumen al tope que elijas cada vez que alguien lo sube de más. Arranca sola al encender la TV.

## Cómo funciona

- `VolumeGuardService` es un servicio en primer plano. Escucha los cambios de volumen y además revisa cada 0,5 s. Si el volumen supera el tope, lo baja.
- `MainActivity` es una pantalla simple para elegir el tope (botones − / + de 5 en 5 %), ver el volumen actual y activar o desactivar el limitador.
- `BootReceiver` inicia el servicio al encender la TV.

## Obtener el APK sin instalar nada (GitHub)

1. Crea una cuenta gratis en github.com y un repositorio nuevo.
2. Sube todo el contenido de esta carpeta (botón "Add file" → "Upload files"). Asegúrate de que se suba la carpeta oculta `.github`.
3. Ve a la pestaña **Actions**, abre **Compilar APK** y espera unos minutos a que termine.
4. Entra a la ejecución terminada y descarga **LimiteVolumen-apk** (un zip que contiene `app-debug.apk`).

## Alternativa: Android Studio (en tu PC)

Abre la carpeta con Android Studio, espera la sincronización y usa **Build → Build APK(s)**.

## Instalar en la TV

Elige una opción:

- **USB:** copia el APK a una memoria USB, conéctala a la TV y ábrelo con un explorador de archivos (por ejemplo "File Commander" o "X-plore"). Si pide permiso para instalar apps de origen desconocido, actívalo.
- **Send Files to TV:** instala esta app en la TV y en tu celular, y envía el APK por Wi-Fi.
- **ADB:** activa "Opciones de desarrollador" (Ajustes → Preferencias del dispositivo → Acerca de → pulsa 7 veces "Compilación"), activa la depuración por red y desde tu PC ejecuta:
  `adb connect IP_DE_LA_TV` y luego `adb install app-debug.apk`

## Primer uso

1. Abre **Límite de Volumen** desde la lista de apps de la TV (hay que abrirla al menos una vez para que pueda arrancar sola después).
2. Elige el volumen máximo con − / +.
3. Prueba subir el volumen con el control: debería volver al tope en menos de un segundo.

## Limitaciones conocidas

- En algunos modelos el volumen lo maneja el hardware de la TV y no el sistema Android. Si el número "Volumen actual" de la pantalla no cambia al subir el volumen, el límite no podrá aplicarse en ese modelo.
- Cualquiera con el control remoto puede abrir la app y cambiar o desactivar el límite. Se puede añadir un PIN.
- Esta app no apaga la TV. Para eso conviene usar el apagado automático por inactividad que traen los ajustes de la TV.
