package App.Widgets;

import App.ChatFrame;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class ChatOption extends JButton implements ActionListener {

    public ChatOption(String text) {
        ImageIcon userImage = new ImageIcon("App/images/user.png");
        setText(text);
        setIcon(userImage);
        addActionListener(this);
        setFont(new Font("",Font.PLAIN, 20));
        setPreferredSize(new Dimension(250,50));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = ((ChatOption) e.getSource()).getText();
        try {
            new ChatFrame(name);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 SignatureException | IOException | InvalidKeyException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
