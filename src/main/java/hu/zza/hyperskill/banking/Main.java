package hu.zza.hyperskill.banking;

import hu.zza.hyperskill.banking.db.DataBase;
import hu.zza.hyperskill.banking.menu.Menu;

import java.util.Scanner;

import static hu.zza.hyperskill.banking.Secret.PASSWORD;
import static hu.zza.hyperskill.banking.Secret.URL;
import static hu.zza.hyperskill.banking.Secret.USERNAME;


public class Main
{
    final static   Scanner  SCANNER             = new Scanner(System.in);
    final static   DataBase DATABASE            = new DataBase(URL, USERNAME, PASSWORD);
    private static boolean  waitingForUserInput = true;
    
    public static void main(String[] args)
    {
        Menu menu = MenuInitializer.initialize();
        
        try (SCANNER)
        {
            if (DATABASE.connect())
            {
                try (DATABASE)
                {
                    int selected;
                    while (waitingForUserInput)
                    {
                        menu.listOptions();
        
                        selected = SCANNER.nextInt();
        
                        menu.chooseOption(selected);
        
                        if (selected == 0) waitingForUserInput = false;
                    }
                }
            }
        }
    }
}