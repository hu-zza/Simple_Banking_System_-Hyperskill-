package banking;

/**
 * Object that represents menu's current position.
 * A position could symbolize a node or a leaf.
 */

enum Position
{
    // NODES
    ROOT(false), ACCOUNT(false),
    
    // LEAVES
    EXIT(true),
    
    CREATE_ACCOUNT(true), CLOSE_ACCOUNT(true),
    
    LOGIN_ACCOUNT(true), LOGOUT_ACCOUNT(true),
    
    GET_BALANCE(true), ADD_INCOME(true), DO_WITHDRAWAL(true), DO_TRANSFER(true);
    
    
    final boolean isLeaf;
    
    Position(boolean isLeaf)
    {
        this.isLeaf = isLeaf;
    }
}