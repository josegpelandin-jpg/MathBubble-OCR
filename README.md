# MathBubble OCR

Prototipo Android para reconocer operaciones matemáticas visibles en pantalla y resolverlas localmente.

## Funciones
- Burbuja flotante.
- Captura de pantalla autorizada mediante MediaProjection.
- OCR local con Google ML Kit Text Recognition.
- Operaciones: +, -, ×, ÷, *, /, ^, paréntesis, decimales y negativos.
- Busca números visibles que coincidan con el resultado.
- Muestra la respuesta en la burbuja.
- Modo AUTO opcional: usa AccessibilityService.dispatchGesture para tocar las coordenadas de la respuesta detectada.

## Flujo
1. Instala y abre la app.
2. Concede "Mostrar sobre otras apps".
3. Activa MathBubble en Ajustes > Accesibilidad.
4. Pulsa "Autorizar captura e iniciar".
5. Android mostrará su diálogo oficial de captura de pantalla.
6. Abre el juego.
7. Toca la burbuja para alternar AUTO/OFF.

## Codemagic
Sube esta carpeta a un repositorio Git conectado a Codemagic. El archivo `codemagic.yaml`
genera un APK debug en `app/build/outputs/apk/debug/`.

## Notas
- El OCR depende de que la operación y las respuestas sean visualmente legibles.
- La selección automática usa coordenadas de pantalla; interfaces animadas o que cambien muy rápido pueden requerir ajuste.
- El reconocimiento se limita a expresiones aritméticas. No pretende resolver matemática simbólica arbitraria.
- Usa la automatización únicamente donde esté permitida.


## V5 Math Auto
- Operaciones combinadas: +, -, x, /, potencias, parentesis, decimales y negativos.
- Soporte adicional para raiz cuadrada, porcentajes, superindices 2/3 y fracciones Unicode comunes.
- Busca una respuesta numerica coincidente fuera de la linea de la operacion y puede tocarla en modo AUTO.
- Toque corto en la burbuja: AUTO/OFF.
- La pausa por pulsacion larga fue eliminada. Un toque activa o desactiva AUTO.
- Si OCR encuentra una linea etiquetada Puntos/Points/PTS/Score, muestra una vista previa x100 en la burbuja. No modifica el puntaje de otra aplicacion.
