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

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hive.ql.parse.ASTNode;

import br.com.porcelli.parser.plsql.PLSQLParser;

/**
 * Class Represents a filter that contains subquery.
 * e.g.
 * exists (select ...from...where...)
 * col1 in (select col2 from..where...)
 * col1 > (select avg(col2) from...where...)
 * col1 > all (select col2 from...where)
 */
public class SubQFilterBlock extends FilterBlockBase {
  /**
   * Operation Types of subquery filter
   */
  enum OPType {
    IN, // in
    NOTIN, // not in
    RELATIONAL, // > >= < <= = <> !=
    ALL, // all
    SOMEANY, // some/any. some is equivalent to any
    EXISTS, // exists
    NOTEXISTS, // not exists
    ISNULL, // is null
    ISNOTNULL, // is not null
    COLUMNSUBQ, // column subquery
    UNKNOWN
  };

  /**
   * The operation type of this subquery filter
   */
  OPType opType;

  /**
   * 
   * Aggregation Phase. Indicate the aggregation phase of this SubQ.
   * Facilitate the processing of aggregation functions
   * e.g. select count(*) case, we have to separate it
   * into 2 phases, 1 is select * and merge it with subtrees
   * and then we add another select count(*) on top of select *.
   * 
   */
  enum AggrPhase {
    AGGREGATION_PHASE,
    SELECTALL_PHASE,
    NOTAPPLICABLE
  };

  /**
   * set default value of aggPhase as NOTAPPLICABLE
   */
  AggrPhase aggPhase = AggrPhase.NOTAPPLICABLE;
  /**
   * The containing select query's qInfo
   */
  QueryInfo qInfo;

  /**
   * Whether this is the top subquery block
   * 
   * top subq block is actually the containing select query.
   * top subq block is not the root of a filter block tree.
   */
  boolean isTopSubQBlock = false;
  /**
   * the top op node of this filter expression
   * e.g. col1 > (select col2 from t) > is the top op
   */
  SqlASTNode op;

  /**
   * whether subquery component is on left side of op
   */
  boolean isLeftExprSubQ;
  /* ================ members for the subquery component======== */
  /**
   * The subquery component
   * SqlASTNode SUBQUERY
   */
  SqlASTNode subquery;
  /**
   * Alias of the subquery component
   * normally is generated
   */
  ASTNode subqueryAlias;
  /**
   * The select item list of the subquery component
   * SqlASTNode SELECT_LIST
   */
  List<SqlASTNode> selectList;
  /**
   * The list of subquery as select items in select list
   */
  private List<FilterBlockBase> colFBs;
  /**
   * from clause of the subquery component
   * SqlASTNode SQL92_RESERVED_FROM
   */
  SqlASTNode from;
  /**
   * select key of the subquery component
   * SqlASTNode SQL92_RESERVED_SELECT
   */
  SqlASTNode selectKey;
  /**
   * normal filters within the subquery component where
   */
  NormalFilterBlock normalFilter; // child normal filters
  /**
   * the non-subquery component on the other side of top op
   * SqlASTNode CASCATED_ELEMENT
   */
  SqlASTNode nonSubQExpr; // CASCATED_ELEMENT

  /**
   * Constructor.
   */
  private SubQFilterBlock() {
    super(FilterBlockBase.Type.SUBQ);
  }

  /**
   * Constructor.
   * 
   * @param opType
   */
  public SubQFilterBlock(OPType opType) {
    this();
    this.opType = opType;
  }

  /**
   * Copy Constructor
   * 
   * @param sfb
   */
  public SubQFilterBlock(SubQFilterBlock sfb) {
    // only copy the children,
    // do not copy the parent child links in base
    this();
    this.opType = sfb.opType;
    this.qInfo = sfb.qInfo;
    this.isTopSubQBlock = sfb.isTopSubQBlock;
    this.op = sfb.op;
    this.isLeftExprSubQ = sfb.isLeftExprSubQ;
    this.subquery = sfb.subquery;
    this.subqueryAlias = sfb.subqueryAlias;
    this.selectList = sfb.selectList;
    this.colFBs = sfb.colFBs;
    this.from = sfb.from;
    this.selectKey = sfb.selectKey;
    this.normalFilter = sfb.normalFilter;
    this.nonSubQExpr = sfb.nonSubQExpr;


  }

