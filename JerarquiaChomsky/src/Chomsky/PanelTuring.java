package Chomsky;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class PanelTuring extends PanelBase {

    private static final String ABC = """
            q0,#=q1,#,R
            q1,a=q2,X,R
            q1,X=q1,X,R
            q1,Y=q5,Y,R
            q1,$=qA,$,S
            q2,a=q2,a,R
            q2,Y=q2,Y,R
            q2,b=q3,Y,R
            q3,b=q3,b,R
            q3,Z=q3,Z,R
            q3,c=q4,Z,L
            q4,a=q4,a,L
            q4,b=q4,b,L
            q4,X=q4,X,L
            q4,Y=q4,Y,L
            q4,Z=q4,Z,L
            q4,#=q1,#,R
            q5,Y=q5,Y,R
            q5,Z=q5,Z,R
            q5,$=qA,$,S
            """;
    private static final String INC = """
            q0,0=q0,0,R
            q0,1=q0,1,R
            q0,_=q1,_,L
            q1,1=q1,0,L
            q1,0=qA,1,S
            q1,_=qA,1,S
            """;
    private static final String SUMA = """
            q0,1=q0,1,R
            q0,+=q1,1,R
            q1,1=q1,1,R
            q1,_=q2,_,L
            q2,1=qA,_,S
            """;
    private static final String IGUAL = """
            q0,0=q1,X,R
            q0,Y=q4,Y,R
            q0,_=qA,_,S
            q1,0=q1,0,R
            q1,Y=q1,Y,R
            q1,1=q2,Y,L
            q2,0=q2,0,L
            q2,Y=q2,Y,L
            q2,X=q0,X,R
            q4,Y=q4,Y,R
            q4,_=qA,_,S
            """;

    private final boolean acotado;
    private final String[] nombres, tablas, desc;
    private final boolean[] muestraResultado;
    private final String[][][] ej;
    private final JTextArea tabla = new JTextArea();

    private Map<String, String[]> d = new LinkedHashMap<>();
    private List<String> cinta = new ArrayList<>();
    private String q = "q0";
    private int head, pasos, preset;

    public PanelTuring(boolean acotado) {
        this.acotado = acotado;
        if (acotado) {
            nombres = new String[]{"a^n b^n c^n  (no libre de contexto)"};
            tablas = new String[]{ABC};
            desc = new String[]{"Marca una a (X), una b (Y) y una c (Z) por ronda; el cabezal va y vuelve entre los delimitadores."};
            muestraResultado = new boolean[]{false};
            ej = new String[][][]{ { {"","1"}, {"abc","1"}, {"aabbcc","1"}, {"aaabbbccc","1"},
                                     {"aabbc","0"}, {"abcc","0"}, {"acb","0"} } };
        } else {
            nombres = new String[]{"Incrementar un número binario (+1)", "Suma unaria  (111+11 = 11111)", "Acepta 0^n 1^n"};
            tablas = new String[]{INC, SUMA, IGUAL};
            desc = new String[]{"Va al final del número y suma 1 propagando el acarreo hacia la izquierda.",
                                "Cambia el + por un 1 y borra el último 1 de la cinta.",
                                "Marca un 0 (X) y un 1 (Y) por ronda hasta que no queden símbolos sin marcar."};
            muestraResultado = new boolean[]{true, true, false};
            ej = new String[][][]{
                { {"1011","1"}, {"111","1"}, {"0","1"}, {"1000","1"} },
                { {"111+11","1"}, {"1+1","1"}, {"11+111","1"} },
                { {"0011","1"}, {"01","1"}, {"","1"}, {"001","0"}, {"0101","0"} } };
        }
        JPanel lienzo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujar((Graphics2D) g, getWidth(), getHeight());
            }
        };
        lienzo.setBackground(Color.WHITE);
        tabla.setEditable(false);
        tabla.setBackground(new Color(250, 251, 253));
        tabla.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(tabla);
        sp.setPreferredSize(new Dimension(250, 0));
        sp.setBorder(BorderFactory.createTitledBorder("Transiciones"));
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(lienzo, BorderLayout.CENTER);
        centro.add(sp, BorderLayout.EAST);
        construir(centro);
    }

    @Override protected String[] nombres() { return nombres; }
    @Override protected String[][] ejemplos(int p) { return ej[p]; }

    @Override protected void iniciar(int p, String ent) {
        preset = p;
        d = new LinkedHashMap<>();
        StringBuilder sb = new StringBuilder();
        for (String l : tablas[p].split("\n")) {
            l = l.trim();
            if (l.isEmpty()) continue;
            l = l.replace("#", IZQ).replace("$", DER);
            String[] kv = l.split("=");
            d.put(kv[0], kv[1].split(","));
            sb.append(DELTA).append("(").append(kv[0]).append(") = (").append(kv[1]).append(")\n");
        }
        tabla.setText(sb.toString());
        cinta = new ArrayList<>();
        if (acotado) {
            cinta.add(IZQ);
            for (char c : ent.toCharArray()) cinta.add("" + c);
            cinta.add(DER);
        } else if (ent.isEmpty()) cinta.add("_");
        else for (char c : ent.toCharArray()) cinta.add("" + c);
        head = 0; q = "q0"; pasos = 0;
        log("Inicio: estado q0, cabezal en la primera celda");
    }

    @Override protected void avanzar() {
        if (q.equals("qA")) { terminado = true; aceptada = true; return; }
        if (++pasos > 5000) { terminado = true; log("Se superó el límite de pasos (posible ciclo infinito)"); return; }
        String leido = cinta.get(head);
        String[] t = d.get(q + "," + leido);
        if (t == null) {
            terminado = true; aceptada = false;
            log(DELTA + "(" + q + ", " + leido + ") no está definida -> se detiene sin aceptar");
            return;
        }
        log(DELTA + "(" + q + ", " + leido + ") = (" + t[0] + ", " + t[1] + ", " + t[2] + ")");
        cinta.set(head, t[1]);
        q = t[0];
        if (t[2].equals("R")) head++;
        else if (t[2].equals("L")) head--;
        if (acotado) {
            if (head < 0 || head >= cinta.size()) {
                terminado = true; aceptada = false;
                log("El cabezal intentó salir de los delimitadores");
                return;
            }
        } else {
            if (head < 0) { cinta.add(0, "_"); head = 0; }
            if (head >= cinta.size()) cinta.add("_");
        }
        if (q.equals("qA")) { terminado = true; aceptada = true; }
    }

    private void dibujar(Graphics2D g, int W, int H) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        List<String> celdas = new ArrayList<>();
        int off = acotado ? 0 : 3;
        for (int i = 0; i < off; i++) celdas.add("_");
        celdas.addAll(cinta);
        for (int i = 0; i < off; i++) celdas.add("_");
        cinta(g, W, 60, celdas.toArray(new String[0]), head + off, -1, q);

        g.setColor(new Color(45, 95, 155));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.drawString("Estado activo: " + q + "     Pasos: " + pasos, 30, 160);
        g.setColor(new Color(55, 65, 75));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        g.drawString(acotado ? "Cinta acotada a la entrada: el cabezal no puede cruzar " + IZQ + " ni " + DER
                             : "Cinta infinita en ambos sentidos (los blancos \"_\" se agregan cuando hace falta)", 30, 195);
        g.drawString(desc[preset], 30, 220);
        if (terminado && aceptada && muestraResultado[preset]) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
            g.setColor(new Color(20, 130, 60));
            g.drawString("Resultado en la cinta: " + String.join("", cinta).replace("_", ""), 30, 265);
        }
    }
}