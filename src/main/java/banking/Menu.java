package banking;

import java.util.HashMap;

class Menu
{
    private static final HashMap<Position, banking.MenuEntry> MENU = banking.MenuStructure.MENU;
    private static final banking.MenuEntry NULL_MENU_ENTRY = new banking.MenuEntry("", new Position[0]);
    
    private Position position;
    private Position[] availablePositions;
    
    Menu()
    {
        this(Position.ROOT);
    }
    
    Menu(Position position)
    {
        this.setPosition(position);
    }
    
    
    // INSTANCE METHODS
    
    void listOptions()
    {
        if (availablePositions.length == 0) return;
        
        for (int i = 1; i < availablePositions.length; i++)
        {
            System.out.printf(
                    "%d. %s%n",
                    i,
                    getMenuEntry(availablePositions[i]).getName()
            );
        }
    
        System.out.println("0. Exit");
    }
    
    void chooseOption(int optionNr)
    {
        if (0 <= optionNr && optionNr < availablePositions.length)
        {
            Position pos = availablePositions[optionNr];
            
            setPosition(
                    pos.isLeaf ?
                    getMenuEntry(pos).get() :
                    pos
            );
        }
        else
        {
            System.err.println("Incorrect or unavailable menu position.");
        }
    }
    
    private void setPosition(Position position)
    {
        this.position = position;
        availablePositions = getMenuEntry(position).getLinks();
    }
    
    private banking.MenuEntry getMenuEntry(Position position)
    {
        return MENU.getOrDefault(position, NULL_MENU_ENTRY);
    }
}
