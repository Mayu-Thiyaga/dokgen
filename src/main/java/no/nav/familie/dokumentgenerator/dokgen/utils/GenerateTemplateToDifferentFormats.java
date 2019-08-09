package no.nav.familie.dokumentgenerator.dokgen.utils;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import org.apache.commons.io.IOUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class GenerateTemplateToDifferentFormats {

    private RetrieveResources resources;

    private Node parseMarkdown(String content) {
        Parser parser = Parser.builder().build();
        return parser.parse(content);
    }

    private String renderToHTML(Node document) {
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    private String convertMarkdownTemplateToHtml(String content) {
        Node document = parseMarkdown(content);
        return renderToHTML(document);
    }

    @Inject
    public GenerateTemplateToDifferentFormats(RetrieveResources resources) {
        this.resources = resources;
    }

    public void addDocumentParts(Document document){
        String resourceLocation = FileManager.getContentRoot() + "assets/htmlParts/";
        try{

            String header = new String(Files.readAllBytes(Paths.get(resourceLocation + "headerTemplate.html")));
            String footer = new String(Files.readAllBytes(Paths.get(resourceLocation + "footerTemplate.html")));

            Element body = document.body();
            body.prepend(header);
            body.append(footer);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public Document appendHtmlMetadata(String html, String cssName) {
        String convertedTemplate = convertMarkdownTemplateToHtml(html);

        Document document = Jsoup.parse(("<div id=\"content\">" + convertedTemplate + "</div>"));
        Element head = document.head();

        head.append("<meta charset=\"UTF-8\">");
        head.append("<style>" + resources.getCss(cssName) + "</style>");

        return document;
    }

    public void generatePDF(Document html, ByteArrayOutputStream outputStream) {
        org.w3c.dom.Document doc = new W3CDom().fromJsoup(html);

        PdfRendererBuilder builder = new PdfRendererBuilder();
        try{
            byte[] colorProfile = IOUtils.toByteArray(new FileInputStream(FileManager.getContentRoot() + "assets/sRGB2014.icc"));

            builder
                    .useFont(
                            new File(FileManager.getContentRoot() + "assets/fonts/fontpack/SourceSansPro-Regular.ttf"),
                            "Source Sans Pro",
                            400,
                            BaseRendererBuilder.FontStyle.NORMAL,
                            false
                    )
                    .useFont(
                            new File(FileManager.getContentRoot() + "assets/fonts/fontpack/SourceSansPro-Bold.ttf"),
                            "Source Sans Pro",
                            700,
                            BaseRendererBuilder.FontStyle.OBLIQUE,
                            false
                    )
                    .useFont(
                            new File(FileManager.getContentRoot() + "assets/fonts/fontpack/SourceSansPro-Italic.ttf"),
                            "Source Sans Pro",
                            400,
                            BaseRendererBuilder.FontStyle.ITALIC,
                            false
                    )
                    .useColorProfile(colorProfile)
                    .useSVGDrawer(new BatikSVGDrawer())
                    .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
                    .withW3cDocument(doc, "")
                    .toStream(outputStream)
                    .buildPdfRenderer()
                    .createPDF();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