  @Override
  public boolean validate() {
    // check single select item
    /*
     * boolean validSelectItem = false;
     * if (isTopSubQBlock()){
     * validSelectItem = true;
     * } else {
     * if (selectList.getType() == PLSQLParser.SELECT_LIST){
     * validSelectItem = selectList.getChildCount()==1;
     * } else if (selectList.getType() == PLSQLParser.ASTERISK &&
     * (opType == OPType.EXISTS || opType == OPType.NOTEXISTS)) {
     * validSelectItem = true;
     * }
     * }
     * return (hasOnlyChild()||!hasChild()) && validSelectItem;
     */
    return (hasOnlyChild() || !hasChild());
  }

  public void addColSubQFB(FilterBlockBase fb) {
    if (colFBs == null) {
      colFBs = new ArrayList<FilterBlockBase>();
    }
    colFBs.add(fb);
  }


  public List<FilterBlockBase> getAllColSubQFB() {
    return colFBs;
  }

  /**
   * set aggr phase
   * 
   * @param aggrPhase
   */
  public void setAggrPhase(AggrPhase aggrPhase) {
    this.aggPhase = aggrPhase;
  }

  /**
   * Get aggr phase
   * 
   * @return
   */
  public AggrPhase getAggrPhase() {
    return aggPhase;
  }

  /**
   * set subquery component's alias
   * 
   * @param alias
   */
  public void setSubQueryAlias(ASTNode alias) {
    assert (qInfo != null);
    getQueryInfo().setSubQAlias(subquery, alias);
    subqueryAlias = alias;
  }

  /**
   * Get subquery component's alias
   * 
   * @return
   */
  public ASTNode getSubQueryAlias() {
    assert (qInfo != null);
    if (subqueryAlias == null) {
      subqueryAlias = getQueryInfo().getSubQAlias(subquery);
    }
    return subqueryAlias;
  }

  /**
   * set query info
   * 
   * @param qInfo
   */
  public void setQueryInfo(QueryInfo qInfo) {
    this.qInfo = qInfo;
  }

  /**
   * get query info
   * 
   * @return
   */
  public QueryInfo getQueryInfo() {
    return qInfo;
  }

  /**
   * set top op
   * 
   * @param op
   */
  public void setOp(SqlASTNode op) {
    this.op = op;
  }

  /**
   * get top op
   * 
   * @return
   */
  public SqlASTNode getOp() {
    return op;
  }

  /**
   * set whether left hand component is subquery
   * 
   * @param isLeftSubQ
   */
  public void setIsLeftExprSubQ(boolean isLeftSubQ) {
    isLeftExprSubQ = isLeftSubQ;
  }

  /**
   * get whether left hand component is subquery
   * 
   * @return
   */
  public boolean isLeftExprSubQ() {
    return isLeftExprSubQ;
  }

  /**
   * mark this subq filter block the top subq block
   */
  public void setIsTopSubQBlock() {
    isTopSubQBlock = true;
  }

  /**
   * check whether this is the top subq block
   * 
   * @return
   */
  public boolean isTopSubQBlock() {
    return isTopSubQBlock;
  }

  /**
   * get parent subq block
   * if parent is not subq block, return null
   * 
   * @return
   */
  public SubQFilterBlock getParentSubQ() {
    FilterBlockBase p = getParent();
    if (p.getType() == Type.SUBQ) {
      return (SubQFilterBlock) p;
    } else {
      return null;
    }
  }

  /**
   * get child subq block
   * if one and only child is not subq block, return null
   * 
   * @return
   */
  public SubQFilterBlock getChildSubQ() {
    if (hasChildSubQ()) {
      return (SubQFilterBlock) getOnlyChild();
    } else {
      return null;
    }
  }

  /**
   * whether this has child subq block
   * 
   * @return
   */
  public boolean hasChildSubQ() {
    if (hasOnlyChild() &&
        getOnlyChild().getType() == Type.SUBQ) {
      return true;
    }
    return false;
  }

  /**
   * set subq filter operation type
   * 
   * @param type
   */
  public void setOpType(OPType type) {
    opType = type;
  }

  /**
   * get subq filter operation type
   * 
   * @return
   */
  public OPType getOpType() {
    return opType;
  }

  /**
   * set the non subq component
   * 
   * @param expr
   */
  public void setNonSubQExpr(SqlASTNode expr) {
    nonSubQExpr = expr;
  }

  /**
   * get the non subq component
   * 
   * @return
   */
  public SqlASTNode getNonSubQExpr() {
    return nonSubQExpr;
  }

  /**
   * set the subquery node of subq component
   * 
   * @param subquery
   */
  public void setSubQueryNodeInSubQ(SqlASTNode subquery) {
    assert (subquery.getType() == PLSQLParser.SUBQUERY);
    this.subquery = subquery;
  }

  /**
   * get the subquery node of subq component
   * 
   * @return
   */
  public SqlASTNode getSubQueryNodeInSubQ() {
    return subquery;
  }

