'use strict';

describe('Service: PerusteProjektiSivunavi', function() {

  // load the service's module
  beforeEach(module('eperusteApp'));

  beforeEach(function () {
    var mockTiedot = {
      getProjekti: function () { return {}; },
      getPeruste: function () { return {}; },
      getSisalto: function () { return {}; },
      getYlTiedot: function () {
        return {
          oppiaineet: [
            {nimi: {fi: 'Matematiikka'}},
            {nimi: {fi: 'B-kieli'}, oppimaarat: [
              {nimi: {fi: 'Ranska'}},
              {nimi: {fi: 'Saksa'}},
            ]}
          ]
        };
      }
    };

    var loader = {
      then: function (cb) {
        cb(mockTiedot);
      }
    };

    var yleinenMock = {
      isPerusopetus: function () { return true; }
    };

    module(function ($provide) {
        $provide.value('PerusteprojektiTiedotService', loader);
        $provide.value('YleinenData', yleinenMock);

    });

  });

  // instantiate service
  var PerusteProjektiSivunavi, PerusopetusService, $timeout, $http;

  beforeEach(inject(function(_PerusteProjektiSivunavi_, _PerusopetusService_, _$timeout_, $httpBackend) {
    PerusteProjektiSivunavi = _PerusteProjektiSivunavi_;
    PerusopetusService = _PerusopetusService_;
    $timeout = _$timeout_;
    $http = $httpBackend;
    $httpBackend.when('GET', /cas\/me/).respond({});
    $httpBackend.when('GET', /views.+/).respond({});
    $httpBackend.when('GET', /localisation.+/).respond({});
    $httpBackend.when('GET', /eperusteet-service\/api.+/).respond({});
  }));

  it('should build sivunavi hierarchy for YL', function () {
    var items = [];
    var callbacks = {
      changed: function (value) {
        items = value;
      },
      typeChanged: function () {}
    };

    spyOn(callbacks, 'typeChanged');
    PerusteProjektiSivunavi.register('itemsChanged', callbacks.changed);
    PerusteProjektiSivunavi.register('typeChanged', callbacks.typeChanged);
    PerusteProjektiSivunavi.refresh(true);
    $timeout.flush();

    expect(callbacks.typeChanged).toHaveBeenCalledWith('YL');
    var level2Items = _.filter(items, function (item) {
      return item.depth === 2;
    });
    expect(_.size(level2Items)).toBe(2);
  });

});
