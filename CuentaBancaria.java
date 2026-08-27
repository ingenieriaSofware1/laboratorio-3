package modelo;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class CuentaBancaria {
    // Atributos privados (encapsulamiento)
    private String id;
    private String titular;
    private double saldo;
    private TipoCuenta tipoCuenta;
    private LocalDateTime fechaCreacion;
    private boolean activa;
    
    // Constructor
    public CuentaBancaria(String titular, double saldoInicial, TipoCuenta tipoCuenta) {
        validarTitular(titular);
        validarSaldoInicial(saldoInicial);
        
        this.id = UUID.randomUUID().toString();
        this.titular = titular;
        this.saldo = saldoInicial;
        this.tipoCuenta = tipoCuenta;
        this.fechaCreacion = LocalDateTime.now();
        this.activa = true;
    }
    
    // Métodos abstractos (las subclases deben implementar)
    public abstract void aplicarComisionMensual();
    protected abstract void validarRetiro(double monto) throws IllegalArgumentException;
    protected abstract void actualizarLimites(double monto);
    
    // Métodos concretos comunes
    public void depositar(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del depósito debe ser positivo");
        }
        if (!activa) {
            throw new IllegalStateException("La cuenta está inactiva");
        }
        this.saldo += monto;
        registrarAuditoria("DEPOSITO", monto, "Depósito exitoso");
    }
    
    public void retirar(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del retiro debe ser positivo");
        }
        if (!activa) {
            throw new IllegalStateException("La cuenta está inactiva");
        }
        
        validarRetiro(monto); // Delegado a subclase
        this.saldo -= monto;
        actualizarLimites(monto); // Delegado a subclase
        registrarAuditoria("RETIRO", monto, "Retiro exitoso");
    }
    
    public double consultarSaldo() {
        return this.saldo;
    }
    
    // Métodos de validación privados
    private void validarTitular(String titular) {
        if (titular == null || titular.trim().isEmpty()) {
            throw new IllegalArgumentException("El titular no puede estar vacío");
        }
    }
    
    private void validarSaldoInicial(double saldoInicial) {
        if (saldoInicial < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }
    }
    
    // Método de auditoría (simulado)
    protected void registrarAuditoria(String tipo, double monto, String mensaje) {
        System.out.printf("[AUDITORIA] Cuenta: %s, Titular: %s, Tipo: %s, Monto: %.2f, Mensaje: %s%n",
            id.substring(0, 8), titular, tipo, monto, mensaje);
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public String getTitular() { return titular; }
    public double getSaldo() { return saldo; }
    public TipoCuenta getTipoCuenta() { return tipoCuenta; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public boolean isActiva() { return activa; }
    
    public void setTitular(String titular) {
        validarTitular(titular);
        this.titular = titular;
    }
    
    protected void setSaldo(double saldo) {
        this.saldo = saldo;
    }
    
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
    
    @Override
    public String toString() {
        return String.format("CuentaBancaria{id='%s', titular='%s', saldo=%.2f, tipo=%s, activa=%s}",
            id.substring(0, 8), titular, saldo, tipoCuenta, activa);
    }
}