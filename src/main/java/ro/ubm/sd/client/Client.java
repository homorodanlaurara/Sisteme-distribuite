package ro.ubm.sd.client; 
import ro.ubm.sd.common.Config; 
import java.util.Map; 
import java.util.Scanner; 
public class Client { 
    public static void main(String[] args) { 
        Map<String, String> params = Config.parseArgs(args); 
        String coordinator = params.getOrDefault("coordinator", "localhost:7001"); 
        System.out.printf("[client] coordonator configurat:  put, get, ls, rm, nodes, exit%n", coordinator); 
        Scanner scanner = new Scanner(System.in); 
        while (true) { 
            System.out.print("sd-client^> "); 
            String line = scanner.nextLine().trim(); 
            if ("exit".equalsIgnoreCase(line)) { break; } 
            if (!line.isEmpty()) { System.out.println("Comanda primita: " + line); } 
        } 
        scanner.close(); 
    } 
} 
