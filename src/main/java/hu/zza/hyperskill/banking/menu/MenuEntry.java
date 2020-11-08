package hu.zza.hyperskill.banking.menu;

import java.util.function.Supplier;


public abstract class MenuEntry
{
    private final String            name;
    private final Position          position;
    private final Position[]        links;
    private final Supplier<Integer> function;
    private final Position[]        forwardLinks;
    
    
    // Full-fledged, low-level constructor
    private MenuEntry(Position position,
                      String name,
                      Position[] links,
                      Supplier<Integer> function,
                      Position... forwardLinks
    )
    {
        this.name         = name;
        this.position     = position;
        this.links        = links.clone();
        this.function     = function;
        this.forwardLinks = forwardLinks.clone();
    }
    
    String getName()
    {
        return name;
    }
    
    Position getPosition()
    {
        return position;
    }
    
    Position[] getLinks()
    {
        return links;
    }
    
    Supplier<Integer> getFunction()
    {
        return function;
    }
    
    Position[] getForwardLinks()
    {
        return forwardLinks;
    }
    
    abstract Position select();
    
    
    ///////////////////////
    // INNER STATIC CLASSES
    
    public static class Node extends MenuEntry
    {
        public Node(Position position, String name, Position... links)
        {
            super(position, name, links, () -> 0, position);
        }
        
        @Override
        Position select()
        {
            // There is no forward, a Node returns the Position of itself.
            return getForwardLinks()[0];
        }
    }
    
    
    public static class Leaf extends MenuEntry
    {
        public Leaf(Position position, String name, Supplier<Integer> function, Position... forwardLinks)
        {
            super(position, name, new Position[0], function, forwardLinks);
        }
        
        @Override
        Position select()
        {
            // Supplier returns an integer: 0 (OK), or other (error codes).
            // Forwarding in accord to this code.
            return getForwardLinks()[getFunction().get()];
        }
    }
}