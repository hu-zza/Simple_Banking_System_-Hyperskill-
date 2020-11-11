package hu.zza.hyperskill.banking.db;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import static hu.zza.hyperskill.banking.db.ReplyType.CONNECTED;
import static hu.zza.hyperskill.banking.db.ReplyType.ERROR;
import static hu.zza.hyperskill.banking.db.ReplyType.NOT_CONNECTED;


public class DataBase implements AutoCloseable
{
    private final String     URL;
    private final String     USERNAME;
    private final String     PASSWORD;
    private       Connection connection;
    
    public DataBase(String url, String username, String password)
    {
        this.URL      = url;
        this.USERNAME = username;
        this.PASSWORD = password;
    }
    
    
    
    // INSTANCE METHODS
    
    // PUBLIC INTERFACE
    
    public boolean connect()
    {
        return executeWithHysteresis(
                this::connectWithHysteresis,
                CONNECTED,
                "The program could not open the connection to the database."
        );
    }
    
    public boolean isConnected()
    {
        return this.checkValidity().isType(CONNECTED);
    }
    
    @Override
    public void close()
    {
        executeWithHysteresis(
                this::closeWithHysteresis,
                NOT_CONNECTED,
                "The program could not close the connection to the database."
        );
    }
    
    public DB_Reply processQuery(DB_Query dataBaseQuery)
    {
        if (checkValidity().isNotType(CONNECTED)) return new DB_Reply(NOT_CONNECTED);
        
        try
        {
            return dataBaseQuery
                           .getTransactionType()
                           .getBiFunction()
                           .apply(this, dataBaseQuery);
        }
        catch (Exception e)
        {
            return makeErrorReply(e, "An exception occur when processing a database query:%n%s%n%n");
        }
    }
    
    
    // PACKAGE-PRIVATE INTERFACE
    
    DB_Reply checkValidity()
    {
        if (connection != null)
        {
            try
            {
                return connection.isValid(3) ? new DB_Reply(CONNECTED) : new DB_Reply(NOT_CONNECTED);
            }
            catch (SQLException e)
            {
                return makeErrorReply(e, "SQLException when checking connection validity:%n%s%n%n");
            }
        }
        else
        {
            return new DB_Reply(NOT_CONNECTED);
        }
    }
    
    Connection getConnection()
    {
        return connection;
    }
    
    
    // LOW-LEVEL
    
    private boolean executeWithHysteresis(Supplier<DB_Reply> methodToExecute,
                                          ReplyType targetReplyType,
                                          String... timeoutMessages
    )
    {
        DB_Reply reply;
        int      hysteresis = 3;
        int      counter    = 0;
        
        do
        {
            reply = methodToExecute.get();
            
            if (reply.isType(targetReplyType)) return true;
            
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException ignored)
            {
            }
        } while (counter++ < hysteresis);
        
        for (var msg : timeoutMessages) System.out.println(msg);
        
        return false;
    }
    
    private DB_Reply connectWithHysteresis()
    {
        var dataSource = new MysqlDataSource();
        dataSource.setURL(URL);
        try
        {
            dataSource.setServerTimezone("UTC");
            this.connection = dataSource.getConnection(USERNAME, PASSWORD);
        }
        catch (SQLException e)
        {
            return makeErrorReply(e, "SQLException when establishing the connection:%n%s%n%n");
        }
        
        return checkValidity();
    }
    
    private DB_Reply closeWithHysteresis()
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            return makeErrorReply(e, "SQLException when closing the connection:%n%s%n%n");
        }
        return new DB_Reply(NOT_CONNECTED);
    }
    
    
    private DB_Reply makeErrorReply(Exception exception, String messageFormat)
    {
        String msg = String.format(messageFormat, exception);
        System.err.print(msg);
        return new DB_Reply(ERROR, msg);
    }
}
