package modelo;

public enum TipoCuenta {
    AHORROS("Cuenta de Ahorros"),
    CORRIENTE("Cuenta Corriente");
    
    private String descripcion;
    
    TipoCuenta(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}