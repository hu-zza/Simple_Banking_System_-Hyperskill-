package banking;

import static banking.DataBaseReply.ReplyType.*;
import static banking.DataBaseReply.ReplyType.AUTHENTICATED;
import static banking.DataBaseReply.ReplyType.CLOSED;
import static banking.DataBaseReply.ReplyType.EXISTS;
import static banking.DataBaseReply.ReplyType.MODIFIED;
import static banking.DataBaseReply.ReplyType.TRANSFERRED;
import static banking.Main.database;
import static banking.Main.scanner;
import static banking.TransactionType.*;

// TO-DO:
// PATTERN CHECK FOR INPUT!!!

abstract class AccountManager
{
    private static boolean loggedIn;
    private static String cardNumber;
    private static banking.Account account;
    
    
    static banking.Position loginAccount()
    {
        String pinCode;
    
        scanner.nextLine();
        
        System.out.printf("%nEnter your card number:%n");
        cardNumber = scanner.nextLine().strip();
    
        System.out.println("Enter your PIN:");
        pinCode = scanner.nextLine().strip();
    
        if (cardNumber.length() == 16 && pinCode.length() == 4)
        {
            banking.Account tmpAccount = banking.Account.createAccount(0, cardNumber, pinCode, 0);
            var reply = database.processQuery(new DataBaseQuery(AUTHENTICATE_ACCOUNT, tmpAccount));
            if (reply.isType(AUTHENTICATED))
            {
                loggedIn = true;
                account = tmpAccount;
                synchronizeAccount();
                System.out.printf("%nYou have successfully logged in!%n%n");
                return banking.Position.ACCOUNT;
            }
            else
            {
                cardNumber = "";
                account = null;
            }
        }
        System.out.printf("%nWrong card number or PIN!%n%n");
        return banking.Position.ROOT;
    }
    
    static banking.Position logoutAccount()
    {
        loggedIn = false;
        cardNumber = "";
        account = null;
        
        System.out.printf("%nYou have successfully logged out!%n%n");
        return banking.Position.ROOT;
    }
    
    
    /////////////////////
    // ONLY AFTER LOGIN
    
    static banking.Position getBalance()
    {
        if (!loggedIn) return banking.Position.ROOT;
    
        synchronizeAccount();
        System.out.printf("%nBalance: %d%n%n", account.getBalance());
        
        return banking.Position.ACCOUNT;
    }
    
    
    static banking.Position addIncome()
    {
        if (!loggedIn) return banking.Position.ROOT;
    
        System.out.printf("%nEnter income:%n");
        
        scanner.nextLine();
        String[] transactionDetails = {scanner.nextLine().strip()};
    
        synchronizeAccount();
        DataBaseReply reply;
        reply = database.processQuery(
                new DataBaseQuery(MODIFY_BALANCE, account, transactionDetails)
        );
    
        System.out.println(
                reply.isType(MODIFIED) ?
                "Income was added!" :
                reply.getDetails()[0]
        );
        System.out.println();
        
        return banking.Position.ACCOUNT;
    }
    
    static banking.Position doWithdrawal()
    {
        if (!loggedIn) return banking.Position.ROOT;

        System.out.print("%nEnter how much money you want to withdraw:%n");
    
        scanner.nextLine();
        String amount = scanner.nextLine().strip();
        String[] transactionDetails = {amount.startsWith("-") ? amount : "-" + amount};
    
        synchronizeAccount();
        DataBaseReply reply;
        reply = database.processQuery(
                new DataBaseQuery(MODIFY_BALANCE, account, transactionDetails)
        );
        
        System.out.println(
                reply.isType(MODIFIED) ?
                "Success!" :
                reply.getDetails()[0]
        );
        System.out.println();
        
        return banking.Position.ACCOUNT;
    }
    
    static banking.Position doTransfer()
    {
        if (!loggedIn) return banking.Position.ROOT;
    
        System.out.printf("%nTransfer%nEnter card number:%n");
    
        scanner.nextLine();
        String payeeCardNumber = scanner.nextLine().strip();
        banking.Account payeeWrapperAccount = banking.Account.createWrapperAccount(payeeCardNumber);
        
        if (cardNumber.equals(payeeCardNumber))
            System.out.println("You can't transfer money to the same account!");
        else
        if (!banking.Account.verifyChecksum(payeeCardNumber))
            System.out.println("Probably you made mistake in the card number. Please try again!");
        else
        if (
                database.processQuery(new DataBaseQuery(
                        CHECK_ACCOUNT_EXISTENCE, payeeWrapperAccount
                )).isNotType(EXISTS)
        )
            System.out.println("Such a card does not exist.");
        else
        {
            System.out.println("Enter how much money you want to transfer:");
            String amountToTransfer = scanner.nextLine().strip();
            
            synchronizeAccount();
    
            if (account.getBalance() < Integer.parseInt(amountToTransfer))
                System.out.println("Not enough money!");
            else
            {
                DataBaseReply reply;
                reply = database.processQuery(
                        new DataBaseQuery(DO_TRANSFER, account, new String[]{amountToTransfer}, payeeWrapperAccount)
                );
    
                System.out.println(
                        reply.isType(TRANSFERRED) ?
                        "Success!" :
                        reply.getDetails()[0]
                );
            }
        }
    
        System.out.println();
        return banking.Position.ACCOUNT;
    }
    
    static banking.Position closeAccount()
    {
        if (!loggedIn) return banking.Position.ROOT;
        
        synchronizeAccount();
        DataBaseReply reply;
        reply = database.processQuery(new DataBaseQuery(CLOSE_ACCOUNT, account));
    
        System.out.println();
        System.out.println(
                reply.isType(CLOSED) ?
                "The account has been closed!" :
                reply.getDetails()[0]
        );
        System.out.println();
    
        loggedIn = false;
        cardNumber = "";
        account = null;
        return banking.Position.ROOT;
    }
    
    
    // LOW-LEVEL
    
    private static void synchronizeAccount()
    {
        database.processQuery(new DataBaseQuery(SYNCHRONIZE_ACCOUNT, account));
    }
}
