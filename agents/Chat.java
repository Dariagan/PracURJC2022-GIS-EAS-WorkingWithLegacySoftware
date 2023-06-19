package raf.agents;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import raf.principal.Agent;
import raf.principal.RaAddress;

// MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE PONERSE A CARGAR AGENTES. CTRL+A Y CTRL+/ (DEL NUMPAD)
public class Chat extends Agent {
    /**
     * List of all the servers in the domain.
     */
    private final Vector<RaAddress> raAddresses = new Vector<>();
    private String s;

    /**
     * Points to the next destination in v.
     */
    int i;

    public Chat(String name) {
        super("Chat_" + name);
    }

    public void onCreate() {
        i = 0;
        s = "hola que tal";



        Enumeration<RaAddress> serversEnumeration = agency.getServers(this).elements();
        while (serversEnumeration.hasMoreElements()) {
            raAddresses.addElement(serversEnumeration.nextElement());
        }
    }

    /**
     * Shows chat window.
     */
    public void onArrival() {

        final TextComponent frame = new TextComponent();

        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            public void windowActivated(WindowEvent e) {
                frame.textPane.requestFocus();
            }
        };
        frame.addWindowListener(l);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * This is automatically called if the agent arrives on a base.
     */
    public void run() {

        try {
            if (i < raAddresses.size()) {
                destination = raAddresses.elementAt(i);
                ++i;
                System.out.println("Intentando disparar");
                fireDispatchRequest();
            } else {
                fireDestroyRequest();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            fireDestroyRequest();
        }
    }

    public class TextComponent extends JFrame implements java.awt.event.ActionListener {
        private final  JTextArea textPane;
        private final JTextArea changeLog;
        private final String newline = System.getProperty("line.separator");;

        private final JButton aceptar;

        public TextComponent() {
            // Some initial setup
            super("Chatero");

            // Create the text area and configure it
            textPane = new JTextArea(5, 40);
            textPane.setEditable(true);
            JScrollPane scrollPane = new JScrollPane(textPane);

            // Create the text area for the status log and configure it
            changeLog = new JTextArea(5, 30);
            changeLog.setText(s);
            changeLog.setEditable(false);
            JScrollPane scrollPaneForLog = new JScrollPane(changeLog);

            // Create a split pane for the change log and the text area
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, scrollPaneForLog);
            splitPane.setOneTouchExpandable(true);
            aceptar = new JButton("Aceptar");
            aceptar.addActionListener(this);
            aceptar.setActionCommand("enable");

            // Create the status area
            JPanel statusPane = new JPanel(new GridLayout(1, 1));

            // Add the components to the frame
            BorderLayout borderLayout = new BorderLayout();
            JPanel contentPane = new JPanel();
            contentPane.setLayout(borderLayout);
            contentPane.add(splitPane, BorderLayout.NORTH);
            contentPane.add(statusPane, BorderLayout.CENTER);
            contentPane.add(aceptar, BorderLayout.SOUTH);
            setContentPane(contentPane);
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (aceptar.getActionCommand().equals("enable")) {
                textPane.selectAll();
                s = s + textPane.getSelectedText();

                dispose();
            }
        }
    }
}