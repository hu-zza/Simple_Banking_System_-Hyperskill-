package hu.zza.hyperskill.banking.db;

import hu.zza.hyperskill.banking.Account;


public class DB_Query {
    private final TransactionType transactionType;
    private final String[]        transactionDetails;
    private final Account         account;
    private final Account[]       additionalAccounts;
    
    
    public DB_Query(TransactionType transactionType, Account account) {
        this(transactionType, account, new String[0]);
    }
    
    
    public DB_Query(TransactionType transactionType,
                    Account account,
                    String[] transactionDetails,
                    Account... additionalAccounts) {
        this.transactionType    = transactionType;
        this.transactionDetails = transactionDetails.clone();
        this.account            = account;
        this.additionalAccounts = additionalAccounts.clone();
    }
    
    
    ///////////////////////////////////////////////////
    // GETTERS ONLY FOR THE PACKAGE-PRIVATE PROCESSING
    
    // TRANSACTION TYPE
    
    
    TransactionType getTransactionType() {
        return transactionType;
    }
    
    
    // OWNER ACCOUNT
    
    
    Account getAccount() {
        return account;
    }
    
    
    // TRANSACTION DETAILS
    
    
    String[] getTransactionDetails() {
        return transactionDetails.clone();
    }
    
    
    // ADDITIONAL ACCOUNTS
    
    
    Account[] getAdditionalAccounts() {
        return additionalAccounts.clone();
    }
}
