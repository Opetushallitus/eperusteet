# Generoi openapi-kuvauksen
gen_openapi:
	@cd eperusteet/eperusteet-service/ \
		&& mvn clean verify -Pspringdoc \
		&& cp target/openapi/eperusteet.spec.json ../../generated
		
# Generoi julkinen openapikuvaus		
gen_openapi_ext:
	@cd eperusteet/eperusteet-service/ \
		&& mvn clean verify -Pspringdoc-ext \
		&& cp target/openapi/eperusteet-ext.spec.json ../../generated
