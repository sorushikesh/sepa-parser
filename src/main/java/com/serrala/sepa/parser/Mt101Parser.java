package com.serrala.sepa.parser;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.model.SepaTransaction;

public class Mt101Parser implements StatementParser {
    private void processDebtorBlock(SepaTransaction currentTx, String debtorBlockStr) {
        if (currentTx == null || debtorBlockStr == null) return;
        String[] linesArr = debtorBlockStr.split("\\n");
        boolean nameSet = false;
        boolean ibanSet = currentTx.getDebtorIban() != null && !currentTx.getDebtorIban().isEmpty();
        // Always check the first line for IBAN/account number
        if (!ibanSet && linesArr.length > 0 && linesArr[0].startsWith("/")) {
            String possibleAcct = linesArr[0].substring(1).trim();
            if (!possibleAcct.isEmpty()) {
                currentTx.setDebtorIban(possibleAcct);
                ibanSet = true;
            }
        }
        for (int idx = 0; idx < linesArr.length; idx++) {
            String l = linesArr[idx];
            if (idx == 0 && l.startsWith("/")) continue; // already handled
            if (!ibanSet && l.startsWith("/")) {
                String possibleAcct = l.substring(1).trim();
                if (!possibleAcct.isEmpty()) {
                    currentTx.setDebtorIban(possibleAcct);
                    ibanSet = true;
                }
            } else if (!l.isEmpty() && !nameSet) {
                // Remove leading numeric prefix and slash (e.g., '1/' or '/')
                String normalized = l.replaceFirst("^\\d+/", "").replaceFirst("^/", "").trim();
                currentTx.setDebtorName(normalized);
                nameSet = true;
            }
        }
        // Do not set UNKNOWN if missing
    }

    private void processCreditorBlock(SepaTransaction currentTx, String creditorBlockStr) {
        if (currentTx == null || creditorBlockStr == null) return;
        String[] linesArr = creditorBlockStr.split("\\n");
        boolean nameSet = false;
        for (String l : linesArr) {
            if (l.startsWith("/")) {
                String possibleAcct = l.substring(1).trim();
                if (!possibleAcct.isEmpty()) {
                    currentTx.setCreditorIban(possibleAcct);
                }
            } else if (!l.isEmpty() && !nameSet) {
                // Remove leading numeric prefix and slash (e.g., '1/' or '/')
                String normalized = l.replaceFirst("^\\d+/", "").replaceFirst("^/", "").trim();
                currentTx.setCreditorName(normalized);
                nameSet = true;
            }
        }
        // Do not set UNKNOWN if missing
    }

