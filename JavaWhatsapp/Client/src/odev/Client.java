package odev;
import odev.cerceve.ClientFrame;

public class Client {

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {              //tan�m� a�a��da yaz�yor.
            public void run() {
                new ClientFrame().setVisible(true);
            }
        });
    }
}


/*invokeLater Desktop uygulamalar�nda  kullan�l�r.  java.awt.EventQueue s�n�f�ndaki metotlar�n tetikleyen cover metottur.
invokeLater asenkron olarak �al��t�r�l�r.

invokeLater'�n �nemi uzun hesaplamalar yapaca��m�z zaman ya da network'e ba�lant� yap�p bir veritaban�ndan veri �ekece�imiz zaman ortaya ��kar. E�er bu i�lemleri AWT-Thread (Event Dispatch Thread) i�inde
 yaparsak ve kullan�c� pencereyi ikon durumuna getirip tekrar ekrana b�y�t�rse pencere i�erisinin boyanmad���n� do�al olarak program�n dondu�u gibi bir imaj olu�tu�unu g�r�rs�n�z.
*/
