package com.serrala.sepa.util;

import com.serrala.sepa.model.SepaStatement;
import com.serrala.sepa.model.SepaTransaction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Mt940Generator {
    public static String generate(SepaStatement statement) {
        StringBuilder sb = new StringBuilder();
        // Header
        sb.append(":20:").append(statement.getStatementId() != null ? statement.getStatementId() : "STMT-001").append("\n");
        sb.append(":25:").append(statement.getAccountIban() != null ? statement.getAccountIban() : "").append("\n");
        sb.append(":28C:1\n");
        // Opening balance
        String openDate = statement.getPeriodFrom() != null ? statement.getPeriodFrom().replace("-","") : new SimpleDateFormat("yyMMdd").format(new Date());
        sb.append(":60F:C0,00EUR" + openDate + "\n");
        // Transactions
        List<SepaTransaction> txs = statement.getTransactions();
        int seq = 1;
        for (SepaTransaction tx : txs) {
            String date = openDate;
            String amount = tx.getAmount() != null ? tx.getAmount().replace(".", ",") : "0,00";
            sb.append(":61:").append(date).append("C").append(amount).append("NTRF").append(tx.getEndToEndId() != null ? tx.getEndToEndId() : "").append("\n");
            sb.append(":86:").append(tx.getRemittanceInfo() != null ? tx.getRemittanceInfo() : "").append(" ");
            sb.append("DBTR:").append(tx.getDebtorName() != null ? tx.getDebtorName() : "").append(" ");
            sb.append("CDTR:").append(tx.getCreditorName() != null ? tx.getCreditorName() : "").append("\n");
            seq++;
        }
        // Closing balance
        String closeDate = statement.getPeriodTo() != null ? statement.getPeriodTo().replace("-","") : new SimpleDateFormat("yyMMdd").format(new Date());
        sb.append(":62F:C0,00EUR" + closeDate + "\n");
        return sb.toString();
    }
}
