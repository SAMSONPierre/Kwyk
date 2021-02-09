package Kwyk;

import java.awt.EventQueue;

public class Main {
    public static void main(String[]a){
        EventQueue.invokeLater(()-> new Control(new View()));
    }
}