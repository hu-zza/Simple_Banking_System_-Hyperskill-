package banking;

import banking.DataBaseReply.ReplyType;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import static banking.DataBaseReply.ReplyType.*;

class DataBase
{
    private final String URL;
    private final String USERNAME;
    private final String PASSWORD;
    private Connection connection;
    
    DataBase(String url, String username, String password)
    {
        this.URL = url;
        this.USERNAME = username;
        this.PASSWORD = password;
    }
    
    
    static boolean executeWithHysteresis(
            Supplier<banking.DataBaseReply> supplier, ReplyType targetReplyType, String... timeoutMsg
    )
    {
        banking.DataBaseReply reply;
        int hysteresis = 5;
        int counter = 0;

        do
        {
            reply = supplier.get();

            if (reply.isType(targetReplyType))
            {
                return true;
            }

        } while (counter++ < hysteresis);


        for (var string : timeoutMsg)
        {
            System.out.println(string);
        }

        return false;
    }
    
    
    // INSTANCE METHODS
    
    banking.DataBaseReply connect()
    {
        var dataSource = new MysqlDataSource();
        dataSource.setURL(URL);
        try
        {
            dataSource.setServerTimezone("UTC");
        }
        catch (SQLException e)
        {
            return makeErrorReply(e, "SQLException when setting server's time zone:%n%s%n%n");
        }
    
        try
        {
            this.connection = dataSource.getConnection(USERNAME, PASSWORD);
        }
        catch (SQLException e)
        {
            return makeErrorReply(e, "SQLException when establishing the connection:%n%s%n%n");
        }
    
        return isConnected();
    }
    
    banking.DataBaseReply isConnected()
    {
        if (connection != null)
        {
            try
            {
                return connection.isValid(3) ?
                       new banking.DataBaseReply(CONNECTED) :
                       new banking.DataBaseReply(NOT_CONNECTED);
            }
            catch (SQLException e)
            {
                return makeErrorReply(e, "SQLException when checking connection validity:%n%s%n%n");
            }
        }
        else
        {
            return new banking.DataBaseReply(NOT_CONNECTED);
        }
    }
    
    Connection getConnection()
    {
        return connection;
    }
    
   
    banking.DataBaseReply close()
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            return makeErrorReply(e, "SQLException when closing the connection:%n%s%n%n");
        }
        return new banking.DataBaseReply(NOT_CONNECTED);
    }
    
    
    banking.DataBaseReply processQuery(banking.DataBaseQuery dataBaseQuery)
    {
        if (isConnected().isNotType(CONNECTED)) return new banking.DataBaseReply(NOT_CONNECTED);
        
        try
        {
            return dataBaseQuery.getTransactionType().function.apply(this, dataBaseQuery);
        }
        catch (Exception e)
        {
            return makeErrorReply(e, "An exception occur when processing a database query:%n%s%n%n");
        }
    }
    
    private banking.DataBaseReply makeErrorReply(Exception e, String s)
    {
        String msg = String.format(s, e);
        System.err.print(msg);
        return new banking.DataBaseReply(ERROR, msg);
    }
}
