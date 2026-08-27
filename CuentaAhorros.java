package modelo;

public class CuentaAhorros extends CuentaBancaria {
    // Atributos específicos de ahorros
    private static final double TASA_INTERES_MENSUAL = 0.015; // 1.5% mensual
    private static final double SALDO_MINIMO = 10000.0; // $10,000 COP
    private static final double COMISION_SALDO_MINIMO = 5000.0; // $5,000 COP
    private static final int MAX_RETIROS_MENSUALES = 4;
    private int retirosEsteMes;
    private double interesesAcumulados;
    
    public CuentaAhorros(String titular, double saldoInicial) {
        super(titular, saldoInicial, TipoCuenta.AHORROS);
        this.retirosEsteMes = 0;
        this.interesesAcumulados = 0.0;
    }
    
    @Override
    public void aplicarComisionMensual() {
        // 1. Aplicar intereses
        double intereses = getSaldo() * TASA_INTERES_MENSUAL;
        this.interesesAcumulados += intereses;
        setSaldo(getSaldo() + intereses);
        
        // 2. Cobrar comisión si no se mantiene saldo mínimo
        if (getSaldo() < SALDO_MINIMO) {
            double comision = COMISION_SALDO_MINIMO;
            setSaldo(getSaldo() - comision);
            registrarAuditoria("COMISION_SALDO_MINIMO", comision, 
                String.format("Saldo mínimo no mantenido. Saldo: %.2f", getSaldo()));
        }
        
        // 3. Reiniciar contador de retiros
        retirosEsteMes = 0;
        
        registrarAuditoria("COMISION_MENSUAL", 0, 
            String.format("Intereses aplicados: %.2f, Comisión cobrada: %s", 
                intereses, getSaldo() < SALDO_MINIMO ? "Sí" : "No"));
    }
    
    @Override
    protected void validarRetiro(double monto) throws IllegalArgumentException {
        // Verificar límite de retiros mensuales
        if (retirosEsteMes >= MAX_RETIROS_MENSUALES) {
            throw new IllegalStateException(
                String.format("Límite de retiros mensuales excedido (máximo %d)", MAX_RETIROS_MENSUALES));
        }
        
        // Verificar saldo suficiente
        if (getSaldo() < monto) {
            throw new IllegalArgumentException(
                String.format("Saldo insuficiente. Saldo actual: %.2f, Monto solicitado: %.2f", 
                    getSaldo(), monto));
        }
        
        // Verificar que después del retiro no quede por debajo del saldo mínimo
        if (getSaldo() - monto < 0) { // Las cuentas de ahorro no pueden quedar en negativo
            throw new IllegalArgumentException(
                "Las cuentas de ahorro no pueden quedar en saldo negativo");
        }
    }
    
    @Override
    protected void actualizarLimites(double monto) {
        retirosEsteMes++;
        registrarAuditoria("CONTADOR_RETIROS", 0, 
            String.format("Retiro #%d de %d este mes", retirosEsteMes, MAX_RETIROS_MENSUALES));
    }
    
    // Métodos específicos de ahorros
    public double getInteresesAcumulados() {
        return interesesAcumulados;
    }
    
    public int getRetirosEsteMes() {
        return retirosEsteMes;
    }
    
    public void reiniciarContadorRetiros() {
        retirosEsteMes = 0;
    }
    
    @Override
    public String toString() {
        return super.toString() + 
            String.format(", CuentaAhorros{retirosEsteMes=%d, interesesAcumulados=%.2f, saldoMinimo=%.2f}",
                retirosEsteMes, interesesAcumulados, SALDO_MINIMO);
    }
}