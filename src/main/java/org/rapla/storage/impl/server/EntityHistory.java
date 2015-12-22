package org.rapla.storage.impl.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rapla.entities.Entity;
import org.rapla.entities.configuration.internal.RaplaMapImpl;
import org.rapla.framework.RaplaException;
import org.rapla.jsonrpc.common.internal.JSONParserWrapper;
import org.rapla.storage.impl.server.EntityHistory.HistoryEntry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EntityHistory
{
    public Collection<String> getAllIds()
    {
        return map.keySet();
    }

    public static class HistoryEntry
    {
        private long timestamp;
        private String id;
        private String json;
        private Class<? extends Entity> type;
        boolean isDelete;

        public HistoryEntry()
        {
        }

        public HistoryEntry(long timestamp, String json, Class<? extends Entity> type, boolean isDelete)
        {
            super();
            this.isDelete = isDelete;
            this.timestamp = timestamp;
            this.json = json;
            this.type = type;
        }

        public Class<? extends Entity> getType()
        {
            return type;
        }

        public String getId()
        {
            return id;
        }

        public long getTimestamp()
        {
            return timestamp;
        }

        @Override
        public String toString()
        {
            return "HistoryEntry [timestamp=" + timestamp + ", id=" + id + "]";
        }
    }

    private final Map<String, List<EntityHistory.HistoryEntry>> map = new LinkedHashMap<String, List<EntityHistory.HistoryEntry>>();
    private final Gson gson;

    public EntityHistory()
    {
        Class[] additionalClasses = new Class[] { RaplaMapImpl.class };
        final GsonBuilder gsonBuilder = JSONParserWrapper.defaultGsonBuilder(additionalClasses);
        gson = gsonBuilder.create();
    }

    /** returns the history entry with a timestamp<= since or null if no such entry exists*/
    public Entity get(String id, Date since)
    {
        final List<EntityHistory.HistoryEntry> historyEntries = map.get(id);
        if (historyEntries == null)
        {
            // FIXME handle history ends
            throw new RaplaException("History not available for id " + id);
        }
        final EntityHistory.HistoryEntry emptyEntryWithTimestamp = new EntityHistory.HistoryEntry();
        emptyEntryWithTimestamp.timestamp = since.getTime();
        int index = Collections.binarySearch(historyEntries, emptyEntryWithTimestamp, new Comparator<EntityHistory.HistoryEntry>()
        {
            @Override
            public int compare(EntityHistory.HistoryEntry o1, EntityHistory.HistoryEntry o2)
            {
                return (int) (o1.timestamp - o2.timestamp);
            }
        });
        /*
        * possible results:
        * we get an index >= 0 -> We found an entry, which has the timestamp of the last update from the client. We need to get this one
        * we get an index < 0 -> We have no entry within the list, which has the timestamp. Corresponding to the binary search API -index -1 is the index where to insert a entry having this timestamp. So we need -index -1 to get the last one with an timestamp smaller than the requested one.
        */
        if (index < 0)
        {
            index = -index - 2;
        }
        if (index < 0)
        {
            return null;
        }
        EntityHistory.HistoryEntry entry = historyEntries.get(index);
        return getEntity(entry);
    }

    public Entity getEntity(HistoryEntry entry)
    {
        String json = entry.json;
        Class<? extends Entity> type = entry.type;
        final Entity entity = gson.fromJson(json, type);
        return entity;
    }

    public EntityHistory.HistoryEntry addHistoryEntry(String id, String json, Class<? extends Entity> entityClass, Date timestamp, boolean isDelete)
    {
        List<EntityHistory.HistoryEntry> historyEntries = map.get(id);
        if (historyEntries == null)
        {
            historyEntries = new ArrayList<EntityHistory.HistoryEntry>();
            map.put(id, historyEntries);
        }
        final EntityHistory.HistoryEntry newEntry = new EntityHistory.HistoryEntry();
        newEntry.timestamp = timestamp.getTime();
        newEntry.json = json;
        newEntry.type = entityClass;
        newEntry.id = id;
        int index = historyEntries.size();
        newEntry.isDelete = isDelete;
        insert(historyEntries, newEntry, index);
        return newEntry;
    }

    private void insert(List<EntityHistory.HistoryEntry> historyEntries, EntityHistory.HistoryEntry newEntry, int index)
    {
        if (index == 0)
        {
            historyEntries.add(0, newEntry);
        }
        else
        {
            final long timestamp = historyEntries.get(index - 1).timestamp;
            if (timestamp > newEntry.timestamp)
            {
                insert(historyEntries, newEntry, index - 1);
            }
            else if (timestamp == newEntry.timestamp)
            {
                // Do nothing as already inserted... maybe check it
            }
            else
            {
                historyEntries.add(index, newEntry);
            }
        }
    }

    public EntityHistory.HistoryEntry addHistoryEntry(Entity entity, Date timestamp, boolean isDelete)
    {
        final String id = entity.getId();
        final String json = gson.toJson(entity);
        final Class<? extends Entity> entityClass = entity.getClass();
        return addHistoryEntry(id, json, entityClass, timestamp,isDelete);
    }

    public void clear()
    {
        map.clear();
    }

    List<HistoryEntry> getHistoryList(String key)
    {
        return map.get(key);
    }

    public void removeUnneeded(Date date)
    {
        final Set<String> keySet = map.keySet();
        final long time = date.getTime();
        for (String key : keySet)
        {
            final List<HistoryEntry> list = map.get(key);
            while (list.size() >= 2 && list.get(1).timestamp < time)
            {
                list.remove(0);
            }
        }
    }
}