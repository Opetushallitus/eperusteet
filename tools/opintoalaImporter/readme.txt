Avaa peruste excel
Tallenna haluttu välilehti csv:nä (utf8)
	- Käytä Libre officea millä muunnat .xlxs tiedoston ensin odf formaattiin. Tämä näin jotta utf8 muunnos onnistuisi.
Aja java ohjelma
 - java -jar opintoalaImporter <koulutusalakoodi> <polku opintoala.csv:hen>
 - esim. java -jar opintoalaImporter 1 ../peruste.csv
Aja sql kantaan
 - C:\Projektit\OPH\ePerusteet>psql -U test -d test -f C:\Users\harrik\Documents\NetBeansProjects\opintoalaImporter\dist\opintoala.sql