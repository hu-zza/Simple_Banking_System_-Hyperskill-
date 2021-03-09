package hu.zza.hyperskill.banking.menu;

import java.util.function.Supplier;


public abstract class MenuEntry {
    private final String            name;
    private final Position          position;
    private final Position[]        links;
    private final Supplier<Integer> function;
    private final NodePosition[]    functionLinks;
    
    
    // Full-fledged, low-level constructor
    private MenuEntry(Position position,
                      String name,
                      Position[] links,
                      Supplier<Integer> function,
                      NodePosition... functionLinks) {
        this.name          = name;
        this.position      = position;
        this.links         = links.clone();
        this.function      = function;
        this.functionLinks = functionLinks.clone();
    }
    
    
    ///////////////////////////////////////////////////
    // GETTERS ONLY FOR THE PACKAGE-PRIVATE PROCESSING
    
    
    String getName() {
        return name;
    }
    
    
    Position getPosition() {
        return position;
    }
    
    
    Position[] getLinks() {
        return links;
    }
    
    
    Supplier<Integer> getFunction() {
        return function;
    }
    
    
    Position[] getFunctionLinks() {
        return functionLinks;
    }
    
    
    /**
     * Menu performs this method on every selected MenuEntry, then redirects itself to the returning Position.
     * Nodes returns only with the Position of themselves, because selecting a Node means only this (navigating).
     * Leaves performs a function, the returning Position depends on the returning value of its function.
     *
     * @return The Position where the Menu redirects itself after selecting a MenuEntry.
     */
    Position select() {
        return getFunctionLinks()[getFunction().get()];
    }
    
    
    ///////////////////////////
    // INNER STATIC SUBCLASSES
    
    
    public static class Node extends MenuEntry {
        public Node(NodePosition position, String name, Position... links) {
            /*
            Constructor parameters in order:
            
            position        -   The position of this MenuEntry.
            name            -   Human-friendly name of this MenuEntry.
            links           -   An array of Positions of other reachable MenuEntries (Nodes and Leaves).
            
            function        -   "Placeholder" function, always returns 0.
            functionLinks   -   An array of Positions with only one element: The Position of this MenuEntry.
             */
            super(position, name, links, () -> 0, position);
        }
    }
    
    
    public static class Leaf extends MenuEntry {
        public Leaf(LeafPosition position, String name, Supplier<Integer> function, NodePosition... functionLinks) {
            /*
            Constructor parameters in order:

            position        -   The position of this MenuEntry
            name            -   Human-friendly name of this MenuEntry
            
            links           -   There is no link, because it's not a Node. You can not jump from the Position of a Leaf
                                to other Positions, because you never jump on that. You can choose the Position of a
                                Leaf from the links of a Node, then it's function performs and returns with an integer.
                                After that your position will be functionLinks[returnValue]. So Position of a Leaf is
                                never used as real position, it's only a reference.
           
            function        -   The essence of a Leaf. Performs a task and returns an integer (index for forwarding).
                                Return values: 0 is for the success, others are error codes.
                                
            functionLinks   -   An array of Positions: the forwarding options for a Leaf. The outcome of a Leaf's
                                function controls the forwarding: functionLinks[returnValue].
             */
            super(position, name, new Position[0], function, functionLinks);
        }
    }
}