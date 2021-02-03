package ca.retrylife.marketcap.database.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Material;

/**
 * In intermediary object between {@link Material} objects and the database
 */
public class MaterialList {

    // Internal list
    private List<String> enumNames;

    /**
     * Create a MaterialList from a pre-existing list
     * 
     * @param commaSeparated A comma-separated string containing names, or empty, or
     *                       null
     */
    public MaterialList(String commaSeparated) {

        // Handle null
        if (commaSeparated == null) {

            // Init empty
            this.enumNames = new ArrayList<String>();

        } else {

            // Split by commas, and turn to list
            this.enumNames = new ArrayList<String>(Arrays.asList(commaSeparated.split(",")));

        }
    }

    /**
     * Check if a {@link Material} is contained in this list
     * 
     * @param mat {@link Material}
     * @return Is in the list?
     */
    public boolean contains(Material mat) {
        return this.contains(mat.toString());
    }

    /**
     * Check if a {@link String} is contained in this list
     * 
     * @param mat {@link String}
     * @return Is in the list?
     */
    public boolean contains(String name) {
        return this.enumNames.contains(name);
    }

    /**
     * Add a {@link Material} to this list
     * 
     * @param mat {@link Material}
     */
    public void add(Material mat) {
        add(mat.toString());
    }

    /**
     * Add a {@link String} to this list
     * 
     * @param name {@link String}
     */
    public void add(String name) {

        // Only add if new
        if (!this.contains(name)) {
            this.enumNames.add(name);
        }

    }

    /**
     * Remove a {@link Material} from this list
     * 
     * @param mat {@link Material}
     */
    public void remove(Material mat) {
        remove(mat.toString());
    }

    /**
     * Remove a {@link String} from this list
     * 
     * @param name {@link String}
     */
    public void remove(String name) {

        // Only if contains
        if (this.contains(name)) {
            this.enumNames.remove(name);
        }

    }

    /**
     * Get the internal list
     * 
     * @return {@link List}
     */
    public List<String> getList() {
        return this.enumNames;
    }

    @Override
    public String toString() {

        StringJoiner joiner = new StringJoiner(",");
        this.enumNames.forEach(joiner::add);
        return joiner.toString();

    }

}