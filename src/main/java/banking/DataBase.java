package banking;

import banking.DataBaseReply.ReplyType;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import static banking.DataBaseReply.ReplyType.CONNECTED;
import static banking.DataBaseReply.ReplyType.ERROR;
import static banking.DataBaseReply.ReplyType.NOT_CONNECTED;


class DataBase
{
    private final String     URL;
    private final String     USERNAME;
    private final String     PASSWORD;
    private       Connection connection;
    
    DataBase(String url, String username, String password)
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
    static boolean executeWithHysteresis(Supplier<DataBaseReply> methodToExecute,
                                         ReplyType targetReplyType,
                                         String... timeoutMessages
    )
    {
        DataBaseReply reply;
        int           hysteresis = 3;
        int           counter    = 0;
        
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
    
    DataBaseReply connect()
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
    
    DataBaseReply isConnected()
    {
        if (connection != null)
        {
            try
            {
                return connection.isValid(3) ? new DataBaseReply(CONNECTED) : new DataBaseReply(NOT_CONNECTED);
            }
            catch (SQLException e)
            {
                return makeErrorReply(e, "SQLException when checking connection validity:%n%s%n%n");
            }
        }
        else
        {
            return new DataBaseReply(NOT_CONNECTED);
        }
    }
    
    Connection getConnection()
    {
        return connection;
    }
    
    
    DataBaseReply close()
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            return makeErrorReply(e, "SQLException when closing the connection:%n%s%n%n");
        }
        return new DataBaseReply(NOT_CONNECTED);
    }
    
    
    // QUERY PROCESSING
    
    DataBaseReply processQuery(DataBaseQuery dataBaseQuery)
    {
        if (isConnected().isNotType(CONNECTED)) return new DataBaseReply(NOT_CONNECTED);
        
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
    
    private DataBaseReply makeErrorReply(Exception exception, String messageFormat)
    {
        String msg = String.format(messageFormat, exception);
        System.err.print(msg);
        return new DataBaseReply(ERROR, msg);
    }
}
