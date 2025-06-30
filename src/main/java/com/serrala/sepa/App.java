package com.serrala.sepa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serrala.sepa.parser.StatementParser;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main( String[] args ) {
        try {
            if (args.length < 1) {
                logger.info("Usage: java -jar sepa-parser.jar <pain.001.xml>");
                return;
            }
            java.io.File xmlFile = new java.io.File(args[0]);
            if (!xmlFile.exists() || xmlFile.length() == 0) {
                logger.error("File not found or is empty: {}", xmlFile.getAbsolutePath());
                return;
            }
            logger.info("File found: {}, size: {} bytes", xmlFile.getAbsolutePath(), xmlFile.length());
            java.nio.file.Files.lines(xmlFile.toPath()).limit(10).forEach(logger::info);

            // Detect file type and use appropriate parser via Factory/Strategy
            String rootNamespace = null;
            boolean isMt101 = false;
            try {
                rootNamespace = extractRootNamespace(xmlFile);
            } catch (Exception e) {
                logger.debug("XML parse error: {}", e.getMessage());
                isMt101 = isMt101File(xmlFile);
            }
            StatementParser parser = com.serrala.sepa.parser.ParserFactory.getParser(xmlFile, rootNamespace, isMt101);
            com.serrala.sepa.model.SepaStatement statement = parser.parse(xmlFile);

            logger.info("Account IBAN: {}", statement.getAccountIban());
            logger.info("Account Currency: {}", statement.getAccountCurrency());
            logger.info("Transactions: {}", statement.getTransactions().size());

            com.serrala.sepa.util.StatementOutputWriter.writeAllOutputs(statement);
        } catch (IllegalArgumentException e) {
            logger.error("Error: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
        }
    }

    // Helper to extract the root XML namespace from a file
    public static String extractRootNamespace(java.io.File xmlFile) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(xmlFile);
        return doc.getDocumentElement().getNamespaceURI();
    }

    // Helper to check if a file is an MT101 file by scanning for key tags
    public static boolean isMt101File(java.io.File file) throws Exception {
        java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
        for (String line : lines) {
            if (line.startsWith(":20:") || line.startsWith(":25:") || line.startsWith(":21:") || line.startsWith(":32B:")) {
                return true;
            }
        }
        return false;
    }

    public static String extractRootNamespace(java.io.InputStream inputStream) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(inputStream);
        return doc.getDocumentElement().getNamespaceURI();
    }

    public static boolean isMt101File(java.io.InputStream inputStream) throws Exception {
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(":20:") || line.startsWith(":25:") || line.startsWith(":21:") || line.startsWith(":32B:")) {
                return true;
            }
        }
        return false;
    }
}
