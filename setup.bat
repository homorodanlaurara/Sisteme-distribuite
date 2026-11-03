@echo off
echo Se creeaza structura si fisierele proiectului...

:: Creare directoare
mkdir docs scripts src\main\java\ro\ubm\sd\client src\main\java\ro\ubm\sd\node src\main\java\ro\ubm\sd\common data 2>nul

:: .gitignore
echo target/> .gitignore
echo data/ >> .gitignore
echo .idea/ >> .gitignore
echo *.iml >> .gitignore
echo *.class >> .gitignore

:: pom.xml
echo ^<?xml version="1.0" encoding="UTF-8"?^> > pom.xml
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^> >> pom.xml
echo     ^<modelVersion^>4.0.0^</modelVersion^> >> pom.xml
echo     ^<groupId^>ro.ubm.sd^</groupId^> >> pom.xml
echo     ^<artifactId^>sd-storage^</artifactId^> >> pom.xml
echo     ^<version^>0.1.0^</version^> >> pom.xml
echo     ^<properties^> >> pom.xml
echo         ^<maven.compiler.source^>17^</maven.compiler.source^> >> pom.xml
echo         ^<maven.compiler.target^>17^</maven.compiler.target^> >> pom.xml
echo         ^<project.build.sourceEncoding^>UTF-8^</project.build.sourceEncoding^> >> pom.xml
echo     ^/properties^> >> pom.xml
echo ^</project^> >> pom.xml

:: README.md
echo # Distributed Storage System (sd-storage) > README.md
echo ## Membrii Echipei >> README.md
echo * Laura >> README.md
echo ## Compilare >> README.md
echo mvn clean package >> README.md

:: docs/cerinte.md
echo # Cerinte Etapa 1 > docs\cerinte.md
echo Proiect SD - Sistem de stocare distribuit. >> docs\cerinte.md

:: Config.java
echo package ro.ubm.sd.common; > src\main\java\ro\ubm\sd\common\Config.java
echo import java.util.HashMap; >> src\main\java\ro\ubm\sd\common\Config.java
echo import java.util.Map; >> src\main\java\ro\ubm\sd\common\Config.java
echo public class Config { >> src\main\java\ro\ubm\sd\common\Config.java
echo     public static Map^<String, String^> parseArgs(String[] args) { >> src\main\java\ro\ubm\sd\common\Config.java
echo         Map^<String, String^> map = new HashMap^<^>(); >> src\main\java\ro\ubm\sd\common\Config.java
echo         for (int i = 0; i ^< args.length; i++) { >> src\main\java\ro\ubm\sd\common\Config.java
echo             if (args[i].startsWith("--")) { >> src\main\java\ro\ubm\sd\common\Config.java
echo                 String key = args[i].substring(2); >> src\main\java\ro\ubm\sd\common\Config.java
echo                 if (i + 1 ^< args.length ^&^& !args[i + 1].startsWith("--")) { >> src\main\java\ro\ubm\sd\common\Config.java
echo                     map.put(key, args[i + 1]); >> src\main\java\ro\ubm\sd\common\Config.java
echo                     i++; >> src\main\java\ro\ubm\sd\common\Config.java
echo                 } else { >> src\main\java\ro\ubm\sd\common\Config.java
echo                     map.put(key, "true"); >> src\main\java\ro\ubm\sd\common\Config.java
echo                 } >> src\main\java\ro\ubm\sd\common\Config.java
echo             } >> src\main\java\ro\ubm\sd\common\Config.java
echo         } >> src\main\java\ro\ubm\sd\common\Config.java
echo         return map; >> src\main\java\ro\ubm\sd\common\Config.java
echo     } >> src\main\java\ro\ubm\sd\common\Config.java
echo } >> src\main\java\ro\ubm\sd\common\Config.java

:: StorageNode.java
echo package ro.ubm.sd.node; > src\main\java\ro\ubm\sd\node\StorageNode.java
echo import ro.ubm.sd.common.Config; >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo import java.io.File; >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo import java.util.Map; >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo public class StorageNode { >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo     public static void main(String[] args) { >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo         Map^<String, String^> params = Config.parseArgs(args); >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo         String id = params.getOrDefault("id", "n-unknown"); >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo         int port = Integer.parseInt(params.getOrDefault("port", "7001")); >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo         String dirPath = params.getOrDefault("dir", "data/" + id); >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo         boolean isCoordinator = params.containsKey("coordinator"); >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo         File dir = new File(dirPath); >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo         if (!dir.exists()) { dir.mkdirs(); } >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo         String role = isCoordinator ? "nod de stocare (si coordonator)" : "nod de stocare"; >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo         System.out.printf("[%s] pornit: port %d, director %s, rol: %s%%n", id, port, dirPath, role); >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo     } >> src\main\java\ro\ubm\sd\node\StorageNode.java
echo } >> src\main\java\ro\ubm\sd\node\StorageNode.java

:: Client.java
echo package ro.ubm.sd.client; > src\main\java\ro\ubm\sd\client\Client.java
echo import ro.ubm.sd.common.Config; >> src\main\java\ro\ubm\sd\client\Client.java
echo import java.util.Map; >> src\main\java\ro\ubm\sd\client\Client.java
echo import java.util.Scanner; >> src\main\java\ro\ubm\sd\client\Client.java
echo public class Client { >> src\main\java\ro\ubm\sd\client\Client.java
echo     public static void main(String[] args) { >> src\main\java\ro\ubm\sd\client\Client.java
echo         Map^<String, String^> params = Config.parseArgs(args); >> src\main\java\ro\ubm\sd\client\Client.java
echo         String coordinator = params.getOrDefault("coordinator", "localhost:7001"); >> src\main\java\ro\ubm\sd\client\Client.java
echo         System.out.printf("[client] coordonator configurat: %s; comenzi: put, get, ls, rm, nodes, exit%%n", coordinator); >> src\main\java\ro\ubm\sd\client\Client.java
echo         Scanner scanner = new Scanner(System.in); >> src\main\java\ro\ubm\sd\client\Client.java
echo         while (true) { >> src\main\java\ro\ubm\sd\client\Client.java
echo             System.out.print("sd-client^> "); >> src\main\java\ro\ubm\sd\client\Client.java
echo             String line = scanner.nextLine().trim(); >> src\main\java\ro\ubm\sd\client\Client.java
echo             if ("exit".equalsIgnoreCase(line)) { break; } >> src\main\java\ro\ubm\sd\client\Client.java
echo             if (!line.isEmpty()) { System.out.println("Comanda primita: " + line); } >> src\main\java\ro\ubm\sd\client\Client.java
echo         } >> src\main\java\ro\ubm\sd\client\Client.java
echo         scanner.close(); >> src\main\java\ro\ubm\sd\client\Client.java
echo     } >> src\main\java\ro\ubm\sd\client\Client.java
echo } >> src\main\java\ro\ubm\sd\client\Client.java
