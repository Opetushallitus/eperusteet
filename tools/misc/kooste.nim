import
    httpclient,
    future,
    sequtils,
    csv,
    json


const koosteEndpoint = "https://virkailija.opintopolku.fi/eperusteet-service/api/perusteet/kooste"

var
    client = newHttpClient()
    rivit = newSeq[seq[string]]()


proc getLocalized(obj: JsonNode): string =
    if obj.hasKey("fi"):
        obj["fi"].getStr
    elif obj.hasKey("sv"):
        obj["sv"].getStr
    else:
        "<ei käännöstä>"


when isMainModule:
    echo "Fetching data..."
    for peruste in client.getContent(koosteEndpoint)
        .parseJson
        .elems:
            echo "Writing " & peruste["diaarinumero"].getStr
            rivit.add(@[peruste["diaarinumero"].getStr, peruste["nimi"].getLocalized()])
            for tosa in peruste["tutkinnonOsat"].elems:
                rivit.add(@[tosa["koodiArvo"].getStr, tosa["nimi"].getLocalized()])
            rivit.add(@[""])

    discard "kooste.csv"
        .writeAll(rivit)
