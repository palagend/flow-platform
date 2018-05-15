/*
 * Copyright 2018 fir.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flow.platform.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

/**
 * @author yang
 */
public class NodeTree {

    private final static int DEFAULT_SIZE = 20;

    /**
     * Create node tree from yml
     */
    public static NodeTree create(String yml) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create node tree from Node object
     * @param root
     * @return
     */
    public static NodeTree create(Node root) {
        return new NodeTree(root);
    }

    private final Map<NodePath, NodeWithIndex> cached = new HashMap<>(DEFAULT_SIZE);

    private final List<Node> ordered = new ArrayList<>(DEFAULT_SIZE);

    @Getter
    private final Node root;

    private NodeTree(Node root) {
        buildTree(root);
        ordered.remove(root);
        this.root = root;
    }

    /**
     * Parse to tree to yml string
     */
    public String toYml() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get next Node instance from path
     */
    public Node next(NodePath path) {
        NodeWithIndex nodeWithIndex = getWithIndex(path);

        // next is out of range
        if ((nodeWithIndex.index + 1) > (ordered.size() - 1)) {
            return null;
        }

        return ordered.get(nodeWithIndex.index + 1);
    }

    /**
     * Get parent Node instance from path
     */
    public Node parent(NodePath path) {
        return getWithIndex(path).node.getParent();
    }

    public Node get(NodePath path) {
        return getWithIndex(path).node;
    }

    private NodeWithIndex getWithIndex(NodePath path) {
        NodeWithIndex nodeWithIndex = cached.get(path);

        if (Objects.isNull(nodeWithIndex)) {
            throw new IllegalArgumentException("The node path doesn't existed");
        }

        return nodeWithIndex;
    }

    /**
     * Reset node path and parent reference and put to cache
     */
    private void buildTree(Node root) {
        for (Node child : root.getChildren()) {
            child.setPath(NodePath.create(root.getName(), child.getName()));
            child.setParent(root);
            buildTree(child);
        }

        ordered.add(root);
        cached.put(root.getPath(), new NodeWithIndex(root, ordered.size() - 1));
    }

    private class NodeWithIndex {

        Node node;

        int index;

        NodeWithIndex(Node node, int index) {
            this.node = node;
            this.index = index;
        }
    }
}