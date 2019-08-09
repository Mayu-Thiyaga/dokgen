package no.nav.familie.dokumentgenerator.dokgen.services;

import no.nav.familie.dokumentgenerator.dokgen.utils.FileManager;
import no.nav.familie.dokumentgenerator.dokgen.utils.JsonUtils;
import no.nav.familie.dokumentgenerator.dokgen.utils.RetrieveResources;
import org.everit.json.schema.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class TestSetService {
    private JsonUtils jsonUtils;
    private FileManager fileManager;
    private RetrieveResources resources;

    @Inject
    private void setJsonUtils(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

    @Inject
    private void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Inject
    private void setResources(RetrieveResources resources) {
        this.resources = resources;
    }


    public List<String> getTestdataNames(String templateName) {
        String path = fileManager.getContentRoot() + "templates/" + templateName + "/testdata/";
        return resources.retrieveFileNames(path);
    }


    public String getDefaultTestSet(String templateName) {
        String path = "./content/templates/" + templateName + "/TomtTestsett.json";
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEmptyTestSet(String templateName) {
        return jsonUtils.getEmptyTestData(templateName);
    }

    public ResponseEntity createTestSet(String templateName, String testSetName, String testSetContent) {
        String createdFileName;

        try {
            jsonUtils.validateTestData(templateName, testSetContent);
            createdFileName = fileManager.createNewTestSet(templateName, testSetName, testSetContent);
        } catch (ValidationException e) {
            System.out.println("e.toJSON().toString() = " + e.toJSON().toString());
            return new ResponseEntity<>(e.toJSON().toString(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>("Klarte ikke å lage nytt testsett!", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(createdFileName, HttpStatus.CREATED);
    }

}
