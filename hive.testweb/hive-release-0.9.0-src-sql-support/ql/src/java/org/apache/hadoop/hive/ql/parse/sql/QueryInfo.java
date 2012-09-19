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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.hive.ql.parse.ASTNode;

import br.com.porcelli.parser.plsql.PLSQLParser;


/**
 * A facility class that keeps information needed for processing.
 * 
 * QueryInfo is something similar to Hive's QB structure. For each
 * subquery/query encountered, a new QueryInfo object is created
 * and information is collected within this subquery.
 * Each SqlASTNode has a reference to a QueryInfo object, and all
 * SqlASTNode within the same subquery refers to the same QueryInfo
 * object.
 * 
 * The QueryInfo objects of the entire query is organized as a
 * simple tree. Each QueryInfo object has a reference to its
 * parent QueryInfo object. The top QueryInfo is a dummy node.
 * The next layer are the top-level queries. Next next... layers
 * are nested children subqueries.
 * 
 * Note that here subquery only refers to subqueries inside
 * top-level query's from clause. Subqueries in other places
 * (e.g. filters) are handled by other data structure
 * (i.e. SubQFilterBlock).
 * 
 */
public class QueryInfo {
  /**
   * reference to the parent QueryInfo object
   */
  private QueryInfo parentQInfo = null;
  /**
   * Each select and its sub nodes forms a subquery.
   * this is the root node of this subquery.
   * The node is of type SQL92_RESERVED_SELECT
   */
  private SqlASTNode selectKey = null;
  /**
   * The insert destinations for all children selections
   * Because of the structure of SQL AST tree, insert destinations
   * are located in the parent qInfo of the select queries.
   */
  private Set<SqlASTNode> insertDestinationsForSubQ;
  /**
   * The from clause in this select query
   */
  private SqlASTNode from;
  /**
   * Map from SUBQUERY node to alias node.
   * alias is either randomly generated or extracted from
   * the original SQL AST.
   */
  private HashMap<SqlASTNode, ASTNode> subqToAlias;
  /**
   * The root of filter block tree for this query.
   */
  private FilterBlockBase rootFilterBlock;
  /**
   * Map from select key to src table or aliases referred in that select query
   */
  private HashMap<SqlASTNode, Set<String>> selectKeyToSrcTblAlias;

  /**
   * 
   * Class to Represent A Column.
   * 
   */
  public static class Column {
    /** table name */
    String tblName;
    /** column name */
    String colName;

    /**
     * Constructor
     */
    public Column() {
    }

    /**
     * Constructor
     * 
     * @param tname
     *          tablename
     * @param cname
     *          column name
     */
    public Column(String tname, String cname) {
      tblName = tname;
      colName = cname;
    }

    public void setTblAlias(String tname) {
      tblName = tname;
    }

    public String getTblAlias() {
      return tblName;
    }

    public void setColAlias(String cname) {
      colName = cname;
    }

    public String getColAlias() {
      return colName;
    }

