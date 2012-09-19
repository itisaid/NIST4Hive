/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.ql.parse.sql;


/**
 * 
 * The abstract base class of all the FilterBlock classes.
 * 
 */
public abstract class FilterBlockBase {
  /**
   * 
   * Type definition of filter blocks.
   * 
   */
  enum Type {
    NULL_OP,
    LOGIC_AND,
    LOGIC_OR,
    LOGIC_NOT,
    NORMAL,
    SUBQ,
    UNKNOWN
  };

  /**
   * Type of this filter block
   */
  Type type;

  /**
   * left child in tree
   */
  FilterBlockBase leftChild = null;
  /**
   * right child in tree
   */
  FilterBlockBase rightChild = null;
  /**
   * parent node in tree
   */
  FilterBlockBase parent = null;

  /**
   * Constructor
   * 
   * @param type
   *          type of this filter block
   */
  public FilterBlockBase(Type type) {
    this.type = type;
  }

  /**
   * Whether this filter block has parent
   * 
   * @return
   */
  public boolean hasParent() {
    return (parent != null);
  }

  /**
   * Whether this filter block has right child
   * 
   * @return
   */
  public boolean hasRightChild() {
    return (rightChild != null);
  }

  /**
   * Whether this filter block has left child
   * 
   * @return
   */
  public boolean hasLeftChild() {
    return (leftChild != null);
  }

  /**
   * Whether this filter block has at least one child
   * 
   * @return
   */
  public boolean hasChild() {
    return (leftChild != null || rightChild != null);
  }

  /**
   * Whether this filter block has one and only child
   * 
   * @return
   */
  public boolean hasOnlyChild() {
    return (leftChild != null && rightChild == null);
  }

  /**
   * Get type of this filter block.
   * 
   * @return
   */
  public Type getType() {
    return type;
  }

  /**
   * Check if input child is the left child of this FB
   * 
   * @param child
   *          the input child to be checked
   * @return
   */
  public boolean isLeftChild(FilterBlockBase child) {
    return (child == leftChild) ? true : false;
  }

  /**
   * Check if input child is the right child of this FB
   * 
   * @param child
   *          the input child to be checked
   * @return
   */
  public boolean isRightChild(FilterBlockBase child) {
    return (child == rightChild) ? true : false;
  }

  /**
   * Replace the child with a new child and retain the subtree
   * 
   * @param oldChild
   *          the original child to be replaced
   * @param newChild
   *          new child to fill in the place
   */
  public boolean replaceChild(FilterBlockBase oldChild, FilterBlockBase newChild) {
    if (isLeftChild(oldChild)) {
      leftChild = newChild;
      if (newChild != null) {
        newChild.parent = this;
        if (oldChild != null) {
          newChild.leftChild = oldChild.leftChild;
          newChild.rightChild = oldChild.rightChild;
        }
      }
    } else if (isRightChild(oldChild)) {
      rightChild = newChild;
      if (newChild != null) {
        newChild.parent = this;
        if (oldChild != null) {
          newChild.leftChild = oldChild.leftChild;
          newChild.rightChild = oldChild.rightChild;
        }
      }
    } else {
      return false;
    }
    return true;
  }

  /**
   * Replace the entire subtree with new subtree with root newChild
   * 
   * @param oldChild
   *          the root of subtree to be replaced
   * @param newChild
   *          the root of new subtree to fill in the place
   * @return
   */
  public boolean replaceChildTree(FilterBlockBase oldChild, FilterBlockBase newChild) {
    if (isLeftChild(oldChild)) {
      leftChild = newChild;
      if (newChild != null) {
        newChild.parent = this;
      }
    } else if (isRightChild(oldChild)) {
      rightChild = newChild;
      if (newChild != null) {
        newChild.parent = this;
      }
    } else {
      return false;
    }
    return true;
  }

  /**
   * Sanity check for this node.
   * 
   * @return
   */
  public abstract boolean validate();


  /**
   * get parent
   * 
   * @return
   */
  protected FilterBlockBase getParent() {
    return parent;
  }

  /**
   * Set left child
   * 
   * @param left
   */
  public void setLeftChild(FilterBlockBase left) {
    leftChild = left;
    if (left != null) {
      left.parent = this;
    }
  }

  /**
   * get left child
   * 
   * @return
   */
  public FilterBlockBase getLeftChild() {
    return leftChild;
  }

  /**
   * set right child
   * 
   * @param right
   */
  protected void setRightChild(FilterBlockBase right) {
    rightChild = right;
    if (right != null) {
      right.parent = this;
    }
  }

  /**
   * get right child
   * 
   * @return
   */
  protected FilterBlockBase getRightChild() {
    return rightChild;
  }

  /**
   * set one and only child
   * remove any existing right child
   * 
   * @param child
   */
  protected void setOnlyChild(FilterBlockBase child) {
    leftChild = child;
    if (child != null) {
      child.parent = this;
    }
    rightChild = null;
  }

  /**
   * get one and only child
   * if this node has more than one child
   * return nothing
   * 
   * @return
   */
  protected FilterBlockBase getOnlyChild() {
    if (!hasRightChild()) {
      return leftChild;
    }
    return null;
  }

  @Override
  public String toString() {
    return type.toString();
  }

  /**
   * Print the entire filter block tree starting from this FB
   * 
   * @return The block tree string
   */
  public String toStringTree() {
    if (leftChild == null) {
      return this.toString();
    }
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(this.toString());
    sb.append(" ");
    // dump left Child
    sb.append(leftChild.toStringTree());
    if (rightChild != null) {
      sb.append(" ");
      sb.append(rightChild.toStringTree());
    }
    sb.append(")");
    return sb.toString();
  }
}
