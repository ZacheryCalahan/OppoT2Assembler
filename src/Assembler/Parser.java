package Assembler;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class Parser {
    ArrayList<String> lines = new ArrayList<String>();
    SymbolTable labelTable = new SymbolTable();
    File asmCode;

    public Parser(File asmCode) throws IOException {
        this.asmCode = asmCode;
        getLines();
    }

    public void getLines() throws IOException {
        Scanner sc = new Scanner(asmCode);
        int instr = 0;
        String currLine;
        while(sc.hasNextLine()) {
            currLine = sc.nextLine();
            if (!currLine.startsWith("#")) {
                String[] code = currLine.split("#");   // Remove comments
                while(code[0].startsWith(" ")) {
                    code[0] = code[0].stripIndent();      // Remove whitespace
                }
                
                if (!code[0].isEmpty()){
                    if (!parseLabel(code[0], instr)) {
                        lines.add(code[0]);
                        instr++;
                    }
                }
            }
        }
        sc.close();
    }

    public boolean parseLabel(String currentLine, int index) {
        if (currentLine.replace(" ", "").endsWith(":")) {
            labelTable.addSymbol(index, currentLine.replace(" ", "").replace(":", ""));
            return true;
        }
        return false;
    }

    //Debug
    public void printLines() {
        for (String string : lines) {
            System.out.println(string);
        }
    }

    public void printLabels() {
        System.out.println(labelTable.sym_table);
    }
}
