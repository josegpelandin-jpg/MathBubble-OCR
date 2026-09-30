# MathBubble OCR — FIXED

Proyecto Android nativo preparado para Codemagic. La corrección principal es incluir el módulo `app` y ejecutar `gradle :app:assembleDebug`, evitando el error anterior `Task 'assembleDebug' not found in root project`.

## Importante
Esta versión corrige la compilación y contiene la base Android con permiso de superposición, servicio de accesibilidad y dependencia ML Kit OCR. La captura OCR continua y el reconocimiento universal de expresiones requieren pruebas reales en el teléfono/juego y ajustes posteriores; no se garantiza compatibilidad con cualquier interfaz o juego.
