package odev.cerceve;

import odev.model.ChatMessage;
import odev.model.ChatMessage.Action;
import odev.service.ClientService;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;


public class ClientFrame extends javax.swing.JFrame {

    private Socket socket;
    private ChatMessage message;
    private ClientService service;
    
    private javax.swing.JButton btnBaglan;
    private javax.swing.JButton btnGonder;
    private javax.swing.JButton btnHerkeseGonder;
    private javax.swing.JButton btnResimGonder;
    private javax.swing.JButton btnCikis;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList listOnlines;
    private javax.swing.JTextArea txtAreaReceive;
    private javax.swing.JTextArea txtAreaSend;
    private javax.swing.JTextField txtName;
    private javax.swing.JLabel messageLabel;
    
    public ClientFrame() {
        initComponents();
    }

    private class ListenerSocket implements Runnable {													//soket dinleme sýnýfý. Kullanýcý giriþ yaptýktan sonra yapacaðý iþlemler bu sýnýfta tanýmlanmýstýr

        private ObjectInputStream input;                                                                //input=serverdan clienta gelen stream(veri)

        public ListenerSocket(Socket socket) {															
            try {
                this.input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            ChatMessage message = null;
            try {
                while ((message = (ChatMessage) input.readObject()) != null) {
                	Action action = message.getAction();										//gelen mesaj nesnesinin action bilgisi alýnýr ve aþaðýdaki iþlemlerden biri yapýlýr.

                    if (action.equals(Action.CONNECT)) {
                        connected(message);									//connect metodu çalýþtýrýlýr.
                    } else if (action.equals(Action.DISCONNECT)) {
                        disconnected();                                    //ilgili komponentler true false yapýlýr
                        socket.close();
                    } else if (action.equals(Action.SEND_ONE)) {
                        showMessage(message);													//clienta gelen mesaj nesnesinin  action'u SEND_ONE ise showMessage metodu çalýþtýrýlýyor. 
                    } else if (action.equals(Action.USERS_ONLINE)) {
                        refreshOnlines(message);										
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void connected(ChatMessage message) {
        if (message.getText().equals("NO")) {
            this.txtName.setText("");
            JOptionPane.showMessageDialog(this, "Bu isimde bir kullanýcý zaten sistemde mevcut.");
            return;
        }

        this.message = message;
        this.btnBaglan.setEnabled(false);
        this.txtName.setEditable(false);				//kullanýcý basarýlý giriþ yaparsa gerekli componentlerin enable true yapýldý.

        this.btnCikis.setEnabled(true);
        this.txtAreaSend.setEnabled(true);
        this.txtAreaReceive.setEnabled(true);
        this.btnGonder.setEnabled(true);
        this.btnResimGonder.setEnabled(true);
        this.btnHerkeseGonder.setEnabled(true);

        JOptionPane.showMessageDialog(this, "Baþarýlý bir þekilde giriþ yaptýnýz.");
    }

    private void disconnected() {											//ilgili komponentler true false yapýlýyor.

        this.btnBaglan.setEnabled(true);
        this.txtName.setEditable(true);

        this.btnCikis.setEnabled(false);
        this.txtAreaSend.setEnabled(false);
        this.txtAreaReceive.setEnabled(false);
        this.btnGonder.setEnabled(false);
        this.btnResimGonder.setEnabled(true);
        this.btnHerkeseGonder.setEnabled(false);
        this.btnResimGonder.setEnabled(false);
        this.txtAreaReceive.setText("");
        this.txtAreaSend.setText("");

        JOptionPane.showMessageDialog(this, "Baþarý bir þekilde çýkýþ yaptýnýz.");
    }

    private void refreshOnlines(ChatMessage message) {
        System.out.println(message.getSetOnlines().toString());
        
        Set<String> names = message.getSetOnlines();
        
        names.remove(message.getName());
        
        String[] array = (String[]) names.toArray(new String[names.size()]);
        
        this.listOnlines.setListData(array);
        this.listOnlines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.listOnlines.setLayoutOrientation(JList.VERTICAL);
    }

    @SuppressWarnings("unchecked")            									 // 	deprecated metotlar varsa warning vermesini engelliyor.
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        btnBaglan = new javax.swing.JButton();          //Baðlan butonu
        btnCikis = new javax.swing.JButton();			   //Çýkýþ butonu
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listOnlines = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAreaReceive = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAreaSend = new javax.swing.JTextArea();
        btnGonder = new javax.swing.JButton();			//Gönder butonu
        btnResimGonder = new javax.swing.JButton();	
        btnHerkeseGonder = new javax.swing.JButton();			//Herkese gönder butonu

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Connect Panel"));

        btnBaglan.setText("Baðlan");
        btnBaglan.addActionListener(new java.awt.event.ActionListener() {				//baðlan butonu listeneri
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBaglanActionPerformed(evt);
            }
        });

        btnCikis.setText("Çýkýþ"); 
        btnCikis.setEnabled(false);
        btnCikis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {                   //çýkýþ butonu listeneri
                btnCikisActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBaglan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCikis)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnBaglan)
                .addComponent(btnCikis))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Online Kiþiler"));

        jScrollPane3.setViewportView(listOnlines);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setLayout(new BoxLayout(jPanel4, BoxLayout.Y_AXIS));
       
        jScrollPane1.setViewportView(jPanel4);

        txtAreaSend.setColumns(20);
        txtAreaSend.setRows(5);
        txtAreaSend.setEnabled(false);
        jScrollPane2.setViewportView(txtAreaSend);

        btnGonder.setText("Gönder");
        btnGonder.setEnabled(false);
        btnGonder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {			//gönder butonu listeneri
                btnGonderActionPerformed(evt);
            }
        });

