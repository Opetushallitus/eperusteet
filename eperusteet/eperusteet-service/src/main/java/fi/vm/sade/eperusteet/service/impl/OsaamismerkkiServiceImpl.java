package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.liite.LiiteTyyppi;
import fi.vm.sade.eperusteet.domain.osaamismerkki.Osaamismerkki;
import fi.vm.sade.eperusteet.domain.osaamismerkki.OsaamismerkkiKategoria;
import fi.vm.sade.eperusteet.domain.osaamismerkki.OsaamismerkkiTila;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiBaseDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiExternalDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiKategoriaLiiteDto;
import fi.vm.sade.eperusteet.dto.osaamismerkki.OsaamismerkkiQuery;
import fi.vm.sade.eperusteet.dto.util.PageDto;
import fi.vm.sade.eperusteet.repository.OsaamismerkkiKategoriaRepository;
import fi.vm.sade.eperusteet.repository.OsaamismerkkiRepository;
import fi.vm.sade.eperusteet.repository.OsaamismerkkiRepositoryCustom;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepository;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.LiiteTiedostoService;
import fi.vm.sade.eperusteet.service.OsaamismerkkiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional
public class OsaamismerkkiServiceImpl implements OsaamismerkkiService {

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OsaamismerkkiRepository osaamismerkkiRepository;

    @Autowired
    private OsaamismerkkiKategoriaRepository osaamismerkkiKategoriaRepository;

    @Autowired
    private OsaamismerkkiRepositoryCustom osaamismerkkiRepositoryCustom;

    @Autowired
    private LiiteRepository liiteRepository;

    @Autowired
    private LiiteTiedostoService liiteTiedostoService;

    @Autowired
    private KoodistoClient koodistoClient;

    public static final Set<String> IMAGE_TYPES;

