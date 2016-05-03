import
    future,
    sequtils,
    csv,
    json


var rivit = newSeq[seq[string]]()

for peruste in readFile("kooste.json")
    .parseJson
    .elems:
        rivit.add(@[peruste["diaarinumero"].getStr, peruste["nimi"]["fi"].getStr])
        for tosa in peruste["tutkinnonOsat"].elems:
            rivit.add(@[tosa["koodiArvo"].getStr, tosa["nimi"]["fi"].getStr])
        rivit.add(@[""])

discard "kooste.csv"
    .writeAll(rivit)
