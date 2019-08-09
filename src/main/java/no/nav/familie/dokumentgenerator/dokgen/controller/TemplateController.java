package no.nav.familie.dokumentgenerator.dokgen.controller;


import io.swagger.annotations.ApiOperation;
import no.nav.familie.dokumentgenerator.dokgen.services.TestSetService;
import no.nav.familie.dokumentgenerator.dokgen.utils.FileManager;
import org.json.JSONObject;

import no.nav.familie.dokumentgenerator.dokgen.services.TemplateService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000"})
@RestController
public class TemplateController {

    private TemplateService templateManagementService;
    private TestSetService testSetService;

    @Inject
    public TemplateController(FileManager fileManager, TemplateService templateService, TestSetService testSetService) {
        this.templateManagementService = templateService;
        this.testSetService = testSetService;
    }

    @GetMapping("/mal/alle")
    @ApiOperation(value = " Få en oversikt over alle malforslagene")
    public List<String> getAllTemplateNames() {
        return templateManagementService.getTemplateSuggestions();
    }

    @GetMapping(value = "/mal/{templateName}", produces = "text/plain")
    @ApiOperation(value = "Hent malen i markdown")
    public String getTemplateContentInMarkdown(@PathVariable String templateName) {
        return templateManagementService.getMarkdownTemplate(templateName);
    }


    @PostMapping(value = "/mal/{format}/{templateName}", consumes = "application/json")
    @ApiOperation(
            value = "Generer malen i ønsket format",
            notes = "Støttede formater er <b>html</b> og <b>pdf</b>, hvor PDF er av versjonen PDF/A"
    )
    public ResponseEntity setTemplateContent(@PathVariable String format,
                                             @PathVariable String templateName,
                                             @RequestBody String payload) {
        return templateManagementService.returnLetterResponse(
                format,
                templateName,
                payload,
                true
        );
    }


    @ConditionalOnProperty(prefix="my.controller", name="enabled")
    @PutMapping(value = "/mal/{format}/{templateName}", consumes = "application/json")
    @ApiOperation(value = "")
    public ResponseEntity updateTemplateContent(@PathVariable String format,
                                                @PathVariable String templateName,
                                                @RequestBody String payload) {
        return templateManagementService.saveAndReturnTemplateResponse(
                format,
                templateName,
                payload,
                true
        );
    }


    @GetMapping(value = "mal/{templateName}/testdata")
    @ApiOperation(value = "Hent de forskjellige testdataene for spesifikk mal")
    public ResponseEntity<List<String>> getTestData(@PathVariable String templateName) {

        List<String> response = testSetService.getTestdataNames(templateName);

        if (response == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(testSetService.getTestdataNames(templateName), HttpStatus.OK);
    }


    @GetMapping(value = "mal/{templateName}/tomtTestSett", produces = "application/json")
    @ApiOperation(value = "Hent et tomt testsett for malen som kan fylles ut")
    public ResponseEntity<String> getEmptyTestSet(@PathVariable String templateName) {
        return new ResponseEntity<>(testSetService.getEmptyTestSet(templateName), HttpStatus.OK);
    }


    @PostMapping(value = "mal/{templateName}/nyttTestSett", consumes = "application/json", produces = "application/json")
    @ApiOperation(
            value = "Lag et nytt testsett for en mal",
            notes = "For å generere et tomt testsett må oppsettet i payloaden følge lignende struktur: \n" +
                    "{" +
                    "\n\"content\": {" +
                    "\n\"begrunnelse\": \"BEGRUNNELSE\"," +
                    "\n\"paragraf\": \"10, 11\"," +
                    "\n\"enhet\": \"ENHET\"," +
                    "\n\"saksbehandler\": \"Ola Nordmann\"\n" +
                    "},\n" +
                    "\"name\": \"Navn på testsettet\"\n" +
                    "}"
    )
    public ResponseEntity createNewTestSet(@PathVariable String templateName, @RequestBody String payload) {
        JSONObject obj = new JSONObject(payload);
        String testSetName = obj.getString("name");
        String testSetContent = obj.getString("content");
        return testSetService.createTestSet(templateName, testSetName, testSetContent);
    }


    @PostMapping(value = "/brev/{format}/{templateName}", consumes = "application/json", produces = "text/html")
    @ApiOperation(value = "")
    public ResponseEntity getTemplateContentInHtml(@PathVariable String format,
                                                   @PathVariable String templateName,
                                                   @RequestBody String payload) {
        return templateManagementService.returnLetterResponse(
                format,
                templateName,
                payload,
                false
        );
    }
}
