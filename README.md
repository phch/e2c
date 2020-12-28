# Introduction
Old college project in Java to translate E programs to their semantically equivalent C program.

The general approach is to perform this translation in one pass, which means syntactic analysis, semantic analysis, and code generation are all performed in one pass.

This project was divided and implemented in several parts:
* Scanner
* Parser
* Symbol table
* Code generation

# E language BNF
    program ::= block
    block ::= declaration_list statement_list
    declaration_list ::= {declaration}
    statement_list ::= {statement}
    declaration ::= ’@’ id { ’,’ id }
    statement ::= assignment | print | do | if
    print ::= ’!’ expr
    assignment ::= ref_id ’=’ expr
    ref_id ::= [ ’˜’ [ number ] ] id
    do ::= ’<’ guarded_command ’>’
    if ::= ’[’ guarded_command { ’|’ guarded_command } [ ’%’ block ] ’]’
    guarded_command ::= expr ’:’ block
    expr ::= term { addop term }
    term ::= factor { multop factor }
    factor ::= ’(’ expr ’)’ | ref_id | number
    addop ::= ’+’ | ’-’
    multop ::= ’*’ | ’/’
