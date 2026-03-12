package com.mauricionoj.quimica.service;

import com.mauricionoj.quimica.model.ElementoInfo;
import com.mauricionoj.quimica.model.ResultadoAnalisis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio principal de nomenclatura química.
 * Identifica el tipo de compuesto y genera nombres en 3 sistemas:
 * IUPAC, Stock y Tradicional.
 */
@Service
public class NomenclaturService {

    @Autowired
    private ElementosDB db;

    // ── Prefijos IUPAC para subíndices ────────────────────────────────────
    private static final String[] PREFIJOS = {
        "", "mono", "di", "tri", "tetra", "penta",
        "hexa", "hepta", "octa", "nona", "deca"
    };

    // ── Sufijos tradicionales por estado de oxidación ─────────────────────
    private static final Map<Integer, String> SUFIJO_OSO_ICO = Map.of(
        1, "oso",  2, "oso",  3, "oso",
        4, "ico",  5, "ico",  6, "ico",  7, "ico"
    );

    // ── Números romanos ───────────────────────────────────────────────────
    private static final Map<Integer, String> ROMANOS = Map.of(
        1, "I", 2, "II", 3, "III", 4, "IV",
        5, "V", 6, "VI", 7, "VII", 8, "VIII"
    );

    /**
     * Punto de entrada principal.
     * Recibe un mapa símbolo→cantidad de átomos y devuelve el análisis completo.
     */
    public ResultadoAnalisis analizar(Map<String, Integer> atomos, String formulaOriginal) {

        List<String> pasos   = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        // ── Paso 1: Validar que todos los elementos existen ───────────────
        pasos.add("Paso 1 — Identificar elementos presentes en la fórmula.");
        for (String simbolo : atomos.keySet()) {
            if (!db.existeElemento(simbolo)) {
                errores.add("Elemento desconocido: " + simbolo);
            } else {
                ElementoInfo e = db.getElemento(simbolo);
                pasos.add("  · " + simbolo + " = " + e.getNombre() +
                          " (grupo: " + e.getGrupo() + ")");
            }
        }

        if (!errores.isEmpty()) {
            return ResultadoAnalisis.builder()
                    .formulaEntrada(formulaOriginal)
                    .valida(false)
                    .errores(errores)
                    .pasos(pasos)
                    .build();
        }

        // ── Paso 2: Identificar tipo de compuesto ─────────────────────────
        pasos.add("Paso 2 — Clasificar el tipo de compuesto.");
        String tipo = identificarTipo(atomos, pasos);

        // ── Paso 3: Generar nombres según el tipo ─────────────────────────
        pasos.add("Paso 3 — Generar nomenclatura en los 3 sistemas.");
        String iupac       = "";
        String stock       = "";
        String tradicional = "";
        String descripcion = "";
        String usoComun    = "";

        switch (tipo) {
            case "OXIDO_BASICO" -> {
                String metal = obtenerMetal(atomos);
                int cantO    = atomos.get("O");
                int cantM    = atomos.get(metal);
                int oxidacion = calcularOxidacion(metal, cantO, cantM, pasos);

                iupac       = generarOxidoIUPAC(metal, cantM, cantO, pasos);
                stock       = generarOxidoStock(metal, oxidacion, pasos);
                tradicional = generarOxidoTradicional(metal, oxidacion, pasos);
                descripcion = "Compuesto binario formado por un metal y oxígeno.";
                usoComun    = obtenerUsoOxido(metal);
            }
            case "OXIDO_ACIDO" -> {
                String noMetal = obtenerNoMetalSinO(atomos);
                int cantO      = atomos.get("O");
                int cantNM     = atomos.get(noMetal);
                int oxidacion  = calcularOxidacionNoMetal(noMetal, cantO, cantNM, pasos);

                iupac       = generarAnhidridoIUPAC(noMetal, cantNM, cantO, pasos);
                stock       = generarAnhidridoStock(noMetal, oxidacion, pasos);
                tradicional = generarAnhidridoTradicional(noMetal, oxidacion, pasos);
                descripcion = "Óxido ácido (anhídrido): no metal + oxígeno.";
                usoComun    = obtenerUsoAnhidrido(noMetal);
            }
            case "HIDROXIDO" -> {
                String metal  = obtenerMetal(atomos);
                int cantOH    = atomos.getOrDefault("OH", atomos.get("O"));
                int oxidacion = (cantOH != 0) ? cantOH : 1;

                iupac       = "hidróxido de " + db.getNombre(metal).toLowerCase();
                stock       = "hidróxido de " + db.getNombre(metal).toLowerCase() +
                              (tieneVariosEstados(metal) ? "(" + ROMANOS.get(oxidacion) + ")" : "");
                tradicional = generarHidroxidoTradicional(metal, oxidacion, pasos);
                descripcion = "Base o hidróxido: metal + grupo OH⁻.";
                usoComun    = obtenerUsoHidroxido(metal);
                pasos.add("  · Los hidróxidos son bases. Se forman cuando un óxido metálico reacciona con agua.");
            }
            case "ACIDO_HIDRÁCIDO" -> {
                String noMetal = obtenerNoMetalSinH(atomos);
                iupac       = "ácido " + raizNombre(noMetal) + "hídrico";
                stock       = iupac;
                tradicional = iupac;
                descripcion = "Ácido hidrácido: hidrógeno + no metal (sin oxígeno).";
                usoComun    = obtenerUsoAcido(noMetal, false);
                pasos.add("  · Los hidrácidos se nombran: ácido + raíz del no metal + -hídrico.");
            }
            case "ACIDO_OXACIDO" -> {
                String noMetal = obtenerNoMetalSinH(atomos);
                int cantO      = atomos.get("O");
                int cantH      = atomos.get("H");
                int oxidacion  = calcularOxidacionNoMetal(noMetal,
                                     cantO - cantH / 2, atomos.get(noMetal), pasos);

                iupac       = generarOxacidoIUPAC(noMetal, cantO, cantH, pasos);
                stock       = iupac;
                tradicional = generarOxacidoTradicional(noMetal, oxidacion, pasos);
                descripcion = "Ácido oxácido: hidrógeno + no metal + oxígeno.";
                usoComun    = obtenerUsoAcido(noMetal, true);
            }
            case "SAL_BINARIA" -> {
                String metal   = obtenerMetal(atomos);
                String noMetal = obtenerNoMetalSinO(atomos);
                int cantM      = atomos.get(metal);
                int cantNM     = atomos.get(noMetal);

                iupac       = generarSalBinariaIUPAC(metal, cantM, noMetal, cantNM, pasos);
                stock       = generarSalBinariaStock(metal, noMetal, cantM, cantNM, pasos);
                tradicional = generarSalBinariaTradicional(metal, noMetal, cantM, cantNM, pasos);
                descripcion = "Sal binaria: metal + no metal (sin oxígeno).";
                usoComun    = obtenerUsoSalBinaria(metal, noMetal);
            }
            default -> {
                tipo        = "COMPUESTO_NO_IDENTIFICADO";
                descripcion = "Compuesto no clasificado en las categorías disponibles.";
                pasos.add("  · No se pudo identificar el tipo de compuesto automáticamente.");
            }
        }

        // ── Construir y devolver resultado ────────────────────────────────
        List<ElementoInfo> infoElementos = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : atomos.entrySet()) {
            ElementoInfo base = db.getElemento(entry.getKey());
            if (base != null) {
                infoElementos.add(ElementoInfo.builder()
                        .simbolo(base.getSimbolo())
                        .nombre(base.getNombre())
                        .subindice(entry.getValue())
                        .numeroAtomico(base.getNumeroAtomico())
                        .grupo(base.getGrupo())
                        .estadosOxidacion(base.getEstadosOxidacion())
                        .masaAtomica(base.getMasaAtomica())
                        .build());
            }
        }

