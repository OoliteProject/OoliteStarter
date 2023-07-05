/*
 */

package oolite.starter.model;

/**
 *
 * @author hiran
 */
public class ExpansionReference implements Comparable<ExpansionReference> {

    public enum Status {
        OK, MISSING, SURPLUS
    }

    private String name;
    private Status status;

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
