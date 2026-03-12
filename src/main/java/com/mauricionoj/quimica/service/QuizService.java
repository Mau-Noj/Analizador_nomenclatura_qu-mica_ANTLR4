package com.mauricionoj.quimica.service;

import com.mauricionoj.quimica.model.PreguntaQuiz;
import com.mauricionoj.quimica.model.ResultadoAnalisis;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuizService {

    // Fórmulas conocidas para generar preguntas
    private static final List<String[]> FORMULAS = List.of(
        // { formula, nombre IUPAC, tipo }
        new String[]{"H2O",   "óxido de dihidrógeno",  "oxido"},
        new String[]{"NaCl",  "cloruro de sodio",       "sal"},
        new String[]{"CO2",   "dióxido de carbono",     "anhidrido"},
        new String[]{"H2SO4", "ácido sulfúrico",        "acido"},
        new String[]{"NaOH",  "hidróxido de sodio",     "hidroxido"},
        new String[]{"CaCO3", "carbonato de calcio",    "sal"},
        new String[]{"HCl",   "ácido clorhídrico",      "acido"},
        new String[]{"NH3",   "trihidruro de nitrógeno","otro"},
        new String[]{"Fe2O3", "trióxido de dihierro",   "oxido"},
        new String[]{"Ca(OH)2","hidróxido de calcio",   "hidroxido"}
    );

    private final NomenclaturService nomenclaturService;
    private final Random random = new Random();

    public QuizService(NomenclaturService nomenclaturService) {
        this.nomenclaturService = nomenclaturService;
    }

    /**
     * Genera una pregunta aleatoria de opción múltiple.
     */
    public PreguntaQuiz generarPregunta() {
        // Elegir fórmula al azar
        String[] base = FORMULAS.get(random.nextInt(FORMULAS.size()));
        String formula    = base[0];
        String nombreCorr = base[1];

        // Generar 3 distractores tomando nombres de otras fórmulas
        List<String> distractores = new ArrayList<>();
        List<String[]> otras = new ArrayList<>(FORMULAS);
        otras.removeIf(f -> f[0].equals(formula));
        Collections.shuffle(otras);
        for (int i = 0; i < 3 && i < otras.size(); i++) {
            distractores.add(otras.get(i)[1]);
        }

        // Armar opciones y mezclar
        List<String> opciones = new ArrayList<>(distractores);
        opciones.add(nombreCorr);
        Collections.shuffle(opciones);

        String id = UUID.randomUUID().toString().substring(0, 8);

        return PreguntaQuiz.builder()
            .id(id)
            .tipo("formula_a_nombre")
            .pregunta("¿Cuál es el nombre IUPAC de " + formula + "?")
            .respuestaCorrecta(nombreCorr)
            .opciones(opciones)
            .explicacion("La fórmula " + formula + " corresponde a: " + nombreCorr)
            .dificultad("media")
            .build();
    }

    /**
     * Verifica si la respuesta del usuario es correcta.
     * Retorna un mapa con: correcto (boolean), respuestaCorrecta, explicacion.
     */
    public Map<String, Object> verificarRespuesta(String idPregunta,
                                                   String formulaPreguntada,
                                                   String respuestaUsuario) {
        // Buscar la respuesta correcta en la lista
        String correcta = FORMULAS.stream()
            .filter(f -> f[0].equalsIgnoreCase(formulaPreguntada))
            .map(f -> f[1])
            .findFirst()
            .orElse(null);

        Map<String, Object> resultado = new LinkedHashMap<>();

        if (correcta == null) {
            resultado.put("correcto", false);
            resultado.put("mensaje", "Fórmula no encontrada en el banco de preguntas.");
            return resultado;
        }

        boolean correcto = correcta.equalsIgnoreCase(respuestaUsuario.trim());
        resultado.put("correcto", correcto);
        resultado.put("respuestaUsuario", respuestaUsuario);
        resultado.put("respuestaCorrecta", correcta);
        resultado.put("explicacion", "La fórmula " + formulaPreguntada + " es: " + correcta);
        return resultado;
    }
}