        return ResultadoAnalisis.builder()
                .formulaEntrada(formulaOriginal)
                .tipoCompuesto(tipo)
                .nombreIUPAC(iupac)
                .nombreStock(stock)
                .nombreTradicional(tradicional)
                .elementos(infoElementos)
                .pasos(pasos)
                .valida(true)
                .errores(errores)
                .descripcion(descripcion)
                .usoComun(usoComun)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // IDENTIFICACIÓN DE TIPO
    // ══════════════════════════════════════════════════════════════════════

    private String identificarTipo(Map<String, Integer> atomos, List<String> pasos) {
        boolean tieneO  = atomos.containsKey("O");
        boolean tieneH  = atomos.containsKey("H");
        boolean tieneOH = tieneO && tieneH;

        long cantMetales   = atomos.keySet().stream().filter(db::esMetal).count();
        long cantNoMetales = atomos.keySet().stream()
                             .filter(s -> db.esNoMetal(s) && !s.equals("O") && !s.equals("H"))
                             .count();

        String tipo;

        if (cantMetales >= 1 && tieneO && !tieneH && cantNoMetales == 0) {
            tipo = "OXIDO_BASICO";
            pasos.add("  · Contiene metal + oxígeno → Óxido básico.");
        } else if (cantMetales == 0 && tieneO && !tieneH && cantNoMetales >= 1) {
            tipo = "OXIDO_ACIDO";
            pasos.add("  · Contiene no metal + oxígeno (sin H ni metal) → Óxido ácido.");
        } else if (cantMetales >= 1 && tieneOH && cantNoMetales == 0) {
            tipo = "HIDROXIDO";
            pasos.add("  · Contiene metal + O + H → Hidróxido (base).");
        } else if (tieneH && !tieneO && cantNoMetales >= 1 && cantMetales == 0) {
            tipo = "ACIDO_HIDRÁCIDO";
            pasos.add("  · Contiene H + no metal (sin O) → Ácido hidrácido.");
        } else if (tieneH && tieneO && cantNoMetales >= 1 && cantMetales == 0) {
            tipo = "ACIDO_OXACIDO";
            pasos.add("  · Contiene H + no metal + O → Ácido oxácido.");
        } else if (cantMetales >= 1 && cantNoMetales >= 1 && !tieneO && !tieneH) {
            tipo = "SAL_BINARIA";
            pasos.add("  · Contiene metal + no metal (sin O ni H) → Sal binaria.");
        } else {
            tipo = "DESCONOCIDO";
            pasos.add("  · No se pudo clasificar automáticamente.");
        }

        return tipo;
    }

