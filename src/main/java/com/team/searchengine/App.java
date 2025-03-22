package com.team.searchengine;

import com.team.searchengine.indexer.Indexer;

public class App 
{
    public static void main(String[] args) 
    {
        System.out.println("Testing Indexer...");
        Indexer.main(args); // Call the Indexer's main method
        System.out.println("Indexer test completed.");
    }
}
