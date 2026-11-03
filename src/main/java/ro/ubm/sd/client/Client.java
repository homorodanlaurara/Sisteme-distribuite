package ro.ubm.sd.client;

import ro.ubm.sd.common.Config;
import ro.ubm.sd.common.Protocol;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        System.out.println("[client] Comenzi disponibile: nodes, put <port> <fisier>, get <port> <fisier> <destinatie>, exit");
        Scanner scanner = new Scanner(System.in);

        int teamGroupNumber = 1;
        String multicastGroupIp = "239.255.0." + teamGroupNumber;
        int multicastPort = 4446;

        while (true) {
            System.out.print("sd-client> ");
            String line = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(line)) {
                break;
            }

            String[] parts = line.split("\\s+");
            if (parts.length == 0 || parts[0].isEmpty()) continue;

            String cmd = parts[0];

            if ("nodes".equalsIgnoreCase(cmd)) {
                listNodes(multicastGroupIp, multicastPort);
            } else if ("put".equalsIgnoreCase(cmd) && parts.length >= 3) {
                int port = Integer.parseInt(parts[1]);
                Path file = Path.of(parts[2]);
                sendFile("localhost", port, file);
            } else if ("get".equalsIgnoreCase(cmd) && parts.length >= 4) {
                int port = Integer.parseInt(parts[1]);
                String fileName = parts[2];
                Path destDir = Path.of(parts[3]);
                getFile("localhost", port, fileName, destDir);
            } else {
                System.out.println("Comanda necunoscuta sau parametri gresiti!");
            }
        }
        scanner.close();
    }

    private static void listNodes(String groupIp, int port) {
        System.out.println("Ascult nodurile active timp de 3 secunde...");
        try (MulticastSocket socket = new MulticastSocket(port)) {
            InetAddress group = InetAddress.getByName(groupIp);
            socket.joinGroup(new InetSocketAddress(group, port), null);
            socket.setSoTimeout(3000);

            long end = System.currentTimeMillis() + 3000;
            byte[] buffer = new byte[1024];

            while (System.currentTimeMillis() < end) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    String[] p = msg.split(";");
                    if (p.length == 3 && p[0].equals("HELLO")) {
                        System.out.printf("%s@%s:%s%n", p[1], packet.getAddress().getHostAddress(), p[2]);
                    }
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Eroare la ascultarea multicast: " + e.getMessage());
        }
    }

    private static void sendFile(String host, int port, Path file) {
        if (!Files.exists(file)) {
            System.out.println("Fisierul local nu exista: " + file);
            return;
        }
        try (Socket socket = new Socket(host, port);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            String sha = Protocol.checksum(file);
            Protocol.writeHeader(out, "PUT " + file.getFileName() + " " + Files.size(file) + " " + sha);

            try (InputStream data = Files.newInputStream(file)) {
                data.transferTo(out);
            }
            out.flush();

            String[] response = Protocol.readHeader(in);
            System.out.println("Raspuns nod: " + String.join(" ", response));
        } catch (IOException e) {
            System.out.println("Eroare la trimiterea fisierului: " + e.getMessage());
        }
    }

    private static void getFile(String host, int port, String fileName, Path destDir) {
        try (Socket socket = new Socket(host, port);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            Protocol.writeHeader(out, "GET " + fileName);
            String[] response = Protocol.readHeader(in);

            if (response.length > 0 && "NOT_FOUND".equalsIgnoreCase(response[0])) {
                System.out.println("NOT_FOUND");
                return;
            }

            if (response.length >= 3 && "FILE".equalsIgnoreCase(response[0])) {
                long size = Long.parseLong(response[1]);
                String expectedSha = response[2];

                if (!Files.exists(destDir)) {
                    Files.createDirectories(destDir);
                }
                Path target = destDir.resolve(fileName);
                String actualSha = Protocol.receiveBytes(in, size, target);

                if (actualSha.equals(expectedSha)) {
                    System.out.printf("descarcat %s, checksum OK%n", target);
                } else {
                    System.out.println("Eroare: Checksum diferit la descarcare!");
                }
            }
        } catch (IOException e) {
            System.out.println("Eroare la descarcare: " + e.getMessage());
        }
    }
}