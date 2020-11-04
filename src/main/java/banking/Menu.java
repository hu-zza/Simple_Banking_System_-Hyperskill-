package banking;

import java.util.HashMap;


class Menu
{
    private static final HashMap<Position, MenuEntry> MENU = MenuStructure.MENU;
    private static final MenuEntry                    NULL = new MenuEntry("", new Position[0]);
    
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
            System.out.printf("%d. %s%n", i, getMenuEntry(availablePositions[i]).getName());
        }
        
        System.out.println("0. Exit");
    }
    
    void chooseOption(int optionNr)
    {
        if (0 <= optionNr && optionNr < availablePositions.length)
        {
            Position pos = availablePositions[optionNr];
            
            setPosition(pos.isLeaf ? getMenuEntry(pos).get() : pos);
        }
        else
        {
            System.err.println("Incorrect or unavailable menu position.");
        }
    }
    
    private void setPosition(Position position)
    {
        availablePositions = getMenuEntry(position).getLinks();
    }
    
    private MenuEntry getMenuEntry(Position position)
    {
        return MENU.getOrDefault(position, NULL);
    }
}