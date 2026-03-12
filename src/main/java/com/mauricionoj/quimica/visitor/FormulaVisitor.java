package com.mauricionoj.quimica.visitor;

import com.mauricionoj.quimica.QuimicaParserBaseVisitor;
import com.mauricionoj.quimica.QuimicaParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Visitor que recorre el árbol sintáctico generado por QuimicaParser
 * y devuelve un Map<String, Integer> con los átomos y sus cantidades.
 *
 * Ejemplo: Ca3(PO4)2  →  {Ca=3, P=2, O=8}
 */
public class FormulaVisitor extends QuimicaParserBaseVisitor<Map<String, Integer>> {

    // ── formula : compuesto EOF ───────────────────────────────────────────
    @Override
    public Map<String, Integer> visitFormula(QuimicaParser.FormulaContext ctx) {
        return visit(ctx.compuesto());
    }

    // ── compuesto : grupo+ ────────────────────────────────────────────────
    @Override
    public Map<String, Integer> visitCompuesto(QuimicaParser.CompuestoContext ctx) {
        Map<String, Integer> total = new HashMap<>();

        for (QuimicaParser.GrupoContext grupo : ctx.grupo()) {
            Map<String, Integer> parcial = visit(grupo);
            mergeSumar(total, parcial);
        }

        return total;
    }

    // ── grupoSimple : elementoConSubindice ────────────────────────────────
    @Override
    public Map<String, Integer> visitGrupoSimple(QuimicaParser.GrupoSimpleContext ctx) {
        return visit(ctx.elementoConSubindice());
    }

    // ── grupoParentesis : '(' compuesto ')' subindice? ────────────────────
    @Override
    public Map<String, Integer> visitGrupoParentesis(QuimicaParser.GrupoParentesisContext ctx) {
        Map<String, Integer> interno = visit(ctx.compuesto());

        int multiplicador = 1;
        if (ctx.subindice() != null) {
            multiplicador = Integer.parseInt(ctx.subindice().NUMERO().getText());
        }

        return multiplicarAtom(interno, multiplicador);
    }

    // ── elementoConSubindice : ELEMENTO subindice? ────────────────────────
    @Override
    public Map<String, Integer> visitElementoConSubindice(QuimicaParser.ElementoConSubindiceContext ctx) {
        String simbolo = ctx.ELEMENTO().getText();

        int cantidad = 1;
        if (ctx.subindice() != null) {
            cantidad = Integer.parseInt(ctx.subindice().NUMERO().getText());
        }

        Map<String, Integer> resultado = new HashMap<>();
        resultado.put(simbolo, cantidad);
        return resultado;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Suma los átomos de 'origen' dentro de 'destino'.
     */
    private void mergeSumar(Map<String, Integer> destino, Map<String, Integer> origen) {
        origen.forEach((elemento, cantidad) ->
            destino.merge(elemento, cantidad, Integer::sum)
        );
    }

    /**
     * Multiplica todas las cantidades del mapa por un factor.
     */
    private Map<String, Integer> multiplicarAtom(Map<String, Integer> mapa, int factor) {
        Map<String, Integer> resultado = new HashMap<>();
        mapa.forEach((elemento, cantidad) ->
            resultado.put(elemento, cantidad * factor)
        );
        return resultado;
    }
}