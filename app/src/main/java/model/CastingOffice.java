package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Room that stores rank upgrade costs for dollars and credits.
 */
public class CastingOffice extends Room {
    private Map<Integer, Integer> dollarCosts;
    private Map<Integer, Integer> creditCosts;

    public CastingOffice(String name) {
        this.name = name;
        this.dollarCosts = new HashMap<Integer, Integer>();
        this.creditCosts = new HashMap<Integer, Integer>();
    }

    /**
     * Adds one upgrade cost read from board.xml.
     */
    public void addUpgradeCost(Integer rank, String currency, Integer amount) {
        if ("dollar".equals(currency)) {
            dollarCosts.put(rank, amount);
        } else if ("credit".equals(currency)) {
            creditCosts.put(rank, amount);
        }
    }

    public Integer getUpgradeCost(Integer rank, String currency) {
        if ("dollar".equals(currency)) {
            return dollarCosts.get(rank);
        } else if ("credit".equals(currency)) {
            return creditCosts.get(rank);
        }

        return null;
    }

    public Integer getDollarCost(Integer rank) {
        return dollarCosts.get(rank);
    }

    public Integer getCreditCost(Integer rank) {
        return creditCosts.get(rank);
    }

    public Map<Integer, Integer> getDollarCosts() {
        return dollarCosts;
    }

    public Map<Integer, Integer> getCreditCosts() {
        return creditCosts;
    }

    /**
     * Returns all ranks that can be bought in the casting office.
     */
    public Set<Integer> getUpgradeRanks() {
        Set<Integer> ranks = new HashSet<Integer>();
        ranks.addAll(dollarCosts.keySet());
        ranks.addAll(creditCosts.keySet());
        return new TreeSet<Integer>(ranks);
    }
}
