package wardmonitor;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class Dashboard extends JPanel {
    private final JLabel[] cells=new JLabel[5];

    Dashboard() {
        super(new GridLayout(0,3, 8,8));
        setBorder(BorderFactory.createEmptyBorder(8,8, 8,8));

        // Put the wards in the first row and the shared air readings below them.
        for (int i=0; i<cells.length; i++) {
            JLabel cell=new JLabel("", JLabel.CENTER);
            cell.setOpaque(true);
            cell.setForeground(Color.BLACK);
            cells[i]=cell;
            add(cell);
        }
    }

    void showReadings(EnvironmentalController.Reading[] readings) {
        // Change each cell independently and clear its warning when it recovers.
        for (int i=0; i<cells.length; i++) {
            EnvironmentalController.Reading reading=readings[i];
            cells[i].setText("<html>"+reading.getText().replace("\n","<br>")+"</html>");
            cells[i].setBackground(reading.hasWarning() ? Color.RED : Color.WHITE);
        }
    }
}

