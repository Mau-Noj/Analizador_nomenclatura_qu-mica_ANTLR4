parser grammar QuimicaParser;

options { tokenVocab = QuimicaLexer; }

// ── Regla raíz ────────────────────────────────────────────────────────────
formula
    : compuesto EOF
    ;

// ── Compuesto: uno o más grupos ───────────────────────────────────────────
compuesto
    : grupo+
    ;

// ── Grupo: elemento simple o grupo entre paréntesis ──────────────────────
grupo
    : elementoConSubindice                          # grupoSimple
    | PAREN_ABRE compuesto PAREN_CIERRA subindice?  # grupoParentesis
    ;

// ── Elemento con su subíndice opcional ───────────────────────────────────
elementoConSubindice
    : ELEMENTO subindice?
    ;

// ── Subíndice numérico ────────────────────────────────────────────────────
subindice
    : NUMERO
    ;
