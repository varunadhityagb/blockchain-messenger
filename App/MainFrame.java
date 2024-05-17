package App;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class MainFrame {

    public MainFrame(String userName)  {
        SkeletonFrame frame = new SkeletonFrame();
        frame.setSize(331,720);
        frame.setResizable(false);
        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(100,50));
        topPanel.setBackground(new Color(39, 48, 67));
        topPanel.setLayout(new BorderLayout(40,0));
        frame.add(topPanel, BorderLayout.NORTH);

        ImageIcon settingsIcon = new ImageIcon("App/images/settings.png");
        ImageIcon newIcon = new ImageIcon("App/images/plus.png");

        JButton addChat = new JButton(newIcon);
        JLabel title = new JLabel("Messenger");
        JButton settings = new JButton(settingsIcon);

        title.setForeground(Color.lightGray);
        title.setFont(new Font("", Font.BOLD,25));

        topPanel.add(addChat, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);
        topPanel.add(settings, BorderLayout.EAST);

        topPanel.revalidate();

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10,15));
        userPanel.setBackground(new Color(39, 48, 67));

        ChatOption o = new ChatOption("0000");
        userPanel.add(new ChatOption("varun"));
        userPanel.add(o);

        userPanel.add(new ChatOption("praanesh"));


        frame.add(userPanel);
        userPanel.revalidate();

    }
}



//new Color(39, 48, 67)