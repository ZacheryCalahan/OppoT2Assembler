package Assembler;
import java.util.ArrayList;

public class Processor {
    SymbolTable registerTable;
    SymbolTable instructionTable;
    SymbolTable conditionTable;
    SymbolTable labelTable;


    String[] lines;
    int[] instructions;


    public Processor(ArrayList<String> lines, SymbolTable labelTable)  {
        this.lines = lines.toArray(new String[lines.size()]);

        this.labelTable = labelTable;
        instructions = new int[this.lines.length * 2];

        registerTable = new SymbolTable();
        for (int i = 0; i < 32; i++) {
            String key = "r" + i;
            registerTable.put(key, i);
        }
        registerTable.put("SP", 31);  
        instructionTable = new SymbolTable();
        conditionTable = new SymbolTable();
        buildTables();
    }


    // Line by line decoder
    public void buildBin() {
        String[] tokens;
        int mask;
        int instructionOut;
        int offset = 0;
        for (int i = 0; i < lines.length; i++) {
            instructionOut = 0;
            tokens = tokenize(lines[i]);
            switch(determineType(tokens)) {
                case 4: {   //RRCI
                    mask = 0b00000000000000000001111111111111;
                    instructionOut = instructionTable.get(tokens[0]) << 27;                     //opcode
                    instructionOut = (instructionOut | registerTable.get(tokens[1]) << 22);     //rA
                    instructionOut = (instructionOut | registerTable.get(tokens[2]) << 17);     //rB
                    instructionOut = (instructionOut | conditionTable.get(tokens[3]) << 13);    //cond
                    if ((labelTable.get(tokens[4]) - i)> 0x1fff) {
                        Error.warnUser("Immediate is too large. Instruction: " + i);
                    }
                    instructionOut = (instructionOut | (mask & (labelTable.get(tokens[4]) - i)));   //imm
                    
                    instructions[i + offset] = instructionOut;
                    //System.out.println("Type RRCI: " + Integer.toHexString(instructionOut));
                    break;
                } case 3: { //RRI
                    mask = 0b00000000000000011111111111111111;
                    int imm;
                    instructionOut = instructionTable.get(tokens[0]) << 27;                     //opcode
                    instructionOut = (instructionOut | registerTable.get(tokens[1]) << 22);     //rA
                    instructionOut = (instructionOut | registerTable.get(tokens[2]) << 17);     //rB
                    
                    if (tokens[3].startsWith("0x")) {
                        imm = (Integer.parseInt(tokens[3].replace("0x", ""), 16));        //imm
                    } else if (tokens[3].startsWith("0b")) {
                        imm = (Integer.parseInt(tokens[3].replace("0b", ""), 2));
                    } else {
                        imm = (Integer.parseInt(tokens[3]));
                    }
                    if (imm > 0x1FFFF) {
                        Error.warnUser("Immediate is too large. Instruction: " + i);
                    }
                    instructionOut = (instructionOut | (mask & imm));
                    
                    
                    instructions[i + offset] = instructionOut;
                    //System.out.println("Type RRI: " + Integer.toHexString(instructionOut));
                    break;
                } case 2: { //RRR
                    mask = 0;
                    instructionOut = instructionTable.get(tokens[0]) << 27;                     //opcode
                    instructionOut = (instructionOut | registerTable.get(tokens[1]) << 22);     //rA
                    instructionOut = (instructionOut | registerTable.get(tokens[2]) << 17);     //rB
                    instructionOut = (instructionOut | registerTable.get(tokens[3]));           //rC

                    instructions[i + offset] = instructionOut;
                    //System.out.println("Type RRR: " + Integer.toHexString(instructionOut));
                    break;
                } case 1: { //RRI
                    int imm;
                    if (tokens[2].startsWith("0x")) {
                        imm = (Integer.parseUnsignedInt(tokens[2].replace("0x", ""), 16));        //imm
                    } else if (tokens[2].startsWith("0b")) {
                        imm = (Integer.parseUnsignedInt(tokens[2].replace("0b", ""), 2));
                    } else {
                        imm = (Integer.parseUnsignedInt(tokens[2]));
                    }
                    mask = 0b00000000000000000111111111111111;
                    instructionOut = instructionTable.get(tokens[0]) << 27;                     //opcode
                    instructionOut = (instructionOut | registerTable.get(tokens[1]) << 22);     //rA
                    instructionOut = (instructionOut | (mask & imm));   //imm
                    
                    instructions[i + offset] = instructionOut;
                    //System.out.println("Type RRR: " + Integer.toHexString(instructionOut));
                    break;
                } case 5: { //ret
                    instructionOut = instructionTable.get(tokens[0]) << 27;                     //opcode

                    instructions[i + offset] = instructionOut;
                    //System.out.println("Type ret: " + Integer.toHexString(instructionOut));
                    break;
                } case 6: { //STACK OPS
                    instructionOut = instructionTable.get(tokens[0]) << 27;
                    instructionOut = (instructionOut | registerTable.get(tokens[1]) << 22);     //rA

                    instructions[i + offset] = instructionOut;
                    //System.out.println("Type R: " + Integer.toHexString(instructionOut));
                } case 7: { //movi command
                    int imm;
                    if (tokens[2].startsWith("0x")) {
                        imm = (Integer.parseUnsignedInt(tokens[2].replace("0x", ""), 16));        //imm
                    } else if (tokens[2].startsWith("0b")) {
                        imm = (Integer.parseUnsignedInt(tokens[2].replace("0b", ""), 2));
                    } else {
                        imm = (Integer.parseUnsignedInt(tokens[2]));
                    }

                    instructionOut = instructionTable.get("lui") << 27;
                    instructionOut = (instructionOut | registerTable.get(tokens[1]) << 22);     //rA
                    instructionOut = (instructionOut | (0b00000000000000000111111111111111 & imm));
                    instructions[i + offset] = instructionOut;
                    instructionOut = 0;
                    offset++;
                    mask = 0b00000000000000011111111111111111;
                    int movi = imm >> 15;
                    instructionOut = instructionTable.get("addi") << 27;
                    instructionOut = (instructionOut | registerTable.get(tokens[1]) << 22);     //rA
                    instructionOut = (instructionOut | registerTable.get("r0") << 17);
                    instructionOut = (instructionOut | (mask & movi));
                    instructions[i + offset] = instructionOut;
                    break;
                }
            }
        }
    }

