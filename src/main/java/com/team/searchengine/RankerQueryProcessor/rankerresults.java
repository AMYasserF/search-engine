package com.team.searchengine.RankerQueryProcessor;

public class rankerresults {
   public String link;
   public String title;
  public  String description;

    public rankerresults() {
    }

    public rankerresults(String s, String f, String g) {
        link = s;
        title = f;
        description = g;
    }
}