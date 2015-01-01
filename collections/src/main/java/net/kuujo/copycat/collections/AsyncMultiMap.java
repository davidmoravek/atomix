/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.copycat.collections;

import net.kuujo.copycat.DiscreteResource;
import net.kuujo.copycat.ResourceException;
import net.kuujo.copycat.cluster.ClusterConfig;
import net.kuujo.copycat.cluster.coordinator.ClusterCoordinator;
import net.kuujo.copycat.cluster.coordinator.CoordinatorConfig;
import net.kuujo.copycat.internal.AbstractManagedResource;
import net.kuujo.copycat.internal.cluster.coordinator.DefaultClusterCoordinator;

import java.util.concurrent.ExecutionException;

/**
 * Asynchronous multi-map.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <K> The multimap key type.
 * @param <V> The multimap entry type.
 */
public interface AsyncMultiMap<K, V> extends AsyncMultiMapProxy<K, V>, DiscreteResource {

  /**
   * Creates a new asynchronous multimap.
   *
   * @param name The asynchronous multimap name.
   * @param uri The asynchronous multimap member URI.
   * @param cluster The cluster configuration.
   * @param <K> The multimap key type.
   * @param <V> The multimap value type.
   * @return The asynchronous multimap.
   */
  static <K, V> AsyncMultiMap<K, V> create(String name, String uri, ClusterConfig cluster) {
    return create(name, uri, cluster, new AsyncMultiMapConfig());
  }

  /**
   * Creates a new asynchronous multimap.
   *
   * @param name The asynchronous multimap name.
   * @param uri The asynchronous multimap member URI.
   * @param cluster The cluster configuration.
   * @param config The multimap configuration.
   * @param <K> The multimap key type.
   * @param <V> The multimap value type.
   * @return The asynchronous multimap.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static <K, V> AsyncMultiMap<K, V> create(String name, String uri, ClusterConfig cluster, AsyncMultiMapConfig config) {
    ClusterCoordinator coordinator = new DefaultClusterCoordinator(uri, new CoordinatorConfig().withClusterConfig(cluster).addResourceConfig(name, config.resolve(cluster)));
    try {
      coordinator.open().get();
      return (AsyncMultiMap<K, V>) ((AbstractManagedResource) coordinator.<AsyncMultiMap<K, V>>getResource(name).get()).withShutdownTask(coordinator::close);
    } catch (InterruptedException e) {
      throw new ResourceException(e);
    } catch (ExecutionException e) {
      throw new ResourceException(e.getCause());
    }
  }

}
