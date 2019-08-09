package no.nav.familie.dokumentgenerator.dokgen.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class JsonUtils {

    private JsonNode readJsonFile(URI path) {
        if (path != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readTree(new File(path));
            } catch (IOException e) {
                System.out.println("Kan ikke finne JSON fil!");
                e.printStackTrace();
            }
        }
        return null;
    }

    private JsonNode getTestSetField(String templateName, String testSet) {
        URI path = Paths.get(FileManager.getContentRoot() + "templates/" + templateName + "/testdata/" + testSet + ".json").toUri();
        return readJsonFile(path);
    }

    public JsonNode getJsonFromString(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json);
    }

    public JsonNode extractInterleavingFields(String templateName, JsonNode jsonContent, boolean useTestSet) {
        JsonNode valueFields;
        if (useTestSet) {
            valueFields = getTestSetField(
                    templateName,
                    jsonContent.get("testSetName").textValue()
            );
        } else {
            return jsonContent.get("interleavingFields");
        }
        return valueFields;
    }

    public void validateTestData(String templateName, String json) throws ValidationException {
        String jsonSchemaLocation = "./content/templates/" + templateName + "/" + templateName + ".schema.json";
        try (InputStream inputStream = new FileInputStream(jsonSchemaLocation)) {

            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(new JSONObject(json)); // throws a ValidationException if this object is invalid
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getEmptyTestData(String templateName) {
        String path = FileManager.getContentRoot() + "templates/" + templateName + "/TomtTestsett.json";
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
