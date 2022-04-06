package fi.vm.sade.eperusteet.service;


public interface NavigationBuilderPublic extends NavigationBuilder {
    @Override
    default Class getImpl() {
        return NavigationBuilderPublic.class;
    }

}
