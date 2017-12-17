package com.github.cimsbioko.server.domain.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Calendar;

/**
 * An AuditableEntity can be any entity stored in the database that needs to be audited
 */
@MappedSuperclass
public abstract class AuditableEntity implements UuidIdentifiable, Serializable {

    private static final long serialVersionUID = -4703049354466276068L;

    @Id
    @Column(length = 32)
    String uuid;

    protected boolean deleted = false;

    protected Calendar created;

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @XmlTransient
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    @Override
    public int hashCode() {
        if (null == uuid) {
            return 0;
        }
        return uuid.hashCode();
    }
}
