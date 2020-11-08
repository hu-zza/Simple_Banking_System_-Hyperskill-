package hu.zza.hyperskill.banking.menu;

import java.util.HashMap;


public class MenuStructure
{
    private final HashMap<Position, MenuEntry> menu = new HashMap<>();
    private       boolean                      finalized;
    
    public void setFinalized()
    {
        finalized = true;
    }
    
    public MenuEntry put(MenuEntry menuEntry)
    {
        if (!finalized) return menu.put(menuEntry.getPosition(), menuEntry);
        return null;
    }
    
    public boolean containsKey(Position position)
    {
        return menu.containsKey(position);
    }
    
    public MenuEntry get(Position position)
    {
        return menu.get(position);
    }
}
