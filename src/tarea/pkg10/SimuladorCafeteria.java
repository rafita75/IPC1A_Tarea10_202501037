package tarea.pkg10;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SimuladorCafeteria extends JFrame {

    private final Cola<Cliente> cola = new Cola<>();
    private final JTextField txtNombre = new JTextField(15);
    private final JButton btnAgregar = new JButton("Agregar a Cola");
    private final JTextArea areaCola = new JTextArea(10, 25);

    // Panel Barista 1
    private final JLabel lblEstadoB1 = new JLabel("Libre");
    private final JProgressBar progresoB1 = new JProgressBar(0, 100);
    private final JLabel lblAtendidosB1 = new JLabel("Atendidos: 0");

    // Panel Barista 2
    private final JLabel lblEstadoB2 = new JLabel("Libre");
    private final JProgressBar progresoB2 = new JProgressBar(0, 100);
    private final JLabel lblAtendidosB2 = new JLabel("Atendidos: 0");

    // Panel Barista 3
    private final JLabel lblEstadoB3 = new JLabel("Libre");
    private final JProgressBar progresoB3 = new JProgressBar(0, 100);
    private final JLabel lblAtendidosB3 = new JLabel("Atendidos: 0");

    // Estadísticas generales
    private final JLabel lblTotalAtendidos = new JLabel("Total atendidos: 0");
    private final JLabel lblPromedioEspera = new JLabel("Tiempo promedio de espera: 0.00 s");

    private int contadorIds = 1;
    private int totalAtendidos = 0;
    private long sumaTiemposEspera = 0;

    private final Random random = new Random();

    public SimuladorCafeteria() {
        setTitle("Simulador de Atención - Cafetería");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ---------------- Panel superior: ingreso ----------------
        JPanel panelIngreso = new JPanel();
        panelIngreso.setBorder(BorderFactory.createTitledBorder("Ingreso de Cliente"));

        panelIngreso.add(new JLabel("Nombre:"));
        panelIngreso.add(txtNombre);
        panelIngreso.add(btnAgregar);

        add(panelIngreso, BorderLayout.NORTH);

        // ---------------- Panel izquierdo: cola ----------------
        JPanel panelCola = new JPanel(new BorderLayout());
        panelCola.setBorder(BorderFactory.createTitledBorder("Clientes en Espera"));

        areaCola.setEditable(false);
        JScrollPane scrollCola = new JScrollPane(areaCola);
        panelCola.add(scrollCola, BorderLayout.CENTER);

        add(panelCola, BorderLayout.WEST);

        // ---------------- Panel central: baristas ----------------
        JPanel panelBaristas = new JPanel(new GridLayout(1, 3, 10, 10));
        panelBaristas.setBorder(BorderFactory.createTitledBorder("Estaciones de Servicio"));

        panelBaristas.add(crearPanelBarista("Barista 1", lblEstadoB1, progresoB1, lblAtendidosB1));
        panelBaristas.add(crearPanelBarista("Barista 2", lblEstadoB2, progresoB2, lblAtendidosB2));
        panelBaristas.add(crearPanelBarista("Barista 3", lblEstadoB3, progresoB3, lblAtendidosB3));

        add(panelBaristas, BorderLayout.CENTER);

        // ---------------- Panel inferior: estadísticas ----------------
        JPanel panelEstadisticas = new JPanel(new GridLayout(2, 1));
        panelEstadisticas.setBorder(BorderFactory.createTitledBorder("Estadísticas"));

        panelEstadisticas.add(lblTotalAtendidos);
        panelEstadisticas.add(lblPromedioEspera);

        add(panelEstadisticas, BorderLayout.SOUTH);

        // Acción botón
        btnAgregar.addActionListener(e -> agregarCliente());

        // Crear y arrancar los 3 baristas
        Thread b1 = new Thread(new Barista(
                "Barista 1",
                this,
                lblEstadoB1,
                progresoB1,
                lblAtendidosB1
        ));

        Thread b2 = new Thread(new Barista(
                "Barista 2",
                this,
                lblEstadoB2,
                progresoB2,
                lblAtendidosB2
        ));

        Thread b3 = new Thread(new Barista(
                "Barista 3",
                this,
                lblEstadoB3,
                progresoB3,
                lblAtendidosB3
        ));

        b1.start();
        b2.start();
        b3.start();
    }

    private JPanel crearPanelBarista(String titulo, JLabel estado, JProgressBar progreso, JLabel atendidos) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(titulo));

        progreso.setStringPainted(true);

        panel.add(new JLabel("Estado:"));
        panel.add(estado);
        panel.add(Box.createVerticalStrut(15));
        panel.add(new JLabel("Progreso del pedido:"));
        panel.add(progreso);
        panel.add(Box.createVerticalStrut(15));
        panel.add(atendidos);

        return panel;
    }

    private void agregarCliente() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el nombre del cliente.");
            return;
        }

        String tipoPedido = generarTipoPedido();
        int tiempoPreparacion = random.nextInt(4) + 2; // entre 2 y 5 segundos

        Cliente cliente = new Cliente(
                contadorIds++,
                nombre,
                tipoPedido,
                tiempoPreparacion
        );

        cola.encolar(cliente);
        actualizarAreaCola();

        txtNombre.setText("");
        txtNombre.requestFocus();
    }

    private String generarTipoPedido() {
        int opcion = random.nextInt(4);

        switch (opcion) {
            case 0:
                return "Café Americano";
            case 1:
                return "Capuchino";
            case 2:
                return "Latte";
            default:
                return "Mocaccino";
        }
    }

    public synchronized Cliente obtenerSiguienteCliente() {
        Cliente cliente = cola.desencolar();
        if (cliente != null) {
            SwingUtilities.invokeLater(this::actualizarAreaCola);
        }
        return cliente;
    }

    public synchronized void registrarAtencion(long tiempoEsperaMs) {
        totalAtendidos++;
        sumaTiemposEspera += tiempoEsperaMs;

        double promedioSegundos = (sumaTiemposEspera / (double) totalAtendidos) / 1000.0;

        SwingUtilities.invokeLater(() -> {
            lblTotalAtendidos.setText("Total atendidos: " + totalAtendidos);
            lblPromedioEspera.setText(String.format("Tiempo promedio de espera: %.2f s", promedioSegundos));
        });
    }

    public void actualizarAreaCola() {
        areaCola.setText(cola.mostrarCola());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimuladorCafeteria ventana = new SimuladorCafeteria();
            ventana.setVisible(true);
        });
    }
}

