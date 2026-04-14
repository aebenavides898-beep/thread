package ejercicio3;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ejercicio 3: Simulación de Restaurante
 * Hilos: BlockingQueue, AtomicInteger, Thread.sleep para simular trabajo
 * SOLID: SRP, ISP (interfaces segregadas para preparar vs servir), DIP
 */

// --- Modelo de orden ---
class Orden {
    private static final AtomicInteger contador = new AtomicInteger(0);
    private final int id;
    private final String plato;
    private final String mesa;

    public Orden(String plato, String mesa) {
        this.id = contador.incrementAndGet();
        this.plato = plato;
        this.mesa = mesa;
    }

    public int getId() { return id; }
    public String getPlato() { return plato; }
    public String getMesa() { return mesa; }

    @Override
    public String toString() {
        return String.format("Orden#%d(%s para %s)", id, plato, mesa);
    }
}

// --- ISP: Interfaces segregadas ---
interface Preparable {
    void preparar(Orden orden) throws InterruptedException;
}

interface Servible {
    void servir(Orden orden) throws InterruptedException;
}

// --- Cola de órdenes (SRP: solo gestiona la cola, DIP: las clases dependen de esta abstracción) ---
interface ColaOrdenes {
    void agregarOrden(Orden orden) throws InterruptedException;
    Orden tomarOrden() throws InterruptedException;
}

class ColaOrdenesImpl implements ColaOrdenes {
    private final BlockingQueue<Orden> cola;

    public ColaOrdenesImpl(int capacidad) {
        this.cola = new LinkedBlockingQueue<>(capacidad);
    }

    @Override
    public void agregarOrden(Orden orden) throws InterruptedException {
        cola.put(orden);
    }

    @Override
    public Orden tomarOrden() throws InterruptedException {
        return cola.take();
    }
}

// --- Mesero: toma pedidos y los pone en cola (SRP) ---
class Mesero implements Runnable {
    private final String nombre;
    private final ColaOrdenes colaPendientes;
    private final String[][] pedidos;

    public Mesero(String nombre, ColaOrdenes colaPendientes, String[][] pedidos) {
        this.nombre = nombre;
        this.colaPendientes = colaPendientes;
        this.pedidos = pedidos;
    }

    @Override
    public void run() {
        try {
            for (String[] pedido : pedidos) {
                Orden orden = new Orden(pedido[0], pedido[1]);
                System.out.printf("[%s] Tomó pedido: %s%n", nombre, orden);
                colaPendientes.agregarOrden(orden);
                Thread.sleep((long) (Math.random() * 400 + 100));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// --- Cocinero: prepara órdenes y las pasa a la cola de servir (SRP + ISP) ---
class Cocinero implements Preparable, Runnable {
    private final String nombre;
    private final ColaOrdenes colaPendientes;
    private final ColaOrdenes colaListas;
    private final int cantidadPorPreparar;

    public Cocinero(String nombre, ColaOrdenes colaPendientes, ColaOrdenes colaListas, int cantidadPorPreparar) {
        this.nombre = nombre;
        this.colaPendientes = colaPendientes;
        this.colaListas = colaListas;
        this.cantidadPorPreparar = cantidadPorPreparar;
    }

    @Override
    public void preparar(Orden orden) throws InterruptedException {
        System.out.printf("  [%s] Preparando %s...%n", nombre, orden);
        Thread.sleep((long) (Math.random() * 1000 + 500));
        System.out.printf("  [%s] ¡%s lista!%n", nombre, orden);
        colaListas.agregarOrden(orden);
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < cantidadPorPreparar; i++) {
                Orden orden = colaPendientes.tomarOrden();
                preparar(orden);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// --- Repartidor: sirve órdenes terminadas (SRP + ISP) ---
class Repartidor implements Servible, Runnable {
    private final String nombre;
    private final ColaOrdenes colaListas;
    private final int cantidadPorServir;

    public Repartidor(String nombre, ColaOrdenes colaListas, int cantidadPorServir) {
        this.nombre = nombre;
        this.colaListas = colaListas;
        this.cantidadPorServir = cantidadPorServir;
    }

    @Override
    public void servir(Orden orden) throws InterruptedException {
        System.out.printf("    [%s] Sirviendo %s en %s%n", nombre, orden.getPlato(), orden.getMesa());
        Thread.sleep((long) (Math.random() * 300 + 100));
        System.out.printf("    [%s] ¡%s entregada!%n", nombre, orden);
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < cantidadPorServir; i++) {
                Orden orden = colaListas.tomarOrden();
                servir(orden);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// --- Main ---
public class RestauranteSimulacion {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== EJERCICIO 3: SIMULACIÓN DE RESTAURANTE ===");
        System.out.println("Principios SOLID: SRP, ISP, DIP");
        System.out.println("Conceptos de hilos: BlockingQueue, AtomicInteger");
        System.out.println("================================================\n");

        ColaOrdenes colaPendientes = new ColaOrdenesImpl(5);
        ColaOrdenes colaListas = new ColaOrdenesImpl(5);

        String[][] pedidosMesero1 = {
                {"Tacos", "Mesa-1"}, {"Enchiladas", "Mesa-2"}, {"Pozole", "Mesa-1"}
        };
        String[][] pedidosMesero2 = {
                {"Sopa", "Mesa-3"}, {"Hamburguesa", "Mesa-4"}, {"Ensalada", "Mesa-3"}
        };

        Thread mesero1 = new Thread(new Mesero("Mesero-Ana", colaPendientes, pedidosMesero1));
        Thread mesero2 = new Thread(new Mesero("Mesero-Luis", colaPendientes, pedidosMesero2));
        Thread cocinero1 = new Thread(new Cocinero("Chef-Mario", colaPendientes, colaListas, 3));
        Thread cocinero2 = new Thread(new Cocinero("Chef-Diana", colaPendientes, colaListas, 3));
        Thread repartidor = new Thread(new Repartidor("Repartidor-Pedro", colaListas, 6));

        mesero1.start();
        mesero2.start();
        cocinero1.start();
        cocinero2.start();
        repartidor.start();

        mesero1.join();
        mesero2.join();
        cocinero1.join();
        cocinero2.join();
        repartidor.join();

        System.out.println("\n=== Todas las órdenes fueron servidas ===");
    }
}
