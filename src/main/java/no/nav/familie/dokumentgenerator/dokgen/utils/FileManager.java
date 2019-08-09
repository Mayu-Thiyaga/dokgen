package no.nav.familie.dokumentgenerator.dokgen.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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

    private static String contentRoot;

    private static void writeToFile(String folder, String fileName, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(
                        new File(contentRoot + "templates/" + folder + "/" + fileName).getPath()
                )
        );
        writer.append(content);
        writer.close();
    }

    @Value("${path.content.root}")
    public void setContentRoot(String cr) {
        contentRoot = cr;
        System.out.println("contentRoot = " + contentRoot);
    }

    public static String getContentRoot() {
        return contentRoot;
    }

    public static void saveTemplateFile(String templateName, String markdownContent) {
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

    public static String createNewTestSet(String templateName, String testSetName, String testSetContent) throws IOException {
        String path = contentRoot + "templates/" + templateName + "/testdata/" + testSetName + ".json";
        Path newFilePath = Paths.get(path);
        Files.write(newFilePath, testSetContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        return testSetName;
    }
}
