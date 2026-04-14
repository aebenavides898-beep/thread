package ejercicio5;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Ejercicio 5: Carrera de Vehículos
 * Hilos: CountDownLatch, ConcurrentHashMap, CyclicBarrier
 * SOLID: SRP, OCP, LSP (diferentes vehículos sustituibles), DIP
 */

// --- Abstracción de vehículo (DIP + OCP: nuevos vehículos sin modificar la carrera) ---
interface Vehiculo {
    String getNombre();
    int getVelocidadBase();
    double getFactorAleatorio();
}

// --- Implementaciones (LSP: todos intercambiables donde se espere un Vehiculo) ---
class Auto implements Vehiculo {
    private final String nombre;

    public Auto(String nombre) { this.nombre = nombre; }

    @Override public String getNombre() { return "🚗 " + nombre; }
    @Override public int getVelocidadBase() { return 8; }
    @Override public double getFactorAleatorio() { return 0.5; }
}

class Moto implements Vehiculo {
    private final String nombre;

    public Moto(String nombre) { this.nombre = nombre; }

    @Override public String getNombre() { return "🏍️ " + nombre; }
    @Override public int getVelocidadBase() { return 10; }
    @Override public double getFactorAleatorio() { return 0.8; }
}

class Camion implements Vehiculo {
    private final String nombre;

    public Camion(String nombre) { this.nombre = nombre; }

    @Override public String getNombre() { return "🚛 " + nombre; }
    @Override public int getVelocidadBase() { return 5; }
    @Override public double getFactorAleatorio() { return 0.3; }
}

// --- Registro de progreso (SRP: solo gestiona el estado de la carrera) ---
class RegistroCarrera {
    private final ConcurrentHashMap<String, Integer> progreso = new ConcurrentHashMap<>();
    private final List<String> ordenLlegada = Collections.synchronizedList(new ArrayList<>());
    private final int distanciaTotal;

    public RegistroCarrera(int distanciaTotal) {
        this.distanciaTotal = distanciaTotal;
    }

    public void registrarVehiculo(String nombre) {
        progreso.put(nombre, 0);
    }

    public boolean avanzar(String nombre, int distancia) {
        int nuevaPos = progreso.merge(nombre, distancia, Integer::sum);
        if (nuevaPos >= distanciaTotal && !ordenLlegada.contains(nombre)) {
            ordenLlegada.add(nombre);
            return true;
        }
        return false;
    }

    public int getProgreso(String nombre) {
        return progreso.getOrDefault(nombre, 0);
    }

    public int getDistanciaTotal() { return distanciaTotal; }

    public List<String> getOrdenLlegada() {
        return Collections.unmodifiableList(ordenLlegada);
    }
}

// --- Corredor: hilo que simula el avance de un vehículo (SRP) ---
class Corredor implements Runnable {
    private final Vehiculo vehiculo;
    private final RegistroCarrera registro;
    private final CountDownLatch semaforo;
    private final CountDownLatch meta;

    public Corredor(Vehiculo vehiculo, RegistroCarrera registro, CountDownLatch semaforo, CountDownLatch meta) {
        this.vehiculo = vehiculo;
        this.registro = registro;
        this.semaforo = semaforo;
        this.meta = meta;
    }

    @Override
    public void run() {
        try {
            registro.registrarVehiculo(vehiculo.getNombre());
            System.out.printf("[%s] En la línea de salida%n", vehiculo.getNombre());

            semaforo.await();

            while (true) {
                int avance = vehiculo.getVelocidadBase()
                        + (int) (Math.random() * vehiculo.getVelocidadBase() * vehiculo.getFactorAleatorio());

                boolean termino = registro.avanzar(vehiculo.getNombre(), avance);

                int progreso = registro.getProgreso(vehiculo.getNombre());
                int porcentaje = Math.min(100, (progreso * 100) / registro.getDistanciaTotal());
                String barra = generarBarra(porcentaje);
                System.out.printf("  %s %s %d%%%n", vehiculo.getNombre(), barra, porcentaje);

                if (termino) {
                    System.out.printf("  >>> %s CRUZÓ LA META! <<<%n", vehiculo.getNombre());
                    meta.countDown();
                    return;
                }

                Thread.sleep((long) (Math.random() * 300 + 200));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String generarBarra(int porcentaje) {
        int llenos = porcentaje / 5;
        int vacios = 20 - llenos;
        return "[" + "=".repeat(llenos) + " ".repeat(vacios) + "]";
    }
}

// --- Director de carrera (SRP: coordina el evento) ---
class DirectorCarrera {
    private final List<Vehiculo> participantes;
    private final int distancia;

    public DirectorCarrera(List<Vehiculo> participantes, int distancia) {
        this.participantes = participantes;
        this.distancia = distancia;
    }

    public RegistroCarrera iniciarCarrera() throws InterruptedException {
        RegistroCarrera registro = new RegistroCarrera(distancia);
        CountDownLatch semaforo = new CountDownLatch(1);
        CountDownLatch meta = new CountDownLatch(participantes.size());

        List<Thread> hilos = new ArrayList<>();
        for (Vehiculo v : participantes) {
            Thread t = new Thread(new Corredor(v, registro, semaforo, meta));
            hilos.add(t);
            t.start();
        }

        Thread.sleep(500);
        System.out.println("\n>>> 3... 2... 1... ¡ARRANCAN! <<<\n");
        semaforo.countDown();

        meta.await();
        for (Thread t : hilos) {
            t.join();
        }

        return registro;
    }
}

// --- Main ---
public class CarreraVehiculos {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== EJERCICIO 5: CARRERA DE VEHÍCULOS ===");
        System.out.println("Principios SOLID: SRP, OCP, LSP, DIP");
        System.out.println("Conceptos de hilos: CountDownLatch, ConcurrentHashMap");
        System.out.println("==========================================\n");

        List<Vehiculo> participantes = List.of(
                new Auto("Rayo"),
                new Auto("Veloz"),
                new Moto("Trueno"),
                new Moto("Centella"),
                new Camion("Titán")
        );

        DirectorCarrera director = new DirectorCarrera(participantes, 100);
        RegistroCarrera resultado = director.iniciarCarrera();

        System.out.println("\n=== PODIO ===");
        List<String> llegada = resultado.getOrdenLlegada();
        for (int i = 0; i < llegada.size(); i++) {
            String medalla = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> "  " + (i + 1) + ".";
            };
            System.out.printf("  %s %s%n", medalla, llegada.get(i));
        }
    }
}
