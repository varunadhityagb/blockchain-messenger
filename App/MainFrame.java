package App;

import javax.swing.*;
import java.awt.*;

public class MainFrame {

    public MainFrame(String userName)  {
        SkeletonFrame frame = new SkeletonFrame();
        frame.setSize(1028,720);
        JPanel userPanel = new JPanel();
        userPanel.setBackground(new Color(193, 73, 83));
        userPanel.setPreferredSize(new Dimension(300,100));

        JPanel chatPanel = new JPanel();
        chatPanel.setBackground(new Color(119, 136, 153));
        chatPanel.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(39, 48, 67));
        titlePanel.setPreferredSize(new Dimension(1000,50));
        titlePanel.setLayout(new BorderLayout());

        JLabel title = new JLabel(userName);
        title.setSize(new Dimension(500 ,100));
        title.setForeground(new Color(220, 238, 209));
        title.setFont(new Font("", Font.BOLD, 30));


        frame.add(userPanel, BorderLayout.WEST);
        frame.add(chatPanel);
        titlePanel.add(title, BorderLayout.CENTER);
        chatPanel.add(titlePanel, BorderLayout.NORTH);
    }
}
