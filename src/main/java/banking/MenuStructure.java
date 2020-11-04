package banking;

import java.util.HashMap;


abstract class MenuStructure
{
    static final HashMap<Position, banking.MenuEntry> MENU = new HashMap<>();
    
    static
    {
        //////////
        // NODES
        
        MENU.put(Position.ROOT, new banking.MenuEntry("Simple Banking System", new Position[] {
                Position.EXIT, Position.CREATE_ACCOUNT, Position.LOGIN_ACCOUNT
        }));
        
        MENU.put(Position.ACCOUNT, new banking.MenuEntry("Your account", new Position[] {
                Position.EXIT, Position.GET_BALANCE, Position.ADD_INCOME, // banking.Position.DO_WITHDRAWAL
                Position.DO_TRANSFER, Position.CLOSE_ACCOUNT, Position.LOGOUT_ACCOUNT
        }));
        
        
        
        //////////
        // LEAVES
        
        
        // GENERAL leaf
        
        MENU.put(Position.EXIT, new banking.MenuEntry("Exit", Main::exit));
        
        
        // ROOT (parent node)
        
        MENU.put(Position.CREATE_ACCOUNT, new banking.MenuEntry("Create an account", Account::createNewAccount));
        
        MENU.put(Position.LOGIN_ACCOUNT, new banking.MenuEntry("Log into account", AccountManager::loginAccount));
        
        
        // ACCOUNT (parent node)
        
        MENU.put(Position.GET_BALANCE, new banking.MenuEntry("Balance", AccountManager::getBalance));
        
        MENU.put(Position.ADD_INCOME, new banking.MenuEntry("Add income", AccountManager::addIncome));
        
        MENU.put(Position.DO_WITHDRAWAL, new banking.MenuEntry("Do withdrawal", AccountManager::doWithdrawal));
        
        
        MENU.put(Position.DO_TRANSFER, new banking.MenuEntry("Do transfer", AccountManager::doTransfer));
        
        MENU.put(Position.CLOSE_ACCOUNT, new banking.MenuEntry("Close account", AccountManager::closeAccount));
        
        MENU.put(Position.LOGOUT_ACCOUNT, new banking.MenuEntry("Log out", AccountManager::logoutAccount));
    }
}