    static {
        IMAGE_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                MediaType.IMAGE_JPEG_VALUE,
                MediaType.IMAGE_PNG_VALUE,
                "image/svg+xml"
        )));
    }

    @Override
    public List<OsaamismerkkiExternalDto> getOsaamismerkit() {
        return mapper.mapAsList(osaamismerkkiRepository.findAllByTila(OsaamismerkkiTila.JULKAISTU), OsaamismerkkiExternalDto.class) ;
    }

    @Override
    public OsaamismerkkiDto getOsaamismerkkiByUri(String koodiUri) {
        return mapper.map(osaamismerkkiRepository.findByKoodiUriAndTila(koodiUri, OsaamismerkkiTila.JULKAISTU), OsaamismerkkiDto.class) ;
    }

    @Override
    public List<OsaamismerkkiBaseDto> findJulkisetBy(OsaamismerkkiQuery query) {
        query.setTila(Collections.singleton(OsaamismerkkiTila.JULKAISTU.toString()));

        Page<Osaamismerkki> osaamismerkit = osaamismerkkiRepositoryCustom.findBy(PageRequest.of(0, 1000), query);
        return mapper.mapAsList(osaamismerkit.getContent(), OsaamismerkkiBaseDto.class);
    }

    @Override
    public Page<OsaamismerkkiDto> findBy(OsaamismerkkiQuery query) {
        PageRequest pageRequest = PageRequest.of(query.getSivu(), query.getSivukoko());
        Page<Osaamismerkki> osaamismerkit = osaamismerkkiRepositoryCustom.findBy(pageRequest, query);
        return new PageDto<>(osaamismerkit, OsaamismerkkiDto.class, pageRequest, mapper);
    }

    @Override
    public OsaamismerkkiBaseDto getJulkinenOsaamismerkkiById(Long id) {
        Osaamismerkki osaamismerkki = osaamismerkkiRepository.findByIdAndTila(id, OsaamismerkkiTila.JULKAISTU);
        return mapper.map(osaamismerkki, OsaamismerkkiBaseDto.class);
    }

    @Override
    public OsaamismerkkiBaseDto getJulkinenOsaamismerkkiByKoodi(Long koodi) {
        String koodiUri = KoodistoUriArvo.OSAAMISMERKIT + "_" + koodi;
        Osaamismerkki osaamismerkki = osaamismerkkiRepository.findByKoodiUriAndTila(koodiUri, OsaamismerkkiTila.JULKAISTU);
        return mapper.map(osaamismerkki, OsaamismerkkiBaseDto.class);
    }

    @Override
    @Transactional
    public OsaamismerkkiDto updateOsaamismerkki(OsaamismerkkiDto osaamismerkkiDto) {
        Osaamismerkki osaamismerkki = mapper.map(osaamismerkkiDto, Osaamismerkki.class);

        if (OsaamismerkkiTila.JULKAISTU.equals(osaamismerkki.getTila())) {
            if (ObjectUtils.isEmpty(osaamismerkki.getKoodiUri())) {
                KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella(KoodistoUriArvo.OSAAMISMERKIT, osaamismerkkiDto.getNimi());
                if (lisattyKoodi != null) {
                    osaamismerkki.setKoodiUri(lisattyKoodi.getKoodiUri());
                }
            }
            else if (hasRelevantKoodiDataChanged(osaamismerkkiDto)) {
                updateKoodistoKoodi(osaamismerkkiDto);
            }
        }

        osaamismerkki = osaamismerkkiRepository.save(osaamismerkki);
        return mapper.map(osaamismerkki, OsaamismerkkiDto.class);
    }

    @Override
    @Transactional
    public void deleteOsaamismerkki(Long id) {
        Osaamismerkki osaamismerkki = osaamismerkkiRepository.findById(id).orElse(null);
        osaamismerkkiRepository.delete(osaamismerkki);
    }

    @Override
    public List<OsaamismerkkiKategoriaDto> getKategoriat() {
        List<OsaamismerkkiKategoria> kategoriat = osaamismerkkiKategoriaRepository.findAll();
        return mapper.mapAsList(kategoriat, OsaamismerkkiKategoriaDto.class);
    }

    @Override
    public List<OsaamismerkkiKategoriaDto> getJulkisetKategoriat(OsaamismerkkiQuery query) {
        List<OsaamismerkkiKategoria> kategoriat = osaamismerkkiKategoriaRepository.findAll();
        List<Osaamismerkki> osaamismerkit = mapper.mapAsList(findJulkisetBy(query), Osaamismerkki.class);

        List<OsaamismerkkiKategoria> julkisetKategoriat = kategoriat.stream()
                .filter(kategoria -> osaamismerkit.stream()
                        .map(Osaamismerkki::getKategoria)
                        .map(OsaamismerkkiKategoria::getId)
                        .anyMatch(merkki -> merkki.equals(kategoria.getId())))
                .collect(toList());

        return mapper.mapAsList(julkisetKategoriat, OsaamismerkkiKategoriaDto.class);
    }

    @Override
    @Transactional
    public OsaamismerkkiKategoriaDto updateKategoria(OsaamismerkkiKategoriaDto kategoriaDto) throws HttpMediaTypeNotSupportedException, MimeTypeException {
        OsaamismerkkiKategoria kategoria;

        if (kategoriaDto.getId() == null) {
            kategoria = mapper.map(kategoriaDto, OsaamismerkkiKategoria.class);
        } else {
            kategoria = osaamismerkkiKategoriaRepository.findById(kategoriaDto.getId()).orElseThrow();
            kategoria.setNimi(mapper.map(kategoriaDto.getNimi(), TekstiPalanen.class));
            kategoria.setKuvaus(mapper.map(kategoriaDto.getKuvaus(), TekstiPalanen.class));
        }

        UUID liiteDbId = kategoria.getLiite() != null ? kategoria.getLiite().getId() : null;

        if (kategoriaDto.getLiite().getId() == null) {
            kategoria.setLiite(addLiite(kategoriaDto.getLiite()));
        }

        kategoria = osaamismerkkiKategoriaRepository.save(kategoria);
        deleteVanhaLiite(kategoria.getLiite().getId(), liiteDbId);
        return mapper.map(kategoria, OsaamismerkkiKategoriaDto.class);
    }

    @Override
    @Transactional
    public void deleteKategoria(Long id) {
        long linkitykset = osaamismerkkiRepository.countByKategoriaId(id);
        if (linkitykset > 0) {
            throw new BusinessRuleViolationException("osaamismerkkiin-liitettya-teemaa-ei-voi-poistaa");
        } else {
            OsaamismerkkiKategoria kategoria = osaamismerkkiKategoriaRepository.findById(id).orElseThrow();
            osaamismerkkiKategoriaRepository.deleteById(id);
            liiteRepository.deleteById(kategoria.getLiite().getId());
        }
    }

    private void deleteVanhaLiite(UUID uusiId, UUID vanhaId) {
        if (vanhaId != null && !uusiId.equals(vanhaId)) {
            liiteRepository.findById(vanhaId).ifPresent(liite -> liiteRepository.delete(liite));
        }
    }

    private Liite addLiite(OsaamismerkkiKategoriaLiiteDto liiteDto) throws HttpMediaTypeNotSupportedException, MimeTypeException {
        if (liiteDto != null) {
            Pair<UUID, String> filePair = uploadLiite(liiteDto);
            return liiteRepository.findById(filePair.getFirst()).orElse(null);
        }
        return null;
    }

    private Pair<UUID, String> uploadLiite(OsaamismerkkiKategoriaLiiteDto liite) throws HttpMediaTypeNotSupportedException, MimeTypeException {
        try {
            byte[] decoder = Base64.getDecoder().decode(liite.getBinarydata());
            InputStream is = new ByteArrayInputStream(decoder);
            return liiteTiedostoService.uploadFile(
                    null,
                    liite.getNimi(),
                    is,
                    decoder.length,
                    LiiteTyyppi.OSAAMISMERKKIKUVA,
                    IMAGE_TYPES,
                    null, null, null);
        } catch (IOException e) {
            throw new BusinessRuleViolationException("liitteen-lisaaminen-epaonnistui");
        }
    }

    private boolean hasRelevantKoodiDataChanged(OsaamismerkkiDto osaamismerkkiDto) {
        if (osaamismerkkiDto.getId() == null) {
            return false;
        }

        OsaamismerkkiDto osaamismerkkiDb = mapper.map(osaamismerkkiRepository.findById(osaamismerkkiDto.getId()).orElse(null), OsaamismerkkiDto.class);
        if (osaamismerkkiDb == null) {
            return false;
        }
        return !osaamismerkkiDto.getNimi().equals(osaamismerkkiDb.getNimi())
                || (osaamismerkkiDto.getVoimassaoloAlkaa() != null && !osaamismerkkiDto.getVoimassaoloAlkaa().equals(osaamismerkkiDb.getVoimassaoloAlkaa()))
                || (osaamismerkkiDto.getVoimassaoloLoppuu() != null && !osaamismerkkiDto.getVoimassaoloLoppuu().equals(osaamismerkkiDb.getVoimassaoloLoppuu()))
                || (osaamismerkkiDb.getVoimassaoloLoppuu() != null && !osaamismerkkiDb.getVoimassaoloLoppuu().equals(osaamismerkkiDto.getVoimassaoloLoppuu()));
    }

    private void updateKoodistoKoodi(OsaamismerkkiDto osaamismerkkiDto) {
        KoodistoKoodiDto koodistoKoodi = koodistoClient.get(KoodistoUriArvo.OSAAMISMERKIT, osaamismerkkiDto.getKoodiUri());
        KoodistoMetadataDto[] metadata = koodistoKoodi.getMetadata();

        for (KoodistoMetadataDto meta : metadata) {
            if (Kieli.SV.equals(Kieli.of(meta.getKieli()))) {
                meta.setNimi(osaamismerkkiDto.getNimi().get(Kieli.SV));
                meta.setKuvaus(osaamismerkkiDto.getKuvaus() != null ? osaamismerkkiDto.getKuvaus().get(Kieli.SV) : null);
            }
            else {
                meta.setNimi(osaamismerkkiDto.getNimi().get(Kieli.FI));
                meta.setKuvaus(osaamismerkkiDto.getKuvaus() != null ? osaamismerkkiDto.getKuvaus().get(Kieli.FI) : null);
            }
        }

        KoodistoKoodiDto koodi = new KoodistoKoodiDto();
        koodi.setVersio(koodistoKoodi.getVersio());
        koodi.setVersion(koodistoKoodi.getVersion());
        koodi.setKoodiUri(koodistoKoodi.getKoodiUri());
        koodi.setKoodiArvo(koodistoKoodi.getKoodiArvo());
        koodi.setMetadata(metadata);
        koodi.setVoimassaAlkuPvm(DateUtils.addHours(osaamismerkkiDto.getVoimassaoloAlkaa(), 12));
        koodi.setVoimassaLoppuPvm(osaamismerkkiDto.getVoimassaoloLoppuu() != null ? DateUtils.addHours(osaamismerkkiDto.getVoimassaoloLoppuu(), 12) : null);

        KoodistoKoodiDto updatedKoodi = koodistoClient.updateKoodi(koodi);
        if (updatedKoodi == null) {
            log.error("Koodin päivittäminen epäonnistui: {}", koodistoKoodi.getKoodiUri());
        }
    }
}
