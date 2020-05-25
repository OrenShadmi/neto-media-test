package com.netomedia.exam.hangman.player;

import com.netomedia.exam.hangman.model.ServerResponse;
import com.netomedia.exam.hangman.server.HangmanServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HangmanPlayer {

    private static HangmanServer server = new HangmanServer();



    public static void main(String[] args) throws Exception {
        //Implement your Hangman Player here
        ArrayList<String> dictionary;
        Map<Character, Integer> mostCommonLetterInWordMap;



        ServerResponse serverResponse = server.startNewGame();
        if(serverResponse != null){

            dictionary = readFromTxtFile();


            /** Filter dictionary by the length of the word returned from server */
            ServerResponse finalServerResponse = serverResponse;
            dictionary = (ArrayList<String>) dictionary.stream()
                                                     .filter(s -> s.length() == finalServerResponse.getHangman().length())
                                                     .collect(Collectors.toList());

            mostCommonLetterInWordMap = initMap();

                while(!serverResponse.getGameEnded()) {

                    mostCommonLetterInWordMap = countMostCommonLetterInWords(dictionary, mostCommonLetterInWordMap);
                    Map.Entry<Character, Integer> entry = findMax(mostCommonLetterInWordMap);
                    serverResponse = guess(serverResponse, String.valueOf(entry.getKey()));


                    /** guess untill a correct answer */
                    while(!serverResponse.isCorrect()) {
                        mostCommonLetterInWordMap.remove(entry.getKey());
                        entry = findMax(mostCommonLetterInWordMap);
                        serverResponse = guess(serverResponse, String.valueOf(entry.getKey()));
                    }

                    /** filter dictionary by index of a char */
                    Map.Entry<Character, Integer> finalEntry = entry;
                    ServerResponse finalServerResponse1 = serverResponse;
                    dictionary = (ArrayList<String>) dictionary.stream()
                            .filter(s -> s.indexOf(finalEntry.getKey()) == finalServerResponse1.getHangman().indexOf(finalEntry.getKey()))
                            .collect(Collectors.toList());
                    printDictionary(dictionary);

                    mostCommonLetterInWordMap.remove(entry.getKey());

                    if(dictionary.size() == 1){
                        serverResponse = guess(serverResponse, dictionary.get(0));
                    }
                }
        }


        System.out.println(serverResponse.toString());
    }

    private static Map<Character, Integer> countMostCommonLetterInWords(ArrayList<String> dictionary,
                                                                        Map<Character, Integer> mostCommonLetterInWordMap)  {
        for (Map.Entry<Character, Integer>  entry: mostCommonLetterInWordMap.entrySet()) {
            ArrayList<String> filteredDict = (ArrayList<String>) dictionary.stream()
                    .filter(s -> s.indexOf(entry.getKey()) != -1)
                    .collect(Collectors.toList());
            mostCommonLetterInWordMap.put(entry.getKey(), filteredDict.size());
        }

        return mostCommonLetterInWordMap;
    }

    public static Map.Entry<Character, Integer> findMax(Map<Character, Integer> map)  {
        Optional<Map.Entry<Character, Integer>> maxEntry = map.entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue));


        return maxEntry.get();
    }

    private static ServerResponse guess(ServerResponse serverResponse, String key) throws Exception {
        return server.guess(serverResponse.getToken(), key);
    }


    private static void printMap(Map<Character, Integer> map) {
        for (Map.Entry<Character,Integer> entry: map.entrySet()) {
            System.out.println("Key: " + entry.getKey() + " | " + "Value: " + entry.getValue());
        }
    }


    private static Map<Character,Integer> initMap(){
        Map<Character, Integer> map = new HashMap<>();
        String allLetters = "abcdefghijklmnopqrstuvwxyz";

        for (Character c : allLetters.toCharArray()) {
            map.put(c, 0);
        }

        return map;
    }




    private static ArrayList<String> readFromTxtFile() throws IOException {
        BufferedReader bufReader = new BufferedReader(new FileReader("src/main/resources/dictionary.txt"));
        ArrayList<String> listOfLines = new ArrayList<>();
        String line = bufReader.readLine();
        while (line != null) {

            listOfLines.add(line);
            line = bufReader.readLine();
        }

        printDictionary(listOfLines);

        bufReader.close();
        return listOfLines;

    }

    private static void printDictionary(ArrayList<String> listOfLines) {
        System.out.println("Number of words in dictionary: " + listOfLines.size());
//        listOfLines.stream().forEach(System.out::println);
    }
}