    // ══════════════════════════════════════════════════════════════════════
    // GENERADORES DE NOMBRE — ÓXIDOS BÁSICOS
    // ══════════════════════════════════════════════════════════════════════

    private String generarOxidoIUPAC(String metal, int cantM, int cantO, List<String> pasos) {
        String prefM = cantM > 1 ? PREFIJOS[cantM] : "";
        String prefO = cantO > 1 ? PREFIJOS[cantO] : "";
        String nombre = prefO + "óxido de " + prefM + db.getNombre(metal).toLowerCase();
        pasos.add("  IUPAC: " + prefO + "óxido de [prefijo]" + db.getNombre(metal).toLowerCase());
        return nombre;
    }

    private String generarOxidoStock(String metal, int oxidacion, List<String> pasos) {
        if (!tieneVariosEstados(metal)) {
            pasos.add("  Stock: Metal con un solo estado de oxidación — sin número romano.");
            return "óxido de " + db.getNombre(metal).toLowerCase();
        }
        String nombre = "óxido de " + db.getNombre(metal).toLowerCase() +
                        "(" + ROMANOS.getOrDefault(oxidacion, "?") + ")";
        pasos.add("  Stock: oxidación del metal = " + oxidacion +
                  " → " + ROMANOS.getOrDefault(oxidacion, "?"));
        return nombre;
    }

    private String generarOxidoTradicional(String metal, int oxidacion, List<String> pasos) {
        if (!tieneVariosEstados(metal)) {
            return "óxido de " + db.getNombre(metal).toLowerCase();
        }
        int[] estados = db.getEstadosOxidacion(metal);
        String sufijo = (estados.length >= 2 && oxidacion == estados[estados.length - 1])
                        ? "oso" : "ico";
        String raiz = raizNombre(metal);
        pasos.add("  Tradicional: oxidación " + oxidacion + " → sufijo -" + sufijo);
        return "óxido " + raiz + sufijo;
    }

