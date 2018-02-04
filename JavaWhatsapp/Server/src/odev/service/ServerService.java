package odev.service;

//import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;

import odev.model.ChatMessage;
import odev.model.ChatMessage.Action;

public class ServerService {

    private ServerSocket serverSocket;
    private Socket socket;
    private Map<String, ObjectOutputStream> mapOnlines = new HashMap<String, ObjectOutputStream>();                           //online ki�ilerin tutuldugu liste. Hashmap veri yap�s� ile. map veri yap�s� key=string value=object

    public ServerService() {
        try {
            serverSocket = new ServerSocket(1234);															//sunucu �al��t�r�ld�g�nda 1234 portu aktif ediliyor 
            System.out.println("Sunucu Aktif");
            while (true) {
                socket = serverSocket.accept();
                new Thread(new ListenerSocket(socket)).start();												//sunucuya bir istek geldiginde thread olusturulurak sunucu server aras�nda soket olusturuluyor.
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class ListenerSocket implements Runnable {												//threada ait �zellikler tan�mland�

        private ObjectOutputStream output;
        private ObjectInputStream input;

        public ListenerSocket(Socket socket) {
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream (socket.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {																						//thread �al��t�g�nda run metodu kosar.
        	ChatMessage message = null;																			//UYARI: mesaj nesnedir. sunucu client aras�nda nesne al�sverisi ger�ekle�iyor.
            try {
                while ((message = (ChatMessage) input.readObject()) != null) {                                //Server'a gelen mesaj nesnesi dinleniyo . Server'a gelen mesaj null degilse while'a gir
                    Action action = message.getAction();													
                    if (action.equals(Action.CONNECT)) {													//gelen aksiyonun(enum:connect,send,disconnect) bilgisi  action degiskenine atan�yor. (client giri� butonuna bas�nca action enum bilgisi connect olarak setleniyor)										
                        boolean isConnect = connect(message, output);										//client i�in connect metodu �al��t�r�l�yor 
                        if (isConnect) {																	//connect true ise online ki�ilerin listesine login olan kulan�c� ekleniyor.
                            mapOnlines.put(message.getName(), output);
                            sendOnlines();                                                                  //kullan�c� login yapt�ktan sonra sunucu online  ki�ilerin listesini(mapOnlines) g�nceller. Ama�: di�er kullan�c�lar�n ekran�nda login olan ki�i g�r�ls�n.
                        }
                    } else if (action.equals(Action.DISCONNECT)) {											//NOT:client sisteme girdikten sonra ilk �nce online ki�ilere eklenir ve sonra sendonlines metodu cagrilir.
                        disconnect(message, output);														//disconnect olursa liste g�ncellenir.
                        sendOnlines();																	
                        return;
                    } else if (action.equals(Action.SEND_ONE)) {	
                        sendOne(message);																	//clienttan gelen mesaj�n action'u SEND_ONE ise sendOne metodu �agr�l�r.
                    } else if (action.equals(Action.SEND_ALL)) {
                        sendAll(message);
                    }
                }
            } catch (IOException ex) {
                ChatMessage cm = new ChatMessage();
                cm.setName(message.getName());
                disconnect(cm, output);
                sendOnlines();
                
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean connect(ChatMessage message, ObjectOutputStream output) {             //client sunucuya baglan�r
        if (mapOnlines.size() == 0) {													//eger sunucuya bagl� kimse yoksa direk mesaj�n text'ini tes yapar ve send metodunu �a��r�r. ard�ndan true d�ner ve client sunucuya baglan�r.
            message.setText("YES");
            send(message, output);														//send metoduyla mesaj nesnesi text'i yes yap�larak client'a geri g�nderilir. Yani i�lemler nesne �zerinde ger�ekle�tirilir.
            return true;
        }

        if (mapOnlines.containsKey(message.getName())) {                                   //i�erde ayn� iismde kula�c� varsa hata verir.
            message.setText("NO");
            send(message, output);
            return false;
        } else {																		//i�erde birileri varsa isim farkl� ise client baglan�r.
            message.setText("YES");
            send(message, output);
            return true;
        }
    }

    private void disconnect(ChatMessage message, ObjectOutputStream output) {
        mapOnlines.remove(message.getName());

        message.setText(" sohbetten ayr�ld�!");

        message.setAction(Action.SEND_ONE);

        sendAll(message);

        System.out.println("User " + message.getName() + " cikis yapti.");
    }

    private void send(ChatMessage message, ObjectOutputStream output) {
        try {
            output.writeObject(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendOne(ChatMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {				//online ki�ilerin listesini a�ar ve 
            if (kv.getKey().equals(message.getMesajAlici())) {								//mesaj� alacak ki�i listede var ise(getKey)          nameReserved=mesaj� alacak ki�i
                try {
                    kv.getValue().writeObject(message);											//g�ndericinin g�nderdi�i mesaj nesnesini sunucu, al�c�ya g�nderir..okuma veya degisiklik yapmadan direk g�nderir.
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void sendAll(ChatMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {				//t�m aktif clientlara mesaj atma metodu
            //if (!kv.getKey().equals(message.getName())) {
                message.setAction(Action.SEND_ONE);
                try {
                    kv.getValue().writeObject(message);											//mesaj nesnesi online ki�ilerin hepsine g�nderilir.
                } catch (IOException ex) {
                	 ex.printStackTrace();
                }
            //}
        }
    }

    private void sendOnlines() {
        Set<String> setNames = new HashSet<String>();
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {			//yeni olu�turulan HashSet vei yap�s�na onlineListteki mevcut ki�iler ekleniyor.(mevcut=eski onlinelar + �imdi giri� yapan)
            setNames.add(kv.getKey());
        }

        ChatMessage message = new ChatMessage();
        message.setAction(Action.USERS_ONLINE);						//Yeni mesaj nesnesi olusturuluyor ve Action'una USERS_ONLINE setleniyor.
        message.setSetOnlines(setNames);							//mesaj nesnesinin setOnlines alan�na sistemdeki kullan�c�lar setleniyor.

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            message.setName(kv.getKey());
            try {
                kv.getValue().writeObject(message); 				//bu metodun sonucunda servera bagl� t�m clientlara mesaj nesnesi gidiyor ve onlineki�i listeleri g�ncelleniyor.
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
