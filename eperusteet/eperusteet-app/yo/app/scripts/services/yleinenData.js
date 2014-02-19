'use strict';
/*global _*/

angular.module('eperusteApp')
  .service('YleinenData', function YleinenData($translate, Arviointiasteikot, $rootScope) {

      this.kontekstit = [
        'ammatillinenperuskoulutus',
        'ammatillinenaikuiskoulutus'
      ];


      this.navigaatiopolkuElementit = [];

      this.kielet = {
        'suomi': 'fi',
        'ruotsi': 'sv'
      };

      this.kieli = 'fi';

      this.arviointiasteikot = undefined;

      this.haeArviointiasteikot = function() {
        if (this.arviointiasteikot === undefined) {
          var self = this;
          Arviointiasteikot.query({}, function(tulos) {

            self.arviointiasteikot = _.indexBy(tulos, 'id');
            $rootScope.$broadcast('arviointiasteikot');

          }, function(/*virhe*/) {
            // TODO
          });

        } else {
          $rootScope.$broadcast('arviointiasteikot');
        }
      };

      this.lisääKontekstitPerusteisiin = function(perusteet) {
        if (perusteet) {
          for (var i = 0; i < perusteet.length; i++) {
            switch (perusteet[i].tutkintokoodi)
            {
              case '1':
                perusteet[i].konteksti = this.kontekstit[0];
                break;
              case '2':
                perusteet[i].konteksti = this.kontekstit[1];
                break;
              case '3':
                perusteet[i].konteksti = this.kontekstit[1];
                break;
            }
          }
        }
      };

      this.vaihdaKieli = function(kielikoodi) {

        var löytyi = false;
        for (var avain in this.kielet) {
          if (this.kielet.hasOwnProperty(avain)) {
            if (this.kielet[avain] === kielikoodi) {
              löytyi = true;
              $translate.use(kielikoodi);
              this.kieli = kielikoodi;
              break;
            }
          }
        }
        // Jos kielikoodi ei löydy listalta niin käytetään suomea.
        if (!löytyi) {
          $translate.use('fi');
          this.kieli = 'fi';
        }
      };

      this.valitseKieli = function(teksti) {

        if (teksti && teksti.hasOwnProperty(this.kieli)) {
          return teksti[this.kieli];
        } else {
          return '';
        }

      };

    });
