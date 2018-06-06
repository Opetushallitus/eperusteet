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


import $ from "jquery";
import "jquery-ui";
import "angular";
import _ from "lodash";
import "jquery-sticky";

import "bootstrap";
import "angular-ui-router";
import "angular-sanitize";
import "angular-resource";
import "angular-translate";
import "angular-animate";
import "angular-i18n/angular-locale_fi-fi";
import "angular-cache";
import "angular-local-storage";
import "angular-translate-loader-static-files";
import "angular-ui-sortable";
import "angular-ui-tree";
import "angular-elastic";
import "angular-ui-bootstrap";
import "angular-loading-bar";
import "ui-select";
import "restangular";
import "ng-file-upload";
// import "es6-promise";

import "eperusteet-frontend-utils/mathdisplay";

import "styles/eperusteet.scss";

import "moment/locale/fi.js";
import "moment/locale/sv.js";

import "./lmixins";

import "scripts/app";
import "scripts/api";
import "scripts/misc";
import "scripts/directives/valmiskaannos";
import "scripts/directives/sortabletable";
import "scripts/directives/generictree";
import "scripts/directives/epfooter";
import "scripts/controllers/virhe";
import "scripts/controllers/tuotekstikappale";
import "scripts/directives/pagination";
import "scripts/directives/muokkaus/tutke2osa";
import "scripts/directives/readmore";
import "scripts/directives/contenttree";
import "scripts/directives/perusteprojekti/murupolku";
import "scripts/services/utils";
import "scripts/services/kieli";
import "scripts/services/kuvat";
import "scripts/services/leikelauta";
import "scripts/services/koulutuksenosamap";
import "scripts/services/ulkopuoliset";
import "scripts/services/algoritmit";
import "scripts/services/tyoryhmat";
import "scripts/services/projektiryhma";
import "scripts/services/pagination";
import "scripts/services/pdf";
import "scripts/services/excel";
import "scripts/services/muodostumissaannot";
import "scripts/services/lokalisointi";
import "scripts/services/versionhelper";
import "scripts/services/proxyservice";
import "scripts/directives/iconrole";
import "scripts/directives/tagcloud";
import "scripts/directives/rajaus";
import "scripts/directives/ohje";
import "scripts/directives/muokkaus/versiotiedot";
import "scripts/directives/multiinput";
import "scripts/directives/kommentit";
import "scripts/directives/kommenttiviesti";
import "scripts/directives/osalistaus";
import "scripts/directives/muokkaus/leikelauta";
import "scripts/directives/osallinenosa";
import "scripts/directives/muokattavaosio";
import "scripts/directives/perusopetus/tavoitteet";
import "scripts/directives/perusopetus/sisaltoalueet";
import "scripts/directives/perusopetus/arviointi";
import "scripts/controllers/perusteprojekti/osanmuokkaus";
import "scripts/directives/muokkaus/vuosiluokkakokonaisuus";
import "scripts/directives/muokkaus/oppiaine";
import "scripts/directives/muokkaus/perusopetusosaaminen";
import "scripts/directives/muokkaus/osaaminen";
import "scripts/controllers/excel";
import "scripts/controllers/tiedotteet";
import "scripts/directives/spinner";
import "scripts/controllers/modals/uudelleenohjaus";
import "scripts/controllers/modals/muodostumisryhma";
import "scripts/directives/fileselect";
import "scripts/directives/formfield";
import "scripts/directives/followscroll";
import "scripts/services/perusteet";
import "scripts/services/lukko";
import "scripts/services/perusteenosat";
import "scripts/services/varihyrra";
import "scripts/services/colorcalculator";
import "scripts/controllers/haku";
import "scripts/controllers/perusteprojekti/projektinTila";
import "scripts/filters/kaanna";
import "scripts/filters/tutkintokoodi";
import "scripts/filters/koulutusalakoodi";
import "scripts/filters/customfilters";
import "scripts/directives/koodisto";
import "scripts/directives/tree";
import "scripts/controllers/modals/aikakatko";
import "scripts/directives/sivunavigaatio";
import "scripts/services/notifikaatiot";
import "scripts/directives/statusbadge";
import "scripts/directives/sticky";
import "scripts/services/suosikit";
import "scripts/services/haku";
import "scripts/services/kayttajaprofiilit";
import "scripts/services/koulutusalat";
import "scripts/controllers/suosikit";
import "scripts/services/yleinenData";
import "scripts/services/arviointi"
import "scripts/services/navigaatiopolku"
import "scripts/services/editointikontrollit"
import "scripts/directives/editointikontrollit"
import "scripts/directives/editointiKontrolli"
import "scripts/directives/tuotutkinnonosa"
import "scripts/controllers/muokkaus"
import "scripts/directives/muokkaus/lukko"
import "scripts/directives/muokkaus/revertnote"
import "scripts/controllers/perusteprojekti/tutkinnonosa"
import "scripts/controllers/perusteprojekti/tutkinnonosa2018"
import "scripts/controllers/perusteprojekti/koulutuksenosa"
import "scripts/controllers/perusteprojekti/tekstikappale"
import "scripts/directives/pikamenu"
import "scripts/directives/editinplace"
import "scripts/directives/termistoviitteet"
import "scripts/services/lukiokoulutus"
import "scripts/services/perusteprojekti"
import "scripts/controllers/perusteprojekti/projektinTiedot"
import "scripts/controllers/perusteprojekti/muodostumissaannot"
import "scripts/controllers/perusteprojekti/tutkinnonosat"
import "scripts/controllers/perusteprojekti/projektiryhma"
import "scripts/controllers/perusteprojekti/projektinperustiedot"
import "scripts/controllers/perusteprojekti/termisto"
import "scripts/controllers/omatperusteprojektit"
import "scripts/services/perusopetus"
import "scripts/controllers/perusopetus"
import "scripts/controllers/lukiokoulutus"
import "scripts/controllers/kurssit"
import "scripts/directives/datecomparisonvalidator"
import "scripts/directives/muokkaus/muokattavakentta"
import "scripts/directives/arviointi"
import "scripts/directives/ammattitaitovaatimukset"
import "scripts/directives/valmaarviointi"
import "scripts/controllers/modals/ilmoitusdialogi"
import "scripts/controllers/perusteprojekti/perusteenTiedot"
import "scripts/services/opintoalat"
import "scripts/services/kommentit"
import "scripts/directives/muokkaus/kenttalistaus"
import "scripts/directives/dateformatvalidator"
import "scripts/controllers/aloitussivu"
import "scripts/controllers/perusteprojekti/perusteprojekti"
import "scripts/services/perusteprojekti/sivunavi"
import "scripts/controllers/perusteprojekti/sisalto"
import "scripts/controllers/perusteprojekti/kommentit"
import "scripts/controllers/perusteprojekti/toimikausi"
import "scripts/services/varmistusdialogi"
import "scripts/controllers/admin"
import "scripts/directives/numberinput"
import "scripts/directives/dateformatter"
import "scripts/services/koulutusalat"
import "scripts/controllers/modals/tilanvaihtovirhe"
import "scripts/directives/diaarinumerouniikki"
import "scripts/directives/oikeustarkastelu"
import "scripts/controllers/modals/rakenneosamodal"
import "scripts/controllers/perusteprojekti/tutkinnonosaosaalue"
import "scripts/controllers/perusteprojekti/esiopetus"
import "scripts/controllers/perusteprojekti/tpoopetus"
import "scripts/directives/hallintalinkki"
import "scripts/directives/muokkaus/aihekokonaisuudet"
import "scripts/directives/muokkaus/lukiokoulutusyleisettavoitteet"
import "scripts/states/opas/state"
import "scripts/states/perusteprojekti/suoritustapa/aipesisalto/state"
import "scripts/states/perusteprojekti/suoritustapa/aipeosalistaus/state"
import "scripts/states/perusteprojekti/suoritustapa/aipeosaalue/state"
import "scripts/states/perusteprojekti/suoritustapa/aipeosaalue/oppiaine/state"
import "scripts/states/perusteprojekti/suoritustapa/aipeosaalue/oppiaine/kurssi/state"
import "scripts/services/aipe"
import "scripts/directives/muokkaus/vaihe"
import "scripts/directives/perustetoisetprojektit"
import "scripts/components/tavoitteet/tavoitteet"
import "scripts/directives/ckeditor";
