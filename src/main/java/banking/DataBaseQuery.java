package banking;

import java.util.Objects;

class DataBaseQuery
{
    private final banking.TransactionType transactionType;
    private final String[] transactionDetails;
    private final banking.Account account;
    private final banking.Account[] additionalAccounts;
    
    DataBaseQuery(banking.TransactionType transactionType, banking.Account account)
    {
        this(transactionType, account, new String[0]);
    }
    
    DataBaseQuery(banking.TransactionType transactionType, banking.Account account, String[] transactionDetails, banking.Account... additionalAccounts)
    {
        this.transactionType = transactionType;
        this.transactionDetails = transactionDetails.clone();
        this.account = account;
        this.additionalAccounts = additionalAccounts.clone();
    }
    
    
    // TRANSACTION TYPE
    
    banking.TransactionType getTransactionType()
    {
        return transactionType;
    }
    
    boolean isType(banking.TransactionType transactionType)
    {
        return Objects.equals(this.transactionType, transactionType);
    }
    
    
    
    // OWNER ACCOUNT
    
    banking.Account getAccount()
    {
        return account;
    }
    
    
    
    // TRANSACTION DETAILS
    
    String[] getTransactionDetails()
    {
        return transactionDetails.clone();
    }
    
    int countOfTransactionDetails()
    {
        return transactionDetails.length;
    }
    
    
    // ADDITIONAL ACCOUNTS
    
    banking.Account[] getAdditionalAccounts()
    {
        return additionalAccounts.clone();
    }
    
    int countOfAdditionalAccounts()
    {
        return additionalAccounts.length;
    }
}
