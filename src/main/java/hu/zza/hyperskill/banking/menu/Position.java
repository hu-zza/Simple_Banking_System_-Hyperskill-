package hu.zza.hyperskill.banking.menu;

/**
 * Object that represents menu's current position.
 * A position could symbolize a node or a leaf.
 */

public enum Position
{
    // NODES
    ROOT, ACCOUNT,
    
    // LEAVES
    EXIT,
    
    CREATE_ACCOUNT, CLOSE_ACCOUNT,
    
    LOGIN_ACCOUNT, LOGOUT_ACCOUNT,
    
    GET_BALANCE, ADD_INCOME, DO_WITHDRAWAL, DO_TRANSFER
}