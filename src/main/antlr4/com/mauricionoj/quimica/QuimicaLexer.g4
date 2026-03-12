lexer grammar QuimicaLexer;

// ── Elementos químicos comunes ────────────────────────────────────────────
// Orden importa: los más largos primero

// Metales de transición
ELEMENTO
    : 'He' | 'Li' | 'Be'
    | 'Ne' | 'Na' | 'Mg' | 'Al' | 'Si'
    | 'Cl' | 'Ar' | 'Ca' | 'Sc' | 'Ti'
    | 'Cr' | 'Mn' | 'Fe' | 'Co' | 'Ni'
    | 'Cu' | 'Zn' | 'Ga' | 'Ge' | 'As'
    | 'Se' | 'Br' | 'Kr' | 'Rb' | 'Sr'
    | 'Ag' | 'Cd' | 'In' | 'Sn' | 'Sb'
    | 'Te' | 'Xe' | 'Cs' | 'Ba' | 'La'
    | 'Au' | 'Hg' | 'Tl' | 'Pb' | 'Bi'
    | 'Pt' | 'Pd' | 'Rh' | 'Ru' | 'Mo'
    | 'Zr' | 'Nb' | 'Tc' | 'Re' | 'Os'
    | 'Ir' | 'W'  | 'Ta' | 'Hf' | 'Lu'
    | 'Al' | 'Si' | 'Mg'
    | 'H'  | 'B'  | 'C'  | 'N'  | 'O'
    | 'F'  | 'P'  | 'S'  | 'K'  | 'V'
    | 'I'  | 'U'
    ;

// ── Números (subíndices y coeficientes) ───────────────────────────────────
NUMERO : [0-9]+ ;

// ── Paréntesis ────────────────────────────────────────────────────────────
PAREN_ABRE  : '(' ;
PAREN_CIERRA: ')' ;

// ── Operadores de estado de oxidación ─────────────────────────────────────
CORCHETE_ABRE  : '[' ;
CORCHETE_CIERRA: ']' ;

// ── Signos de carga ───────────────────────────────────────────────────────
MAS   : '+' ;
MENOS : '-' ;

// ── Espacios (ignorar) ────────────────────────────────────────────────────
WS : [ \t\r\n]+ -> skip ;
