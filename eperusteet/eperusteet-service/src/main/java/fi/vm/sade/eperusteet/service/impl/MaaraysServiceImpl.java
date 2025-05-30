package fi.vm.sade.eperusteet.service.impl;

import com.google.common.base.Throwables;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysAsiasanatFetch;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysKieliLiitteet;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiite;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiittyyTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysKieliLiitteetDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysLiiteDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysQueryDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.MaaraysAsiasanaRepository;
import fi.vm.sade.eperusteet.repository.MaaraysLiiteRepository;
import fi.vm.sade.eperusteet.repository.MaaraysRepository;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.MaaraysService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class MaaraysServiceImpl implements MaaraysService {

    @Autowired
    private MaaraysRepository maaraysRepository;

    @Autowired
    private MaaraysLiiteRepository maaraysLiiteRepository;

    @Autowired
    private MaaraysAsiasanaRepository maaraysAsiasanaRepository;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private EntityManager em;

    @Dto
    @Autowired
    private DtoMapper dtoMapper;

    private static final int BUFSIZE = 64 * 1024;

    @Override
    public Page<MaaraysDto> getMaaraykset(MaaraysQueryDto query) {
        if (!SecurityUtil.isAuthenticated()) {
            query.setJulkaistu(true);
            query.setLuonnos(false);
        }

        Pageable pageable = PageRequest.of(query.getSivu(), query.getSivukoko(),
                Sort.by(query.getJarjestys(), query.getJarjestysTapa()));
        return maaraysRepository.haeMaaraykset(
                query.getNimi(),
                query.getKieli(),
                query.getTyyppi(),
                query.getKoulutustyypit(),
                query.getTila(),
                query.isTuleva(),
                query.isVoimassa(),
                query.isPaattynyt(),
                pageable)
                .map(maarays -> dtoMapper.map(maarays[0], MaaraysDto.class));
    }

    @Override
    public List<String> getMaarayksienKoulutustyypit() {
        return maaraysRepository.findDistinctKoulutustyypit();
    }

    @Override
    public MaaraysDto getPerusteenMaarays(Long perusteId) {
        return dtoMapper.map(maaraysRepository.findFirstByPerusteIdAndLiittyyTyyppiOrderByLuotuAsc(perusteId, MaaraysLiittyyTyyppi.EI_LIITY), MaaraysDto.class);
    }

    @Override
    public List<MaaraysDto> getPerusteenMuutosmaaraykset(Long perusteId) {
        return dtoMapper.mapAsList(maaraysRepository.findByPerusteIdAndLiittyyTyyppiIn(perusteId, Arrays.asList(MaaraysLiittyyTyyppi.MUUTTAA, MaaraysLiittyyTyyppi.KORVAA)), MaaraysDto.class);
    }

    @Override
    public <T> List<T> getMaaraykset(Class<T> clazz) {
        return dtoMapper.mapAsList(maaraysRepository.findAll(), clazz);
    }

    @Override
    public MaaraysDto getMaarays(Long id) {
        Maarays maarays = maaraysRepository.findById(id).orElseThrow();
        MaaraysDto maaraysDto = dtoMapper.map(maaraysRepository.findById(id).orElse(null), MaaraysDto.class);

        if (SecurityUtil.isAuthenticated() && maarays.getMuokkaaja() != null) {
            maaraysDto.setMuokkaajaKayttaja(kayttajanTietoService.hae(maarays.getMuokkaaja()));
        }

        if (!SecurityUtil.isAuthenticated() && !maarays.getTila().equals(MaaraysTila.JULKAISTU)) {
            return null;
        }

        return maaraysDto;
    }

    @Override
    public List<MaaraysDto> getPerusteenJulkaistutMuutosmaaraykset(Long perusteId) {
        return dtoMapper.mapAsList(maaraysRepository.findByPerusteIdAndLiittyyTyyppiInAndTila(perusteId, Arrays.asList(MaaraysLiittyyTyyppi.MUUTTAA, MaaraysLiittyyTyyppi.KORVAA), MaaraysTila.JULKAISTU), MaaraysDto.class);
    }

    @Override
    @Cacheable("maarayskokoelma_asiasanat")
    public Map<Kieli, List<String>> getAsiasanat() {
        Map<Kieli, List<String>> asiasanat = new HashMap<>();
        List<MaaraysAsiasanatFetch> asiasanatList = maaraysAsiasanaRepository.findAllBy();

        asiasanatList.forEach(asiasanaFetch -> {
            asiasanaFetch.getAsiasanat().keySet().forEach(kieli -> {
                if (!asiasanat.containsKey(kieli)) {
                    asiasanat.put(kieli, new ArrayList<>());
                }
                asiasanat.get(kieli).addAll(asiasanaFetch.getAsiasanat().get(kieli).getAsiasana());
            });
        });

        return asiasanat;
    }

    @Override
    @CacheEvict(value="maarayskokoelma_asiasanat", allEntries = true)
    public MaaraysDto addMaarays(MaaraysDto maaraysDto) {
        addLiitteet(maaraysDto);
        maaraysDto.getAsiasanat().values().forEach(asiasana -> asiasana.setId(null));

        return dtoMapper.map(
                maaraysRepository.save(dtoMapper.map(maaraysDto, Maarays.class)),
                MaaraysDto.class);
    }

    @Override
    @CacheEvict(value="maarayskokoelma_asiasanat", allEntries = true)
    public MaaraysDto updateMaarays(MaaraysDto maaraysDto) {
        if (maaraysRepository.findById(maaraysDto.getId()).orElse(null) == null) {
            throw new BusinessRuleViolationException("maaraysta-ei-loydy");
        }

        updateLiitteet(maaraysDto);
        addLiitteet(maaraysDto);
        deleteLiitteet(maaraysDto);

        return dtoMapper.map(
                maaraysRepository.save(dtoMapper.map(maaraysDto, Maarays.class)),
                MaaraysDto.class);
    }

    private void updateLiitteet(MaaraysDto maaraysDto) {
        maaraysDto.getLiitteet().values().stream()
                .map(MaaraysKieliLiitteetDto::getLiitteet)
                .flatMap(Collection::stream)
                .filter(liite -> liite.getId() != null)
                .forEach(liite -> {
                    maaraysLiiteRepository.save(dtoMapper.map(liite, MaaraysLiite.class));
                });
    }

    private void addLiitteet(MaaraysDto maaraysDto) {
        maaraysDto.getLiitteet().values().stream()
                .map(MaaraysKieliLiitteetDto::getLiitteet)
                .flatMap(Collection::stream)
                .filter(liite -> liite.getId() == null && liite.getFileB64() != null)
                .forEach(liite -> {
                    liite.setId(uploadFile(liite));
                });
    }

    private void deleteLiitteet(MaaraysDto maaraysDto) {
        List<UUID> uudetLiitteet = maaraysDto.getLiitteet().values().stream()
                .map(MaaraysKieliLiitteetDto::getLiitteet)
                .flatMap(Collection::stream)
                .map(MaaraysLiiteDto::getId)
                .collect(Collectors.toList());

        Maarays maarays = maaraysRepository.findById(maaraysDto.getId()).orElseThrow();
        maaraysLiiteRepository.deleteAll(maarays.getLiitteet().values().stream()
                .map(MaaraysKieliLiitteet::getLiitteet)
                .flatMap(Collection::stream)
                .filter(liite -> !uudetLiitteet.contains(liite.getId()))
                .collect(Collectors.toList()));
    }

    @Override
    @CacheEvict(value="maarayskokoelma_asiasanat", allEntries = true)
    public void deleteMaarays(Long id, Long perusteId) {
        if (maaraysRepository.findById(id).orElse(null) == null) {
            throw new BusinessRuleViolationException("maaraysta-ei-loydy");
        }

        Maarays maarays = maaraysRepository.findById(id).orElseThrow();

        if (!ObjectUtils.isEmpty(julkaisutRepository.findByMuutosmaarays(maarays))) {
            throw new BusinessRuleViolationException("julkaisun-muutosmaaraysta-ei-voi-poistaa");
        }

        maaraysLiiteRepository.deleteAll(maarays.getLiitteet().values().stream()
                .map(MaaraysKieliLiitteet::getLiitteet)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        maaraysRepository.deleteById(id);
    }

    @Override
    public UUID uploadFile(MaaraysLiiteDto maaraysLiiteUploadDto) {
        try {
            byte[] decoder = Base64.getDecoder().decode(maaraysLiiteUploadDto.getFileB64());

            MaaraysLiite liite = new MaaraysLiite(
                    UUID.randomUUID(),
                    dtoMapper.map(maaraysLiiteUploadDto.getNimi(), TekstiPalanen.class),
                    maaraysLiiteUploadDto.getTiedostonimi(),
                    maaraysLiiteUploadDto.getTyyppi(),
                    BlobProxy.generateProxy(decoder));
            em.persist(liite);
            em.flush();

            return liite.getId();
        } catch(Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new BusinessRuleViolationException("liitteen-tallennus-epaonnistui");
        }
    }

    @Override
    public MaaraysLiiteDto getLiite(UUID uuid) {
        return dtoMapper.map(maaraysLiiteRepository.findById(uuid).orElse(null), MaaraysLiiteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportLiite(UUID id, OutputStream os) throws SQLException, IOException {
        MaaraysLiite liite = maaraysLiiteRepository.findById(id).orElseThrow();
        try (InputStream is = liite.getData().getBinaryStream()) {
            IOUtils.copy(is, os);
        }
    }
}
