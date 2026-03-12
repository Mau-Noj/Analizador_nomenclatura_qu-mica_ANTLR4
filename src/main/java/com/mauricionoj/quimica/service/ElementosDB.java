package com.mauricionoj.quimica.service;

import com.mauricionoj.quimica.model.ElementoInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Base de datos en memoria de elementos químicos.
 * Contiene los elementos más comunes en nomenclatura inorgánica y orgánica básica.
 */
@Component
public class ElementosDB {

    private final Map<String, ElementoInfo> elementos = new HashMap<>();

    public ElementosDB() {
        cargarElementos();
    }

    private void cargarElementos() {

        // ── No metales ────────────────────────────────────────────────────
        agregar("H",  "Hidrógeno",   1,  "no metal",   new int[]{1, -1},          1.008);
        agregar("C",  "Carbono",     6,  "no metal",   new int[]{4, 2, -4},       12.011);
        agregar("N",  "Nitrógeno",   7,  "no metal",   new int[]{5, 4, 3, 2, 1, -3}, 14.007);
        agregar("O",  "Oxígeno",     8,  "no metal",   new int[]{-2, -1},         15.999);
        agregar("F",  "Flúor",       9,  "no metal",   new int[]{-1},             18.998);
        agregar("P",  "Fósforo",     15, "no metal",   new int[]{5, 3, -3},       30.974);
        agregar("S",  "Azufre",      16, "no metal",   new int[]{6, 4, 2, -2},    32.06);
        agregar("Cl", "Cloro",       17, "no metal",   new int[]{7, 5, 3, 1, -1}, 35.45);
        agregar("Se", "Selenio",     34, "no metal",   new int[]{6, 4, -2},       78.971);
        agregar("Br", "Bromo",       35, "no metal",   new int[]{5, 3, 1, -1},    79.904);
        agregar("I",  "Yodo",        53, "no metal",   new int[]{7, 5, 3, 1, -1}, 126.904);

        // ── Metales alcalinos ─────────────────────────────────────────────
        agregar("Li", "Litio",       3,  "metal",      new int[]{1},              6.941);
        agregar("Na", "Sodio",       11, "metal",      new int[]{1},              22.990);
        agregar("K",  "Potasio",     19, "metal",      new int[]{1},              39.098);
        agregar("Rb", "Rubidio",     37, "metal",      new int[]{1},              85.468);
        agregar("Cs", "Cesio",       55, "metal",      new int[]{1},              132.905);

        // ── Metales alcalinotérreos ───────────────────────────────────────
        agregar("Be", "Berilio",     4,  "metal",      new int[]{2},              9.012);
        agregar("Mg", "Magnesio",    12, "metal",      new int[]{2},              24.305);
        agregar("Ca", "Calcio",      20, "metal",      new int[]{2},              40.078);
        agregar("Sr", "Estroncio",   38, "metal",      new int[]{2},              87.62);
        agregar("Ba", "Bario",       56, "metal",      new int[]{2},              137.327);

        // ── Metales de transición ─────────────────────────────────────────
        agregar("Fe", "Hierro",      26, "metal",      new int[]{3, 2},           55.845);
        agregar("Cu", "Cobre",       29, "metal",      new int[]{2, 1},           63.546);
        agregar("Zn", "Zinc",        30, "metal",      new int[]{2},              65.38);
        agregar("Ag", "Plata",       47, "metal",      new int[]{1},              107.868);
        agregar("Au", "Oro",         79, "metal",      new int[]{3, 1},           196.967);
        agregar("Hg", "Mercurio",    80, "metal",      new int[]{2, 1},           200.592);
        agregar("Pb", "Plomo",       82, "metal",      new int[]{4, 2},           207.2);
        agregar("Sn", "Estaño",      50, "metal",      new int[]{4, 2},           118.710);
        agregar("Cr", "Cromo",       24, "metal",      new int[]{6, 3, 2},        51.996);
        agregar("Mn", "Manganeso",   25, "metal",      new int[]{7, 4, 2},        54.938);
        agregar("Ni", "Níquel",      28, "metal",      new int[]{3, 2},           58.693);
        agregar("Co", "Cobalto",     27, "metal",      new int[]{3, 2},           58.933);
        agregar("Ti", "Titanio",     22, "metal",      new int[]{4, 3, 2},        47.867);
        agregar("V",  "Vanadio",     23, "metal",      new int[]{5, 4, 3, 2},     50.942);
        agregar("W",  "Tungsteno",   74, "metal",      new int[]{6, 4, 2},        183.84);
        agregar("Mo", "Molibdeno",   42, "metal",      new int[]{6, 4, 2},        95.96);
        agregar("Pt", "Platino",     78, "metal",      new int[]{4, 2},           195.084);
        agregar("Pd", "Paladio",     46, "metal",      new int[]{4, 2},           106.42);

        // ── Semimetales ───────────────────────────────────────────────────
        agregar("B",  "Boro",        5,  "semimetal",  new int[]{3},              10.811);
        agregar("Si", "Silicio",     14, "semimetal",  new int[]{4, -4},          28.085);
        agregar("As", "Arsénico",    33, "semimetal",  new int[]{5, 3, -3},       74.922);
        agregar("Sb", "Antimonio",   51, "semimetal",  new int[]{5, 3, -3},       121.760);

        // ── Otros metales ─────────────────────────────────────────────────
        agregar("Al", "Aluminio",    13, "metal",      new int[]{3},              26.982);
        agregar("Bi", "Bismuto",     83, "metal",      new int[]{5, 3},           208.980);

        // ── Gases nobles ──────────────────────────────────────────────────
        agregar("He", "Helio",       2,  "gas noble",  new int[]{0},              4.003);
        agregar("Ne", "Neón",        10, "gas noble",  new int[]{0},              20.180);
        agregar("Ar", "Argón",       18, "gas noble",  new int[]{0},              39.948);
        agregar("Kr", "Kriptón",     36, "gas noble",  new int[]{0},              83.798);
        agregar("Xe", "Xenón",       54, "gas noble",  new int[]{0},              131.293);
    }

