package hu.zza.hyperskill.banking.menu;


public class Menu
{
    private final MenuStructure menu;
    private       Position      position;
    private       Position[]    options;
    
    public Menu(MenuStructure menu)
    {
        this(menu, NodePosition.ROOT);
    }
    
    public Menu(MenuStructure menu, Position position)
    {
        this.menu     = menu;
        this.position = position;
        refreshOptions();
    }
    
    
    // INSTANCE METHODS
    
    public void listOptions()
    {
        refreshOptions();
        if (options.length == 0) return;
        
        for (int i = 1; i < options.length; i++)
        {
            MenuEntry menuEntry = menu.get(options[i]);
            
            if (menuEntry != null)
            {
                System.out.printf("%d. %s%n", i, menuEntry.getName());
            }
        }
        
        System.out.println("0. Exit");
    }
    
    public void chooseOption(int optionNr)
    {
        refreshOptions();
        if (0 <= optionNr && optionNr < options.length)
        {
            if (menu.containsKey(options[optionNr]))
            {
                position = menu
                                   .get(options[optionNr])
                                   .select();
            }
        }
        else
        {
            System.err.println("Incorrect or unavailable menu position.");
        }
    }
    
    private void refreshOptions()
    {
        options = menu
                          .get(position)
                          .getLinks();
    }
    
}