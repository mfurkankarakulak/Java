package odev.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;

public class ChatMessage implements Serializable {                              //mesaj nesnemizin olusturuldugu sýnýf
    
    private String name;
    private String text;
    private String mesajAlici;
    private Set<String> setOnlines = new HashSet<String>();
    private Action action;
    private ImageIcon img;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMesajAlici() {
        return mesajAlici;
    }

    public void setMesajAlici(String mesajAlici) {
        this.mesajAlici = mesajAlici;
    }

    public Set<String> getSetOnlines() {
        return setOnlines;
    }

    public void setSetOnlines(Set<String> setOnlines) {
        this.setOnlines = setOnlines;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
    public ImageIcon getImage() {
    	return img;
    }
    public void setImage(ImageIcon img) {
    	this.img = img;
    }        
    public enum Action {
        CONNECT, DISCONNECT, SEND_ONE, SEND_ALL, USERS_ONLINE
    }
}
