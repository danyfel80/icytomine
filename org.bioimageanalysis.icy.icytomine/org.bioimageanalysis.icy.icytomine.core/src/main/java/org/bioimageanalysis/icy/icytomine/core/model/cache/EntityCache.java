package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Entity;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import icy.plugin.PluginLoader;

public abstract class EntityCache<K, V extends Entity> {
	private static CacheManager cacheManager;
	protected static final long ENTITY_HEAP_SIZE = 500;

	static {
		cacheManager = CacheManagerBuilder.newCacheManagerBuilder().withClassLoader(PluginLoader.getLoader()).build(true);
	}

	public static CacheManager getCacheManager() {
		return cacheManager;
	}

	private CytomineClient client;
	private Cache<K, V> cache;

	public EntityCache(CytomineClient client) {
		this.client = client;
	}

	public V retrieve(K entityId) {
		V entity = getCache().get(entityId);
		if (entity == null) {
			throw new EntityCacheException(
					String.format("Entity was not found in %s cache: %s", getValueClass().getSimpleName(), entityId.toString()));
		}
		return entity;
	}

	public void store(K entityId, V entity) {
		getCache().put(entityId, entity);
	}

	public Cache<K, V> getCache() {
		if (cache == null) {
			cache = retrieveOrCreateCache();
		}
		return cache;
	}

	private Cache<K, V> retrieveOrCreateCache() {
			Cache<K,V> cache = cacheManager.getCache(getCacheAlias(), getKeyClass(), getValueClass());
			if (cache == null) {
			return cacheManager.createCache(getCacheAlias(),
					CacheConfigurationBuilder
							.newCacheConfigurationBuilder(getKeyClass(), getValueClass(), ResourcePoolsBuilder.heap(getHeapSize()))
							.build());
			}
			return cache;
	}

	public String getCacheAlias() {
		return String.format("%s%d", getClass().getSimpleName(), getClient().hashCode());
	}

	protected CytomineClient getClient() {
		return client;
	}

	protected abstract Class<K> getKeyClass();

	protected abstract Class<V> getValueClass();

	public long getHeapSize() {
		return ENTITY_HEAP_SIZE;
	}

}
