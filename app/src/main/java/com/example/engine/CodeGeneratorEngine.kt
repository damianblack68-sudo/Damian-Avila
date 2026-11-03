package com.example.engine

import com.example.domain.model.CodeProjectPayload

object CodeGeneratorEngine {

    fun isCodeRequest(input: String): Boolean {
        val lower = input.lowercase()
        return lower.contains("html") ||
                lower.contains("crear app") ||
                lower.contains("crea una app") ||
                lower.contains("programar") ||
                lower.contains("codigo") ||
                lower.contains("código") ||
                lower.contains("pagina web") ||
                lower.contains("página web") ||
                lower.contains("javascript") ||
                lower.contains("python") ||
                lower.contains("kotlin") ||
                lower.contains("css") ||
                lower.contains("desarrollo web") ||
                lower.contains("script")
    }

    fun generateCode(prompt: String): CodeProjectPayload {
        val lower = prompt.lowercase()

        return when {
            lower.contains("python") -> generatePythonProject(prompt)
            lower.contains("kotlin") || lower.contains("android") -> generateKotlinProject(prompt)
            lower.contains("calculadora") -> generateCalculatorWebApp(prompt)
            lower.contains("juego") || lower.contains("game") -> generateMiniGameWebApp(prompt)
            lower.contains("dashboard") || lower.contains("panel") || lower.contains("analisis") -> generateDashboardWebApp(prompt)
            lower.contains("tarea") || lower.contains("todo") || lower.contains("notas") -> generateTaskManagerWebApp(prompt)
            else -> generateGenericInteractiveWebApp(prompt)
        }
    }

