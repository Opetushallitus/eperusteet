package fi.vm.sade.eperusteet.tools.perusteimporter;

import au.com.bytecode.opencsv.CSVReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author harrik
 */
public class PerusteImporter {

    /**
     * 16.12.2013: Lisätty "ruotsinnus" -toiminto. Eli luodaan ruotsinkielen koodilla
     *             perusteotsikko, missä csv:ssä oleva perusteen nimi on ympäröity hakasulkeilla.
     * @param args the command line arguments
     */
    /*args[]: (koulutusala, polku tiedostoon missä listattu perusteet)*/    
    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.out.println("Vähintään kaksi parametriä. <koulutusalakoodi>, <perustetiedosto>\n"
                    + "Koulutusalakoodit:\n"
                    + "1: Tekniikan ja liikenteen ala\n"
                    + "2: Kulttuuriala,\n"
                    + "3: Luonnonvara- ja ympäristöala,\n"
                    + "4: Yhteiskuntatieteiden, liiketalouden ja hallinnon ala,\n"
                    + "5: Humanistinen ja kasvatusala,\n"
                    + "6: Matkailu-, ravitsemis- ja talousala,\n"
                    + "7: Sosiaali-, terveys- ja liikunta-ala,\n"
                    + "8: Luonnontieteiden ala,\n"
                    + "\n"
                    + "Tutkintotyyppikoodit:\n"
                    + "1: perustutkinto,\n"
                    + "2: ammattitutkinto,\n"
                    + "3: erikoisammattitutkinto");
            return;
        }
        
        String koulutusala = args[0];
        String perusteTiedosto = args[1];

        Map<Integer, List<String>> perusteNimet;
        
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(perusteTiedosto), "UTF-8"), 
                                        ',', '\"', 2);
                       
        String output = "";
        try {

            perusteNimet = luoMap(reader);

            StringBuilder sb = new StringBuilder();

            sb.append("create table id (id bigint);\n");

            for (Integer mapIndex : perusteNimet.keySet()) {

                for (String peruste : perusteNimet.get(mapIndex)) {
                    sb.append("insert into id values (nextval('hibernate_sequence'));\n");
                    sb.append("insert into tekstipalanen(id) select id from id;\n");

                    sb.append("insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'FI' as kieli, ");
                    sb.append("'").append(peruste).append("'");
                    sb.append(" as teksti from id;\n");
                    
                    sb.append("insert into tekstipalanen_teksti(tekstipalanen_id, kieli, teksti) select id, 'SV' as kieli, ");
                    sb.append("'[").append(peruste).append("]'");
                    sb.append(" as teksti from id;\n");

                    sb.append("insert into peruste(nimi_id, koulutusala_id, tutkintokoodi, paivays, id) ");
                    sb.append("select id.id, ka.id, ");
                    sb.append("'").append(mapIndex.intValue()).append("', ");
                    sb.append("current_timestamp, nextval('hibernate_sequence') ");
                    sb.append("from id, koulutusala ka where ");
                    sb.append("ka.koodi='").append(koulutusala).append("';\n");

                    sb.append("delete from id;\n");

                }
            }
            sb.append("DROP TABLE ID;");
            output = sb.toString();
            PrintStream out = new PrintStream(System.out, true, "UTF-8");
            out.println(output);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PerusteImporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            reader.close();
        }

        PrintWriter writer = new PrintWriter("peruste.sql", "UTF-8");
        writer.println(output);
        writer.close();

    }

    public static Map<Integer, List<String>> luoMap(CSVReader reader) throws IOException {
        Map<Integer, List<String>> perusteNimetTemp = new HashMap<Integer, List<String>>();
        perusteNimetTemp.put(1, new ArrayList<String>());
        perusteNimetTemp.put(2, new ArrayList<String>());
        perusteNimetTemp.put(3, new ArrayList<String>());
        
        String[] rivi = reader.readNext();

        while (rivi != null) {
        
            for (int i = 0; i < rivi.length; i++) {
                if (!rivi[i].trim().equals("")) {
                    perusteNimetTemp.get(i + 1).add(rivi[i].trim());
                }
            }
            rivi = reader.readNext();
        }

        return perusteNimetTemp;
    }

}
