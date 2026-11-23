# Simulador de la Jerarquía de Chomsky

Aplicación de escritorio en Java (Swing) con interfaz gráfica interactiva para simular y validar modelos computacionales correspondientes a los cuatro niveles de la Jerarquía de Chomsky 

## Requisitos e instalación

- **Java JDK** (Java 17 o superior recomendado).
- **Eclipse IDE** (o cualquier IDE compatible con proyectos Java).

### Pasos para ejecutar en Eclipse
1. Clona o descarga el repositorio y extrae el contenido.
2. Abre **Eclipse IDE** y ve a **File > Import... > General > Existing Projects into Workspace**.
3. Selecciona la carpeta `JerarquiaChomsky` y haz clic en **Finish**[cite: 1].
4. Abre `src/Chomsky/Main.java`, haz clic derecho y selecciona **Run As > Java Application**.

## Manual de usuario

Al iniciar el programa se abre una ventana principal con cuatro pestañas correspondientes a cada nivel:

1. **Seleccionar el módulo / lenguaje:** Usa el selector **Lenguaje** en la parte superior para elegir un autómata/máquina predefinido o el modo editable (en Nivel 3).
2. **Ingresar o elegir una cadena:** Escribe una cadena en el campo **Cadena** o haz clic en alguno de los botones rápidos en **Ejemplos** (marcados con `✓` para cadenas válidas y `✗` para cadenas no válidas).
3. **Controlar la simulación:**
   - **Reiniciar:** Restablece el estado inicial, la cinta y la pila.
   - **Paso ›:** Ejecuta una única transición para inspeccionar el cambio paso a paso.
   - **▶ Ejecutar / ❚❚ Pausar:** Ejecuta la animación automática según el deslizador **Velocidad**.
4. **Revisar resultados:** Observa la representación gráfica central, el indicador de aceptación/rechazo y el registro de transiciones (`δ`) en la parte inferior.

## Módulos y ejemplos de prueba por nivel

### Nivel 3 · Lenguajes Regulares (AFD)
Visualiza los estados, estados finales (doble borde), transiciones y el estado actual al leer la cadena símbolo a símbolo.
- **Cadenas binarias que terminan en 01:** Acepta `01`, `1101`, `0101`; rechaza `10`, `0`, `ε`.
- **Cantidad par de 1s:** Acepta `ε`, `1001`, `0110`; rechaza `111`, `10`.
- **Contiene la subcadena aba:** Acepta `baba`, `aabab`; rechaza `abba`, `bbb`.
- **Personalizado:** Permite editar `inicio`, `final` y transiciones `origen,símbolo,destino` en tiempo real.

### Nivel 2 · Lenguajes Libres de Contexto (Autómata de Pila / LIFO)
Muestra el consumo de la cadena de entrada y la evolución visual de la pila con `Z` como marcador de fondo y el tope resaltado.
- **a^n b^n (n >= 0):** Acepta `ε`, `aabb`, `aaabbb`; rechaza `aab`, `abab`, `ba`.
- **Paréntesis balanceados `( ) [ ] { }`:** Acepta `([]{})`, `(()())`; rechaza `(]`, `(()`, `())`.
- **w c w^R:** Acepta `abcba`, `abbcbba`, `c`; rechaza `abcab`, `abab`.

### Nivel 1 · Lenguajes Sensibles al Contexto (Autómata Linealmente Acotado)
Representa la cinta delimitada por `⊢` y `⊣`, el movimiento bidireccional del cabezal y la tabla de transiciones.
- **a^n b^n c^n:** Acepta `ε`, `abc`, `aabbcc`, `aaabbbccc`; rechaza `aabbc`, `abcc`, `acb`.

### Nivel 0 · Máquina de Turing
Muestra la cinta extendible con blancos `_`, el alfabeto de la cinta, el cabezal de lectura/escritura, el estado activo, el conteo de pasos y la tabla de transiciones.
- **Incrementar un número binario (+1):** Acepta y transforma `1011` -> `1100`, `111` -> `1000`, `0` -> `1`.
- **Suma unaria:** Acepta y opera `111+11` -> `11111`, `1+1` -> `11`.
- **Acepta 0^n 1^n:** Acepta `0011`, `01`, `ε`; rechaza `001`, `0101`.

## Capturas de pantalla

- `docs/nivel3.png` — Nivel 3 (Regulares / AFD)
- `docs/nivel2.png` — Nivel 2 (Libres de contexto / Pila)
- `docs/nivel1.png` — Nivel 1 (Sensibles al contexto / LBA)
- `docs/nivel0.png` — Nivel 0 (Máquina de Turing)