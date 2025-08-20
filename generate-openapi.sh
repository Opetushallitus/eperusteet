#!/bin/bash
set -euo pipefail

# Generoi openapi-kuvauksen
gen_openapi() {
  cd eperusteet/eperusteet-service/ \
    && mvn clean verify -Pspringdoc \
    && cp target/openapi/eperusteet.spec.json ../../generated
  cd - > /dev/null
}

# Generoi julkinen openapikuvaus
gen_openapi_ext() {
  cd eperusteet/eperusteet-service/ \
    && mvn clean verify -Pspringdoc-ext \
    && cp target/openapi/eperusteet-ext.spec.json ../../generated
  cd - > /dev/null
}

# Dispatch based on argument
case "${1:-}" in
  openapi)
    gen_openapi
    ;;
  openapi_ext)
    gen_openapi_ext
    ;;
  *)
    echo "Usage: $0 {gen_openapi|gen_openapi_ext}"
    exit 1
    ;;
esac
