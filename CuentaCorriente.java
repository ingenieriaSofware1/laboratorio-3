package modelo;

public class CuentaCorriente extends CuentaBancaria {
    // Atributos específicos de corriente
    private static final double LIMITE_SOBREGIRO_INICIAL = 500000.0; // $500,000 COP
    private static final double COMISION_SOBREGIRO = 0.05; // 5% del sobregiro usado
    private static final double INTERES_SOBREGIRO = 0.02; // 2% mensual sobre sobregiro
    private double limiteSobregiro;
    private double sobregiroUsado;
    private double deudaSobregiro;
    
    public CuentaCorriente(String titular, double saldoInicial) {
        super(titular, saldoInicial, TipoCuenta.CORRIENTE);
        this.limiteSobregiro = LIMITE_SOBREGIRO_INICIAL;
        this.sobregiroUsado = 0.0;
        this.deudaSobregiro = 0.0;
    }
    
    public CuentaCorriente(String titular, double saldoInicial, double limiteSobregiro) {
        super(titular, saldoInicial, TipoCuenta.CORRIENTE);
        if (limiteSobregiro <= 0) {
            throw new IllegalArgumentException("El límite de sobregiro debe ser positivo");
        }
        this.limiteSobregiro = limiteSobregiro;
        this.sobregiroUsado = 0.0;
        this.deudaSobregiro = 0.0;
    }
    
    @Override
    public void aplicarComisionMensual() {
        // 1. Calcular comisión por uso de sobregiro
        if (sobregiroUsado > 0) {
            double comision = sobregiroUsado * COMISION_SOBREGIRO;
            double intereses = sobregiroUsado * INTERES_SOBREGIRO;
            this.deudaSobregiro += comision + intereses;
            
            // Registrar auditoría
            registrarAuditoria("COMISION_SOBREGIRO", comision, 
                String.format("Comisión por sobregiro: %.2f", comision));
            registrarAuditoria("INTERES_SOBREGIRO", intereses, 
                String.format("Interés por sobregiro: %.2f", intereses));
            
            // Si la deuda excede el límite, bloquear cuenta
            if (deudaSobregiro >= limiteSobregiro * 1.5) {
                setActiva(false);
                registrarAuditoria("BLOQUEO_CUENTA", 0, 
                    "Cuenta bloqueada por exceder límite de sobregiro");
            }
        }
        
        // Registrar resumen mensual
        registrarAuditoria("COMISION_MENSUAL", 0, 
            String.format("Resumen mensual - Sobregiro usado: %.2f, Deuda: %.2f", 
                sobregiroUsado, deudaSobregiro));
        
        // Reiniciar contador de sobregiro para el nuevo mes
        sobregiroUsado = 0;
    }
    
    @Override
    protected void validarRetiro(double monto) throws IllegalArgumentException {
        double disponibleTotal = getSaldo() + (limiteSobregiro - deudaSobregiro);
        
        if (disponibleTotal < monto) {
            throw new IllegalArgumentException(
                String.format("Límite de sobregiro insuficiente. Disponible: %.2f, Monto solicitado: %.2f",
                    disponibleTotal, monto));
        }
    }
    
    @Override
    protected void actualizarLimites(double monto) {
        // Calcular cuánto del retiro usa el sobregiro
        if (getSaldo() < 0) {
            // Si ya está en sobregiro, todo el retiro es sobregiro adicional
            double nuevoSobregiro = monto;
            this.sobregiroUsado += nuevoSobregiro;
            this.deudaSobregiro += nuevoSobregiro;
            registrarAuditoria("SOBREGIRO", monto, 
                String.format("Uso de sobregiro: %.2f (Total usado: %.2f)", monto, sobregiroUsado));
        } else if (getSaldo() < monto) {
            // Solo la parte que excede el saldo usa sobregiro
            double usadoSobregiro = monto - getSaldo();
            this.sobregiroUsado += usadoSobregiro;
            this.deudaSobregiro += usadoSobregiro;
            registrarAuditoria("SOBREGIRO", usadoSobregiro, 
                String.format("Uso parcial de sobregiro: %.2f (Total usado: %.2f)", 
                    usadoSobregiro, sobregiroUsado));
        }
    }
    
    // Métodos específicos de corriente
    public double getLimiteSobregiro() {
        return limiteSobregiro;
    }
    
    public double getSobregiroUsado() {
        return sobregiroUsado;
    }
    
    public double getDeudaSobregiro() {
        return deudaSobregiro;
    }
    
    public double getLimiteDisponible() {
        return limiteSobregiro - deudaSobregiro;
    }
    
    public void pagarDeudaSobregiro(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser positivo");
        }
        if (monto > deudaSobregiro) {
            throw new IllegalArgumentException(
                String.format("El pago excede la deuda. Deuda actual: %.2f", deudaSobregiro));
        }
        
        depositar(monto); // El depósito reduce la deuda automáticamente
        this.deudaSobregiro -= monto;
        
        // Si la deuda se paga completamente, reactivar la cuenta si estaba bloqueada
        if (deudaSobregiro == 0 && !isActiva()) {
            setActiva(true);
            registrarAuditoria("REACTIVACION_CUENTA", 0, "Cuenta reactivada por pago de deuda");
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + 
            String.format(", CuentaCorriente{limiteSobregiro=%.2f, sobregiroUsado=%.2f, deuda=%.2f, disponible=%.2f}",
                limiteSobregiro, sobregiroUsado, deudaSobregiro, getLimiteDisponible());
    }
}