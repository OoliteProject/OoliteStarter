/*
 */
package oolite.starter.model;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oolite.starter.Oolite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class Expansion implements Comparable<Expansion> {
    private static final Logger log = LogManager.getLogger();
    
    private static final String EXPANSION_OOLITE_MUST_BE_SET = "oolite must be set before";
    
    /**
     * Expansion Manager status fields.
     * See https://wiki.alioth.net/index.php/Expansions_Manager
     */
    public static class EMStatus {
        private Color color;
        private Expansion latest;
        private boolean update;
        private boolean incompatible;
        private List<Expansion> conflicting = new ArrayList<>();
        private List<Expansion> missing = new ArrayList<>();
        private List<Expansion> requiredBy = new ArrayList<>();

        /**
         * Create a new status.
         */
        public EMStatus() {
        }

        /**
         * Create a new status with parameters.
         */
        public EMStatus(Color color, Expansion latest, boolean incompatible) {
            this.color = color;
            this.latest = latest;
            this.incompatible = incompatible;
        }

        /**
         * Returns true if this expansion can be installed as update.
         * 
         * @return whether this expansion is an update
         */
        public boolean isUpdate() {
            return update;
        }

        /**
         * Sets whether this expansion is an update for some other one.
         * 
         * @param update if this expansion is the newer version of something else
         */
        public void setUpdate(boolean update) {
            this.update = update;
        }

        /**
         * Returns the intended color.
         */
        public Color getColor() {
            return color;
        }

        /**
         * Sets the intended color.
         */
        public void setColor(Color color) {
            this.color = color;
        }

        /**
         * Returns if the version is the latest.
         * 
         * @return true if no expansion
         */
        public boolean isLatest() {
            return latest==null;
        }

        /**
         * Sets the latest expansion.
         * To mark an expansion to be the latest, set this property to null.
         */
        public void setLatest(Expansion latest) {
            this.latest = latest;
        }
        
        /**
         * Returns the expansion that should be updated to.
         * 
         * @return the expansion
         */
        public Expansion getLatest() {
            return latest;
        }

        /**
         * Returns if the expansion is conflicting.
         */
        public boolean isConflicting() {
            return !conflicting.isEmpty();
        }

        /**
         * Returns the expansion this expansion is conflicting with.
         * 
         * @return the list of expansions
         */
        public List<Expansion> getConflicting() {
            return this.conflicting;
        }

        /**
         * Returns if required other expansions are missing.
         */
        public boolean isMissingDeps() {
            return !missing.isEmpty();
        }

        /**
         * Returns the list of unresolved expansions.
         * 
         * @return the list of expansions
         */
        public List<Expansion> getMissing() {
            return this.missing;
        }
        
        /**
         * Returns if this expansion is required by others.
         * 
         * @return true if it is required, false otherwise
         */
        public boolean isRequired() {
            return !requiredBy.isEmpty();
        }
        
        /**
         * Returns if this expansion is required by currently installed ones.
         * 
         * @return true if it is required, false otherwise
         */
        public boolean isCurrentlyRequired() {
            for (Expansion r: requiredBy) {
                if (r.isEnabled()) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Returns the list of expansions requiring this one.
         * 
         * @return the list of expansions
         */
        public List<Expansion> getRequiredBy() {
            return this.requiredBy;
        }

        /**
         * Returns if the expansion is compatible with the current version of Oolite.
         */
        public boolean isIncompatible() {
            return incompatible;
        }

        /**
         * Sets if the expansion is compatible with the current version of Oolite.
         */
        public void setIncompatible(boolean incompatible) {
            this.incompatible = incompatible;
        }

    }
    
    public static class Dependency {
        private String identifier;
        private String version;
        private String description;
        private String maximumVersion;

        /**
         * Creates a new Dependency.
         */
        public Dependency() {
        }
        
        /**
         * Creates a new Dependency with given identifier.
         */
        public Dependency(String identifier) {
            this.identifier = identifier;
        }
        
        /**
         * Creates a new Dependency with given identifier and version.
         */
        public Dependency(String identifier, String version) {
            this.identifier = identifier;
            this.version = version;
        }

        /**
         * Creates a new Dependency with given identifier, version and maximumVersion.
         */
        public Dependency(String identifier, String version, String maximumVersion) {
            this.identifier = identifier;
            this.version = version;
            this.maximumVersion = maximumVersion;
        }

        /**
         * Returns the referenced expansion's identifier.
         * @return the identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Sets the referenced expansion's identifier.
         * @param identifier the identifier
         */
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        /**
         * Returns the referenced expansion's version.
         * 
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        /**
         * Sets the referenced expansion's version.
         * 
         * @param version the version
         */
        public void setVersion(String version) {
            this.version = version;
        }

        /**
         * Returns the dependency description.
         * 
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the dependency description.
         * 
         * @param description the description
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * Returns the maximum version to match this dependency.
         * 
         * @return the version
         */
        public String getMaximumVersion() {
            return maximumVersion;
        }

        /**
         * Sets the maximum version to match this dependency.
         * 
         * @param maximumVersion the version
         */
        public void setMaximumVersion(String maximumVersion) {
            this.maximumVersion = maximumVersion;
        }

        @Override
        public String toString() {
            return "Dependency{" + "identifier=" + identifier + ", version=" + version + ", description=" + description + ", maximumVersion=" + maximumVersion + '}';
        }
        
    }
    
    private String author;
    private String category;
    private List<Dependency> conflictOxps;
    private String description;
    private String downloadUrl;
    private long fileSize;
    private String identifier;
    private String informationUrl;
    private String license;
    private String maximumOoliteVersion;
    private List<Dependency> optionalOxps;
    private String requiredOoliteVersion;
    private List<Dependency> requiresOxps;
    private String tags;
    private String title;
    private LocalDateTime uploadDate;
    private String version;
    
    private Oolite oolite;
    
    private boolean online;
    private File localFile;
    private EMStatus emStatus = new EMStatus();
    
    private PropertyChangeSupport pcs;

    /**
     * Creates a new Expansion.
     */
    public Expansion() {
        pcs = new PropertyChangeSupport(this);
    }
    
    /**
     * Adds a propertyChangeListener.
     * 
     * @param pcl the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    /**
     * Removes a propertyChangeListener.
     * 
     * @param pcl the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    /**
     * Returns the author.
     * 
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     * 
     * @param author the author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the category.
     * 
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category.
     * 
     * @param category the category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns conflicting OXPs.
     * 
     * @return the list of OXPs.
     */
    public List<Dependency> getConflictOxps() {
        return conflictOxps;
    }

    /**
     * Sets conflicting OXPs.
     * 
     * @param conflictOxps the list of OXPs.
     */
    public void setConflictOxps(List<Dependency> conflictOxps) {
        this.conflictOxps = conflictOxps;
    }

    /**
     * Returns the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * 
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the download url.
     * 
     * @return the url
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Sets the download url.
     * 
     * @param downloadUrl the url
     */
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * Returns the file size.
     * 
     * @return size in bytes
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the file size.
     * 
     * @param fileSize size in bytes
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Returns the identifier.
     * 
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier.
     * 
     * @param identifier the identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the information url.
     * 
     * @return the url
     */
    public String getInformationUrl() {
        return informationUrl;
    }

    /**
     * Sets the information url.
     * 
     * @param informationUrl the url
     */
    public void setInformationUrl(String informationUrl) {
        this.informationUrl = informationUrl;
    }

    /**
     * Returns the license.
     * 
     * @return the license
     */
    public String getLicense() {
        return license;
    }

    /**
     * Sets the license.
     * 
     * @param license the license
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * Returns the maximum oolite version.
     * 
     * @return the version string
     */
    public String getMaximumOoliteVersion() {
        return maximumOoliteVersion;
    }

    /**
     * Sets the maximum oolite version.
     * 
     * @param maximumOoliteVersion the version string
     */
    public void setMaximumOoliteVersion(String maximumOoliteVersion) {
        this.maximumOoliteVersion = maximumOoliteVersion;
    }

    /**
     * Returns the optional OXPs list.
     * 
     * @return the list of OXPs
     */
    public List<Dependency> getOptionalOxps() {
        return optionalOxps;
    }

    /**
     * Sets the optional OXPs list.
     * 
     * @param optionalOxps the list of OXPs
     */
    public void setOptionalOxps(List<Dependency> optionalOxps) {
        this.optionalOxps = optionalOxps;
    }

    /**
     * Returns the required Oolite version number.
     * 
     * @return the version string
     */
    public String getRequiredOoliteVersion() {
        return requiredOoliteVersion;
    }

    /**
     * Sets the required Oolite version number.
     * 
     * @param requiredOoliteVersion the version string
     */
    public void setRequiredOoliteVersion(String requiredOoliteVersion) {
        this.requiredOoliteVersion = requiredOoliteVersion;
    }

    /**
     * Returns the OXPs required by this OXP.
     * 
     * @return the OXP list
     */
    public List<Dependency> getRequiresOxps() {
        return requiresOxps;
    }

    /**
     * Sets the OXPs required by this OXP.
     * 
     * @param requiresOxps the OXP list
     */
    public void setRequiresOxps(List<Dependency> requiresOxps) {
        this.requiresOxps = requiresOxps;
    }

    /**
     * Returns the tags.
     * 
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * Sets the tags.
     * 
     * @param tags the tags
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * Returns the title.
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     * 
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the upload date.
     * 
     * @return the date
     */
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    /**
     * Sets the upload date.
     * 
     * @param uploadDate the date
     */
    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    /**
     * Returns the expansion's version.
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the expansion's version.
     * 
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the online status.
     * 
     * @return true if and only if the expansion is found online
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Sets the online status.
     * 
     * @param online true if and only if the expansion is found online
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Returns the local status.
     * 
     * @return true if and only if the expansion is found locally
     */
    public boolean isLocal() {
        return localFile != null;
    }

    /**
     * Returns the active status.
     * If it is not installed, it is also not enabled.
     * 
     * @return true if and only if the expansion is visible for Oolite.
     */
    public boolean isEnabled() {
        if (oolite == null) {
            throw new IllegalStateException(EXPANSION_OOLITE_MUST_BE_SET);
        }
        if (localFile == null) {
            return false;
        }
        // check if our localFile is contained in the disabled folder
        try {
            return !oolite.isDisabled(this);
        } catch (IOException e) {
            log.info("Could not check whether expansion is disabled", e);
            return false;
        }
    }
    
    /**
     * Returns true if this expansion is managed.
     * 
     * @return 
     */
    public boolean isManaged() {
        try {
            return oolite.isManaged(this);
        } catch (IOException e) {
            log.info("Could not determine isManaged", e);
            return false;
        }
    }

    /**
     * Returns the oolite handler for this expansion.
     * 
     * @return the handler
     */
    public Oolite getOolite() {
        return oolite;
    }

    /**
     * Sets the oolite handler for this expansion.
     * 
     * @param oolite the handler
     */
    public void setOolite(Oolite oolite) {
        this.oolite = oolite;
    }

    /**
     * Returns the installation directory (OXP) or file (OXZ) of this expansion.
     * 
     * @return the directory/file
     */
    public File getLocalFile() {
        return localFile;
    }

    /**
     * Sets the installation directory (OXP) or file (OXZ) of this expansion.
     * 
     * @param localFile the directory/file
     */
    public void setLocalFile(File localFile) {
        File oldValue = this.localFile;
        this.localFile = localFile;
        pcs.firePropertyChange("localFile", oldValue, localFile);
    }
    
    /**
     * 
     * @return 
     */
    public EMStatus getEMStatus() {
        return emStatus;
    }

    @Override
    public int hashCode() {
        String s = "" + getIdentifier() + getVersion();
        return s.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Expansion o) {
            return compareTo(o) == 0;
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("(");
        sb.append(getIdentifier() + ", " + getVersion() + ", " + getDownloadUrl());
        if (isLocal()) {
            sb.append(", local");
        }
        sb.append(", " + localFile);
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * Installs this expansion.
     */
    public void install() throws IOException {
        if (oolite == null) {
            throw new IllegalStateException(EXPANSION_OOLITE_MUST_BE_SET);
        }
        oolite.install(this);
    }
    
    /**
     * Enables this expansion.
     */
    public void enable() throws IOException {
        if (oolite == null) {
            throw new IllegalStateException(EXPANSION_OOLITE_MUST_BE_SET);
        }
        oolite.enable(this);
    }
    
    /**
     * Disables this expansion.
     */
    public void disable() throws IOException {
        if (oolite == null) {
            throw new IllegalStateException(EXPANSION_OOLITE_MUST_BE_SET);
        }
        oolite.disable(this);
    }
    
    /**
     * Removes this expansion.
     */
    public void remove() throws IOException {
        if (oolite == null) {
            throw new IllegalStateException(EXPANSION_OOLITE_MUST_BE_SET);
        }
        oolite.remove(this);
    }

    /**
     * Compares this object with the specified object for order. 
     * Returns a negative integer, zero, or a positive integer as this object 
     * is less than, equal to, or greater than the specified object.
     * <p>
     * The implementor must ensure 
     * signum(x.compareTo(y)) == -signum(y.compareTo(x)) for all x and y. 
     * (This implies that x.compareTo(y) must throw an exception if and only 
     * if y.compareTo(x) throws an exception.)
     * <p>
     * The implementor must also ensure that the relation is transitive: 
     * (x.compareTo(y) > 0 && y.compareTo(z) > 0) implies x.compareTo(z) > 0.
     * <p>
     * Finally, the implementor must ensure that 
     * x.compareTo(y)==0 implies that signum(x.compareTo(z)) == signum(y.compareTo(z)), 
     * for all z.
     * <p>
     * API Note:<br>
     * It is strongly recommended, but not strictly required that 
     * (x.compareTo(y)==0) == (x.equals(y)). Generally speaking, any class 
     * that implements the Comparable interface and violates this condition 
     * should clearly indicate this fact. The recommended language is 
     * "Note: this class has a natural ordering that is inconsistent with equals."
     * 
     * @param t the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Expansion t) {
        String s1 = getIdentifier() + getVersion();
        String s2 = t.getIdentifier() + t.getVersion();
        return s1.compareTo(s2);
    }
    
    /**
     * Returns the list of required expansions as ExpansionReferences.
     * 
     * @return the list
     */
    public List<ExpansionReference> getRequiredRefs() {
        log.warn("getRequiredRefs()");
        
        if (oolite == null) {
            throw new IllegalStateException("oolite must not be null");
        }
        List<ExpansionReference> result = new ArrayList<>();
        if (getRequiresOxps() != null) {
            for (Dependency dep: getRequiresOxps()) {
                log.warn("getting expansion reference for '{}'", dep);
                ExpansionReference er = oolite.getExpansionReference(dep);
                result.add(er);
            }
        }
        return result;
    }
    
    /**
     * Returns the list of conflicting expansions as ExpansionReferences.
     * 
     * @return the list
     */
    public List<ExpansionReference> getConflictRefs() {
        List<ExpansionReference> result = new ArrayList<>();
        if (conflictOxps != null) {
            for (Dependency dep: conflictOxps) {
                ExpansionReference er = oolite.getExpansionReference(dep);
                switch (er.getStatus()) {
                    case CONFLICT:
                        break;
                    case MISSING:
                        er.setStatus(ExpansionReference.Status.OK);
                        break;
                    case OK:
                        er.setStatus(ExpansionReference.Status.CONFLICT);
                        break;
                    case REQUIRED_MISSING:
                        er.setStatus(ExpansionReference.Status.OK);
                        break;
                    case SURPLUS:
                        break;
                }
                result.add(er);
            }
        }
        return result;
    }
    
    /**
     * Returns the list of optional expansions as ExpansionReferences.
     * 
     * @return the list
     */
    public List<ExpansionReference> getOptionalRefs() {
        List<ExpansionReference> result = new ArrayList<>();
        for (Dependency dep: getOptionalOxps()) {
            ExpansionReference er = oolite.getExpansionReference(dep);
            switch (er.getStatus()) {
                case CONFLICT:
                    break;
                case MISSING:
                    er.setStatus(ExpansionReference.Status.OK);
                    break;
                case OK:
                    er.setStatus(ExpansionReference.Status.OK);
                    break;
                case REQUIRED_MISSING:
                    er.setStatus(ExpansionReference.Status.OK);
                    break;
                case SURPLUS:
                    break;
            }
            result.add(er);
        }
        return result;
    }

    /**
     * Returns true if this expansion is nested inside another expansion directory.
     * 
     * @return true if it is nested, false otherwise
     */
    public boolean isNested() {
        Pattern p = Pattern.compile("\\.oxp.+\\.oxp", Pattern.CASE_INSENSITIVE);
        String file = String.valueOf(getLocalFile());
        Matcher m = p.matcher(file);
        return m.find();
    }
}