    private void agregar(String simbolo, String nombre, int numAtomico,
                         String grupo, int[] oxidaciones, double masa) {
        elementos.put(simbolo, ElementoInfo.builder()
                .simbolo(simbolo)
                .nombre(nombre)
                .numeroAtomico(numAtomico)
                .grupo(grupo)
                .estadosOxidacion(oxidaciones)
                .masaAtomica(masa)
                .subindice(1)
                .build());
    }

    // ── Métodos públicos ──────────────────────────────────────────────────

    public ElementoInfo getElemento(String simbolo) {
        return elementos.get(simbolo);
    }

    public boolean existeElemento(String simbolo) {
        return elementos.containsKey(simbolo);
    }

    public boolean esMetal(String simbolo) {
        ElementoInfo e = elementos.get(simbolo);
        return e != null && e.getGrupo().equals("metal");
    }

    public boolean esNoMetal(String simbolo) {
        ElementoInfo e = elementos.get(simbolo);
        return e != null && e.getGrupo().equals("no metal");
    }

    public boolean esOxigeno(String simbolo) {
        return "O".equals(simbolo);
    }

    public boolean esHidrogeno(String simbolo) {
        return "H".equals(simbolo);
    }

    public int[] getEstadosOxidacion(String simbolo) {
        ElementoInfo e = elementos.get(simbolo);
        return e != null ? e.getEstadosOxidacion() : new int[]{};
    }

    public String getNombre(String simbolo) {
        ElementoInfo e = elementos.get(simbolo);
        return e != null ? e.getNombre() : simbolo;
    }

    public Map<String, ElementoInfo> getTodos() {
        return elementos;
    }
}