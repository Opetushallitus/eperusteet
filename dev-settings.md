
### ~/.m2/settings.xml (GitHub Packages)

Maven-riippuvuudet haetaan GitHub Packagesista. Mallina voi käyttää repon [`.github/maven/settings.xml`](.github/maven/settings.xml)-tiedostoa. Lokaalisti korvaa ympäristömuuttujat GitHub-käyttäjätunnuksella ja personal access tokenilla (`read:packages`).

Tarvittavat server-id:t:
- `github-eperusteet-backend-utils`
- `github-java-utils`
- `github-auditlogger`

### user.home\oph-configuration\override.properties
- ajoaikaiset CAS/palvelutunnukset; korvaa [username] ja [password] käyttäjän testiopintopolku tunnuksiin

```properties

fi.vm.sade.eperusteet.amosaa.oph_username=[username]
fi.vm.sade.eperusteet.amosaa.oph_password=[password]
fi.vm.sade.eperusteet.ylops.oph_username=[username]
fi.vm.sade.eperusteet.ylops.oph_password=[password]
fi.vm.sade.eperusteet.oph_username=[username]
fi.vm.sade.eperusteet.oph_password=[password]

```

