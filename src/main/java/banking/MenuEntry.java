package banking;

import java.util.function.Supplier;


/**
 * Object that represents one node or one leaf of the menu.
 * A node has a banking.Position array with nodes / leaves. (It has no function.)
 * Nodes are for navigating in the menu.
 * A leaf has a function, but it has no banking.Position array.
 * Leaves are for implementing the programs functionality.
 */

class MenuEntry
{
    private final String             NAME;
    private final Position[]         LINKS;
    private final Supplier<Position> FUNCTION;
    
    
    
    // Constructor for nodes
    MenuEntry(String name, Position[] links)
    {
        this(name, links, MenuEntry::nullFunction);
    }
    
    // Constructor for leaves
    MenuEntry(String name, Supplier<Position> function)
    {
        this(name, new Position[] {}, function);
    }
    
    // Full-fledged, low-level constructor
    private MenuEntry(String name, Position[] links, Supplier<Position> function)
    {
        this.NAME     = name;
        this.LINKS    = links.clone();
        this.FUNCTION = function;
    }
    
    static Position nullFunction()
    {
        System.err.println("This banking.MenuEntry has no function.");
        return Position.ROOT;
    }
    
    
    // INSTANCE METHODS
    
    String getNAME()
    {
        return NAME;
    }
    
    Position[] getLINKS()
    {
        return LINKS;
    }
    
    Position get()
    {
        return FUNCTION.get();
    }
}
