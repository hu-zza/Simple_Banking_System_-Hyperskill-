package banking;

import java.util.HashMap;


abstract class MenuStructure
{
    static final HashMap<Position, MenuEntry> MENU = new HashMap<>();
    
    static
    {
        //////////
        // NODES
        
        MENU.put(Position.ROOT, new MenuEntry("Simple Banking System", new Position[] {
                Position.EXIT, Position.CREATE_ACCOUNT, Position.LOGIN_ACCOUNT
        }));
        
        MENU.put(Position.ACCOUNT, new MenuEntry("Your account", new Position[] {
                Position.EXIT, Position.GET_BALANCE, Position.ADD_INCOME, // banking.Position.DO_WITHDRAWAL
                Position.DO_TRANSFER, Position.CLOSE_ACCOUNT, Position.LOGOUT_ACCOUNT
        }));
        
        
        
        //////////
        // LEAVES
        
        
        // GENERAL leaf
        
        MENU.put(Position.EXIT, new MenuEntry("Exit", Main::exit));
        
        
        // ROOT (parent node)
        
        MENU.put(Position.CREATE_ACCOUNT, new MenuEntry("Create an account", Account::createNewAccount));
        
        MENU.put(Position.LOGIN_ACCOUNT, new MenuEntry("Log into account", AccountManager::loginAccount));
        
        
        // ACCOUNT (parent node)
        
        MENU.put(Position.GET_BALANCE, new MenuEntry("Balance", AccountManager::getBalance));
        
        MENU.put(Position.ADD_INCOME, new MenuEntry("Add income", AccountManager::addIncome));
        
        MENU.put(Position.DO_WITHDRAWAL, new MenuEntry("Do withdrawal", AccountManager::doWithdrawal));
        
        
        MENU.put(Position.DO_TRANSFER, new MenuEntry("Do transfer", AccountManager::doTransfer));
        
        MENU.put(Position.CLOSE_ACCOUNT, new MenuEntry("Close account", AccountManager::closeAccount));
        
        MENU.put(Position.LOGOUT_ACCOUNT, new MenuEntry("Log out", AccountManager::logoutAccount));
    }
}
