package com.serrala.sepa.parser;

import java.io.File;

import com.serrala.sepa.model.SepaStatement;

public interface StatementParser {
    SepaStatement parse(File file) throws Exception;
}
