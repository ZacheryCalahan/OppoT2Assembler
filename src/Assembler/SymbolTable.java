package Assembler;
import java.util.Hashtable;

public class SymbolTable {
    Hashtable<String,Integer> sym_table = new Hashtable<String, Integer>();
    
    public SymbolTable() {
        
    }

    public void addSymbol(int lineIndex, String symbol) {
        if (!sym_table.contains(symbol)) {
            sym_table.put(symbol, lineIndex);
        }
    }

    public boolean contains(String label) {
        return sym_table.contains(label);
    }

    public void put(String symbol, int value) {
        sym_table.put(symbol, value);
    }
    
    public int get(String key) {
        return sym_table.get(key);
    }
    
            

}
