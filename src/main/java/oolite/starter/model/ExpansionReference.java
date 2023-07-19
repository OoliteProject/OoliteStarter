/*
 */

package oolite.starter.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hiran
 */
public class ExpansionReference implements Comparable<ExpansionReference> {

    public enum Status {
        /**
         * This expansion is happy. All requirements are met and no conflicts detected.
         */
        OK, 
        /**
         * This expansion is missing. It was in the list wanted OXPs but is not installed.
         */
        MISSING, 
        /**
         * This expansion is surplus. It is installed despite it was not wanted.
         */
        SURPLUS,
        /**
         * This expansion is required by another expansion, yet it is not installed.
         */
        REQUIRED_MISSING,
        /**
         * This expansion is installed and conflicts with another installed expansion.
         */
        CONFLICT
    }

    private String name;
    private Status status;
    private List<String> reasons = new ArrayList<>();

    /**
     * Creates a new ExpansionReference.
     */
    public ExpansionReference() {
    }
    
    /**
     * Creates a new ExpansionReference.
     */
    public ExpansionReference(String name) {
        this.name = name;
    }
    
    /**
     * Creates a new ExpansionReference.
     */
    public ExpansionReference(String name, Status status) {
        this.name = name;
        this.status = status;
    }
    
    /**
     * Returns the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the status.
     * 
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * 
     * @param status the status
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    
    /**
     * Adds a reason to this reference.
     * 
     * @param reason the reason
     */
    public void addReason(String reason) {
        reasons.add(reason);
    }
    
    /**
     * Returns all reasons for this reference.
     * 
     * @return the reasons
     */
    public List<String> getReasons() {
        return reasons;
    }

    @Override
    public int compareTo(ExpansionReference other) {
        if (other == null) {
            return 1;
        }
        if (other == this) {
            return 0;
        }
        if (name == null) {
            return 1;
        }
        return this.name.compareTo(other.name);
    }

    @Override
    public int hashCode() {
        return ("" + name).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ExpansionReference er) {
            return compareTo(er) == 0;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "ExpansionReference{" + "name=" + name + ", status=" + status + '}';
    }

}
