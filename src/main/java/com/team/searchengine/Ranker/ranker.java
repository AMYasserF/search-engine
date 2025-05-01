package com.team.searchengine.Ranker;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ranker {

    double titleweight = 3, h1weight = 2, h2weight = 1.5, h3weight = 1.2;
    HashMap<String, Double> links = new HashMap<String, Double>();
    ArrayList<wordResult> romee = new ArrayList<wordResult>();
    ArrayList<String> sortedlinks = new ArrayList<String>();

    public void score(ArrayList<wordResult> r, ArrayList<String> words) {

        this.romee = r;
        // increase the rank of the links based on the number of times they appear in
        // the page
        // kl mra bzwd el rank bta3 el link l kol klma

        injectPageRanks(r);

        ///// to add rank based on the tags
        for (int j = 0; j < r.size(); j++) {
            for (int i = 0; i < r.get(j).Links.size(); i++) {

                if (r.get(j).Headers.get(i).get(0) == true) {
                    if (links.containsKey(r.get(j).Links.get(i)))
                        links.put(r.get(j).Links.get(i), links.get(r.get(j).Links.get(i)) + 5 * h1weight);
                    else
                        links.put(r.get(j).Links.get(i), h1weight);
                }
                if (r.get(j).Headers.get(i).get(1) == true) {
                    if (links.containsKey(r.get(j).Links.get(i)))
                        links.put(r.get(j).Links.get(i), links.get(r.get(j).Links.get(i)) + 5 * h2weight);
                    else
                        links.put(r.get(j).Links.get(i), h2weight);
                }
                if (r.get(j).Headers.get(i).get(2) == true) {
                    if (links.containsKey(r.get(j).Links.get(i)))
                        links.put(r.get(j).Links.get(i), links.get(r.get(j).Links.get(i)) + 5 * h3weight);
                    else
                        links.put(r.get(j).Links.get(i), h3weight);
                }

            }
        }
        ///// to add rank based on the tf*idf

        for (int j = 0; j < r.size(); j++) {
            for (int i = 0; i < r.get(j).Links.size(); i++) {
                if (links.containsKey(r.get(j).Links.get(i)))
                    links.put(r.get(j).Links.get(i),
                            links.get(r.get(j).Links.get(i)) + 15 * r.get(j).TF.get(i) * r.get(j).idf);
                else
                    links.put(r.get(j).Links.get(i), 15 * r.get(j).TF.get(i) * r.get(j).idf);
            }
        }
        /// to add a rank if the words is found in the title
        for (int j = 0; j < r.size(); j++) {
            for (int i = 0; i < r.get(j).Titles.size(); i++) {
                String Str = new String(r.get(j).Titles.get(i));
                if (Str.toLowerCase().contains(words.get(j).toLowerCase())) {
                    if (links.containsKey(r.get(j).Links.get(i)))
                        links.put(r.get(j).Links.get(i), links.get(r.get(j).Links.get(i)) + 5 * titleweight);
                    else
                        links.put(r.get(j).Links.get(i), 5 * titleweight);
                }
            }
        }

        ////// to add the rank of pagerank
        for (int j = 0; j < r.size(); j++) {
            for (int i = 0; i < r.get(j).Links.size(); i++) {
                if (links.containsKey(r.get(j).Links.get(i)))
                    links.put(r.get(j).Links.get(i), links.get(r.get(j).Links.get(i)) + 20 * r.get(j).ranks.get(i));
                else
                    links.put(r.get(j).Links.get(i), 20.0 * r.get(j).ranks.get(i));
            }
        }

    }
    // sort elements by values

    void sortByValue() {

        sortedlinks.clear();

        // convert HashMap into List
        List<Entry<String, Double>> list = new ArrayList<>(links.entrySet());

        // sorting the list elements
        Collections.sort(list, new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        for (Entry<String, Double> entry : list) {
            this.sortedlinks.add(entry.getKey());
        }

    }

    public ArrayList<rankerresults> setresults() {
        ArrayList<rankerresults> results = new ArrayList<rankerresults>();

        // Build a map: link -> (title, description)
        HashMap<String, String[]> linkInfo = new HashMap<>();
        for (int j = 0; j < this.romee.size(); j++) {
            for (int i = 0; i < romee.get(j).Links.size(); i++) {
                String link = romee.get(j).Links.get(i);
                String title = romee.get(j).Titles.get(i);
                String description = romee.get(j).Discreption.get(i);

                // If not already added, add it (keep the first occurrence only)
                if (!linkInfo.containsKey(link)) {
                    linkInfo.put(link, new String[] { title, description });
                }
            }
        }

        // Now create the results list based on sorted links
        for (int i = 0; i < sortedlinks.size(); i++) {
            String link = sortedlinks.get(i);
            if (linkInfo.containsKey(link) && !containsLink(results, link)) {
                rankerresults result = new rankerresults();
                result.link = link;
                result.title = linkInfo.get(link)[0]; // get title
                result.description = linkInfo.get(link)[1]; // get description
                results.add(result);
            }
        }

        return results;
    }

    // Helper method to check if the link already exists in results
    private boolean containsLink(ArrayList<rankerresults> results, String link) {
        for (rankerresults result : results) {
            if (result.link.equals(link)) {
                return true; // link already exists
            }
        }
        return false; // link not found
    }

    private void injectPageRanks(ArrayList<wordResult> results) {
        Map<String, Set<String>> outLinks = PageRankCalculator.buildOutLinksFromDatabase();
        Map<String, Double> pageRanks = PageRankCalculator.computePageRank(outLinks);

        // Inject PageRank into wordResults
        for (wordResult wr : results) {
            wr.ranks.clear();
            for (int i = 0; i < wr.Links.size(); i++) {
                String target = wr.Links.get(i);
                Double rank = pageRanks.getOrDefault(target, 0.0); // Fetch PageRank
                wr.addranks(rank);
            }
        }
    }

}