    private fun generateCalculatorWebApp(prompt: String): CodeProjectPayload {
        val html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Onyx Calc Pro</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
                body { background: #0A0A0E; color: #FFFFFF; display: flex; justify-content: center; align-items: center; min-height: 100vh; padding: 16px; }
                .calc-card { background: #14141C; border: 1px solid #282838; border-radius: 24px; padding: 24px; width: 100%; max-width: 360px; box-shadow: 0 20px 40px rgba(0,0,0,0.6); }
                .brand { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; font-size: 13px; color: #FF2A54; font-weight: 700; letter-spacing: 1px; }
                .screen { background: #0D0D14; border-radius: 16px; padding: 20px; text-align: right; margin-bottom: 20px; border: 1px solid #1E1E2C; }
                .prev { font-size: 14px; color: #8A8D9B; min-height: 20px; overflow-x: auto; }
                .current { font-size: 36px; font-weight: 700; color: #FFFFFF; overflow-x: auto; margin-top: 4px; }
                .grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
                button { background: #1E1E2C; border: none; outline: none; border-radius: 14px; height: 56px; font-size: 18px; font-weight: 600; color: #FFFFFF; cursor: pointer; transition: all 0.15s ease; }
                button:active { transform: scale(0.94); }
                .btn-op { background: #2A1F2D; color: #FF2A54; }
                .btn-equal { background: #FF2A54; color: #FFFFFF; grid-column: span 2; font-weight: 800; }
                .btn-clear { background: #2A151A; color: #FF5370; }
              </style>
            </head>
            <body>
              <div class="calc-card">
                <div class="brand">
                  <span>ONYX CALC AI</span>
                  <span>INGENIERÍA</span>
                </div>
                <div class="screen">
                  <div class="prev" id="prevExp"></div>
                  <div class="current" id="display">0</div>
                </div>
                <div class="grid">
                  <button class="btn-clear" onclick="clearAll()">AC</button>
                  <button class="btn-op" onclick="deleteChar()">⌫</button>
                  <button class="btn-op" onclick="appendOp('/')">÷</button>
                  <button class="btn-op" onclick="appendOp('*')">×</button>
                  <button onclick="appendNum('7')">7</button>
                  <button onclick="appendNum('8')">8</button>
                  <button onclick="appendNum('9')">9</button>
                  <button class="btn-op" onclick="appendOp('-')">−</button>
                  <button onclick="appendNum('4')">4</button>
                  <button onclick="appendNum('5')">5</button>
                  <button onclick="appendNum('6')">6</button>
                  <button class="btn-op" onclick="appendOp('+')">+</button>
                  <button onclick="appendNum('1')">1</button>
                  <button onclick="appendNum('2')">2</button>
                  <button onclick="appendNum('3')">3</button>
                  <button class="btn-op" onclick="appendOp('^')">xʸ</button>
                  <button onclick="appendNum('0')">0</button>
                  <button onclick="appendNum('.')">.</button>
                  <button class="btn-equal" onclick="compute()">=</button>
                </div>
              </div>
              <script>
                let current = '0';
                let expression = '';
                function update() {
                  document.getElementById('display').innerText = current;
                  document.getElementById('prevExp').innerText = expression;
                }
                function appendNum(num) {
                  if (current === '0' && num !== '.') current = num;
                  else current += num;
                  update();
                }
                function appendOp(op) {
                  expression = current + ' ' + op + ' ';
                  current = '0';
                  update();
                }
                function clearAll() {
                  current = '0';
                  expression = '';
                  update();
                }
                function deleteChar() {
                  current = current.length > 1 ? current.slice(0, -1) : '0';
                  update();
                }
                function compute() {
                  try {
                    let full = (expression + current).replace(/÷/g, '/').replace(/×/g, '*').replace(/\^/g, '**');
                    let res = Function('"use strict";return (' + full + ')')();
                    expression = full + ' =';
                    current = String(Number(res.toFixed(6)));
                  } catch(e) {
                    current = 'Error';
                  }
                  update();
                }
              </script>
            </body>
            </html>
        """.trimIndent()

        return CodeProjectPayload(
            title = "Onyx Calc Pro • Aplicación Web HTML5/JS",
            language = "html",
            filename = "onyx_calc.html",
            code = html,
            htmlPreviewContent = html,
            explanation = "Calculadora científica moderna con interfaz táctil responsiva inspirada en Onyx Open. Compatible con exportación HTML pura y ejecución en cualquier navegador o editor de código móvil."
        )
    }

    private fun generateMiniGameWebApp(prompt: String): CodeProjectPayload {
        val html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Onyx Reflex Arcade</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: sans-serif; }
                body { background: #0B0E14; color: #FFF; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100vh; padding: 16px; }
                h1 { color: #FF2A54; font-size: 24px; margin-bottom: 6px; letter-spacing: 1px; }
                p { color: #8C94A0; font-size: 13px; margin-bottom: 16px; }
                #gameCanvas { background: #131822; border: 2px solid #FF2A54; border-radius: 16px; box-shadow: 0 0 20px rgba(255,42,84,0.3); }
                .hud { display: flex; justify-content: space-between; width: 320px; margin-bottom: 12px; font-weight: bold; }
                .score-tag { color: #00E676; }
                .btn { margin-top: 16px; background: #FF2A54; border: none; color: white; padding: 12px 24px; border-radius: 12px; font-weight: 700; cursor: pointer; }
              </style>
            </head>
            <body>
              <h1>ONYX REFLEX ARCADE</h1>
              <p>Toca o haz clic sobre los núcleos de energía cuántica antes de que expiren</p>
              <div class="hud">
                <span>PUNTOS: <span class="score-tag" id="score">0</span></span>
                <span>TIEMPO: <span id="time">30</span>s</span>
              </div>
              <canvas id="gameCanvas" width="320" height="320"></canvas>
              <button class="btn" onclick="startGame()">Reiniciar Desafío</button>
              <script>
                const canvas = document.getElementById('gameCanvas');
                const ctx = canvas.getContext('2d');
                let score = 0, timeLeft = 30, timer = null, target = { x: 160, y: 160, r: 24 };
                function spawnTarget() {
                  target.x = 30 + Math.random() * (canvas.width - 60);
                  target.y = 30 + Math.random() * (canvas.height - 60);
                  draw();
                }
                function draw() {
                  ctx.clearRect(0, 0, canvas.width, canvas.height);
                  ctx.beginPath();
                  ctx.arc(target.x, target.y, target.r, 0, Math.PI * 2);
                  ctx.fillStyle = '#FF2A54';
                  ctx.shadowColor = '#FF2A54';
                  ctx.shadowBlur = 15;
                  ctx.fill();
                  ctx.closePath();
                  ctx.fillStyle = '#FFF';
                  ctx.font = '12px sans-serif';
                  ctx.fillText('CORE', target.x - 16, target.y + 4);
                }
                canvas.addEventListener('pointerdown', (e) => {
                  if (timeLeft <= 0) return;
                  const rect = canvas.getBoundingClientRect();
                  const px = e.clientX - rect.left;
                  const py = e.clientY - rect.top;
                  const dist = Math.hypot(px - target.x, py - target.y);
                  if (dist <= target.r) {
                    score += 100;
                    document.getElementById('score').innerText = score;
                    spawnTarget();
                  }
                });
                function startGame() {
                  score = 0; timeLeft = 30;
                  document.getElementById('score').innerText = score;
                  document.getElementById('time').innerText = timeLeft;
                  clearInterval(timer);
                  spawnTarget();
                  timer = setInterval(() => {
                    timeLeft--;
                    document.getElementById('time').innerText = timeLeft;
                    if (timeLeft <= 0) {
                      clearInterval(timer);
                      ctx.fillStyle = '#FFF';
                      ctx.font = '18px sans-serif';
                      ctx.fillText('¡TIEMPO TERMINADO!', 65, 160);
                    }
                  }, 1000);
                }
                startGame();
              </script>
            </body>
            </html>
        """.trimIndent()

        return CodeProjectPayload(
            title = "Onyx Reflex Arcade • Juego Web HTML5 Canvas",
            language = "html",
            filename = "onyx_arcade.html",
            code = html,
            htmlPreviewContent = html,
            explanation = "Mini juego interactivo con gráficos acelerados por Canvas 2D y detección de toques táctiles móviles."
        )
    }

    private fun generateDashboardWebApp(prompt: String): CodeProjectPayload {
        val html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Onyx Engineering KPI Dashboard</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, sans-serif; }
                body { background: #080B10; color: #ECEFF4; padding: 20px; }
                .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 1px solid #1F2937; padding-bottom: 14px; }
                .title { font-size: 20px; font-weight: 800; color: #FF2A54; }
                .status-badge { background: #064E3B; color: #34D399; padding: 4px 10px; border-radius: 999px; font-size: 11px; font-weight: 700; }
                .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 14px; margin-bottom: 24px; }
                .card { background: #111827; border: 1px solid #1F2937; border-radius: 16px; padding: 16px; }
                .card-label { font-size: 12px; color: #9CA3AF; margin-bottom: 6px; }
                .card-val { font-size: 24px; font-weight: 800; color: #F9FAFB; }
                .progress-bar { background: #1F2937; border-radius: 8px; height: 8px; overflow: hidden; margin-top: 10px; }
                .progress-fill { background: #FF2A54; height: 100%; border-radius: 8px; transition: width 0.4s ease; }
                .chart-box { background: #111827; border: 1px solid #1F2937; border-radius: 16px; padding: 18px; margin-top: 16px; }
                .bars { display: flex; align-items: flex-end; justify-content: space-around; height: 120px; padding-top: 20px; }
                .bar-col { display: flex; flex-direction: column; align-items: center; gap: 6px; }
                .bar-stick { width: 28px; background: #FF2A54; border-radius: 6px 6px 0 0; }
                .bar-tag { font-size: 11px; color: #9CA3AF; }
              </style>
            </head>
            <body>
              <div class="header">
                <div>
                  <div class="title">ONYX CONTROL CENTER</div>
                  <div style="font-size: 12px; color: #6B7280;">Monitoreo de Telemetría & Rendimiento</div>
                </div>
                <div class="status-badge">● EN LÍNEA</div>
              </div>
              <div class="metrics-grid">
                <div class="card">
                  <div class="card-label">Eficiencia Operativa</div>
                  <div class="card-val" id="eff">98.4%</div>
                  <div class="progress-bar"><div class="progress-fill" style="width: 98%;"></div></div>
                </div>
                <div class="card">
                  <div class="card-label">Tareas Resueltas</div>
                  <div class="card-val">1,240</div>
                  <div class="progress-bar"><div class="progress-fill" style="width: 82%; background: #10B981;"></div></div>
                </div>
              </div>
              <div class="chart-box">
                <div style="font-size: 14px; font-weight: 700; margin-bottom: 12px;">Carga de Cálculo por Módulo (kHz)</div>
                <div class="bars">
                  <div class="bar-col"><div class="bar-stick" style="height: 65px;"></div><span class="bar-tag">CAD</span></div>
                  <div class="bar-col"><div class="bar-stick" style="height: 95px; background: #10B981;"></div><span class="bar-tag">Física</span></div>
                  <div class="bar-col"><div class="bar-stick" style="height: 45px;"></div><span class="bar-tag">Código</span></div>
                  <div class="bar-col"><div class="bar-stick" style="height: 80px; background: #6366F1;"></div><span class="bar-tag">IA</span></div>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()

        return CodeProjectPayload(
            title = "Onyx Control Dashboard • Panel Web HTML5",
            language = "html",
            filename = "onyx_dashboard.html",
            code = html,
            htmlPreviewContent = html,
            explanation = "Dashboard interactivo para control de proyectos de ingeniería y productividad con gráficos de barras CSS responsivos."
        )
    }

    private fun generateTaskManagerWebApp(prompt: String): CodeProjectPayload {
        val html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Onyx Task Workspace</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, sans-serif; }
                body { background: #0D1117; color: #C9D1D9; padding: 18px; max-width: 500px; margin: 0 auto; }
                h2 { color: #FF2A54; margin-bottom: 12px; font-size: 20px; }
                .input-row { display: flex; gap: 8px; margin-bottom: 20px; }
                input { flex: 1; background: #161B22; border: 1px solid #30363D; padding: 12px; border-radius: 10px; color: #FFF; outline: none; }
                button { background: #FF2A54; border: none; color: white; padding: 12px 18px; border-radius: 10px; font-weight: 700; cursor: pointer; }
                .task-item { background: #161B22; border: 1px solid #30363D; padding: 12px 16px; border-radius: 10px; margin-bottom: 8px; display: flex; align-items: center; justify-content: space-between; }
                .task-item.done { text-decoration: line-through; opacity: 0.5; }
              </style>
            </head>
            <body>
              <h2>ONYX NOTION TASK APP</h2>
              <div class="input-row">
                <input id="taskInput" placeholder="Añadir tarea de ingeniería...">
                <button onclick="addTask()">Agregar</button>
              </div>
              <div id="taskList">
                <div class="task-item" onclick="toggleTask(this)">✓ Revisar tolerancias en plano de chasis</div>
                <div class="task-item" onclick="toggleTask(this)">✓ Exportar proyecto web HTML5 a Spck Editor</div>
                <div class="task-item" onclick="toggleTask(this)">✓ Resolver ecuaciones diferenciales de Laplace</div>
              </div>
              <script>
                function addTask() {
                  const input = document.getElementById('taskInput');
                  if (!input.value.trim()) return;
                  const item = document.createElement('div');
                  item.className = 'task-item';
                  item.innerText = '○ ' + input.value;
                  item.onclick = function() { toggleTask(item); };
                  document.getElementById('taskList').prepend(item);
                  input.value = '';
                }
                function toggleTask(elem) {
                  elem.classList.toggle('done');
                  if (elem.classList.contains('done')) {
                    elem.innerText = '✓ ' + elem.innerText.replace(/^[○✓]\s*/, '');
                  } else {
                    elem.innerText = '○ ' + elem.innerText.replace(/^[○✓]\s*/, '');
                  }
                }
              </script>
            </body>
            </html>
        """.trimIndent()

        return CodeProjectPayload(
            title = "Onyx Task Workspace • Mini App Web",
            language = "html",
            filename = "tasks_app.html",
            code = html,
            htmlPreviewContent = html,
            explanation = "Mini aplicación web interactiva con lista de tareas dinámicas, persistencia DOM y diseño minimalista."
        )
    }

    private fun generateGenericInteractiveWebApp(prompt: String): CodeProjectPayload {
        val cleanTopic = prompt.take(40).replace("\"", "")
        val html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Onyx Smart App</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
                body { background: #0A0D14; color: #FFFFFF; padding: 24px 16px; display: flex; justify-content: center; }
                .container { width: 100%; max-width: 480px; }
                .hero { background: linear-gradient(135deg, #1C0A12 0%, #111827 100%); border: 1px solid #FF2A54; border-radius: 20px; padding: 24px; text-align: center; margin-bottom: 20px; box-shadow: 0 10px 25px rgba(255,42,84,0.2); }
                .badge { display: inline-block; background: #FF2A54; color: #FFF; font-size: 11px; font-weight: 800; padding: 4px 12px; border-radius: 999px; margin-bottom: 12px; letter-spacing: 1px; }
                h1 { font-size: 22px; font-weight: 800; margin-bottom: 8px; line-height: 1.3; }
                p { color: #9CA3AF; font-size: 14px; line-height: 1.5; margin-bottom: 20px; }
                .interactive-box { background: #161D2A; border-radius: 16px; padding: 18px; border: 1px solid #232E42; margin-top: 16px; text-align: left; }
                .btn-action { background: #FF2A54; border: none; color: white; width: 100%; padding: 14px; border-radius: 12px; font-size: 15px; font-weight: 700; cursor: pointer; transition: transform 0.1s ease; }
                .btn-action:active { transform: scale(0.98); }
                .output { margin-top: 14px; background: #0E131D; border-radius: 10px; padding: 12px; font-family: monospace; font-size: 13px; color: #34D399; min-height: 48px; border: 1px dashed #232E42; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="hero">
                  <div class="badge">ONYX GENERATOR v4.0</div>
                  <h1>Aplicación Web Creada</h1>
                  <p>Módulo interactivo generado para: "$cleanTopic"</p>
                  <button class="btn-action" onclick="runSimulation()">Ejecutar Simulación Dinámica</button>
                  <div class="output" id="consoleLog">> Sistema en espera... Presiona el botón para procesar.</div>
                </div>
                <div class="interactive-box">
                  <h3 style="font-size: 15px; color: #FF2A54; margin-bottom: 8px;">Conexión con Entornos de Programación</h3>
                  <p style="font-size: 13px; color: #9CA3AF;">Este archivo HTML5 es autónomo (HTML, CSS y JS integrados). Puedes abrirlo directamente con navegadores como Chrome/Edge o importarlo en editores de código como Spck, Acode o VS Code.</p>
                </div>
              </div>
              <script>
                let cycles = 0;
                function runSimulation() {
                  cycles++;
                  const log = document.getElementById('consoleLog');
                  log.innerHTML = '> Iteración ' + cycles + ' completada con éxito.<br>> Estado del motor: Fluido y optimizado.<br>> Timestamp: ' + new Date().toLocaleTimeString();
                }
              </script>
            </body>
            </html>
        """.trimIndent()

        return CodeProjectPayload(
            title = "Página Web Interactiva • HTML5 & JavaScript",
            language = "html",
            filename = "app_onyx.html",
            code = html,
            htmlPreviewContent = html,
            explanation = "Estructura web completa lista para su previsualización interactiva y compatible con exportación a archivos .html y aplicaciones de programación."
        )
    }

    private fun generatePythonProject(prompt: String): CodeProjectPayload {
        val cleanPrompt = prompt.replace("\"", "")
        val code = """
            # ===============================================================
            # ONYX OPEN • MOTOR COGNITIVO DE INGENIERÍA Y PROGRAMACIÓN
            # Proyecto: Simulación & Análisis Numérico en Python
            # Solicitud: $cleanPrompt
            # Creado para: damianblack68@gmail.com
            # ===============================================================

            import math
            import sys

            class OnyxEngine:
                def __init__(self, name: str = "Onyx Evolution"):
                    self.name = name
                    self.status = "Activo - Precisión 100%"
                    self.metrics = []

                def calcular_resistencia_estructural(self, carga_n: float, area_m2: float) -> dict:
                    # Calcula esfuerzo normal (Sigma = F / A) y factor de seguridad
                    if area_m2 <= 0:
                        raise ValueError("El área debe ser mayor que cero.")
                    esfuerzo_pa = carga_n / area_m2
                    limite_fluencia_acero = 250e6  # 250 MPa (Acero A36)
                    factor_seguridad = limite_fluencia_acero / esfuerzo_pa
                    
                    return {
                        "esfuerzo_mpa": esfuerzo_pa / 1e6,
                        "factor_seguridad": factor_seguridad,
                        "estado": "SEGURO" if factor_seguridad >= 1.5 else "CRÍTICO"
                    }

                def exportar_informe(self, datos: dict) -> str:
                    informe = "=== REPORTE TÉCNICO ONYX ===\n"
                    for k, v in datos.items():
                        informe += "- " + str(k) + ": " + str(v) + "\n"
                    return informe

            if __name__ == "__main__":
                engine = OnyxEngine()
                print("Iniciando " + engine.name + "...")
                resultado = engine.calcular_resistencia_estructural(carga_n=85000, area_m2=0.0012)
                print(engine.exportar_informe(resultado))
        """.trimIndent()

        return CodeProjectPayload(
            title = "Script de Ingeniería Estructural • Python 3",
            language = "python",
            filename = "onyx_analysis.py",
            code = code,
            htmlPreviewContent = null,
            explanation = "Script de Python orientado a objetos con tipado estático, cálculo de esfuerzo mecánico y verificación de factor de seguridad según normas de ingeniería."
        )
    }

    private fun generateKotlinProject(prompt: String): CodeProjectPayload {
        val code = """
            package com.onyx.engineering

            import kotlinx.coroutines.flow.MutableStateFlow
            import kotlinx.coroutines.flow.asStateFlow

            /**
             * Módulo de Procesamiento de Ingeniería y Tareas Escolares
             * Generado por Onyx Open para Android Jetpack Compose
             */
            data class CalculationState(
                val inputMetric: Double = 0.0,
                val result: Double = 0.0,
                val isSuccess: Boolean = false
            )

            class OnyxEngineeringController {
                private val _state = MutableStateFlow(CalculationState())
                val state = _state.asStateFlow()

                fun computeTrajectory(velocity: Double, angleDeg: Double, g: Double = 9.81) {
                    val angleRad = Math.toRadians(angleDeg)
                    // Alcance horizontal máximo R = (v^2 * sin(2*theta)) / g
                    val reach = (velocity * velocity * Math.sin(2 * angleRad)) / g
                    _state.value = CalculationState(
                        inputMetric = velocity,
                        result = reach,
                        isSuccess = true
                    )
                }
            }
        """.trimIndent()

        return CodeProjectPayload(
            title = "Módulo de Ingeniería Android • Kotlin",
            language = "kotlin",
            filename = "OnyxController.kt",
            code = code,
            htmlPreviewContent = null,
            explanation = "Controlador en Kotlin compatible con Jetpack Compose y Coroutines para cálculo de cinemática y trayectorias físicas."
        )
    }
}
