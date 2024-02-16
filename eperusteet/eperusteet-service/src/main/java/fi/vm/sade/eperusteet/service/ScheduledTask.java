package fi.vm.sade.eperusteet.service;

public interface ScheduledTask {
    // Mitä suurempi arvo, sen tärkeämpi
    int getPriority();
    String getName();
    void execute();
}
