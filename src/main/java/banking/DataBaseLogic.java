package banking;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

import static banking.DataBaseReply.ReplyType.AUTHENTICATED;
import static banking.DataBaseReply.ReplyType.AVAILABLE;
import static banking.DataBaseReply.ReplyType.CLOSED;
import static banking.DataBaseReply.ReplyType.CREATED;
import static banking.DataBaseReply.ReplyType.ERROR;
import static banking.DataBaseReply.ReplyType.EXISTS;
import static banking.DataBaseReply.ReplyType.MODIFIED;
import static banking.DataBaseReply.ReplyType.NOT_EXISTS;
import static banking.DataBaseReply.ReplyType.SYNCHRONIZED;
import static banking.DataBaseReply.ReplyType.TRANSFERRED;
import static banking.DataBaseReply.ReplyType.UPDATED;

abstract class DataBaseLogic
{

    // INSTANCE METHODS on database
    
    static DataBaseReply isExist(DataBase dataBase, DataBaseQuery dataBaseQuery)
    {
        var result = makeQuery(
                dataBase.getConnection(),
                String.format("SELECT * FROM card WHERE number = %s;", dataBaseQuery.getAccount().getCardNumber())
        );
        
        return result.size() == 0 ?
               new DataBaseReply(NOT_EXISTS) :
               new DataBaseReply(EXISTS);
    }
    
    
    static DataBaseReply addAccount(DataBase dataBase, DataBaseQuery dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        var result = makeUpdate(
                dataBase.getConnection(),
                String.format("INSERT INTO card (number, pin, balance) VALUES ('%s', '%s', %d);",
                              account.getCardNumber(),
                              account.getPinCode(),
                              account.getBalance()
                )
        );

        return result == 1 ?
               new DataBaseReply(CREATED) :
               new DataBaseReply(ERROR);
    }
    
    static DataBaseReply authenticateAccount(DataBase dataBase, DataBaseQuery dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        var result = makeQuery(
                dataBase.getConnection(),
                String.format("SELECT * FROM card WHERE number = '%s' AND pin = '%s';",
                              account.getCardNumber(),
                              account.getPinCode()
                )
        );
        
        if (result.size() == 1)
        {
            account.setDatabaseId(Integer.parseInt(result.get(0)[0]));
            return new DataBaseReply(AUTHENTICATED);
        }
        else
        {
            return new DataBaseReply(ERROR);
        }
    }
    
    static DataBaseReply synchronizeAccount(DataBase dataBase, DataBaseQuery dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        var result = makeQuery(
                dataBase.getConnection(),
                String.format("SELECT * FROM card WHERE id = '%d' AND number = '%s' AND pin = '%s';",
                              account.getDatabaseId(),
                              account.getCardNumber(),
                              account.getPinCode()
                )
        );
        
        if (result.size() == 1)
        {
            account.setBalance(Integer.parseInt(result.get(0)[3]));
            // Another setter callings...
            
            return new DataBaseReply(SYNCHRONIZED);
        }
        else
        {
            return new DataBaseReply(ERROR);
        }
    }
    
    static DataBaseReply updateAccount(DataBase dataBase, DataBaseQuery dataBaseQuery)
    {
        return new DataBaseReply(UPDATED, "This is feature is not implemented yet.");
    }
    
    static DataBaseReply deleteAccount(DataBase dataBase, DataBaseQuery dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        var result = makeUpdate(
                dataBase.getConnection(),
                String.format("DELETE FROM card WHERE id = %d AND number = '%s' AND pin = '%s';",
                              account.getDatabaseId(),
                              account.getCardNumber(),
                              account.getPinCode()
                )
        );
        
        return result == 1 ?
               new DataBaseReply(CLOSED) :
               new DataBaseReply(ERROR);
    }
    
