help:
	@just -l

build:
	@cd ./eperusteet/ && mvn clean install

clean:
	@cd ./eperusteet/ && mvn clean compile

gen_kooste:
	nim c -d:ssl -r ./tools/misc/kooste.nim

gen_schema:
	@cd ./eperusteet/eperusteet-service && mvn clean compile hibernate4:export -o|$EDITOR -

sync_localisations:
	@node ./tools/lokalisointi/sync_locales.js eperusteet/eperusteet-app/yo/app/localisation
