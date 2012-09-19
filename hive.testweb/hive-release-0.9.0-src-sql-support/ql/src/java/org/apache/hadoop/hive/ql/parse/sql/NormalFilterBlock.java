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


import br.com.porcelli.parser.plsql.PLSQLParser;

/**
 * 
 * A normal filter which does not contain any subqueries
 * 
 */
public class NormalFilterBlock extends FilterBlockBase {

  /**
   * filters that does not refer to src tables in parent/ancestor queries.
   */
  private UnCorrelatedFilter ucFilter;
  /**
   * Filters that refers to src tables in parent/ancestor queries
   */
  private CorrelatedFilter cFilter;

  /**
   * Constructor.
   */
  public NormalFilterBlock() {
    super(FilterBlockBase.Type.NORMAL);
  }

  @Override
  public boolean validate() {
    return (hasOnlyChild() == false);

  }

  /**
   * Set uncorrelated filter component.
   * 
   * @param filter
   */
  public void setUnCorrelatedFilter(UnCorrelatedFilter filter) {
    ucFilter = filter;
  }

  /**
   * Set uncorrelated filter component
   * 
   * @param expr
   *          SqlASTNode
   */
  public void setUnCorrelatedFilter(SqlASTNode expr) {
    if (ucFilter == null) {
      ucFilter = new UnCorrelatedFilter();
    }
    ucFilter.setRawFilterExpr(expr);
  }

  /**
   * Get uncorrelated filter component.
   * 
   * @return
   */
  public UnCorrelatedFilter getUnCorrelatedFilter() {
    return ucFilter;
  }

  /**
   * Set correlated filter component.
   * 
   * @param filter
   */
  public void setCorrelatedFilter(CorrelatedFilter filter) {
    cFilter = filter;
  }

  /**
   * Set correlated filter component
   * 
   * @param expr
   *          SqlASTNode
   */
  public void setCorrelatedFilter(SqlASTNode expr) {
    if (cFilter == null) {
      cFilter = new CorrelatedFilter();
    }
    cFilter.setRawFilterExpr(expr);
  }

  /**
   * Get correlated filter component.
   * 
   * @return
   */
  public CorrelatedFilter getCorrelatedFilter() {
    return cFilter;
  }

  /**
   * Merge another uncorrelated filter into self's uncorrelated
   * component according to opType.
   * 
   * @param opType
   *          AND or OR
   * @param nf
   *          new uncorrelated filter
   * @param originalFilterAsLeftChild
   *          whether self's uncorrelated
   *          component becomes the left child of new op
   * @throws SqlXlateException
   */
  public void mergeFilter(FilterBlockBase.Type opType, UnCorrelatedFilter nf,
      boolean originalFilterAsLeftChild) throws SqlXlateException {
    if (nf == null) {
      return;
    }
    if (ucFilter == null) {
      ucFilter = nf;
    } else {
      ucFilter.setRawFilterExpr(NormalFilterBlock.mergeFilter(opType, nf.getRawFilterExpr(),
          ucFilter.getRawFilterExpr(), originalFilterAsLeftChild));
    }
  }

  /**
   * Merge another uncorrelated filter into self's uncorrelated
   * component according to opType.
   * 
   * @param logicOp
   *          SqlASTNode SQL92_RESERVED_AND or SQL92_RESERVED_OR
   * @param nf
   *          new uncorrelated filter
   * @param originalFilterAsLeftChild
   *          whether self's uncorrelated
   *          component becomes the left child of new op
   * @throws SqlXlateException
   */
  public void mergeFilter(SqlASTNode logicOp, UnCorrelatedFilter nf,
      boolean originalFilterAsLeftChild) throws SqlXlateException {
    if (nf == null) {
      return;
    }
    if (ucFilter == null) {
      ucFilter = nf;
    } else {
      ucFilter.setRawFilterExpr(NormalFilterBlock.mergeFilter(logicOp, nf.getRawFilterExpr(),
          ucFilter.getRawFilterExpr(), originalFilterAsLeftChild));
    }
  }

  /**
   * Merge another correlated filter into self's correlated
   * component according to opType.
   * 
   * @param opType
   *          AND or OR
   * @param nf
   *          new uncorrelated filter
   * @param originalFilterAsLeftChild
   *          whether self's correlated
   *          component becomes the left child of new op
   * @throws SqlXlateException
   */
  public void mergeFilter(FilterBlockBase.Type opType, CorrelatedFilter nf,
      boolean originalFilterAsLeftChild) throws SqlXlateException {
    if (nf == null) {
      return;
    }
    if (cFilter == null) {
      cFilter = nf;
    } else {
      cFilter.setRawFilterExpr(NormalFilterBlock.mergeFilter(opType, nf.getRawFilterExpr(), cFilter
          .getRawFilterExpr(), originalFilterAsLeftChild));
    }
  }

