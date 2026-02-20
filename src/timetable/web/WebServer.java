package timetable.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import timetable.*;

public class WebServer {

    public static void main(String[] args) throws IOException {
        String portStr = System.getenv("PORT");
        int port = (portStr != null) ? Integer.parseInt(portStr) : 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Serve static files (index.html)
        server.createContext("/", new StaticHandler());

        // API Endpoint for generation
        server.createContext("/api/generate", new GenerateHandler());

        server.setExecutor(null);
        server.start();

        String ip = InetAddress.getLocalHost().getHostAddress();
        System.out.println("==================================================");
        System.out.println("MOBILE SERVER STARTED!");
        System.out.println(" To use on your phone:");
        System.out.println(" 1. Connect phone to same WiFi as this PC.");
        System.out.println(" 2. Open Chrome/Safari on phone.");
        System.out.println(" 3. Go to: http://" + ip + ":" + port);
        System.out.println("==================================================");
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/"))
                path = "/index.html";

            // Basic security: prevent directory traversal
            if (path.contains("..")) {
                sendResponse(t, 403, "403 Forbidden");
                return;
            }

            // Read from available web directory
            String[] possibleRoots = {
                    "src/timetable/web",
                    "web",
                    "src/web",
                    "."
            };

            String foundPath = null;
            for (String root : possibleRoots) {
                String fullPath = root + path;
                if (Files.exists(Paths.get(fullPath)) && !Files.isDirectory(Paths.get(fullPath))) {
                    foundPath = fullPath;
                    break;
                }
            }

            if (foundPath != null) {
                System.out.println("Serving: " + foundPath);
                byte[] response = Files.readAllBytes(Paths.get(foundPath));
                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

                // Set Content-Type
                if (path.endsWith(".html"))
                    t.getResponseHeaders().add("Content-Type", "text/html");
                else if (path.endsWith(".css"))
                    t.getResponseHeaders().add("Content-Type", "text/css");
                else if (path.endsWith(".js"))
                    t.getResponseHeaders().add("Content-Type", "application/javascript");

                t.sendResponseHeaders(200, response.length);
                OutputStream os = t.getResponseBody();
                os.write(response);
                os.close();
            } else {
                System.err.println("File Not Found: " + path + " (Checked " + Arrays.toString(possibleRoots) + ")");
                sendResponse(t, 404, "404 Not Found: " + path);
            }
        }
    }

    private static void sendResponse(HttpExchange t, int code, String response) throws IOException {
        t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(code, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }

    static class GenerateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Handle CORS preflight
            if ("OPTIONS".equals(t.getRequestMethod())) {
                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
                t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                t.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(t.getRequestMethod())) {
                InputStream is = t.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                try {
                    String result = processRequest(body);
                    t.getResponseHeaders().add("Content-Type", "application/json");
                    sendResponse(t, 200, result);
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMsg = "Execution Error: "
                            + (e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                    sendResponse(t, 500, "{\"status\": \"error\", \"message\": \"" + escapeJson(errorMsg) + "\"}");
                }
            } else {
                sendResponse(t, 405, "{\"status\": \"error\", \"message\": \"405 Method Not Allowed\"}");
            }
        }

        private String escapeJson(String s) {
            if (s == null)
                return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }

        private String processRequest(String body) {
            int periodsPerDay = 8;
            List<String> days = new ArrayList<>();
            List<ClassRoom> classes = new ArrayList<>();
            List<Teacher> teachers = new ArrayList<>();

            String[] lines = body.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split("\\|");
                String type = parts[0];

                if (type.equals("CONFIG")) {
                    periodsPerDay = Integer.parseInt(parts[1]);
                    String[] d = parts[2].split(",");
                    for (String day : d)
                        days.add(day.trim());
                } else if (type.equals("CLASS")) {
                    String name = parts[1];
                    ClassRoom c = new ClassRoom(name);
                    classes.add(c);
                    for (int i = 2; i < parts.length; i++) {
                        String[] subParts = parts[i].split(":");
                        String subName = subParts[0];
                        int lec = Integer.parseInt(subParts[1]);
                        String combo = (subParts.length > 2) ? subParts[2] : "";
                        List<String> combinedList = new ArrayList<>();
                        if (!combo.isEmpty() && !combo.equals("None")) {
                            for (String cc : combo.split(";"))
                                combinedList.add(cc);
                        }
                        c.addSubject(subName, lec, combinedList);
                    }
                } else if (type.equals("TEACHER")) {
                    String name = parts[1];
                    Teacher teacher = new Teacher(name);
                    teachers.add(teacher);
                    if (parts.length > 2 && !parts[2].isEmpty()) {
                        String[] teachItems = parts[2].split(";");
                        for (String item : teachItems) {
                            String[] p = item.split(":");
                            if (p.length == 2)
                                teacher.addTeaches(p[0], p[1]);
                        }
                    }
                    if (parts.length > 3 && !parts[3].isEmpty()) {
                        String[] naItems = parts[3].split(";");
                        for (String item : naItems) {
                            String[] p = item.split(":");
                            String day = p[0];
                            String[] per = p[1].split(",");
                            List<Integer> perList = new ArrayList<>();
                            for (String pp : per)
                                perList.add(Integer.parseInt(pp));
                            teacher.addNotAvailable(day, perList);
                        }
                    }
                } else if (type.equals("FIXED")) {
                    // FIXED|className|day|period|subject|teacherName
                    String className = parts[1];
                    String day = parts[2];
                    int period = Integer.parseInt(parts[3]);
                    String sub = parts[4];
                    String tName = parts[5];

                    for (ClassRoom c : classes) {
                        if (c.name.equals(className)) {
                            c.addFixedPeriod(day, period, sub, tName);
                            break;
                        }
                    }
                }
            }

            TimetableGenerator tg = new TimetableGenerator(days, periodsPerDay, teachers, classes);
            if (tg.generate()) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"status\": \"success\",");
                sb.append("\"config\": {");
                sb.append("\"periods\": ").append(periodsPerDay).append(",");
                sb.append("\"days\": [");
                for (int i = 0; i < days.size(); i++) {
                    sb.append("\"").append(escapeJson(days.get(i))).append("\"").append(i < days.size() - 1 ? "," : "");
                }
                sb.append("]},");

                // Class Schedules
                sb.append("\"classSchedules\": {");
                for (int i = 0; i < classes.size(); i++) {
                    ClassRoom cls = classes.get(i);
                    sb.append("\"").append(escapeJson(cls.name)).append("\": {");
                    Map<Integer, TimetableEntry> sched = tg.classSchedule.get(cls.name);
                    List<Integer> keys = new ArrayList<>(sched.keySet());
                    for (int j = 0; j < keys.size(); j++) {
                        int key = keys.get(j);
                        TimetableEntry entry = sched.get(key);
                        sb.append("\"").append(key).append("\": {");
                        sb.append("\"subject\": \"").append(escapeJson(entry.subject)).append("\",");
                        sb.append("\"teacher\": \"").append(escapeJson(entry.teacherName)).append("\"");
                        sb.append("}").append(j < keys.size() - 1 ? "," : "");
                    }
                    sb.append("}").append(i < classes.size() - 1 ? "," : "");
                }
                sb.append("},");

                // Teacher Schedules
                sb.append("\"teacherSchedules\": {");
                for (int i = 0; i < teachers.size(); i++) {
                    Teacher t = teachers.get(i);
                    sb.append("\"").append(escapeJson(t.name)).append("\": {");
                    Map<Integer, String> sched = tg.teacherSchedule.get(t.name);
                    List<Integer> keys = new ArrayList<>(sched.keySet());
                    for (int j = 0; j < keys.size(); j++) {
                        int key = keys.get(j);
                        String clsName = sched.get(key);
                        // Find subject for this teacher in this class at this time
                        String subject = "Unknown";
                        String baseClass = clsName.split("\\+")[0];
                        TimetableEntry e = tg.classSchedule.get(baseClass).get(key);
                        if (e != null)
                            subject = e.subject;

                        sb.append("\"").append(key).append("\": {");
                        sb.append("\"class\": \"").append(escapeJson(clsName)).append("\",");
                        sb.append("\"subject\": \"").append(escapeJson(subject)).append("\"");
                        sb.append("}").append(j < keys.size() - 1 ? "," : "");
                    }
                    sb.append("}").append(i < teachers.size() - 1 ? "," : "");
                }
                sb.append("}");
                sb.append("}");
                return sb.toString();
            } else {
                return "{\"status\": \"error\", \"message\": \"No valid timetable possible with given constraints.\"}";
            }
        }
    }
}
