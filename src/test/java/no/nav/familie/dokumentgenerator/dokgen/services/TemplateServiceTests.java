package no.nav.familie.dokumentgenerator.dokgen.services;

import no.nav.familie.dokumentgenerator.dokgen.utils.FileManager;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TemplateServiceTests {

    private TemplateService templateService;
    private static String contentRoot;
    private static FileManager fileManager;

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder(new File("./"));


    @Inject
    private void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Inject
    private void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    private static void copyAssetsToContentRoot() throws IOException {
        File srcDir = new File("./src/test/resources/test-assets/content/assets");
        File destDir = new File(contentRoot + "/assets");

        try {
            org.apache.commons.io.FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTestTemplateToContentRoot(String name, String content) throws IOException {
        org.apache.commons.io.FileUtils.writeStringToFile(
                new File(contentRoot + "/templates/" + name + "/" + name + ".hbs"),
                content,
                StandardCharsets.UTF_8
        );
    }

    @BeforeClass
    public static void setup() throws IOException {

        File contentFolder = temporaryFolder.newFolder("content");
        contentRoot = temporaryFolder.getRoot() + "/" + contentFolder.getName() + "/";

        copyAssetsToContentRoot();
        FileManager fileManager = new FileManager();
        ReflectionTestUtils.setField(fileManager, "contentRoot", contentRoot);
    }

    @AfterClass
    public static void tearDown() {
        ReflectionTestUtils.setField(fileManager, "contentRoot", "./content/");
    }


    @Test
    public void testSavingTemplateShouldReturnConvertedTemplateToHtml() throws IOException {
        String templateName = "testName";
        String markdownContent = "\"#Hei, {{name}}\"";
        String interleavingFields = "{\"name\": \"Peter\"}";
        writeTestTemplateToContentRoot(templateName, "#Hallo, {{name}}");
        ResponseEntity res = templateService.saveAndReturnTemplateResponse(
                "html",
                templateName,
                "{\"markdownContent\": " + markdownContent + ", \"interleavingFields\": " + interleavingFields + "}",
                false
        );

        Assert.assertEquals(HttpStatus.OK, res.getStatusCode());
        assertThat("Body", (String) res.getBody(), containsString("#Hei, Peter"));
        assertThat("Body", (String) res.getBody(), not(containsString("#Hallo, {{name}}")));
    }

    @Test
    public void testGetTemplateSuggestionsShouldReturnCorrectTemplateNames() throws IOException {
        writeTestTemplateToContentRoot("Tem1", "1");
        writeTestTemplateToContentRoot("Tem2", "2");
        writeTestTemplateToContentRoot("Tem3", "3");

        List<String> expectedResult = new ArrayList<>() {
            {
                add("Tem1");
                add("Tem2");
                add("Tem3");
            }
        };

        List<String> actualResult = templateService.getTemplateSuggestions();
        Assert.assertEquals(expectedResult.toArray().length, actualResult.toArray().length);
    }
}
