SHELL  = /bin/sh

# pretty minimal makefile
e2c:
	javac *.java

# invoke via "make clean".
# WARNING: make sure you know what this is going to do before you invoke it!!!
<<<<<<< HEAD
# (N.B., in this part it removes .c files too!)
clean:
	/bin/rm -f *.class *~ core* *.output a.out *.c *.o
=======
clean:
	/bin/rm -f *.class *~ core* *.output
>>>>>>> 1a95f34a90c8ce440244c2a8feae4c218e142d3d

# just do `make remake' instead of `make clean; make'
remake: clean e2c
