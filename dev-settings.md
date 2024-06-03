
### user.home\oph-configuration\override.properties
- ulkopuolisten palveluiden sijaintien configuraatiot
- korvaa [username] ja [password] käyttäjän testiopintopolku tunnuksiin

```properties
cas.service.authentication-service=https://virkailija.testiopintopolku.fi/authentication-service
cas.service.organisaatio-service=https://virkailija.testiopintopolku.fi/organisaatio-service
cas.service.oppijanumerorekisteri-service=https://virkailija.testiopintopolku.fi/oppijanumerorekisteri-service
cas.service.kayttooikeus-service=https://virkailija.testiopintopolku.fi/kayttooikeus-service
fi.vm.sade.eperusteet.ylops.eperusteet-service=https://virkailija.testiopintopolku.fi/eperusteet-service
fi.vm.sade.eperusteet.amosaa.eperusteet-service=https://virkailija.testiopintopolku.fi/eperusteet-service
koodisto.service.url=https://virkailija.testiopintopolku.fi/koodisto-service

fi.vm.sade.eperusteet.salli_virheelliset=true

fi.vm.sade.eperusteet.amosaa.oph_username=[username]
fi.vm.sade.eperusteet.amosaa.oph_password=[password]
fi.vm.sade.eperusteet.ylops.oph_username=[username]
fi.vm.sade.eperusteet.ylops.oph_password=[password]
fi.vm.sade.eperusteet.oph_username=[username]
fi.vm.sade.eperusteet.oph_password=[password]

web.url.cas=https\://virkailija.testiopintopolku.fi/cas
hibernate.show_sql=false
```