    @Override
    public SepaStatement parse(File mt101File) throws Exception {
        List<String> lines = Files.readAllLines(mt101File.toPath());
        SepaStatement statement = new SepaStatement();
        List<SepaTransaction> transactions = new ArrayList<>();
        String accountIban = null;
        String statementCurrency = null;
        SepaTransaction pendingTx = null;
        // Add pendingCreditorBlock/pendingCreditorIban for correct association
        StringBuilder pendingCreditorBlock = new StringBuilder();
        StringBuilder currentDebtorBlock = new StringBuilder();
        String currentDebtorIban = null; // persist across transactions
        String currentPurpose = null;
        String currentRemittance = null;
        String lastRemittance = null;
        String lastPurpose = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmedLine = line.trim();
            // Sender/Ordering Customer block (can be :50A:, :50H:, :50K:, :50L:)
            if (trimmedLine.matches(":50[A-Z]?:.*")) {
                currentDebtorBlock.setLength(0);
                // Only update currentDebtorIban if a new :50: block is found
                String content = trimmedLine.replaceFirst(":50[A-Z]?:", "").trim();
                if (content.startsWith("/")) {
                    String possibleAcct = content.substring(1).trim();
                    if (!possibleAcct.isEmpty()) {
                        currentDebtorIban = possibleAcct;
                    }
                    currentDebtorBlock.append(content).append("\n");
                } else if (!content.isEmpty()) {
                    currentDebtorBlock.append(content).append("\n");
                }
                int j = i + 1;
                while (j < lines.size() && !lines.get(j).trim().startsWith(":")) {
                    currentDebtorBlock.append(lines.get(j).trim()).append("\n");
                    j++;
                }
                i = j - 1;
                continue;
            }
            // Beneficiary/Receiver block (can be :59:, :59A:, :59F:)
            if (trimmedLine.matches(":59[A-Z]?:.*")) {
                // Buffer the creditor block for the next transaction
                pendingCreditorBlock.setLength(0);
                String content = trimmedLine.replaceFirst(":59[A-Z]?:", "").trim();
                if (!content.isEmpty()) {
                    pendingCreditorBlock.append(content).append("\n");
                }
                int j = i + 1;
                while (j < lines.size() && !lines.get(j).trim().startsWith(":")) {
                    pendingCreditorBlock.append(lines.get(j).trim()).append("\n");
                    j++;
                }
                i = j - 1;
                continue;
            }
            if (trimmedLine.startsWith(":21:")) {
                if (pendingTx != null) {
                    // Propagate last non-empty remittance/purpose if missing
                    if ((pendingTx.getRemittanceInfo() == null || pendingTx.getRemittanceInfo().isEmpty()) && lastRemittance != null) {
                        pendingTx.setRemittanceInfo(lastRemittance);
                    }
                    if ((pendingTx.getPurposeCode() == null || pendingTx.getPurposeCode().isEmpty()) && lastPurpose != null) {
                        pendingTx.setPurposeCode(lastPurpose);
                    }
                    // Do not set UNKNOWN if missing
                    transactions.add(pendingTx);
                }
                // Before starting a new transaction, assign any pending creditor block to the previous transaction if it has UNKNOWN creditor
                if (!transactions.isEmpty() && pendingCreditorBlock.length() > 0) {
                    SepaTransaction lastTx = transactions.get(transactions.size() - 1);
                    if ((lastTx.getCreditorName() == null || lastTx.getCreditorName().equals("UNKNOWN")) && (lastTx.getCreditorIban() == null || lastTx.getCreditorIban().equals("UNKNOWN"))) {
                        processCreditorBlock(lastTx, pendingCreditorBlock.toString());
                    }
                    pendingCreditorBlock.setLength(0);
                }
                pendingTx = new SepaTransaction();
                pendingTx.setEndToEndId(trimmedLine.substring(4).trim());
                // Debtor
                if (currentDebtorIban != null && !currentDebtorIban.isEmpty()) {
                    pendingTx.setDebtorIban(currentDebtorIban);
                }
                if (currentDebtorBlock.length() > 0) {
                    processDebtorBlock(pendingTx, currentDebtorBlock.toString());
                }
                // Creditor: assign pending block if present (should be empty here)
                if (pendingCreditorBlock.length() > 0) {
                    processCreditorBlock(pendingTx, pendingCreditorBlock.toString());
                    pendingCreditorBlock.setLength(0);
                }
                // Do not set UNKNOWN if missing
                continue;
            }
            // Amount, currency, and remittance info
            if (trimmedLine.startsWith(":32B:")) {
                String val = trimmedLine.substring(5).trim();
                if (val.length() >= 3 && pendingTx != null) {
                    String currency = val.substring(0, 3);
                    String amount = val.substring(3).replace(",", ".").trim();
                    pendingTx.setAmount(amount.isEmpty() ? "0.00" : amount);
                    pendingTx.setCurrency(currency);
                    statementCurrency = currency;
                }
            } else if (trimmedLine.startsWith(":70:")) {
                if (pendingTx != null) {
                    String rem = trimmedLine.substring(4).trim();
                    pendingTx.setRemittanceInfo(rem);
                    if (!rem.isEmpty()) lastRemittance = rem;
                }
            } else if (trimmedLine.startsWith(":23E:")) {
                if (pendingTx != null) {
                    String purp = trimmedLine.substring(5).trim();
                    pendingTx.setPurposeCode(purp);
                    if (!purp.isEmpty()) lastPurpose = purp;
                }
            } else if (trimmedLine.startsWith(":25:")) {
                accountIban = trimmedLine.substring(4).trim();
            }
        }
        // After loop, finalize and add the last transaction if present
        if (pendingTx != null) {
            if ((pendingTx.getRemittanceInfo() == null || pendingTx.getRemittanceInfo().isEmpty()) && lastRemittance != null) {
                pendingTx.setRemittanceInfo(lastRemittance);
            }
            if ((pendingTx.getPurposeCode() == null || pendingTx.getPurposeCode().isEmpty()) && lastPurpose != null) {
                pendingTx.setPurposeCode(lastPurpose);
            }
            // Do not set UNKNOWN if missing
            transactions.add(pendingTx);
        }
        // After loop, assign any pending creditor block to the last transaction if it still has UNKNOWN creditor
        if (!transactions.isEmpty() && pendingCreditorBlock.length() > 0) {
            SepaTransaction lastTx = transactions.get(transactions.size() - 1);
            if ((lastTx.getCreditorName() == null || lastTx.getCreditorName().equals("UNKNOWN")) && (lastTx.getCreditorIban() == null || lastTx.getCreditorIban().equals("UNKNOWN"))) {
                processCreditorBlock(lastTx, pendingCreditorBlock.toString());
            }
        }
        // After loop, assign any pending blocks to the last transaction (if any blocks remain)
        if (pendingTx != null) {
            if (currentDebtorIban != null && !currentDebtorIban.isEmpty()) {
                pendingTx.setDebtorIban(currentDebtorIban);
            }
            if (currentDebtorBlock.length() > 0) {
                processDebtorBlock(pendingTx, currentDebtorBlock.toString());
            }
            if (pendingCreditorBlock.length() > 0) {
                processCreditorBlock(pendingTx, pendingCreditorBlock.toString());
                pendingCreditorBlock.setLength(0);
            }
            // Do not set UNKNOWN if missing
            if (currentPurpose != null && !currentPurpose.isEmpty()) {
                pendingTx.setPurposeCode(currentPurpose);
            }
            if (currentRemittance != null && !currentRemittance.isEmpty()) {
                pendingTx.setRemittanceInfo(currentRemittance);
            }
        }
        // Only fallback if BOTH debtor IBAN and debtor name are missing, and creditor fields are present
        String fallbackIban = null;
        for (SepaTransaction tx : transactions) {
            boolean missingDebtor = (tx.getDebtorIban() == null || tx.getDebtorIban().isEmpty()) && (tx.getDebtorName() == null || tx.getDebtorName().isEmpty());
            boolean hasCreditor = (tx.getCreditorIban() != null && !tx.getCreditorIban().isEmpty()) && (tx.getCreditorName() != null && !tx.getCreditorName().isEmpty());
            if (missingDebtor && hasCreditor) {
                tx.setDebtorIban(tx.getCreditorIban());
                tx.setDebtorName(tx.getCreditorName());
            }
            System.out.println("DEBUG: TX DebtorIban=" + tx.getDebtorIban() + ", CreditorIban=" + tx.getCreditorIban() + ", DebtorName=" + tx.getDebtorName() + ", CreditorName=" + tx.getCreditorName());
            if (tx.getDebtorIban() != null && !tx.getDebtorIban().isEmpty()) {
                fallbackIban = tx.getDebtorIban();
                break;
            }
            if (tx.getCreditorIban() != null && !tx.getCreditorIban().isEmpty()) {
                fallbackIban = tx.getCreditorIban();
                break;
            }
        }
        if (fallbackIban != null && (accountIban == null || accountIban.isEmpty())) {
            accountIban = fallbackIban;
        }
        statement.setAccountIban(accountIban);
        statement.setAccountCurrency(statementCurrency);
        if (statement.getAccountCurrency() == null || statement.getAccountCurrency().isEmpty()) statement.setAccountCurrency("EUR");
        statement.setTransactions(transactions);
        System.out.println("DEBUG: Final IBAN = " + statement.getAccountIban());
        return statement;
    }
}
