package banking;


class DataBaseQuery
{
    private final TransactionType transactionType;
    private final String[]        transactionDetails;
    private final Account         account;
    private final Account[]       additionalAccounts;
    
    DataBaseQuery(TransactionType transactionType, Account account)
    {
        this(transactionType, account, new String[0]);
    }
    
    DataBaseQuery(TransactionType transactionType,
                  Account account,
                  String[] transactionDetails,
                  Account... additionalAccounts
    )
    {
        this.transactionType    = transactionType;
        this.transactionDetails = transactionDetails.clone();
        this.account            = account;
        this.additionalAccounts = additionalAccounts.clone();
    }
    
    
    // TRANSACTION TYPE
    
    TransactionType getTransactionType()
    {
        return transactionType;
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
    
    
    // OWNER ACCOUNT
    
    Account getAccount()
    {
        return account;
    }
    
    
    // ADDITIONAL ACCOUNTS
    
    Account[] getAdditionalAccounts()
    {
        return additionalAccounts.clone();
    }
    
    int countOfAdditionalAccounts()
    {
        return additionalAccounts.length;
    }
}
