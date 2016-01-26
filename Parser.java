/* *** This file is given as part of the programming assignment. *** */
import java.util.*;

public class Parser {

	public static class SymbolTable {
        // Mimicking "stack" behavior with vector class
		ArrayList<ArrayList<String>> scoping = new ArrayList<ArrayList<String>>();

		private void addList() {
            // Add to the back of "stack"
			scoping.add(new ArrayList<String>());
		}   

		private void removeList() {
            // Delete from back of "stack"
			scoping.remove(scoping.size() - 1);
		}

		private void addVariable(String s) {
            // Array list for current scope will be at the top of "stack"
			int lastElementIndex = scoping.size() - 1;
			ArrayList<String> arrList = scoping.get(lastElementIndex);
			boolean undeclared = true;

            // Check if variable is valid (undeclared variable)
			for (String var : arrList) {
                // Found a previous declaration of the variable in the same scope
				if (var.equals(s)) {
                    // Send a warning and continue by ignoring redeclaration
					undeclared = false;
					redeclaration_error(s);
				}
			}

			if (undeclared) {
				arrList.add(s);
				scoping.set(lastElementIndex, arrList);
				
                // Debug code
                // System.out.println(s + " at scope " + scoping.size());
			}
		}

        private void checkDeclaration(Token tk){
            boolean exists = false;
            //if there is an undeclaration error
            for (ArrayList<String> arr : scoping) {
                for(String var : arr) {
                    if (tk.string.equals(var))
                        exists = true;
                }
            }
            if (!exists) {
                undeclared_error(tk);
            }
        }

        private void checkDeclaration(String prefix, Token tk){
            boolean exists = false;
            String varName = prefix + tk.string;

            // '~<id>' is global scope, denoted by -1
            int scope = (prefix.substring(1).equals("")) ? -1 : Integer.parseInt(prefix.substring(1));
            
            // Search global block or specific block for variable declaration
            int blockIndex = (scope == -1) ? 0 : scoping.size() - 1 - scope;

            // Bad '~' scoping reference
            if (blockIndex < 0)
                scoping_error(varName, tk.lineNumber);

            for(String var : scoping.get(blockIndex)) {
                if (tk.string.equals(var))
                    exists = true;
            }
            
            if (!exists) {
                scoping_error(varName, tk.lineNumber);
            }
        }

        // Error statements
		private void redeclaration_error(String s) {
			System.err.print( "redeclaration of variable " + s + '\n');
		}

		private void undeclared_error(Token tk) {
			System.err.print(tk.string + " is an undeclared variable on line " + tk.lineNumber + '\n');
			System.exit(1);
		}

        private void scoping_error(String s, int lineNumber) {
            System.err.print( "no such variable " + s + " on line " + lineNumber + "\n");
            System.exit(1);
        }
	}

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
        System.out.print("main()");
    	block();
    }

    private void block(){
    	st.addList();
        System.out.print("{");
    	declaration_list();
    	statement_list();
    	st.removeList();
        System.out.print("}");
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
        System.out.print("int ");
        // Add variable declaration to block array list
    	st.addVariable(tok.string);
    	System.out.print("x_" + tok.string);
        mustbe(TK.ID);
    	while( is(TK.COMMA) ) {
    		scan();

            // Add variable declaration to block array list
    		st.addVariable(tok.string);
            System.out.print(", x_" + tok.string);
    		mustbe(TK.ID);
    	}
        System.out.print(";");
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
        System.out.print("printf(\"%d\", ");
    	expr();
        System.out.print(");");
    }

    private void assignment() {
    	ref_id();
        System.out.print("=");
    	mustbe(TK.ASSIGN);
    	expr();
        System.out.print(";");
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

        System.out.print("x_" + tok.string);
    	mustbe(TK.ID);
    }


    private void DO(){
        System.out.print("while");
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
        System.out.print("(");
    	expr();
        System.out.print(")")
    	mustbe(TK.THEN);
    	block();
    }

    private void expr(){
    	term();
    	while (is(TK.PLUS) || is(TK.MINUS)){
            if (is(TK.PLUS))
                System.out.print("+");
            if (is(TK.MINUS))
                System.out.print("-");
    		scan();
    		term();
    	}
    }

    private void term() {
    	factor();
    	while (is(TK.TIMES) || is(TK.DIVIDE)){
            if (is(TK.TIMES))
                System.out.print("*");
            if (is(TK.DIVIDE))
                System.out.print("/");
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
            System.out.print(tok.string);
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