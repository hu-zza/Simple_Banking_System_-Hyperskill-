package banking;

import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static banking.DataBaseReply.ReplyType.AUTHENTICATED;
import static banking.DataBaseReply.ReplyType.CONNECTED;
import static banking.DataBaseReply.ReplyType.CREATED;
import static banking.DataBaseReply.ReplyType.EXISTS;
import static banking.DataBaseReply.ReplyType.NOT_CONNECTED;
import static banking.Main.database;
import static banking.TransactionType.*;

class Account
{
    private static final Random rndGenerator = new Random();
    private static final String BIN = "400000";
    
    private final String cardNumber;
    private final String pinCode;
    private int databaseId;
    private int balance;
    
    
    private Account(int databaseId, String cardNumber, String pinCode, int balance)
    {
        this.databaseId = databaseId;
        this.cardNumber = cardNumber;
        this.pinCode = pinCode;
        this.balance = balance;
    }
    
    static Account createAccount(int databaseId, String cardNumber, String pinCode, int balance)
    {
        // Maybe there are some checks in the future...
        return new Account(databaseId, cardNumber, pinCode, balance);
    }
    
    static Account createWrapperAccount(String cardNumber)
    {
        return new Account(0, cardNumber, "", 0);
    }
    
    static Position createBlankAccount()
    {
        var cardNumber = generateCardNumber();
        if (cardNumber == null) return Position.ROOT;
    
        var pinCode = rndGenerator
                          .ints(4, 0, 10)
                          .mapToObj(String::valueOf)
                          .collect(Collectors.joining());
    
    
        DataBaseReply reply;
        DataBaseReply auth;
        var tmpAccount = new Account(0, cardNumber, pinCode, 0);
        
        reply = database.processQuery(new DataBaseQuery(
                ADD_ACCOUNT_TO_DATABASE, tmpAccount
        ));
        
        auth = database.processQuery(new DataBaseQuery(
                AUTHENTICATE_ACCOUNT, tmpAccount
        ));
        
    
        if (reply.isType(CREATED) && auth.isType(AUTHENTICATED)) {
            
            System.out.printf(
                    "%nYour card has been created%nYour card number:%n%s%nYour card PIN:%n%s%n%n",
                    cardNumber,
                    pinCode
            );
        }
        else
        {
            System.out.println("Your card has not been created.");
        }
        
        return Position.ROOT;
    }
    
    static boolean verifyChecksum(String cardNumber)
    {
        int len = cardNumber.length() - 1;
        if (len != 15) return false;
        
        return Objects.equals(
                cardNumber.substring(len),
                calculateChecksum(cardNumber.substring(0, 6), cardNumber.substring(6, len))
        );
    }
    
    
    // LOW-LEVEL
    
    private static String generateCardNumber()
    {
        String tmpAccountNumber;
        String tmpCardNumber;
        DataBaseReply reply;
        
        int hysteresis = 0;
        do
        {

            tmpAccountNumber = rndGenerator
                                       .ints(9, 0, 10)
                                       .mapToObj(String::valueOf)
                                       .collect(Collectors.joining());
                                             
                                             
            tmpCardNumber = BIN + tmpAccountNumber + calculateChecksum(BIN, tmpAccountNumber);
            
            reply = database.processQuery(new DataBaseQuery(
                    CHECK_ACCOUNT_EXISTENCE, Account.createWrapperAccount(tmpCardNumber)
            ));
            
            if (reply.isType(NOT_CONNECTED))
            {
                DataBase.executeWithHysteresis(database::connect, CONNECTED, "Can not connect to database.");
                if (hysteresis++ > 3) break;
            }
        
        } while (reply.isType(EXISTS));
        
        
        if (tmpAccountNumber.isEmpty())
        {
            System.out.println("Can not create new card.");
            return null;
        }
        
        return tmpCardNumber;
    }
    
    private static String calculateChecksum(String binString, String accountString)
    {
        
        int sum = 0;
        int value;
        var array = (binString + accountString).toCharArray();
        
        for (int i = 0; i < array.length; i++)
        {
            value =  Character.getNumericValue(array[i]);
            sum += value;
        
            if (i % 2 == 0)
            {
                sum += value < 5 ?
                       value :
                       value - 9;
            }
        }
        return String.valueOf((sum / 10 * 10 + 10 - sum) % 10);
    }
    
    
    // INSTANCE METHODS
    
    int getDatabaseId()
    {
        return databaseId;
    }
    
    String getCardNumber()
    {
        return cardNumber;
    }
    
    String getPinCode()
    {
        return pinCode;
    }
    
    int getBalance()
    {
        return balance;
    }
    
    void setDatabaseId(int databaseId)
    {
        this.databaseId = databaseId;
    }
    
    void setBalance(int balance)
    {
        this.balance = balance;
    }
}
