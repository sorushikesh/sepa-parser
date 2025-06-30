package com.serrala.sepa.parser;

import java.io.File;

public class ParserFactory {
    public static StatementParser getParser(File file, String rootNamespace, boolean isMt101) {
        String fileNameLower = file.getName().toLowerCase();
        if ("urn:iso:std:iso:20022:tech:xsd:pain.001.001.09".equals(rootNamespace)) {
            // Use streaming parser for pain.001 files
            return new SepaCtPain001StaxParser();
        } else if ("urn:iso:std:iso:20022:tech:xsd:pain.008.001.08".equals(rootNamespace)) {
            return new SepaDdPain008Parser();
        } else if (isMt101 || fileNameLower.endsWith(".rft")) {
            return new Mt101Parser();
        } else {
            throw new IllegalArgumentException("Unsupported SEPA/MT file type or namespace: " + rootNamespace);
        }
    }
}
