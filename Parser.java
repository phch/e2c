/* *** This file is given as part of the programming assignment. *** */
import java.util.*;

public class Parser {

	SymbolTable st = new SymbolTable();

    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
    	tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
    	this.scanner = scanner;
    	scan();
    	program();
    	if( tok.kind != TK.EOF )
    		parse_error("junk after logical end of program");
    }

    private void program() {
    	block();
    }

    private void block(){
    	st.addList();
    	declaration_list();
    	statement_list();
    	st.removeList();
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.

    	while( is(TK.DECLARE) ) {
    		declaration();
    	}
    }

    private void declaration() {
    	mustbe(TK.DECLARE);

        // Add variable declaration to block array list
    	st.addVariable(tok.string);

    	mustbe(TK.ID);
    	while( is(TK.COMMA) ) {
    		scan();

            // Add variable declaration to block array list
    		st.addVariable(tok.string);

    		mustbe(TK.ID);
    	}
    }

    private void statement_list() {
    	while(is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF)){
    		statement();
    	}
    }

    private void statement() {
    	if (is(TK.PRINT)){
    		print();
    	}

    	else if (is(TK.DO)){
    		DO();
    	}	

    	else if (is(TK.IF)){
    		IF();
    	}

    	else if (is(TK.TILDE) || is(TK.ID)) {
    		assignment();
    	}
    }

    private void print() {
    	mustbe(TK.PRINT);
    	expr();
    }

    private void assignment() {
    	ref_id();
    	mustbe(TK.ASSIGN);
    	expr();
    }

    private void ref_id() {
        boolean checkScope = false;
        String prefix = null;
    	if(is(TK.TILDE)) {
            prefix = "~";
    		scan();
    		if (is(TK.NUM)) {
                prefix = prefix + tok.string;
    			scan();
            }
    	}

        // Ensure that the ID has been declared
        if (prefix != null) {
            // Variable scope restricted by '~'
            st.checkDeclaration(prefix, tok);
        } 
        else {
            // Check all declaration blocks
            st.checkDeclaration(tok);
        }

    	mustbe(TK.ID);
    }


    private void DO(){
    	mustbe(TK.DO);
    	guarded_command();
    	mustbe(TK.ENDDO);
    }

    private void IF(){
    	mustbe(TK.IF);
    	guarded_command();
    	while (is(TK.ELSEIF)){
    		scan();
    		guarded_command();
    	}
    	if(is(TK.ELSE)){
    		scan();
    		block();
    	}
    	mustbe(TK.ENDIF); 
    }

    private void guarded_command(){
    	expr();
    	mustbe(TK.THEN);
    	block();
    }

    private void expr(){
    	term();
    	while (is(TK.PLUS) || is(TK.MINUS)){
    		scan();
    		term();
    	}
    }

    private void term() {
    	factor();
    	while (is(TK.TIMES) || is(TK.DIVIDE)){
    		scan();
    		factor(); 
    	}
    }

    private void factor(){
    	if (is(TK.LPAREN)){
    		scan();
    		expr();
    		mustbe(TK.RPAREN);
    	}

    	else if (is(TK.TILDE) || is(TK.ID)){
    		ref_id();
    	}

    	else if (is(TK.NUM)){
    		scan();
    	}
    }

    // is current token what we want?
    private boolean is(TK tk) {
    	return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
    	if( tok.kind != tk ) {
    		System.err.print( "mustbe: want " + tk + ", got " +
    			tok + "\n");
    		parse_error( "missing token (mustbe)");
    	}
    	scan();
    }

    private void parse_error(String msg) {
    	System.err.print( "can't parse: line "
    		+ tok.lineNumber + " " + msg + "\n" );
    	System.exit(1);
    }
}