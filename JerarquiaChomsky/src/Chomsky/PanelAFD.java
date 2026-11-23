package Chomsky;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PanelAFD extends PanelBase {

    static class Afd {
        String inicio;
        List<String> estados = new ArrayList<>();
        Set<String> finales = new HashSet<>();
        Map<String, Map<Character, String>> d = new LinkedHashMap<>();
    }

    private static final String T01 = "inicio: q0\nfinal: q2\nq0,0,q1\nq0,1,q0\nq1,0,q1\nq1,1,q2\nq2,0,q1\nq2,1,q0";
    private static final String PAR = "inicio: q0\nfinal: q0\nq0,0,q0\nq0,1,q1\nq1,0,q1\nq1,1,q0";
    private static final String ABA = "inicio: q0\nfinal: q3\nq0,a,q1\nq0,b,q0\nq1,a,q1\nq1,b,q2\nq2,a,q3\nq2,b,q0\nq3,a,q3\nq3,b,q3";

    private static final String[] NOMBRES = {
        "Cadenas binarias que terminan en 01",
        "Cantidad par de 1s",
        "Contiene la subcadena aba",
        "Personalizado (editar el autómata)" };
    private static final String[] TEXTOS = { T01, PAR, ABA, T01 };
    private static final String[][][] EJ = {
        { {"01","1"}, {"1101","1"}, {"0101","1"}, {"10","0"}, {"0","0"}, {"","0"} },
        { {"","1"}, {"1001","1"}, {"0110","1"}, {"111","0"}, {"10","0"} },
        { {"baba","1"}, {"aabab","1"}, {"abba","0"}, {"bbb","0"} },
        { {"01","1"}, {"10","0"} } };

    private final JTextArea editor = new JTextArea(T01);
    private final JScrollPane panelEditor;
    private Afd afd;
    private String entrada = "", cur = "";
    private String[] ultima;
    private int pos;

    public PanelAFD() {
        JPanel lienzo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujar((Graphics2D) g, getWidth(), getHeight());
            }
        };
        lienzo.setBackground(Color.WHITE);
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        editor.setBackground(new Color(250, 251, 253));
        panelEditor = new JScrollPane(editor);
        panelEditor.setPreferredSize(new Dimension(235, 0));
        panelEditor.setBorder(BorderFactory.createTitledBorder("inicio / final / origen,símbolo,destino"));
        panelEditor.setVisible(false);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(lienzo, BorderLayout.CENTER);
        centro.add(panelEditor, BorderLayout.EAST);
        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { reiniciar(); }
            public void removeUpdate(DocumentEvent e) { reiniciar(); }
            public void changedUpdate(DocumentEvent e) { }
        });
        construir(centro);
    }

    @Override protected String[] nombres() { return NOMBRES; }
    @Override protected String[][] ejemplos(int p) { return EJ[p]; }

    private static Afd parsear(String txt) {
        Afd a = new Afd();
        for (String linea : txt.split("\n")) {
            linea = linea.trim();
            if (linea.isEmpty()) continue;
            String low = linea.toLowerCase();
            if (low.startsWith("inicio:")) a.inicio = linea.substring(7).trim();
            else if (low.startsWith("final:")) {
                for (String f : linea.substring(6).split(",")) if (!f.trim().isEmpty()) a.finales.add(f.trim());
            } else {
                String[] t = linea.split(",");
                if (t.length == 3 && !t[1].trim().isEmpty())
                    a.d.computeIfAbsent(t[0].trim(), k -> new LinkedHashMap<>()).put(t[1].trim().charAt(0), t[2].trim());
            }
        }
        if (a.inicio == null && !a.d.isEmpty()) a.inicio = a.d.keySet().iterator().next();
        if (a.inicio != null) {
            a.estados.add(a.inicio);
            for (var e : a.d.entrySet()) {
                if (!a.estados.contains(e.getKey())) a.estados.add(e.getKey());
                for (String dest : e.getValue().values()) if (!a.estados.contains(dest)) a.estados.add(dest);
            }
            for (String f : a.finales) if (!a.estados.contains(f)) a.estados.add(f);
        }
        return a;
    }

    @Override protected void iniciar(int preset, String ent) {
        boolean custom = preset == NOMBRES.length - 1;
        panelEditor.setVisible(custom);
        afd = parsear(custom ? editor.getText() : TEXTOS[preset]);
        entrada = ent; pos = 0; ultima = null;
        if (afd.inicio == null) { cur = ""; terminado = true; log("El autómata está vacío."); return; }
        cur = afd.inicio;
        log("Inicio en " + cur);
        revalidate();
    }

    @Override protected void avanzar() {
        if (pos >= entrada.length()) { fin(); return; }
        char s = entrada.charAt(pos);
        String nx = afd.d.getOrDefault(cur, Map.of()).get(s);
        if (nx == null) {
            terminado = true; aceptada = false;
            log(DELTA + "(" + cur + ", " + s + ") no está definida -> rechazada");
            return;
        }
        log(DELTA + "(" + cur + ", " + s + ") = " + nx);
        ultima = new String[]{cur, nx};
        cur = nx; pos++;
        if (pos >= entrada.length()) fin();
    }

    private void fin() { terminado = true; aceptada = afd.finales.contains(cur); }

    private void dibujar(Graphics2D g, int W, int H) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String[] celdas = entrada.isEmpty() ? new String[]{EPS} : entrada.split("");
        cinta(g, W, 40, celdas, (!terminado && pos < entrada.length()) ? pos : -1, pos, cur);
        if (afd == null || afd.inicio == null) return;

        int n = afd.estados.size();
        double R = 26, cx = W / 2.0, cy = 115 + (H - 115) / 2.0;
        double rx = n > 1 ? Math.min(W / 2.0 - 110, 300) : 0, ry = n > 1 ? Math.max(40, (H - 115) / 2.0 - 80) : 0;
        Map<String, double[]> P = new HashMap<>();
        for (int i = 0; i < n; i++) {
            double a = Math.PI + 2 * Math.PI * i / n;
            P.put(afd.estados.get(i), new double[]{cx + rx * Math.cos(a), cy + ry * Math.sin(a)});
        }
        Map<String, String> aristas = new LinkedHashMap<>();
        for (var e : afd.d.entrySet())
            for (var t : e.getValue().entrySet())
                aristas.merge(e.getKey() + "|" + t.getValue(), String.valueOf(t.getKey()), (x, y) -> x + "," + y);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        for (var ar : aristas.entrySet()) {
            String[] ab = ar.getKey().split("\\|");
            double[] p1 = P.get(ab[0]), p2 = P.get(ab[1]);
            boolean hot = ultima != null && ultima[0].equals(ab[0]) && ultima[1].equals(ab[1]);
            g.setColor(hot ? new Color(45, 95, 155) : new Color(130, 140, 155));
            g.setStroke(new BasicStroke(hot ? 3f : 1.7f));
            double x1 = p1[0], y1 = p1[1], x2 = p2[0], y2 = p2[1], lx, ly;
            if (ab[0].equals(ab[1])) {
                double dx = x1 - cx, dy = y1 - cy, l = Math.hypot(dx, dy);
                if (n == 1 || l < 1) { dx = 0; dy = -1; } else { dx /= l; dy /= l; }
                double px = -dy, py = dx;
                double ex = x1 + dx * R + px * 11, ey = y1 + dy * R + py * 11;
                double c2x = x1 + dx * (R + 55) + px * 28, c2y = y1 + dy * (R + 55) + py * 28;
                g.draw(new CubicCurve2D.Double(x1 + dx * R - px * 11, y1 + dy * R - py * 11,
                        x1 + dx * (R + 55) - px * 28, y1 + dy * (R + 55) - py * 28, c2x, c2y, ex, ey));
                flecha(g, ex, ey, Math.atan2(ey - c2y, ex - c2x));
                lx = x1 + dx * (R + 62); ly = y1 + dy * (R + 62);
            } else {
                double L = Math.hypot(x2 - x1, y2 - y1), px = -(y2 - y1) / L, py = (x2 - x1) / L;
                double sg = 1;
                if (!aristas.containsKey(ab[1] + "|" + ab[0])) {
                    double mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
                    if (Math.hypot(mx - px * 30 - cx, my - py * 30 - cy) > Math.hypot(mx + px * 30 - cx, my + py * 30 - cy)) sg = -1;
                }
                double qx = (x1 + x2) / 2 + px * 38 * sg, qy = (y1 + y2) / 2 + py * 38 * sg;
                double ux = qx - x1, uy = qy - y1, ul = Math.hypot(ux, uy); ux /= ul; uy /= ul;
                double vx = qx - x2, vy = qy - y2, vl = Math.hypot(vx, vy); vx /= vl; vy /= vl;
                double ex = x2 + vx * (R + 2), ey = y2 + vy * (R + 2);
                g.draw(new QuadCurve2D.Double(x1 + ux * R, y1 + uy * R, qx, qy, ex, ey));
                flecha(g, ex, ey, Math.atan2(ey - qy, ex - qx));
                lx = (x1 + x2) / 4 + qx / 2 + px * 10 * sg; ly = (y1 + y2) / 4 + qy / 2 + py * 10 * sg;
            }
            g.setColor(hot ? new Color(45, 95, 155) : new Color(40, 50, 60));
            texto(g, ar.getValue(), lx, ly);
        }
       
        double[] s0 = P.get(afd.inicio);
        g.setColor(new Color(45, 95, 155)); g.setStroke(new BasicStroke(1.8f));
        g.draw(new Line2D.Double(s0[0] - R - 40, s0[1], s0[0] - R - 8, s0[1]));
        flecha(g, s0[0] - R - 1, s0[1], 0);
       
        for (String s : afd.estados) {
            double[] p = P.get(s);
            Ellipse2D c = new Ellipse2D.Double(p[0] - R, p[1] - R, 2 * R, 2 * R);
            g.setColor(s.equals(cur) ? new Color(255, 214, 102) : new Color(245, 248, 252));
            g.fill(c);
            g.setColor(afd.finales.contains(s) ? new Color(30, 120, 75) : new Color(55, 70, 90));
            g.setStroke(new BasicStroke(2f)); g.draw(c);
            if (afd.finales.contains(s)) g.draw(new Ellipse2D.Double(p[0] - R + 5, p[1] - R + 5, 2 * R - 10, 2 * R - 10));
            g.setColor(new Color(25, 35, 45));
            texto(g, s, p[0], p[1]);
        }
    }
}