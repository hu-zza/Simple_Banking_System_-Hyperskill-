package hu.zza.hyperskill.banking;

import hu.zza.hyperskill.banking.db.DB_Query;
import hu.zza.hyperskill.banking.db.DB_Reply;

import static hu.zza.hyperskill.banking.Main.DATABASE;
import static hu.zza.hyperskill.banking.Main.SCANNER;
import static hu.zza.hyperskill.banking.db.ReplyType.AUTHENTICATED;
import static hu.zza.hyperskill.banking.db.ReplyType.CLOSED;
import static hu.zza.hyperskill.banking.db.ReplyType.EXISTS;
import static hu.zza.hyperskill.banking.db.ReplyType.MODIFIED;
import static hu.zza.hyperskill.banking.db.ReplyType.TRANSFERRED;
import static hu.zza.hyperskill.banking.db.TransactionType.AUTHENTICATE_ACCOUNT;
import static hu.zza.hyperskill.banking.db.TransactionType.CHECK_ACCOUNT_EXISTENCE;
import static hu.zza.hyperskill.banking.db.TransactionType.CLOSE_ACCOUNT;
import static hu.zza.hyperskill.banking.db.TransactionType.DO_TRANSFER;
import static hu.zza.hyperskill.banking.db.TransactionType.MODIFY_BALANCE;
import static hu.zza.hyperskill.banking.db.TransactionType.SYNCHRONIZE_ACCOUNT;


// TO-DO:
// PATTERN CHECK FOR INPUT!!!

//////////////////////////////////////////
// Utility class for Account manipulation.

abstract class AccountManager
{
    private static boolean loggedIn;
    private static Account account;
    
    
    static int loginAccount()
    {
        String cardNumber;
        String pinCode;
        
        SCANNER.nextLine();
        
        System.out.printf("%nEnter your card number:%n");
        cardNumber = SCANNER
                             .nextLine()
                             .strip();
        
        System.out.println("Enter your PIN:");
        pinCode = SCANNER
                          .nextLine()
                          .strip();
        
        if (cardNumber.length() == 16 && pinCode.length() == 4)
        {
            Account tmpAccount = Account.createExistingAccount(cardNumber, pinCode);
            DB_Reply reply     = DATABASE.processQuery(new DB_Query(AUTHENTICATE_ACCOUNT, tmpAccount));
            
            if (reply.isType(AUTHENTICATED))
            {
                loggedIn = true;
                account  = tmpAccount;
                synchronizeAccount();
                System.out.printf("%nYou have successfully logged in!%n%n");
                return 0; // Position.ACCOUNT
            }
            else
            {
                loggedIn = false;
                account  = null;
            }
        }
        System.out.printf("%nWrong card number or PIN!%n%n");
        return 1; // Position.ROOT
    }
    
    
    
    /////////////////////
    // ONLY AFTER LOGIN
    
    static int logoutAccount()
    {
        if (!loggedIn) return 1; // Position.ROOT
        
        loggedIn = false;
        account  = null;
        
        System.out.printf("%nYou have successfully logged out!%n%n");
        return 0; // Position.ROOT
    }
    
    static int getBalance()
    {
        if (!loggedIn) return 1; // Position.ROOT
        
        synchronizeAccount();
        System.out.printf("%nBalance: %d%n%n", account.getBalance());
        
        return 0; // Position.ACCOUNT
    }
    
    
    static int addIncome()
    {
        if (!loggedIn) return 1; // Position.ROOT
        
        System.out.printf("%nEnter income:%n");
        
        SCANNER.nextLine();
        String[] transactionDetails = {SCANNER.nextLine().strip()};
        
        synchronizeAccount();
        DB_Reply reply;
        reply = DATABASE.processQuery(new DB_Query(MODIFY_BALANCE, account, transactionDetails));
        
        System.out.println(reply.isType(MODIFIED) ? "Income was added!" : reply.getDetails()[0]);
        System.out.println();
        
        return 0; // Position.ACCOUNT
    }
    
    static int doWithdrawal()
    {
        if (!loggedIn) return 1; // Position.ROOT
        
        System.out.print("%nEnter how much money you want to withdraw:%n");
        
        SCANNER.nextLine();
        String amount = SCANNER
                                .nextLine()
                                .strip();
        
        String[] transactionDetails = {amount.startsWith("-") ? amount : "-" + amount};
        
        synchronizeAccount();
        DB_Reply reply;
        reply = DATABASE.processQuery(new DB_Query(MODIFY_BALANCE, account, transactionDetails));
        
        System.out.println(reply.isType(MODIFIED) ? "Success!" : reply.getDetails()[0]);
        System.out.println();
        
        return 0; // Position.ACCOUNT
    }
    
    static int doTransfer()
    {
        if (!loggedIn) return 1; // Position.ROOT
        
        System.out.printf("%nTransfer%nEnter card number:%n");
        
        SCANNER.nextLine();
        String payeeCardNumber = SCANNER
                                         .nextLine()
                                         .strip();
        
        Account payeeWrapperAccount = Account.createWrapperAccount(payeeCardNumber);
        
        if (account
                    .getCardNumber()
                    .equals(payeeCardNumber))
        {
            System.out.println("You can't transfer money to the same account!");
        }
        else if (!Account.verifyChecksum(payeeCardNumber))
        {
            System.out.println("Probably you made mistake in the card number. Please try again!");
        }
        else if (DATABASE
                         .processQuery(new DB_Query(CHECK_ACCOUNT_EXISTENCE, payeeWrapperAccount))
                         .isNotType(EXISTS))
        {
            System.out.println("Such a card does not exist.");
        }
        else
        {
            System.out.println("Enter how much money you want to transfer:");
            String amountToTransfer = SCANNER
                                              .nextLine()
                                              .strip();
            
            synchronizeAccount();
            
            if (account.getBalance() < Integer.parseInt(amountToTransfer))
            {
                System.out.println("Not enough money!");
            }
            else
            {
                DB_Reply reply;
                reply = DATABASE.processQuery(new DB_Query(DO_TRANSFER,
                                                           account,
                                                           new String[] {amountToTransfer},
                                                           payeeWrapperAccount
                ));
                
                System.out.println(reply.isType(TRANSFERRED) ? "Success!" : reply.getDetails()[0]);
            }
        }
        
        System.out.println();
        return 0; // Position.ACCOUNT
    }
    
    static int closeAccount()
    {
        if (!loggedIn) return 1; // Position.ROOT
        
        synchronizeAccount();
        DB_Reply reply;
        reply = DATABASE.processQuery(new DB_Query(CLOSE_ACCOUNT, account));
        
        System.out.println();
        System.out.println(reply.isType(CLOSED) ? "The account has been closed!" : reply.getDetails()[0]);
        System.out.println();
        
        loggedIn = false;
        account  = null;
        return 0; // Position.ROOT
    }
    
    
    // LOW-LEVEL
    
    private static void synchronizeAccount()
    {
        DATABASE.processQuery(new DB_Query(SYNCHRONIZE_ACCOUNT, account));
    }
}
