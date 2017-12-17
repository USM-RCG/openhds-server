package com.github.cimsbioko.server.controller.service;

import java.sql.SQLException;

import com.github.cimsbioko.server.domain.model.Privilege;
import org.hibernate.exception.ConstraintViolationException;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.annotations.Authorized;

/**
 * Interface that represents a generic service that can be used to create/read/delete entities in the system
 * This serves as a basic entity service that can be used by cruds
 * In the case of specialized functionality for an entity, a new service class can be created
 * That service class can inherit from this one and be injected in through the Spring application context
 * or can be a completely different service
 *
 * @author Dave
 */
public interface EntityService {

    /**
     * Creates/persists an entity to the datastore
     *
     * @param entityItem the item to persist
     * @throws IllegalArgumentException
     * @throws ConstraintViolationException if the entity violates a constraint
     * @throws SQLException                 if the entity fails to be persisted
     */
    @Authorized({Privilege.CREATE_ENTITY})
    <T> void create(T entityItem) throws IllegalArgumentException, ConstraintViolations, SQLException;

    @Authorized({Privilege.EDIT_ENTITY})
    <T> void save(T entityItem) throws ConstraintViolations, SQLException;

    /**
     * Read entity
     *
     * @param id
     * @return
     */
    @Authorized({Privilege.VIEW_ENTITY})
    <T> T read(Class<T> entityType, String id);

    /**
     * Delete entity from persistence
     *
     * @param entityItem
     * @throws SQLException
     */
    @Authorized({Privilege.DELETE_ENTITY})
    <T> void delete(T entityItem) throws SQLException;

}