    // ══════════════════════════════════════════════════════════════════════
    // GENERADORES DE NOMBRE — ÓXIDOS ÁCIDOS (ANHÍDRIDOS)
    // ══════════════════════════════════════════════════════════════════════

    private String generarAnhidridoIUPAC(String noMetal, int cantNM, int cantO, List<String> pasos) {
        String prefNM = cantNM > 1 ? PREFIJOS[cantNM] : "";
        String prefO  = cantO  > 1 ? PREFIJOS[cantO]  : "";
        String nombre = prefO + "óxido de " + prefNM + db.getNombre(noMetal).toLowerCase();
        pasos.add("  IUPAC: usa prefijos griegos para cantidad de átomos.");
        return nombre;
    }

    private String generarAnhidridoStock(String noMetal, int oxidacion, List<String> pasos) {
        pasos.add("  Stock: óxido de " + db.getNombre(noMetal).toLowerCase() +
                  "(" + ROMANOS.getOrDefault(oxidacion, "?") + ")");
        return "óxido de " + db.getNombre(noMetal).toLowerCase() +
               "(" + ROMANOS.getOrDefault(oxidacion, "?") + ")";
    }

    private String generarAnhidridoTradicional(String noMetal, int oxidacion, List<String> pasos) {
        int[] estados = db.getEstadosOxidacion(noMetal);
        String sufijo = (estados.length >= 2 && oxidacion == estados[estados.length - 1])
                        ? "oso" : "ico";
        String nombre = "anhídrido " + raizNombre(noMetal) + sufijo;
        pasos.add("  Tradicional: anhídrido + raíz + oso/ico según estado de oxidación.");
        return nombre;
    }

    // ══════════════════════════════════════════════════════════════════════
    // GENERADORES DE NOMBRE — HIDRÓXIDOS
    // ══════════════════════════════════════════════════════════════════════

    private String generarHidroxidoTradicional(String metal, int oxidacion, List<String> pasos) {
        if (!tieneVariosEstados(metal)) {
            return "hidróxido de " + db.getNombre(metal).toLowerCase();
        }
        int[] estados = db.getEstadosOxidacion(metal);
        String sufijo = (estados.length >= 2 && oxidacion == estados[estados.length - 1])
                        ? "oso" : "ico";
        pasos.add("  Tradicional: raíz del metal + sufijo -" + sufijo);
        return raizNombre(metal) + sufijo;
    }

    // ══════════════════════════════════════════════════════════════════════
    // GENERADORES DE NOMBRE — ÁCIDOS OXÁCIDOS
    // ══════════════════════════════════════════════════════════════════════

    private String generarOxacidoIUPAC(String noMetal, int cantO, int cantH, List<String> pasos) {
        pasos.add("  IUPAC: tetra-oxo, tri-oxo según cantidad de oxígenos.");
        return "ácido " + PREFIJOS[Math.min(cantO, 10)] + "oxo" +
               raizNombre(noMetal) + "ico";
    }

    private String generarOxacidoTradicional(String noMetal, int oxidacion, List<String> pasos) {
        int[] estados = db.getEstadosOxidacion(noMetal);
        String raiz = raizNombre(noMetal);
        String nombre;

        if (estados.length == 1) {
            nombre = "ácido " + raiz + "ico";
        } else if (oxidacion == estados[0]) {
            // Estado más alto → ico
            nombre = "ácido " + raiz + "ico";
        } else {
            // Estado más bajo → oso
            nombre = "ácido " + raiz + "oso";
        }
        pasos.add("  Tradicional: ácido + raíz + oso/ico. Oxidación " + oxidacion + ".");
        return nombre;
    }

    // ══════════════════════════════════════════════════════════════════════
    // GENERADORES DE NOMBRE — SALES BINARIAS
    // ══════════════════════════════════════════════════════════════════════