        btnHerkeseGonder.setText("Herkese Gönder");
        btnHerkeseGonder.setEnabled(false);
        btnHerkeseGonder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHerkeseGonderActionPerformed(evt);									//herkese gönder butonu listeneri
            }
        });

        btnResimGonder.setText("Resim Gönder");
        btnResimGonder.setEnabled(false);
        btnResimGonder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {			//gönder butonu listeneri
                btnResimGonderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)				//jScrollPane1 Yatay Uzunluk Ayarlama
                    .addComponent(jScrollPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
        				.addComponent(btnResimGonder)
                        .addComponent(btnHerkeseGonder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGonder)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)		// jScrollPane1 Dikey Uzunluk Ayarlama
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            		.addComponent(btnResimGonder)
                    .addComponent(btnGonder)
                    .addComponent(btnHerkeseGonder))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }
    public void showMessage(ChatMessage msg) {
    	if(msg.getImage() == null) {															//eðer text göndeirliyorsa
    		if(!(msg.getName().equals(this.txtName.getText())))
    			messageLabel = new JLabel(msg.getName() + " : " + msg.getText());				//yeni label olusturulup gönderilen metin label içerisine yazýlýyor.
    		else
    			messageLabel = new JLabel("Sen : " + msg.getText());
        	jPanel4.add(messageLabel);
        	jPanel4.revalidate();
        	jPanel4.repaint();	
    	}else {																							//eðer resim alýþverisi oluyorsa.
    		Image temp = msg.getImage().getImage();
    		temp = temp.getScaledInstance(200, 200,  java.awt.Image.SCALE_SMOOTH); 						//gönderilen resim imageicon oalrak gönderiliyor. büyük boyutta gönderiliyor. alýcý image 
    		ImageIcon newImageIcon = new ImageIcon(temp);												//formatýna dönüþtürüp resmin boyutunu küçülttükten sonra tekrar imageicon'a dönüþtürüp ekranda gösteriyor.
    		
    		if(!(msg.getName().equals(this.txtName.getText())))
    			messageLabel = new JLabel("" ,newImageIcon,SwingConstants.LEFT);        //alýcýnýn ekranýnda olusan resim
    		else
    			messageLabel = new JLabel("",newImageIcon,SwingConstants.LEFT);						//göndericinin ekranýnda olusan resim
    		
        	jPanel4.add(messageLabel);
        	jPanel4.revalidate();
        	jPanel4.repaint();	
    	}
    	
    }

    private void btnBaglanActionPerformed(java.awt.event.ActionEvent evt) {				//ilk önce baglan butonuna týklanýr. mesaj nesnesinin action'u connect yapýlarak sunucuya gönderilir.
        String name = this.txtName.getText();												//ardýndan sunucudan gelen meaj nesnesi ListenerSocket içerisinde okunur ve YES-NO bilgisine göre iþlem yapýlýr.									

        if (!name.isEmpty()) {
            this.message = new ChatMessage();
            this.message.setAction(Action.CONNECT);
            this.message.setName(name);                                                  //mesajýn adý göndericinin adý ile setlenir.

            this.service = new ClientService();											
            this.socket = this.service.connect();										//client ile sunucu arasýnda soket olusturuluyor

            new Thread(new ListenerSocket(this.socket)).start();

            this.service.send(message);													//ardýndan sunucuya mesaj nesnesi gönderiliyor.(action:connect)
        }
    }

    private void btnCikisActionPerformed(java.awt.event.ActionEvent evt) {				//çýkýþ butonu
        ChatMessage message = new ChatMessage();										//yeni mesaj nesnesi olusturup action'u disconnect yapýlýyor ve sunucuya gönderiliyor.
        message.setName(this.message.getName());
        message.setAction(Action.DISCONNECT);
        this.service.send(message);
        disconnected();																	//ilgili komponentler true false yapýlýyor.
    }

    private void btnHerkeseGonderActionPerformed(java.awt.event.ActionEvent evt) {                     //herkese gönder butonu
    	this.listOnlines.clearSelection();
    	
    	String text = this.txtAreaSend.getText();
        
        String name = this.message.getName();											//text:gönderilecek metin         name:göndericinin adý
        
        this.message = new ChatMessage();												//yeni mesaj nesnesi olusturuluyor								
        
        if (this.listOnlines.getSelectedIndex() > -1)  {
            this.message.setMesajAlici((String) this.listOnlines.getSelectedValue());   //listede seçilen kiþi mesaj alýcý olarak setleniyor.
            this.message.setAction(Action.SEND_ONE);									//olusturulan nesnenin action'u send_one olarak setleniyor.
           
        } else {
            this.message.setAction(Action.SEND_ALL);									//eger lsiteden kiþi seçilmezse action send_all olarak seçiliyor
        }
        
        this.message.setName(name);													//gönderici setleniyo
        this.message.setText(text);													//mesajýn içerigi setlendi
        if (!text.isEmpty()) {
            showMessage(this.message);
            this.service.send(this.message);											//olusturulan mesaj nesnesi sunucuya gönderiliyor
       
        }else {
        	JOptionPane.showMessageDialog(this, "Boþ mesaj gönderemezsiniz!");
        }
        this.txtAreaSend.setText("");
    }

    private void btnGonderActionPerformed(java.awt.event.ActionEvent evt) {            //gönder butonuna 
        String text = this.txtAreaSend.getText();
        
        String name = this.message.getName();											//text:gönderilecek metin         name:göndericinin adý
        
        this.message = new ChatMessage();												//yeni mesaj nesnesi olusturuluyor								
        
        if (this.listOnlines.getSelectedIndex() > -1)  {
            this.message.setMesajAlici((String) this.listOnlines.getSelectedValue());   //listede seçilen kiþi mesaj alýcý olarak setleniyor.
            this.message.setAction(Action.SEND_ONE);									//olusturulan nesnenin action'u send_one olarak setleniyor.
           
        } else {
            this.message.setAction(Action.SEND_ALL);									//eger lsiteden kiþi seçilmezse action send_all olarak seçiliyor
        }
        
        this.message.setName(name);													//gönderici setleniyo
        this.message.setText(text);													//mesajýn içerigi setlendi
        if (!text.isEmpty()) {
            showMessage(this.message);
            this.service.send(this.message);											//olusturulan mesaj nesnesi sunucuya gönderiliyor
        
        }else {
        	JOptionPane.showMessageDialog(this, "Boþ mesaj gönderemezsiniz!");
        }
        this.txtAreaSend.setText("");
       
    }
    private void btnResimGonderActionPerformed(java.awt.event.ActionEvent evt) {            //gönder butonuna 
    	
    	JFileChooser fileChooser = new JFileChooser();
		fileChooser.showDialog(this, "Resim Sec");
		File file = fileChooser.getSelectedFile();
		ImageIcon img = null;
		
		try {
			if(file != null)
				img = new ImageIcon(ImageIO.read(file));
			else
				System.out.println("resim gönderme basarisiz oldu.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        String name = this.message.getName();											//text:gönderilecek metin         name:göndericinin adý
        
        this.message = new ChatMessage();												//yeni mesaj nesnesi olusturuluyor								
        this.message.setImage(img);
        
        
        if (this.listOnlines.getSelectedIndex() > -1)  {
            this.message.setMesajAlici((String) this.listOnlines.getSelectedValue());   //listede seçilen kiþi mesaj alýcý olarak setleniyor.
            this.message.setAction(Action.SEND_ONE);									//olusturulan nesnenin action'u send_one olarak setleniyor.
        } else {
            this.message.setAction(Action.SEND_ALL);									//eger lsiteden kiþi seçilmezse action send_all olarak seçiliyor
        }
        
        this.message.setName(name);													//gönderici setleniyo
        														
            showMessage(this.message);
            this.service.send(this.message);											//olusturulan mesaj nesnesi sunucuya gönderiliyor
       
    }

    
}
