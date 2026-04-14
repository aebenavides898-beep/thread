package ejercicio1;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Ejercicio 1: Productor-Consumidor
 * Hilos: wait(), notify(), synchronized
 * SOLID: SRP (cada clase tiene una responsabilidad), DIP (dependen de la interfaz Buffer)
 */

// --- Abstracción del buffer (DIP) ---
interface Buffer<T> {
    void producir(T item) throws InterruptedException;
    T consumir() throws InterruptedException;
}

// --- Implementación del buffer acotado (SRP: solo gestiona el almacenamiento) ---
class BufferAcotado<T> implements Buffer<T> {
    private final Queue<T> cola = new LinkedList<>();
    private final int capacidad;

    public BufferAcotado(int capacidad) {
        this.capacidad = capacidad;
    }

    @Override
    public synchronized void producir(T item) throws InterruptedException {
        while (cola.size() == capacidad) {
            System.out.println("  [Buffer lleno] Productor esperando...");
            wait();
        }
        cola.add(item);
        System.out.println("  [Buffer] Agregado: " + item + " | Tamaño: " + cola.size());
        notifyAll();
    }

    @Override
    public synchronized T consumir() throws InterruptedException {
        while (cola.isEmpty()) {
            System.out.println("  [Buffer vacío] Consumidor esperando...");
            wait();
        }
        T item = cola.poll();
        System.out.println("  [Buffer] Retirado: " + item + " | Tamaño: " + cola.size());
        notifyAll();
        return item;
    }
}

// --- Productor (SRP: solo produce elementos) ---
class Productor implements Runnable {
    private final Buffer<String> buffer;
    private final String nombre;
    private final int cantidad;

    public Productor(Buffer<String> buffer, String nombre, int cantidad) {
        this.buffer = buffer;
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= cantidad; i++) {
                String producto = nombre + "-Producto#" + i;
                System.out.println("[" + nombre + "] Produciendo: " + producto);
                buffer.producir(producto);
                Thread.sleep((long) (Math.random() * 500));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// --- Consumidor (SRP: solo consume elementos) ---
class Consumidor implements Runnable {
    private final Buffer<String> buffer;
    private final String nombre;
    private final int cantidad;

    public Consumidor(Buffer<String> buffer, String nombre, int cantidad) {
        this.buffer = buffer;
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < cantidad; i++) {
                String item = buffer.consumir();
                System.out.println("[" + nombre + "] Consumió: " + item);
                Thread.sleep((long) (Math.random() * 800));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// --- Main ---
public class ProductorConsumidor {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== EJERCICIO 1: PRODUCTOR-CONSUMIDOR ===");
        System.out.println("Principios SOLID: SRP, DIP");
        System.out.println("Conceptos de hilos: synchronized, wait(), notifyAll()");
        System.out.println("==========================================\n");

        Buffer<String> buffer = new BufferAcotado<>(3);

        Thread productor1 = new Thread(new Productor(buffer, "Productor-A", 5));
        Thread productor2 = new Thread(new Productor(buffer, "Productor-B", 5));
        Thread consumidor1 = new Thread(new Consumidor(buffer, "Consumidor-X", 5));
        Thread consumidor2 = new Thread(new Consumidor(buffer, "Consumidor-Y", 5));

        productor1.start();
        productor2.start();
        consumidor1.start();
        consumidor2.start();

        productor1.join();
        productor2.join();
        consumidor1.join();
        consumidor2.join();

        System.out.println("\n=== Todos los hilos finalizaron ===");
    }
}
