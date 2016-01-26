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