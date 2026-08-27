package servicio;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Clase para registro de auditoría bancaria que implementa AutoCloseable
 * para garantizar el cierre correcto del recurso usando try-with-resources.
 */
public class RegistroAuditoriaBancaria implements AutoCloseable {
    
    // Constantes
    private static final String ARCHIVO_LOG = "auditoria_bancaria.log";
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String SEPARADOR = " | ";
    
    // Recursos
    private final BufferedWriter writer;
    private final FileWriter fileWriter;
    private final AtomicLong contadorRegistros;
    private boolean cerrado;
    
    /**
     * Constructor - Abre el recurso de escritura
     * @throws IOException Si no se puede abrir el archivo
     */
    public RegistroAuditoriaBancaria() throws IOException {
        this(ARCHIVO_LOG);
    }
    
    /**
     * Constructor con nombre de archivo personalizado
     */
    public RegistroAuditoriaBancaria(String nombreArchivo) throws IOException {
        this.fileWriter = new FileWriter(nombreArchivo, true);
        this.writer = new BufferedWriter(fileWriter);
        this.contadorRegistros = new AtomicLong(0);
        this.cerrado = false;
        escribirCabecera();
    }
    
    /**
     * Método principal para registrar auditoría
     */
    public void registrar(String cuentaId, String titular, String tipo, 
                          double monto, String mensaje) throws IOException {
        if (cerrado) {
            throw new IllegalStateException("El recurso de auditoría ya está cerrado");
        }
        
        String registro = construirRegistro(cuentaId, titular, tipo, monto, mensaje);
        writer.write(registro);
        writer.newLine();
        writer.flush();
        
        contadorRegistros.incrementAndGet();
        
        // También mostrar en consola para feedback
        System.out.print("[AUDITORIA] " + registro);
    }
    
    /**
     * Versión sobrecargada para registrar sin monto
     */
    public void registrar(String cuentaId, String titular, String tipo, String mensaje) 
            throws IOException {
        registrar(cuentaId, titular, tipo, 0.0, mensaje);
    }
    
    /**
     * Método para registrar múltiples eventos en una sola operación
     */
    public void registrarBatch(String... registros) throws IOException {
        if (cerrado) {
            throw new IllegalStateException("El recurso de auditoría ya está cerrado");
        }
        
        for (String registro : registros) {
            writer.write(registro);
            writer.newLine();
        }
        writer.flush();
    }
    
    /**
     * Obtener el número total de registros escritos
     */
    public long getTotalRegistros() {
        return contadorRegistros.get();
    }
    
    /**
     * Verificar si el recurso está cerrado
     */
    public boolean isCerrado() {
        return cerrado;
    }
    
    // ============ MÉTODOS PRIVADOS ============
    
    /**
     * Construye el string de registro formateado
     */
    private String construirRegistro(String cuentaId, String titular, String tipo, 
                                     double monto, String mensaje) {
        StringBuilder sb = new StringBuilder();
        
        // Timestamp
        sb.append(LocalDateTime.now().format(FORMATTER));
        sb.append(SEPARADOR);
        
        // ID de cuenta (truncado para legibilidad)
        String idCorta = cuentaId.length() > 8 ? cuentaId.substring(0, 8) : cuentaId;
        sb.append(padRight(idCorta, 8));
        sb.append(SEPARADOR);
        
        // Titular
        sb.append(padRight(titular, 20));
        sb.append(SEPARADOR);
        
        // Tipo de operación
        sb.append(padRight(tipo, 15));
        sb.append(SEPARADOR);
        
        // Monto (formateado)
        if (monto > 0) {
            sb.append(padRight(String.format("%.2f", monto), 10));
        } else {
            sb.append(padRight("-", 10));
        }
        sb.append(SEPARADOR);
        
        // Mensaje
        sb.append(mensaje);
        
        return sb.toString();
    }
    
    /**
     * Método auxiliar para padding a la derecha (reemplaza padEnd)
     */
    private String padRight(String texto, int longitud) {
        if (texto == null) {
            texto = "";
        }
        if (texto.length() >= longitud) {
            return texto.substring(0, longitud);
        }
        StringBuilder sb = new StringBuilder(texto);
        while (sb.length() < longitud) {
            sb.append(' ');
        }
        return sb.toString();
    }
    
    /**
     * Método auxiliar para padding a la izquierda (por si se necesita)
     */
    private String padLeft(String texto, int longitud) {
        if (texto == null) {
            texto = "";
        }
        if (texto.length() >= longitud) {
            return texto.substring(0, longitud);
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < longitud - texto.length()) {
            sb.append(' ');
        }
        sb.append(texto);
        return sb.toString();
    }
    
    /**
     * Escribe una cabecera de sesión en el log
     */
    private void escribirCabecera() throws IOException {
        // ✅ USO DEL MÉTODO MANUAL padRight
        String cabecera = String.format("%s | %s | %s | %s | %s | %s",
            padRight("TIMESTAMP", 23),
            padRight("CUENTA", 8),
            padRight("TITULAR", 20),
            padRight("TIPO", 15),
            padRight("MONTO", 10),
            "MENSAJE"
        );
        
        String separador = repeat("=", 100);
        
        writer.write(separador);
        writer.newLine();
        writer.write("INICIO DE SESIÓN DE AUDITORÍA");
        writer.newLine();
        writer.write("Fecha: " + LocalDateTime.now().format(FORMATTER));
        writer.newLine();
        writer.write(separador);
        writer.newLine();
        writer.write(cabecera);
        writer.newLine();
        writer.write(repeat("-", 100));
        writer.newLine();
        writer.flush();
    }
    
    /**
     * Escribe un pie de sesión en el log
     */
    private void escribirPie() throws IOException {
        String separador = repeat("=", 100);
        writer.write(separador);
        writer.newLine();
        writer.write("FIN DE SESIÓN DE AUDITORÍA");
        writer.newLine();
        writer.write("Total de registros: " + contadorRegistros.get());
        writer.newLine();
        writer.write("Fecha de cierre: " + LocalDateTime.now().format(FORMATTER));
        writer.newLine();
        writer.write(separador);
        writer.newLine();
        writer.flush();
    }
    
    /**
     * Método auxiliar para repetir un carácter (reemplaza repeat de Java 11+)
     */
    private String repeat(String cadena, int veces) {
        if (veces <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            sb.append(cadena);
        }
        return sb.toString();
    }
    
    // ============ IMPLEMENTACIÓN DE AutoCloseable ============
    
    /**
     * Cierra el recurso de auditoría de forma segura
     */
    @Override
    public void close() throws IOException {
        if (!cerrado) {
            try {
                escribirPie();
                
                if (writer != null) {
                    writer.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                
                cerrado = true;
                System.out.println("[AUDITORIA] Recurso de auditoría cerrado correctamente. " +
                                 "Registros escritos: " + contadorRegistros.get());
                
            } catch (IOException e) {
                System.err.println("[ERROR] Fallo al cerrar el recurso de auditoría: " + e.getMessage());
                throw e;
            }
        }
    }
}