  /**
   * Merge another correlated filter into self's correlated
   * component according to opType.
   * 
   * @param logicOp
   *          SqlASTNode SQL92_RESERVED_AND or SQL92_RESERVED_OR
   * @param nf
   *          new uncorrelated filter
   * @param originalFilterAsLeftChild
   *          whether self's correlated
   *          component becomes the left child of new op
   * @throws SqlXlateException
   */
  public void mergeFilter(SqlASTNode logicOp, CorrelatedFilter nf, boolean originalFilterAsLeftChild)
      throws SqlXlateException {
    if (nf == null) {
      return;
    }
    if (cFilter == null) {
      cFilter = nf;
    } else {
      cFilter.setRawFilterExpr(NormalFilterBlock.mergeFilter(logicOp, nf.getRawFilterExpr(),
          cFilter.getRawFilterExpr(), originalFilterAsLeftChild));
    }
  }

  /**
   * Base class for both correlated and uncorrelated filter component.
   */
  public static class FilterBase {
    /**
     * raw filter node
     */
    private SqlASTNode filter;

    /**
     * Constructor.
     */
    protected FilterBase() {
    }

    /**
     * Constructor.
     * 
     * @param filter
     *          raw filter node
     */
    protected FilterBase(SqlASTNode filter) {
      this.filter = filter;
    }

    /**
     * Set raw filter
     * 
     * @param filter
     */
    public void setRawFilterExpr(SqlASTNode filter) {
      this.filter = filter;
    }

    /**
     * Get raw filter
     * 
     * @param filter
     */
    public SqlASTNode getRawFilterExpr() {
      return filter;
    }
  }

  /**
   * 
   * UnCorrelatedFilter class.
   * 
   */
  public static class UnCorrelatedFilter extends FilterBase {
    public UnCorrelatedFilter() {
      super();
    }

    public UnCorrelatedFilter(SqlASTNode filter) {
      super(filter);
    }
  }

  /**
   * 
   * CorrelatedFilter class.
   * 
   */
  public static class CorrelatedFilter extends FilterBase {
    public CorrelatedFilter() {
      super();
    }

    public CorrelatedFilter(SqlASTNode filter) {
      super(filter);
    }
  }

  /**
   * Utility for merging normal filters
   * 
   * @param logicOp
   * @param newFilter
   * @param oriFilter
   * @param originalFilterAsLeftChild
   * @return
   * @throws SqlXlateException
   */
  private static SqlASTNode mergeFilter(SqlASTNode logicOp, SqlASTNode newFilter,
      SqlASTNode oriFilter, boolean originalFilterAsLeftChild) throws SqlXlateException {
    assert (logicOp.getType() == PLSQLParser.SQL92_RESERVED_AND || logicOp.getType() == PLSQLParser.SQL92_RESERVED_OR);
    SqlASTNode op = SqlXlateUtil.newSqlASTNode(logicOp.getToken());
    return mergeFilterInternal(op, newFilter, oriFilter, originalFilterAsLeftChild);
  }

  /**
   * Utility for merging normal filters
   * 
   * @param logicOp
   * @param newFilter
   * @param oriFilter
   * @param originalFilterAsLeftChild
   * @return
   * @throws SqlXlateException
   */
  private static SqlASTNode mergeFilter(FilterBlockBase.Type opType, SqlASTNode newFilter,
      SqlASTNode oriFilter, boolean originalFilterAsLeftChild) throws SqlXlateException {
    SqlASTNode op = null;
    if (opType == FilterBlockBase.Type.LOGIC_AND) {
      op = SqlXlateUtil.newSqlASTNode(PLSQLParser.SQL92_RESERVED_AND, "and");
    } else if (opType == FilterBlockBase.Type.LOGIC_OR) {
      op = SqlXlateUtil.newSqlASTNode(PLSQLParser.SQL92_RESERVED_OR, "or");
    } else {
      throw new SqlXlateException("unsupported merging logical op" + opType);
    }
    return mergeFilterInternal(op, newFilter, oriFilter, originalFilterAsLeftChild);
  }

  /**
   * Utility for merging normal filters
   * 
   * @param logicOp
   * @param newFilter
   * @param oriFilter
   * @param originalFilterAsLeftChild
   * @return
   * @throws SqlXlateException
   */
  private static SqlASTNode mergeFilterInternal(SqlASTNode logicOp, SqlASTNode newFilter,
      SqlASTNode oriFilter, boolean originalFilterAsLeftChild) {
    SqlASTNode opNew = logicOp;
    if (originalFilterAsLeftChild) {
      // add original filter as left child
      opNew.addChild(oriFilter);
      opNew.addChild(newFilter);
    } else {
      // add original filter as right child
      opNew.addChild(newFilter);
      opNew.addChild(oriFilter);
    }
    return opNew;
  }
}
