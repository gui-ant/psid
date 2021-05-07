package grp07;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import common.CustomLogger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainForm {

    private final CloudToCluster cloudToCluster;
    private final ClusterToMySQL clusterToMySQL;

    private JButton btnExecute;
    private JTextArea txtLog;
    private JButton btn2;
    private JButton btn3;
    private JPanel btnPanel;
    private JPanel mainPanel;
    private JComboBox selectMethod;

    public MainForm(CloudToCluster cloudToCluster, ClusterToMySQL clusterToMySQL) throws Exception {
        this.cloudToCluster = cloudToCluster;
        this.clusterToMySQL = clusterToMySQL;
        this.selectMethod.addItem("direct");
        this.selectMethod.addItem("mqtt");

        CustomLogger logger = new CustomLogger<JTextArea>() {

            @Override
            protected JTextArea getLogComponent() {
                return MainForm.this.txtLog;
            }

            @Override
            protected void writeToComponent(String text) {
                getLogComponent().append(text + "\n");
            }
        };
        this.cloudToCluster.setLog(logger);
        this.clusterToMySQL.setLog(logger);

        JFrame frame = new JFrame("FrmCloudToCluster");

        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        btnExecute.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                String selectedItem = (String) MainForm.this.selectMethod.getSelectedItem();

                cloudToCluster.initialize();

                if (selectedItem.equals("direct")) {
                    clusterToMySQL.initialize();
                } else if (selectedItem.equals("mqtt")) {

                }
            }
        });
    }

    public static void main(String[] args) {
        /* Linhas adicionas para desabilitar logs do mongo.driver na consola */
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        try {
            new MainForm(new CloudToCluster(), new ClusterToMySQL());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
