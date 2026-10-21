package ro.ubm.sd.node; 
import ro.ubm.sd.common.Config; 
import java.io.File; 
import java.util.Map; 
public class StorageNode { 
    public static void main(String[] args) { 
        Map<String, String> params = Config.parseArgs(args); 
        String id = params.getOrDefault("id", "n-unknown"); 
        int port = Integer.parseInt(params.getOrDefault("port", "7001")); 
        String dirPath = params.getOrDefault("dir", "data/" + id); 
        boolean isCoordinator = params.containsKey("coordinator"); 
        File dir = new File(dirPath); 
        if (!dir.exists()) { dir.mkdirs(); } 
        String role = isCoordinator ? "nod de stocare (si coordonator)" : "nod de stocare"; 
        System.out.printf("[ port s, rol: n", id, port, dirPath, role); 
    } 
} 
