package hu.zza.hyperskill.banking;

import hu.zza.hyperskill.banking.menu.Menu;
import hu.zza.hyperskill.banking.menu.MenuEntry;
import hu.zza.hyperskill.banking.menu.MenuStructure;
import hu.zza.hyperskill.banking.menu.Position;


public class MenuInitializer
{
    static Menu initialize()
    {
        var menuStructure = new MenuStructure();
        
        
        //////////
        // NODES
        
        menuStructure.put(new MenuEntry.Node(Position.ROOT,
                                             "Simple Banking System",
                                             Position.EXIT,
                                             Position.CREATE_ACCOUNT,
                                             Position.LOGIN_ACCOUNT
        ));
        
        menuStructure.put(new MenuEntry.Node(Position.ACCOUNT,
                                             "Your account",
                                             Position.EXIT,
                                             Position.GET_BALANCE,
                                             Position.ADD_INCOME,
                                             // Position.DO_WITHDRAWAL,
                                             Position.DO_TRANSFER,
                                             Position.CLOSE_ACCOUNT,
                                             Position.LOGOUT_ACCOUNT
        ));
        
        
        
        //////////
        // LEAVES
        
        // Constant forward link arrays for the most typical cases.
        // The schema: FORWARD_<success>_<fail>
        final Position[] FORWARD_ROOT_ROOT = {Position.ROOT, Position.ROOT};
        final Position[] FORWARD_ACCOUNT_ROOT = {Position.ACCOUNT, Position.ROOT};
        
        // GENERAL leaf
        
        menuStructure.put(new MenuEntry.Leaf(Position.EXIT, "Exit", Main::exit, FORWARD_ROOT_ROOT));
        
        
        // ROOT (parent node)
        
        menuStructure.put(new MenuEntry.Leaf(
                Position.CREATE_ACCOUNT,
                "Create an account",
                Account::createNewAccount,
                FORWARD_ROOT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(Position.LOGIN_ACCOUNT,
                                             "Log into account",
                                             AccountManager::loginAccount,
                                             FORWARD_ACCOUNT_ROOT
        ));
        
        
        // ACCOUNT (parent node)
        
        menuStructure.put(new MenuEntry.Leaf(Position.GET_BALANCE,
                                             "Balance",
                                             AccountManager::getBalance,
                                             FORWARD_ACCOUNT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(Position.ADD_INCOME,
                                             "Add income",
                                             AccountManager::addIncome,
                                             FORWARD_ACCOUNT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(Position.DO_WITHDRAWAL,
                                             "Do withdrawal",
                                             AccountManager::doWithdrawal,
                                             FORWARD_ACCOUNT_ROOT
        ));
        
        
        menuStructure.put(new MenuEntry.Leaf(Position.DO_TRANSFER,
                                             "Do transfer",
                                             AccountManager::doTransfer,
                                             FORWARD_ACCOUNT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(Position.CLOSE_ACCOUNT,
                                             "Close account",
                                             AccountManager::closeAccount,
                                             FORWARD_ROOT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(Position.LOGOUT_ACCOUNT,
                                             "Log out",
                                             AccountManager::logoutAccount,
                                             FORWARD_ROOT_ROOT
        ));
        
        
        menuStructure.setFinalized();
        
        return new Menu(menuStructure);
    }
}