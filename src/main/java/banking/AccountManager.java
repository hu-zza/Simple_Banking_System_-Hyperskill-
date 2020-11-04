package banking;

import static banking.DataBaseReply.ReplyType.AUTHENTICATED;
import static banking.DataBaseReply.ReplyType.CLOSED;
import static banking.DataBaseReply.ReplyType.EXISTS;
import static banking.DataBaseReply.ReplyType.MODIFIED;
import static banking.DataBaseReply.ReplyType.TRANSFERRED;
import static banking.Main.database;
import static banking.Main.scanner;
import static banking.TransactionType.AUTHENTICATE_ACCOUNT;
import static banking.TransactionType.CHECK_ACCOUNT_EXISTENCE;
import static banking.TransactionType.CLOSE_ACCOUNT;
import static banking.TransactionType.DO_TRANSFER;
import static banking.TransactionType.MODIFY_BALANCE;
import static banking.TransactionType.SYNCHRONIZE_ACCOUNT;

// TO-DO:
// PATTERN CHECK FOR INPUT!!!


abstract class AccountManager
{
    private static boolean loggedIn;
    private static Account account;
    
    
    static Position loginAccount()
    {
        String cardNumber;
        String pinCode;
        
        scanner.nextLine();
        
        System.out.printf("%nEnter your card number:%n");
        cardNumber = scanner
                             .nextLine()
                             .strip();
        
        System.out.println("Enter your PIN:");
        pinCode = scanner
                          .nextLine()
                          .strip();
        
        if (cardNumber.length() == 16 && pinCode.length() == 4)
        {
            Account tmpAccount  = Account.createExistingAccount(cardNumber, pinCode);
            DataBaseReply reply = database.processQuery(new DataBaseQuery(AUTHENTICATE_ACCOUNT, tmpAccount));
            
            if (reply.isType(AUTHENTICATED))
            {
                loggedIn = true;
                account  = tmpAccount;
                synchronizeAccount();
                System.out.printf("%nYou have successfully logged in!%n%n");
                return Position.ACCOUNT;
            }
            else
            {
                loggedIn = false;
                account  = null;
            }
        }
        System.out.printf("%nWrong card number or PIN!%n%n");
        return Position.ROOT;
    }
    
    static Position logoutAccount()
    {
        loggedIn = false;
        account  = null;
        
        System.out.printf("%nYou have successfully logged out!%n%n");
        return Position.ROOT;
    }
    
    
    /////////////////////
    // ONLY AFTER LOGIN
    
    static Position getBalance()
    {
        if (!loggedIn) return Position.ROOT;
        
        synchronizeAccount();
        System.out.printf("%nBalance: %d%n%n", account.getBalance());
        
        return Position.ACCOUNT;
    }
    
    
    static Position addIncome()
    {
        if (!loggedIn) return Position.ROOT;
        
        System.out.printf("%nEnter income:%n");
        
        scanner.nextLine();
        String[] transactionDetails = {scanner.nextLine().strip()};
        
        synchronizeAccount();
        DataBaseReply reply;
        reply = database.processQuery(new DataBaseQuery(MODIFY_BALANCE, account, transactionDetails));
        
        System.out.println(reply.isType(MODIFIED) ? "Income was added!" : reply.getDetails()[0]);
        System.out.println();
        
        return Position.ACCOUNT;
    }
    
    static Position doWithdrawal()
    {
        if (!loggedIn) return Position.ROOT;
        
        System.out.print("%nEnter how much money you want to withdraw:%n");
        
        scanner.nextLine();
        String amount = scanner
                                .nextLine()
                                .strip();
        
        String[] transactionDetails = {amount.startsWith("-") ? amount : "-" + amount};
        
        synchronizeAccount();
        DataBaseReply reply;
        reply = database.processQuery(new DataBaseQuery(MODIFY_BALANCE, account, transactionDetails));
        
        System.out.println(reply.isType(MODIFIED) ? "Success!" : reply.getDetails()[0]);
        System.out.println();
        
        return Position.ACCOUNT;
    }
    
    static Position doTransfer()
    {
        if (!loggedIn) return Position.ROOT;
        
        System.out.printf("%nTransfer%nEnter card number:%n");
        
        scanner.nextLine();
        String payeeCardNumber = scanner
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
        else if (database
                         .processQuery(new DataBaseQuery(CHECK_ACCOUNT_EXISTENCE, payeeWrapperAccount))
                         .isNotType(EXISTS))
        {
            System.out.println("Such a card does not exist.");
        }
        else
        {
            System.out.println("Enter how much money you want to transfer:");
            String amountToTransfer = scanner
                                              .nextLine()
                                              .strip();
            
            synchronizeAccount();
            
            if (account.getBalance() < Integer.parseInt(amountToTransfer))
            {
                System.out.println("Not enough money!");
            }
            else
            {
                DataBaseReply reply;
                reply = database.processQuery(new DataBaseQuery(DO_TRANSFER,
                                                                account,
                                                                new String[] {amountToTransfer},
                                                                payeeWrapperAccount
                ));
                
                System.out.println(reply.isType(TRANSFERRED) ? "Success!" : reply.getDetails()[0]);
            }
        }
        
        System.out.println();
        return Position.ACCOUNT;
    }
    
    static Position closeAccount()
    {
        if (!loggedIn) return Position.ROOT;
        
        synchronizeAccount();
        DataBaseReply reply;
        reply = database.processQuery(new DataBaseQuery(CLOSE_ACCOUNT, account));
        
        System.out.println();
        System.out.println(reply.isType(CLOSED) ? "The account has been closed!" : reply.getDetails()[0]);
        System.out.println();
        
        loggedIn = false;
        account  = null;
        return Position.ROOT;
    }
    
    
    // LOW-LEVEL
    
    private static void synchronizeAccount()
    {
        database.processQuery(new DataBaseQuery(SYNCHRONIZE_ACCOUNT, account));
    }
}
