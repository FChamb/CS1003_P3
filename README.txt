CS1003 Practical 3

Description:
This program contains four classes which work together to create a database and initialize it properly. Then
that database is filled with entries for three given authors: "Alan Dearle", "Ian Gent", and "Ozgur Akgun".
Finally, the database can be queried to retrieve five different result pertaining to information stored. A
test class is also provided to check that the four main classes function properly. A text file containing
DDL script is provided to show the table schema for the database should a manual initialization be required.
Lastly an ER Diagram of the database relationship schema is also included.

Usage:
To begin using this code follow these instructions:
1. Run the command: export CLASSPATH=${CLASSPATH}:./sqlite-jdbc-3.40.1.0.jar
2. Run the command: javac *.java
3. Run the command: java InitialiseDB.java
4. Run the command: java PopulateDB.java
5. Run the command: java QueryDB.java # <# represents a query matched to the specifications i.e. 1-5>
6. Optionally, run: java Test.java