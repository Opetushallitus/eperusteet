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
  ext)
    gen_openapi_ext
    ;;
  "")
    gen_openapi
    ;;
  *)
    echo "Usage: $0 [ext]"
    echo "  (no args)  - generate standard OpenAPI spec"
    echo "  ext       - generate extended/public OpenAPI spec"
    exit 1
    ;;
esac
