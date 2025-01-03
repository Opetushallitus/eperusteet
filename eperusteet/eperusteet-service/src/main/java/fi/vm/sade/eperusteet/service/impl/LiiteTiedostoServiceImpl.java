package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;
import fi.vm.sade.eperusteet.service.LiiteService;
import fi.vm.sade.eperusteet.service.LiiteTiedostoService;
import fi.vm.sade.eperusteet.service.util.Pair;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import jakarta.servlet.http.Part;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Set;
import java.util.UUID;

@Service
public class LiiteTiedostoServiceImpl implements LiiteTiedostoService {

    private static final int BUFSIZE = 64 * 1024;
    @Autowired
    private LiiteService liitteet;

    final Tika tika = new Tika();

    public Pair<UUID, String> uploadFile(
            Long id,
            String nimi,
            InputStream is,
            long koko,
            LiiteTyyppi tyyppi,
            Set<String> tyypit,
            Integer width,
            Integer height,
            Part file) throws IOException, MimeTypeException, HttpMediaTypeNotSupportedException {
        try (PushbackInputStream pis = new PushbackInputStream(is, BUFSIZE)) {
            byte[] buf = new byte[koko < BUFSIZE ? (int) koko : BUFSIZE];
            int len = pis.read(buf);
            if (len < buf.length) {
                throw new IOException("luku epäonnistui");
            }
            pis.unread(buf);
            String mime = tika.detect(buf);
            MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
            String extension = mimeTypes.forName(mime).getExtension();

            if (!tyypit.contains(mime)) {
                throw new HttpMediaTypeNotSupportedException(mime + " ei ole tuettu");
            }

            if (width != null && height != null && file != null) {
                String mediaType = tika.detect(buf);
                ByteArrayOutputStream os = scaleImage(file, mediaType, width, height);
                return Pair.of(liitteet.add(id, tyyppi, mime, nimi, os.size(), new PushbackInputStream(new ByteArrayInputStream(os.toByteArray()))), extension);
            } else {
                if (LiiteTyyppi.JULKAISUMUUTOSMAARAYS.equals(tyyppi)) {
                    return Pair.of(liitteet.addJulkaisuLiite(null, tyyppi, mime, nimi, koko, pis), extension);
                } else if (LiiteTyyppi.OSAAMISMERKKIKUVA.equals(tyyppi)) {
                    return Pair.of(liitteet.addOsaamismerkkiLiite(tyyppi, mime, nimi, koko, pis), extension);
                } else {
                    return Pair.of(liitteet.add(id, tyyppi, mime, nimi, koko, pis), extension);
                }
            }
        }
    }

    private ByteArrayOutputStream scaleImage(@RequestParam("file") Part file, String tyyppi, Integer width, Integer height) throws IOException {
        BufferedImage a = ImageIO.read(file.getInputStream());
        BufferedImage preview = new BufferedImage(width, height, a.getType());
        preview.createGraphics().drawImage(a.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(preview, tyyppi.replace("image/", ""), os);
        return os;
    }
}
