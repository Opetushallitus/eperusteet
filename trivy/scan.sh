#!/usr/bin/env bash

# Skriptin luoma haavoittuvuudet_* tiedosto on tyhjä jos trivy ei löytänyt yhtään haavoittuvuutta

# Luo GitHub token:
#https://github.com/settings/personal-access-tokens
#Generate new token

#### New fine-grained personal access token
#Token name: trivy-skannaus
#Resource owner: Opetushallitus
#Expiration: <mikä tahansa muu kuin 'No expiration'>

#### Repository accesss
#Only select repositories: [X]
#- Valitse skannattavat repot

#### Permissions
#Repository permissions
#- Contents: Read-only
#- Metadata: Read-only (pakollinen)

# Lisää luomasi GitHub token
export GITHUB_TOKEN=''

# Skannattavat vakavuudet
vakavuudet='CRITICAL,HIGH,MEDIUM,LOW'

# Skannattavien repojen nimet
repot=(
  'eperusteet'
  'eperusteet-ui'
  'eperusteet-amosaa'
  'eperusteet-amosaa-ui'
  'eperusteet-ylops'
  'eperusteet-ylops-ui'
  'eperusteet-frontend-utils'
  'eperusteet-opintopolku'
  'eperusteet-pdf'
)

for repo in "${repot[@]}"; do
  printf "Skannataan repo: ${repo}\n"
  trivy repo --scanners vuln --severity ${vakavuudet} "github.com/Opetushallitus/${repo}" --format template --template "@html.tpl" --output "results/${repo}_$(date '+%Y-%m-%d').html" &
done

printf "\nOdotetaan taustatehtävien valmistumista...\n"
wait

printf "\nKaikki valmista.\n\n"