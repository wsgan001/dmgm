/*
 * This file is part of Directed Multigraph Miner (DMGM).
 *
 * DMGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DMGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DMGM. If not, see <http://www.gnu.org/licenses/>.
 */


package org.biiig.dmgm.impl.operators.fsm;

import javafx.util.Pair;
import org.biiig.dmgm.api.model.CachedGraph;

import java.util.Collection;

/**
 * Superclass of pattern growers.
 */
public abstract class GrowChildrenBase implements GrowChildren {

  /**
   * Parent DFS code of all children generated by this instance.
   */
  private final DfsCode parent;

  GrowChildrenBase(DfsCode parent) {
    this.parent = parent;
  }

  @Override
  public void addChildren(
      CachedGraph withGraph, DfsEmbedding parentEmbedding,
      Collection<Pair<DfsCode, WithEmbedding>> output) {

    boolean rightmost = true;
    for (int fromTime : parent.getRightmostPath()) {
      int fromId = parentEmbedding.getVertexId(fromTime);

      CachedGraph graph = withGraph.getGraph();
      for (int edgeId : getEdgeIds(graph, fromId)) {
        // if not contained in parent embedding
        if (! parentEmbedding.containsEdgeId(edgeId)) {

          // determine times of incident vertices in parent embedding
          int toId = getToId(graph, edgeId);
          int toTime = parentEmbedding.getVertexTime(toId);

          // CHECK FOR BACKWARDS GROWTH OPTIONS

          // grow backwards
          if (rightmost && toTime >= 0) {
            int toLabel = graph.getVertexLabel(toId);
            DfsCode childCode = parent
                .growBackwards(fromTime, toTime, graph.getEdgeLabel(edgeId), isOutgoing());

            DfsEmbedding childEmbedding = parentEmbedding.growBackwards(edgeId);

            output.add(new Pair<>(childCode, childEmbedding));

            // grow forwards
          } else if (toTime < 0) {
            int toLabel = graph.getVertexLabel(toId);
            toTime = parent.getVertexCount();
            DfsCode childCode = parent
                .growForwards(fromTime, toTime, graph.getEdgeLabel(edgeId), isOutgoing(), toLabel);

            DfsEmbedding childEmbedding = parentEmbedding.growForwards(edgeId, toId);

            output.add(new Pair<>(childCode, childEmbedding));

          }
        }
      }

      rightmost = false;
    }
  }

  /**
   * Get incoming or outgoing edge ids of a vertex.
   *
   * @param graph graph
   * @param fromId vertex id
   * @return edge ids
   */
  protected abstract int[] getEdgeIds(CachedGraph graph, int fromId);

  /**
   * Get source or target vertex id of en edge.
   *
   * @param graph graph
   * @param edgeId edge id
   * @return vertex id
   */
  protected abstract int getToId(CachedGraph graph, int edgeId);

  /**
   * Get the direction of the current edge traversal.
   *
   * @return true <=> traversed in direction
   */
  protected abstract boolean isOutgoing();
}