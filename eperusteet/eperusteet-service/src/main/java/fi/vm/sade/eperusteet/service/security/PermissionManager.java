/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.security;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.authorization.PerusteprojektiPermissionRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;

import static fi.vm.sade.eperusteet.service.security.PermissionManager.Permission.*;
import fi.vm.sade.eperusteet.service.util.Pair;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
public class PermissionManager {

    @Autowired
    private PermissionHelper helper;

    @Autowired
    private PerusteprojektiRepository projektiRepository;

    @Autowired
    private PerusteprojektiPermissionRepository perusteProjektit;

    private static final Logger LOG = LoggerFactory.getLogger(PermissionManager.class);

    public enum Permission {

        LUKU("luku"),
        POISTO("poisto"),
        MUOKKAUS("muokkaus"),
        KOMMENTOINTI("kommentointi"),
        LUONTI("luonti"),
        TILANVAIHTO("tilanvaihto");

        private final String permission;

        private Permission(String permission) {
            this.permission = permission;
        }

        @Override
        public String toString() {
            return permission;
        }
    }

    public enum Target {

        PERUSTEPROJEKTI("perusteprojekti"),
        PERUSTE("peruste"),
        PERUSTEENMETATIEDOT("perusteenmetatiedot"),
        PERUSTEENOSA("perusteenosa");

        private final String target;

        private Target(String target) {
            this.target = target;
        }

        @Override
        public String toString() {
            return target;
        }
    }

    //TODO: oikeidet kovakodattua, oikeuksien tarkastaus.
    private static final Map<Target, Map<ProjektiTila, Map<Permission, Set<String>>>> allowedRoles;

