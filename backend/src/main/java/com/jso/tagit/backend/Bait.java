package com.jso.tagit.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/** The object model for the data we are sending through endpoints */
@Entity
public class Bait {

    @Id private String id;
    @Index private long last_modified;
    private int selectionCount;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLast_modified()
    {
        return last_modified;
    }

    public void setLast_modified(long last_modified) {
        this.last_modified = last_modified;
    }

    public int getSelectionCount()
    {
        return selectionCount;
    }

    public void setSelectionCount(int selectionCount) { this.selectionCount = selectionCount; }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}