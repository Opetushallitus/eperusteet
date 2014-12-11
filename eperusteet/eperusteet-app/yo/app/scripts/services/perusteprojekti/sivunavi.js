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
/* global _ */

angular.module('eperusteApp')
.service('PerusteProjektiSivunavi', function (PerusteprojektiTiedotService, $stateParams, $q,
    $state, $location, YleinenData, PerusopetusService, Kaanna, $timeout, Utils) {
  var STATE_OSAT = 'root.perusteprojekti.suoritustapa.tutkinnonosat';
  var STATE_TUTKINNON_OSA = 'root.perusteprojekti.suoritustapa.tutkinnonosa';
  var STATE_TEKSTIKAPPALE = 'root.perusteprojekti.suoritustapa.tekstikappale';
  var STATE_OSALISTAUS = 'root.perusteprojekti.osalistaus';
  var STATE_OSAALUE = 'root.perusteprojekti.osaalue';
  var isTutkinnonosatActive = function () {
    return $state.is(STATE_OSAT) || $state.is(STATE_TUTKINNON_OSA);
  };
  var AM_ITEMS = [
    {
      label: 'tutkinnonosat',
      link: [STATE_OSAT, {}],
      isActive: isTutkinnonosatActive,
      $type: 'ep-parts'
    }
  ];
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

  function getLink(lapsi) {
    if (!lapsi.perusteenOsa) {
      return '';
    }
    var params = {
      perusteenOsaViiteId: lapsi.id,
      versio: null
    };
    if (perusteenTyyppi === 'YL') {
      _.extend(params, {suoritustapa: 'ops'});
    }
    return lapsi.perusteenOsa.tunniste && lapsi.perusteenOsa.tunniste === 'rakenne' ?
      ['root.perusteprojekti.suoritustapa.muodostumissaannot', {versio: ''}] :
      [STATE_TEKSTIKAPPALE, params];
  }

  var processNode = function (node, level) {
    level = level || 0;
    _.each(node.lapset, function (lapsi) {
      var label = lapsi.perusteenOsa ? lapsi.perusteenOsa.nimi : '';
      items.push({
        label: label,
        id: lapsi.id,
        depth: level,
        link: getLink(lapsi),
        isActive: isRouteActive,
        $type: (lapsi.perusteenOsa && lapsi.perusteenOsa.tunniste === 'rakenne') ? 'ep-tree' : 'ep-text'
      });
      nameMap[lapsi.id] = label;
      processNode(lapsi, level + 1);
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

  var isYlRouteActive = function (item) {
    // ignore tabId
    var tablessUrl = $state.href(item.link[0],
      _.extend(_.clone(item.link[1]), {tabId: ''})).replace(/#/g, '');
    return $location.url().indexOf(tablessUrl) > -1;
  };

  function ylMapper(osa, key, level) {
    level = level || 0;
    items.push({
      depth: level,
      label: _.has(osa, 'nimi') ? osa.nimi : osa.perusteenOsa.nimi,
      link: [STATE_OSAALUE, {osanTyyppi: key, osanId: osa.id, tabId: 0}],
      isActive: isYlRouteActive
    });
    _(osa.oppimaarat).sortBy(Utils.nameSort).each(function (lapsi) {
      ylMapper(lapsi, key, level + 1);
    });
  }

  function mapYL(osat, key) {
    _(osat).sortBy(Utils.nameSort).each(function (osa) {
      ylMapper(osa, key, 1);
    });
  }

  var buildTree = function () {
    items = [];
    if (perusteenTyyppi === 'YL') {
      var tiedot = service.getYlTiedot();
      _.each(PerusopetusService.LABELS, function (key, label) {
        items.push({
          label: label,
          link: [STATE_OSALISTAUS, {osanTyyppi: key}]
        });
        mapYL(tiedot[key], key);
      });
    } else {
      items = _.clone(AM_ITEMS);
    }
    processNode(data.projekti.peruste.sisalto);
    $timeout(function () {
      callbacks.itemsChanged(items);
    });
  };

  var load = function () {
    data.projekti = service.getProjekti();
    data.projekti.peruste = service.getPeruste();
    data.projekti.peruste.sisalto = service.getSisalto();
    perusteenTyyppi = YleinenData.isPerusopetus(data.projekti.peruste) ? 'YL' : 'AM';
    callbacks.typeChanged(perusteenTyyppi);
    buildTree();
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
  };

  this.isVisible = function () {
    return _isVisible;
  };

  this.setCrumb = function (ids) {
    var crumbEl = angular.element('#tekstikappale-crumbs');
    ids.splice(0, 1);
    ids.reverse();
    var crumbs = _.map(ids, function (id) {
      return {name: nameMap[id], id: id};
    });
    var scope = crumbEl.scope();
    if (scope) {
      scope.setCrumbs(crumbs);
    }
  };
});
