package fi.vm.sade.eperusteet.tools.opintoalaimporter;

import au.com.bytecode.opencsv.CSVReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class OpintoalaImporter 
{
    /**
     * 
     * @param args koulutusala, polku tiedostoon missä listattu opintoalat
     */
    public static void main( String[] args ) throws IOException, Exception
    {
        if (args.length < 2) {
            System.out.println("Vähintään kaksi parametriä. <koulutusalakoodi>, <opintoalatiedosto>\n"
                    + "Koulutusalakoodit:\n"
                    + "1: Tekniikan ja liikenteen ala\n"
                    + "2: Kulttuuriala,\n"
                    + "3: Luonnonvara- ja ympäristöala,\n"
                    + "4: Yhteiskuntatieteiden, liiketalouden ja hallinnon ala,\n"
                    + "5: Humanistinen ja kasvatusala,\n"
                    + "6: Matkailu-, ravitsemis- ja talousala,\n"
                    + "7: Sosiaali-, terveys- ja liikunta-ala,\n"
                    + "8: Luonnontieteiden ala");
            return;
        }

        List <String> opintoalat = new ArrayList<String>();
        String[] opintoalatArray = new String[] {"Kielitieteet", "Muu humanistinen ja kasvatusala", "Opetus- ja kasvatustyö", "Vapaa-aika ja nuorisotyö",
                                "Käsi- ja taideteollisuus", "Musiikki", "Teatteri ja tanssi", "Viestintä- ja informaatiotieteet",
                                "Kotitalous- ja kuluttajapalvelut", "Majoitus- ja ravitsemisala", "Matkailuala", "Puhdistuspalvelut",
                                "Tietojenkäsittely",
                                "Kalatalous", "Luonto- ja ympäristöala", "Maatilatalous", "Metsätalous", "Muu luonnonvara- ja ympäristöalan koulutus", "Puutarhatalous",
                                "Farmasia ja muu lääkehuolto", "Hammaslääketiede ja muu hammashuolto", "Kauneudenhoitoala", "Kuntoutus ja liikunta", /*"Opetus- ja kasvatustyö",*/ "Sosiaali- ja terveysalojen yhteiset", "Sosiaaliala", "Tekniset terveyspalvelut", "Terveysala",
                                "Ajoneuvo- ja kuljetustekniikka", "Arkkitehtuuri ja rakentaminen", "Elintarvikeala ja biotekniikka", "Graafinen ja viestintätekniikka", "Kone-, metalli- ja energiatekniikka", "Muu tekniikan ja liikenteen alan koulutus", "Prosessi-, kemian- ja materiaalitekniikka", "Sähkö- ja automaatiotekniikka", "Tekstiili- ja vaatetustekniikka", "Tieto- ja tietoliikennetekniikka",
                                "Liiketalous ja kauppa"};
        opintoalat.addAll(Arrays.asList(opintoalatArray));
        
        String koulutusalakoodi = args[0];
        String perusteTiedosto = args[1];

        Map<String, List<String>> tutkinnotOpintoaloittain;
        
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(perusteTiedosto), "UTF-8"), 
                                        ',', '\"', 1);
                       
        String output = "";
        try {

            tutkinnotOpintoaloittain = luoMap(reader, opintoalat);
            
            StringBuilder sb = new StringBuilder();

            for (String opintoalakoodi : tutkinnotOpintoaloittain.keySet()) {
                sb.append("insert into koulutusala_opintoala(koulutusala_id, opintoala_id) ");
                sb.append("select ka.id as koulutusala_id, oa.id as opintoala_id from koulutusala ka, opintoala oa ");
                sb.append("where ka.koodi = ");
                sb.append("'").append(koulutusalakoodi).append("' ");
                sb.append("and oa.koodi= ");
                sb.append("'").append(opintoalakoodi).append("';\n");
            }

            sb.append("\n");
            
            for (String opintoalakoodi : tutkinnotOpintoaloittain.keySet()) {
                for(String tutkinto : tutkinnotOpintoaloittain.get(opintoalakoodi)) {
                    sb.append("insert into peruste_opintoala(peruste_id, opintoala_id) ");
                    sb.append("select p.id as peruste_id, oa.id as opintoala_id from peruste p ");
                    sb.append("join tekstipalanen t on p.nimi_id = t.id ");
                    sb.append("join tekstipalanen_teksti tt on tt.tekstipalanen_id = t.id and tt.kieli='FI' and tt.teksti= ");
                    sb.append("'").append(tutkinto).append("' ");
                    sb.append("join opintoala oa on oa.koodi = ");
                    sb.append("'").append(opintoalakoodi).append("';\n");
                }
            }
            
            output = sb.toString();
            PrintStream out = new PrintStream(System.out, true, "UTF-8");
            out.println(output);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OpintoalaImporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            reader.close();
        }
        
        PrintWriter writer = new PrintWriter("opintoalat.sql", "UTF-8");
        writer.println(output);
        writer.close();
        
    }
    
    public static Map<String, List<String>> luoMap(CSVReader reader, List<String> opintoalat) throws IOException, Exception {
        Map<String, List<String>> tutkinnotOpintoaloittainTemp = new HashMap<String, List<String>>();

        String[] rivi = reader.readNext();
        List<String> avaimet = new ArrayList<String>();
        
        if (rivi != null) {
            for (String opintoala : rivi) {
                System.out.println("opintoala: " + opintoala);
                if (opintoalat.indexOf(opintoala.trim()) == -1) {
                    System.out.println("Opintoalaa ei löydy listalta: " + opintoala);
                    throw new Exception("opintoala ei täsmää");
                }
                String opintoalaKey = Integer.toString(opintoalat.indexOf(opintoala.trim()) + 1 );
                tutkinnotOpintoaloittainTemp.put(opintoalaKey, new ArrayList<String>());
                avaimet.add(opintoalaKey);
            }
        }

        System.out.println("avaimet: " + avaimet);

        rivi = reader.readNext();
        
        while (rivi != null) {
            
            System.out.println("rivi: " + rivi);
        
            for (int i = 0; i < rivi.length; i++) {
                if (!rivi[i].trim().equals("")) {
                    System.out.println("ei tyhjä rivin alkio: " + rivi[i].trim());
                    System.out.println("i: " + i);
                    tutkinnotOpintoaloittainTemp.get(avaimet.get(i)).add(rivi[i].trim());
                }
            }
            rivi = reader.readNext();
        }

        return tutkinnotOpintoaloittainTemp;
    }
    
    
}
