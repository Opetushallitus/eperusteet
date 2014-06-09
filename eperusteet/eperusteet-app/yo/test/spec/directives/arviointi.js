'use strict';

// TODO disabloitu, päivitä testit
xdescribe('Directive: arviointi', function () {

  // load the directive's module
  beforeEach(module('eperusteApp',
                    'views/partials/arviointi.html',
                    'views/partials/arvioinninTekstikentta.html'));

  var element, scope, $compile;

  beforeEach(inject(function ($rootScope, _$compile_, $httpBackend) {
    $httpBackend.when('GET', /localisation.+/).respond({});
    $httpBackend.when('GET', /eperusteet-service\/api.+/).respond({});
    scope = $rootScope.$new();
    $compile = _$compile_;
  }));

  function kaannaElementti() {
    element = angular.element('<arviointi arviointi="arviointi"></arviointi>');
    $compile(element)(scope);
    scope.$digest();
  };

  it('näyttää tekstin jos teksti on olemassa ja kohdealueita ei ole', function () {
    scope.arviointi = {
      id: 2276,
      lisatiedot: {
        _id: '2277',
        fi: 'Arviointi tekstinä'
      },
      arvioinninKohdealueet: []
    };
    kaannaElementti();
    expect(element.html()).toContain('Arviointi tekstinä');
  });

  it('näyttää kohdealueet eikä tekstiä jos teksti on tyhjä', function () {
    scope.arviointi = {
      id: 2276,
      lisatiedot: {
        _id: '2277',
        fi: '',
        se: ''
      },
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
    kaannaElementti();
    var html = element.html();
    expect(html).toContain('Työprosessin hallinta');
    expect(html).toContain('Suunnittelu');
  });
});
