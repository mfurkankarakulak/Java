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
    private Map<String, ObjectOutputStream> mapOnlines = new HashMap<String, ObjectOutputStream>();                           //online kiþilerin tutuldugu liste. Hashmap veri yapýsý ile. map veri yapýsý key=string value=object

    public ServerService() {
        try {
            serverSocket = new ServerSocket(1234);															//sunucu çalýþtýrýldýgýnda 1234 portu aktif ediliyor 
            System.out.println("Sunucu Aktif");
            while (true) {
                socket = serverSocket.accept();
                new Thread(new ListenerSocket(socket)).start();												//sunucuya bir istek geldiginde thread olusturulurak sunucu server arasýnda soket olusturuluyor.
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class ListenerSocket implements Runnable {												//threada ait özellikler tanýmlandý

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
        public void run() {																						//thread çalýþtýgýnda run metodu kosar.
        	ChatMessage message = null;																			//UYARI: mesaj nesnedir. sunucu client arasýnda nesne alýsverisi gerçekleþiyor.
            try {
                while ((message = (ChatMessage) input.readObject()) != null) {                                //Server'a gelen mesaj nesnesi dinleniyo . Server'a gelen mesaj null degilse while'a gir
                    Action action = message.getAction();													
                    if (action.equals(Action.CONNECT)) {													//gelen aksiyonun(enum:connect,send,disconnect) bilgisi  action degiskenine atanýyor. (client giriþ butonuna basýnca action enum bilgisi connect olarak setleniyor)										
                        boolean isConnect = connect(message, output);										//client için connect metodu çalýþtýrýlýyor 
                        if (isConnect) {																	//connect true ise online kiþilerin listesine login olan kulanýcý ekleniyor.
                            mapOnlines.put(message.getName(), output);
                            sendOnlines();                                                                  //kullanýcý login yaptýktan sonra sunucu online  kiþilerin listesini(mapOnlines) günceller. Amaç: diðer kullanýcýlarýn ekranýnda login olan kiþi görülsün.
                        }
                    } else if (action.equals(Action.DISCONNECT)) {											//NOT:client sisteme girdikten sonra ilk önce online kiþilere eklenir ve sonra sendonlines metodu cagrilir.
                        disconnect(message, output);														//disconnect olursa liste güncellenir.
                        sendOnlines();																	
                        return;
                    } else if (action.equals(Action.SEND_ONE)) {	
                        sendOne(message);																	//clienttan gelen mesajýn action'u SEND_ONE ise sendOne metodu çagrýlýr.
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

    private boolean connect(ChatMessage message, ObjectOutputStream output) {             //client sunucuya baglanýr
        if (mapOnlines.size() == 0) {													//eger sunucuya baglý kimse yoksa direk mesajýn text'ini tes yapar ve send metodunu çaðýrýr. ardýndan true döner ve client sunucuya baglanýr.
            message.setText("YES");
            send(message, output);														//send metoduyla mesaj nesnesi text'i yes yapýlarak client'a geri gönderilir. Yani iþlemler nesne üzerinde gerçekleþtirilir.
            return true;
        }

        if (mapOnlines.containsKey(message.getName())) {                                   //içerde ayný iismde kulaýcý varsa hata verir.
            message.setText("NO");
            send(message, output);
            return false;
        } else {																		//içerde birileri varsa isim farklý ise client baglanýr.
            message.setText("YES");
            send(message, output);
            return true;
        }
    }

    private void disconnect(ChatMessage message, ObjectOutputStream output) {
        mapOnlines.remove(message.getName());

        message.setText(" sohbetten ayrýldý!");

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
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {				//online kiþilerin listesini açar ve 
            if (kv.getKey().equals(message.getMesajAlici())) {								//mesajý alacak kiþi listede var ise(getKey)          nameReserved=mesajý alacak kiþi
                try {
                    kv.getValue().writeObject(message);											//göndericinin gönderdiði mesaj nesnesini sunucu, alýcýya gönderir..okuma veya degisiklik yapmadan direk gönderir.
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void sendAll(ChatMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {				//tüm aktif clientlara mesaj atma metodu
            //if (!kv.getKey().equals(message.getName())) {
                message.setAction(Action.SEND_ONE);
                try {
                    kv.getValue().writeObject(message);											//mesaj nesnesi online kiþilerin hepsine gönderilir.
                } catch (IOException ex) {
                	 ex.printStackTrace();
                }
            //}
        }
    }

    private void sendOnlines() {
        Set<String> setNames = new HashSet<String>();
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {			//yeni oluþturulan HashSet vei yapýsýna onlineListteki mevcut kiþiler ekleniyor.(mevcut=eski onlinelar + þimdi giriþ yapan)
            setNames.add(kv.getKey());
        }

        ChatMessage message = new ChatMessage();
        message.setAction(Action.USERS_ONLINE);						//Yeni mesaj nesnesi olusturuluyor ve Action'una USERS_ONLINE setleniyor.
        message.setSetOnlines(setNames);							//mesaj nesnesinin setOnlines alanýna sistemdeki kullanýcýlar setleniyor.

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            message.setName(kv.getKey());
            try {
                kv.getValue().writeObject(message); 				//bu metodun sonucunda servera baglý tüm clientlara mesaj nesnesi gidiyor ve onlinekiþi listeleri güncelleniyor.
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
