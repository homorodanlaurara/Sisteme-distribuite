package ro.ubm.sd.common; 
import java.util.HashMap; 
import java.util.Map; 
public class Config { 
    public static Map<String, String> parseArgs(String[] args) { 
        Map<String, String> map = new HashMap<>(); 
        for (int i = 0; i < args.length; i++) { 
            if (args[i].startsWith("--")) { 
                String key = args[i].substring(2); 
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) { 
                    map.put(key, args[i + 1]); 
                    i++; 
                } else { 
                    map.put(key, "true"); 
                } 
            } 
        } 
        return map; 
    } 
} 
