package com.mauricionoj.quimica.model;

import java.util.List;

public class ResultadoAnalisis {

    private String       formulaEntrada;
    private String       tipoCompuesto;
    private String       nombreIUPAC;
    private String       nombreStock;
    private String       nombreTradicional;
    private List<ElementoInfo> elementos;
    private List<String> pasos;
    private boolean      valida;
    private List<String> errores;
    private String       descripcion;
    private String       usoComun;

    public ResultadoAnalisis() {}

    // ── Builder ───────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String       formulaEntrada;
        private String       tipoCompuesto;
        private String       nombreIUPAC;
        private String       nombreStock;
        private String       nombreTradicional;
        private List<ElementoInfo> elementos;
        private List<String> pasos;
        private boolean      valida;
        private List<String> errores;
        private String       descripcion;
        private String       usoComun;

        public Builder formulaEntrada(String v)         { this.formulaEntrada = v;    return this; }
        public Builder tipoCompuesto(String v)          { this.tipoCompuesto = v;     return this; }
        public Builder nombreIUPAC(String v)            { this.nombreIUPAC = v;       return this; }
        public Builder nombreStock(String v)            { this.nombreStock = v;       return this; }
        public Builder nombreTradicional(String v)      { this.nombreTradicional = v; return this; }
        public Builder elementos(List<ElementoInfo> v)  { this.elementos = v;         return this; }
        public Builder pasos(List<String> v)            { this.pasos = v;             return this; }
        public Builder valida(boolean v)                { this.valida = v;            return this; }
        public Builder errores(List<String> v)          { this.errores = v;           return this; }
        public Builder descripcion(String v)            { this.descripcion = v;       return this; }
        public Builder usoComun(String v)               { this.usoComun = v;          return this; }

        public ResultadoAnalisis build() {
            ResultadoAnalisis r = new ResultadoAnalisis();
            r.formulaEntrada  = this.formulaEntrada;
            r.tipoCompuesto   = this.tipoCompuesto;
            r.nombreIUPAC     = this.nombreIUPAC;
            r.nombreStock     = this.nombreStock;
            r.nombreTradicional = this.nombreTradicional;
            r.elementos       = this.elementos;
            r.pasos           = this.pasos;
            r.valida          = this.valida;
            r.errores         = this.errores;
            r.descripcion     = this.descripcion;
            r.usoComun        = this.usoComun;
            return r;
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public String             getFormulaEntrada()   { return formulaEntrada; }
    public String             getTipoCompuesto()    { return tipoCompuesto; }
    public String             getNombreIUPAC()      { return nombreIUPAC; }
    public String             getNombreStock()      { return nombreStock; }
    public String             getNombreTradicional(){ return nombreTradicional; }
    public List<ElementoInfo> getElementos()        { return elementos; }
    public List<String>       getPasos()            { return pasos; }
    public boolean            isValida()            { return valida; }
    public List<String>       getErrores()          { return errores; }
    public String             getDescripcion()      { return descripcion; }
    public String             getUsoComun()         { return usoComun; }

    // ── Setters ───────────────────────────────────────────────────────────
    public void setFormulaEntrada(String v)          { this.formulaEntrada = v; }
    public void setTipoCompuesto(String v)           { this.tipoCompuesto = v; }
    public void setNombreIUPAC(String v)             { this.nombreIUPAC = v; }
    public void setNombreStock(String v)             { this.nombreStock = v; }
    public void setNombreTradicional(String v)       { this.nombreTradicional = v; }
    public void setElementos(List<ElementoInfo> v)   { this.elementos = v; }
    public void setPasos(List<String> v)             { this.pasos = v; }
    public void setValida(boolean v)                 { this.valida = v; }
    public void setErrores(List<String> v)           { this.errores = v; }
    public void setDescripcion(String v)             { this.descripcion = v; }
    public void setUsoComun(String v)                { this.usoComun = v; }
}