package Chomsky;

import java.awt.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Simulador de la jerarquía de Chomsky");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            tabs.setBackground(new Color(235, 240, 245));
            tabs.addTab("Nivel 3 · Regulares", new PanelAFD());
            tabs.addTab("Nivel 2 · Libres de contexto", new PanelPila());
            tabs.addTab("Nivel 1 · Sensibles al contexto", new PanelTuring(true));
            tabs.addTab("Nivel 0 · Máquina de Turing", new PanelTuring(false));
            f.add(tabs);
            f.setSize(1060, 710);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}