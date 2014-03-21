import com.google.gson.Gson;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;


public class BlahTest {

    class Row {
        String closedDate;
        String operationalUnit;
        String country;
        String project;
        String openingDate;

        public Row(List<String> line) {
            this.operationalUnit = line.get(0);
            this.country = line.get(1);
            this.project = line.get(2);
            this.openingDate = line.size() > 3 ? LocalDate.parse(line.get(3), DateTimeFormatter.ofPattern("dd-MM-yyyy").withLocale(Locale.getDefault())).format(DateTimeFormatter.ISO_DATE) :  LocalDate.of(1900, Month.JANUARY, 1).format(DateTimeFormatter.ISO_DATE);
            this.closedDate = line.size() > 4  ? LocalDate.parse(line.get(4), DateTimeFormatter.ofPattern("dd-MM-yyyy")).format(DateTimeFormatter.ISO_DATE) : null;
        }
    }

    static int i =0 ;
    static HashMap<String,String> map = new HashMap<>();

    @Test
    public void should_some() throws IOException {
        Map<String, Object> baseObject = new HashMap<>();
        Gson gs = new Gson();


        baseObject.put("active", true);
        baseObject.put("openingDate", LocalDate.of(1900, Month.JANUARY, 1).toString());
        Path path = Paths.get("/Users/TWI/Downloads/data1.csv");
        List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
        PrintStream out = new PrintStream(new FileOutputStream("someoutput.json"));
        System.setOut(out);

        Function<? super String, List<String>> lineMapper = line -> asList(line.split("\t")).stream().map(String::trim).collect(Collectors.toList());
        List<Row> splitLines = lines.stream().map(lineMapper).map(Row::new).collect(Collectors.toList());


        Map<String, List<Row>> collect = splitLines.stream().collect(Collectors.groupingBy(s -> s.operationalUnit));
        List<Map<String, Object>> orgUnit = collect.keySet().stream().map(ou -> {
            Map<String, Object> cloneOrgUnit = new HashMap<>(baseObject);
            cloneOrgUnit.put("name", ou);
            cloneOrgUnit.put("shortName", ou);
            String id = "opunit_" + i;
            cloneOrgUnit.put("id", id);
            map.put(ou , id);
            i++;
            return cloneOrgUnit;
        }).collect(Collectors.toList());

        i=0;



        Map<String, List<Row>> collectCountries = splitLines.stream().collect(Collectors.groupingBy(s -> s.country));
        List<Map<String, Object>> countries = collectCountries.entrySet().stream().map(entry -> {
            Map<String, Object> cloneCountries = new HashMap<>(baseObject);
            cloneCountries.put("name", entry.getKey());
            cloneCountries.put("shortName", entry.getKey());
            Row values = entry.getValue().get(0);
            cloneCountries.put("parent", createHash(map.get(values.operationalUnit)));
            String id = "country_" + i;
            cloneCountries.put("id", id);
            map.put(entry.getKey() , id);
            i++;
            return cloneCountries;
        }).collect(Collectors.toList());

        i=0;

        List<Map<String, Object>> projects = splitLines.stream().map(entry -> {
            Map<String, Object> cloneProjects = new HashMap<>(baseObject);
            cloneProjects.put("name", entry.project);
            cloneProjects.put("shortName", entry.project);
            cloneProjects.put("parent", createHash(map.get(entry.country)));
            cloneProjects.put("id", "proj_"+ i);
            cloneProjects.replace("openingDate", entry.openingDate);
            cloneProjects.put("closedDate", entry.closedDate);
            i++;
            return cloneProjects;
        }).collect(Collectors.toList());


        orgUnit.addAll(countries);
        orgUnit.addAll(projects);
        out.println(gs.toJson(orgUnit));

    }



    public Map createHash(String id) {
        Map<String, Object> parentMap = new HashMap<>();
        parentMap.put("id", id);
        return parentMap;
    }


}