    static {
        Map<Target, Map<ProjektiTila, Map<Permission, Set<String>>>> allowedRolesTmp = new EnumMap<>(Target.class);

        Set<String> r1 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_<oid>");
        Set<String> r2 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_<oid>", "ROLE_APP_EPERUSTEET_READ_UPDATE_<oid>");
        Set<String> r3 = Sets.newHashSet("ROLE_APP_EPERUSTEET_READ_<oid>", "ROLE_APP_EPERUSTEET_CRUD_<oid>", "ROLE_APP_EPERUSTEET_READ_UPDATE_<oid>");

        //perusteenosa, peruste (näiden osalta oletetaan että peruste tai sen osa on tilassa LUONNOS
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
            tmp.put(ProjektiTila.VIIMEISTELY, perm);
            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            tmp.put(ProjektiTila.VALMIS, perm);
            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            tmp.put(ProjektiTila.JULKAISTU, perm);
            tmp.put(ProjektiTila.POISTETTU, Collections.<Permission, Set<String>>emptyMap());
            allowedRolesTmp.put(Target.PERUSTE, tmp);
            allowedRolesTmp.put(Target.PERUSTEENOSA, tmp);
        }
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUONTI, Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD"));
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
            perm.put(TILANVAIHTO, r1);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.VIIMEISTELY, perm);
            perm.put(LUKU, r3);
            perm.put(TILANVAIHTO, r1);
            tmp.put(ProjektiTila.VALMIS, perm);
            tmp.put(ProjektiTila.POISTETTU, Collections.<Permission, Set<String>>emptyMap());
            allowedRolesTmp.put(Target.PERUSTEPROJEKTI, tmp);
        }
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
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.VIIMEISTELY, perm);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            tmp.put(ProjektiTila.VALMIS, perm);
            perm = Maps.newHashMap();
            perm.put(LUKU, Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD"));
            perm.put(MUOKKAUS, Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD"));
            tmp.put(null, perm);
            allowedRolesTmp.put(Target.PERUSTEENMETATIEDOT, tmp);
        }
        //XXX:debug
        LOG.debug("Oikeusmappaukset:");
        assert (allowedRolesTmp.keySet().containsAll(EnumSet.allOf(Target.class)));
        for (Map.Entry<Target, Map<ProjektiTila, Map<Permission, Set<String>>>> t : allowedRolesTmp.entrySet()) {
            for (Map.Entry<ProjektiTila, Map<Permission, Set<String>>> p : t.getValue().entrySet()) {
                for (Map.Entry<Permission, Set<String>> per : p.getValue().entrySet()) {
                    LOG.debug(t.getKey() + ":" + p.getKey() + ":" + per.getKey() + ":" + Arrays.toString(per.getValue().toArray()));
                }
            }
        }

        allowedRoles = Collections.unmodifiableMap(allowedRolesTmp);
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

    @PreAuthorize("isAuthenticated()")
    public boolean hasPermission(Authentication authentication, Serializable targetId, Target targetType, Permission permission) {
        LOG.warn(String.format("Checking permission %s to %s{id=%s} by %s", permission, targetType, targetId, authentication));

        if (!authentication.isAuthenticated()) {
            return false;
        }

        if (LUKU.equals(permission)) {
            //tarkistetaan onko lukuoikeus suoraan julkaistu -statuksen perusteella
            if (PerusteTila.VALMIS == helper.findPerusteTilaFor(targetType, targetId)) {
                return true;
            }
        }

        //tarkistataan ensin "any" oikeudet.
        boolean allowed = hasAnyRole(authentication, null, getAllowedRoles(targetType, null, permission));
        if (!allowed && targetId != null) {
            for (Pair<String, ProjektiTila> ppt : findPerusteProjektiTila(targetType, targetId)) {
                allowed = allowed | hasAnyRole(authentication, ppt.getFirst(), getAllowedRoles(targetType, ppt.getSecond(), permission));
            }
        }
        return allowed;
    }

    private boolean hasAnyRole(Authentication authentication, String perusteProjektiRyhmaOid, Collection<String> roles) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails user = (UserDetails) principal;
            for (String role : roles) {
                SimpleGrantedAuthority auth = new SimpleGrantedAuthority(perusteProjektiRyhmaOid == null ? role : role.replace("<oid>", perusteProjektiRyhmaOid));
                if (user.getAuthorities().contains(auth)) {
                    return true;
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
        Perusteprojekti projekti = projektiRepository.findOne(id);
        if (projekti == null) {
            throw new BusinessRuleViolationException("Perusteprojektia ei ole olemassa");
        }
        permissionMap.put(Target.PERUSTEPROJEKTI, getPermissions(authentication, projekti.getId(), Target.PERUSTEPROJEKTI, projekti.getTila()));

        Peruste peruste = projekti.getPeruste();
        if (peruste != null) {
            permissionMap.put(Target.PERUSTE, getPermissions(authentication, peruste.getId(), Target.PERUSTE, projekti.getTila()));
            permissionMap.put(Target.PERUSTEENMETATIEDOT, getPermissions(authentication, peruste.getId(), Target.PERUSTEENMETATIEDOT, projekti.getTila()));
        } else {
            throw new BusinessRuleViolationException("Perustetta ei ole olemassa");
        }

        return permissionMap;
    }

    /**
     *
     * @param authentication
     * @param targetId
     * @param targetType
     * @param tila
     * @return
     */
    // TODO: tila parametrin voisi varmaan karsia pois
    private Set<Permission> getPermissions(Authentication authentication, Serializable targetId, Target targetType, ProjektiTila tila) {
        LOG.warn(String.format("Getting permissions for %s{id=%s} in state %s by %s", targetType, targetId, tila.toString(), authentication));

        Set<Permission> permission = new HashSet<>();

        Map<ProjektiTila, Map<Permission, Set<String>>> tempTargetKohtaiset = allowedRoles.get(targetType);
        Map<Permission, Set<String>> tempProjektitilaKohtaiset = tempTargetKohtaiset.get(tila);

        for (Map.Entry<Permission, Set<String>> per : tempProjektitilaKohtaiset.entrySet()) {
            boolean hasRole = false;
            for (Pair<String, ProjektiTila> ppt : findPerusteProjektiTila(targetType, targetId)) {
                hasRole = hasRole | hasAnyRole(authentication, ppt.getFirst(), per.getValue());
            }
            if (hasRole) {
                permission.add(per.getKey());
            }
        }

        return permission;
    }

    private Set<Pair<String, ProjektiTila>> findPerusteProjektiTila(Target targetType, Serializable targetId) {

        if (!(targetId instanceof Long)) {
            throw new IllegalArgumentException("Expected Long");
        }
        final Long id = (Long) targetId;

        final Set<Pair<String, ProjektiTila>> empty = Collections.emptySet();
        switch (targetType) {

            case PERUSTEENMETATIEDOT:
            case PERUSTE: {
                return Sets.newHashSet(perusteProjektit.findByPeruste(id));
            }
            case PERUSTEPROJEKTI: {
                Perusteprojekti pp = perusteProjektit.findOne(id);
                return pp == null ? empty : Collections.singleton(Pair.of(pp.getRyhmaOid(), pp.getTila()));
            }
            case PERUSTEENOSA: {
                return Sets.newHashSet(perusteProjektit.findTilaByPerusteenOsaId(id));
            }
            default:
                throw new IllegalArgumentException(targetType.toString());
        }
    }
}
