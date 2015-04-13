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

/**
 * Esityspuolen moduuli. Yhteistä koodia eperusteet ja eperusteet-opintopolku
 * -projektien välillä.
 * Käyttää joko omia ep-alkuisia komponentteja tai yleisiä komponentteja, jotka täytyy
 * määritellä moduulin ulkopuolella.
 */
angular.module('eperusteet.esitys', [
]).provider('epEsitysSettings', function() {
  var settings = {
    'perusopetusState': 'root.perusopetus'
  };

  this.setValue = function(key, value) {
    settings[key] = value;
  };

  this.$get = function() {
    return settings;
  };
});
