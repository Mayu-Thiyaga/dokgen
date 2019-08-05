package no.nav.familie.dokumentgenerator.dokgen.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


@Component
public class FileManager {

    private static FileManager single_instance = null;
    private final String contentRoot;


    private FileManager(){
        this.contentRoot = "./content/";
    }

    private FileManager(String contentRoot){
        this.contentRoot = contentRoot;
    }

    private void writeToFile(String folder, String fileName, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(
                        new File(this.getContentRoot() + "templates/" + folder + "/" + fileName).getPath()
                )
        );
        writer.append(content);
        writer.close();
    }


    public static FileManager getInstance() {
        if (single_instance == null)
            single_instance = new FileManager();

        return single_instance;
    }

    public static FileManager getInstance(String contentRoot) {
        if (single_instance == null)
            single_instance = new FileManager(contentRoot);

        return single_instance;
    }

    public String getContentRoot() {
        return contentRoot;
    }

    public void saveTemplateFile(String templateName, String markdownContent) {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        String strippedHtmlSyntax = Jsoup.clean(
                markdownContent,
                "",
                Whitelist.none(),
                settings
        );

        try {
            String fileName = templateName + ".hbs";
            writeToFile(templateName, fileName, strippedHtmlSyntax);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    String getCss(String cssName){
        try {
            return new String(Files.readAllBytes(Paths.get(this.getContentRoot() + "assets/css/" + cssName + ".css")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Kunne ikke åpne template malen");
        }
        return null;
    }


    public String createNewTestSet(String templateName, String testSetName, String testSetContent) throws IOException {
        String path = this.getContentRoot() + "templates/" + templateName + "/testdata/" + testSetName + ".json";
        Path newFilePath = Paths.get(path);
        Files.write(newFilePath, testSetContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        return testSetName;
    }
}
