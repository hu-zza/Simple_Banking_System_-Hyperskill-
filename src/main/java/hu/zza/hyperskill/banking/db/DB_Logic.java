package hu.zza.hyperskill.banking.db;

import hu.zza.hyperskill.banking.Account;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

import static hu.zza.hyperskill.banking.db.ReplyType.AUTHENTICATED;
import static hu.zza.hyperskill.banking.db.ReplyType.AVAILABLE;
import static hu.zza.hyperskill.banking.db.ReplyType.CLOSED;
import static hu.zza.hyperskill.banking.db.ReplyType.CREATED;
import static hu.zza.hyperskill.banking.db.ReplyType.ERROR;
import static hu.zza.hyperskill.banking.db.ReplyType.EXISTS;
import static hu.zza.hyperskill.banking.db.ReplyType.MODIFIED;
import static hu.zza.hyperskill.banking.db.ReplyType.NOT_EXISTS;
import static hu.zza.hyperskill.banking.db.ReplyType.SYNCHRONIZED;
import static hu.zza.hyperskill.banking.db.ReplyType.TRANSFERRED;
import static hu.zza.hyperskill.banking.db.ReplyType.UPDATED;


/////////////////////////////////////
// Utility class for DB manipulation.

abstract class DB_Logic
{
    
    // INSTANCE METHODS on database
    
    static DB_Reply isExist(DataBase dataBase, DB_Query dataBaseQuery)
    {
        var result = makeQuery(dataBase.getConnection(),
                               String.format(
                                       "SELECT * FROM card WHERE number = %s;",
                                       dataBaseQuery
                                               .getAccount()
                                               .getCardNumber()
                               )
        );
        
        return result.size() == 0 ? new DB_Reply(NOT_EXISTS) : new DB_Reply(EXISTS);
    }
    
    
    static DB_Reply addAccount(DataBase dataBase, DB_Query dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        var result = makeUpdate(dataBase.getConnection(), String.format(
                "INSERT INTO card (number, pin, balance) VALUES ('%s', '%s', %d);",
                account.getCardNumber(),
                account.getPinCode(),
                account.getBalance()
        ));
        
        return result == 1 ? new DB_Reply(CREATED) : new DB_Reply(ERROR);
    }
    
    static DB_Reply authenticateAccount(DataBase dataBase, DB_Query dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        var result = makeQuery(dataBase.getConnection(), String.format(
                "SELECT * FROM card WHERE number = '%s' AND pin = '%s';",
                account.getCardNumber(),
                account.getPinCode()
        ));
        
        if (result.size() == 1)
        {
            account.setDatabaseId(Integer.parseInt(result.get(0)[0]));
            return new DB_Reply(AUTHENTICATED);
        }
        else
        {
            return new DB_Reply(ERROR);
        }
    }
    
    static DB_Reply synchronizeAccount(DataBase dataBase, DB_Query dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        var result = makeQuery(dataBase.getConnection(), String.format(
                "SELECT * FROM card WHERE id = '%d' AND number = '%s' AND pin = '%s';",
                account.getDatabaseId(),
                account.getCardNumber(),
                account.getPinCode()
        ));
        
        if (result.size() == 1)
        {
            account.setBalance(Integer.parseInt(result.get(0)[3]));
            // Another setter callings...
            
            return new DB_Reply(SYNCHRONIZED);
        }
        else
        {
            return new DB_Reply(ERROR);
        }
    }
    
    static DB_Reply updateAccount(DataBase dataBase, DB_Query dataBaseQuery)
    {
        return new DB_Reply(UPDATED, "This is feature is not implemented yet.");
    }
    
    static DB_Reply deleteAccount(DataBase dataBase, DB_Query dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        var result = makeUpdate(dataBase.getConnection(), String.format(
                "DELETE FROM card WHERE id = %d AND number = '%s' AND pin = '%s';",
                account.getDatabaseId(),
                account.getCardNumber(),
                account.getPinCode()
        ));
        
        return result == 1 ? new DB_Reply(CLOSED) : new DB_Reply(ERROR);
    }
    
