package ro.ubm.sd.node;

import ro.ubm.sd.common.Config;
import ro.ubm.sd.common.Protocol;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.*;

public class StorageNode {

    private record Member(String id, InetAddress address, int port, long lastSeen) {}

    public static void main(String[] args) {
        Map<String, String> params = Config.parseArgs(args);
        String id = params.getOrDefault("id", "n1");
        int tcpPort = Integer.parseInt(params.getOrDefault("port", "7001"));
        Path dir = Path.of(params.getOrDefault("dir", "data/" + id));

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 1. Pornire Server TCP pentru fisiere
        ExecutorService threadPool = Executors.newCachedThreadPool();
        threadPool.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
                System.out.printf("[%s] Server TCP pornit pe portul %d%n", id, tcpPort);
                while (true) {
                    Socket socket = serverSocket.accept();
                    threadPool.submit(() -> handleClientConnection(socket, dir, id));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 2. Pornire Multicast (Anunturi HELLO si Ascultare)
        ConcurrentMap<String, Member> members = new ConcurrentHashMap<>();
        int teamGroupNumber = 1; // Schimba numarul echipei daca este necesar (ex: 239.255.0.k)
        String multicastGroupIp = "239.255.0." + teamGroupNumber;
        int multicastPort = 4446;

        // Fir de trimis anunțuri HELLO la fiecare 2 secunde
        threadPool.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress group = InetAddress.getByName(multicastGroupIp);
                String message = "HELLO;" + id + ";" + tcpPort;
                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);
                    socket.send(packet);
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Fir de ascultare grup multicast
        threadPool.submit(() -> {
            try (MulticastSocket socket = new MulticastSocket(multicastPort)) {
                InetAddress group = InetAddress.getByName(multicastGroupIp);
                InetSocketAddress socketAddress = new InetSocketAddress(group, multicastPort);
                NetworkInterface netIf = NetworkInterface.getByName("wlan0"); // sau null / eth0 in functie de sistem
                socket.joinGroup(socketAddress, netIf);

                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    String[] p = msg.split(";");
                    if (p.length != 3 || !p[0].equals("HELLO") || p[1].equals(id)) continue;

                    Member m = new Member(p[1], packet.getAddress(), Integer.parseInt(p[2]), System.currentTimeMillis());
                    if (members.put(m.id(), m) == null) {
                        System.out.printf("[%s] nod descoperit: %s@%s:%d (cunosc acum %d)%n",
                                id, m.id(), m.address().getHostAddress(), m.port(), members.size());
                    }
                }
            } catch (Exception e) {
                // Gestionare erori retea multicast
            }
        });

        System.out.printf("[%s] Nod complet functional. Director: %s%n", id, dir);
    }

    private static void handleClientConnection(Socket socket, Path dir, String nodeId) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            String[] h = Protocol.readHeader(in);
            if (h.length == 0) return;

            String command = h[0];
            if ("PUT".equalsIgnoreCase(command) && h.length >= 4) {
                handlePut(h, in, out, dir);
            } else if ("GET".equalsIgnoreCase(command) && h.length >= 2) {
                handleGet(h, in, out, dir);
            }
        } catch (IOException e) {
            // Conexiune inchisa
        }
    }

    private static void handlePut(String[] h, DataInputStream in, DataOutputStream out, Path dir) throws IOException {
        String name = h[1];
        long size = Long.parseLong(h[2]);
        String expected = h[3];
        Path tmp = dir.resolve(name + ".part");
        String actual = Protocol.receiveBytes(in, size, tmp);

        if (!actual.equals(expected)) {
            Files.deleteIfExists(tmp);
            Protocol.writeHeader(out, "ERROR checksum_diferit");
            return;
        }
        Files.move(tmp, dir.resolve(name), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        Protocol.writeHeader(out, "STORED " + actual);
    }

    private static void handleGet(String[] h, DataInputStream in, DataOutputStream out, Path dir) throws IOException {
        String name = h[1];
        Path file = dir.resolve(name);
        if (!Files.exists(file)) {
            Protocol.writeHeader(out, "NOT_FOUND");
            return;
        }
        long size = Files.size(file);
        String sha = Protocol.checksum(file);
        Protocol.writeHeader(out, "FILE " + size + " " + sha);
        try (InputStream fis = Files.newInputStream(file)) {
            fis.transferTo(out);
        }
        out.flush();
    }
}