    private String generarSalBinariaIUPAC(String metal, int cantM,
                                           String noMetal, int cantNM,
                                           List<String> pasos) {
        String prefM  = cantM  > 1 ? PREFIJOS[cantM]  : "";
        String prefNM = cantNM > 1 ? PREFIJOS[cantNM] : "";
        String raizNM = raizNombre(noMetal);
        pasos.add("  IUPAC: [prefijo]uro de [prefijo]metal.");
        return prefNM + raizNM + "uro de " + prefM + db.getNombre(metal).toLowerCase();
    }

    private String generarSalBinariaStock(String metal, String noMetal,
                                           int cantM, int cantNM,
                                           List<String> pasos) {
        String raizNM = raizNombre(noMetal);
        int oxidacion = (cantNM * Math.abs(db.getEstadosOxidacion(noMetal)[
                         db.getEstadosOxidacion(noMetal).length - 1])) / cantM;
        pasos.add("  Stock: oxidación del metal calculada = " + oxidacion);
        if (!tieneVariosEstados(metal)) {
            return raizNM + "uro de " + db.getNombre(metal).toLowerCase();
        }
        return raizNM + "uro de " + db.getNombre(metal).toLowerCase() +
               "(" + ROMANOS.getOrDefault(oxidacion, "?") + ")";
    }

    private String generarSalBinariaTradicional(String metal, String noMetal,
                                                 int cantM, int cantNM,
                                                 List<String> pasos) {
        String raizNM = raizNombre(noMetal);
        if (!tieneVariosEstados(metal)) {
            return raizNM + "uro de " + db.getNombre(metal).toLowerCase();
        }
        int oxidacion = (cantNM * Math.abs(db.getEstadosOxidacion(noMetal)[
                         db.getEstadosOxidacion(noMetal).length - 1])) / cantM;
        int[] estados = db.getEstadosOxidacion(metal);
        String sufijo = (estados.length >= 2 && oxidacion == estados[estados.length - 1])
                        ? "oso" : "ico";
        pasos.add("  Tradicional: " + raizNombre(metal) + sufijo + " + uro.");
        return raizNM + "uro " + raizNombre(metal) + sufijo;
    }

    // ══════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════════════

    private String obtenerMetal(Map<String, Integer> atomos) {
        return atomos.keySet().stream()
                .filter(db::esMetal)
                .findFirst()
                .orElse("");
    }

    private String obtenerNoMetalSinO(Map<String, Integer> atomos) {
        return atomos.keySet().stream()
                .filter(s -> db.esNoMetal(s) && !s.equals("O"))
                .findFirst()
                .orElse("");
    }

    private String obtenerNoMetalSinH(Map<String, Integer> atomos) {
        return atomos.keySet().stream()
                .filter(s -> db.esNoMetal(s) && !s.equals("H") && !s.equals("O"))
                .findFirst()
                .orElse("");
    }

    private boolean tieneVariosEstados(String simbolo) {
        int[] estados = db.getEstadosOxidacion(simbolo);
        return estados != null && estados.length > 1;
    }

    private int calcularOxidacion(String metal, int cantO, int cantMetal, List<String> pasos) {
        // Oxígeno tiene oxidación -2
        int totalO = cantO * 2;
        int oxidacion = totalO / cantMetal;
        pasos.add("  · Cálculo: O aporta " + totalO + " cargas negativas. " +
                  "Metal con " + cantMetal + " átomo(s) → oxidación = +" + oxidacion);
        return oxidacion;
    }

    private int calcularOxidacionNoMetal(String noMetal, int cantO,
                                          int cantNM, List<String> pasos) {
        int totalO = cantO * 2;
        int oxidacion = cantNM > 0 ? totalO / cantNM : 0;
        pasos.add("  · Oxidación del no metal = " + totalO + " / " + cantNM +
                  " = " + oxidacion);
        return oxidacion;
    }

