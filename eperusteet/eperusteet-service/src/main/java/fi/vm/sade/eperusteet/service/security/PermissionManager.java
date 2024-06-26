package fi.vm.sade.eperusteet.service.security;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.authorization.PerusteprojektiPermissionRepository;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.util.Pair;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;

import static fi.vm.sade.eperusteet.service.security.PermissionManager.Permission.*;

@Service
public class PermissionManager {

    @Autowired
    private PermissionHelper helper;

    @Autowired
    private PerusteprojektiRepository projektiRepository;

    @Autowired
    private PerusteprojektiPermissionRepository perusteProjektit;

    @Autowired
    TutkinnonOsaViiteRepository viiteRepository;

    @Autowired
    PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private Environment env;

    private static final Logger LOG = LoggerFactory.getLogger(PermissionManager.class);
    private static final String OPH_ADMIN = "ROLE_APP_EPERUSTEET_ADMIN_1.2.246.562.10.00000000001";

    public enum Permission {

        LUKU("luku"),
        POISTO("poisto"),
        MUOKKAUS("muokkaus"),
        KOMMENTOINTI("kommentointi"),
        LUONTI("luonti"),
        KORJAUS("korjaus"),
        TILANVAIHTO("tilanvaihto");

        private final String permission;

        Permission(String permission) {
            this.permission = permission;
        }

        @Override
        public String toString() {
            return permission;
        }
    }

    public enum Target {

        ARVIOINTIASTEIKKO("arviointiasteikko"),
        PERUSTEPROJEKTI("perusteprojekti"),
        PERUSTE("peruste"),
        POHJA("pohja"),
        PERUSTEENMETATIEDOT("perusteenmetatiedot"),
        PERUSTEENOSA("perusteenosa"),
        TUTKINNONOSAVIITE("tutkinnonosaviite"),
        PERUSTEENOSAVIITE("perusteenosaviite"),
        TIEDOTE("tiedote"),
        ORGANISAATIO("organisaatio"),
        OSAAMISMERKIT("osaamismerkit"),
        MAARAYS("maarays");

        private final String target;

        Target(String target) {
            this.target = target;
        }

        @Override
        public String toString() {
            return target;
        }
    }

    //TODO: oikeudet kovakoodattua, oikeuksien tarkastaus.
    private static final Map<Target, Map<ProjektiTila, Map<Permission, Set<String>>>> allowedRoles;

