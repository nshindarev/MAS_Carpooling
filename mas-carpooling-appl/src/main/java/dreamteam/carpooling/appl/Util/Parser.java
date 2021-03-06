package dreamteam.carpooling.appl.Util;

import org.jgrapht.Graph;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *  Парсер. Считывает город из файла. Город доступен по методу getCity()
 */

public class Parser {
    //logger
    public static final Logger logger = LoggerFactory.getLogger(Parser.class);

    //private fields
    private File city_file;
    private VertexProvider<String> vertex_provider;
    private EdgeProvider<String, Integer> edge_provider;
    private MyCityGraph<String, MyWeightedEdge> city;


    //public fields
    public MyCityGraph<String, MyWeightedEdge> getCity (){
            return this.city;
    }

    /**
     *
     * @param city_file путь к файлу от root-директории
     */
    public Parser (File city_file){
    try{
        this.city_file = city_file;
    }
    catch (Exception ex){
        logger.error(ex.getMessage());
    }
        parseCityFromFile();
    }
    public Parser (){
        try {
            this.city_file = new File("mas-carpooling-appl/src/main/resources/small_city.gml");
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        parseCityFromFile();
        setDistricts();

    }


    public void parseCityFromFile(){
        this.city = new MyCityGraph<String, MyWeightedEdge>(MyWeightedEdge.class);


        GmlImporter<String,MyWeightedEdge> importer =
                new GmlImporter<>(
                        (String label, Map<String, String> attributes)
                                 -> {return label;},
                        (String from, String to, String label, Map<String, String> attributes)
                                 -> { return city.getEdgeFactory().createEdge(from, to); });



        try{
            importer.importGraph(this.city, this.city_file);
        }
        catch (ImportException ex){
            logger.error("сломалось при чтении из файла " + city_file.getPath());
        }
    }

    private void setDistricts() {
        List<District> districts = new CreatorDistrict(this).getDistricts();
        for (District district : districts) {
            city.addCity_district(district.getDistrictName(), district.getVertexes());
        }
    }
}