  /**
   * set select key of subq component
   * 
   * @param selectKey
   */
  public void setSelectKeyInSubQ(SqlASTNode selectKey) {
    assert (selectKey.getType() == PLSQLParser.SQL92_RESERVED_SELECT);
    this.selectKey = selectKey;
  }

  /**
   * get select key of subq component
   * 
   * @return
   */
  public SqlASTNode getSelectKeyInSubQ() {
    return selectKey;
  }

  /**
   * set from clause of subq component
   * 
   * @param from
   */
  public void setFromInSubQ(SqlASTNode from) {
    assert (from.getType() == PLSQLParser.SQL92_RESERVED_FROM);
    this.from = from;
  }

  /**
   * get from clause of subq component
   * 
   * @return
   */
  public SqlASTNode getFromInSubQ() {
    return from;
  }

  public void addSelectItemInSubQ(SqlASTNode selectItemOrAsterisk) {
    if (selectList == null) {
      selectList = new ArrayList<SqlASTNode>();
    }
    selectList.add(selectItemOrAsterisk);
  }

  public void addSelectListInSubQ(SqlASTNode selectList) throws SqlXlateException {
    if (selectList.getType() == PLSQLParser.SELECT_LIST) {
      for (int i = 0; i < selectList.getChildCount(); i++) {
        if (selectList.getChild(i).getType() == PLSQLParser.SELECT_ITEM) {
          addSelectItemInSubQ((SqlASTNode) selectList.getChild(i));
        }
      }
    } else if (selectList.getType() == PLSQLParser.ASTERISK) {
      addSelectItemInSubQ(selectList);
    } else {
      throw new SqlXlateException("Unrecognized Select Item :" + selectList.toStringTree());
    }
  }

  /**
   * get select list of subq component
   * 
   * @return
   */
  public List<SqlASTNode> getSelectListInSubQ() {
    return selectList;
  }

  /**
   * merge another normal filter with self's normal filter
   * 
   * @param type
   * @param newFilter
   * @param originalFilterAsLeftChild
   * @throws SqlXlateException
   */
  public void mergeNormalFilter(FilterBlockBase.Type type, NormalFilterBlock newFilter,
      boolean originalFilterAsLeftChild) throws SqlXlateException {
    if (normalFilter == null) {
      normalFilter = newFilter;
    } else {
      // merge uncorrelated and correlated filter
      normalFilter.mergeFilter(type, newFilter.getCorrelatedFilter(), originalFilterAsLeftChild);
      normalFilter.mergeFilter(type, newFilter.getUnCorrelatedFilter(), originalFilterAsLeftChild);
    }
  }

  /**
   * merge another filter node into the correlated component of self's normal filter
   * 
   * @param type
   * @param newFilterExpr
   * @param originalFilterAsLeftChild
   * @throws SqlXlateException
   */
  public void mergeCorrelatedFilter(FilterBlockBase.Type type, SqlASTNode newFilterExpr,
      boolean originalFilterAsLeftChild) throws SqlXlateException {
    if (normalFilter == null) {
      normalFilter = new NormalFilterBlock();
    }
    normalFilter.mergeFilter(type, new NormalFilterBlock.CorrelatedFilter(newFilterExpr),
        originalFilterAsLeftChild);
  }

  /**
   * merge another fitler node into the uncorrelated component of self's normal filter
   * 
   * @param type
   * @param newFilterExpr
   * @param originalFilterAsLeftChild
   * @throws SqlXlateException
   */
  public void mergeUnCorrelatedFilter(FilterBlockBase.Type type, SqlASTNode newFilterExpr,
      boolean originalFilterAsLeftChild) throws SqlXlateException {
    if (normalFilter == null) {
      normalFilter = new NormalFilterBlock();
    }
    normalFilter.mergeFilter(type, new NormalFilterBlock.UnCorrelatedFilter(newFilterExpr),
        originalFilterAsLeftChild);
  }

  /**
   * set normal filter
   * 
   * @param filter
   */
  public void setNormalFilter(NormalFilterBlock filter) {
    normalFilter = filter;
  }

  /**
   * get normal filter
   * 
   * @return
   */
  public NormalFilterBlock getNormalFilter() {
    return normalFilter;
  }

  /**
   * whether has normal filter
   * 
   * @return
   */
  public boolean hasNormalFilter() {
    return (normalFilter != null);
  }

  @Override
  public String toString() {
    if (hasNormalFilter()) {
      return super.toString() + "-" + getNormalFilter().toString();
    } else {
      return super.toString();
    }
  }
}
