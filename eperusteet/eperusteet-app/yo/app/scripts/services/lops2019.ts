import * as angular from "angular";

angular
    .module("eperusteApp")
    .service("Lops2019Service", function(
        $state,
        Api
    ) {
        let tiedot = null;
        this.setTiedot = value => {
            tiedot = value;
        };

        this.LAAJA_ALAINEN_OSAAMINEN = "laaja_alainen_osaaminen";
        this.OPPIAINEET_OPPIMAARAT = "oppiaineet_oppimaarat";
        this.sisallot = [
            {
                tyyppi: this.LAAJA_ALAINEN_OSAAMINEN,
                label: "laaja-alainen-osaaminen",
                emptyPlaceholder: "tyhja-placeholder-laaja-alainen-osaaminen",
                addLabel: "lisaa-laaja-alainen-osaaminen",
                stateName: 'lops2019laajaalaiset'
            },
            {
                tyyppi: this.OPPIAINEET_OPPIMAARAT,
                label: "oppiaineet-oppimaarat",
                emptyPlaceholder: "tyhja-placeholder-oppiaineet-oppimaarat",
                addLabel: "lisaa-oppiaine",
                stateName: 'lops2019oppiaineet'
            }
        ];

        this.getSisalto = async () => {
            return await Api
                .one("perusteet", tiedot.getPeruste().id)
                .one("suoritustavat", "lukiokoulutus2019")
                .customGET("sisalto");
        };

        this.getOpetus = () => {
            const opetus = {
                lapset: []
            };

            // Iteroi alueet
            _.each(this.sisallot, async item => {
                const data = {
                    nimi: item.label,
                    tyyppi: item.tyyppi,
                    lapset: [],
                    $type: "ep-parts",
                    $url: $state.href("root.perusteprojekti.suoritustapa." + item.stateName)
                };

                try {
                    data.lapset = await this.getOsat(item.tyyppi);
                } catch (e) {
                    console.warn(e);
                    // TODO
                }

                opetus.lapset.push(data);
            });

            return opetus;
        };

        this.getOsat = async tyyppi => {
            try {
                switch (tyyppi) {
                    case this.LAAJA_ALAINEN_OSAAMINEN:
                        // Hae laaja-alainen osaaminen
                        return await Api
                            .one("perusteet", tiedot.getPeruste().id)
                            .one("lops2019")
                            .customGET("laajaalaiset");
                    case this.OPPIAINEET_OPPIMAARAT:
                        // Hae oppiaineet
                        return await Api
                            .one("perusteet", tiedot.getPeruste().id)
                            .one("lops2019")
                            .customGET("oppiaineet");
                }
            } catch (e) {
                console.warn(e);
                // TODO
            }
        };
    });
