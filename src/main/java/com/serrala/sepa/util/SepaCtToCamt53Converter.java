package com.serrala.sepa.util;

import java.io.File;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.model.SepaTransaction;
import com.serrala.sepa.parser.SepaCtPain001StaxParser;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Utility class that converts SEPA Credit Transfer (pain.001) files into
 * CAMT053 statement XML strings. Missing mandatory CAMT fields are
 * automatically populated with placeholder values via
 * {@link SepaStatementUtils#ensureMandatoryFields(SepaStatement)}.
 */
public final class SepaCtToCamt53Converter {

    private SepaCtToCamt53Converter() {
        // utility class
    }

    /**
     * Parses the provided pain.001 file and generates CAMT053 statement strings
     * for versions 3 and 8.
     *
     * @param pain001File the SEPA Credit Transfer file to convert
     * @return result object containing the CAMT053 v3 and v8 XML strings
     * @throws Exception if parsing or generation fails
     */
    public static ConversionResult convert(File pain001File) throws Exception {
        SepaCtPain001StaxParser parser = new SepaCtPain001StaxParser();
        SepaStatement statement = parser.parse(pain001File);

        // Use creditor information for the statement account as required
        if (statement.getTransactions() != null && !statement.getTransactions().isEmpty()) {
            SepaTransaction tx = statement.getTransactions().get(0);
            if (tx.getCreditorIban() != null && !tx.getCreditorIban().isEmpty()) {
                statement.setAccountIban(tx.getCreditorIban());
            }
        }

        // Extract original message id from the pain file for statement id
        String msgId = extractMsgId(pain001File);
        if (msgId != null && !msgId.isEmpty()) {
            statement.setStatementId(msgId + "-STMT");
        }

        SepaStatementUtils.ensureMandatoryFields(statement);

        String camt53v3 = Camt053V3Generator.generate(statement);
        String camt53v8 = Camt053V8Generator.generate(statement);
        return new ConversionResult(camt53v3, camt53v8);
    }

    private static String extractMsgId(File pain001File) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(pain001File);
            org.w3c.dom.NodeList nodes = doc.getElementsByTagNameNS(
                    "urn:iso:std:iso:20022:tech:xsd:pain.001.001.09", "MsgId");
            if (nodes.getLength() > 0) {
                return nodes.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            // ignore and return null
        }
        return null;
    }

    /**
     * Holder for generated CAMT053 statement XML.
     */
    public static class ConversionResult {
        private final String camt53V3Xml;
        private final String camt53V8Xml;

        public ConversionResult(String camt53V3Xml, String camt53V8Xml) {
            this.camt53V3Xml = camt53V3Xml;
            this.camt53V8Xml = camt53V8Xml;
        }

        public String getCamt53V3Xml() {
            return camt53V3Xml;
        }

        public String getCamt53V8Xml() {
            return camt53V8Xml;
        }
    }
}
