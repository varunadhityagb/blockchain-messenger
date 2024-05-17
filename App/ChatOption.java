package App;

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

    ChatOption(String text) {
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
        } catch (NoSuchPaddingException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalBlockSizeException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (BadPaddingException ex) {
            throw new RuntimeException(ex);
        } catch (SignatureException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (InvalidKeyException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