    static DB_Reply modifyBalance(DataBase dataBase, DB_Query dataBaseQuery)
    {
        Account account = dataBaseQuery.getAccount();
        
        // amountToAdd can be either positive (add) or negative (withdraw) number.
        int amountToAdd = Integer.parseInt(dataBaseQuery.getTransactionDetails()[0]);
        
        // WITHDRAWAL: Check if the account's balance has enough funds.
        if (amountToAdd < 0)
        {
            DB_Reply reply = checkAvailableFunds(dataBase, account, amountToAdd);
            if (reply.isType(ERROR)) return reply;
        }
        
        var result = makeUpdate(dataBase.getConnection(), String.format(
                "UPDATE card SET balance = balance + %d WHERE id = %d AND number = '%s' AND pin = '%s';",
                amountToAdd,
                account.getDatabaseId(),
                account.getCardNumber(),
                account.getPinCode()
        ));
        
        return result == 1 ? new DB_Reply(MODIFIED) : new DB_Reply(ERROR);
    }
    
    static DB_Reply doTransfer(DataBase dataBase, DB_Query dataBaseQuery)
    {
        Account ownerAccount = dataBaseQuery.getAccount();
        // hu.zza.hyperskill.banking.db.DB_Query has capacity for multiple payee accounts...
        // But it has not implemented yet.
        Account payeeAccount = dataBaseQuery.getAdditionalAccounts()[0];
        
        // amountToTransfer can be only a positive number.
        int amountToTransfer = Integer.parseInt(dataBaseQuery.getTransactionDetails()[0]);
        
        if (Objects.equals(ownerAccount.getCardNumber(), payeeAccount.getCardNumber()))
        {
            return new DB_Reply(ERROR, "You can not transfer money to the same account!");
        }
        
        if (amountToTransfer <= 0)
        {
            return new DB_Reply(ERROR, "The transferred amount of money has to be greater than zero.");
        }
        
        // Check if the account's balance has enough funds.
        DB_Reply reply = checkAvailableFunds(dataBase, ownerAccount, amountToTransfer);
        if (reply.isType(ERROR)) return reply;
        
        
        reply = isExist(dataBase,
                        new DB_Query(TransactionType.CHECK_ACCOUNT_EXISTENCE,
                                     Account.createWrapperAccount(payeeAccount.getCardNumber())
                        )
        );
        if (reply.isType(NOT_EXISTS)) return reply;
        
        
        var result = makeUpdate(dataBase.getConnection(), String.format(
                "UPDATE card SET balance = balance - %d WHERE id = %d AND number = '%s' AND pin = '%s';"
                + "UPDATE card SET balance = balance + %1$d WHERE number = '%s';",
                amountToTransfer,
                ownerAccount.getDatabaseId(),
                ownerAccount.getCardNumber(),
                ownerAccount.getPinCode(),
                payeeAccount.getCardNumber()
        ));
        
        return result == 1 ? new DB_Reply(TRANSFERRED) : new DB_Reply(ERROR);
    }
    
    
    // MID-LEVEL XD
    
    private static DB_Reply checkAvailableFunds(DataBase dataBase, Account account, int requiredAmount)
    {
        var result = makeQuery(dataBase.getConnection(), String.format(
                "SELECT * FROM card WHERE id = %d AND number = '%s';",
                account.getDatabaseId(),
                account.getCardNumber()
        ));
        
        if (Integer.parseInt(result.get(0)[3]) < requiredAmount)
        {
            return new DB_Reply(ERROR, "Insufficient funds for the requested transaction.");
        }
        
        return new DB_Reply(AVAILABLE);
    }
    
    
    // LOW-LEVEL METHODS: MySQL queries
    
    private static ArrayList<String[]> makeQuery(Connection connection, String sqlStatement)
    {
        ArrayList<String[]> result = new ArrayList<>();
        
        try (var statement = connection.createStatement())
        {
            try (var resultSet = statement.executeQuery(sqlStatement))
            {
                int
                        columnsCount =
                        resultSet
                                .getMetaData()
                                .getColumnCount();
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
