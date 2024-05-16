package App;

import blockchain.Crypto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;


public class LoginFrame {

    public LoginFrame(Callback callback) {
        SkeletonFrame frame = new SkeletonFrame();
        frame.callback = callback;
        frame.setSize(331, 322);
        frame.setLayout(new GridLayout(3,1));

        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        String publicKeyString;


        KeyPair keyPair;
        File privateKeyFile = new File("private_key.ser");
        File publicKeyFile = new File("public_key.ser");

        if (!privateKeyFile.exists() && !publicKeyFile.exists()) {
            keyPair = Crypto.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            Crypto.serializeKeyPair(keyPair);
        } else {
            keyPair = Crypto.deserializeKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        }



        JLabel name = new JLabel("Enter your name: ");
        name.setPreferredSize(new Dimension(100,15));
        JTextField nameText = new JTextField();
        nameText.setPreferredSize(new Dimension(100,15));
        JLabel key = new JLabel("Public key: ");
        key.setPreferredSize(new Dimension(100,15));
        JLabel keyText = new JLabel(publicKeyString);

        panel1.add(name);
        panel1.add(nameText);

        panel2.add(key);
        panel2.add(keyText);

        JButton login = new JButton("Login");
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nameOfUser = nameText.getText();
                try (ObjectOutputStream userFile = new ObjectOutputStream(new FileOutputStream("userName.ser")))
                {
                    userFile.writeObject(nameOfUser);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (!nameOfUser.isEmpty()) {
                    callback.onLogin(nameOfUser);
                    frame.dispose();
                } else
                    JOptionPane.showMessageDialog(frame, "Please enter your name", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(panel1);
        frame.add(panel2);
        frame.add(login);

        panel1.revalidate();
        panel2.revalidate();
    }
}
