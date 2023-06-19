package raf.agenthandler;

import raf.principal.RaAddress;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

/**
 * Class in charge of generating the view of the AgentHandler
 */
public class AgentHandlerView extends JFrame implements ActionListener, ListSelectionListener {

    private final AgentHandlerController controller;
    private final JList<DefaultListModel<String>> agentList;
    private final JMenu fileDropDown, selectedAgentDropDown;
    private final JScrollPane agentListScroller;

    public AgentHandlerView(AgentHandlerController controller){
        super("Agent handler");

        this.controller = controller;

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar (menuBar);

        // Load agent button
        fileDropDown = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Load...");
        menuItem.setActionCommand("LOAD");
        menuItem.addActionListener(this);
        fileDropDown.add (menuItem);
        menuBar.add(fileDropDown);

        // Manipulate agents menu
        selectedAgentDropDown = new JMenu ("Selected agent...");

        menuBar.add(selectedAgentDropDown);

        selectedAgentDropDown.setActionCommand("SELECTED_AGENT");

        selectedAgentDropDown.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (agentList.getSelectedIndex() == -1) {
                    selectedAgentDropDown.getPopupMenu().setVisible(false);
                    JOptionPane.showMessageDialog(null, "No agent selected.");
                }
            }
        });
        // Add dispatch button to "Selected agent..." menu
        JMenuItem dispatchItem = new JMenuItem("Dispatch to...");
        dispatchItem.setActionCommand("DISPATCH");
        dispatchItem.addActionListener(this);
        selectedAgentDropDown.add(dispatchItem);

        // Add delete button to "Selected agent..." menu
        JMenuItem deleteItem = new JMenuItem ("Delete");
        deleteItem.setActionCommand ("DELETE");
        deleteItem.addActionListener (this);
        selectedAgentDropDown.add(deleteItem);



        // Frame contents
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout (new GridLayout(1,1));
        panel.setPreferredSize(new java.awt.Dimension(500, 300));

        agentList = new JList(this.controller.getListModel());
        agentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agentList.addListSelectionListener(this);
        agentListScroller = new JScrollPane(agentList);

        panel.add(agentListScroller);
    }

    @Override
    public void actionPerformed (ActionEvent e){
        switch (e.getActionCommand().toUpperCase()) {
            case "LOAD" -> handleLoadItemClick();
            case "DISPATCH" -> handleDispatchItemClick();
            case "DELETE" -> handleDeleteItemClick();
            default -> throw new RuntimeException(
                    String.format("GRaLauncherView ActionEvent %s not implemented.", e.getActionCommand()));
        }
    }

    private void handleLoadItemClick(){
        String pick = (String) JOptionPane.showInputDialog(
                null,
                "Pick an agent",
                "Mobile agents",
                JOptionPane.PLAIN_MESSAGE,
                null,
                controller.getDisplayedAgentsPath().list(),
                null);
        if (pick != null)
            controller.handleUserSelectsAgent(pick);
    }
    private void handleDispatchItemClick(){
        if (controller.getRaAgency().getServers(this) != null) {
            Enumeration<RaAddress> serverEnumeration = controller.getRaAgency().getServers(this).elements();
            Object[] displayedAgencies = new Object[50];
            int i = 0;
            while (serverEnumeration.hasMoreElements()) {
                displayedAgencies[i] = serverEnumeration.nextElement();
                i++;
            }
            RaAddress pick = (RaAddress) JOptionPane.showInputDialog(
                    null,
                    "Elige una Agencia",
                    "Agencia Destino",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    displayedAgencies, null);
            if (pick != null)
                controller.handleUserDispatchesAgent(pick.toString());
            else
                JOptionPane.showMessageDialog(null, "No destination agency was selected.");
        }
        else JOptionPane.showMessageDialog(null, "No destination agencies available.");
    }
    private void handleDeleteItemClick(){
        controller.handleUserDeletesAgent();
        agentListScroller.revalidate();
        agentListScroller.repaint();
    }

    @Override
    public synchronized void valueChanged(ListSelectionEvent e) {
        controller.handleUserSelectsAgent(agentList.getSelectedIndex());
    }
}
