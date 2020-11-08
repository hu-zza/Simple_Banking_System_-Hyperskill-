package hu.zza.hyperskill.banking.menu;

import java.util.function.Supplier;


public abstract class MenuEntry
{
    final         Supplier<Integer> function;
    final         Position[]        forwardLinks;
    private final String            name;
    private final Position          position;
    private final Position[]        links;
    
    
    
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
    
    public String getName()
    {
        return name;
    }
    
    public Position getPosition()
    {
        return position;
    }
    
    public Position[] getLinks()
    {
        return links;
    }
    
    public abstract Position select();
    
    
    
    public static class Node extends MenuEntry
    {
        public Node(Position position, String name, Position... links)
        {
            super(position, name, links, () -> 0, position);
        }
        
        @Override
        public Position select()
        {
            return forwardLinks[0];
        }
    }
    
    
    public static class Leaf extends MenuEntry
    {
        public Leaf(Position position, String name, Supplier<Integer> function, Position... forwardLinks)
        {
            super(position, name, new Position[0], function, forwardLinks);
        }
        
        @Override
        public Position select()
        {
            return forwardLinks[function.get()];
        }
    }
}