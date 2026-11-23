package Chomsky;

import java.awt.*;
import javax.swing.*;

public class PanelPila extends PanelBase {

    private static final String[] NOMBRES = {
        "a^n b^n  (n >= 0)",
        "Paréntesis balanceados ( ) [ ] { }",
        "w c w^R  (palíndromo con marca central c)" };
    private static final String[][][] EJ = {
        { {"","1"}, {"aabb","1"}, {"aaabbb","1"}, {"aab","0"}, {"abab","0"}, {"ba","0"} },
        { {"([]{})","1"}, {"(()())","1"}, {"(]","0"}, {"(()","0"}, {"())","0"} },
        { {"abcba","1"}, {"abbcbba","1"}, {"c","1"}, {"abcab","0"}, {"abab","0"} } };

    private String entrada = "", q = "q0";
    private StringBuilder pila = new StringBuilder("Z");
    private int pos, preset;

    public PanelPila() {
        JPanel lienzo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujar((Graphics2D) g, getWidth(), getHeight());
            }
        };
        lienzo.setBackground(Color.WHITE);
        construir(lienzo);
    }

    @Override protected String[] nombres() { return NOMBRES; }
    @Override protected String[][] ejemplos(int p) { return EJ[p]; }

    @Override protected void iniciar(int p, String ent) {
        preset = p; entrada = ent; pos = 0; q = "q0";
        pila = new StringBuilder("Z");
        log("Inicio: estado q0, pila = Z (Z marca el fondo)");
    }

    private String[] regla(String q, char s, char t) {
        switch (preset) {
            case 0:
                if (q.equals("q0") && s == 'a') return new String[]{"q0", "" + t + "A"};
                if (s == 'b' && t == 'A' && (q.equals("q0") || q.equals("q1"))) return new String[]{"q1", ""};
                return null;
            case 1:
                if ("([{".indexOf(s) >= 0) return new String[]{q, "" + t + s};
                char m = s == ')' ? '(' : s == ']' ? '[' : s == '}' ? '{' : 0;
                if (m != 0 && t == m) return new String[]{q, ""};
                return null;
            default:
                if (q.equals("q0") && (s == 'a' || s == 'b')) return new String[]{"q0", "" + t + s};
                if (q.equals("q0") && s == 'c') return new String[]{"q1", "" + t};
                if (q.equals("q1") && (s == 'a' || s == 'b') && s == t) return new String[]{"q1", ""};
                return null;
        }
    }

    private boolean acepta() {
        boolean fondo = pila.length() == 1;
        return preset == 2 ? (q.equals("q1") && fondo) : fondo;
    }

    @Override protected void avanzar() {
        if (pos >= entrada.length()) { fin(); return; }
        char s = entrada.charAt(pos), t = pila.charAt(pila.length() - 1);
        String[] r = regla(q, s, t);
        if (r == null) {
            terminado = true; aceptada = false;
            log("(" + q + ", " + s + ", " + t + ") sin transición -> rechazada");
            return;
        }
        pila.setLength(pila.length() - 1);
        pila.append(r[1]);
        log("(" + q + ", " + s + ", " + t + ") " + FLECHA + " (" + r[0] + ", apila " + (r[1].isEmpty() ? EPS : r[1]) + ")");
        q = r[0]; pos++;
        if (pos >= entrada.length()) fin();
    }

    private void fin() {
        terminado = true; aceptada = acepta();
        log("Fin de la cadena. Pila (tope a la izquierda) = " + new StringBuilder(pila).reverse()
                + (aceptada ? " -> solo queda Z" : " -> no cumple la condición de aceptación"));
    }

    private void dibujar(Graphics2D g, int W, int H) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String[] celdas = entrada.isEmpty() ? new String[]{EPS} : entrada.split("");
        cinta(g, W, 40, celdas, (!terminado && pos < entrada.length()) ? pos : -1, pos, q);

        g.setColor(new Color(242, 245, 249));
        g.fillRoundRect(35, 122, 220, 44, 10, 10);
        g.setColor(new Color(210, 218, 226));
        g.drawRoundRect(35, 122, 220, 44, 10, 10);

        g.setColor(new Color(45, 95, 155));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.drawString("Estado actual: " + q, 48, 150);

        int cw = 144, x = W - cw - 65, base = H - 25, n = pila.length();
        int ch = Math.max(12, Math.min(34, (base - 130) / n - 3));
        g.setColor(new Color(55, 70, 90));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        texto(g, "Pila (tope arriba)", x + cw / 2.0, 110);
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(x - 6, 125, x - 6, base + 3);
        g.drawLine(x + cw + 6, 125, x + cw + 6, base + 3);
        g.drawLine(x - 6, base + 3, x + cw + 6, base + 3);
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < n; i++) {
            int y = base - (i + 1) * (ch + 3) + 3;
            g.setColor(i == n - 1 ? new Color(255, 214, 102) : new Color(240, 244, 249));
            g.fillRoundRect(x, y, cw, ch, 8, 8);
            g.setColor(i == n - 1 ? new Color(180, 120, 20) : new Color(130, 145, 160));
            g.drawRoundRect(x, y, cw, ch, 8, 8);
            g.setColor(new Color(30, 40, 50));
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, Math.min(16, ch - 2)));
            texto(g, "" + pila.charAt(i), x + cw / 2.0, y + ch / 2.0);
            if (i == n - 1) {
                g.setColor(new Color(45, 95, 155));
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                g.drawString("tope " + FLECHA, x - 62, y + ch / 2 + 4);
            }
        }
    }
}