package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.osaamismerkki.OsaamismerkkiTila;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaLiiteDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import org.apache.commons.lang.time.DateUtils;
import org.apache.tika.mime.MimeTypeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class OsaamismerkkiServiceIT extends AbstractIntegrationTest {

    @Autowired
    private OsaamismerkkiService osaamismerkkiService;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void addKategoriaJaOsaamismerkki() throws HttpMediaTypeNotSupportedException, MimeTypeException {
        OsaamismerkkiKategoriaDto kategoria = osaamismerkkiService.updateKategoria(createKategoria());
        assertThat(osaamismerkkiService.getKategoriat()).hasSize(1);

        osaamismerkkiService.updateOsaamismerkki(createOsaamismerkki(kategoria));
        assertThat(osaamismerkkiService.findBy(new OsaamismerkkiQuery())).hasSize(1);
    }

    @Test
    public void findOsaamismerkit() throws HttpMediaTypeNotSupportedException, MimeTypeException {
        OsaamismerkkiKategoriaDto kategoria = osaamismerkkiService.updateKategoria(createKategoria());

        OsaamismerkkiDto julkaistavaMerkki = createOsaamismerkki(kategoria);
        julkaistavaMerkki.setTila(OsaamismerkkiTila.JULKAISTU);

        osaamismerkkiService.updateOsaamismerkki(julkaistavaMerkki);
        osaamismerkkiService.updateOsaamismerkki(createOsaamismerkki(kategoria));

        assertThat(osaamismerkkiService.findBy(new OsaamismerkkiQuery())).hasSize(2);
        assertThat(osaamismerkkiService.findJulkisetBy(new OsaamismerkkiQuery())).hasSize(1);
    }

    @Test
    public void deleteOsaamismerkki() throws HttpMediaTypeNotSupportedException, MimeTypeException {
        OsaamismerkkiKategoriaDto kategoria = osaamismerkkiService.updateKategoria(createKategoria());

        OsaamismerkkiDto merkki = osaamismerkkiService.updateOsaamismerkki(createOsaamismerkki(kategoria));
        assertThat(osaamismerkkiService.findBy(new OsaamismerkkiQuery())).hasSize(1);
        osaamismerkkiService.deleteOsaamismerkki(merkki.getId());
        assertThat(osaamismerkkiService.findBy(new OsaamismerkkiQuery())).hasSize(0);
    }

    @Test()
    public void deleteKategoria() throws HttpMediaTypeNotSupportedException, MimeTypeException {
        OsaamismerkkiKategoriaDto kategoria = osaamismerkkiService.updateKategoria(createKategoria());
        assertThat(osaamismerkkiService.getKategoriat()).hasSize(1);
        osaamismerkkiService.deleteKategoria(kategoria.getId());
        assertThat(osaamismerkkiService.getKategoriat()).hasSize(0);
    }

    @Test
    public void deleteKategoriaFail() throws HttpMediaTypeNotSupportedException, MimeTypeException {
        expectedEx.expect(BusinessRuleViolationException.class);
        expectedEx.expectMessage("osaamismerkkiin-liitettya-kategoriaa-ei-voi-poistaa");

        OsaamismerkkiKategoriaDto kategoria = osaamismerkkiService.updateKategoria(createKategoria());
        osaamismerkkiService.updateOsaamismerkki(createOsaamismerkki(kategoria));
        osaamismerkkiService.deleteKategoria(kategoria.getId());
    }

    private OsaamismerkkiKategoriaDto createKategoria() {
        // 1x1 kokoinen jpeg-kuva binääristringinä, samanlaisena minä käliltä tulee bäkkärille
        String kuvaBinaarina = "/9j/4AAQSkZJRgABAQEA8ADwAAD/2wBDAAIBAQIBAQICAgICAgICAwUDAwMDAwYEBAMFBwYHBwcGBwcICQsJCAgKCAcHCg0KCgsMDAwMBwkODw0MDgsMDAz/2wBDAQICAgMDAwYDAwYMCAcIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAz/wAARCAABAAEDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9/KKKKAP/2Q==";

        OsaamismerkkiKategoriaLiiteDto kategoriaLiite = new OsaamismerkkiKategoriaLiiteDto();
        kategoriaLiite.setBinarydata(kuvaBinaarina);
        kategoriaLiite.setMime("image/jpeg");

        OsaamismerkkiKategoriaDto kategoria = new OsaamismerkkiKategoriaDto();
        kategoria.setNimi(LokalisoituTekstiDto.of("kategoria"));
        kategoria.setLiite(kategoriaLiite);
        return kategoria;
    }

   private OsaamismerkkiDto createOsaamismerkki(OsaamismerkkiKategoriaDto kategoria) {
       OsaamismerkkiDto merkki = new OsaamismerkkiDto();
       merkki.setNimi(LokalisoituTekstiDto.of("osaamismerkki"));
       merkki.setTila(OsaamismerkkiTila.LAADINTA);
       merkki.setKategoria(kategoria);
       merkki.setVoimassaoloAlkaa(DateUtils.addDays(new Date(),-1));
       merkki.setArviointikriteerit(new ArrayList<>());
       merkki.setOsaamistavoitteet(new ArrayList<>());
       return merkki;
   }
}