    static DataBaseReply modifyBalance(DataBase dataBase, DataBaseQuery dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        // amountToAdd can be either positive (add) or negative (withdraw) number.
        int amountToAdd = Integer.parseInt(dataBaseQuery.getTransactionDetails()[0]);
        
        // WITHDRAWAL: Check if the account's balance has enough funds.
        if (amountToAdd < 0)
        {
            DataBaseReply reply = checkAvailableFunds(dataBase, account, amountToAdd);
            if (reply.isType(ERROR)) return reply;
        }
    
        var result = makeUpdate(
                dataBase.getConnection(),
                String.format("UPDATE card SET balance = balance + %d WHERE id = %d AND number = '%s' AND pin = '%s';",
                              amountToAdd,
                              account.getDatabaseId(),
                              account.getCardNumber(),
                              account.getPinCode()
                )
        );
        
        return result == 1 ?
               new DataBaseReply(MODIFIED) :
               new DataBaseReply(ERROR);
    }
    
    static DataBaseReply doTransfer(DataBase dataBase, DataBaseQuery dataBaseQuery)
    {
        Account ownerAccount = dataBaseQuery.getAccount();
        // banking.DataBaseQuery has capacity for multiple payee accounts...
        // But it is not implemented yet.
        Account payeeAccount =  dataBaseQuery.getAdditionalAccounts()[0];

        // amountToTransfer can be only a positive number.
        int amountToTransfer = Integer.parseInt(dataBaseQuery.getTransactionDetails()[0]);
    
        if (Objects.equals(ownerAccount.getCardNumber(), payeeAccount.getCardNumber()))
            return new DataBaseReply(ERROR, "You can not transfer money to the same account!");
    
        if (amountToTransfer <= 0)
            return new DataBaseReply(ERROR, "The transferred amount of money has to be greater than zero.");
    
        // Check if the account's balance has enough funds.
        DataBaseReply reply = checkAvailableFunds(dataBase, ownerAccount, amountToTransfer);
        if (reply.isType(ERROR)) return reply;
        
        
        reply = isExist(dataBase, new DataBaseQuery(
                banking.TransactionType.CHECK_ACCOUNT_EXISTENCE,
                Account.createWrapperAccount(payeeAccount.getCardNumber())
        ));
        if (reply.isType(NOT_EXISTS)) return reply;
    
    
        var result = makeUpdate(
                dataBase.getConnection(),
                String.format("UPDATE card SET balance = balance - %d WHERE id = %d AND number = '%s' AND pin = '%s';" +
                              "UPDATE card SET balance = balance + %1$d WHERE number = '%s';",
                              amountToTransfer,
                              ownerAccount.getDatabaseId(),
                              ownerAccount.getCardNumber(),
                              ownerAccount.getPinCode(),
                              payeeAccount.getCardNumber()
                )
        );
        
        return result == 1 ?
               new DataBaseReply(TRANSFERRED) :
               new DataBaseReply(ERROR);
    }
    
    
    // MID-LEVEL XD
    
    private static DataBaseReply checkAvailableFunds(DataBase dataBase, Account account, int requiredAmount)
    {
        var result = makeQuery(
                dataBase.getConnection(),
                String.format("SELECT * FROM card WHERE id = %d AND number = '%s';",
                              account.getDatabaseId(),
                              account.getCardNumber()
                )
        );
        
        if (Integer.parseInt(result.get(0)[3]) < requiredAmount)
            return new DataBaseReply(ERROR, "Insufficient funds for the requested transaction.");
        
        return new DataBaseReply(AVAILABLE);
    }
    
    
    // LOW-LEVEL METHODS: MySQL queries
    
    private static ArrayList<String[]> makeQuery(Connection connection, String sqlStatement)
    {
        ArrayList<String[]> result = new ArrayList<>();
        
        try (var statement = connection.createStatement())
        {
            try (var resultSet = statement.executeQuery(sqlStatement))
            {
                int columnsCount = resultSet.getMetaData().getColumnCount();
                var row = new String[columnsCount];
                
                while (resultSet.next())
                {
                    for (int i = 1; i <= columnsCount; i++)
                    {
                        row[i - 1] = resultSet.getString(i);
                    }
                    result.add(row.clone());
                }
            }
        }
        catch (SQLException e)
        {
            System.err.printf("SQLException when sending query:%n%s%n%n", e);
        }
        
        return result;
    }
    
    
    private static int makeUpdate(Connection connection, String sqlStatement)
    {
        try (var statement = connection.createStatement())
        {
            return statement.executeUpdate(sqlStatement);
        }
        catch (SQLException e)
        {
            System.err.printf("SQLException when sending update:%n%s%n%n", e);
        }
        
        return -1;
    }
}
