describe("Perusteprojekti", () => {
    it("Kirjautuminen hallinnalla", () => {
        return browser.get("http://test:test@localhost:9000/")
            .then(() => browser.get("http://localhost:9000/"));
    });

    it("Luonti - Ammatillinen", () => {
        return PerusteHelpers.luoPerusteprojekti("ammatillinen", 2);
    });

    it("Luonti - Julkaisukuntoon", () => {
        return PerusteHelpers.asetaJulkaisuvalmius();
    });

    it("Luonti - Tekstikappaleen lisääminen", () => {
        return PerusteHelpers.lisaaTekstikappale("foobar", "Hello world");
    });

    it("Luonti - Tutkinnon osan lisääminen", () => {
        return PerusteHelpers.lisaaTutkinnonOsa("Testi tutkinnon osa");
    });

});

describe("Perusteprojektin julkaisu", () => {
    it("Kirjautuminen hallinnalla", () => {
        browser.get("http://localhost:9000/");
        return browser.waitForAngular();
    });

    it("Luonti", () => PerusteHelpers.luoPerusteprojekti("ammatillinen", 2));

    it("Julkaisu", () => PerusteHelpers.asetaJulkaisuvalmius()
        .then(() => PerusteHelpers.vaihdaTila("viimeistely"))
        .then(() => PerusteHelpers.vaihdaTila("valmis"))
        .then(() => PerusteHelpers.vaihdaTila("julkaistu")));

    it("Julkaistun perustietojen muokkaus", () => PerusteHelpers.muokkaaPerusteenTiedot("Uusi nimi", "uusidiaari", "10.2.2238"));

    it("Julkaisusta luonnokseen", () => PerusteHelpers.vaihdaTila("luonnos"));

    it("Julkaistu -> luonnos perustietojen muokkaus", () => PerusteHelpers.muokkaaPerusteenTiedot("Vanha nimi", "vanhadiaari", "1.2.2238"));

    it("Julkaisu uudestaan", () => PerusteHelpers.asetaJulkaisuvalmius()
        .then(() => PerusteHelpers.vaihdaTila("viimeistely"))
        .then(() => PerusteHelpers.vaihdaTila("valmis"))
        .then(() => PerusteHelpers.vaihdaTila("julkaistu")));

});
