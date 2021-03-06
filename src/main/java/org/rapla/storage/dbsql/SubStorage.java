package org.rapla.storage.dbsql;

import org.rapla.entities.Entity;
import org.rapla.framework.RaplaException;

import java.sql.SQLException;
import java.util.Collection;

public interface SubStorage<T extends Entity<T>> extends  Storage<T>
{
    void updateWithForeignId(String foreignId) throws SQLException,RaplaException;
    void deleteIds(Collection<String> ids) throws SQLException,RaplaException;
}
