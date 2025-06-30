package com.serrala.sepa.parser;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.model.SepaTransaction;

/**
 * Streaming parser for pain.001.001.09 using StAX.
 */
public class SepaCtPain001StaxParser implements StatementParser {
    private static final String NS = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.09";

    @Override
    public SepaStatement parse(File xmlFile) throws Exception {
        SepaStatement statement = new SepaStatement();
        List<SepaTransaction> transactions = new ArrayList<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XMLEventReader reader = factory.createXMLEventReader(fis);
            Deque<String> path = new ArrayDeque<>();
            SepaTransaction currentTx = null;
            String lastText = null;
            String instdAmtCurrency = null;
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    StartElement start = event.asStartElement();
                    String local = start.getName().getLocalPart();
                    path.push(local);
                    if ("CdtTrfTxInf".equals(local)) {
                        currentTx = new SepaTransaction();
                    } else if ("InstdAmt".equals(local)) {
                        Attribute attr = start.getAttributeByName(javax.xml.namespace.QName.valueOf("Ccy"));
                        if (attr != null) {
                            instdAmtCurrency = attr.getValue();
                        }
                    }
                    lastText = "";
                } else if (event.isCharacters()) {
                    Characters chars = event.asCharacters();
                    if (!chars.isWhiteSpace()) {
                        lastText += chars.getData();
                    }
                } else if (event.isEndElement()) {
                    EndElement end = event.asEndElement();
                    String local = end.getName().getLocalPart();
                    String parent = path.size() > 1 ? path.toArray(new String[0])[1] : null;
                    if (lastText != null && !lastText.isEmpty()) {
                        if (currentTx == null) {
                            // Statement level fields
                            if ("IBAN".equals(local) && "DbtrAcct".equals(parent)) {
                                statement.setAccountIban(lastText.trim());
                            } else if ("Ccy".equals(local) && "DbtrAcct".equals(parent)) {
                                statement.setAccountCurrency(lastText.trim());
                            }
                        } else {
                            // Transaction fields
                            if ("Nm".equals(local) && "Dbtr".equals(parent)) {
                                currentTx.setDebtorName(lastText.trim());
                            } else if ("Nm".equals(local) && "Cdtr".equals(parent)) {
                                currentTx.setCreditorName(lastText.trim());
                            } else if ("IBAN".equals(local) && "DbtrAcct".equals(parent)) {
                                currentTx.setDebtorIban(lastText.trim());
                            } else if ("IBAN".equals(local) && "CdtrAcct".equals(parent)) {
                                currentTx.setCreditorIban(lastText.trim());
                            } else if ("InstdAmt".equals(local)) {
                                currentTx.setAmount(lastText.trim());
                                if (instdAmtCurrency != null) {
                                    currentTx.setCurrency(instdAmtCurrency);
                                    instdAmtCurrency = null;
                                }
                            } else if ("EndToEndId".equals(local)) {
                                currentTx.setEndToEndId(lastText.trim());
                            } else if ("Ustrd".equals(local)) {
                                currentTx.setRemittanceInfo(lastText.trim());
                            } else if ("Cd".equals(local) && "Purp".equals(parent)) {
                                currentTx.setPurposeCode(lastText.trim());
                            }
                        }
                    }
                    if ("CdtTrfTxInf".equals(local) && currentTx != null) {
                        transactions.add(currentTx);
                        currentTx = null;
                    }
                    path.pop();
                    lastText = "";
                }
            }
        }
        if ((statement.getAccountIban() == null || statement.getAccountIban().isEmpty()) && !transactions.isEmpty()) {
            statement.setAccountIban(transactions.get(0).getDebtorIban());
        }
        if ((statement.getAccountCurrency() == null || statement.getAccountCurrency().isEmpty()) && !transactions.isEmpty()) {
            statement.setAccountCurrency(transactions.get(0).getCurrency());
        }
        statement.setTransactions(transactions);
        return statement;
    }
}
