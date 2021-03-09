package hu.zza.hyperskill.banking;

import hu.zza.hyperskill.banking.menu.LeafPosition;
import hu.zza.hyperskill.banking.menu.Menu;
import hu.zza.hyperskill.banking.menu.MenuEntry;
import hu.zza.hyperskill.banking.menu.MenuStructure;
import hu.zza.hyperskill.banking.menu.NodePosition;


public class MenuInitializer {
    static Menu initialize() {
        var menuStructure = new MenuStructure();
        
        
        //////////
        // NODES
        
        menuStructure.put(new MenuEntry.Node(NodePosition.ROOT, "Simple Banking System", LeafPosition.EXIT,
                                             LeafPosition.CREATE_ACCOUNT, LeafPosition.LOGIN_ACCOUNT));
        
        menuStructure.put(
                new MenuEntry.Node(NodePosition.ACCOUNT, "Your account", LeafPosition.EXIT, LeafPosition.GET_BALANCE,
                                   LeafPosition.ADD_INCOME,
                                   // Position.DO_WITHDRAWAL,
                                   LeafPosition.DO_TRANSFER, LeafPosition.CLOSE_ACCOUNT, LeafPosition.LOGOUT_ACCOUNT));
        
        
        
        //////////
        // LEAVES
        
        // Constant forward link arrays for the most typical cases.
        // The schema: FORWARD_<success>_<fail>
        
        final NodePosition[] FORWARD_ROOT_ROOT    = {NodePosition.ROOT, NodePosition.ROOT};
        final NodePosition[] FORWARD_ACCOUNT_ROOT = {NodePosition.ACCOUNT, NodePosition.ROOT};
        
        
        // GENERAL leaf
        
        menuStructure.put(new MenuEntry.Leaf(LeafPosition.EXIT, "Exit", () -> {
            System.out.printf("%nBye!%n%n");
            return 0;
        }, FORWARD_ROOT_ROOT));
        
        
        // ROOT (parent node)
        
        menuStructure.put(
                new MenuEntry.Leaf(LeafPosition.CREATE_ACCOUNT, "Create an account", Account::createNewAccount,
                                   FORWARD_ROOT_ROOT));
        
        menuStructure.put(
                new MenuEntry.Leaf(LeafPosition.LOGIN_ACCOUNT, "Log into account", AccountManager::loginAccount,
                                   FORWARD_ACCOUNT_ROOT));
        
        
        // ACCOUNT (parent node)
        
        menuStructure.put(new MenuEntry.Leaf(LeafPosition.GET_BALANCE, "Balance", AccountManager::getBalance,
                                             FORWARD_ACCOUNT_ROOT));
        
        menuStructure.put(new MenuEntry.Leaf(LeafPosition.ADD_INCOME, "Add income", AccountManager::addIncome,
                                             FORWARD_ACCOUNT_ROOT));
        
        menuStructure.put(new MenuEntry.Leaf(LeafPosition.DO_WITHDRAWAL, "Do withdrawal", AccountManager::doWithdrawal,
                                             FORWARD_ACCOUNT_ROOT));
        
        
        menuStructure.put(new MenuEntry.Leaf(LeafPosition.DO_TRANSFER, "Do transfer", AccountManager::doTransfer,
                                             FORWARD_ACCOUNT_ROOT));
        
        menuStructure.put(new MenuEntry.Leaf(LeafPosition.CLOSE_ACCOUNT, "Close account", AccountManager::closeAccount,
                                             FORWARD_ROOT_ROOT));
        
        menuStructure.put(new MenuEntry.Leaf(LeafPosition.LOGOUT_ACCOUNT, "Log out", AccountManager::logoutAccount,
                                             FORWARD_ROOT_ROOT));
        
        
        menuStructure.setFinalized();
        
        return new Menu(menuStructure);
    }
}