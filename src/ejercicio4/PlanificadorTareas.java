package ejercicio4;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Ejercicio 4: Planificador de Tareas con Pool de Hilos
 * Hilos: ExecutorService, Future, Callable
 * SOLID: OCP (nuevos tipos de tarea sin modificar planificador), LSP (todas las tareas son intercambiables), DIP
 */

// --- Resultado de tarea ---
class ResultadoTarea {
    private final String nombreTarea;
    private final String resultado;
    private final long tiempoMs;

    public ResultadoTarea(String nombreTarea, String resultado, long tiempoMs) {
        this.nombreTarea = nombreTarea;
        this.resultado = resultado;
        this.tiempoMs = tiempoMs;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (tomó %dms)", nombreTarea, resultado, tiempoMs);
    }
}

// --- Interfaz de tarea (DIP + OCP) ---
interface TareaPlanificable extends Callable<ResultadoTarea> {
    String getNombre();
}

// --- Tarea: Cálculo de números primos (SRP) ---
class TareaCalculoPrimos implements TareaPlanificable {
    private final int limite;

    public TareaCalculoPrimos(int limite) {
        this.limite = limite;
    }

    @Override
    public String getNombre() { return "CalculoPrimos"; }

    @Override
    public ResultadoTarea call() {
        long inicio = System.currentTimeMillis();
        int count = 0;
        for (int n = 2; n <= limite; n++) {
            if (esPrimo(n)) count++;
        }
        long tiempo = System.currentTimeMillis() - inicio;
        System.out.printf("  [%s] Encontrados %d primos hasta %d%n", Thread.currentThread().getName(), count, limite);
        return new ResultadoTarea(getNombre(), "Encontrados " + count + " primos hasta " + limite, tiempo);
    }

    private boolean esPrimo(int n) {
        if (n < 2) return false;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}

// --- Tarea: Cálculo de factorial (SRP, LSP: intercambiable con cualquier TareaPlanificable) ---
class TareaFactorial implements TareaPlanificable {
    private final int numero;

    public TareaFactorial(int numero) {
        this.numero = numero;
    }

    @Override
    public String getNombre() { return "Factorial"; }

    @Override
    public ResultadoTarea call() {
        long inicio = System.currentTimeMillis();
        long resultado = 1;
        for (int i = 2; i <= numero; i++) {
            resultado *= i;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ResultadoTarea(getNombre(), "Interrumpido", System.currentTimeMillis() - inicio);
            }
        }
        long tiempo = System.currentTimeMillis() - inicio;
        System.out.printf("  [%s] %d! = %d%n", Thread.currentThread().getName(), numero, resultado);
        return new ResultadoTarea(getNombre(), numero + "! = " + resultado, tiempo);
    }
}

// --- Tarea: Fibonacci (SRP, LSP) ---
class TareaFibonacci implements TareaPlanificable {
    private final int n;

    public TareaFibonacci(int n) {
        this.n = n;
    }

    @Override
    public String getNombre() { return "Fibonacci"; }

    @Override
    public ResultadoTarea call() {
        long inicio = System.currentTimeMillis();
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ResultadoTarea(getNombre(), "Interrumpido", System.currentTimeMillis() - inicio);
            }
        }
        long tiempo = System.currentTimeMillis() - inicio;
        System.out.printf("  [%s] Fibonacci(%d) = %d%n", Thread.currentThread().getName(), n, b);
        return new ResultadoTarea(getNombre(), "Fibonacci(" + n + ") = " + b, tiempo);
    }
}

// --- Tarea: Simulación de descarga (SRP, LSP) ---
class TareaDescarga implements TareaPlanificable {
    private final String archivo;
    private final int tamanoMB;

    public TareaDescarga(String archivo, int tamanoMB) {
        this.archivo = archivo;
        this.tamanoMB = tamanoMB;
    }

    @Override
    public String getNombre() { return "Descarga-" + archivo; }

    @Override
    public ResultadoTarea call() {
        long inicio = System.currentTimeMillis();
        try {
            for (int i = 0; i <= 100; i += 25) {
                System.out.printf("  [%s] Descargando %s... %d%%%n", Thread.currentThread().getName(), archivo, i);
                Thread.sleep(tamanoMB * 50L);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ResultadoTarea(getNombre(), "Interrumpido", System.currentTimeMillis() - inicio);
        }
        long tiempo = System.currentTimeMillis() - inicio;
        return new ResultadoTarea(getNombre(), archivo + " (" + tamanoMB + "MB) descargado", tiempo);
    }
}

// --- Planificador (SRP: solo gestiona la ejecución, DIP: trabaja con TareaPlanificable) ---
class Planificador {
    private final ExecutorService pool;

    public Planificador(int numHilos) {
        this.pool = Executors.newFixedThreadPool(numHilos);
    }

    public List<Future<ResultadoTarea>> ejecutarTodas(List<TareaPlanificable> tareas) {
        List<Future<ResultadoTarea>> futuros = new ArrayList<>();
        for (TareaPlanificable tarea : tareas) {
            System.out.println("[Planificador] Enviando: " + tarea.getNombre());
            futuros.add(pool.submit(tarea));
        }
        return futuros;
    }

    public void apagar() {
        pool.shutdown();
    }

    public boolean esperarTermino(long timeoutSegundos) throws InterruptedException {
        return pool.awaitTermination(timeoutSegundos, TimeUnit.SECONDS);
    }
}

// --- Main ---
public class PlanificadorTareas {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("=== EJERCICIO 4: PLANIFICADOR DE TAREAS ===");
        System.out.println("Principios SOLID: OCP, LSP, DIP");
        System.out.println("Conceptos de hilos: ExecutorService, Future, Callable");
        System.out.println("=============================================\n");

        Planificador planificador = new Planificador(3);

        List<TareaPlanificable> tareas = List.of(
                new TareaCalculoPrimos(50000),
                new TareaFactorial(15),
                new TareaFibonacci(20),
                new TareaDescarga("datos.csv", 3),
                new TareaDescarga("imagen.png", 5)
        );

        List<Future<ResultadoTarea>> futuros = planificador.ejecutarTodas(tareas);
        planificador.apagar();
        planificador.esperarTermino(30);

        System.out.println("\n=== RESULTADOS ===");
        for (Future<ResultadoTarea> futuro : futuros) {
            System.out.println("  " + futuro.get());
        }

        System.out.println("\n=== Todas las tareas completadas ===");
    }
}
