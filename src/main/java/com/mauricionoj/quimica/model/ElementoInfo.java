package com.mauricionoj.quimica.model;

public class ElementoInfo {

    private String simbolo;
    private String nombre;
    private int    subindice;
    private int    numeroAtomico;
    private String grupo;
    private int[]  estadosOxidacion;
    private double masaAtomica;

    public ElementoInfo() {}

    public ElementoInfo(String simbolo, String nombre, int subindice,
                        int numeroAtomico, String grupo,
                        int[] estadosOxidacion, double masaAtomica) {
        this.simbolo          = simbolo;
        this.nombre           = nombre;
        this.subindice        = subindice;
        this.numeroAtomico    = numeroAtomico;
        this.grupo            = grupo;
        this.estadosOxidacion = estadosOxidacion;
        this.masaAtomica      = masaAtomica;
    }

    // ── Builder ───────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String simbolo;
        private String nombre;
        private int    subindice = 1;
        private int    numeroAtomico;
        private String grupo;
        private int[]  estadosOxidacion;
        private double masaAtomica;

        public Builder simbolo(String v)          { this.simbolo = v;          return this; }
        public Builder nombre(String v)           { this.nombre = v;           return this; }
        public Builder subindice(int v)           { this.subindice = v;        return this; }
        public Builder numeroAtomico(int v)       { this.numeroAtomico = v;    return this; }
        public Builder grupo(String v)            { this.grupo = v;            return this; }
        public Builder estadosOxidacion(int[] v)  { this.estadosOxidacion = v; return this; }
        public Builder masaAtomica(double v)      { this.masaAtomica = v;      return this; }

        public ElementoInfo build() {
            return new ElementoInfo(simbolo, nombre, subindice,
                                    numeroAtomico, grupo,
                                    estadosOxidacion, masaAtomica);
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public String getSimbolo()          { return simbolo; }
    public String getNombre()           { return nombre; }
    public int    getSubindice()        { return subindice; }
    public int    getNumeroAtomico()    { return numeroAtomico; }
    public String getGrupo()            { return grupo; }
    public int[]  getEstadosOxidacion() { return estadosOxidacion; }
    public double getMasaAtomica()      { return masaAtomica; }

    // ── Setters ───────────────────────────────────────────────────────────
    public void setSimbolo(String v)         { this.simbolo = v; }
    public void setNombre(String v)          { this.nombre = v; }
    public void setSubindice(int v)          { this.subindice = v; }
    public void setNumeroAtomico(int v)      { this.numeroAtomico = v; }
    public void setGrupo(String v)           { this.grupo = v; }
    public void setEstadosOxidacion(int[] v) { this.estadosOxidacion = v; }
    public void setMasaAtomica(double v)     { this.masaAtomica = v; }

    @Override
    public String toString() {
        return "ElementoInfo{simbolo='" + simbolo + "', nombre='" + nombre +
               "', subindice=" + subindice + ", grupo='" + grupo + "'}";
    }
}