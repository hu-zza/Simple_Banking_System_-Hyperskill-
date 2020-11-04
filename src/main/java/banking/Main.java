package banking;

import java.util.Scanner;

import static banking.DataBaseReply.ReplyType.CONNECTED;
import static banking.DataBaseReply.ReplyType.NOT_CONNECTED;
import static banking.Secret.PASSWORD;
import static banking.Secret.URL;
import static banking.Secret.USERNAME;


public class Main
{
    final static   Scanner  scanner             = new Scanner(System.in);
    final static   DataBase database            = new DataBase(URL, USERNAME, PASSWORD);
    private static boolean  waitingForUserInput = true;
    
    public static void main(String[] args)
    {
        
        var menu = new Menu();
        
        try (scanner)
        {
            if (connectDataBase())
            {
                int selected;
                while (waitingForUserInput)
                {
                    menu.listOptions();
                    
                    selected = scanner.nextInt();
                    menu.chooseOption(selected);
                    
                    if (selected == 0) waitingForUserInput = false;
                }
            }
        }
    }
    
    static Position exit()
    {
        closeDataBase();
        
        System.out.printf("%nBye!");
        return Position.ROOT;
    }
    
    private static boolean connectDataBase()
    {
        return DataBase.executeWithHysteresis(database::connect,
                                              CONNECTED,
                                              "The program could not open the connection to the database."
        );
    }
    
    private static boolean closeDataBase()
    {
        return DataBase.executeWithHysteresis(database::close,
                                              NOT_CONNECTED,
                                              "The program could not close the connection to the database."
        );
        
    }
}