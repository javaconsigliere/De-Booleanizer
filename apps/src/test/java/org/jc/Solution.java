package org.jc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Solution {

    static class Transaction
    {
        final String name;
        final int time;
        final float amount;
        final String city;
        Transaction(String token)
        {
            String[] tokens = token.split(",");
            int index = 0;
            name = tokens[index++];
            time = Integer.parseInt(tokens[index++]);
            amount = Float.parseFloat(tokens[index++]);
            city = tokens[index];
        }

        public String toString()
        {
            return name + "," + time + "," + amount + "," + city;
        }
    }
    public List<String> invalidTransactions(String[] transactions) {
        List<String> invalidTransactions = new ArrayList<>();
        List<Transaction> transactionList = new ArrayList<>();
        for(String transaction : transactions)
            transactionList.add( new Transaction(transaction));

        Map<String, Transaction> userLastTransaction = new HashMap<>();
        for(Transaction currentTr : transactionList)
        {
            Transaction userLastTr =  userLastTransaction.get(currentTr.name);
            if (userLastTr == null)
            {
                userLastTransaction.put(currentTr.name, currentTr);
                continue;
            }
            if (currentTr.amount > 1000)
            {
                invalidTransactions.add(currentTr.toString());
            }
            else if(currentTr.name.equals(userLastTr.name) &&
                    currentTr.time - userLastTr.time < 60 &&
                   !currentTr.city.equals(userLastTr.city))
            {
                invalidTransactions.add(currentTr.toString());
                invalidTransactions.add(userLastTr.toString());
                userLastTransaction.remove(currentTr.name);
            }
        }

        return invalidTransactions;
    }

    public boolean voweGame(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if ("aeiou".indexOf(c) != -1) {
                count++;
            }
        }
        return count % 2 == 0; // Returns true if the number of vowels is even
    }

}
