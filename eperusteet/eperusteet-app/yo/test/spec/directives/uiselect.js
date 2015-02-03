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

describe('angular-ui-select', function() {

  // load the service's module
  beforeEach(module('eperusteApp'));

  var $templateCache;
  beforeEach(inject(function(_$templateCache_) {
      $templateCache = _$templateCache_;
  }));

  it('should have the IE9 template fix', function() {
    var fix = $templateCache.get("eperusteet/ui-select-choices-fix.html");
    expect($templateCache.get('bootstrap/choices.tpl.html')).toBe(fix);
  });

});

