package ejercicio2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;

/**
 * Ejercicio 2: Transferencias Bancarias con Hilos
 * Hilos: ReentrantLock, prevención de deadlock con ordenamiento de locks
 * SOLID: SRP (cuenta separada de transferencia), OCP (nuevos tipos de transacción sin modificar existentes)
 */

// --- Interfaz de transacción (OCP: se pueden agregar tipos sin modificar código existente) ---
interface Transaccion {
    boolean ejecutar();
    String getDescripcion();
}

// --- Cuenta bancaria (SRP: solo gestiona saldo) ---
class CuentaBancaria {
    private final int id;
    private final String titular;
    private double saldo;
    private final Lock lock = new ReentrantLock();

    public CuentaBancaria(int id, String titular, double saldoInicial) {
        this.id = id;
        this.titular = titular;
        this.saldo = saldoInicial;
    }

    public int getId() { return id; }
    public String getTitular() { return titular; }

    public double getSaldo() {
        lock.lock();
        try {
            return saldo;
        } finally {
            lock.unlock();
        }
    }

    public void depositar(double monto) {
        saldo += monto;
    }

    public boolean retirar(double monto) {
        if (saldo >= monto) {
            saldo -= monto;
            return true;
        }
        return false;
    }

    public Lock getLock() { return lock; }

    @Override
    public String toString() {
        return String.format("Cuenta[%d-%s: $%.2f]", id, titular, saldo);
    }
}

// --- Transferencia (SRP: solo ejecuta transferencias, OCP: implementa Transaccion) ---
class Transferencia implements Transaccion {
    private final CuentaBancaria origen;
    private final CuentaBancaria destino;
    private final double monto;

    public Transferencia(CuentaBancaria origen, CuentaBancaria destino, double monto) {
        this.origen = origen;
        this.destino = destino;
        this.monto = monto;
    }

    @Override
    public boolean ejecutar() {
        // Prevención de deadlock: siempre bloquear en orden de ID
        CuentaBancaria primero = origen.getId() < destino.getId() ? origen : destino;
        CuentaBancaria segundo = origen.getId() < destino.getId() ? destino : origen;

        primero.getLock().lock();
        segundo.getLock().lock();
        try {
            if (origen.retirar(monto)) {
                destino.depositar(monto);
                System.out.printf("  ✓ Transferencia: $%.2f de %s → %s%n", monto, origen.getTitular(), destino.getTitular());
                return true;
            } else {
                System.out.printf("  ✗ Fondos insuficientes: %s intentó enviar $%.2f%n", origen.getTitular(), monto);
                return false;
            }
        } finally {
            segundo.getLock().unlock();
            primero.getLock().unlock();
        }
    }

    @Override
    public String getDescripcion() {
        return String.format("Transferencia $%.2f: %s → %s", monto, origen.getTitular(), destino.getTitular());
    }
}

// --- Hilo que ejecuta múltiples transacciones ---
class EjecutorTransacciones implements Runnable {
    private final List<Transaccion> transacciones;
    private final String nombre;

    public EjecutorTransacciones(String nombre, List<Transaccion> transacciones) {
        this.nombre = nombre;
        this.transacciones = transacciones;
    }

    @Override
    public void run() {
        for (Transaccion t : transacciones) {
            System.out.println("[" + nombre + "] Ejecutando: " + t.getDescripcion());
            t.ejecutar();
            try {
                Thread.sleep((long) (Math.random() * 300));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

// --- Main ---
public class BancoTransferencias {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== EJERCICIO 2: TRANSFERENCIAS BANCARIAS ===");
        System.out.println("Principios SOLID: SRP, OCP");
        System.out.println("Conceptos de hilos: ReentrantLock, prevención de deadlock");
        System.out.println("==============================================\n");

        CuentaBancaria alice = new CuentaBancaria(1, "Alice", 1000);
        CuentaBancaria bob = new CuentaBancaria(2, "Bob", 1000);
        CuentaBancaria carlos = new CuentaBancaria(3, "Carlos", 1000);

        System.out.println("Estado inicial:");
        System.out.println("  " + alice);
        System.out.println("  " + bob);
        System.out.println("  " + carlos);
        System.out.println();

        List<Transaccion> lote1 = List.of(
                new Transferencia(alice, bob, 200),
                new Transferencia(bob, carlos, 150),
                new Transferencia(alice, carlos, 100)
        );

        List<Transaccion> lote2 = List.of(
                new Transferencia(bob, alice, 300),
                new Transferencia(carlos, alice, 250),
                new Transferencia(carlos, bob, 100)
        );

        Thread hilo1 = new Thread(new EjecutorTransacciones("Cajero-1", lote1));
        Thread hilo2 = new Thread(new EjecutorTransacciones("Cajero-2", lote2));

        hilo1.start();
        hilo2.start();
        hilo1.join();
        hilo2.join();

        System.out.println("\nEstado final:");
        System.out.println("  " + alice);
        System.out.println("  " + bob);
        System.out.println("  " + carlos);

        double total = alice.getSaldo() + bob.getSaldo() + carlos.getSaldo();
        System.out.printf("\nTotal en el sistema: $%.2f (debe ser $3000.00)%n", total);
    }
}
