package App;

import blockchain.BlockChain;
import blockchain.Crypto;
import blockchain.DigitalSignature;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

public class InfoPage{
    public InfoPage(String text) {
        SkeletonFrame frame = new SkeletonFrame();
        frame.setSize(250, 200);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2 - 15, dim.height/2-frame.getSize().height/2);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 20));
        ImageIcon userIcon = new ImageIcon("App/images/circle-user.png");
        JLabel userIconLabel = new JLabel(userIcon);
        frame.add(userIconLabel);
        JLabel userNameLabel = new JLabel(text);
        userNameLabel.setFont(new Font("", Font.BOLD, 20));
        frame.add(userNameLabel);
        JButton copyPublicKey = new JButton("Copy Public Key");
        frame.add(copyPublicKey);
        copyPublicKey.addActionListener(e -> {
            KeyPair keyPair = Crypto.deserializeKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            StringSelection stringSelection = new StringSelection(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
    }
}
