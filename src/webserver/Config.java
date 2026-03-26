package webserver;

import java.io.*;
import java.util.Properties;

public class Config {
    private int port = 8080;
    private String documentRoot = "public_html";
    private String defaultFile = "index.html";

    public Config(String configFilePath) {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(configFilePath)) {
            prop.load(input);
            this.port = Integer.parseInt(prop.getProperty("port", "8080"));
            this.documentRoot = prop.getProperty("document_root", "public_html");
            this.defaultFile = prop.getProperty("default_file", "index.html");
            System.out.println("Configuración cargada: Puerto=" + port + ", Root=" + documentRoot);
        } catch (IOException ex) {
            System.err.println("No se pudo cargar la configuración de " + configFilePath + ", usando valores por defecto.");
        }
    }

    public int getPort() { return port; }
    public String getDocumentRoot() { return documentRoot; }
    public String getDefaultFile() { return defaultFile; }
}
