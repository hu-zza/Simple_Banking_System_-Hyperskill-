package hu.zza.hyperskill.banking.db;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import static hu.zza.hyperskill.banking.db.DB_Reply.ReplyType;
import static hu.zza.hyperskill.banking.db.DB_Reply.ReplyType.CONNECTED;
import static hu.zza.hyperskill.banking.db.DB_Reply.ReplyType.ERROR;
import static hu.zza.hyperskill.banking.db.DB_Reply.ReplyType.NOT_CONNECTED;


public class DataBase
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
    
    
    /**
     * Execute methods related to database connection with hysteresis. Maximum trials count is 3 at one second interval.
     *
     * @param methodToExecute The executeWithHysteresis() tries to execute this.
     * @param targetReplyType The required return value from the method.
     * @param timeoutMessages String vararg to display line-by-line if time is over.
     *
     * @return true - if executed successfully //
     *         false - if an error occurs
     */
    public static boolean executeWithHysteresis(Supplier<DB_Reply> methodToExecute,
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
    
    
    // INSTANCE METHODS
    
    // CONNECTION: establish, get, close
    
    public DB_Reply connect()
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
        
        return isConnected();
    }
    
    DB_Reply isConnected()
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
    
    
    public DB_Reply close()
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
    
    
    // QUERY PROCESSING
    
    public DB_Reply processQuery(DB_Query dataBaseQuery)
    {
        if (isConnected().isNotType(CONNECTED)) return new DB_Reply(NOT_CONNECTED);
        
        try
        {
            return dataBaseQuery.getTransactionType().function.apply(this, dataBaseQuery);
        }
        catch (Exception e)
        {
            return makeErrorReply(e, "An exception occur when processing a database query:%n%s%n%n");
        }
    }
    
    
    // LOW-LEVEL
    
    private DB_Reply makeErrorReply(Exception exception, String messageFormat)
    {
        String msg = String.format(messageFormat, exception);
        System.err.print(msg);
        return new DB_Reply(ERROR, msg);
    }
}
