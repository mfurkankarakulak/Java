package odev.service;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import odev.model.ChatMessage;

public class ClientService {
    
    private Socket socket;
    private ObjectOutputStream output;
    
    public Socket connect() {
        try {
            this.socket = new Socket("192.168.19.145", 1234);
            this.output = new ObjectOutputStream(socket.getOutputStream());
        } catch (UnknownHostException ex) {
        	ex.printStackTrace();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        
        return socket;
    }
    
    public void send(ChatMessage message) {
        try {
            output.writeObject(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
