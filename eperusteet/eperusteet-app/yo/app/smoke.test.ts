import "app/testutils";

angular.module("testModule", [])
    .service("SomeService", ($rootScope) => {
        return {
            works() {
                return "yes";
            }
        }
    });


describe("smoke", () => {
    beforeEach(angular.mock.module("testModule"));

    test("Can import service", () => {
        angular.mock.inject((SomeService) => {
            expect(SomeService.works()).toEqual("yes");
        });
    });
});
