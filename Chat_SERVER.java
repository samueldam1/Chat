import java.io.*;
import java.net.*; 
import java.util.*;

public class Chat_SERVER {

    // Array con todos los clientes conectados al servidor
    public static List<Cliente> clientes = new ArrayList<>(); 
    
    private static final int MAX_USUARIOS = 10; // Numero fijo de usuarios maximos simultaneos
    
    
    public static void main(String[] args) {
        
        Scanner sc = new Scanner(System.in); // Instanciar Scanner
        
        System.out.println("SERVER: Asigna puerto al servidor: ");
        int puerto = 6666;
        //int puerto = sc.nextInt();
        
        ServerSocket serverSocket = null;         

        try {            
            serverSocket = new ServerSocket(puerto);
            System.out.println("SERVER: Servidor alojado en el puerto: " + puerto);   
            
            while (true) {   
                if (clientes.size() == 0)
                    System.out.println("SERVER: No hay ningún cliente conectado.");
                
                System.out.println("SERVER: Esperando cliente...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("SERVER: Nuevo cliente conectado!");
                
                if (clientes.size() < MAX_USUARIOS) {
                    Cliente instanciaCliente = new Cliente(clientSocket);
                    clientes.add(instanciaCliente);
                    instanciaCliente.start(); // Comienza un hilo en cliente
                    
                } else {
                    String respuesta = "Sala llena. Inténtelo más tarde";
                    DataOutputStream write = new DataOutputStream(clientSocket.getOutputStream()); 
                    write.writeUTF(respuesta);
                    System.out.println("Server: Numero de clientes excedido");
                }
            } 
            
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }
    
    /**
     * Método que dado un mensaje se lo envia a todos los clientes en el array clientes
     * y al servidor.
    */
    public static void broadcastMensaje(String mensaje) throws IOException {
        
        System.out.println(mensaje); // Mensaje en el servidor
        
        for (Cliente client : clientes) {
            client.broadcastMensaje(mensaje); // Metodo de Cliente
        }
    }
    
}

class Cliente extends Thread {
    
    private Socket clientSocket;
    private String nickname;
    
    private DataOutputStream write;
    private DataInputStream read;

    
    public Cliente (Socket socket) {
        this.clientSocket = socket;
    }
    
    public void run() {
        try {
            write = new DataOutputStream(clientSocket.getOutputStream());
            read = new DataInputStream(clientSocket.getInputStream());
            
            nickname = read.readUTF();
                System.out.println("SERVER: Nuevo cliente conectado: " + nickname);

            // Nuevo usuario se conecta
            Chat_SERVER.broadcastMensaje("> Nuevo cliente conectado: " + nickname + ". Actualmente hay " + Chat_SERVER.clientes.size() + " usuarios conectados.");
            
            write.writeUTF("Bienvenidx " + nickname + "!");
            
            // Manejo mensajes
            String mensajeUsuario;
            
            while((mensajeUsuario = read.readUTF()) != null ) { // Mientras el mensaje no sea nulo
                if (mensajeUsuario.equalsIgnoreCase("/bye")) {
                    break;
                }
                Chat_SERVER.broadcastMensaje(nickname + ": " + mensajeUsuario);            
            }
            
            // Mensaje de desconexión
            Chat_SERVER.broadcastMensaje("> " + nickname + " ha abandonado el chat");
            
            // Cerrar conexiones
            read.close();
            write.close();
            clientSocket.close();
            Chat_SERVER.clientes.remove(this);
            if (Chat_SERVER.clientes.size() == 0)
                    System.out.println("SERVER: No hay ningún cliente conectado.");
                
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    public void enviarMensaje(String mensaje) throws IOException {
        mensaje = nickname + ": " + mensaje;
        write.writeUTF(mensaje);
    }
    
    public void broadcastMensaje(String mensaje) throws IOException {
        write.writeUTF(mensaje);
    }
    
}
