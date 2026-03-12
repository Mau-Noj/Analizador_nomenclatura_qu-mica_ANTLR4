package com.mauricionoj.quimica.model;

import java.util.List;

public class PreguntaQuiz {

    private String       id;
    private String       tipo;
    private String       pregunta;
    private String       respuestaCorrecta;
    private List<String> opciones;
    private String       explicacion;
    private String       dificultad;

    public PreguntaQuiz() {}

    // ── Builder ───────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String       id;
        private String       tipo;
        private String       pregunta;
        private String       respuestaCorrecta;
        private List<String> opciones;
        private String       explicacion;
        private String       dificultad;

        public Builder id(String v)                { this.id = v;                return this; }
        public Builder tipo(String v)              { this.tipo = v;              return this; }
        public Builder pregunta(String v)          { this.pregunta = v;          return this; }
        public Builder respuestaCorrecta(String v) { this.respuestaCorrecta = v; return this; }
        public Builder opciones(List<String> v)    { this.opciones = v;          return this; }
        public Builder explicacion(String v)       { this.explicacion = v;       return this; }
        public Builder dificultad(String v)        { this.dificultad = v;        return this; }

        public PreguntaQuiz build() {
            PreguntaQuiz p = new PreguntaQuiz();
            p.id                = this.id;
            p.tipo              = this.tipo;
            p.pregunta          = this.pregunta;
            p.respuestaCorrecta = this.respuestaCorrecta;
            p.opciones          = this.opciones;
            p.explicacion       = this.explicacion;
            p.dificultad        = this.dificultad;
            return p;
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public String       getId()                { return id; }
    public String       getTipo()              { return tipo; }
    public String       getPregunta()          { return pregunta; }
    public String       getRespuestaCorrecta() { return respuestaCorrecta; }
    public List<String> getOpciones()          { return opciones; }
    public String       getExplicacion()       { return explicacion; }
    public String       getDificultad()        { return dificultad; }

    // ── Setters ───────────────────────────────────────────────────────────
    public void setId(String v)                { this.id = v; }
    public void setTipo(String v)              { this.tipo = v; }
    public void setPregunta(String v)          { this.pregunta = v; }
    public void setRespuestaCorrecta(String v) { this.respuestaCorrecta = v; }
    public void setOpciones(List<String> v)    { this.opciones = v; }
    public void setExplicacion(String v)       { this.explicacion = v; }
    public void setDificultad(String v)        { this.dificultad = v; }
}