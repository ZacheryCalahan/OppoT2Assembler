package Assembler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class Main {
    public static void main(String[] args) {

        //Objects
        File asmCode;
        File asmOut;
        FileWriter fw;
        Parser p;
     
        try {
            asmCode = new File(args[0]);
            asmOut = new File(args[1]);
            fw = new FileWriter(asmOut);
            p = new Parser(asmCode);
    
            // Build binary
            Processor build = new Processor(p.lines, p.labelTable);
            build.buildBin();
    
            for (int instr : build.instructions) {
                if (instr != 0) {
                    fw.append(Integer.toHexString(instr) + "\n");
                    //System.out.println(Integer.toHexString(instr));
                }
                
                
            }
    
            fw.close();
        } catch (IOException e) {
            System.out.println("File not found. Expected args <assembly-file.s> <File-output>");
            e.printStackTrace();
        }

       
    }
}