    @Override
    public String toString() {
      if (tblName == null || tblName.isEmpty()) {
        return colName;
      } else {
        return tblName + "." + colName;
      }
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof Column)) {
        return false;
      }
      return tblName.equals(((Column) other).tblName) &&
            colName.equals(((Column) other).colName);
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }
  }

  /**
   * Constructor
   */
  public QueryInfo() {

  }

  /**
   * Set filter block tree root
   * 
   * @param root
   */
  public void setFilterBlockTreeRoot(FilterBlockBase root) {
    this.rootFilterBlock = root;
  }

  /**
   * Get filter block tree root
   * 
   * @return
   */
  public FilterBlockBase getFilterBlockTreeRoot() {
    return rootFilterBlock;
  }

  /**
   * Map SubQuery Node to alias node.
   * 
   * @param subq
   *          SUBQUERY node
   * @param alias
   *          ASTNode Identifier
   */
  public void setSubQAlias(SqlASTNode subq, ASTNode alias) {
    if (subqToAlias == null) {
      subqToAlias = new HashMap<SqlASTNode, ASTNode>();
    }
    subqToAlias.put(subq, alias);
  }

  /**
   * Get mapped alias of SubQuery node
   * 
   * @param subq
   *          SUBQUERY node
   * @return alias ASTNode
   */
  public ASTNode getSubQAlias(SqlASTNode subq) {
    if (subqToAlias == null) {
      return null;
    }
    return subqToAlias.get(subq);
  }

  /**
   * Add one insert destination.
   * 
   * @param into
   *          SqlASTNode INTO node
   */
  public void addInsertDestinationForSubQ(SqlASTNode into) {
    if (insertDestinationsForSubQ == null) {
      insertDestinationsForSubQ = new HashSet<SqlASTNode>();
    }
    insertDestinationsForSubQ.add(into);
  }

  /**
   * Get all insert destinations
   * 
   * destinations may be more than one (if multi-table insert)
   * 
   * @return the into node set
   */
  public Set<SqlASTNode> getInsertDestinationsForSubQ() {
    return insertDestinationsForSubQ;
  }

  /**
   * Get the insert destinations for this query.
   * It is retrieved from parent QueryInfo object.
   * 
   * @return
   */
  public Set<SqlASTNode> getInsertDestinationsForThisQuery() {
    // get the destination from parent
    if (hasParentQueryInfo()) {
      return parentQInfo.getInsertDestinationsForSubQ();
    }
    return null;
  }

  /**
   * Setter method for from clause of this query.
   * 
   * @param from
   *          SqlASTNode SQL92_RESERVED_FROM
   */
  public void setFrom(SqlASTNode from) {
    this.from = from;
  }

  /**
   * Getter method for from clause of this query
   * 
   * @return SqlASTNode SQL92_RESERVED_FROM
   */
  public SqlASTNode getFromClauseForThisQuery() {
    return from;
  }

  /**
   * Setter for parent qInfo.
   * 
   * @param parent
   *          parent QInfo
   */
  public void setParentQueryInfo(QueryInfo parent) {
    parentQInfo = parent;
  }

  /**
   * Getter for parent qInfo
   * 
   * @return parent qInfo
   */
  public QueryInfo getParentQueryInfo() {
    return parentQInfo;
  }

  /**
   * Check whether this has parent QInfo
   * 
   * @return
   */
  public boolean hasParentQueryInfo() {
    return (parentQInfo != null);
  }

  /**
   * Getter for whether this is the root of QInfo Tree
   * If this qInfo doesn't have any parent
   * then it's qInfo tree Root
   * 
   * @return
   */
  public boolean isQInfoTreeRoot() {
    return !hasParentQueryInfo();
  }

  /**
   * Set select key of this query
   * 
   * @param selectStat
   *          SqlASTNode SQL92_RESERVED_SELECT
   */
  public void setSelectKeyForThisQ(SqlASTNode selectStat) {
    this.selectKey = selectStat;
  }

  /**
   * Get select key of this query
   * 
   * @return
   */
  public SqlASTNode getSelectKeyForThisQ() {
    return selectKey;
  }

  /**
   * Get all the source tables and aliases referred in from clause
   * in this select query.
   * 
   * @param select
   *          SqlASTNode select key of this query
   * @return the set of table names and aliases
   */
  public Set<String> getSrcTblAliasForSelectKey(SqlASTNode select) {
    if (selectKeyToSrcTblAlias == null) {
      selectKeyToSrcTblAlias = new HashMap<SqlASTNode, Set<String>>();
    }
    Set<String> srcTblAlias = selectKeyToSrcTblAlias.get(select);
    if (srcTblAlias == null) {
      srcTblAlias = new HashSet<String>();
      SqlXlateUtil.getSrcTblAlias((SqlASTNode) select
          .getFirstChildWithType(PLSQLParser.SQL92_RESERVED_FROM), srcTblAlias);
      selectKeyToSrcTblAlias.put(select, srcTblAlias);
    }
    return srcTblAlias;
  }
}
