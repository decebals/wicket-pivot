/*
 * Copyright 2012 Decebal Suiu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.wicket.pivot.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author Decebal Suiu
 */
public class TreeHelper {

	public static List<Node> getLeafs(Node root) {
		List<Node> leafs = new ArrayList<>();
		Iterator<Node> it = new TreeIterator(root);
		while (it.hasNext()) {
			Node node = it.next();
			if (node.isLeaf()) {
				leafs.add(node);
			}
		}
		
		return leafs;
	}
	
	public static List<List<Object>> getLeafValues(Node root) {
		List<List<Object>> leafValues = new ArrayList<>();
		List<Node> leafs = getLeafs(root);
		for (Node leaf : leafs) {
			leafValues.add(leaf.getPathValues());
		}

		return leafValues;
	}
	
	public static Node getNode(Node root, List<Object> pathValues) {
		Node node = root;
		for (Object value : pathValues) {
			node = getChild(node, value);			
		}
		
		return node;
	}
	
	public static Node getChild(Node node, Object value) {
		for (Node child : node.getChildren()) {
			Object data = child.getData();
			if (Objects.equals(value, data)) {
				return child;
			}
		}
		
		return null;
	}

	
	public static void printTree(Node root) {
		Iterator<Node> it = new TreeIterator(root);
		while (it.hasNext()) {
			Node node = it.next();
			int level = node.getLevel();
			if (level == 0) {
				System.out.println(String.format(node.toString() + " [%s]", getLeafs(node).size()));
			} else {
				System.out.println(String.format("%" + 3 * level + "s " + node.toString() + " [%s]", " ", getLeafs(node).size()));
			}
		}
	}
	
	public static void printLeafValues(Node root) {
		List<List<Object>> leafValues = getLeafValues(root);
		System.out.println("count > " + leafValues.size());
		for (List<Object> values : leafValues) {
			System.out.println(values);
		}		
	}

}
