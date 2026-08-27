import modelo.*;
import servicio.RegistroAuditoriaBancaria;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA BANCARIO MODERNO ===\n");
        
        // ✅ USO DE TRY-WITH-RESOURCES
        // El recurso se cierra AUTOMÁTICAMENTE al salir del bloque
        try (RegistroAuditoriaBancaria auditoria = new RegistroAuditoriaBancaria()) {
            
            System.out.println("1. Creando cuentas...");
            CuentaAhorros cuentaAhorros = new CuentaAhorros("María Pérez", 15000.0);
            CuentaCorriente cuentaCorriente = new CuentaCorriente("Juan Gómez", 100000.0);
            
            // Inyectar la auditoría en las cuentas
            cuentaAhorros.setAuditoria(auditoria);
            cuentaCorriente.setAuditoria(auditoria);
            
            System.out.println(cuentaAhorros);
            System.out.println(cuentaCorriente);
            System.out.println();
            
            System.out.println("2. Realizando operaciones...");
            
            // Depósitos
            cuentaAhorros.depositar(5000.0);
            cuentaCorriente.depositar(200000.0);
            
            // Retiros
            System.out.println("\n--- Retiro en Cuenta Ahorros ---");
            try {
                cuentaAhorros.retirar(3000.0);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            System.out.println("\n--- Retiro en Cuenta Corriente (con sobregiro) ---");
            try {
                cuentaCorriente.retirar(350000.0);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            System.out.println("\n3. Aplicando comisiones mensuales...");
            cuentaAhorros.aplicarComisionMensual();
            cuentaCorriente.aplicarComisionMensual();
            
            System.out.println("\n4. Estado final de cuentas:");
            System.out.println(cuentaAhorros);
            System.out.println(cuentaCorriente);
            
            System.out.println("\n5. Total de registros de auditoría: " + auditoria.getTotalRegistros());
            
        } catch (IOException e) {
            System.err.println("Error al abrir el archivo de auditoría: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
            e.printStackTrace();
        }
        
        // ⚠️ EL RECURSO YA ESTÁ CERRADO AUTOMÁTICAMENTE AQUÍ
        System.out.println("\n=== PROGRAMA FINALIZADO ===");
        System.out.println("El archivo de auditoría se cerró correctamente.");
    }
}