    /**
     * Obtiene la raíz del nombre del elemento para formar nombres compuestos.
     * Ej: Hierro → ferr, Cobre → cupr, Azufre → sulf
     */
    public String raizNombre(String simbolo) {
        return switch (simbolo) {
            case "Fe" -> "ferr";
            case "Cu" -> "cupr";
            case "Au" -> "aur";
            case "Ag" -> "argent";
            case "Pb" -> "plumb";
            case "Sn" -> "estann";
            case "Hg" -> "mercur";
            case "S"  -> "sulf";
            case "N"  -> "nitr";
            case "P"  -> "fosfor";
            case "Cl" -> "clor";
            case "Br" -> "brom";
            case "I"  -> "yod";
            case "C"  -> "carbon";
            case "F"  -> "fluor";
            case "As" -> "arsen";
            case "Cr" -> "crom";
            case "Mn" -> "mangan";
            default   -> {
                String n = db.getNombre(simbolo);
                if (n == null) yield simbolo.toLowerCase();
                // Quitar terminación vocal + agregar raíz
                n = n.toLowerCase();
                if (n.endsWith("o") || n.endsWith("a") || n.endsWith("e")) {
                    yield n.substring(0, n.length() - 1);
                }
                yield n;
            }
        };
    }

    // ── Usos comunes ──────────────────────────────────────────────────────

    private String obtenerUsoOxido(String metal) {
        return switch (metal) {
            case "Fe" -> "Óxido de hierro (herrumbre) — muy común en corrosión ambiental.";
            case "Ca" -> "Cal viva (CaO) — usada en construcción y tratamiento de agua.";
            case "Na" -> "Usado en síntesis química industrial.";
            case "Al" -> "Alúmina (Al₂O₃) — usada en cerámica y electrónica.";
            case "Cu" -> "Encontrado en patinas de cobre y pigmentos históricos.";
            default   -> "Compuesto con aplicaciones industriales y de laboratorio.";
        };
    }

    private String obtenerUsoAnhidrido(String noMetal) {
        return switch (noMetal) {
            case "C"  -> "CO₂ — gas de efecto invernadero, esencial en fotosíntesis.";
            case "S"  -> "SO₂ — contaminante atmosférico, lluvia ácida.";
            case "N"  -> "NO₂ — contaminante urbano, smog fotoquímico.";
            case "P"  -> "P₂O₅ — deshidratante potente en laboratorio.";
            default   -> "Óxido ácido con aplicaciones en síntesis química.";
        };
    }

    private String obtenerUsoHidroxido(String metal) {
        return switch (metal) {
            case "Na" -> "NaOH (sosa cáustica) — limpiadores, fabricación de jabón.";
            case "Ca" -> "Ca(OH)₂ (cal apagada) — tratamiento de agua, construcción.";
            case "Mg" -> "Mg(OH)₂ — antiácido (leche de magnesia).";
            case "Al" -> "Antiácido y agente coagulante en potabilización de agua.";
            case "Fe" -> "Fe(OH)₃ — coagulante en tratamiento de aguas residuales.";
            default   -> "Base con aplicaciones en industria química.";
        };
    }

    private String obtenerUsoAcido(String noMetal, boolean esOxacido) {
        return switch (noMetal) {
            case "S"  -> esOxacido ? "H₂SO₄ — ácido sulfúrico, uno de los más producidos industrialmente."
                                   : "H₂S — gas tóxico con olor a huevo podrido.";
            case "N"  -> "HNO₃ — ácido nítrico, fabricación de fertilizantes y explosivos.";
            case "Cl" -> esOxacido ? "HClO₄ — ácido perclórico, oxidante fuerte."
                                   : "HCl — ácido clorhídrico, digestión gástrica y decapado industrial.";
            case "P"  -> "H₃PO₄ — ácido fosfórico, fertilizantes y refrescos de cola.";
            case "C"  -> "H₂CO₃ — ácido carbónico, bebidas carbonatadas.";
            default   -> "Ácido con aplicaciones en síntesis química.";
        };
    }

    private String obtenerUsoSalBinaria(String metal, String noMetal) {
        if (metal.equals("Na") && noMetal.equals("Cl"))
            return "NaCl — sal de mesa, conservante alimentario, electrolito corporal.";
        if (metal.equals("Ca") && noMetal.equals("F"))
            return "CaF₂ — fluorita, fuente de flúor para pasta dental.";
        if (metal.equals("Fe") && noMetal.equals("S"))
            return "FeS₂ — pirita, 'oro de los tontos', fuente de azufre.";
        return "Sal binaria con aplicaciones industriales o de laboratorio.";
    }
}