package no.nav.familie.dokumentgenerator.demo.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class JsonUtils {

    private JsonNode readJsonFile(URL path) {
        if (path != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readTree(new File(path.toURI()));
            } catch (IOException | URISyntaxException e) {
                System.out.println("Kan ikke finne JSON fil!");
                e.printStackTrace();
            }
        }
        return null;
    }

    private JsonNode getTestSetField(String templateName, String testSet){
        URL path = ClassLoader.getSystemResource("./content/templates/" + templateName + "/testdata/" + testSet + ".json");
        return readJsonFile(path);
    }

    public JsonNode getJsonFromString(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json);
    }

    public JsonNode extractInterleavingFields(String templateName, JsonNode jsonContent, boolean useTestSet){
        JsonNode valueFields;
        if(useTestSet){
            valueFields = getTestSetField(
                    templateName,
                    jsonContent.get("testSetName").textValue()
            );
        }
        else{
            return jsonContent.get("interleavingFields");
        }
        return valueFields;
    }
}