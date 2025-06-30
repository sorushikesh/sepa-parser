package com.serrala.sepa.util;

import java.io.File;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.parser.SepaCtPain001StaxParser;

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
        SepaStatementUtils.ensureMandatoryFields(statement);
        String camt53v3 = Camt053V3Generator.generate(statement);
        String camt53v8 = Camt053V8Generator.generate(statement);
        return new ConversionResult(camt53v3, camt53v8);
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
