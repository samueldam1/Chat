import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chat_CLIENTE {

    public static void main(String[] args){
        
        Scanner sc = new Scanner(System.in); // Instanciar Scanner   
          
        System.out.println("Introduzca la dirección IP del servidor:");
        String ip;
        //ip = sc.next();
        ip = "192.168.0.1";
        
        System.out.println("Introduzca el puerto del servidor:");
        int puerto;
        //puerto = sc.nextInt();
        puerto = 6666;
        
        // ESTABLECER NICKNAME                       
        System.out.println("Introduzca su nombre de usuario:");
        String nickname = sc.next();
        
        // ESTABLECER CONEXION
        try {
            Socket clientSocket = new Socket();		
            InetSocketAddress addr = new InetSocketAddress(ip, puerto);
            clientSocket.connect(addr);
            
            // ENVIAR NICKNAME
            DataOutputStream write = new DataOutputStream(clientSocket.getOutputStream());
            write.writeUTF(nickname); // Enviar mensaje  
            
            // INSTANCIAR HILOLECTURA
            HiloLectura hiloleer = new HiloLectura(clientSocket);
            hiloleer.start();
            
            // INSTANCIAR HILOESCRITURA
            HiloEscritura hiloescribir = new HiloEscritura(clientSocket);
            hiloescribir.start();
        
        } catch (IOException ex) {
            Logger.getLogger(Chat_CLIENTE.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

class HiloLectura extends Thread {

    Socket socket;
    DataInputStream lectura;
    
    public HiloLectura (Socket socket) {
        this.socket = socket;    
    }
        
    public void run() {
        try {
            lectura = new DataInputStream(socket.getInputStream());
            
            while(true) {                
                String mensaje = lectura.readUTF();          
                System.out.println(mensaje);    
                
                if (mensaje.equals("Sala llena. Inténtelo más tarde"))
                    System.exit(0);         
            }            
        } catch (IOException ex) {
            System.exit(0);  
        }
    }

}

class HiloEscritura extends Thread {

    Socket socket;
    DataOutputStream write;
    Scanner sc;
    
    public HiloEscritura (Socket socket) {
        this.socket = socket;    
    }
        
    public void run() {
        try {
            write = new DataOutputStream(socket.getOutputStream());
            sc = new Scanner(System.in); // Instanciar Scanner  
            
            while(true) {                
                String mensaje = sc.nextLine();
                if (mensaje != "/bye")
                    write.writeUTF(mensaje);
                else 
                    break;                    
            }
            
            // Cerrar instancias
            sc.close();
            write.close();                   
            socket.close();
            System.exit(0);   
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}