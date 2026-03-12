package com.mauricionoj.quimica.controller;

import com.mauricionoj.quimica.QuimicaLexer;
import com.mauricionoj.quimica.QuimicaParser;
import com.mauricionoj.quimica.model.PreguntaQuiz;
import com.mauricionoj.quimica.model.ResultadoAnalisis;
import com.mauricionoj.quimica.service.NomenclaturService;
import com.mauricionoj.quimica.service.QuizService;
import com.mauricionoj.quimica.visitor.FormulaVisitor;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QuimicaController {

    private final NomenclaturService nomenclaturService;
    private final QuizService        quizService;

    public QuimicaController(NomenclaturService nomenclaturService,
                              QuizService quizService) {
        this.nomenclaturService = nomenclaturService;
        this.quizService        = quizService;
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /api/analizar/formula
    // Body: { "formula": "Ca3(PO4)2" }
    // Respuesta: ResultadoAnalisis completo con nombres IUPAC, Stock, Tradicional
    // ══════════════════════════════════════════════════════════════════════
    @PostMapping("/analizar/formula")
    public ResponseEntity<?> analizarFormula(@RequestBody Map<String, String> body) {
        String formula = body.get("formula");

        if (formula == null || formula.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "El campo 'formula' es obligatorio."));
        }

        try {
            Map<String, Integer> atomos = parsearFormula(formula.trim());
            ResultadoAnalisis resultado = nomenclaturService.analizar(atomos, formula.trim());
            return ResponseEntity.ok(resultado);

        } catch (FormulaInvalidaException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error",   "Fórmula inválida",
                    "detalle", e.getMessage(),
                    "formula", formula
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /api/analizar/nombre
    // Body: { "nombre": "óxido de calcio" }
    // Respuesta básica — búsqueda en tabla de fórmulas conocidas
    // ══════════════════════════════════════════════════════════════════════
    @PostMapping("/analizar/nombre")
    public ResponseEntity<?> analizarNombre(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");

        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "El campo 'nombre' es obligatorio."));
        }

        // Tabla básica nombre → fórmula (puede expandirse)
        Map<String, String> tablaInversa = Map.ofEntries(
            Map.entry("óxido de calcio",          "CaO"),
            Map.entry("óxido de sodio",           "Na2O"),
            Map.entry("óxido de hierro",          "Fe2O3"),
            Map.entry("hidróxido de sodio",       "NaOH"),
            Map.entry("hidróxido de calcio",      "Ca(OH)2"),
            Map.entry("ácido clorhídrico",        "HCl"),
            Map.entry("ácido sulfúrico",          "H2SO4"),
            Map.entry("ácido nítrico",            "HNO3"),
            Map.entry("cloruro de sodio",         "NaCl"),
            Map.entry("carbonato de calcio",      "CaCO3"),
            Map.entry("dióxido de carbono",       "CO2"),
            Map.entry("agua",                     "H2O")
        );

        String nombreLower = nombre.trim().toLowerCase();
        String formula = tablaInversa.get(nombreLower);

        if (formula == null) {
            return ResponseEntity.ok(Map.of(
                "nombre",  nombre,
                "formula", "No encontrada en la base de datos.",
                "nota",    "Use POST /api/analizar/formula para análisis completo."
            ));
        }

        try {
            Map<String, Integer> atomos = parsearFormula(formula);
            ResultadoAnalisis resultado = nomenclaturService.analizar(atomos, formula);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("nombre", nombre, "formula", formula));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /api/validar
    // Body: { "formula": "H2SO4" }
    // Respuesta: { valida, errores, atomos }
    // ══════════════════════════════════════════════════════════════════════
    @PostMapping("/validar")
    public ResponseEntity<?> validarFormula(@RequestBody Map<String, String> body) {
        String formula = body.get("formula");

        if (formula == null || formula.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "El campo 'formula' es obligatorio."));
        }

        List<String> errores = new ArrayList<>();

        try {
            // Crear lexer con colector de errores
            CharStream input = CharStreams.fromString(formula.trim());
            QuimicaLexer lexer = new QuimicaLexer(input);
            lexer.removeErrorListeners();
            ErrorCollector errorCollector = new ErrorCollector();
            lexer.addErrorListener(errorCollector);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            QuimicaParser parser = new QuimicaParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorCollector);

            ParseTree tree = parser.formula();
            errores.addAll(errorCollector.getErrores());

            if (!errores.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "formula", formula,
                    "valida",  false,
                    "errores", errores
                ));
            }

            // Si no hubo errores sintácticos, extraer átomos
            FormulaVisitor visitor = new FormulaVisitor();
            Map<String, Integer> atomos = visitor.visit(tree);

            return ResponseEntity.ok(Map.of(
                "formula", formula,
                "valida",  true,
                "atomos",  atomos,
                "errores", List.of()
            ));

        } catch (Exception e) {
            errores.add("Error al procesar: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "formula", formula,
                "valida",  false,
                "errores", errores
            ));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/quiz/pregunta
    // Respuesta: PreguntaQuiz con opciones (sin revelar respuesta correcta)
    // ══════════════════════════════════════════════════════════════════════
    @GetMapping("/quiz/pregunta")
    public ResponseEntity<PreguntaQuiz> obtenerPregunta() {
        PreguntaQuiz pregunta = quizService.generarPregunta();
        // No enviamos la respuesta correcta al cliente
        PreguntaQuiz sinRespuesta = PreguntaQuiz.builder()
            .id(pregunta.getId())
            .tipo(pregunta.getTipo())
            .pregunta(pregunta.getPregunta())
            .opciones(pregunta.getOpciones())
            .dificultad(pregunta.getDificultad())
            .build();
        return ResponseEntity.ok(sinRespuesta);
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /api/quiz/responder
    // Body: { "formula": "H2O", "respuesta": "óxido de dihidrógeno" }
    // Respuesta: { correcto, respuestaCorrecta, explicacion }
    // ══════════════════════════════════════════════════════════════════════
    @PostMapping("/quiz/responder")
    public ResponseEntity<?> responderPregunta(@RequestBody Map<String, String> body) {
        String formula   = body.get("formula");
        String respuesta = body.get("respuesta");

        if (formula == null || respuesta == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Se requieren 'formula' y 'respuesta'."));
        }

        Map<String, Object> resultado = quizService.verificarRespuesta(
            body.getOrDefault("id", ""),
            formula,
            respuesta
        );
        return ResponseEntity.ok(resultado);
    }

    // ══════════════════════════════════════════════════════════════════════
    // UTILIDADES PRIVADAS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Convierte una fórmula en texto a Map<String, Integer> usando ANTLR.
     * Lanza FormulaInvalidaException si hay errores sintácticos.
     */
    private Map<String, Integer> parsearFormula(String formula) {
        CharStream input = CharStreams.fromString(formula);
        QuimicaLexer lexer = new QuimicaLexer(input);
        lexer.removeErrorListeners();
        ErrorCollector errores = new ErrorCollector();
        lexer.addErrorListener(errores);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        QuimicaParser parser = new QuimicaParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errores);

        ParseTree tree = parser.formula();

        if (!errores.getErrores().isEmpty()) {
            throw new FormulaInvalidaException(String.join("; ", errores.getErrores()));
        }

        FormulaVisitor visitor = new FormulaVisitor();
        return visitor.visit(tree);
    }

    // ── Colector de errores ANTLR ─────────────────────────────────────────
    private static class ErrorCollector extends BaseErrorListener {
        private final List<String> errores = new ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            errores.add("Línea " + line + ":" + charPositionInLine + " — " + msg);
        }

        public List<String> getErrores() { return errores; }
    }

    // ── Excepción interna ─────────────────────────────────────────────────
    private static class FormulaInvalidaException extends RuntimeException {
        public FormulaInvalidaException(String msg) { super(msg); }
    }
}