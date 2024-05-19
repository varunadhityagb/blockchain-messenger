package App.Widgets;

import javax.swing.*;
import java.awt.*;

public class MessageBubble extends RoundedPanel {

    public MessageBubble(String text, boolean isSender) {
        setRoundBottomLeft(25);
        setRoundBottomRight(25);
        setRoundTopLeft(25);
        setRoundTopRight(25);
        int width = ((text.length() < 5) ? (text.length()+5) : (text.length()+5))*10;
        setPreferredSize(new Dimension(width, 50));
        setLayout(isSender ? new FlowLayout(FlowLayout.LEFT, 10, 10) : new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JLabel label = new JLabel(text);
        label.setFont(new Font("", Font.PLAIN, 20));
        label.setForeground(Color.BLACK);
        setBackground(isSender ? new Color(100, 255, 0) : new Color(0, 100, 230));
        add(label, BorderLayout.CENTER);




    }
}