// ---------------- Clase Cliente ----------------
class Cliente {
    private final int id;
    private final String nombre;
    private final String tipoPedido;
    private final int tiempoPreparacion;
    private final long tiempoLlegada;

    public Cliente(int id, String nombre, String tipoPedido, int tiempoPreparacion) {
        this.id = id;
        this.nombre = nombre;
        this.tipoPedido = tipoPedido;
        this.tiempoPreparacion = tiempoPreparacion;
        this.tiempoLlegada = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoPedido() {
        return tipoPedido;
    }

    public int getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public long getTiempoLlegada() {
        return tiempoLlegada;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | " + nombre + " | " + tipoPedido + " | " + tiempoPreparacion + "s";
    }
}

// ---------------- Cola Manual con Nodos ----------------
class Cola<T> {

    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        public Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamano;

    public Cola() {
        frente = null;
        fin = null;
        tamano = 0;
    }

    public synchronized void encolar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);

        if (estaVacia()) {
            frente = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }

        tamano++;
    }

    public synchronized T desencolar() {
        if (estaVacia()) {
            return null;
        }

        T dato = frente.dato;
        frente = frente.siguiente;

        if (frente == null) {
            fin = null;
        }

        tamano--;
        return dato;
    }

    public synchronized T frente() {
        if (estaVacia()) {
            return null;
        }
        return frente.dato;
    }

    public synchronized boolean estaVacia() {
        return frente == null;
    }

    public synchronized int tamano() {
        return tamano;
    }

    public synchronized String mostrarCola() {
        if (estaVacia()) {
            return "No hay clientes en espera.";
        }

        StringBuilder sb = new StringBuilder();
        Nodo<T> actual = frente;

        while (actual != null) {
            sb.append(actual.dato.toString()).append("\n");
            actual = actual.siguiente;
        }

        return sb.toString();
    }
}

// ---------------- Clase Barista (Hilo) ----------------
class Barista implements Runnable {

    private final String nombreBarista;
    private final SimuladorCafeteria ventana;
    private final JLabel lblEstado;
    private final JProgressBar progreso;
    private final JLabel lblAtendidos;

    private int clientesAtendidos = 0;

    public Barista(String nombreBarista, SimuladorCafeteria ventana,
                   JLabel lblEstado, JProgressBar progreso, JLabel lblAtendidos) {
        this.nombreBarista = nombreBarista;
        this.ventana = ventana;
        this.lblEstado = lblEstado;
        this.progreso = progreso;
        this.lblAtendidos = lblAtendidos;
    }

    @Override
    public void run() {
        while (true) {
            Cliente cliente = ventana.obtenerSiguienteCliente();

            if (cliente != null) {
                long tiempoEspera = System.currentTimeMillis() - cliente.getTiempoLlegada();

                SwingUtilities.invokeLater(() -> {
                    lblEstado.setText("Atendiendo a " + cliente.getNombre());
                    progreso.setValue(0);
                });

                // Simular preparación
                int tiempoTotalMs = cliente.getTiempoPreparacion() * 1000;
                int pasos = 100;
                int pausa = tiempoTotalMs / pasos;

                for (int i = 1; i <= pasos; i++) {
                    try {
                        Thread.sleep(pausa);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    final int valor = i;
                    SwingUtilities.invokeLater(() -> progreso.setValue(valor));
                }

                clientesAtendidos++;
                ventana.registrarAtencion(tiempoEspera);

                SwingUtilities.invokeLater(() -> {
                    lblEstado.setText("Libre");
                    lblAtendidos.setText("Atendidos: " + clientesAtendidos);
                    progreso.setValue(0);
                });

            } else {
                SwingUtilities.invokeLater(() -> {
                    lblEstado.setText("Libre");
                    progreso.setValue(0);
                });

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}