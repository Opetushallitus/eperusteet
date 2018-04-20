import _ from "lodash";
import {
    setInput,
    compiled,
    getComponent,
    inject,
    getOfType,
    testDirective,
    testModule,
    mockApp
} from "../../testutils";

describe("Muodostumissaannot", () => {
    let Muodostumissaannot: any;

    beforeEach(async () => {
        mockApp();
        Muodostumissaannot = await getComponent("Muodostumissaannot");
    });

    test("Can be injected", () => expect(Muodostumissaannot).toBeTruthy());

    test("Laajuuden laskenta", () => {
        Muodostumissaannot.laskeLaajuudet(rakenne, {});
        expect(rakenne.osat.length).toEqual(1);
        expect(rakenne.osat[0].osat.length).toEqual(4);
        expect(rakenne.osat[0].$laajuus).toEqual(1050);
    });

    test("Ryhmän validointi", () => {
        const result = Muodostumissaannot.validoiRyhma(rakenne, {});
        console.log(rakenne.$virhe);
    });

});


var rakenne = {
  "rooli" : "määritelty",
  "muodostumisSaanto" : {
    "laajuus" : {
      "minimi" : 120,
      "maksimi" : 120,
    },
  },
  "osaamisala" : null,
  "osat" : [ {
    "rooli" : "määritelty",
    "muodostumisSaanto" : {
      "laajuus" : {
        "minimi" : 60,
        "maksimi" : 60,
      },
    },
    "osaamisala" : null,
    "osat" : [ {
      "rooli" : "tutkintonimike",
      "muodostumisSaanto" : {
        "laajuus" : {
          "minimi" : 1500,
          "maksimi" : 1500,
        },
      },
      "osat" : [ ],
    }, {
      "rooli" : "tutkintonimike",
      "muodostumisSaanto" : {
        "laajuus" : {
          "minimi" : 1000,
          "maksimi" : 1000,
        },
      },
      "osat" : [ ],
    }, {
      "rooli" : "osaamisala",
      "muodostumisSaanto" : {
        "laajuus" : {
          "minimi" : 50,
          "maksimi" : 50,
        },
      },
      "osaamisala" : {
        "nimi" : {
          "sv" : "Kompetensområdet för bioteknik",
          "fi" : "Biotekniikan osaamisala"
        },
        "osaamisalakoodiArvo" : "1544",
        "osaamisalakoodiUri" : "osaamisala_1544"
      },
      "osat" : [ ],
      "versioId" : null
    }, {
      "vieras" : null,
      "tunniste" : "d7b6b7e7-d5bd-4182-b0df-9d7da273d016",
      "pakollinen" : false,
      "nimi" : {
        "_tunniste" : "f70de90b-d225-4dbd-9fea-68821433b0a0",
        "fi" : "Apteekkialan osaamisala",
        "sv" : "Kompetensområdet för apoteksbranschen",
        "_id" : "816"
      },
      "rooli" : "osaamisala",
      "muodostumisSaanto" : {
        "laajuus" : {
          "minimi" : 60,
          "maksimi" : 60,
          "yksikko" : null
        },
        "koko" : null
      },
      "osaamisala" : {
        "nimi" : {
          "sv" : "Kompetensområdet för apoteksbranschen",
          "fi" : "Apteekkialan osaamisala"
        },
        "osaamisalakoodiArvo" : "1785",
        "osaamisalakoodiUri" : "osaamisala_1785"
      },
      "osat" : [ ],
      "versioId" : null
    } ],
    "versioId" : null
  } ],
  "versioId" : 93
}
