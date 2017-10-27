import * as yd from "./yleinenData";
import _ from "lodash";

import { getComponent, inject, getOfType, testDirective, testModule, mockApp } from "app/testutils";

describe("YleinenData", async () => {
    let YleinenData: any;

    beforeEach(async () => {
        mockApp();
        YleinenData = await getComponent("YleinenData");
    });

    test("Can be injected", () => expect(YleinenData).toBeTruthy());

    test("dateOptions", async () => {
        yd.dateOptions["year-format"] === "yy";
    });

    test("koulutustyyppiInfo avaimet", () => {
        expect(_.size(yd.koulutustyyppiInfo)).toBeGreaterThanOrEqual(15);
    });

    test("koulutustyyppiInfo arvot", () => {
        _.values(yd.koulutustyyppiInfo).forEach((ki: any) => {
            expect(ki).toHaveProperty("nimi");
            expect(_.isString(ki.nimi)).toEqual(true);

            expect(ki).toHaveProperty("oletusSuoritustapa");
            expect(_.isString(ki.oletusSuoritustapa)).toEqual(true);

            expect(ki).toHaveProperty("hasTutkintonimikkeet");
            expect(_.isBoolean(ki.hasTutkintonimikkeet)).toEqual(true);

            expect(ki).toHaveProperty("hasLaajuus");
            expect(_.isBoolean(ki.hasLaajuus)).toEqual(true);

            expect(ki).toHaveProperty("hakuState");
            expect(_.isString(ki.hakuState)).toEqual(true);

            expect(ki).toHaveProperty("sisaltoTunniste");
            expect(_.isString(ki.sisaltoTunniste)).toEqual(true);

            expect(ki).toHaveProperty("hasPdfCreation");
            expect(_.isBoolean(ki.hasPdfCreation)).toEqual(true);
        });
    });

    test("koulutustyyppiInfo uniikit arvot", () => {
        const uniikitNimet = _(yd.koulutustyyppiInfo)
            .values()
            .map("nimi")
            .uniq()
            .size();
        expect(uniikitNimet).toEqual(_.size(yd.koulutustyyppiInfo));
    });

    test("default export is a function", () => {
        expect(_.isFunction(yd.default)).toBeTruthy();
    });
});