    public String[] tokenize(String line) {
        String[] tokens = new String[5];
        tokens = line.split(" ");
        return tokens;
    }

    public int determineType(String[] tokens) {
        switch(tokens.length) {
            case 1: {       //ret
                return 5;
            }
            
            case 2: {
                return 6;   //push & pop
            }
            
            case 3: {
                if (tokens[0].contains("movi")) {
                    return 7;   // Mov
                } else {
                    return 1;   //RI Type
                }
                    
                
                
            }
            case 4: {
                if (tokens[3].contains("r")) {
                    return 2;   //RRR type
                } else {
                    return 3;   //RRI type
                }
            }
            case 5: {
                return 4;   //RRCI type
            } default: {
                return 0;   // Error
            }
        }
        
        
        
    }

    public void buildTables() {
        instructionTable.put("add", 0);
        instructionTable.put("addi", 1);
        instructionTable.put("sub", 2);
        instructionTable.put("subi", 3);
        instructionTable.put("mult", 4);
        instructionTable.put("multi", 5);
        instructionTable.put("xor", 6);
        instructionTable.put("xori", 7);
        instructionTable.put("shl", 8);
        instructionTable.put("shli", 9);
        instructionTable.put("shr", 10);
        instructionTable.put("shri", 11);
        instructionTable.put("neg", 12);
        instructionTable.put("lw", 13);
        instructionTable.put("st", 14);
        instructionTable.put("jmpc", 18);
        instructionTable.put("in", 19);
        instructionTable.put("out", 20);
        instructionTable.put("lui", 27);
        instructionTable.put("call", 28);
        instructionTable.put("ret", 29);
        instructionTable.put("push", 30);
        instructionTable.put("pop", 31);  

        conditionTable.put("=", 0);
        conditionTable.put("!=", 1);
        conditionTable.put(">", 2);
        conditionTable.put(">=", 3);
        conditionTable.put("<", 4);
        conditionTable.put("<=", 5);
    }
}
