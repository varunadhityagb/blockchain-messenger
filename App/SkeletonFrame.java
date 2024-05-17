package App;

import javax.swing.*;
import java.awt.*;

public class SkeletonFrame extends JFrame {

    SkeletonFrame() {
        ImageIcon icon = new ImageIcon("App/images/messenger.png");
        this.setTitle("Blockchain Messenger");
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(icon.getImage());
        this.setSize(512,512);
        this.setMinimumSize(new Dimension(331, 322));
        this.getContentPane().setBackground(new Color(119, 136, 153));

    }

}


/*
    Elegant Background (Light Slate Gray): RGB(119, 136, 153)
    Secondary Background (Dark Olive Green): RGB(85, 107, 47)
    Primary Accent Color (Dark Orchid): RGB(153, 50, 204)
    Secondary Accent Color (Dark Cyan): RGB(0, 139, 139)
    Warning/Error Color (Firebrick): RGB(178, 34, 34)
    Success Color (Medium Spring Green): RGB(0, 250, 154)
    Info Color (Light Sea Green): RGB(32, 178, 170)
    Text Color (Black): RGB(0, 0, 0) or RGB(33, 33, 33) for softer black
    Subdued Text Color (Dim Gray): RGB(105, 105, 105)
 */
