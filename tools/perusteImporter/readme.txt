Avaa peruste excel
Tallenna haluttu välilehti csv:nä (utf8)
Aja java ohjelma
 - java -jar PerusteImporter <koulutusalakoodi> <polku peruste.csv:hen>
 - esim. java -jar PerusteImporter 1 ../peruste.csv
Aja sql kantaan
 - C:\Projektit\OPH\ePerusteet>psql -U test -d test -f C:\Users\harrik\Documents\NetBeansProjects\perusteImporter\dist\peruste.sql