    static {
        Map<Target, Map<ProjektiTila, Map<Permission, Set<String>>>> allowedRolesTmp = new EnumMap<>(Target.class);

        Set<String> r0 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001");
        Set<String> r0Admin = Sets.newHashSet(OPH_ADMIN);
        Set<String> r1 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001",
                "ROLE_APP_EPERUSTEET_CRUD_<oid>");
        Set<String> r2 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001",
                "ROLE_APP_EPERUSTEET_CRUD_<oid>", "ROLE_APP_EPERUSTEET_READ_UPDATE_<oid>");
        Set<String> r3 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001",
                "ROLE_APP_EPERUSTEET_CRUD_<oid>", "ROLE_APP_EPERUSTEET_READ_UPDATE_<oid>",
                "ROLE_APP_EPERUSTEET_READ_<oid>");
        Set<String> r4 = Sets.newHashSet("ROLE_VIRKAILIJA", "ROLE_APP_EPERUSTEET");
        Set<String> r5 = Sets.newHashSet("ROLE_VIRKAILIJA", "ROLE_APP_EPERUSTEET", "ROLE_ANONYMOUS");
        Set<String> maaraysRead = Sets.newHashSet("ROLE_APP_EPERUSTEET_MAARAYS_READ", "ROLE_APP_EPERUSTEET_MAARAYS_CRUD");
        Set<String> maaraysCRUD = Sets.newHashSet("ROLE_APP_EPERUSTEET_MAARAYS_CRUD");

        // Peruste
        {
            EnumMap<ProjektiTila, Map<Permission, Set<String>>> tmp = new EnumMap<>(ProjektiTila.class);
            Map<Permission, Set<String>> perm;

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r2);
            perm.put(POISTO, r2);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.LAADINTA, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.KOMMENTOINTI, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(KOMMENTOINTI, r2);
            perm.put(MUOKKAUS, r1);
            tmp.put(ProjektiTila.VIIMEISTELY, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(TILANVAIHTO, r1);
            tmp.put(ProjektiTila.VALMIS, perm);

            perm = Maps.newHashMap();
            perm.put(KORJAUS, r1);
            perm.put(MUOKKAUS, r1);
            perm.put(LUKU, r5);
            tmp.put(ProjektiTila.JULKAISTU, perm);

            perm = Maps.newHashMap();
            perm.put(TILANVAIHTO, r1);
            perm.put(LUKU, r1);
            tmp.put(ProjektiTila.POISTETTU, perm);

            allowedRolesTmp.put(Target.PERUSTE, tmp);
        }
        // Perusteprojekti
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();

            perm.put(LUONTI, Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001"));
            perm.put(LUKU, Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001"));
            tmp.put(null, perm);

            perm = Maps.newHashMap();
            perm.put(TILANVAIHTO, r1);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r2);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.LAADINTA, perm);

            perm = Maps.newHashMap();
            perm.put(TILANVAIHTO, r1);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.KOMMENTOINTI, perm);

            perm = Maps.newHashMap();
            perm.put(TILANVAIHTO, r1);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.VIIMEISTELY, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(TILANVAIHTO, r1);
            perm.put(MUOKKAUS, r1);
            tmp.put(ProjektiTila.VALMIS, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(TILANVAIHTO, r0Admin);
            perm.put(MUOKKAUS, r1);
            perm.put(KORJAUS, r1);
            tmp.put(ProjektiTila.JULKAISTU, perm);

            perm = Maps.newHashMap();
            perm.put(TILANVAIHTO, r1);
            perm.put(LUKU, r1);
            tmp.put(ProjektiTila.POISTETTU, perm);

            allowedRolesTmp.put(Target.PERUSTEPROJEKTI, tmp);
        }
        // Perusteenosa, tutkinnon osa viite, perusteen osa viite
        // EP-1174
        {
            EnumMap<ProjektiTila, Map<Permission, Set<String>>> tmp = new EnumMap<>(ProjektiTila.class);
            Map<Permission, Set<String>> perm;

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r2);
            perm.put(POISTO, r2);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.LAADINTA, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.KOMMENTOINTI, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(KOMMENTOINTI, r2);
            perm.put(MUOKKAUS, r1);
            tmp.put(ProjektiTila.VIIMEISTELY, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(KORJAUS, r1);
            perm.put(TILANVAIHTO, r1);
            tmp.put(ProjektiTila.VALMIS, perm);

            perm = Maps.newHashMap();
            perm.put(KORJAUS, r1);
            perm.put(LUKU, r5);
            tmp.put(ProjektiTila.JULKAISTU, perm);

            perm = Maps.newHashMap();
            perm.put(KORJAUS, r1);
            perm.put(TILANVAIHTO, r1);
            tmp.put(ProjektiTila.POISTETTU, perm);

            allowedRolesTmp.put(Target.PERUSTEENOSA, tmp);
            allowedRolesTmp.put(Target.TUTKINNONOSAVIITE, tmp);
            allowedRolesTmp.put(Target.PERUSTEENOSAVIITE, tmp);
        }

        // Pohja
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUONTI, r0Admin);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r0Admin);
            perm.put(POISTO, r0Admin);
            tmp.put(null, perm);
            allowedRolesTmp.put(Target.POHJA, tmp);
        }
        // Perusteen metatiedot
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r2);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.LAADINTA, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.KOMMENTOINTI, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.VIIMEISTELY, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            tmp.put(ProjektiTila.VALMIS, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r5);
            perm.put(MUOKKAUS, r1);
            tmp.put(ProjektiTila.JULKAISTU, perm);
            allowedRolesTmp.put(Target.PERUSTEENMETATIEDOT, tmp);
        }
        // Tiedote
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUONTI, r0);
            perm.put(LUKU, r4);
            perm.put(MUOKKAUS, r0);
            perm.put(POISTO, r0);
            tmp.put(null, perm);
            allowedRolesTmp.put(Target.TIEDOTE, tmp);
        }
        // Arviointiasteikko
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUONTI, r0);
            perm.put(LUKU, r5);
            perm.put(MUOKKAUS, r0);
            perm.put(POISTO, r0);
            tmp.put(null, perm);
            allowedRolesTmp.put(Target.ARVIOINTIASTEIKKO, tmp);
        }

        // Organisaatio
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUONTI, r0);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r0);
            perm.put(POISTO, r0);
            tmp.put(null, perm);
            allowedRolesTmp.put(Target.ORGANISAATIO, tmp);
        }

        // Osaamismerkki
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUONTI, r0);
            perm.put(LUKU, r0);
            perm.put(MUOKKAUS, r0);
            perm.put(POISTO, r0);
            tmp.put(null, perm);
            allowedRolesTmp.put(Target.OSAAMISMERKIT, tmp);
        }

        // MAARAYS
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUONTI, maaraysCRUD);
            perm.put(LUKU, maaraysRead);
            perm.put(MUOKKAUS, maaraysCRUD);
            perm.put(POISTO, maaraysCRUD);
            tmp.put(null, perm);
            allowedRolesTmp.put(Target.MAARAYS, tmp);
        }

        if (LOG.isTraceEnabled()) {
            assert (allowedRolesTmp.keySet().containsAll(EnumSet.allOf(Target.class)));
            for (Map.Entry<Target, Map<ProjektiTila, Map<Permission, Set<String>>>> t : allowedRolesTmp.entrySet()) {
                for (Map.Entry<ProjektiTila, Map<Permission, Set<String>>> p : t.getValue().entrySet()) {
                    for (Map.Entry<Permission, Set<String>> per : p.getValue().entrySet()) {
                        LOG.trace(t.getKey() + ":" + p.getKey() + ":" + per.getKey() + ":" + Arrays.toString(per.getValue().toArray()));
                    }
                }
            }
        }

        allowedRoles = Collections.unmodifiableMap(allowedRolesTmp);
    }

    private static Set<String> getAllowedRoles(Target target, Permission permission) {
        return getAllowedRoles(target, null, permission);
    }

    private static Set<String> getAllowedRoles(Target target, ProjektiTila tila, Permission permission) {
        Map<Permission, Set<String>> t = allowedRoles.get(target).get(tila);
        if (t != null) {
            Set<String> r = t.get(permission);
            if (r != null) {
                return r;
            }
        }
        return Collections.emptySet();
    }

    @Transactional(readOnly = true)
    public boolean hasPerustePermission(Authentication authentication, Long perusteId, Permission permission) {
        return hasPermission(authentication, perusteId, Target.PERUSTE, permission);
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Authentication authentication, Serializable targetId, Target targetType, Permission permission) {

        if (Arrays.stream(env.getActiveProfiles()).anyMatch(profile -> profile.equals("developmentPermissionOverride"))) {
            return true;
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("Checking permission %s to %s{id=%s} by %s", permission, targetType, targetId, authentication));
        }

        if (Target.TIEDOTE.equals(targetType) || Target.ARVIOINTIASTEIKKO.equals(targetType) || Target.MAARAYS.equals(targetType)) {
            return hasAnyRole(authentication, getAllowedRoles(targetType, permission));
        }

        if (Target.ORGANISAATIO.equals(targetType)) {
            return hasAnyRole(authentication, String.valueOf(targetId), getAllowedRoles(targetType, permission));
        }

        if (Target.TUTKINNONOSAVIITE.equals(targetType)) {
            // Haetaan perusteen osa mihin viitataan osaviitteessä ja jatketaan luvan tutkimista perusteen osan tiedoilla.
            TutkinnonOsaViite t = viiteRepository.findOne((Long) targetId);
            if (t == null || t.getTutkinnonOsa() == null) {
                throw new NotExistsException("Tutkinnon osan viitettä ei löytynyt");
            }
            targetId = t.getTutkinnonOsa().getId();
            targetType = Target.PERUSTEENOSA;
        }

        if (Target.PERUSTEENOSAVIITE.equals(targetType)) {
            // Haetaan perusteen osa mihin viitataan osaviitteessä ja jatketaan luvan tutkimista perusteen osan tiedoilla.
            PerusteenOsaViite p = perusteenOsaViiteRepository.findOne((Long) targetId);
            if (p == null || p.getPerusteenOsa() == null) {
                throw new NotExistsException("Perusteen osan viitettä ei löytynyt");
            }
            targetId = p.getPerusteenOsa().getId();
            targetType = Target.PERUSTEENOSA;
        }

        if (Target.POHJA.equals(targetType)) {
            return hasAnyRole(authentication, getAllowedRoles(targetType, permission));
        }

        if (targetId != null && (Target.PERUSTEPROJEKTI.equals(targetType) || Target.PERUSTE.equals(targetType)) && !LUKU.equals(permission)) {
            Peruste peruste;
            if (Target.PERUSTE.equals(targetType)) {
                peruste = perusteet.findOne((Long) targetId);
            } else {
                peruste = projektiRepository.findById((Long) targetId).map(Perusteprojekti::getPeruste).orElse(null);
            }

            if (peruste == null) {
                return false;
            }

            if (peruste.getTyyppi().equals(PerusteTyyppi.OPAS)) {
                return hasAnyRole(authentication, getAllowedRoles(targetType, ProjektiTila.LAADINTA, permission));
            }

            if (peruste.getTyyppi().equals(PerusteTyyppi.POHJA)) {
                return isUserAdmin();
            }
        }

        if (LUKU.equals(permission)) {
            PerusteTila tila = helper.findPerusteTilaFor(targetType, targetId);
            //tarkistetaan onko lukuoikeus suoraan julkaistu -statuksen perusteella
            if (PerusteTila.VALMIS == tila) {
                return true;
            }
            // Esikatselu (vain luonnoksille)
            else if (PerusteTila.LUONNOS == tila) {
                if (Target.PERUSTEPROJEKTI.equals(targetType)) {
                    List<Pair<String, Boolean>> loydetyt = perusteProjektit.findEsikatseltavissaById((Long) targetId);
                    if (!loydetyt.isEmpty() && loydetyt.get(0).getSecond()) {
                        return true;
                    }
                }
                if (Target.PERUSTE.equals(targetType)) {
                    List<Pair<String, Boolean>> loydetyt = perusteProjektit.findEsikatseltavissaByPeruste((Long) targetId);
                    if (!loydetyt.isEmpty() && loydetyt.get(0).getSecond()) {
                        return true;
                    }
                }
                if (Target.PERUSTEENOSA.equals(targetType)) {
                    List<Pair<String, Boolean>> loydetyt = perusteProjektit.findEsikatseltavissaByPerusteenOsaId((Long) targetId);
                    if (!loydetyt.isEmpty() && loydetyt.get(0).getSecond()) {
                        return true;
                    }
                }
            }
        }

        if (targetId == null) {
            return hasAnyRole(authentication, getAllowedRoles(targetType, permission));
        } else {
            boolean allowed = false;
            for (Pair<String, ProjektiTila> ppt : findPerusteProjektiTila(targetType, targetId)) {
                allowed = allowed | hasAnyRole(authentication, ppt.getFirst(),
                        getAllowedRoles(targetType, ppt.getSecond(), permission));
            }
            return allowed;
        }
    }

    private boolean hasAnyRole(Authentication authentication, Collection<String> roles) {
        return hasAnyRole(authentication, null, roles);
    }

    private boolean hasAnyRole(Authentication authentication, String perusteProjektiRyhmaOid, Collection<String> roles) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (String role : roles) {
                String accurateRole = perusteProjektiRyhmaOid == null
                        ? role
                        : role.replace("<oid>", perusteProjektiRyhmaOid);
                for (GrantedAuthority authority : authentication.getAuthorities()) {
                    if (authority.getAuthority() != null && authority.getAuthority().equals(accurateRole)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Map<Target, Set<Permission>> getProjectPermissions(Long id) {

        Map<Target, Set<Permission>> permissionMap = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Perusteprojekti projekti = projektiRepository.findById(id).orElse(null);
        if (projekti == null) {
            throw new NotExistsException("Perusteprojektia ei ole olemassa");
        }
        permissionMap.put(Target.PERUSTEPROJEKTI, getPermissions(authentication, projekti.getId(), Target.PERUSTEPROJEKTI, projekti.getTila()));

        Peruste peruste = projekti.getPeruste();
        if (peruste != null) {
            permissionMap.put(Target.PERUSTE, getPermissions(authentication, peruste.getId(), Target.PERUSTE, projekti.getTila()));
            permissionMap.put(Target.PERUSTEENMETATIEDOT, getPermissions(authentication, peruste.getId(), Target.PERUSTEENMETATIEDOT, projekti.getTila()));
        } else {
            throw new NotExistsException("Perustetta ei ole olemassa");
        }

        if (projekti.getPeruste().getTyyppi().equals(PerusteTyyppi.POHJA) && !isUserAdmin()) {
            removeCRUD(permissionMap, Target.PERUSTEPROJEKTI);
            removeCRUD(permissionMap, Target.PERUSTE);
            removeCRUD(permissionMap, Target.PERUSTEENMETATIEDOT);
        }

        return permissionMap;
    }

    public boolean isUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority -> authority.equals(OPH_ADMIN));
    }

    private Map<Target, Set<Permission>> removeCRUD(Map<Target, Set<Permission>> permissionMap, Target target) {
        if (permissionMap.containsKey(target)) {
            Set<Permission> current = permissionMap.get(target);
            current.removeAll(Sets.newHashSet(POISTO, KORJAUS, LUONTI, MUOKKAUS, TILANVAIHTO));
            permissionMap.put(target, current);
        }

        return permissionMap;
    }

    /**
     * @param authentication authentication
     * @param targetId       targetId
     * @param targetType     targetType
     * @param tila           tila
     * @return oikeussetti
     */
    // TODO: tila parametrin voisi varmaan karsia pois
    private Set<Permission> getPermissions(Authentication authentication, Serializable targetId, Target targetType, ProjektiTila tila) {
        Set<Permission> permission = new HashSet<>();

        Map<ProjektiTila, Map<Permission, Set<String>>> tempTargetKohtaiset = allowedRoles.get(targetType);
        Map<Permission, Set<String>> tempProjektitilaKohtaiset = tempTargetKohtaiset.get(tila);
        final Set<Pair<String, ProjektiTila>> projektitila = findPerusteProjektiTila(targetType, targetId);
        if (tempProjektitilaKohtaiset != null) {
            for (Map.Entry<Permission, Set<String>> per : tempProjektitilaKohtaiset.entrySet()) {
                boolean hasRole = false;
                for (Pair<String, ProjektiTila> ppt : projektitila) {
                    hasRole = hasRole | hasAnyRole(authentication, ppt.getFirst(), per.getValue());
                }
                if (hasRole) {
                    permission.add(per.getKey());
                }
            }
        }

        return permission;
    }

    private Set<Pair<String, ProjektiTila>> findPerusteProjektiTila(Target targetType, Serializable targetId) {

        if (!(targetId instanceof Long)) {
            throw new IllegalArgumentException("Expected Long");
        }
        final Long id = (Long) targetId;

        switch (targetType) {
            case PERUSTEENMETATIEDOT:
            case PERUSTE: {
                return setOf(perusteProjektit.findByPeruste(id));
            }
            case PERUSTEPROJEKTI: {
                return setOf(perusteProjektit.findByPairId(id));
            }
            case PERUSTEENOSA: {
                return setOf(perusteProjektit.findTilaByPerusteenOsaId(id));
            }
            default:
                throw new IllegalArgumentException(targetType.toString());
        }
    }

    private static <T> Set<T> setOf(Collection<T> c) {
        if (c == null) {
            throw new NotExistsException();
        }
        if (c.size() == 1) {
            return Collections.singleton(c.iterator().next());
        }
        return new HashSet<>(c);
    }

    public Set<String> kayttajanOrganisaatiot() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .filter(Objects::nonNull)
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(x -> x.split("_"))
                .filter(x -> x.length > 0)
                .map(x -> x[x.length - 1])
                .collect(Collectors.toSet());
    }
}
