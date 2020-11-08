package hu.zza.hyperskill.banking;

import hu.zza.hyperskill.banking.db.DataBase;
import hu.zza.hyperskill.banking.db.ReplyType;
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
            if (connectDataBase())
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
    
    static int exit()
    {
        closeDataBase();
        
        System.out.printf("%nBye!");
        return 0;
    }
    
    private static boolean connectDataBase()
    {
        return DataBase.executeWithHysteresis(DATABASE::connect,
                                              ReplyType.CONNECTED,
                                              "The program could not open the connection to the database."
        );
    }
    
    private static boolean closeDataBase()
    {
        return DataBase.executeWithHysteresis(DATABASE::close,
                                              ReplyType.NOT_CONNECTED,
                                              "The program could not close the connection to the database."
        );
        
    }
}