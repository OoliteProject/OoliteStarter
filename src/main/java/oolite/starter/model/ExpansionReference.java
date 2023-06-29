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

    public String name;
    public Status status;

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
    public String toString() {
        return "ExpansionReference{" + "name=" + name + ", status=" + status + '}';
    }

}
