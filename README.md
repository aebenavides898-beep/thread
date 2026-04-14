# Threads - Ejercicios de Hilos en Java

Proyecto con 5 ejercicios que demuestran conceptos de programacion concurrente (hilos) en Java, aplicando principios SOLID.

## Ejercicios

### Ejercicio 1: Productor-Consumidor
Implementacion del patron clasico Productor-Consumidor usando un buffer acotado.
- **Conceptos de hilos:** `synchronized`, `wait()`, `notifyAll()`
- **Principios SOLID:** SRP, DIP
- **Archivo:** [`src/ejercicio1/ProductorConsumidor.java`](src/ejercicio1/ProductorConsumidor.java)

### Ejercicio 2: Transferencias Bancarias
Simulacion de transferencias bancarias concurrentes con prevencion de deadlock.
- **Conceptos de hilos:** `ReentrantLock`, ordenamiento de locks para prevenir deadlock
- **Principios SOLID:** SRP, OCP
- **Archivo:** [`src/ejercicio2/BancoTransferencias.java`](src/ejercicio2/BancoTransferencias.java)

### Ejercicio 3: Simulacion de Restaurante
Simulacion de un restaurante con meseros, cocineros y repartidores trabajando en paralelo.
- **Conceptos de hilos:** `BlockingQueue`, `AtomicInteger`
- **Principios SOLID:** SRP, ISP, DIP
- **Archivo:** [`src/ejercicio3/RestauranteSimulacion.java`](src/ejercicio3/RestauranteSimulacion.java)

### Ejercicio 4: Planificador de Tareas
Planificador que ejecuta multiples tareas en un pool de hilos y recolecta resultados.
- **Conceptos de hilos:** `ExecutorService`, `Future`, `Callable`
- **Principios SOLID:** OCP, LSP, DIP
- **Archivo:** [`src/ejercicio4/PlanificadorTareas.java`](src/ejercicio4/PlanificadorTareas.java)

### Ejercicio 5: Carrera de Vehiculos
Simulacion de una carrera entre diferentes tipos de vehiculos con barra de progreso.
- **Conceptos de hilos:** `CountDownLatch`, `ConcurrentHashMap`
- **Principios SOLID:** SRP, OCP, LSP, DIP
- **Archivo:** [`src/ejercicio5/CarreraVehiculos.java`](src/ejercicio5/CarreraVehiculos.java)

## Estructura del Proyecto

```
src/
├── ejercicio1/   # Productor-Consumidor
├── ejercicio2/   # Transferencias Bancarias
├── ejercicio3/   # Simulacion de Restaurante
├── ejercicio4/   # Planificador de Tareas
└── ejercicio5/   # Carrera de Vehiculos
```

## Como Ejecutar

Cada ejercicio tiene su propia clase `main`. Para compilar y ejecutar desde la terminal:

```bash
# Compilar
javac -d out src/ejercicio1/ProductorConsumidor.java

# Ejecutar
java -cp out ejercicio1.ProductorConsumidor
```

Repite el mismo patron para los demas ejercicios cambiando el nombre del paquete y la clase.

## Requisitos

- Java 17 o superior
