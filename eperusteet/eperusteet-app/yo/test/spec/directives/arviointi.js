'use strict';

describe('Directive: arviointi', function () {

  var ASTEIKOT = _.indexBy([{"id" : 2,
    "osaamistasot" : [ {
      "id" : 2,
      "otsikko" : {
        "_id" : "48",
        "fi" : "Tyydyttävä T1",
        "sv" : "[Tyydyttävä T1]"
      }
    }]
  }], 'id');

  // load the directive's module
  beforeEach(module('eperusteApp',
                    'views/aloitussivu.html',
                    'views/partials/arviointi.html',
                    'views/partials/arvioinninTekstikentta.html', function ($provide) {
      $provide.value('YleinenData', {
        arviointiasteikot: ASTEIKOT,
        haeArviointiasteikot: angular.noop,
        vaihdaKieli: angular.noop
      });
    }));

  var element, scope, $compile, $http;


  beforeEach(inject(function ($rootScope, _$compile_, $httpBackend) {
    $http = $httpBackend;
    $httpBackend.when('GET', /localisation.+/).respond({});
    $httpBackend.when('GET', /eperusteet-service\/api.+/).respond({});
    scope = $rootScope.$new();
    $compile = _$compile_;
  }));

  function kaannaElementti() {
    element = angular.element('<arviointi arviointi="arviointi.arvioinninKohdealueet"></arviointi>');
    $compile(element)(scope);
    scope.$digest();
  };

  it('näyttää kohdealueet ja kriteerit', function () {
    scope.arviointi = {
      id: 2276,
      lisatiedot: null,
      arvioinninKohdealueet: [{
        otsikko: {fi: 'Työprosessin hallinta'},
        arvioinninKohteet: [{
          otsikko : {fi: 'Suunnittelu'},
          _arviointiAsteikko: '2',
          osaamistasonKriteerit: [{
            _osaamistaso: '2',
            kriteerit: [
              {fi: "kriteeri 1"},
              {fi: "kriteeri 2"},
              {fi: "kriteeri 3"}
            ]
          }]
        }]
      }]
    };
    $http.flush();
    kaannaElementti();
    var html = element.html();
    expect(html).toContain('Työprosessin hallinta');
    expect(html).toContain('Suunnittelu');
    expect(html).toContain('kriteeri 2');
  });
});
