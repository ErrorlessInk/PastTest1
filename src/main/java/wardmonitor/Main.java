package wardmonitor;

import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class Main {
    private static final long REPORT_INTERVAL=TimeUnit.SECONDS.toNanos(30);

    private final EnvironmentalController controller=new EnvironmentalController();
    private final Dashboard dashboard=new Dashboard();
    private long nextReportTime;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    }

    private void start() {
        JFrame frame=new JFrame("Andressey Hospital - Children's Wing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(dashboard);
        frame.setSize(780,350);
        frame.setLocationRelativeTo(null);

        // Fill the grid immediately, then sample once a second on the Swing event thread.
        nextReportTime=System.nanoTime()+REPORT_INTERVAL;
        refresh();
        frame.setVisible(true);
        Timer timer=new Timer(1000,e -> refresh());
        timer.start();
    }

    private void refresh() {
        EnvironmentalController.Reading[] readings=controller.sample();
        dashboard.showReadings(readings);

        // Report the displayed snapshot without polling the environmental feeds again.
        long now=System.nanoTime();
        if (now-nextReportTime>=0) {
            printReport(readings);
            // Keep the 30-second schedule without replaying missed reports after a delay.
            nextReportTime+=REPORT_INTERVAL*(1+(now-nextReportTime)/REPORT_INTERVAL);
        }
    }

    private void printReport(EnvironmentalController.Reading[] readings) {
        StringBuilder report=new StringBuilder("Environmental report\n");
        for (EnvironmentalController.Reading reading : readings) {
            report.append(reading.getText()).append("\n\n");
        }
        report.append(controller.scannerReport());
        System.out.println(report);
    }
}
