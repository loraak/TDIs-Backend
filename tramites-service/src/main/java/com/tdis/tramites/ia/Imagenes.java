package com.tdis.tramites.ia;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/** Reduce la foto antes de mandarla a la IA: menos tokens, menos costo, más rápido. */
final class Imagenes {

    static final int LADO_MAXIMO = 1024;

    record ImagenLista(byte[] bytes, String mimeType) {
    }

    private Imagenes() {
    }

    static ImagenLista preparar(byte[] original) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(original));
            if (img == null) {
                return new ImagenLista(original, detectarMime(original));
            }
            double escala = Math.min(1.0, (double) LADO_MAXIMO / Math.max(img.getWidth(), img.getHeight()));
            int ancho = Math.max(1, (int) Math.round(img.getWidth() * escala));
            int alto = Math.max(1, (int) Math.round(img.getHeight() * escala));

            // RGB con fondo blanco (los PNG con transparencia no se pueden guardar como JPEG)
            BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = salida.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, ancho, alto);
            g.drawImage(img, 0, 0, ancho, alto, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(salida, "jpg", out);
            return new ImagenLista(out.toByteArray(), "image/jpeg");
        } catch (Exception e) {
            return new ImagenLista(original, detectarMime(original));
        }
    }

    private static String detectarMime(byte[] b) {
        if (b.length > 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        return "image/jpeg";
    }
}
