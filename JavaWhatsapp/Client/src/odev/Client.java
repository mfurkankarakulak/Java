package odev;
import odev.cerceve.ClientFrame;

public class Client {

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {              //tanýmý aþaðýda yazýyor.
            public void run() {
                new ClientFrame().setVisible(true);
            }
        });
    }
}


/*invokeLater Desktop uygulamalarýnda  kullanýlýr.  java.awt.EventQueue sýnýfýndaki metotlarýn tetikleyen cover metottur.
invokeLater asenkron olarak çalýþtýrýlýr.

invokeLater'ýn önemi uzun hesaplamalar yapacaðýmýz zaman ya da network'e baðlantý yapýp bir veritabanýndan veri çekeceðimiz zaman ortaya çýkar. Eðer bu iþlemleri AWT-Thread (Event Dispatch Thread) içinde
 yaparsak ve kullanýcý pencereyi ikon durumuna getirip tekrar ekrana büyütürse pencere içerisinin boyanmadýðýný doðal olarak programýn donduðu gibi bir imaj oluþtuðunu görürsünüz.
*/
