package no.nav.familie.dokumentgenerator.dokgen.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Component
public class RetrieveResources {

    private static final Logger LOG = LoggerFactory.getLogger(RetrieveResources.class);

    public String getTemplatePath(String templateName) {
        return String.format("./content/templates/%1$s/%1$s.hbs", templateName);
    }

    public List<String> getTemplateNames(String path) {
        List<String> names = new ArrayList<>();
        File folder;
        File[] listOfFiles;
        folder = new File(path);
        listOfFiles = folder.listFiles();

        if (listOfFiles == null) {
            return null;
        }

        for (File file : listOfFiles) {
            names.add(FilenameUtils.getBaseName(file.getName()));
        }

        return names;
    }

     String getCss(String cssName){
        try {
            return new String(Files.readAllBytes(Paths.get("./content/assets/css/" + cssName + ".css")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Kunne ikke åpne template malen");
        }
        return null;
    }

    public String getEmptyTestData(String templateName) {
        String path = "./content/templates/" + templateName + "/TomtTestsett.json";
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
