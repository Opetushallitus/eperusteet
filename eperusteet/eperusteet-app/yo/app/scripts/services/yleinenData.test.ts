import * as yd from "./yleinenData";
import * as _ from "lodash";


describe("yleinenData", async () => {
    test("dateOptions", async () => {
        yd.dateOptions["year-format"] === "yy";
    });

    test("koulutustyyppiInfo avaimet", () => {
        expect(_.size(yd.koulutustyyppiInfo)).toBeGreaterThanOrEqual(15);
    });

    test("koulutustyyppiInfo arvot", () => {
        _.values(yd.koulutustyyppiInfo)
            .forEach((ki: any) => {
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

    test("default export is a function", () => {
        expect(_.isFunction(yd.default)).toBeTruthy();
    });
});
