import App.Callback;
import App.LoginFrame;
import App.MainFrame;

import javax.swing.*;
import java.io.*;
import java.security.Key;

public class Main implements Callback {
    public static String userName="nOtFiLlEd";
    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        File privateKeyFile = new File("private_key.ser");
        File publicKeyFile = new File("public_key.ser");
        File userNameFile = new File("userName.ser");

        if (!privateKeyFile.exists() && !publicKeyFile.exists() && !userNameFile.exists()) {
            SwingUtilities.invokeLater((Runnable) () -> new Main().showLoginFrame());
        } else {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("userName.ser"))) {
                userName = (String) ois.readObject();
            }
        }

        MainFrame mainFrame = new MainFrame(userName);



    }

    private void showLoginFrame() {
        LoginFrame loginFrame = new LoginFrame(this);

    }

    public void onLogin(String userName) {
        Main.userName = userName;
    }
}
