package com.serrala.sepa.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.model.SepaTransaction;

/**
 * Helper methods for {@link SepaStatement} objects.
 */
public class SepaStatementUtils {
    /**
     * Ensures that mandatory fields in the provided statement and its transactions
     * are populated. Random placeholder data will be used where values are missing.
     *
     * @param statement the statement to update
     */
    public static void ensureMandatoryFields(SepaStatement statement) {
        if (statement == null) {
            return;
        }
        if (isBlank(statement.getAccountIban())) {
            statement.setAccountIban(generateRandomIban());
        }
        if (isBlank(statement.getAccountCurrency())) {
            statement.setAccountCurrency("EUR");
        }
        if (isBlank(statement.getStatementId())) {
            statement.setStatementId(SepaStatement.generateRandomStatementId());
        }
        if (isBlank(statement.getCreationDateTime())) {
            statement.setCreationDateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        }
        if (statement.getTransactions() != null) {
            for (SepaTransaction tx : statement.getTransactions()) {
                if (isBlank(tx.getDebtorName())) {
                    tx.setDebtorName("UNKNOWN");
                }
                if (isBlank(tx.getDebtorIban())) {
                    tx.setDebtorIban(generateRandomIban());
                }
                if (isBlank(tx.getCreditorName())) {
                    tx.setCreditorName("UNKNOWN");
                }
                if (isBlank(tx.getCreditorIban())) {
                    tx.setCreditorIban(generateRandomIban());
                }
                if (isBlank(tx.getAmount())) {
                    tx.setAmount("0.00");
                }
                if (isBlank(tx.getCurrency())) {
                    tx.setCurrency("EUR");
                }
                if (isBlank(tx.getEndToEndId())) {
                    tx.setEndToEndId("ID" + ((int) (Math.random() * 1_000_000)));
                }
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String generateRandomIban() {
        String digits = String.format("%020d", (long) (Math.random() * 1_000_000_000_000_000_000L));
        return "DE" + digits;
    }
}
