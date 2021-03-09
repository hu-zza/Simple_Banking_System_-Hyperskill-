package hu.zza.hyperskill.banking;

import hu.zza.hyperskill.banking.db.DB_Query;
import hu.zza.hyperskill.banking.db.DB_Reply;

import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static hu.zza.hyperskill.banking.Main.DATABASE;
import static hu.zza.hyperskill.banking.db.ReplyType.AUTHENTICATED;
import static hu.zza.hyperskill.banking.db.ReplyType.CREATED;
import static hu.zza.hyperskill.banking.db.ReplyType.EXISTS;
import static hu.zza.hyperskill.banking.db.ReplyType.NOT_CONNECTED;
import static hu.zza.hyperskill.banking.db.TransactionType.ADD_ACCOUNT_TO_DATABASE;
import static hu.zza.hyperskill.banking.db.TransactionType.AUTHENTICATE_ACCOUNT;
import static hu.zza.hyperskill.banking.db.TransactionType.CHECK_ACCOUNT_EXISTENCE;


public class Account {
    private static final Random rndGenerator = new Random();
    private static final String BIN          = "400000";
    
    private final String cardNumber;
    private final String pinCode;
    private       int    databaseId;
    private       int    balance;
    
    
    private Account(String cardNumber, String pinCode) {
        // The database is authoritative,
        // so fields 'databaseId' and 'balance'
        // get value only after auth / sync with it.
        //
        // More info: db.DB_Logic::authenticateAccount
        //            db.DB_Logic::synchronizeAccount
        
        this.cardNumber = cardNumber;
        this.pinCode    = pinCode;
    }
    
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    
    public String getPinCode() {
        return pinCode;
    }
    
    
    public int getDatabaseId() {
        return databaseId;
    }
    
    
    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }
    
    
    // LOW-LEVEL
    
    
    public int getBalance() {
        return balance;
    }
    
    
    public void setBalance(int balance) {
        this.balance = balance;
    }
    
    
    // INSTANCE METHODS
    
    
    public static Account createWrapperAccount(String cardNumber) {
        return new Account(cardNumber, "");
    }
    
    
    static Account createExistingAccount(String cardNumber, String pinCode) {
        return new Account(cardNumber, pinCode);
    }
    
    
    static int createNewAccount() {
        var cardNumber = generateCardNumber();
        if (cardNumber == null) { return 1; }
        
        var pinCode = rndGenerator.ints(4, 0, 10)
                                  .mapToObj(String::valueOf)
                                  .collect(Collectors.joining());
        
        
        DB_Reply reply;
        DB_Reply auth;
        Account  tmpAccount = new Account(cardNumber, pinCode);
        
        reply = DATABASE.processQuery(new DB_Query(ADD_ACCOUNT_TO_DATABASE, tmpAccount));
        
        auth = DATABASE.processQuery(new DB_Query(AUTHENTICATE_ACCOUNT, tmpAccount));
        
        
        if (reply.isType(CREATED) && auth.isType(AUTHENTICATED)) {
            
            System.out.printf("%nYour card has been created%nYour card number:%n%s%nYour card PIN:%n%s%n%n", cardNumber,
                              pinCode);
        } else {
            System.out.println("Your card has not been created.");
        }
        
        return 0;
    }
    
    
    static boolean verifyChecksum(String cardNumber) {
        int len = cardNumber.length() - 1;
        if (len != 15) { return false; }
        
        return Objects.equals(cardNumber.substring(len),
                              calculateChecksum(cardNumber.substring(0, 6), cardNumber.substring(6, len)));
    }
    
    
    private static String generateCardNumber() {
        String   tmpAccountNumber;
        String   tmpCardNumber;
        DB_Reply reply;
        
        int hysteresis = 0;
        do {
            tmpAccountNumber = rndGenerator.ints(9, 0, 10)
                                           .mapToObj(String::valueOf)
                                           .collect(Collectors.joining());
            
            tmpCardNumber = BIN + tmpAccountNumber + calculateChecksum(BIN, tmpAccountNumber);
            
            reply = DATABASE.processQuery(
                    new DB_Query(CHECK_ACCOUNT_EXISTENCE, Account.createWrapperAccount(tmpCardNumber)));
            
            if (reply.isType(NOT_CONNECTED)) {
                DATABASE.connect();
                if (hysteresis++ > 3) { break; }
            }
            
        } while (reply.isType(EXISTS));
        
        
        if (tmpAccountNumber.isEmpty()) {
            System.out.println("Can not create new card.");
            return null;
        }
        
        return tmpCardNumber;
    }
    
    
    private static String calculateChecksum(String binString, String accountString) {
        
        int sum   = 0;
        int value;
        var array = (binString + accountString).toCharArray();
        
        for (int i = 0; i < array.length; i++) {
            value = Character.getNumericValue(array[i]);
            sum += value;
            
            if (i % 2 == 0) {
                sum += value < 5 ? value : value - 9;
            }
        }
        return String.valueOf((sum / 10 * 10 + 10 - sum) % 10);
    }
}
