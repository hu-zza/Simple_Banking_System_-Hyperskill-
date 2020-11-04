package banking;

import java.util.Scanner;

import static banking.DataBaseReply.ReplyType.*;
import static banking.DataBaseReply.ReplyType.CONNECTED;
import static banking.DataBaseReply.ReplyType.NOT_CONNECTED;
import static banking.Secret.*;

// compile group: 'mysql', name: 'mysql-connector-java', version: '8.0.22'

public class Main {
    private static boolean waitingForUserInput = true;
    final static Scanner scanner = new Scanner(System.in);
    final static DataBase database = new DataBase(URL, USERNAME, PASSWORD);
    
    public static void main(String[] args) {
        
        var menu = new banking.Menu();
        
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
    
    static banking.Position exit()
    {
        closeDataBase();
    
        System.out.printf("%nBye!");
        return banking.Position.ROOT;
    }
    
    private static boolean connectDataBase()
    {
        return DataBase.executeWithHysteresis(
                database::connect, CONNECTED, "The program could not open the connection to the database."
        );
    }
    
    private static boolean closeDataBase()
    {
        return DataBase.executeWithHysteresis(
                database::close, NOT_CONNECTED, "The program could not close the connection to the database."
        );
    
    }
}