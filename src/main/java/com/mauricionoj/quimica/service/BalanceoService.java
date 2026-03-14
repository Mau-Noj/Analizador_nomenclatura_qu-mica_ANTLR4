package com.mauricionoj.quimica.service;

import com.mauricionoj.quimica.QuimicaLexer;
import com.mauricionoj.quimica.QuimicaParser;
import com.mauricionoj.quimica.visitor.FormulaVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BalanceoService {

    // ── Aritmética racional exacta ────────────────────────────────────────────
    private static long gcd(long a, long b) { return b == 0 ? Math.abs(a) : gcd(b, a % b); }

    private static class Frac {
        final long n, d;
        static final Frac ZERO = new Frac(0, 1);
        static final Frac ONE  = new Frac(1, 1);

        Frac(long n, long d) {
            if (d == 0) throw new ArithmeticException("División por cero");
            long g = gcd(Math.abs(n), Math.abs(d));
            this.n = (d < 0 ? -n : n) / g;
            this.d = Math.abs(d) / g;
        }
        Frac(long n) { this(n, 1); }

        Frac add(Frac o) { return new Frac(n * o.d + o.n * d, d * o.d); }
        Frac sub(Frac o) { return new Frac(n * o.d - o.n * d, d * o.d); }
        Frac mul(Frac o) { return new Frac(n * o.n, d * o.d); }
        Frac div(Frac o) { return new Frac(n * o.d, d * o.n); }
        Frac neg()       { return new Frac(-n, d); }
        boolean isZero() { return n == 0; }

        @Override public String toString() { return d == 1 ? String.valueOf(n) : n + "/" + d; }
    }

    // ── Parseo de fórmula individual con ANTLR4 ───────────────────────────────
    private Map<String, Integer> parsearFormula(String formula) {
        CharStream input = CharStreams.fromString(formula.trim());
        QuimicaLexer  lexer  = new QuimicaLexer(input);
        QuimicaParser parser = new QuimicaParser(new CommonTokenStream(lexer));
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        ParseTree tree = parser.formula();
        FormulaVisitor visitor = new FormulaVisitor();
        return visitor.visit(tree);
    }

    // ── Eliminación gaussiana sobre Frac[][] → espacio nulo ──────────────────
    private List<Frac> encontrarEspacioNulo(Frac[][] A, int filas, int cols) {
        // Copia de trabajo
        Frac[][] M = new Frac[filas][cols];
        for (int i = 0; i < filas; i++)
            for (int j = 0; j < cols; j++)
                M[i][j] = A[i][j];

        int[] pivotCol = new int[filas];
        Arrays.fill(pivotCol, -1);
        int fila = 0;

        for (int col = 0; col < cols && fila < filas; col++) {
            // Buscar pivote
            int pivotFila = -1;
            for (int i = fila; i < filas; i++) {
                if (!M[i][col].isZero()) { pivotFila = i; break; }
            }
            if (pivotFila == -1) continue;

            // Intercambiar filas
            Frac[] tmp = M[fila]; M[fila] = M[pivotFila]; M[pivotFila] = tmp;
            pivotCol[fila] = col;

            // Normalizar fila pivote
            Frac pivVal = M[fila][col];
            for (int j = 0; j < cols; j++) M[fila][j] = M[fila][j].div(pivVal);

            // Eliminar columna en otras filas
            for (int i = 0; i < filas; i++) {
                if (i == fila || M[i][col].isZero()) continue;
                Frac factor = M[i][col];
                for (int j = 0; j < cols; j++)
                    M[i][j] = M[i][j].sub(factor.mul(M[fila][j]));
            }
            fila++;
        }

        int rango = fila;
        // Variables libres = columnas sin pivote
        Set<Integer> colsPivot = new HashSet<>();
        for (int i = 0; i < rango; i++) if (pivotCol[i] >= 0) colsPivot.add(pivotCol[i]);

        List<Integer> libres = new ArrayList<>();
        for (int j = 0; j < cols; j++) if (!colsPivot.contains(j)) libres.add(j);

        if (libres.isEmpty()) return null; // Sin solución no trivial

        // Fijar primera variable libre = 1, resolver el resto
        int libre = libres.get(0);
        Frac[] sol = new Frac[cols];
        for (int j = 0; j < cols; j++) sol[j] = Frac.ZERO;
        sol[libre] = Frac.ONE;

        // Back-substitution
        for (int i = rango - 1; i >= 0; i--) {
            int pc = pivotCol[i];
            if (pc < 0) continue;
            Frac val = Frac.ZERO;
            for (int j = pc + 1; j < cols; j++) val = val.add(M[i][j].mul(sol[j]));
            sol[pc] = val.neg();
        }

        return Arrays.asList(sol);
    }

    // ── Escalar fracciones a enteros mínimos positivos ────────────────────────
    private List<Long> escalarAEnteros(List<Frac> sol) {
        // MCM de todos los denominadores — sin reasignar, usamos array trick
        long mcmVal = 1;
        for (Frac f : sol) mcmVal = mcmVal / gcd(mcmVal, f.d) * f.d;
        final long mcm = mcmVal;

        List<Long> paso1 = sol.stream()
                .map(f -> f.n * (mcm / f.d))
                .collect(Collectors.toList());

        // Si todos negativos, invertir signos
        List<Long> paso2 = paso1.stream().allMatch(v -> v <= 0)
                ? paso1.stream().map(v -> -v).collect(Collectors.toList())
                : paso1;

        // Dividir por MCD general
        final long mcd = paso2.stream().mapToLong(Math::abs).reduce(0L, BalanceoService::gcd);
        List<Long> enteros = mcd > 1
                ? paso2.stream().map(v -> v / mcd).collect(Collectors.toList())
                : paso2;

        return enteros;
    }

    // ── API pública ───────────────────────────────────────────────────────────
    public Map<String, Object> balancear(String ecuacion) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("ecuacionOriginal", ecuacion);

        // 1. Dividir en reactivos y productos
        String[] partes = ecuacion.split("->|→|⟶|=");
        if (partes.length != 2) {
            resultado.put("valida", false);
            resultado.put("error", "Formato inválido. Usa: H2 + O2 -> H2O  o  H2 + O2 → H2O");
            return resultado;
        }

        List<String> nombresReact = Arrays.stream(partes[0].split("\\+"))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        List<String> nombresProd  = Arrays.stream(partes[1].split("\\+"))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        int nReact = nombresReact.size();
        int nProd  = nombresProd.size();
        int nMol   = nReact + nProd;

        // 2. Parsear cada fórmula
        List<Map<String, Integer>> moleculas = new ArrayList<>();
        try {
            for (String f : nombresReact) moleculas.add(parsearFormula(f));
            for (String f : nombresProd)  moleculas.add(parsearFormula(f));
        } catch (Exception e) {
            resultado.put("valida", false);
            resultado.put("error", "Fórmula no reconocida: " + e.getMessage());
            return resultado;
        }

        // 3. Recopilar todos los elementos
        List<String> elementos = new ArrayList<>(
                moleculas.stream()
                         .flatMap(m -> m.keySet().stream())
                         .collect(Collectors.toCollection(LinkedHashSet::new))
        );
        int nElem = elementos.size();

        if (nElem == 0) {
            resultado.put("valida", false);
            resultado.put("error", "No se reconocieron elementos en la ecuación.");
            return resultado;
        }

        // 4. Construir matriz A (elementos × moléculas)
        //    Reactivos: coeficiente positivo. Productos: negativo.
        Frac[][] A = new Frac[nElem][nMol];
        for (int i = 0; i < nElem; i++)
            for (int j = 0; j < nMol; j++) {
                double signo = j < nReact ? 1.0 : -1.0;
                int cant = moleculas.get(j).getOrDefault(elementos.get(i), 0);
                A[i][j] = new Frac((long)(signo * cant));
            }

        // 5. Resolver espacio nulo
        List<Frac> sol;
        try {
            sol = encontrarEspacioNulo(A, nElem, nMol);
        } catch (Exception e) {
            resultado.put("valida", false);
            resultado.put("error", "No se pudo resolver el sistema: " + e.getMessage());
            return resultado;
        }

        if (sol == null || sol.stream().allMatch(Frac::isZero)) {
            resultado.put("valida", false);
            resultado.put("error", "La ecuación no tiene solución entera positiva (puede que no esté balanceada correctamente).");
            return resultado;
        }

        // 6. Escalar a enteros mínimos positivos
        List<Long> coefs = escalarAEnteros(sol);

        // Verificar que todos sean positivos
        if (coefs.stream().anyMatch(c -> c <= 0)) {
            resultado.put("valida", false);
            resultado.put("error", "No se encontraron coeficientes positivos. Revisa la ecuación.");
            return resultado;
        }

        // 7. Construir string de la ecuación balanceada
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nReact; i++) {
            if (i > 0) sb.append(" + ");
            if (coefs.get(i) > 1) sb.append(coefs.get(i));
            sb.append(nombresReact.get(i));
        }
        sb.append(" \u2192 ");
        for (int i = 0; i < nProd; i++) {
            if (i > 0) sb.append(" + ");
            if (coefs.get(nReact + i) > 1) sb.append(coefs.get(nReact + i));
            sb.append(nombresProd.get(i));
        }

        // 8. Verificar balance (comprobación)
        boolean verificado = verificarBalance(moleculas, coefs, nReact, elementos);

        // 9. Construir lista de moléculas con coeficientes para el frontend
        List<Map<String, Object>> moleculasInfo = new ArrayList<>();
        for (int i = 0; i < nMol; i++) {
            Map<String, Object> info = new LinkedHashMap<>();
            String nombre = i < nReact ? nombresReact.get(i) : nombresProd.get(i - nReact);
            info.put("formula",     nombre);
            info.put("coeficiente", coefs.get(i));
            info.put("lado",        i < nReact ? "reactivo" : "producto");
            info.put("atomos",      moleculas.get(i));
            moleculasInfo.add(info);
        }

        resultado.put("valida",             verificado);
        resultado.put("ecuacionBalanceada", sb.toString());
        resultado.put("coeficientes",       coefs);
        resultado.put("moleculas",          moleculasInfo);
        resultado.put("elementosBalanceados", elementos);
        return resultado;
    }

    private boolean verificarBalance(List<Map<String, Integer>> mols, List<Long> coefs,
                                     int nReact, List<String> elementos) {
        for (String elem : elementos) {
            long sumaReact = 0, sumaProd = 0;
            for (int i = 0; i < nReact; i++)
                sumaReact += coefs.get(i) * mols.get(i).getOrDefault(elem, 0);
            for (int i = nReact; i < mols.size(); i++)
                sumaProd  += coefs.get(i) * mols.get(i).getOrDefault(elem, 0);
            if (sumaReact != sumaProd) return false;
        }
        return true;
    }
}