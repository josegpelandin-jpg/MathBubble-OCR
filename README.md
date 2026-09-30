# Security Bubble Tester

Proyecto Android Studio de prueba.

## Incluye
- Buscador/selector de aplicaciones instaladas.
- Burbuja flotante sobre otras aplicaciones.
- Panel con -1, +1, +100 y valor manual.
- Contador QA independiente.

## Importante
La selección de una aplicación **no** da acceso a su memoria ni altera sus puntos.
Este proyecto está deliberadamente limitado a un contador de prueba propio. Para auditar
una app que controles, conecta su build de desarrollo mediante una interfaz de QA
(BroadcastReceiver protegido, Binder, deep link debug-only, etc.).

## Uso
1. Abrir el proyecto en Android Studio.
2. Sincronizar Gradle.
3. Ejecutar en Android 8+.
4. Conceder "Mostrar sobre otras apps".
5. Seleccionar la aplicación.
6. Activar la burbuja.
