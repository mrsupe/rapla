/*--------------------------------------------------------------------------*
 | Copyright (C) 2006 Gereon Fassbender, Christopher Kohlhaas               |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.entities.storage.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rapla.entities.Entity;
import org.rapla.entities.storage.EntityReferencer;
import org.rapla.entities.storage.EntityResolver;

/** The ReferenceHandler takes care of serializing and deserializing references to Entity objects.
<p>
    The references will be serialized to the ids of the corresponding entity. Deserialization of
    the ids takes place in the contextualize method. You need to provide an EntityResolver on the Context.
</p>
<p>
The ReferenceHandler support both named and unnamed References. Use the latter one, if you don't need to refer to the particular reference by name and if you want to keep the order of the references.

<pre>

// put a named reference
referenceHandler.put("owner",user);

// put unnamed reference
Iterator it = resources.iterator();
while (it.hasNext())
   referenceHandler.add(it.next());

// returns
User referencedUser = referenceHandler.get("owner");

// returns both the owner and the resources
Itertor references = referenceHandler.getReferences();
</pre>

</p>
    @see EntityResolver
 */
public class ReferenceHandler /*extends HashMap<String,List<String>>*/ implements EntityReferencer {
	private Map<String,List<String>> idmap;
    transient EntityResolver resolver;
	
    public ReferenceHandler() {
    	this( new LinkedHashMap<String,List<String>>());
    }
    
    public EntityResolver getResolver()
    {
    	return resolver;
    }
    
    public ReferenceHandler(Map<String,List<String>> idmap) {
    	if ( idmap == null )
    	{
    		idmap = Collections.emptyMap();
    	}
    	this.idmap = idmap;
    }
    /**
     * @see org.rapla.entities.storage.EntityReferencer#setResolver(org.rapla.entities.storage.EntityResolver)
     */
    public void setResolver(EntityResolver resolver)  {
    	if (resolver == null){
    		throw new IllegalArgumentException("Null not allowed");
    	}
		this.resolver = resolver;
			
//    	try {
//	        for (String key :idmap.keySet()) {
//	        	List<String> ids = idmap.get( key); 
//	        	for (String id: ids)
//	            {
//	            	Entity entity = resolver.resolve(id);
//	            }
//	        }
//		} catch (EntityNotFoundException ex) {
//		    clearReferences();
//		    throw ex;
//		}
    }
    
    public Collection<String> getReferencedIds()
    {
    	Set<String> result = new HashSet<String>();
    	if (idmap != null) {
            for (List<String> entries:idmap.values()) {
                for ( String id: entries)
                {
					result.add(id);
                }
            }
        }
        return result;
    }
    
    /** Use this method if you want to implement deserialization of the object manualy.
     * You have to add the reference-ids to other entities immediatly after the constructor.
     * @throws IllegalStateException if contextualize has been called before.
     */
    public void putId(String key,String id) {
    	putIds( key, Collections.singleton(id));
    }
    
    public void addId(String key,String id) {
    	synchronized (this) 
        {
	        List<String> idEntries = idmap.get( key );
	        if ( idEntries == null )
	        {
	        	idEntries = new ArrayList<String>();
	        	idmap.put(key, idEntries);
	        }
			idEntries.add(id);
        }
    }

    public void add(String key, Entity entity) {
		synchronized (this) 
        {
			addId( key, entity.getId());
        }
	}    

    public void putIds(String key,Collection<String> ids) {
        synchronized (this) 
        {
	        if (ids == null || ids.size() == 0) {
	            idmap.remove(key);
	            return;
	        }
	
	        List<String> entries = new ArrayList<String>();
	        for (String id:ids)
	        {
	        	entries.add( id);
	        }
	        idmap.put(key, entries);
        }
    }
    
    public String getId(String key)
    {
    	List<String> entries  = idmap.get(key);
    	if ( entries == null || entries.size() == 0)
    	{
    		return null;
    	}
    	String entry  = entries.get(0);
    	if (entry == null)
    		return null;
    	return entry;
    }
    
	public Collection<String> getIds(String key) 
	{
		List<String> entries  = idmap.get(key);
		if ( entries == null )
		{
			return Collections.emptyList();
		}
		return entries;
	}	

    public void putEntity(String key,Entity entity) {
        synchronized (this) 
        {
	        if (entity == null) {
	            idmap.remove(key);
	            return;
	        }
	        idmap.put(key, Collections.singletonList(entity.getId()) );
        }
    }
    
    public void putList(String key, Collection<Entity>entities) {
        synchronized (this) 
        {
            if (entities == null || entities.size() == 0) 
	        {
	            idmap.remove(key);
	            return;
	        }
	        List<String> idEntries = new ArrayList<String>();
	        for (Entity ent: entities)
	        {
	        	String id = ent.getId();
				idEntries.add( id);
	        }
	        idmap.put(key, idEntries);
        }
    }
    

	public Collection<Entity> getList(String key) 
	{
		List<String> ids  = idmap.get(key);
		if ( ids == null )
		{
			return Collections.emptyList();
		}
		List<Entity> entries = new ArrayList<Entity>(ids.size());
		for ( String id:ids)
		{
			Entity entity = resolver.tryResolve( id );
			entries.add( entity );
		}
		return entries;
	}	

    public Entity getEntity(String key) {
    	 List<String>entries  = idmap.get(key);
         if ( entries == null || entries.size() == 0)
         {
         	return null;
         }
         String id  = entries.get(0);
         if (id == null)
             return null;
 		return resolver.tryResolve( id );
    }

	public boolean removeWithKey(String key) {
    	synchronized (this) 
        {
	        if  ( idmap.remove(key) != null ) {
	            return true;
	        } else {
	            return false;
	        }
	    }
    }

    public boolean remove(String id) {
        boolean removed = false;
    	synchronized (this) 
        {
        	for (String key: idmap.keySet())
        	{
				List<String> entries = idmap.get(key);
				if ( entries.contains( id))
				{
					entries.remove( id);
					removed = true;
				}
        	}
        }
        return removed;
    }

    public boolean isRefering(String id) {
        return getReferencedIds().contains(id);
    }

    public Iterable<String> getReferenceKeys() {
        return idmap.keySet();
    }
    
    public void clearReferences() {
    	idmap.clear();
    }

    @SuppressWarnings("unchecked")
	public ReferenceHandler clone(Map<String,List<String>> idmapClone) {
        ReferenceHandler clone;
		clone = new ReferenceHandler(idmapClone);
		clone.resolver = this.resolver;
		return clone;
    }


	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (String ref: getReferencedIds())
		{
			builder.append(ref);
			builder.append(",");
		}
	    return builder.toString();
	}


}
