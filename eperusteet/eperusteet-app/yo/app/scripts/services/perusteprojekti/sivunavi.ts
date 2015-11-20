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

'use strict';

/// <reference path="../../../ts_packages/tsd.d.ts" />

angular.module('eperusteApp')
.service('PerusteProjektiSivunavi', function (PerusteprojektiTiedotService, $stateParams, $q, $log,
        $state, $location, YleinenData, PerusopetusService, LukiokoulutusService, Kaanna, $timeout, Utils,
        LukioKurssiService) {
  var STATE_OSAT = 'root.perusteprojekti.suoritustapa.tutkinnonosat';
  var STATE_TUTKINNON_OSA = 'root.perusteprojekti.suoritustapa.tutkinnonosa';
  var STATE_TEKSTIKAPPALE = 'root.perusteprojekti.suoritustapa.tekstikappale';
  var STATE_OSALISTAUS = 'root.perusteprojekti.suoritustapa.osalistaus';
  var STATE_LUKIOOSALISTAUS = 'root.perusteprojekti.suoritustapa.lukioosat';
  var STATE_OSAALUE = 'root.perusteprojekti.suoritustapa.osaalue';

  var STATE_LUKIOOSAALUE = 'root.perusteprojekti.suoritustapa.lukioosaalue',
      STATE_LUKIOKURSSI = 'root.perusteprojekti.suoritustapa.kurssi';
  var isTutkinnonosatActive = function () {
    return $state.is(STATE_OSAT) || $state.is(STATE_TUTKINNON_OSA);
  };

  function getAMItems(isVaTe, vateConverter) {
    return [{
      label: vateConverter('tutkinnonosat'),
      link: [STATE_OSAT_ALKU + (isVaTe ? 'koulutuksenosat' : 'tutkinnonosat'), {}],
      isActive: isTutkinnonosatActive,
        $type: 'ep-parts'
    }];
  }

  var service = null;
  var _isVisible = false;
  var items = [];
  var nameMap = {};
  var perusteenTyyppi = 'AM';
  var data = {
    projekti: {
      peruste: {
        sisalto: {
        }
      }
    }
  };
  var callbacks = {
    itemsChanged: angular.noop,
    typeChanged: angular.noop
  };
  var kurssit = null;
  var specialPerusteenOsaParts = {};

  function getLink(lapsi): any {
    if (!lapsi.perusteenOsa) {
      return '';
    }
    var params = {
      perusteenOsaViiteId: lapsi.id,
      versio: null
    };

    if (lapsi.perusteenOsa.tunniste === 'rakenne') {
       return ['root.perusteprojekti.suoritustapa.muodostumissaannot', {versio: ''}];
    }
    else if (lapsi.perusteenOsa.tunniste === 'laajaalainenosaaminen') {
       return ['root.perusteprojekti.suoritustapa.osalistaus', {
         suoritustapa: 'perusopetus',
         osanTyyppi: 'osaaminen'
       }];
    }
    else {
      return [STATE_TEKSTIKAPPALE, params];
    }
  }

  var processNode = function (node, level = 0, parent?) {
    _.each(node.lapset, function (lapsi) {
      var label = lapsi.perusteenOsa ? lapsi.perusteenOsa.nimi : '';
      var link = null,
          special = null,
          isActive = null;
      if (lapsi.perusteenOsa && lapsi.perusteenOsa.osanTyyppi
                && specialPerusteenOsaParts[lapsi.perusteenOsa.osanTyyppi]) {
        special = specialPerusteenOsaParts[lapsi.perusteenOsa.osanTyyppi];
        link = special.link;
        isActive = special.isActive;
      }
      items.push({
        label: label,
        id: lapsi.id,
        depth: level,
        link: link || getLink(lapsi),
        isActive: isActive || isRouteActive,
        $$parentItem: parent,
        $type: (lapsi.perusteenOsa && lapsi.perusteenOsa.tunniste === 'rakenne') ? 'ep-tree' : 'ep-text'
      });
      nameMap[lapsi.id] = label;
      if (special) {
        _.each(special.lapset, function(mappedChild) {
          mappedChild.depth += level;
          items.push(mappedChild);
          nameMap[mappedChild.id] = mappedChild.perusteenOsa ? mappedChild.perusteenOsa.nimi : '';
        });
      }
      processNode(lapsi, level + 1, node);
    });
  };

  var isRouteActive = function (item) {
    // ui-sref-active doesn't work directly in ui-router 0.2.*
    // with optional parameters.
    // Versionless url should be considered same as specific version url.
    var url = item.href && item.href.indexOf('/rakenne') > -1 ?
      item.href.substr(1) : $state.href(STATE_TEKSTIKAPPALE, {
      perusteenOsaViiteId: item.id,
      versio: null
    }, {inherit:true}).replace(/#/g, '');
    return $location.url().indexOf(url) > -1;
  };

  var normalize = function(str) {
    if (!str) {
      return str;
    }
    return str.replace(/\/0\//g, "//");
  };

  var isYlRouteActive = function (item) {
    // ignore tabId
    var tablessUrl = $state.href(item.link[0],
    _.extend(_.clone(item.link[1]), {tabId: ''})).replace(/#/g, '');
    return normalize($location.url()).indexOf(normalize(tablessUrl)) > -1;
  };

  function ylMapper(targetItems, osa, key, level, link?, parent?) {
    level = level || 0;
    targetItems.push({
      depth: level,
      label: _.has(osa, 'nimi') ? osa.nimi : osa.perusteenOsa.nimi,
      link: link || [perusteenTyyppi == 'LU' ? STATE_LUKIOOSAALUE : STATE_OSAALUE, {osanTyyppi: key, osanId: osa.id, tabId: 0}],
      isActive: isYlRouteActive,
      $$parentItem: parent
    });
    _(osa.oppimaarat).sortBy('jnro').each(function (lapsi) {
      ylMapper(targetItems, lapsi, key, level + 1, null, osa);
    }).value();
    if (kurssit && perusteenTyyppi === 'LU' && key === 'oppiaineet_oppimaarat') {
      var foundKurssit = LukioKurssiService.filterOrderedKurssisByOppiaine(kurssit, function (oa) {
        return oa.oppiaineId === osa.id;
      });
      _.each(foundKurssit, function(filteredKurssi) {
        ylMapper(targetItems, filteredKurssi, key, level + 1, [STATE_LUKIOKURSSI, {kurssiId: filteredKurssi.id}], osa);
      });
    }
  }

  function mapYL(target, osat, key, parent) {
    _(osat).sortBy((key === 'oppiaineet' || key === 'oppiaineet-oppimaarat'
        || key === 'oppiaineet_oppimaarat'
        || key === 'aihekokonaisuudet') ? 'jnro' : Utils.nameSort).each(function (osa) {
      ylMapper(target, osa, key, 1, null, parent);
    }).value();
  }
  function lukioOsanTyyppi(key) {
    switch (key) {
      case LukiokoulutusService.OPPIAINEET_OPPIMAARAT: return 'lukioopetussuunnitelmarakenne';
      case LukiokoulutusService.AIHEKOKONAISUUDET: return 'aihekokonaisuudet';
      case LukiokoulutusService.OPETUKSEN_YLEISET_TAVOITTEET: return 'opetuksenyleisettavoitteet';
      default: return null;
    }
  }

  function buildTree(isVaTe, vateConverter) {
    items = [];
    switch (perusteenTyyppi) {
      case 'YL': {
        var tiedot1 = service.getYlTiedot();
        _.each(PerusopetusService.LABELS, function (key, label) {
          var item = {
            label: label,
            link: [STATE_OSALISTAUS, {suoritustapa: 'perusopetus', osanTyyppi: key}]
          };
          items.push(item);
          mapYL(items, tiedot1[key], key, item);
        });
        break;
      }
      case 'LU': {
        var lukioTiedot = service.getYlTiedot();
        kurssit = lukioTiedot.kurssit;
        _.each(LukiokoulutusService.LABELS, function (k) {
          var itemList = [],
              key = lukioOsanTyyppi(k),
              item = {
                lapset: itemList,
                link: [STATE_LUKIOOSALISTAUS, {suoritustapa: 'lukiokoulutus', osanTyyppi: k}],
                isActive: function(node) {
                  var url = $location.url(),
                      href = $state.href(node.link[0], node.link[1]).replace('#/', '/');
                  return url === href;
                }
              };
          specialPerusteenOsaParts[key] = item;
          mapYL(itemList, lukioTiedot[k], k, item);
        });
        break;
      }
      case 'AM':
        items = _.clone(AM_ITEMS);
        break;
      default:break;
    }
    processNode(data.projekti.peruste.sisalto);
    $timeout(function () {
      callbacks.itemsChanged(items);
    });
  }

  var load = function () {
    data.projekti = service.getProjekti();
    data.projekti.peruste = service.getPeruste();
    data.projekti.peruste.sisalto = service.getSisalto();
    perusteenTyyppi = YleinenData.isPerusopetus(data.projekti.peruste) ? 'YL'
            : YleinenData.isLukiokoulutus(data.projekti.peruste) ? 'LU'
            : YleinenData.isSimple(data.projekti.peruste) ? 'ESI'
            : 'AM';
    callbacks.typeChanged(perusteenTyyppi);
    var constIsVaTe = YleinenData.isValmaTelma(data.projekti.peruste);
    buildTree(constIsVaTe, Kielimapper.mapTutkinnonosatKoulutuksenosat(constIsVaTe));
  };

  this.register = function (key, cb) {
    callbacks[key] = cb;
  };

  this.refresh = function (light) {
    if (!service) {
      PerusteprojektiTiedotService.then(function(res) {
        service = res;
        load();
      });
    } else {
      if (light) {
        load();
      } else {
        service.alustaPerusteenSisalto($stateParams, true).then(function () {
          load();
        });
      }
    }
  };

  this.setVisible = function (visible) {
    _isVisible = _.isUndefined(visible) ? true : visible;
    if (!_isVisible) {
      PerusopetusService.clearCache();
    }
  };

  this.isVisible = function () {
    return _isVisible;
  };
});
