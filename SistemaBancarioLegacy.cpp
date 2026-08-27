//SistemaBancarioLegacy.cpp 
//Código a auditar por los estudiantes



#include <iostream>



#include <fstream>



#include <string>





struct CuentaLegacy {

    char* titular;

    double saldo;

    int tipoCuenta; // 1: Ahorros, 2:Corriente

    double limiteSobregiro;



};





CuentaLegacy* crearCuenta(const char* nombre, double saldoInicial, int tipo) {

    CuentaLegacy* c = new CuentaLegacy();

    c->titular = new char[50];

    strcpy(c->titular, nombre);

    c->saldo = saldoInicial;

    c->tipoCuenta = tipo;

    c->limiteSobregiro = (tipo == 2) ? 500.0 : 0.0;

    return c;



}



void procesarRetiro(CuentaLegacy* c, double monto) {

    if (c->tipoCuenta == 1 &&

c->saldo >= monto) {

        c->saldo -= monto;

    } else if (c->tipoCuenta == 2

&& (c->saldo + c->limiteSobregiro) >= monto) {

        c->saldo -= monto; // Fuga potencial de lógica y sin registro de auditoría

    }

    // NOTA: No hay liberación de memoria de 'titular' ni de 'c' al terminar el programa
}

