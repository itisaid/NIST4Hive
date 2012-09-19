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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.exec.ColumnInfo;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.RowResolver;
import org.apache.hadoop.hive.ql.parse.sql.FilterBlockBase.Type;
import org.apache.hadoop.hive.ql.parse.sql.NormalFilterBlock.CorrelatedFilter;
import org.apache.hadoop.hive.ql.parse.sql.NormalFilterBlock.UnCorrelatedFilter;
import org.apache.hadoop.hive.ql.parse.sql.QueryInfo.Column;
import org.apache.hadoop.hive.ql.parse.sql.SqlXlateUtil.HiveMetadata;
import org.apache.hadoop.hive.ql.parse.sql.SubQFilterBlock.AggrPhase;
import org.apache.hadoop.hive.ql.parse.sql.SubQFilterBlock.OPType;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

import br.com.porcelli.parser.plsql.PLSQLParser;



/**
 * A class to generate Hive AST from SQL AST.
 * 
 * The original SQL AST produced by antlr will not be modified.
 * 
 * generate() method is the entry point of generation.
 * In this method we firstly build several facility data structures
 * i.e. QueryInfo tree and FilterBlock tree.
 * Then we use these facility data structures to generate Hive AST.
 * 
 * QueryInfo Tree is to facilitate query's main body generation
 * FilterBlock Tree is to facilitate transformation of filters, especially
 * those contains subqueries (e.g. in (select...from...))
 * 
 */
public class HiveASTGenerator {
  private static final Log LOG = LogFactory.getLog("hive.ql.parse.sql.HiveASTGenerator");
  /**
   * Alias generator for generating table alias
   */
  private final SqlXlateUtil.AliasGenerator aliasGen = new SqlXlateUtil.AliasGenerator();
  /**
   * Operator/Function factory for op/function generation
   */
  private final OpFuncFactory opfuncFactory = new OpFuncFactory();
  /**
   * Hive Metadata. Used to retrieve table meta info, including schema, etc.
   */
  private final HiveMetadata meta;

  /**
   * A bunch of statistics flag indicating the characteristics of this query
   * This is used to decide whether we enable certain part of processing
   * TODO make the switchers fine grit.
   */
  private static class AnalysisStat {
    private boolean hasSubqFilter;
    private boolean hasViewInSrc;
    private boolean hasOrderByPosition;
    private boolean hasJoinUsing;
    private boolean hasTopLevelSubQ;
    private boolean hasMultiTabSelect;

    public AnalysisStat() {
      hasSubqFilter = false;
      hasViewInSrc = false;
      hasOrderByPosition = false;
      hasJoinUsing = false;
      hasTopLevelSubQ = false;
      hasMultiTabSelect = false;
    }

    public void setHasSubQFilter(boolean hasSubQFilter) {
      this.hasSubqFilter = hasSubQFilter;
    }

    public boolean getHasSubQFilter() {
      return this.hasSubqFilter;
    }

    public void setHasView(boolean hasView) {
      this.hasViewInSrc = hasView;
    }

    public boolean getHasView() {
      return hasViewInSrc;
    }

    public void setHasOrderByPosition(boolean hasOrderByPosition) {
      this.hasOrderByPosition = hasOrderByPosition;
    }

    public boolean getHasOrderByPosition() {
      return hasOrderByPosition;
    }

    public void setHasJoinUsing(boolean hasJoinUsing) {
      this.hasJoinUsing = hasJoinUsing;
    }

    public boolean getHashJoinUsing() {
      return hasJoinUsing;
    }

    public void setHasTopLevelSubQ(boolean hasTopLevelSubQ) {
      this.hasTopLevelSubQ = hasTopLevelSubQ;
    }

    public boolean getHasTopLevelSubQ() {
      return this.hasTopLevelSubQ;
    }

    public void setHasMultiTabSelect(boolean hasMultiTabSelect) {
      this.hasMultiTabSelect = hasMultiTabSelect;
    }

    public boolean getHasMultiTabSelect() {
      return this.hasMultiTabSelect;
    }
  }

  private final AnalysisStat analysisStat = new AnalysisStat();

  /**
   * Constructor.
   * 
   * @param conf
   * @throws HiveException
   */
  public HiveASTGenerator(HiveConf conf) throws SqlXlateException {
    meta = new HiveMetadata(conf);
  }

  /**
   * Entry of the generation process.
   * 
   * @param ast
   *          Sql AST
   * @return Hive AST
   * @throws SqlXlateException
   */
  public ASTNode generate(SqlASTNode ast) throws SqlXlateException {
    // check if the root is STATEMENTS type
    if (ast.getType() != PLSQLParser.STATEMENTS) {
      SqlXlateUtil.error(ast);
      return null;
    }
    // prepare facility data structures
    prepare(ast, null);
    LOG.info("Preparation Done.");
    // generate Hive AST for SQL AST with root STATEMENTS
    ASTNode root = genForStatements(ast);
    LOG.info("Generation Done. Hive AST after generation : " + root.toStringTree());
    // check if meta data contains all src
    // this is to find if there's view or
    // if there's any table not found in meta
    checkTableMeta(root);
    // now we don't allow view coexists with new features
    // TODO gonna remove this when we enabled it later
    if (analysisStat.getHasView()
        &&
        (analysisStat.getHasSubQFilter() || analysisStat.getHasOrderByPosition()
            || analysisStat.getHashJoinUsing() || analysisStat.getHasTopLevelSubQ() || analysisStat
            .getHasMultiTabSelect())) {
      throw new SqlXlateException("We do not support view together with one of below features:" +
          "1. subquery filter 2. orderby position 3. join using");
    }
    // post process the generated AST to fix necessary subtrees
    // only do post processing when we don't have view reference
    // coexists with any of the new features needed post processing
    // TODO make this find grit
    if (!analysisStat.getHasView()
        &&
        (analysisStat.getHasSubQFilter() || analysisStat.getHasOrderByPosition()
            || analysisStat.getHashJoinUsing() || analysisStat.getHasTopLevelSubQ() || analysisStat
            .getHasMultiTabSelect())) {
      postprocess(root);
    }
    LOG.info("PostProcess Done. Hive AST after Post Processing: " + root.toStringTree());
    return root;
  }


  /**
   * Check if all table sources are found in hive meta data
   * 
   * @param ast
   * @throws SqlXlateException
   */
  private void checkTableMeta(ASTNode ast) throws SqlXlateException {
    // do it in dfs manner
    for (int i = 0; i < ast.getChildCount(); i++) {
      checkTableMeta((ASTNode) ast.getChild(i));
    }
    if (ast.getType() == HiveParser.TOK_TABREF) {
      ASTNode tabName = (ASTNode) ast.getFirstChildWithType(HiveParser.TOK_TABNAME);
      if (tabName.getChildCount() == 1) {
        if (getMeta().getRRForTbl(tabName.getChild(0).getText()) == null) {
          analysisStat.setHasView(true);
        }
      } else if (tabName.getChildCount() == 2) {
        if (getMeta().getRRForTbl(tabName.getChild(0).getText(),
            tabName.getChild(1).getText()) == null) {
          analysisStat.setHasView(true);
        }
        if (ast.getChildCount() == 1) {
          // A quick fix for cases like: select schema.t.a from schema.t
          // if we found table schema.t but no table alias found, we add an alias for the table
          // TODO currently we set the table internal name as the alias. This would lead to error if
          // tables in two schemas has the same internal name. Later we should add a unique table
          // alias and replace all references
          SqlXlateUtil.attachChild(ast,
              SqlXlateUtil.newASTNode(HiveParser.Identifier, tabName.getChild(1).getText()));
        }
      }
    }
  }

  /**
   * Get metadata object
   * 
   * @return
   */
  private HiveMetadata getMeta() {
    return meta;
  }


  /**
   * Prepare the Facility data structures for later AST generation.
   * 
   * @param ast
   *          SQL AST
   * @param qInfo
   *          the current QueryInfo object
   * @throws SqlXlateException
   */
  protected void prepare(SqlASTNode ast, QueryInfo qInfo) throws SqlXlateException {
    // walk the Sql AST recursively in top down manner
    boolean skipRecursion = false;

    switch (ast.getType()) {
    case PLSQLParser.STATEMENTS:
      // Prepare the root QueryInfo at SQL AST root node
      qInfo = prepareQInfo(ast, qInfo);
      break;
    case PLSQLParser.SQL92_RESERVED_SELECT: {
      // Prepare a new QueryInfo for each query (including top level queries and subqueries in from
      // clause)
      // subqueries in filters in where clauses will not have new QInfo
      // created. FilterBlocks will be created for them.
      qInfo = prepareQInfo(ast, qInfo);
      // Prepare the top most Filter Blocks
      prepareRootFilterBlocks(ast, qInfo);
      break;
    }
    case PLSQLParser.SQL92_RESERVED_INSERT:
      // Prepare the insert destination
      prepareInsertDestination(ast, qInfo);
      break;
    case PLSQLParser.SQL92_RESERVED_FROM:
      // Prepare the from subtree
      prepareFrom(ast, qInfo);
      break;
    case PLSQLParser.SELECT_ITEM:
      // prepare the subqueris in select expressions
      // TODO, treat select expr filters and where filters
      // in a more uniform way
      prepareColFilterBlocks(ast, (SubQFilterBlock) qInfo.getFilterBlockTreeRoot().getOnlyChild(),
          qInfo);
      skipRecursion = true;
      break;
    case PLSQLParser.SQL92_RESERVED_WHERE:
      // Prepare Filter Blocks in where.
      // the nested subq/where will be processed
      // in the prepareFilters method
      prepareFilterBlocks(ast, qInfo);
      skipRecursion = true;
      break;
    default:
    }
    // add reference to qInfo ot each Sql AST node
    ast.setQueryInfo(qInfo);
    // if do not skip recursion, iterate all the children
    if (!skipRecursion) {
      for (int i = 0; i < ast.getChildCount(); i++) {
        prepare((SqlASTNode) ast.getChild(i), qInfo);
      }
    }
  }


  /**
   * Prepare Query Info object
   * 
   * @param ast
   * @param qInfo
   * @return
   */
  private QueryInfo prepareQInfo(SqlASTNode ast, QueryInfo qInfo) {
    QueryInfo nqi = new QueryInfo();
    nqi.setParentQueryInfo(qInfo);
    if (qInfo != null) {
      nqi.setSelectKeyForThisQ(ast);
    }
    return nqi;
  }

  /**
   * Construct the root Filter Block and the top SubQFilterBlock
   * 
   * @param ast
   * @param qInfo
   * @throws SqlXlateException
   */
  private void prepareRootFilterBlocks(SqlASTNode ast, QueryInfo qInfo) throws SqlXlateException {
    // Create a Filter Block Tree root node
    // for each query and set a reference in qInfo
    OpNULLFilterBlock root = new OpNULLFilterBlock();
    qInfo.setFilterBlockTreeRoot(root);
    // Create the top level SubQFilterBlock as child of root FBTree.
    // Add this query's information into the object
    SubQFilterBlock topSubQ = new SubQFilterBlock(OPType.UNKNOWN);
    topSubQ.setQueryInfo(qInfo);
    topSubQ.setFromInSubQ(SqlXlateUtil.getFromInSelectRaw(ast));
    topSubQ.addSelectListInSubQ(SqlXlateUtil.getSelectListInSelectRaw(ast));
    topSubQ.setSelectKeyInSubQ(ast);
    topSubQ.setIsTopSubQBlock();
    root.setOnlyChild(topSubQ);
  }

  /**
   * 
   * Context used for preparing Filter Blocks.
   * 
   */
  private static class FBPrepContext {
    /**
     * A stack to keep track of nested query root nodes
     */
    private final Stack<SqlASTNode> selectKeyStack = new Stack<SqlASTNode>();
    /**
     * Map from SQL AST node to filter block.
     * Not every SQL AST node has a filter block associated.
     * it's just temporary data structure
     */
    private final HashMap<SqlASTNode, FilterBlockBase> astnodeToFilterBlock = new HashMap<SqlASTNode, FilterBlockBase>();

    /**
     * It's for optimizing the Tree
     * 
     * If we met FB subtree like (AND (SUBQ NORMAL))
     * We would like to add NORMAL to the parent SUBQ's normal filter
     * So we register it here and when SUBQ's are pushed down
     * we add NORMAL to SUBQ's parent q.
     */
    private final HashMap<SubQFilterBlock, NormalFilterBlock> parentQNFBmap = new HashMap<SubQFilterBlock, NormalFilterBlock>();

    /**
     * Get the select key stack
     * 
     * @return
     */
    public Stack<SqlASTNode> getSelectKeyStack() {
      return selectKeyStack;
    }

    /**
     * get the node to filter block map
     * 
     * @return
     */
    public Map<SqlASTNode, FilterBlockBase> getNodeToFBMap() {
      return astnodeToFilterBlock;
    }

    /**
     * get the normal fb to parent subq map
     * 
     * @return
     */
    public Map<SubQFilterBlock, NormalFilterBlock> getNFBForParentQMap() {
      return parentQNFBmap;
    }
  }

  /**
   * Prepare the Filter Blocks tree in where.
   * TODO later we shall add support for filters in having also.
   * 
   * @param where
   *          the SQL AST WHERE node
   * @param qInfo
   *          the queryInfo of the enclosing query of where
   * @throws SqlXlateException
   */
  private void prepareFilterBlocks(SqlASTNode where, QueryInfo qInfo) throws SqlXlateException {
    // create context
    FBPrepContext ctx = new FBPrepContext();
    // get the root and top subq fb
    FilterBlockBase root = qInfo.getFilterBlockTreeRoot();
    assert (root.getOnlyChild().getType() == FilterBlockBase.Type.SUBQ);
    assert (((SubQFilterBlock) root.getOnlyChild()).isTopSubQBlock());
    SubQFilterBlock topSubQ = (SubQFilterBlock) root.getOnlyChild();
    // build the entire FB tree
    ctx.getSelectKeyStack().push(qInfo.getSelectKeyForThisQ());
    buildFilterBlockTree(topSubQ, ctx, qInfo, where);
    ctx.getSelectKeyStack().pop();
    // optimize the FB tree
    optimizeFilterBlockTree(topSubQ, ctx);
    // validate the FB tree
    validateFilterBlockTree(root);
  }

  /**
   * Prepare Filter Blocks for SubQueries in Select Item
   * e.g. SELECT A, (SELECT B FROM T2) FROM T1,T2 WHERE...
   * 
   * @param selectItem
   * @param parentFB
   * @param qInfo
   * @throws SqlXlateException
   */
  private void prepareColFilterBlocks(SqlASTNode selectItem, SubQFilterBlock parentFB,
      QueryInfo qInfo) throws SqlXlateException {
    SqlASTNode expr = (SqlASTNode) selectItem.getFirstChildWithType(PLSQLParser.EXPR);
    if (expr == null) {
      return;
    }
    SqlASTNode subq = (SqlASTNode) expr.getFirstChildWithType(PLSQLParser.SUBQUERY);
    if (subq == null) {
      return;
    }
    // TODO currently we don't support nested subqueries in select expr subqueries
    // later we shall add support for this
    if (SqlXlateUtil.hasNodeTypeInTree((SqlASTNode) subq.getChild(0), PLSQLParser.SUBQUERY)) {
      throw new SqlXlateException("Do not support subqueries in subqueries in select item");
    }
    // create context
    FBPrepContext ctx = new FBPrepContext();
    // create the top fb
    SubQFilterBlock fb = createSubQFB(ctx, OPType.COLUMNSUBQ, selectItem, subq, null, qInfo,
        false);
    ctx.getSelectKeyStack().push(fb.getSelectKeyInSubQ());
    // build the FB tree for filters in this selexpr subq
    buildFilterBlockTree(fb, ctx, qInfo, SqlXlateUtil.getWhereInSelectRaw(fb.getSelectKeyInSubQ()));
    ctx.getSelectKeyStack().pop();
    // optimize the FB tree
    optimizeFilterBlockTree(fb, ctx);
    // validate the FB tree
    validateFilterBlockTree(fb);
    parentFB.addColSubQFB(fb);
    LOG.debug("Added col FB : " + fb.toStringTree() + ", for : " + selectItem.toStringTree());
  }

  /**
   * build the initial FB tree
   * 
   * @param parentSubQ
   * @param ctx
   * @param qInfo
   * @param where
   * @throws SqlXlateException
   */
  private void buildFilterBlockTree(SubQFilterBlock parentSubQ,
      FBPrepContext ctx, QueryInfo qInfo, SqlASTNode where) throws SqlXlateException {
    // initialize the filter block tree
    initFilterBlocks(where, qInfo, ctx);
    // attach the subtree to parent fb
    FilterBlockBase fb = getFBForWhere(ctx, where, qInfo, ctx.getSelectKeyStack().peek());
    if (fb.getType() == FilterBlockBase.Type.NORMAL) {
      parentSubQ.setNormalFilter((NormalFilterBlock) fb);
    } else {
      parentSubQ.setOnlyChild(fb);
      analysisStat.setHasSubQFilter(true);
    }
  }

  /**
   * validate filter block tree
   * 
   * @param root
   * @throws SqlXlateException
   */
  private void validateFilterBlockTree(FilterBlockBase fb) throws SqlXlateException {
    // validate the tree recursively in a pre-order
    if (fb == null) {
      return;
    }
    if (!fb.validate()) {
      LOG.error("Invalide Filter Block starting from : " + fb.toStringTree());
      throw new SqlXlateException("Invalid Filter Block node");
    }
    validateFilterBlockTree(fb.getLeftChild());
    validateFilterBlockTree(fb.getRightChild());
  }

  /**
   * Transform the Filter Block Tree to make it amenable to
   * build chaining joins.
   * 
   * @param rootFB
   * @throws SqlXlateException
   */
  private void optimizeFilterBlockTree(FilterBlockBase topSubQ, FBPrepContext ctx)
      throws SqlXlateException {
    LOG.info("The original Filter Block Tree: " + topSubQ.toStringTree());
    // NOT op digestion should come ahead of other processing
    digestNotOp(topSubQ, ctx);
    LOG.debug("Digest NOT op Done. Filter Block Tree is now : " + topSubQ.toStringTree());
    // merge normal filters when necessary to minimize the use of set operations
    mergeNormalFilters(topSubQ, ctx);
    LOG.debug("Merge Normal Filters Done. Filter Block Tree is now : " + topSubQ.toStringTree());
    // push down SubQ in a DFS manner
    pushDownSubQFB(topSubQ, ctx);
    // TODO we may add another step to push correlated filters to the highest FB of its referred
    // scope. This would allows us to handle correlated filters in more than one depth of nested
    // subq
    LOG.info("The optimized Filter Block Tree: " + topSubQ.toStringTree());
  }


  /**
   * Digest all Not Op and merge into subq or normal filter semantics
   * After this process there should not be any NOT FB in the FB tree.
   * 
   * @param ctx
   * @param fb
   */
  private void digestNotOp(FilterBlockBase fb, FBPrepContext ctx) {
    // recursively digest the not op in a top down manner
    if (fb.getType() == FilterBlockBase.Type.LOGIC_NOT) {
      FilterBlockBase child = fb.getOnlyChild();
      FilterBlockBase newOp = null;
      switch (child.getType()) {
      case LOGIC_AND:
      case LOGIC_OR: {
        // not (a and b) -> (not a) or (not b)
        newOp = (child.getType() == Type.LOGIC_AND) ? new OpORFilterBlock()
            : new OpANDFilterBlock();
        FilterBlockBase lhsNot = new OpNOTFilterBlock();
        FilterBlockBase rhsNot = new OpNOTFilterBlock();
        lhsNot.setOnlyChild(child.getLeftChild());
        rhsNot.setOnlyChild(child.getRightChild());
        newOp.setLeftChild(lhsNot);
        newOp.setRightChild(rhsNot);
        break;
      }
      case LOGIC_NOT:
        newOp = child.getOnlyChild();
        break;
      case SUBQ: {
        switch (((SubQFilterBlock) child).getOpType()) {
        case ALL: {
          ((SubQFilterBlock) child).setOpType(OPType.SOMEANY);
          SqlASTNode op = ((SubQFilterBlock) child).getOp();
          // Note: here we directly change the original SqlASTNode
          revertRelationalOp(op);
          break;
        }
        case SOMEANY: {
          ((SubQFilterBlock) child).setOpType(OPType.ALL);
          SqlASTNode op = ((SubQFilterBlock) child).getOp();
          // Note: here we directly change the original SqlASTNode
          revertRelationalOp(op);
          break;
        }
        case RELATIONAL: {
          SqlASTNode op = ((SubQFilterBlock) child).getOp();
          // Note: here we directly change the original SqlASTNode
          revertRelationalOp(op);
          break;
        }
        case EXISTS:
          ((SubQFilterBlock) child).setOpType(OPType.NOTEXISTS);
          break;
        case NOTEXISTS:
          ((SubQFilterBlock) child).setOpType(OPType.EXISTS);
          break;
        case IN:
          ((SubQFilterBlock) child).setOpType(OPType.NOTIN);
          break;
        case NOTIN:
          ((SubQFilterBlock) child).setOpType(OPType.IN);
          break;
        case ISNULL:
          ((SubQFilterBlock) child).setOpType(OPType.ISNOTNULL);
          break;
        case ISNOTNULL:
          ((SubQFilterBlock) child).setOpType(OPType.ISNULL);
          break;
        default:
          // should not come here
          assert (false);
        }
        newOp = child;
        break;
      }
      case NORMAL:
        // we know all normal filters are either UnCorrelated or
        // correlated, don't have both case at present
        NormalFilterBlock nf = (NormalFilterBlock) child;
        assert (nf.getCorrelatedFilter() == null || nf.getUnCorrelatedFilter() == null);
        CorrelatedFilter cf = nf.getCorrelatedFilter();
        UnCorrelatedFilter ucf = nf.getUnCorrelatedFilter();
        // It's not likely to result in chaining SqlASTNode
        // as any chaining NOT FB has been collapsed from top down
        if (cf != null) {
          cf.setRawFilterExpr(
              SqlXlateUtil.revertFilter(cf.getRawFilterExpr(), false));
        }
        if (ucf != null) {
          ucf.setRawFilterExpr(
              SqlXlateUtil.revertFilter(ucf.getRawFilterExpr(), false));
        }
        newOp = child;
        break;
      default:
      }
      fb.getParent().replaceChildTree(fb, newOp);
    }

    if (fb.hasLeftChild()) {
      digestNotOp(fb.getLeftChild(), ctx);
    }
    if (fb.hasRightChild()) {
      digestNotOp(fb.getRightChild(), ctx);
    }

  }

  /**
   * Revert the relational op the have the opposite semantic
   * 
   * @param op
   */
  private void revertRelationalOp(SqlASTNode op) {
    switch (op.getType()) {
    case PLSQLParser.EQUALS_OP:
      SqlXlateUtil.changeNodeToken(op, PLSQLParser.NOT_EQUAL_OP, "<>");
      break;
    case PLSQLParser.NOT_EQUAL_OP:
      SqlXlateUtil.changeNodeToken(op, PLSQLParser.EQUALS_OP, "=");
      break;
    case PLSQLParser.LESS_THAN_OR_EQUALS_OP:
      SqlXlateUtil.changeNodeToken(op, PLSQLParser.GREATER_THAN_OP, ">");
      break;
    case PLSQLParser.LESS_THAN_OP:
      SqlXlateUtil.changeNodeToken(op, PLSQLParser.GREATER_THAN_OR_EQUALS_OP, ">=");
      break;
    case PLSQLParser.GREATER_THAN_OR_EQUALS_OP:
      SqlXlateUtil.changeNodeToken(op, PLSQLParser.LESS_THAN_OP, "<");
      break;
    case PLSQLParser.GREATER_THAN_OP:
      SqlXlateUtil.changeNodeToken(op, PLSQLParser.LESS_THAN_OR_EQUALS_OP, "<=");
      break;
    default:
      assert (false);
    }
  }

  /**
   * merge normal filters where possible
   * 
   * @param onlyChild
   * @param ctx
   * @throws SqlXlateException
   */
  private void mergeNormalFilters(FilterBlockBase fb, FBPrepContext ctx) throws SqlXlateException {
    // do it in DFS manner
    if (fb.hasLeftChild()) {
      mergeNormalFilters(fb.getLeftChild(), ctx);
    }
    if (fb.hasRightChild()) {
      mergeNormalFilters(fb.getRightChild(), ctx);
    }
    // if AND has one child SubQ and the other Normal,
    // merge into one SubQ
    if (fb.getType() == FilterBlockBase.Type.LOGIC_AND) {
      if (fb.getLeftChild().getType() == FilterBlockBase.Type.NORMAL
          && fb.getRightChild().getType() == FilterBlockBase.Type.SUBQ) {
        SubQFilterBlock right = (SubQFilterBlock) fb.getRightChild();
        ctx.getNFBForParentQMap().put(right, (NormalFilterBlock) fb.getLeftChild());
        fb.getParent().replaceChildTree(fb, right);
      } else if (fb.getLeftChild().getType() == FilterBlockBase.Type.SUBQ &&
          fb.getRightChild().getType() == FilterBlockBase.Type.NORMAL) {
        SubQFilterBlock left = (SubQFilterBlock) fb.getLeftChild();
        ctx.getNFBForParentQMap().put(left, (NormalFilterBlock) fb.getRightChild());
        fb.getParent().replaceChildTree(fb, left);
      } else if (fb.getLeftChild().getType() == FilterBlockBase.Type.NORMAL &&
          fb.getRightChild().getType() == FilterBlockBase.Type.NORMAL) {
        NormalFilterBlock left = (NormalFilterBlock) fb.getLeftChild();
        NormalFilterBlock right = (NormalFilterBlock) fb.getRightChild();
        left.mergeFilter(FilterBlockBase.Type.LOGIC_AND, right.getCorrelatedFilter(), true);
        left.mergeFilter(FilterBlockBase.Type.LOGIC_AND, right.getUnCorrelatedFilter(), true);
        fb.getParent().replaceChildTree(fb, left);
      }
    } else if (fb.getType() == FilterBlockBase.Type.SUBQ) {
      // merge child normal into subq
      if (fb.hasOnlyChild() && fb.getOnlyChild().getType() == FilterBlockBase.Type.NORMAL) {
        ((SubQFilterBlock) fb).mergeNormalFilter(FilterBlockBase.Type.LOGIC_AND,
            (NormalFilterBlock) fb.getOnlyChild(), true);
        fb.setOnlyChild(null);
      }
    }


  }

  /**
   * Push all SubQFilterBlocks to the bottom of the FB Tree.
   * 
   * @param fb
   * @throws SqlXlateException
   */
  private void pushDownSubQFB(FilterBlockBase fb, FBPrepContext ctx) throws SqlXlateException {
    // recursively push down the SubQ FB in a DFS manner
    if (fb.hasLeftChild()) {
      pushDownSubQFB(fb.getLeftChild(), ctx);
    }
    if (fb.hasRightChild()) {
      pushDownSubQFB(fb.getRightChild(), ctx);
    }

    if (fb.getType() == FilterBlockBase.Type.SUBQ) {
      // collect the nearest non Op descendants
      if (!fb.hasOnlyChild()) {
        return;
      }
      // For aggregation cases, we need to split the FB into two.
      // e.g. select count(*) from t where cond-A or cond-B
      // would be trasnlated to select count(*) from ( select * from t where cond-A union select *
      // from t where cond-B) instead of select count(*) from t where cond-A union select count(*)
      // from t where cond-B
      // TODO now we only handle select count(*),
      // We'll extend it to handle more cases
      boolean isAggregationFunc = isAggregationFunction((SubQFilterBlock) fb);

      FilterBlockBase child = (FilterBlockBase) fb.getOnlyChild();
      List<FilterBlockBase> result = new ArrayList<FilterBlockBase>();
      collectNearestNonOpDescendants(child, result);
      LOG.debug("Collected Nearset Descendants: " + result);
      // if the nearest non Op descendant set has only one node,
      // then that node is the direct child of fb, don't push.
      // Otherwise, push down fb
      if (result.size() > 1) {
        for (FilterBlockBase item : result) {

          SubQFilterBlock dupFb = new SubQFilterBlock((SubQFilterBlock) fb);
          if (isAggregationFunc) {
            dupFb.setAggrPhase(AggrPhase.SELECTALL_PHASE);
            ((SubQFilterBlock) fb).setAggrPhase(AggrPhase.AGGREGATION_PHASE);
          }
          if (item.getType() == FilterBlockBase.Type.SUBQ) {
            // if item is subq, attach current subq as parent of this item
            insertFBAsParent(dupFb, item);
            // attach any normal filters to this fb passed on from child
            NormalFilterBlock pFB = ctx.getNFBForParentQMap().get(item);
            if (pFB != null) {
              dupFb.mergeNormalFilter(FilterBlockBase.Type.LOGIC_AND, pFB, true);
            }
          } else {
            // if item is normal FB
            insertFBAsParent(dupFb, item);
            dupFb.setOnlyChild(null);
            dupFb.mergeNormalFilter(FilterBlockBase.Type.LOGIC_AND, (NormalFilterBlock) item, true);
          }
        }
        // if it's aggregation function, don't remove the current FB
        if (!isAggregationFunc) {
          // if is aggregation function keep the parent subQFB
          removeSubQFB((SubQFilterBlock) fb);
        }
      } else if (result.size() == 1) {
        // result is fb's direct child
        assert (child == fb.getOnlyChild());
        // attach any normal filters to this fb passed on from child
        NormalFilterBlock pFB = ctx.getNFBForParentQMap().get(child);
        if (pFB != null) {
          ((SubQFilterBlock) fb).mergeNormalFilter(FilterBlockBase.Type.LOGIC_AND,
              pFB, true);
        }
      }
    }
  }

  /**
   * If subq selection is select count(*)
   * 
   * @param fb
   * @return
   */
  public static boolean isSelectCountStar(SubQFilterBlock fb) {
    List<SqlASTNode> selectList = fb.getSelectListInSubQ();
    if (selectList.size() == 1 && selectList.get(0).getType() == PLSQLParser.SELECT_ITEM) {
      return SqlXlateUtil.isSelectCountStar(selectList.get(0));
    }
    return false;
  }

  /**
   * If fb's query is aggregation selection
   * 
   * @param fb
   * @return
   */
  private boolean isAggregationFunction(SubQFilterBlock fb) {
    boolean isSelectCountStart = isSelectCountStar(fb);
    // we may add more aggregation function support later
    return isSelectCountStart;
  }

  /**
   * Insert a FB as parent of child FB
   * 
   * @param toInsert
   *          the node to be inserted
   * @param child
   *          the node to be come the child of toInsert
   */
  private void insertFBAsParent(FilterBlockBase toInsert, FilterBlockBase child) {
    FilterBlockBase parent = child.getParent();
    if (parent.getLeftChild() == child) {
      parent.setLeftChild(toInsert);
      toInsert.setOnlyChild(child);
    } else {
      parent.setRightChild(toInsert);
      toInsert.setOnlyChild(child);
    }
  }

  /**
   * Remove the SubQFB from the FB tree and retain the tree structure.
   * 
   * @param toRemove
   * @throws SqlXlateException
   */
  private void removeSubQFB(SubQFilterBlock toRemove) throws SqlXlateException {
    FilterBlockBase parent = toRemove.getParent();
    FilterBlockBase child = toRemove.getOnlyChild();
    if (parent.getLeftChild() == toRemove) {
      parent.setLeftChild(child);
    } else {
      parent.setRightChild(child);
    }
  }

  /**
   * Collect the nearest NonOpDescendants (i.e.SubQFilterBlock & NormalFilterBlock)
   * 
   * @param fb
   *          the starting point
   * @param result
   *          result node set
   */
  private void collectNearestNonOpDescendants(FilterBlockBase fb, List<FilterBlockBase> result) {
    if (fb.getType() == FilterBlockBase.Type.SUBQ || fb.getType() == FilterBlockBase.Type.NORMAL) {
      result.add(fb);
    } else if (fb.getType() == FilterBlockBase.Type.LOGIC_AND ||
        fb.getType() == FilterBlockBase.Type.LOGIC_OR) {
      collectNearestNonOpDescendants(fb.getLeftChild(), result);
      collectNearestNonOpDescendants(fb.getRightChild(), result);
    }
  }

  /**
   * Initialize Filter Blocks Tree.
   * 
   * @param ast
   *          the ASTNode
   * @param qInfo
   * @param ctx
   * @throws SqlXlateException
   */
  private void initFilterBlocks(SqlASTNode ast, QueryInfo qInfo, FBPrepContext ctx)
      throws SqlXlateException {
    // walk the SQL AST recursively in DFS manner and create a FB tree
    boolean skipRecursion = false;

    // TODO currently don't allow nested subquries in select expression
    // and from clause, later to support
    // skip the subtree to make the processing faster
    if (ast.getType() == PLSQLParser.SELECT_LIST ||
        ast.getType() == PLSQLParser.SQL92_RESERVED_FROM) {

      if (SqlXlateUtil.hasNodeTypeInTree(ast, PLSQLParser.SUBQUERY)) {
        throw new SqlXlateException("Do not support subquries in from or " +
            "select times in subquries in filters");
      }
      skipRecursion = true;
    }

    // push select key of enclosing query
    if (ast.getType() == PLSQLParser.SQL92_RESERVED_SELECT) {
      ctx.getSelectKeyStack().push(ast);
    }
    // traverse children first
    if (!skipRecursion) {
      for (int i = 0; i < ast.getChildCount(); i++) {
        initFilterBlocks((SqlASTNode) ast.getChild(i), qInfo, ctx);
      }
    }
    // pop select key of enclosing query
    if (ast.getType() == PLSQLParser.SQL92_RESERVED_SELECT) {
      ctx.getSelectKeyStack().pop();
    }

    // handle this node
    switch (ast.getType()) {
    case PLSQLParser.SQL92_RESERVED_FROM:
      // TODO add processing for from later
      // currently we skipped this subtree and won't do this branch
      processFromInWhereSubTree(ast);
      break;
    // logic operator
    case PLSQLParser.SQL92_RESERVED_AND:
    case PLSQLParser.SQL92_RESERVED_OR: {
      putCreateAndOrOP(ctx, ast, qInfo);
      break;
    }
    case PLSQLParser.SQL92_RESERVED_NOT: {
      putCreateNotOp(ctx, ast, qInfo);
      break;
    }
      // in / not in
    case PLSQLParser.SQL92_RESERVED_IN:
    case PLSQLParser.NOT_IN: {
      if (ast.getChild(1).getType() == PLSQLParser.SUBQUERY) {
        SqlASTNode leftChild = (SqlASTNode) ast.getChild(0);
        SqlASTNode subq = (SqlASTNode) ast.getChild(1);
        putCreateSubQFilter(ctx, (ast.getType() == PLSQLParser.SQL92_RESERVED_IN) ? OPType.IN
            : OPType.NOTIN, ast, subq, leftChild, qInfo, false);
      }
      break;
    }
      // is null / is not null
    case PLSQLParser.IS_NULL:
    case PLSQLParser.IS_NOT_NULL: {
      if (ast.getChild(0).getType() == PLSQLParser.SUBQUERY) {
        SqlASTNode subq = (SqlASTNode) ast.getChild(0);
        SqlASTNode selectKey = (SqlASTNode) subq
            .getFirstChildWithType(PLSQLParser.SQL92_RESERVED_SELECT);
        SqlASTNode selectList = SqlXlateUtil.getSelectListInSelectRaw(selectKey);
        if (selectList.getType() != PLSQLParser.SELECT_LIST) {
          throw new SqlXlateException("Illegal select list in ISNULL/ISNOTNULL subquery "
              + selectList.toStringTree());
        }
        SqlASTNode selectItem = (SqlASTNode) selectList
            .getFirstChildWithType(PLSQLParser.SELECT_ITEM);
        SqlASTNode expr = (SqlASTNode) selectItem.getFirstChildWithType(PLSQLParser.EXPR);
        if (expr == null) {
          throw new SqlXlateException("Can't found EXPR in select item");
        }
        putCreateSubQFilter(ctx, (ast.getType() == PLSQLParser.IS_NULL) ? OPType.ISNULL
            : OPType.ISNOTNULL, ast, subq, (SqlASTNode) expr.getChild(0), qInfo, false);
      }
      break;
    }
      // exists
    case PLSQLParser.SQL92_RESERVED_EXISTS: {
      putCreateSubQFilter(ctx, OPType.EXISTS, ast, (SqlASTNode) ast.getChild(0), null, qInfo, true);
      break;
    }
      // relational expressions and all/some/any
    case PLSQLParser.GREATER_THAN_OP:
    case PLSQLParser.GREATER_THAN_OR_EQUALS_OP:
    case PLSQLParser.NOT_EQUAL_OP:
    case PLSQLParser.LESS_THAN_OP:
    case PLSQLParser.LESS_THAN_OR_EQUALS_OP:
    case PLSQLParser.EQUALS_OP: {
      SqlASTNode left = (SqlASTNode) ast.getChild(0);
      SqlASTNode right = (SqlASTNode) ast.getChild(1);
      prepareRelationalOp(ctx, ast, left, right, qInfo);
      break;
    }
    default:
    }
  }


  private void prepareRelationalOp(FBPrepContext ctx, SqlASTNode ast, SqlASTNode left,
      SqlASTNode right, QueryInfo qInfo) throws SqlXlateException {
    if (left.getType() == PLSQLParser.SUBQUERY) {
      // left and right child should not both be subquery at the same time
      if (right.getType() == PLSQLParser.SUBQUERY) {
        throw new SqlXlateException(
            "Do not support subquery on both sides of relational operator");
      }
      putCreateSubQFilter(ctx, OPType.RELATIONAL, ast, left, right, qInfo, true);
    } else if (right.getType() == PLSQLParser.SUBQUERY) {
      putCreateSubQFilter(ctx, OPType.RELATIONAL, ast, right, left, qInfo, false);
    } else if (SqlXlateUtil.isAllOperator(left) || SqlXlateUtil.isSomeAnyOperator(left)) {
      // left and right child should not both be quantitative expressions at the same time
      if (SqlXlateUtil.isAllOperator(right) || SqlXlateUtil.isSomeAnyOperator(right)) {
        throw new SqlXlateException(
            "Do not support subquery on both sides of relational operator");
      }
      putCreateSubQFilter(ctx, SqlXlateUtil.isAllOperator(left) ? OPType.ALL : OPType.SOMEANY, ast,
          (SqlASTNode) left.getChild(0), right, qInfo,
          true);

    } else if (SqlXlateUtil.isAllOperator(right) || SqlXlateUtil.isSomeAnyOperator(right)) {
      putCreateSubQFilter(ctx, SqlXlateUtil.isAllOperator(right) ? OPType.ALL : OPType.SOMEANY,
          ast, (SqlASTNode) right.getChild(0), left, qInfo, false);
    }
  }

  /**
   * Process the subqueries in from clauses in the
   * subqueires inside where clauses
   * 
   * @throws SqlXlateException
   */
  private void processFromInWhereSubTree(SqlASTNode from) throws SqlXlateException {
    // TODO Now we do not support subqueries in from clause in where subqs
    // add support for it later
    if (SqlXlateUtil.hasNodeTypeInTree(from, PLSQLParser.SUBQUERY)) {
      throw new SqlXlateException(
          "Do not support subquries in from clause of subqueries in where filter");
    }
  }

  /**
   * construct a filter block for op
   * 
   * @param op
   * @return
   * @throws SqlXlateException
   */
  private FilterBlockBase createFBForLogicOp(SqlASTNode op) throws SqlXlateException {
    switch (op.getType()) {
    case PLSQLParser.SQL92_RESERVED_AND:
      return new OpANDFilterBlock();
    case PLSQLParser.SQL92_RESERVED_OR:
      return new OpORFilterBlock();
    case PLSQLParser.SQL92_RESERVED_NOT:
      return new OpNOTFilterBlock();
    default:
      SqlXlateUtil.error(op);
    }
    return null;
  }

  /**
   * Create logical NOT op FB and register it in context.
   * 
   * @param ctx
   * @param ast
   * @param qInfo
   * @return
   */
  private FilterBlockBase putCreateNotOp(FBPrepContext ctx, SqlASTNode ast, QueryInfo qInfo) {

    SqlASTNode child = (SqlASTNode) ast.getChild(0);
    FilterBlockBase childFB = ctx.getNodeToFBMap().get(child);
    if (childFB == null) {
      // the current enclosing select query
      SqlASTNode selectKeyThisQ = ctx.getSelectKeyStack().peek();
      // if child is logical op, then we have processed it,
      // and we still fount no FB, we know it's uncorrelated
      boolean isChildUnCorrelated = SqlXlateUtil.isLogicalOp(child) ||
          !isCorrelatedFilter(child, qInfo, selectKeyThisQ);
      if (isChildUnCorrelated) {
        return null;
      } else {
        return putCreateNormalFilterCorrelated(ctx, ast);
      }
    } else {
      FilterBlockBase opFB = null;
      if (childFB.getType() == Type.LOGIC_NOT) {
        // a slight optimization in advance
        // if child is also NOT, this and the child cancel out each other
        opFB = childFB.getOnlyChild();
      } else {
        opFB = new OpNOTFilterBlock();
        opFB.setOnlyChild(childFB);
      }
      ctx.getNodeToFBMap().put(ast, opFB);
      return opFB;
    }
  }

  /**
   * Create logical AND/OR op FB and register it in context.
   * 
   * @param ctx
   * @param ast
   * @param qInfo
   * @param selectKeyThisQ
   * @return
   * @throws SqlXlateException
   */
  private FilterBlockBase putCreateAndOrOP(FBPrepContext ctx, SqlASTNode ast, QueryInfo qInfo)
      throws SqlXlateException {
    // the current enclosing select query
    SqlASTNode selectKeyThisQ = ctx.getSelectKeyStack().peek();

    // get left and right child
    SqlASTNode leftChild = (SqlASTNode) ast.getChild(0);
    SqlASTNode rightChild = (SqlASTNode) ast.getChild(1);
    // get left and right FB
    FilterBlockBase leftFB = ctx.getNodeToFBMap().get(leftChild);
    FilterBlockBase rightFB = ctx.getNodeToFBMap().get(rightChild);

    // create the top op
    FilterBlockBase op = createFBForLogicOp(ast);

    if (leftFB == null && rightFB == null) {

      boolean isleftUnCorrelated = SqlXlateUtil.isLogicalOp(leftChild) ||
          !isCorrelatedFilter(leftChild, qInfo, selectKeyThisQ);
      boolean isRightUnCorrelated = SqlXlateUtil.isLogicalOp(rightChild) ||
          !isCorrelatedFilter(rightChild, qInfo, selectKeyThisQ);
      // if both sides are uncorrelated
      if (isleftUnCorrelated && isRightUnCorrelated) {
        return null;
      }
      // if both sides are correlated
      if (!isleftUnCorrelated && !isRightUnCorrelated) {
        return putCreateNormalFilterCorrelated(ctx, ast);
      }
      // one side is correlated and one is uncorrelated
      SqlASTNode cChild = isleftUnCorrelated ? rightChild : leftChild;
      SqlASTNode ucChild = isleftUnCorrelated ? leftChild : rightChild;
      // make tree
      NormalFilterBlock ucNFB = putCreateNormalFilterUnCorrelated(ctx, ucChild);
      NormalFilterBlock cNFB = putCreateNormalFilterCorrelated(ctx, cChild);
      leftFB = (cChild == leftChild) ? cNFB : ucNFB;
      rightFB = (cChild == rightChild) ? cNFB : ucNFB;
      op.setLeftChild(leftFB);
      op.setRightChild(rightFB);
    } else {
      // if leftFB does not exist, create a normal for it
      if (leftFB == null) {
        leftFB = constructNormalFilter(ctx, leftChild, qInfo, selectKeyThisQ);
      }
      // if rightFB does not exist, create a normal for it
      if (rightFB == null) {
        rightFB = constructNormalFilter(ctx, rightChild, qInfo, selectKeyThisQ);
      }
      op.setLeftChild(leftFB);
      op.setRightChild(rightFB);
    }
    ctx.getNodeToFBMap().put(ast, op);
    LOG.debug("Register node to FB: " + ast.toStringTree() + " -> " + op.toStringTree());
    return op;
  }

  /**
   * Construct normal filter
   * 
   * @return
   * 
   */
  private NormalFilterBlock constructNormalFilter(FBPrepContext ctx, SqlASTNode ast,
      QueryInfo qInfo, SqlASTNode selectKeyThisQ) {
    NormalFilterBlock ret = null;
    if (SqlXlateUtil.isLogicalOp(ast) ||
        !isCorrelatedFilter(ast, qInfo, selectKeyThisQ)) {
      ret = putCreateNormalFilterUnCorrelated(ctx, ast);
    } else {
      ret = putCreateNormalFilterCorrelated(ctx, ast);
    }
    return ret;
  }

  /**
   * Check if this is a Correlated Filter.
   * 
   * @param op
   *          top node of this filter subtree
   * @param qInfo
   *          belonging qInfo
   * @param selectKeyThisQ
   *          select key of this subquery
   * @return
   */
  private boolean isCorrelatedFilter(SqlASTNode op, QueryInfo qInfo, SqlASTNode selectKeyThisQ) {
    LOG.debug("Enclosing Select Key: " + selectKeyThisQ.toStringTree());
    Set<String> thisQTbls = qInfo.getSrcTblAliasForSelectKey(selectKeyThisQ);
    Set<String> referedTbls = new HashSet<String>();
    SqlXlateUtil.getReferredTblAlias(op, referedTbls);
    LOG.debug("Referred Tables : " + referedTbls);
    LOG.debug("Src Tables: " + thisQTbls);
    if (thisQTbls.containsAll(referedTbls)) {
      return false;
    } else {
      return true;
    }
  }


  /**
   * Create correlated normal filter and register it in context
   * 
   * @param ctx
   * @param ast
   * @return
   */
  private NormalFilterBlock putCreateNormalFilterCorrelated(FBPrepContext ctx, SqlASTNode ast) {
    LOG.debug("Creating Correlated Normal Filter " + ((ast != null) ? ast.toStringTree() : "null"));
    NormalFilterBlock normal = new NormalFilterBlock();
    normal.setCorrelatedFilter(ast);
    ctx.getNodeToFBMap().put(ast, normal);
    return normal;
  }

  /**
   * Create uncorrelated normal filter and register it in context.
   * 
   * @param ctx
   * @param ast
   * @return
   */
  private NormalFilterBlock putCreateNormalFilterUnCorrelated(FBPrepContext ctx, SqlASTNode ast) {
    LOG.debug("Creating Uncorrelated Normal Filter "
        + ((ast != null) ? ast.toStringTree() : "null"));
    NormalFilterBlock normal = new NormalFilterBlock();
    normal.setUnCorrelatedFilter(ast);
    ctx.getNodeToFBMap().put(ast, normal);
    return normal;
  }

  /**
   * Create subquery FB and register it in context.
   * 
   * @param ctx
   * @param op
   *          op node of this subquery filter
   * @param subquery
   *          subquery node
   * @param nonSubq
   *          the other side of op
   * @param qInfo
   *          belonging qInfo
   * @param isSubQLeftChild
   *          is subquries left child of op node
   * @return
   * @throws SqlXlateException
   */
  private FilterBlockBase putCreateSubQFilter(FBPrepContext ctx, OPType type, SqlASTNode op,
      SqlASTNode subquery,
      SqlASTNode nonSubq, QueryInfo qInfo, boolean isSubQLeftChild) throws SqlXlateException {

    SubQFilterBlock sf = createSubQFB(ctx, type, op, subquery, nonSubq, qInfo,
        isSubQLeftChild);
    SqlASTNode selectKey = sf.getSelectKeyInSubQ();
    // if child block is normal, then add it into subq normal member instead of child
    FilterBlockBase childBlock = getFBForWhere(ctx, SqlXlateUtil.getWhereInSelectRaw(selectKey),
        qInfo, selectKey);
    if (childBlock != null) {
      if (childBlock.getType() == FilterBlockBase.Type.NORMAL) {
        sf.setNormalFilter((NormalFilterBlock) childBlock);
      } else {
        sf.setOnlyChild(childBlock);
      }
    }
    ctx.getNodeToFBMap().put(op, sf);
    return sf;
  }

  /**
   * create a new subqFB without putting it into map.
   * 
   * @param ctx
   * @param type
   * @param op
   * @param subquery
   * @param nonSubq
   * @param qInfo
   * @param isSubQLeftChild
   * @return
   * @throws SqlXlateException
   */
  private SubQFilterBlock createSubQFB(FBPrepContext ctx, OPType type, SqlASTNode op,
      SqlASTNode subquery,
      SqlASTNode nonSubq, QueryInfo qInfo, boolean isSubQLeftChild) throws SqlXlateException {
    SqlASTNode selectKey = (SqlASTNode) subquery
        .getFirstChildWithType(PLSQLParser.SQL92_RESERVED_SELECT);

    SubQFilterBlock sf = new SubQFilterBlock(type);
    sf.setSubQueryNodeInSubQ(subquery);
    sf.setQueryInfo(qInfo);
    ASTNode alias = SqlXlateUtil.newASTNode(HiveParser.Identifier,
        aliasGen.generateAliasForSubQ());
    qInfo.setSubQAlias(subquery, alias);
    sf.setFromInSubQ(SqlXlateUtil.getFromInSelectRaw(selectKey));
    sf.addSelectListInSubQ(SqlXlateUtil.getSelectListInSelectRaw(selectKey));
    sf.setSelectKeyInSubQ(selectKey);
    sf.setOp(op);
    sf.setIsLeftExprSubQ(isSubQLeftChild);
    sf.setNonSubQExpr(nonSubq);
    return sf;
  }

  /**
   * get the FilterBlock for where subtree
   * 
   * @param ctx
   * @param where
   * @param qInfo
   * @param selectKey
   * @return
   * @throws SqlXlateException
   */
  private FilterBlockBase getFBForWhere(FBPrepContext ctx, SqlASTNode where, QueryInfo qInfo,
      SqlASTNode selectKey)
      throws SqlXlateException {
    if (where == null) {
      return null;
    } else {
      SqlASTNode logicExpr = (SqlASTNode) where.getFirstChildWithType(PLSQLParser.LOGIC_EXPR);
      if (logicExpr == null) {
        throw new SqlXlateException("Can not find LIGIC_EXPR as child of where");
      }

      SqlASTNode op = (SqlASTNode) logicExpr.getChild(0);
      FilterBlockBase fb = ctx.getNodeToFBMap().get(op);
      if (fb != null) {
        return fb;
      } else {
        if (SqlXlateUtil.isLogicalOp(op) ||
            !isCorrelatedFilter(op, qInfo, selectKey)) {
          return putCreateNormalFilterUnCorrelated(ctx, op);
        } else {
          return putCreateNormalFilterCorrelated(ctx, op);
        }
      }
    }
  }

  /**
   * Prepare the insert destination clause
   * 
   * @param src
   * @param qInfo
   * @throws SqlXlateException
   */
  private void prepareInsertDestination(SqlASTNode src, QueryInfo qInfo) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SINGLE_TABLE_MODE:
        SqlASTNode into = (SqlASTNode) child.getFirstChildWithType(PLSQLParser.SQL92_RESERVED_INTO);
        if (into.getChild(1) != null && !into.getChild(1).getText().equals("COLUMNS")) {
          throw new SqlXlateException("Only support insert destination is entire table row");
        }
        qInfo.addInsertDestinationForSubQ(into);
        break;
      case PLSQLParser.MULTI_TABLE_MODE:
        // TODO
      default:
        SqlXlateUtil.error(src);
      }
    }
  }

  /**
   * Prepare the from clause.
   * 
   * @param src
   * @param qInfo
   * @throws SqlXlateException
   */
  private void prepareFrom(SqlASTNode src, QueryInfo qInfo) throws SqlXlateException {
    // set from clause for this query
    qInfo.setFrom(src);
    // get subquery alias in from
    prepareSubQAliases(src, qInfo);
  }

  /**
   * Prepare SubQuery Aliases
   * 
   * @param src
   * @param qInfo
   * @throws SqlXlateException
   */
  private void prepareSubQAliases(SqlASTNode src, QueryInfo qInfo) throws SqlXlateException {
    // prepare subq alias for each qInfo
    if (src.getType() == PLSQLParser.TABLE_REF_ELEMENT) {
      SqlASTNode alias = (SqlASTNode) src.getFirstChildWithType(PLSQLParser.ALIAS);
      SqlASTNode child2 = null;
      SqlASTNode subquery = null;
      if ((child2 = (SqlASTNode) src.getFirstChildWithType(PLSQLParser.TABLE_EXPRESSION)) != null) {
        if ((child2 = (SqlASTNode) child2.getFirstChildWithType(PLSQLParser.SELECT_MODE)) != null) {
          if ((child2 = (SqlASTNode) child2.getFirstChildWithType(PLSQLParser.SELECT_STATEMENT)) != null) {
            if ((child2 = (SqlASTNode) child2.getFirstChildWithType(PLSQLParser.SUBQUERY)) != null) {
              subquery = child2;
            }
          }
        }
      }
      if (subquery != null) {
        ASTNode aliasNode = null;
        if (alias == null) {
          aliasNode = SqlXlateUtil.newASTNode(HiveParser.Identifier, aliasGen
              .generateAliasForSubQ());
        } else {
          aliasNode = genForAlias(alias);
        }
        qInfo.setSubQAlias(subquery, aliasNode);
      }
    }

    for (int i = 0; i < src.getChildCount(); i++) {
      prepareSubQAliases((SqlASTNode) src.getChild(i), qInfo);
    }
  }



  /**
   * Generate Hive AST for SQL AST subtree with root type STATEMENTS
   * 
   * @param src
   *          SQL AST subtree
   * @return Hive AST
   * @throws SqlXlateException
   */
  protected ASTNode genForStatements(SqlASTNode src) throws SqlXlateException {
    try {
      for (int i = 0; i < src.getChildCount(); i++) {
        SqlASTNode child = (SqlASTNode) src.getChild(i);
        switch (child.getType()) {
        case PLSQLParser.SELECT_STATEMENT: {
          // walk select statement
          return genForSelectStatement(child);
        }
        case PLSQLParser.SQL92_RESERVED_INSERT: {
          return genForInsertKey(child);
        }
        default:
          SqlXlateUtil.error(child);
        }
      }
      return null;
    } catch (SqlXlateException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Unknown Exception:" + e);
      e.printStackTrace();
      throw new SqlXlateException(e.toString());
    }
  }

  /**
   * Generate Hive AST for SQL AST subtree with root type
   * SQL92_RESERVED_INSERT
   * 
   * @param src
   *          SQL AST subtree
   * @return Hive AST
   * @throws SqlXlateException
   */
  private ASTNode genForInsertKey(SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SINGLE_TABLE_MODE: {
        return genForSingleTableMode(child);
      }
      case PLSQLParser.MULTI_TABLE_MODE:
        // TODO
      default:
        SqlXlateUtil.error(child);
      }
    }
    return null;
  }

  /**
   * Generate Hive AST for SQL AST subtree with root type
   * SINGLE_TABLE_MODE
   * 
   * @param src
   *          SQL AST subtree
   * @return Hive AST
   * @throws SqlXlateException
   */
  private ASTNode genForSingleTableMode(SqlASTNode src) throws SqlXlateException {
    List<ASTNode> ret = new ArrayList<ASTNode>();
    ASTNode query = null;
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SQL92_RESERVED_INTO:
        if (child.getChild(1) != null && !child.getChild(1).getText().equals("COLUMNS")) {
          throw new SqlXlateException("Only support insert destination is entire table row");
        }
        List<SqlASTNode> dests = new ArrayList<SqlASTNode>();
        dests.add(child);
        genInsertIntoDestination(ret, dests);
        break;
      case PLSQLParser.SELECT_STATEMENT:
        query = genForSelectStatement(child);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    if (query == null) {
      throw new SqlXlateException("No SELECT_STATEMENT found");
    }
    if (ret.size() == 1) {
      ASTNode insert = (ASTNode) query.getFirstChildWithType(HiveParser.TOK_INSERT);
      // destination should always be the first child of insert
      insert.setChild(0, ret.get(0));
    } else if (ret.size() > 1) {
      throw new SqlXlateException("Insert destination is more than one in single mode");
    }
    return query;
  }

  /**
   * Generate Hive AST for SQL AST subtree with root type
   * SELECT_STATEMENT
   * 
   * @param src
   *          SQL AST subtree
   * @return Hive AST
   * @throws SqlXlateException
   */
  private ASTNode genForSelectStatement(SqlASTNode src) throws SqlXlateException {
    ASTNode query = null;
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SUBQUERY: {
        query = genForSubQuery(child);
        break;
      }
      case PLSQLParser.SQL92_RESERVED_ORDER:
        if (query == null) {
          throw new SqlXlateException(
              "Can not find subquery statment but find order by in select statement");
        }
        if (query.getType() == HiveParser.TOK_SUBQUERY) {
          throw new SqlXlateException("Do not support order by used in union");
        }
        attachGenForOrderKey((ASTNode) query.getFirstChildWithType(HiveParser.TOK_INSERT), child);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    return query;
  }

  /**
   * Generate Hive AST for SQL AST subtree with root type
   * SQL92_RESERVED_ORDER and insert as children of dest
   * 
   * @param src
   *          SQL AST subtree
   * @return Hive AST
   * @throws SqlXlateException
   */
  private void attachGenForOrderKey(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.ORDER_BY_ELEMENTS:
        attachGenForOrderByElements(dest, child);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
  }

  /**
   * Generate Hive AST for SQL AST subtree with root type
   * ORDER_BY_ELEMENTS and insert as children of dest
   * 
   * @param src
   *          SQL AST subtree
   * @return Hive AST
   * @throws SqlXlateException
   */
  private void attachGenForOrderByElements(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    ASTNode orderby = SqlXlateUtil.newASTNode(HiveParser.TOK_ORDERBY, "TOK_ORDERBY");
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.ORDER_BY_ELEMENT:
        attachGenForOrderByElement(orderby, child);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    if (SqlXlateUtil.isOrderByPosition(orderby)) {
      analysisStat.setHasOrderByPosition(true);
    }
    SqlXlateUtil.attachChild(dest, orderby);
  }

  /**
   * Generate Hive AST for SQL AST subtree with root type
   * ORDER_BY_ELEMENT and insert as children of dest
   * 
   * @param src
   *          SQL AST subtree
   * @return Hive AST
   * @throws SqlXlateException
   */
  private void attachGenForOrderByElement(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    // col + asc/desc
    ASTNode orderkey = null;
    if (src.getChildCount() == 1) {
      orderkey = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEASC, "TOK_TABSORTCOLNAMEASC");
    } else if (src.getChildCount() == 2) {
      if (src.getChild(1).getType() == PLSQLParser.SQL92_RESERVED_DESC) {
        orderkey = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEDESC,
            "TOK_TABSORTCOLNAMEDESC");
      } else if (src.getChild(1).getType() == PLSQLParser.SQL92_RESERVED_ASC) {
        orderkey = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEASC,
            "TOK_TABSORTCOLNAMEASC");
      } else {
        SqlXlateUtil.error((SqlASTNode) src.getChild(1));
      }
    } else {
      SqlXlateUtil.error(src);
    }
    attachGenForExpr(orderkey, (SqlASTNode) src.getChild(0));
    SqlXlateUtil.attachChild(dest, orderkey);
  }

  /**
   * Generate Hive AST for SUBQUERY subtree
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForSubQuery(SqlASTNode src) throws SqlXlateException {
    boolean distinctUnion = true;
    ASTNode topOp = null;
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SQL92_RESERVED_SELECT:
        topOp = genForSelectKey(child);
        break;
      case PLSQLParser.SQL92_RESERVED_UNION:
        // create union node
        if (child.getChild(0).getType() == PLSQLParser.SQL92_RESERVED_ALL) {
          distinctUnion = false;
        }
        ASTNode newUnion = SqlXlateUtil.newASTNode(HiveParser.TOK_UNION, "TOK_UNION");
        // attach the current topOp as child of this union
        SqlXlateUtil.attachChild(newUnion, topOp);
        SqlXlateUtil.attachChild(newUnion, genForUnionKey(child));
        topOp = newUnion;
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }

    assert (topOp != null);

    // if not root query
    if (!src.getQueryInfo().isQInfoTreeRoot()) {
      // attach a new subquery node
      // and attach the topOp
      ASTNode subquery = SqlXlateUtil.newASTNode(HiveParser.TOK_SUBQUERY, "TOK_SUBQUERY");
      SqlXlateUtil.attachChild(subquery, topOp);
      ASTNode subqAlias = src.getQueryInfo().getSubQAlias(src);
      SqlXlateUtil.attachChild(subquery, subqAlias);
      return subquery;
    } else {
      // if is root query and top not is not union
      // return the topOp directly
      if (topOp.getType() == HiveParser.TOK_QUERY) {
        return topOp;
      } else {
        // if is topOp is UNION/INTERSECT, etc.
        // add select * from above the entire subquery
        return genTopLevelSubQ(topOp, distinctUnion);
      }
    }
  }

  /**
   * Add extra select * on top of top level subqueries.
   * 
   * @param topOp
   *          the original query AST generated
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genTopLevelSubQ(ASTNode topOp, boolean isDistinct) throws SqlXlateException {
    analysisStat.setHasTopLevelSubQ(true);
    // gen insert into tmp file
    ASTNode query = SqlXlateUtil.newASTNode(HiveParser.TOK_QUERY, "TOK_QUERY");
    ASTNode insert = genInsertTmpFileDestination();
    // gen select *
    ASTNode select = null;
    if (isDistinct) {
      select = SqlXlateUtil.newASTNode(HiveParser.TOK_SELECTDI, "TOK_SELECTDI");
    } else {
      select = SqlXlateUtil.newASTNode(HiveParser.TOK_SELECT, "TOK_SELECT");
    }
    SqlXlateUtil.attachChild(select, genSelectStarExpr());
    SqlXlateUtil.attachChild(insert, select);
    ASTNode from = SqlXlateUtil.newASTNode(HiveParser.TOK_FROM, "TOK_FROM");
    ASTNode subquery = SqlXlateUtil.newASTNode(HiveParser.TOK_SUBQUERY, "TOK_SUBQUERY");
    ASTNode alias = SqlXlateUtil.newASTNode(HiveParser.Identifier, aliasGen.generateAliasForSubQ());
    SqlXlateUtil.attachChild(subquery, topOp);
    SqlXlateUtil.attachChild(subquery, alias);
    SqlXlateUtil.attachChild(from, subquery);
    SqlXlateUtil.attachChild(query, from);
    SqlXlateUtil.attachChild(query, insert);
    return query;
  }

  /**
   * Generate select * subtree
   * 
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genSelectStarExpr() throws SqlXlateException {
    ASTNode selexpr = SqlXlateUtil.newASTNode(HiveParser.TOK_SELEXPR, "TOK_SELEXPR");
    ASTNode allcolref = SqlXlateUtil.newASTNode(HiveParser.TOK_ALLCOLREF, "TOK_ALLCOLREF");
    SqlXlateUtil.attachChild(selexpr, allcolref);
    return selexpr;
  }

  /**
   * Generate select * subtree
   * 
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genSelectCountStarExpr() throws SqlXlateException {
    // NOTE: generate a fake subtree here for select count * in
    // aggregation mode
    // fix it later in fixColumns
    ASTNode selexpr = SqlXlateUtil.newASTNode(HiveParser.TOK_SELEXPR, "TOK_SELEXPR");
    ASTNode funcStar = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTIONDI, "TOK_FUNCIONDI");
    ASTNode count = SqlXlateUtil.newASTNode(HiveParser.Identifier, "count");
    SqlXlateUtil.attachChild(selexpr, funcStar);
    SqlXlateUtil.attachChild(funcStar, count);
    return selexpr;
  }

  /**
   * Generate Hive AST of insert into temporary file destination.
   * 
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genInsertTmpFileDestination() throws SqlXlateException {
    ASTNode insert = SqlXlateUtil.newASTNode(HiveParser.TOK_INSERT, "TOK_INSERT");
    ASTNode destination = SqlXlateUtil.newASTNode(HiveParser.TOK_DESTINATION, "TOK_DESTINATION");
    SqlXlateUtil.attachChild(insert, destination);
    ASTNode dir = SqlXlateUtil.newASTNode(HiveParser.TOK_DIR, "TOK_DIR");
    SqlXlateUtil.attachChild(destination, dir);
    ASTNode tmpfile = SqlXlateUtil.newASTNode(HiveParser.TOK_TMP_FILE, "TOK_TMP_FILE");
    SqlXlateUtil.attachChild(dir, tmpfile);
    return insert;
  }

  /**
   * Generate Hive AST of insert into defined destination .
   * 
   * @param dests
   * @return
   * @throws SqlXlateException
   */
  private void genInsertIntoDestination(List<ASTNode> ret, List<SqlASTNode> dests)
      throws SqlXlateException {
    for (SqlASTNode dest : dests) {
      ASTNode insertinto = SqlXlateUtil.newASTNode(HiveParser.TOK_INSERT_INTO, "TOK_INSERT_INTO");
      ASTNode tab = SqlXlateUtil.newASTNode(HiveParser.TOK_TAB, "TOK_TAB");
      SqlXlateUtil.attachChild(insertinto, tab);
      // TODO a hack here to remove TOK_TABREF
      // change table expression processing to handle this.
      ASTNode tabref = genForTableRef((SqlASTNode) dest.getChild(0));
      SqlXlateUtil.attachChild(tab, (ASTNode) tabref.getChild(0));
      ret.add(insertinto);
    }
  }

  /**
   * Generate Hive AST for SQL92_RESERVED_UNION subtree
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForUnionKey(SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SQL92_RESERVED_ALL:
        // we only support union all, others will fall into error
        break;
      case PLSQLParser.SQL92_RESERVED_SELECT:
        return genForSelectKey(child);
      case PLSQLParser.SUBQUERY:
        return genForSubQuery(child);
      default:
        SqlXlateUtil.error(child);
      }
    }
    return null;
  }


  /**
   * Generate Hive AST for SQL_92_RESERVED_SELECT subtree
   * 
   * @param selectKey
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForSelectKey(SqlASTNode selectKey) throws SqlXlateException {
    ASTNode rootOp = genASTFromFilterBlockTree(
        selectKey.getQueryInfo().getFilterBlockTreeRoot().
            getOnlyChild());
    return rootOp;
  }

  /**
   * Generate AST from filter block tree
   * 
   * @param fb
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genASTFromFilterBlockTree(FilterBlockBase fb) throws SqlXlateException {
    // walk the tree recursively
    if (fb == null) {
      return null;
    }
    if (fb.getType() == FilterBlockBase.Type.LOGIC_AND) {
      // TODO use intersect to handle and after intersect semantics is available
      // ASTNode op = SqlXlateUtil.newASTNode(HiveParser.TOK_INTERSECT, "TOK_INTERSECT");
      // SqlXlateUtil.attachChild(op, genASTFromFilterBlockTree(fb.getLeftChild()));
      // SqlXlateUtil.attachChild(op, genASTFromFilterBlockTree(fb.getRightChild()));
      // return op;
      throw new SqlXlateException("Do not support filters contain subquries and connected by and");
    } else if (fb.getType() == FilterBlockBase.Type.LOGIC_OR) {
      // TODO use UNION ALL to handle or at present, later we will change
      // non all union
      ASTNode op = SqlXlateUtil.newASTNode(HiveParser.TOK_UNION, "TOK_UNION");
      SqlXlateUtil.attachChild(op, genASTFromFilterBlockTree(fb.getLeftChild()));
      SqlXlateUtil.attachChild(op, genASTFromFilterBlockTree(fb.getRightChild()));
      return op;
    } else if (fb.getType() == FilterBlockBase.Type.SUBQ) {
      ASTNode query = genASTFromSubQFBTree((SubQFilterBlock) fb);
      return query;
    } else {
      // should not come here
      assert (false);
      return null;
    }
  }

  /**
   * Generate AST subtree from chaining SubQFB
   * 
   * @param topFB
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genASTFromSubQFBTree(SubQFilterBlock topFB) throws SqlXlateException {
    // generate subtree recursively in a DFS manner
    if (topFB == null) {
      return null;
    }

    FilterBlockBase child = topFB.getOnlyChild();
    ASTNode childTree = genASTFromFilterBlockTree(child);

    if (topFB.getAggrPhase() == AggrPhase.AGGREGATION_PHASE) {
      ASTNode query = SqlXlateUtil.newASTNode(HiveParser.TOK_QUERY, "TOK_QUERY");
      ASTNode from = SqlXlateUtil.newASTNode(HiveParser.TOK_FROM, "TOK_FROM");
      ASTNode subquery = SqlXlateUtil.newASTNode(HiveParser.TOK_SUBQUERY, "TOK_SUBQUERY");
      ASTNode alias = SqlXlateUtil.newASTNode(HiveParser.Identifier, aliasGen
          .generateAliasForSubQ());
      SqlXlateUtil.attachChild(subquery, childTree);
      SqlXlateUtil.attachChild(subquery, alias);
      SqlXlateUtil.attachChild(from, subquery);
      SqlXlateUtil.attachChild(query, from);
      SqlASTNode selectKey = topFB.getSelectKeyInSubQ();
      ASTNode insert = null;
      // just gen tmp file insertion for each query
      // later we'll replace it with real into destination
      // if (topFB.isTopSubQBlock()) {
      // insert = genInsertDestination(selectKey);
      // } else {
      // insert = genInsertTmpFileDestination();
      // }
      insert = genInsertTmpFileDestination();
      SqlXlateUtil.attachChild(query, insert);
      // gen - - select list
      ASTNode select = SqlXlateUtil.newASTNode(HiveParser.TOK_SELECT, "TOK_SELECT");
      SqlXlateUtil.attachChild(select, genSelectCountStarExpr());
      SqlXlateUtil.attachChild(insert, select);
      return query;
    } else {

      // gen query node
      ASTNode query = SqlXlateUtil.newASTNode(HiveParser.TOK_QUERY, "TOK_QUERY");
      // gen - from
      ASTNode from = SqlXlateUtil.newASTNode(HiveParser.TOK_FROM, "TOK_FROM");
      // support for multi table selection
      ASTNode tableRef = genForMultiTableRef((SqlASTNode) topFB.getFromInSubQ());

      ASTNode crossjoinFilter = null;
      if (childTree == null) {
        SqlXlateUtil.attachChild(from, tableRef);
      } else {
        crossjoinFilter = mergeChildFBTree(topFB, from, tableRef, childTree);
      }
      // add col subq into from and merge filters into filterReturn
      crossjoinFilter = mergeColFBTree(topFB, from, crossjoinFilter);

      SqlXlateUtil.attachChild(query, from);
      // gen - insert
      ASTNode insert = null;
      SqlASTNode selectKey = topFB.getSelectKeyInSubQ();
      // just gen tmp file insertion for each query
      // later we'll replace it with real into destination
      // if (topFB.isTopSubQBlock() && topFB.getAggrPhase() != AggrPhase.SELECTALL_PHASE) {
      // insert = genInsertDestination(selectKey);
      // } else {
      // insert = genInsertTmpFileDestination();
      // }
      insert = genInsertTmpFileDestination();
      SqlXlateUtil.attachChild(query, insert);
      // gen - - select list
      ASTNode select = genForSelectList(selectKey);
      adjustSelectItem(topFB, select);
      SqlXlateUtil.attachChild(insert, select);
      // gen - - where
      ASTNode where = genForWhereKeyUnCorrelatedPart(topFB.getNormalFilter());
      where = mergeFilterInWhere(where,
          SqlXlateUtil.newASTNode(HiveParser.TOK_OP_AND, "AND"),
          crossjoinFilter);
      if (where != null) {
        SqlXlateUtil.attachChild(insert, where);
      }
      // gen - - group by
      attachGenForGroupKey(insert, selectKey);
      addOrderByClauseForQuantityOp(topFB, insert);
      return query;
    }
  }

  /**
   * Merge generated subtrees from Column SubQ into original tree
   * 
   * @param fb
   * @param from
   * @param filterReturn
   * @return
   * @throws SqlXlateException
   */
  private ASTNode mergeColFBTree(SubQFilterBlock fb, ASTNode from, ASTNode filterReturn)
      throws SqlXlateException {
    // TODO Auto-generated method stub
    // handle Col SubQ list
    if (fb.getAllColSubQFB() != null) {
      ASTNode oldJoin = (ASTNode) from.getChild(0);
      from.deleteChild(0);
      LOG.debug("Found Column SubQ Fb ");
      for (FilterBlockBase entry : fb.getAllColSubQFB()) {
        SubQFilterBlock colfb = (SubQFilterBlock) entry;
        LOG.debug("processing " + colfb.getSelectListInSubQ());
        // generate subquery subtree for col subq
        ASTNode childTree = genASTFromFilterBlockTree(colfb);
        ASTNode colsubq = SqlXlateUtil.newASTNode(HiveParser.TOK_SUBQUERY, "TOK_SUBQUERY");
        ASTNode talias = colfb.getSubQueryAlias();
        SqlXlateUtil.attachChild(colsubq, childTree);
        SqlXlateUtil.attachChild(colsubq, talias);
        LOG.debug("generated subquery subtree for " + colfb.getSubQueryAlias().getText() + " : "
            + colsubq.toStringTree());

        // add the original select item into select list as column_0 in subq
        List<Column> colToAddToSelect = new ArrayList<Column>();
        List<ASTNode> parentCols = new ArrayList<ASTNode>();
        Column slectItem = null;
        if ((slectItem = getOnlySelectColumnInSubQ(colfb)) == null) {
          throw new SqlXlateException("Invalid Semantics " + colfb.getOp().toStringTree());
        }
        colToAddToSelect.add(slectItem);
        // add other correlated items into select list in subq
        ASTNode cjFilter = genCorrelatedFilterExprForCrossJoin(colfb,
            colToAddToSelect, parentCols);
        ASTNode filter = null;
        ASTNode newJoin = null;
        // TODO handle no correlated expr case
        if (cjFilter == null) {
          throw new SqlXlateException("No correlated expr found");
        }
        if (cjFilter.getType() == HiveParser.EQUAL) {
          // if cjfilter is equation, we translate it to left outer join
          newJoin = mergeChildTreeLeftOuterJoin(colfb, oldJoin, colsubq, cjFilter);
        } else {
          ASTNode topOp = null;
          // skip the selectItem and starting from the correlated items
          for (int j = 1; j < colToAddToSelect.size(); j++) {
            ASTNode isnull = genNullCheckFilter(SqlXlateUtil.makeASTforColumn(
                new Column(colfb.getSubQueryAlias().getText(),
                    getColAliasBasedOnIndex(j))), true);
            if (j == 1) {
              topOp = isnull;
            } else {
              topOp = SqlXlateUtil.mergeFilters(
                  SqlXlateUtil.newASTNode(HiveParser.TOK_OP_OR, "OR"),
                  topOp, isnull);
            }
          }
          newJoin = mergeChildTreeCrossJoin(colfb, oldJoin, colsubq);
          filter = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_OR, "OR")
              , cjFilter, topOp);
          filterReturn = SqlXlateUtil.mergeFilters(
              SqlXlateUtil.newASTNode(HiveParser.TOK_OP_AND, "AND"),
              filter,
              filterReturn);
        }
        ReplaceSelectInSubQuery(colsubq, colToAddToSelect);
        oldJoin = newJoin;
      }
      SqlXlateUtil.attachChild(from, oldJoin);
    }
    return filterReturn;
  }

  /**
   * Generate AST with multi-table selection support.
   * 
   * @param fromInSubQ
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForMultiTableRef(SqlASTNode fromInSubQ) throws SqlXlateException {
    ASTNode lastTopOp = null;
    for (int i = 0; i < fromInSubQ.getChildCount(); i++) {
      SqlASTNode tableRef = (SqlASTNode) fromInSubQ.getChild(i);
      if (tableRef.getType() == PLSQLParser.TABLE_REF) {
        if (lastTopOp == null) {
          lastTopOp = genForTableRef(tableRef);
        } else {
          ASTNode newTopOp = SqlXlateUtil.newASTNode(HiveParser.TOK_CROSSJOIN, "TOK_CROSSJOIN");
          SqlXlateUtil.attachChild(newTopOp, lastTopOp);
          SqlXlateUtil.attachChild(newTopOp, genForTableRef(tableRef));
          lastTopOp = newTopOp;
        }
      }
    }
    if (fromInSubQ.getChildCount() > 1) {
      analysisStat.setHasMultiTabSelect(true);
    }
    return lastTopOp;
  }

  /**
   * Adjust select item if needed
   * 
   * @param topFB
   * @param select
   * @throws SqlXlateException
   */
  private void adjustSelectItem(SubQFilterBlock topFB, ASTNode select) throws SqlXlateException {
    // if top select no need to adjust select item
    // handle aggregation case
    if (topFB.getAggrPhase() == AggrPhase.SELECTALL_PHASE) {
      replaceSelectItemWithSelectAll(topFB, select);
    }
    fillColSubQInSelect(topFB, select);
    if (topFB.isTopSubQBlock()) {
      return;
    }
    insertColAliasForSelectItem(topFB, select);
  }

  /**
   * Fill the column subqueries with the correct alias
   * 
   * @param topFB
   * @param select
   */
  private void fillColSubQInSelect(SubQFilterBlock topFB, ASTNode select) {
    List<FilterBlockBase> colSubqFBs = topFB.getAllColSubQFB();
    int index = 0;
    for (int i = 0; i < select.getChildCount(); i++) {
      ASTNode selexpr = (ASTNode) select.getChild(i);
      if (selexpr.getType() == HiveParser.TOK_SELEXPR) {
        if (selexpr.getChild(0).getType() == HiveParser.TOK_SUBQUERY) {
          selexpr.deleteChild(0);
          String tabAlias = ((SubQFilterBlock) colSubqFBs.get(index)).getSubQueryAlias().getText();
          ASTNode dot = SqlXlateUtil.newASTNode(HiveParser.DOT, ".");
          ASTNode tabOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL,
              "TOK_TABLE_OR_COL");
          SqlXlateUtil.attachChild(dot, tabOrCol);
          SqlXlateUtil.attachChild(tabOrCol, SqlXlateUtil.newASTNode(HiveParser.Identifier,
              tabAlias));
          SqlXlateUtil.attachChild(dot, SqlXlateUtil.newASTNode(HiveParser.Identifier, this
              .getColAliasBasedOnIndex(0)));
          SqlXlateUtil.attachChild(selexpr, dot);
          index++;
        }
      }
    }
  }

  /**
   * Replace Select Item with Select Star
   * 
   * @param topFB
   * @param select
   * @throws SqlXlateException
   */
  private void replaceSelectItemWithSelectAll(SubQFilterBlock topFB, ASTNode select)
      throws SqlXlateException {
    // remove all child of select
    while (select.getChildCount() > 0) {
      select.deleteChild(0);
    }
    // add select *
    SqlXlateUtil.attachChild(select, genSelectStarExpr());
  }


  /**
   * Insert column alias for the select item
   * 
   * @param fb
   * @param select
   */
  private void insertColAliasForSelectItem(SubQFilterBlock fb, ASTNode select) {
    // only add col alias for non-top select
    ASTNode selectExpr = (ASTNode) select.getFirstChildWithType(HiveParser.TOK_SELEXPR);
    SqlXlateUtil.attachChild(selectExpr,
        SqlXlateUtil.newASTNode(HiveParser.Identifier, getColAliasBasedOnIndex(0)));
  }

  /**
   * Add order by clause to support quantity operator (i.e. some/any/all)
   * 
   * @param topFB
   * @param insert
   */
  private void addOrderByClauseForQuantityOp(SubQFilterBlock topFB, ASTNode insert) {
    if (topFB.getOpType() != OPType.ALL && topFB.getOpType() != OPType.SOMEANY) {
      return;
    }
    SqlASTNode op = topFB.getOp();
    if (op.getType() == PLSQLParser.EQUALS_OP ||
        op.getType() == PLSQLParser.NOT_EQUAL_OP) {
      return;
    }
    // new orderby element to be added
    SqlASTNode qOp = topFB.isLeftExprSubQ() ? (SqlASTNode) op.getChild(0) : (SqlASTNode) op
        .getChild(1);
    ASTNode orderDescription = null;
    if (op.getType() == PLSQLParser.GREATER_THAN_OP ||
        op.getType() == PLSQLParser.GREATER_THAN_OR_EQUALS_OP) {
      if (qOp.getType() == PLSQLParser.SQL92_RESERVED_ALL) {
        if (!topFB.isLeftExprSubQ()) {
          // ... > ALL(SubQ)
          orderDescription = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEDESC,
              "TOK_TABSORTCOLNAMEDESC");
        } else {
          // ALL(SubQ) > ...
          orderDescription = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEASC,
              "TOK_TABSORTCOLNAMEASC");
        }
      } else {
        // some and any are interchangable
        if (!topFB.isLeftExprSubQ()) {
          // ... > SOME(SubQ)
          orderDescription = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEASC,
              "TOK_TABSORTCOLNAMEASC");
        } else {
          // SOME(SubQ) > ...
          orderDescription = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEDESC,
              "TOK_TABSORTCOLNAMEDESC");
        }

      }
    } else if (op.getType() == PLSQLParser.LESS_THAN_OP ||
        op.getType() == PLSQLParser.LESS_THAN_OR_EQUALS_OP) {
      if (qOp.getType() == PLSQLParser.SQL92_RESERVED_ALL) {
        if (!topFB.isLeftExprSubQ()) {
          // ... < ALL(SubQ)
          orderDescription = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEASC,
              "TOK_TABSORTCOLNAMEASC");
        } else {
          // ALL(SUBQ) < ...
          orderDescription = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEDESC,
              "TOK_TABSORTCOLNAMEDESC");
        }
      } else {
        // some and any are interchangable
        // ... < SOME(SUBQ)
        if (!topFB.isLeftExprSubQ()) {
          orderDescription = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEDESC,
              "TOK_TABSORTCOLNAMEDESC");
        } else {
          // SOME(SUBQ) < ...
          orderDescription = SqlXlateUtil.newASTNode(HiveParser.TOK_TABSORTCOLNAMEASC,
              "TOK_TABSORTCOLNAMEASC");
        }
      }
    }

    ASTNode tabOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL, "TOK_TABLE_OR_COL");
    ASTNode colId = SqlXlateUtil.newASTNode(HiveParser.Identifier, getColAliasBasedOnIndex(0));
    SqlXlateUtil.attachChild(orderDescription, tabOrCol);
    SqlXlateUtil.attachChild(tabOrCol, colId);
    ASTNode newOrderElem = orderDescription;

    ASTNode orderby = (ASTNode) insert.getFirstChildWithType(HiveParser.TOK_ORDERBY);

    if (orderby == null) {
      // create new orderby node and orderby element
      orderby = SqlXlateUtil.newASTNode(HiveParser.TOK_ORDERBY, "TOK_ORDERBY");
      SqlXlateUtil.attachChild(orderby, newOrderElem);
      SqlXlateUtil.attachChild(insert, orderby);
    } else {
      // add the order into the first orderby element
      LOG.debug("Found existing order by element, adding the new ordery by as the first priority");
      ArrayList<Node> children = orderby.getChildren();
      // delete all current children
      while (orderby.getChildCount() > 0) {
        orderby.deleteChild(0);
      }
      SqlXlateUtil.attachChild(orderby, newOrderElem);
      for (Node n : children) {
        SqlXlateUtil.attachChild(orderby, (ASTNode) n);
      }
    }

    // add limit 1
    ASTNode limit = SqlXlateUtil.newASTNode(HiveParser.TOK_LIMIT, "TOK_LIMIT");
    ASTNode one = SqlXlateUtil.newASTNode(HiveParser.Number, "1");
    SqlXlateUtil.attachChild(limit, one);
    SqlXlateUtil.attachChild(insert, limit);
  }

  /**
   * Merge Child Tree with self's table using various joins.
   * 
   * @param fb
   * @param from
   * @param thisTable
   * @param childTree
   * @return
   * @throws SqlXlateException
   */
  private ASTNode mergeChildFBTree(SubQFilterBlock fb, ASTNode from, ASTNode thisTable,
      ASTNode childTree) throws SqlXlateException {
    // handle child SubQ Filter Blocks
    SubQFilterBlock childQ = fb.getChildSubQ();
    // add subquery node on top of childTree
    ASTNode subquery = SqlXlateUtil.newASTNode(HiveParser.TOK_SUBQUERY, "TOK_SUBQUERY");
    ASTNode talias = childQ.getSubQueryAlias();
    SqlXlateUtil.attachChild(subquery, childTree);
    SqlXlateUtil.attachChild(subquery, talias);
    ASTNode filterReturn = null;
    ASTNode join = null;
    if (childQ.getOpType() == OPType.IN) {
      if (SqlXlateUtil.isLiteral(childQ.getNonSubQExpr())) {
        // duplicates might exist
        List<Column> colToAddToSelect = new ArrayList<Column>();
        List<ASTNode> parentCols = new ArrayList<ASTNode>();
        ASTNode cjFilter = genCorrelatedFilterExprForCrossJoin(childQ, colToAddToSelect, parentCols);
        Column slectItem = null;
        if ((slectItem = getOnlySelectColumnInSubQ(childQ)) == null) {
          throw new SqlXlateException("Invalid Semantics " + childQ.getOp().toStringTree());
        }
        // add the current select item into array
        String currentSelectItemAlias = getColAliasBasedOnIndex(colToAddToSelect.size());
        colToAddToSelect.add(slectItem);
        ASTNode lInFilter = genLiteralInFilter(childQ, new Column("", currentSelectItemAlias));
        filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_AND,
            "AND"),
            cjFilter, lInFilter);
        join = mergeChildTreeCrossJoin(childQ, thisTable, subquery);
        ReplaceSelectInSubQuery(subquery, colToAddToSelect);
      } else {
        // In is translate to left semi join
        join = mergeChildTreeLeftSemiJoin(fb, thisTable, subquery);
      }
    } else if (childQ.getOpType() == OPType.NOTIN) {
      if (SqlXlateUtil.isLiteral(childQ.getNonSubQExpr())) {
        List<Column> colToAddToSelect = new ArrayList<Column>();
        List<ASTNode> parentCols = new ArrayList<ASTNode>();
        ASTNode crFilter = genCorrelatedFilterExprForCrossJoin(childQ, colToAddToSelect, parentCols);
        Column slectItem = null;
        if ((slectItem = getOnlySelectColumnInSubQ(childQ)) == null) {
          throw new SqlXlateException("Invalid Semantics " + childQ.getOp().toStringTree());
        }
        // add the current select item into array
        String currentSelectItemAlias = getColAliasBasedOnIndex(colToAddToSelect.size());
        colToAddToSelect.add(slectItem);
        if (crFilter.getType() == HiveParser.EQUAL) {
          // if correlated filter is equal, translate to left outer join
          // put crFilter as join condition
          join = mergeChildTreeLeftOuterJoin(childQ, thisTable, subquery, crFilter);
          filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_OR,
              "OR"),
              genNullCheckFilter(
                  SqlXlateUtil.makeASTforColumn("", getColAliasBasedOnIndex(0)), true),
              genLiteralInFilter(childQ,
                  new Column("", currentSelectItemAlias)));

        } else {
          // if not equal filter, translate to cross join
          ASTNode lInFilter = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(
              HiveParser.TOK_OP_OR, "OR"),
              genLiteralInFilter(childQ, new Column("", currentSelectItemAlias)),
              genNullCheckFilter(SqlXlateUtil.makeASTforColumn(slectItem), true));
          filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_AND,
              "AND"),
              crFilter, lInFilter);
          join = mergeChildTreeCrossJoin(childQ, thisTable, subquery);
        }
        ReplaceSelectInSubQuery(subquery, colToAddToSelect);
      } else {
        // not in is translate to cross join
        join = mergeChildCrossJoinCollectRows(childQ, thisTable, subquery);
        String arrayItemAlias = getColAliasBasedOnIndex(0);
        filterReturn = genFilterExpressionNotInArray(
            genForCascatedElement(childQ.getNonSubQExpr()), arrayItemAlias);
      }
    } else if (childQ.getOpType() == OPType.EXISTS) {
      List<Column> colToAddToSelect = new ArrayList<Column>();
      List<ASTNode> parentCols = new ArrayList<ASTNode>();
      filterReturn = genCorrelatedFilterExprForCrossJoin(childQ, colToAddToSelect, parentCols);
      join = mergeChildTreeCrossJoin(childQ, thisTable, subquery);
      ReplaceSelectInSubQuery(subquery, colToAddToSelect);

    } else if (childQ.getOpType() == OPType.NOTEXISTS) {
      // TODO we may
      // translate certain cases to NOT IN
      filterReturn = translateNOTExistsToNotIn(childQ, from, thisTable, subquery);
    } else if (childQ.getOpType() == OPType.RELATIONAL) {
      join = mergeChildCrossJoinCollectRows(childQ, thisTable, subquery);
      String arrayItemAlias = getColAliasBasedOnIndex(0);
      // simple relational single row has only got array size = 1 case
      ASTNode nonSubQOperand = null;
      if (childQ.getNonSubQExpr().getType() == PLSQLParser.CASCATED_ELEMENT) {
        nonSubQOperand = genForCascatedElement(childQ.getNonSubQExpr());
      } else if (OpFuncFactory.isOpOrFunc(childQ.getNonSubQExpr())) {
        nonSubQOperand = genForOpFunc(childQ.getNonSubQExpr());
      } else {
        throw new SqlXlateException("Unsupported Relational SubQ Grammar at "
            + childQ.getNonSubQExpr().toStringTree());
      }
      filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil
          .newASTNode(HiveParser.TOK_OP_AND, "AND"),
          genNullCheckFilter(nonSubQOperand, false),
          SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_AND, "AND"),
              genFilterArraySizeIsOne(arrayItemAlias),
              genFilterExpressionForSingleRowRelational(childQ)));

    } else if (childQ.getOpType() == OPType.ISNULL ||
        childQ.getOpType() == OPType.ISNOTNULL) {
      List<Column> colToAddToSelect = new ArrayList<Column>();
      List<ASTNode> parentCols = new ArrayList<ASTNode>();
      ASTNode crFilter = genCorrelatedFilterExprForCrossJoin(childQ, colToAddToSelect, parentCols);
      Column slectItem = null;
      if ((slectItem = getOnlySelectColumnInSubQ(childQ)) == null) {
        throw new SqlXlateException("Invalid Semantics " + childQ.getOp().toStringTree());
      }
      // add the current select item into array
      String currentSelectItemAlias = getColAliasBasedOnIndex(colToAddToSelect.size());
      colToAddToSelect.add(slectItem);
      if (childQ.getOpType() == OPType.ISNULL &&
          crFilter.getType() == HiveParser.EQUAL) {
        // if correlated filter is equal, translate to left outer join
        // put crFilter as join condition
        join = mergeChildTreeLeftOuterJoin(childQ, thisTable, subquery, crFilter);
        filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil
            .newASTNode(HiveParser.TOK_OP_OR, "OR"),
            genNullCheckFilter(
                SqlXlateUtil.makeASTforColumn("", getColAliasBasedOnIndex(0)), true),
            genNullCheckFilter(
                SqlXlateUtil.makeASTforColumn("", currentSelectItemAlias), true));
      } else {
        ASTNode nullfilter = childQ.getOpType() == OPType.ISNULL ?
            genNullCheckFilter(SqlXlateUtil.makeASTforColumn("", currentSelectItemAlias), true) :
            genNullCheckFilter(SqlXlateUtil.makeASTforColumn("", currentSelectItemAlias), false);
        filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_AND,
            "AND"),
            crFilter,
            nullfilter);
        join = mergeChildTreeCrossJoin(childQ, thisTable, subquery);
      }
      ReplaceSelectInSubQuery(subquery, colToAddToSelect);
    } else if (childQ.getOpType() == OPType.ALL ||
        childQ.getOpType() == OPType.SOMEANY) {
      SqlASTNode op = childQ.getOp();
      if (op.getType() == PLSQLParser.GREATER_THAN_OP ||
          op.getType() == PLSQLParser.GREATER_THAN_OR_EQUALS_OP ||
          op.getType() == PLSQLParser.LESS_THAN_OP ||
          op.getType() == PLSQLParser.LESS_THAN_OR_EQUALS_OP) {
        join = mergeChildTreeCrossJoin(childQ, thisTable, subquery);
        ASTNode cjfilter = genFilterExprForCrossJoin(childQ);
        ASTNode isnotnull = genNullCheckFilter(
            SqlXlateUtil.makeASTforColumn(
                new Column(childQ.getSubQueryAlias().getText(), getColAliasBasedOnIndex(0))),
                false);
        filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_AND,
            "AND"),
            cjfilter, isnotnull);

      } else if (op.getType() == PLSQLParser.NOT_EQUAL_OP) {
        if (childQ.getOpType() == OPType.ALL) {
          // equivalent to NOT IN but add null check
          // <> all case
          // if array > 1 then check if item in the array
          // if array = 1 then should check then item condition and check item
          // not null
          // if array = 0 then it is always true
          join = mergeChildCrossJoinCollectRows(childQ, thisTable, subquery);
          String arrayItemAlias = getColAliasBasedOnIndex(0);
          // if array size > 1
          ASTNode moreItemInArrayCase = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(
              HiveParser.TOK_OP_AND, "AND"),
              genFilterArraySizeLargerThanOne(arrayItemAlias),
              genFilterExpressionNotInArray(genForCascatedElement(childQ
                  .getNonSubQExpr()), arrayItemAlias));
          // if array size = 0
          ASTNode zeroItemInArrayCase = genFilterArraySizeIsZero(arrayItemAlias);
          // if array size = 1
          ASTNode oneItemInArrayCase = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(
              HiveParser.TOK_OP_AND, "AND"),
              genNullCheckFilter(genForCascatedElement(childQ.getNonSubQExpr()), false),
              SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_AND, "AND"),
                  genFilterArraySizeIsOne(arrayItemAlias),
                  genFilterExpressionForSingleRowRelational(childQ)));
          // connect all three cases with or
          filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_OR,
              "OR"),
              SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_OR, "OR"),
                  zeroItemInArrayCase,
                  moreItemInArrayCase),
              oneItemInArrayCase);
        } else {
          // some any also add null check
          // <> some any
          // if array size = 1 check item condition and check not null
          // if array size > 1 always true
          // if array size = 0 always false
          join = mergeChildCrossJoinCollectRows(childQ, thisTable, subquery);

          String arrayItemAlias = getColAliasBasedOnIndex(0);
          // if array size > 1
          ASTNode moreItemInArrayCase = genFilterArraySizeLargerThanOne(arrayItemAlias);
          // if array size = 1 and is not null and
          ASTNode oneItemInArrayCase = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(
              HiveParser.TOK_OP_AND, "AND"),
              genNullCheckFilter(genForCascatedElement(childQ.getNonSubQExpr()), false),
              SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_AND, "AND"),
                  genFilterArraySizeIsOne(arrayItemAlias),
                  genFilterExpressionForSingleRowRelational(childQ)));
          filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_OR,
              "OR"),
              oneItemInArrayCase,
              moreItemInArrayCase);
        }
      } else if (op.getType() == PLSQLParser.EQUALS_OP) {
        if (childQ.getOpType() == OPType.SOMEANY) {
          // equivalent to IN
          join = mergeChildTreeLeftSemiJoin(fb, thisTable, subquery);
        } else {
          // = all
          join = mergeChildCrossJoinCollectRows(childQ, thisTable, subquery);
          // if array size = 0 always true
          ASTNode zeroItemInArrayCase = genFilterArraySizeIsZero(getColAliasBasedOnIndex(0));
          // if array size = 1 check condition
          ASTNode oneItemInArrayCase = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(
              HiveParser.TOK_OP_AND, "AND"),
              genFilterArraySizeIsOne(getColAliasBasedOnIndex(0)),
              genFilterExpressionForSingleRowRelational(childQ));
          // if array size > 1 always false
          filterReturn = SqlXlateUtil.mergeFilters(SqlXlateUtil.newASTNode(HiveParser.TOK_OP_OR,
              "OR"),
              zeroItemInArrayCase,
              oneItemInArrayCase);
        }
      } else {
        assert (false);
      }
    }

    SqlXlateUtil.attachChild(from, join);
    LOG.debug("Composed Filter Return :" +
        ((filterReturn != null) ? filterReturn.toStringTree() : "None"));

    return filterReturn;
  }


  private ASTNode mergeChildTreeLeftOuterJoin(SubQFilterBlock childQ, ASTNode thisTable,
      ASTNode subquery, ASTNode crFilter) {
    ASTNode leftOuterJoin = SqlXlateUtil.newASTNode(HiveParser.TOK_LEFTOUTERJOIN,
        "TOK_LEFTOUTERJOIN");
    SqlXlateUtil.attachChild(leftOuterJoin, thisTable);
    SqlXlateUtil.attachChild(leftOuterJoin, subquery);
    SqlXlateUtil.attachChild(leftOuterJoin, crFilter);
    return leftOuterJoin;
  }

  /**
   * Get the only Select Column In SubQuery
   * 
   * @param colfb
   * @return
   */
  private Column getOnlySelectColumnInSubQ(SubQFilterBlock colfb) {
    if (colfb.getSelectListInSubQ().size() != 1) {
      return null;
    }
    SqlASTNode selectItem = colfb.getSelectListInSubQ().get(0);
    SqlASTNode expr = (SqlASTNode) selectItem.getFirstChildWithType(PLSQLParser.EXPR);
    if (expr != null) {
      SqlASTNode cascatedElem = (SqlASTNode) expr
          .getFirstChildWithType(PLSQLParser.CASCATED_ELEMENT);
      if (cascatedElem != null) {
        SqlASTNode anyElem = (SqlASTNode) cascatedElem
            .getFirstChildWithType(PLSQLParser.ANY_ELEMENT);
        if (anyElem != null) {
          if (anyElem.getChildCount() == 1) {
            // column
            return new Column("", anyElem.getChild(0).getText());
          } else if (anyElem.getChildCount() == 2) {
            // table.column
            return new Column(anyElem.getChild(0).getText(),
                anyElem.getChild(1).getText());
          }
        }
      }
    }
    return null;
  }

  /**
   * Translate Not exists to Not in if possible
   * 
   * @param childQ
   * @param from
   * @param thisTable
   * @param subquery
   * @return
   * @throws SqlXlateException
   */
  private ASTNode translateNOTExistsToNotIn(SubQFilterBlock childQ, ASTNode from,
      ASTNode thisTable, ASTNode subquery) throws SqlXlateException {
    // TODO Auto-generated method stub
    // not in is translate to cross join
    List<Column> colToAddToSelect = new ArrayList<Column>();
    List<ASTNode> parentCols = new ArrayList<ASTNode>();
    ASTNode op = genCorrelatedFilterExprForCrossJoin(childQ, colToAddToSelect, parentCols);
    if (op.getType() != HiveParser.EQUAL) {
      throw new SqlXlateException("Can not translate NOT Exists subquery with " +
          "non equi correlated filters");
    }
    ReplaceSelectInSubQuery(subquery, colToAddToSelect);
    ASTNode join = mergeChildCrossJoinCollectRows(childQ, thisTable, subquery);
    // get parent column ASTNode
    // TODO only check one parent ref
    LOG.debug("Parent Cols : " + parentCols);
    ASTNode filterReturn = genFilterExpressionNotInArray(parentCols.get(0),
        getColAliasBasedOnIndex(0));
    SqlXlateUtil.attachChild(from, join);
    return filterReturn;
  }

  /**
   * Generate Literal In Filter, e.g. 40 IN SUBQ
   * 
   * @param childQ
   * @param cList
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genLiteralInFilter(SubQFilterBlock childQ, Column selectItem)
      throws SqlXlateException {
    // construct in condition
    ASTNode op = null;
    if (childQ.getOpType() == OPType.IN) {
      op = SqlXlateUtil.newASTNode(HiveParser.EQUAL, "=");
    } else if (childQ.getOpType() == OPType.NOTIN) {
      op = SqlXlateUtil.newASTNode(HiveParser.NOTEQUAL, "!=");
    } else {
      assert (false);
    }
    ASTNode literal = genForLiteral(childQ.getNonSubQExpr());
    SqlXlateUtil.attachChild(op, literal);
    SqlXlateUtil.attachChild(op, SqlXlateUtil.makeASTforColumn(selectItem));
    return op;
  }

  /**
   * Generate Filter For null checking of input column
   * 
   * @param col
   *          column to be checked if is null/not null
   * @param isnullcheck
   *          true if it is IS NULL, otherwise it is IS NOT NULL
   * @return
   */
  private ASTNode genNullCheckFilter(ASTNode col, boolean isnullcheck) {
    ASTNode function = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTION, "TOK_FUNCTION");

    ASTNode nullcheck = isnullcheck ?
        SqlXlateUtil.newASTNode(HiveParser.TOK_ISNULL, "TOK_ISNULL") :
        SqlXlateUtil.newASTNode(HiveParser.TOK_ISNOTNULL, "TOK_ISNOTNULL");

    SqlXlateUtil.attachChild(function, nullcheck);
    SqlXlateUtil.attachChild(function, col);

    return function;
  }

  /**
   * Generate AST for Correlated Filter expression
   * 
   * @param childQ
   * @param thisQColAlias
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genCorrelatedFilterExprForCrossJoin(SubQFilterBlock childQ,
      List<Column> thisQColAlias, List<ASTNode> parentCol) throws SqlXlateException {
    // TODO now we assume correlated filter only refer
    // to parent query tables not ancestor query tables
    QueryInfo qInfo = childQ.getQueryInfo();
    CorrelatedFilter filter = childQ.getNormalFilter().getCorrelatedFilter();
    // LOG.debug("Uncorrelated Part of filter : "+childQ.getNormalFilter().getUnCorrelatedFilter().getRawFilterExpr().toStringTree());
    LOG.debug("Correlated Filter Found : "
        + (filter != null ? filter.getRawFilterExpr().toStringTree() : "null"));
    if (filter == null) {
      return null;
    }
    Set<String> srcTbls = qInfo.getSrcTblAliasForSelectKey(childQ.getSelectKeyInSubQ());
    SqlASTNode op = filter.getRawFilterExpr();
    assert (op != null);
    ASTNode result = genForOpFunc(op);
    LOG.debug("Correlated Filter Expression : " + result.toStringTree());
    collectReferredCols(result, childQ.getSubQueryAlias().getText(), srcTbls, thisQColAlias,
        parentCol);
    return result;
  }


  /**
   * Get columns that refer to table in this SubQuery
   * return a column string map with alias.
   * Add all col aliases into aliasMap
   * 
   * @throws SqlXlateException
   */
  private void collectReferredCols(ASTNode node, String subqAlias, Set<String> srcTbls,
      List<Column> colAlias, List<ASTNode> parentCol) throws SqlXlateException {
    assert (node != null);
    boolean skipRecursion = false;
    // TODO add more sanity check
    ASTNode tid = null;
    ASTNode cid = null;
    if (node.getType() == HiveParser.TOK_TABLE_OR_COL &&
        node.getChildCount() == 2) {
      skipRecursion = true;
      // e.g. (TOK_TABLE_OR_COL tname colname)
      // the 2nd child is col name
      tid = (ASTNode) node.getChild(0);
      cid = (ASTNode) node.getChild(1);
      LOG.debug("Found col reference: " + tid.getText() + "." + cid.getText());
    } else if (node.getType() == HiveParser.TOK_TABLE_OR_COL &&
        node.getChildCount() == 1) {
      skipRecursion = true;
      // just column with no table
      // add table information into this
      String cidStr = node.getChild(0).getText();
      node.getToken().setType(HiveParser.DOT);
      node.getToken().setText(".");
      node.deleteChild(0);
      ASTNode tabOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL, "TOK_TABLE_OR_COL");
      tid = SqlXlateUtil.newASTNode(HiveParser.Identifier, "");
      SqlXlateUtil.attachChild(tabOrCol, tid);
      cid = SqlXlateUtil.newASTNode(HiveParser.Identifier, cidStr);
      SqlXlateUtil.attachChild(node, tabOrCol);
      SqlXlateUtil.attachChild(node, cid);
      LOG.debug("Found col reference: " + tid.getText() + "." + cid.getText());
    } else if (node.getType() == HiveParser.DOT) {
      skipRecursion = true;
      // e.g. (. (TOK_TABLE_OR_COL tname) colname)
      tid = (ASTNode) node.getChild(0).getChild(0);
      cid = (ASTNode) node.getChild(1);
      LOG.debug("Found col reference: " + tid.getText() + "." + cid.getText());
    }

    if (tid != null && cid != null) {
      // find all the col references that refers to tables in this query
      if (tid.getText().isEmpty() || srcTbls.contains(tid.getText())) {
        Column c = new Column(tid.getText(), cid.getText());
        // replace the table and column name
        // change table alias to subqAlias
        tid.getToken().setText(subqAlias);
        // String newColAlias = aliasGen.generateAliasForCol().getText();
        // use the index of colalias array to build alias
        // e.g. column_0
        cid.getToken().setText(getColAliasBasedOnIndex(colAlias.size()));
        colAlias.add(c);
        LOG.debug("Collected Column refering table in this query : " + c);
        LOG.debug("Correlated Filter with Alias replaced : " + node.toStringTree());
      } else {
        parentCol.add(node);
      }
    }

    if (!skipRecursion) {
      for (int i = 0; i < node.getChildCount(); i++) {
        collectReferredCols((ASTNode) node.getChild(i), subqAlias, srcTbls, colAlias, parentCol);
      }
    }
  }

  /**
   * Replace select item with referred columns in filter.
   * 
   * @param subquery
   * @param referedColAlias
   * @throws SqlXlateException
   */
  private void ReplaceSelectInSubQuery(ASTNode subquery, List<Column> referedColAlias)
      throws SqlXlateException {

    LOG.debug("Referred Column Alias Map : " + referedColAlias);
    ASTNode query = (ASTNode) subquery.getFirstChildWithType(HiveParser.TOK_QUERY);
    ASTNode insert = (ASTNode) query.getFirstChildWithType(HiveParser.TOK_INSERT);
    ASTNode select = (ASTNode) insert.getFirstChildWithType(HiveParser.TOK_SELECT);
    // List<ASTNode> selectExprList = new ArrayList<ASTNode>();
    while (select.getChildCount() > 0) {
      ASTNode selexpr = (ASTNode) select.getChild(0);
      if (selexpr.getChild(0).getType() == HiveParser.TOK_TABLE_OR_COL) {
        // TODO
        // Now we just remove all the current select items
        // in future we should add these columns and add
        // null check in filter
      } else if (selexpr.getChild(0).getType() == HiveParser.TOK_ALLCOLREF) {
        // we assume * does not coexists with other single columns
      } else {
        throw new SqlXlateException(
            "Do not support items other than simple table col ref or star in select expressions in exists subquery");
      }
      select.deleteChild(0);
    }
    // add all the referred columns in correlated expressions
    addColListToSelect(select, referedColAlias, true);
    return;
  }

  /**
   * Add column list into select expression
   * 
   * @param select
   * @param cList
   * @param addAlias
   */
  private void addColListToSelect(ASTNode select, List<Column> cList, boolean addAlias) {
    int currentIndex = select.getChildCount();
    for (int i = 0; i < cList.size(); i++) {
      Column c = cList.get(i);
      ASTNode selexpr = SqlXlateUtil.newASTNode(HiveParser.TOK_SELEXPR, "TOK_SELEXPR");
      ASTNode item = SqlXlateUtil.makeASTforColumn(c);
      SqlXlateUtil.attachChild(selexpr, item);
      SqlXlateUtil.attachChild(select, selexpr);
      SqlXlateUtil.attachChild(selexpr,
              SqlXlateUtil.newASTNode(HiveParser.Identifier,
                  getColAliasBasedOnIndex(currentIndex + i)));
    }
  }

  /**
   * Generate filter expression for the semantic of
   * not in the array
   * 
   * @param childFB
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genFilterExpressionNotInArray(ASTNode inCol, String arrayColAlias)
      throws SqlXlateException {
    // (not (TOK_FUNCTION array_contains
    // (TOK_TABLE_OR_COL collect_set_col)
    // (TOK_TABLE_OR_CO L inCol) )
    ASTNode not = SqlXlateUtil.newASTNode(HiveParser.TOK_OP_NOT, "not");
    ASTNode function = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTION, "TOK_FUNCTION");
    ASTNode array_contains = SqlXlateUtil.newASTNode(HiveParser.Identifier, "array_contains");
    ASTNode col = SqlXlateUtil.makeASTforColumn("", arrayColAlias);
    SqlXlateUtil.attachChild(not, function);
    SqlXlateUtil.attachChild(function, array_contains);
    SqlXlateUtil.attachChild(function, col);
    SqlXlateUtil.attachChild(function, inCol);
    return not;
  }

  /**
   * Generate filter to check if the size of specified column (
   * of type array) is 1
   * 
   * @param colAliasStr
   *          the column alias of array column
   * @return
   */
  private ASTNode genFilterArraySizeIsOne(String colAliasStr) {
    // add size condition (size(col) = 1)
    ASTNode function = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTION, "TOK_FUNCTION");
    ASTNode size = SqlXlateUtil.newASTNode(HiveParser.Identifier, "size");
    ASTNode col = SqlXlateUtil.makeASTforColumn("", colAliasStr);
    SqlXlateUtil.attachChild(function, size);
    SqlXlateUtil.attachChild(function, col);

    ASTNode op = SqlXlateUtil.newASTNode(HiveParser.EQUAL, "=");

    SqlXlateUtil.attachChild(op, function);
    SqlXlateUtil.attachChild(op, SqlXlateUtil.newASTNode(HiveParser.SmallintLiteral, "1"));

    return op;
  }

  /**
   * Generate filter to check if the size of specified column (
   * of type array) is large than 1
   * 
   * @param colAliasStr
   *          the column alias of array column
   * @return
   */
  private ASTNode genFilterArraySizeLargerThanOne(String colAliasStr) {
    // add size condition (size(col) > 1)
    ASTNode function = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTION, "TOK_FUNCTION");
    ASTNode size = SqlXlateUtil.newASTNode(HiveParser.Identifier, "size");
    ASTNode col = SqlXlateUtil.makeASTforColumn("", colAliasStr);
    SqlXlateUtil.attachChild(function, size);
    SqlXlateUtil.attachChild(function, col);

    ASTNode op = SqlXlateUtil.newASTNode(HiveParser.EQUAL, ">");

    SqlXlateUtil.attachChild(op, function);
    SqlXlateUtil.attachChild(op, SqlXlateUtil.newASTNode(HiveParser.SmallintLiteral, "1"));

    return op;
  }

  /**
   * Generate filter to check if the size of specified column (
   * of type array) is 0
   * 
   * @param colAliasStr
   *          the column alias of array column
   * @return
   */
  private ASTNode genFilterArraySizeIsZero(String colAliasStr) {
    // add size condition (size(col) = 0)
    ASTNode function = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTION, "TOK_FUNCTION");
    ASTNode size = SqlXlateUtil.newASTNode(HiveParser.Identifier, "size");
    ASTNode col = SqlXlateUtil.makeASTforColumn("", colAliasStr);
    SqlXlateUtil.attachChild(function, size);
    SqlXlateUtil.attachChild(function, col);

    ASTNode op = SqlXlateUtil.newASTNode(HiveParser.EQUAL, "=");

    SqlXlateUtil.attachChild(op, function);
    SqlXlateUtil.attachChild(op, SqlXlateUtil.newASTNode(HiveParser.SmallintLiteral, "0"));

    return op;
  }

  /**
   * Generate filter expression to check single row relation op
   * 
   * @param childFB
   * @return
   * @throws SqlXlateException
   */
  public ASTNode genFilterExpressionForSingleRowRelational(SubQFilterBlock childFB)
      throws SqlXlateException {
    // create the condition
    ASTNode ret = null;
    ASTNode src = null;
    SqlASTNode nsExpr = childFB.getNonSubQExpr();
    if (OpFuncFactory.isOpOrFunc(nsExpr)) {
      src = genForOpFunc(nsExpr);
    } else if (nsExpr.getType() == PLSQLParser.CASCATED_ELEMENT) {
      src = genForCascatedElement(nsExpr);
    } else {
      throw new SqlXlateException("Unsupported Non SubQ Expr at : " + nsExpr.toStringTree());
    }

    ASTNode op = opfuncFactory.create(childFB.getOp());
    ret = op;

    if (childFB.getOp().getType() == PLSQLParser.SQL92_RESERVED_NOT) {
      ASTNode childOp = opfuncFactory.create((SqlASTNode) childFB.getOp().getChild(0));
      SqlXlateUtil.attachChild(op, childOp);
      op = childOp;
    }
    assert (!(op.getChildCount() == 1
        && op.getType() == HiveParser.KW_NOT && op.getChild(0).getType() == HiveParser.KW_LIKE));

    ASTNode branketleft = SqlXlateUtil.newASTNode(HiveParser.LSQUARE, "[");
    ASTNode col = SqlXlateUtil.makeASTforColumn("", getColAliasBasedOnIndex(0));
    SqlXlateUtil.attachChild(branketleft, col);
    SqlXlateUtil.attachChild(branketleft, SqlXlateUtil.newASTNode(HiveParser.SmallintLiteral, "0"));

    boolean srcAtLeftHand = !childFB.isLeftExprSubQ();
    if (srcAtLeftHand) {
      SqlXlateUtil.attachChild(op, src);
      SqlXlateUtil.attachChild(op, branketleft);
    } else {
      SqlXlateUtil.attachChild(op, branketleft);
      SqlXlateUtil.attachChild(op, src);
    }
    return ret;
  }



  /**
   * Generate filters for general cross join
   * 
   * @param childFB
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genFilterExprForCrossJoin(SubQFilterBlock childFB) throws SqlXlateException {
    SqlASTNode srcExpr = childFB.getNonSubQExpr();
    SqlASTNode op = childFB.getOp();
    String aliasTbl = childFB.getSubQueryAlias().getText();
    String aliasCol = getColAliasBasedOnIndex(0);
    boolean srcAtLeftHand = !childFB.isLeftExprSubQ();

    // is there any case srcExpr is not cascated element?
    ASTNode src = null;
    if (srcExpr.getType() == PLSQLParser.CASCATED_ELEMENT) {
      src = genForCascatedElement(srcExpr);
    } else if (OpFuncFactory.isOpOrFunc(srcExpr)) {
      src = genForOpFunc(srcExpr);
    } else {
      src = genForLiteral(srcExpr);
    }

    ASTNode opNode = opfuncFactory.create(op);
    // op should never be "NOT LIKE"
    assert (!(opNode.getChildCount() == 1
        && opNode.getType() == HiveParser.KW_NOT && opNode.getChild(0).getType() == HiveParser.KW_LIKE));

    ASTNode dot = SqlXlateUtil.makeASTforColumn(aliasTbl, aliasCol);

    if (srcAtLeftHand) {
      SqlXlateUtil.attachChild(opNode, src);
      SqlXlateUtil.attachChild(opNode, dot);
    } else {
      SqlXlateUtil.attachChild(opNode, dot);
      SqlXlateUtil.attachChild(opNode, src);
    }
    return opNode;
  }

  /**
   * Merge child tree with self table using cross join and add row
   * collection clauses
   * 
   * @param fb
   * @param thisTable
   * @param childSubqTree
   * @return
   * @throws SqlXlateException
   */
  private ASTNode mergeChildCrossJoinCollectRows(SubQFilterBlock childFB, ASTNode thisTable,
      ASTNode childSubqTree) throws SqlXlateException {
    ASTNode crossjoin = SqlXlateUtil.newASTNode(HiveParser.TOK_CROSSJOIN, "TOK_CROSSJOIN");
    SqlXlateUtil.attachChild(crossjoin, thisTable);
    SqlXlateUtil.attachChild(crossjoin, genRowCollectQuery(childFB, childSubqTree));
    return crossjoin;
  }

  /**
   * Generate row collection clauses
   * 
   * @param childFB
   * @param childSubqTree
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genRowCollectQuery(SubQFilterBlock childFB, ASTNode childSubqTree)
      throws SqlXlateException {
    ASTNode subquery = SqlXlateUtil.newASTNode(HiveParser.TOK_SUBQUERY, "TOK_SUBQUERY");
    // create query subtree
    ASTNode query = SqlXlateUtil.newASTNode(HiveParser.TOK_QUERY, "TOK_QUERY");
    // //generate from
    ASTNode from = SqlXlateUtil.newASTNode(HiveParser.TOK_FROM, "TOK_FROM");
    SqlXlateUtil.attachChild(from, childSubqTree);
    SqlXlateUtil.attachChild(query, from);
    // //generate insert
    ASTNode insert = genInsertTmpFileDestination();
    SqlXlateUtil.attachChild(query, insert);
    // gen - - select list
    ASTNode select = SqlXlateUtil.newASTNode(HiveParser.TOK_SELECT, "TOK_SELECT");
    // collet_set(col1)
    ASTNode selectExpr = SqlXlateUtil.newASTNode(HiveParser.TOK_SELEXPR, "TOK_SELEXPR");
    ASTNode function = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTION, "TOK_FUNCTION");
    ASTNode funcName = SqlXlateUtil.newASTNode(HiveParser.Identifier, "collect_set");

    ASTNode dot = SqlXlateUtil.makeASTforColumn(childFB.getSubQueryAlias().getText(),
        getColAliasBasedOnIndex(0));
    SqlXlateUtil.attachChild(select, selectExpr);
    SqlXlateUtil.attachChild(selectExpr, function);
    SqlXlateUtil.attachChild(function, funcName);
    SqlXlateUtil.attachChild(function, dot);
    // gen new alias column_0 for subquery select item
    SqlXlateUtil.attachChild(selectExpr,
        SqlXlateUtil.newASTNode(HiveParser.Identifier, getColAliasBasedOnIndex(0)));
    SqlXlateUtil.attachChild(insert, select);

    SqlXlateUtil.attachChild(subquery, query);
    // create new subquery alias and reset Child SubQ block's alias
    ASTNode newTblAlias = SqlXlateUtil.newASTNode(HiveParser.Identifier, aliasGen
        .generateAliasForSubQ());
    childFB.setSubQueryAlias(newTblAlias);
    SqlXlateUtil.attachChild(subquery, newTblAlias);
    return subquery;
  }

  /**
   * Merge child tree with self table using left semi join.
   * 
   * @param fb
   * @param thisTable
   * @param childSubqTree
   * @return
   * @throws SqlXlateException
   */
  private ASTNode mergeChildTreeLeftSemiJoin(SubQFilterBlock fb, ASTNode thisTable,
      ASTNode childSubqTree) throws SqlXlateException {
    // create top node
    ASTNode lsjoin = SqlXlateUtil.newASTNode(HiveParser.TOK_LEFTSEMIJOIN, "TOK_LEFTSEMIJOIN");
    // attach this table
    SqlXlateUtil.attachChild(lsjoin, thisTable);
    SqlXlateUtil.attachChild(lsjoin, childSubqTree);
    // attach join condition
    ASTNode op = genLSJoinConditionWithAlias(fb, thisTable);
    SqlXlateUtil.attachChild(lsjoin, op);
    return lsjoin;
  }

  /**
   * Merge child tree with self's table using plain cross join
   * 
   * @param fb
   * @param thisTable
   * @param childSubqTree
   * @return
   */
  private ASTNode mergeChildTreeCrossJoin(SubQFilterBlock childQ, ASTNode thisTable,
      ASTNode childSubqTree) {
    ASTNode crossjoin = SqlXlateUtil.newASTNode(HiveParser.TOK_CROSSJOIN, "TOK_CROSSJOIN");
    SqlXlateUtil.attachChild(crossjoin, thisTable);
    SqlXlateUtil.attachChild(crossjoin, childSubqTree);
    return crossjoin;
  }


  /**
   * Generate Hive AST for SELECT_LIST sub tree
   * 
   * @param selectKey
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForSelectList(SqlASTNode selectKey) throws SqlXlateException {
    boolean isDistinct = false;
    ASTNode select = null;
    for (int i = 0; i < selectKey.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) selectKey.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SQL92_RESERVED_FROM:
      case PLSQLParser.SQL92_RESERVED_WHERE:
      case PLSQLParser.SQL92_RESERVED_GROUP:
        // skip
        break;
      case PLSQLParser.SQL92_RESERVED_DISTINCT:
        isDistinct = true;
        break;
      case PLSQLParser.SQL92_RESERVED_ALL:
        isDistinct = false;
        break;
      case PLSQLParser.SELECT_LIST: {
        if (isDistinct) {
          select = SqlXlateUtil.newASTNode(HiveParser.TOK_SELECTDI, "TOK_SELECTDI");
        } else {
          select = SqlXlateUtil.newASTNode(HiveParser.TOK_SELECT, "TOK_SELECT");
        }
        attachGenForSelectList(select, child);
        break;
      }
      case PLSQLParser.ASTERISK: {
        if (isDistinct) {
          select = SqlXlateUtil.newASTNode(HiveParser.TOK_SELECTDI, "TOK_SELECTDI");
        } else {
          select = SqlXlateUtil.newASTNode(HiveParser.TOK_SELECT, "TOK_SELECT");
        }
        ASTNode selexpr = SqlXlateUtil.newASTNode(HiveParser.TOK_SELEXPR, "TOK_SELEXPR");
        ASTNode allcolref = SqlXlateUtil.newASTNode(HiveParser.TOK_ALLCOLREF, "TOK_ALLCOLREF");
        SqlXlateUtil.attachChild(select, selexpr);
        SqlXlateUtil.attachChild(selexpr, allcolref);
        break;
      }
      default:
        SqlXlateUtil.error(child);
      }
    }
    return select;
  }

  /**
   * Generate AST for uncorreclated filter block
   * 
   * @param filter
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForUCFilter(UnCorrelatedFilter filter)
      throws SqlXlateException {
    if (filter == null) {
      return null;
    }
    SqlASTNode op = filter.getRawFilterExpr();
    LOG.debug("Generating UnCorrelated Filter : " + op.toStringTree());
    if (OpFuncFactory.isOpOrFunc(op)) {
      return genForOpFunc(op);
    } else {
      return genForLiteral(op);
    }
  }

  /**
   * Merge extra filters into where subtree
   * 
   * @param where
   * @param newOp
   * @param newFilter
   * @param oldAsLeftHandExpr
   * @return
   */
  private ASTNode mergeFilterInWhere(ASTNode where, ASTNode newOp, ASTNode newFilter) {
    if (newFilter == null || newOp == null) {
      return where;
    }
    if (where == null) {
      where = SqlXlateUtil.newASTNode(HiveParser.TOK_WHERE, "TOK_WHERE");
      SqlXlateUtil.attachChild(where, newFilter);
    } else {
      ASTNode oldOp = (ASTNode) where.getChild(0);
      where.deleteChild(0);
      SqlXlateUtil.attachChild(where, newOp);
      SqlXlateUtil.mergeFilters(newOp, oldOp, newFilter);
    }
    return where;
  }

  /**
   * Generate Join conditions for Left semi join
   * 
   * @param fb
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genLSJoinConditionWithAlias(SubQFilterBlock fb, ASTNode thisTable)
      throws SqlXlateException {
    SubQFilterBlock childQ = fb.getChildSubQ();
    // gen equal op
    ASTNode equal = SqlXlateUtil.newASTNode(HiveParser.EQUAL, "=");
    // gen left ast
    String thisTableStr = SqlXlateUtil.getTblAliasNameFromTabRef(thisTable);
    LOG.debug("The Table Ref : " + thisTableStr);
    ASTNode leftcol = genForCascatedElement(childQ.getNonSubQExpr(), thisTableStr);
    SqlXlateUtil.attachChild(equal, leftcol);
    // gen right ast
    ASTNode rightCol = SqlXlateUtil.makeASTforColumn(childQ.getSubQueryAlias().getText(),
        getColAliasBasedOnIndex(0));
    SqlXlateUtil.attachChild(equal, rightCol);
    return equal;
  }

  /**
   * Generate Hive AST for Group key and attach to dest
   * 
   * @param dest
   * @param selectKey
   * @throws SqlXlateException
   */
  private void attachGenForGroupKey(ASTNode dest, SqlASTNode selectKey) throws SqlXlateException {
    SqlASTNode groupKey = SqlXlateUtil.getGroupKeyInSelect(selectKey);
    if (groupKey == null) {
      return;
    }
    ASTNode groupby = SqlXlateUtil.newASTNode(HiveParser.TOK_GROUPBY, "TOK_GROUPBY");
    SqlXlateUtil.attachChild(dest, groupby);
    for (int i = 0; i < groupKey.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) groupKey.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.GROUP_BY_ELEMENT:
        attachGenForGroupByElement(groupby, child);
        break;
      case PLSQLParser.SQL92_RESERVED_HAVING:
        SqlXlateUtil.attachChild(dest, genForHavingKey(child));
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
  }

  /**
   * Generate Hive AST for having subtree
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForHavingKey(SqlASTNode src) throws SqlXlateException {
    ASTNode having = SqlXlateUtil.newASTNode(HiveParser.TOK_HAVING, "TOK_HAVING");
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.LOGIC_EXPR:
        attachGenForLogicExpr(having, child);
        break;
      case PLSQLParser.EXPR:
        attachGenForExpr(having, child);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    return having;
  }

  /**
   * Generate Hive AST for GROUP_BY_ELEMENT subtree.
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private void attachGenForGroupByElement(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.EXPR:
        attachGenForExpr(dest, child);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
  }

  /**
   * Generate Hive AST for the uncorrelated filters in where key
   * 
   * @param fb
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForWhereKeyUnCorrelatedPart(NormalFilterBlock fb) throws SqlXlateException {
    if (fb == null) {
      return null;
    }
    ASTNode op = genForUCFilter(fb.getUnCorrelatedFilter());
    if (op == null) {
      return null;
    }
    ASTNode where = SqlXlateUtil.newASTNode(HiveParser.TOK_WHERE, "TOK_WHERE");
    SqlXlateUtil.attachChild(where, op);
    return where;
  }

  /**
   * Generate Hive AST for LOGIC_EXPR subtree and attach result nodes
   * to dest node.
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForLogicExpr(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SQL92_RESERVED_IN:
        break;
      default:
        try {
          if (OpFuncFactory.isOpOrFunc(child)) {
            SqlXlateUtil.attachChild(dest, genForOpFunc(child));
          } else {
            // if not match any of above, then treat it as literal.
            // if can't be walked, throw error then.
            SqlXlateUtil.attachChild(dest, genForLiteral(child));
          }
        } catch (SqlXlateException e) {
          throw e;
        }
      }
    }
  }



  /**
   * Generate Hive AST for Operator or Function
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForOpFunc(SqlASTNode src) throws SqlXlateException {
    ASTNode ret = null;
    ASTNode op = opfuncFactory.create(src);
    // /special handling for not like case
    if (op.getChildCount() == 1
        && op.getType() == HiveParser.KW_NOT
        && op.getChild(0).getType() == HiveParser.KW_LIKE) {
      ASTNode like = (ASTNode) op.getChild(0);
      ret = op;
      op = like;
    } else {
      ret = op;
    }
    // create logic operator
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.CASCATED_ELEMENT:
        SqlXlateUtil.attachChild(op, genForCascatedElement(child));
        break;
      case PLSQLParser.STANDARD_FUNCTION:
        attachGenForStandardFunction(op, child);
        break;
      case PLSQLParser.EXPR:
        attachGenForExpr(op, child);
        break;
      case PLSQLParser.EXPR_LIST:
        attachGenForExprList(op, child);
        break;
      case PLSQLParser.SQL92_RESERVED_WHEN:
        attachGenForWhenElse(op, child);
        break;
      case PLSQLParser.SQL92_RESERVED_ELSE:
        attachGenForWhenElse(op, child);
        break;
      default:
        try {
          if (OpFuncFactory.isOpOrFunc(child)) {
            SqlXlateUtil.attachChild(op, genForOpFunc(child));
          } else {
            // if not match any of above, then treat it as literal.
            // if can't be walked, throw error then.
            SqlXlateUtil.attachChild(op, genForLiteral(child));
          }
        } catch (SqlXlateException e) {
          throw e;
        }
      }
    }
    return ret;
  }


  /**
   * Generate Hive AST for when/else(then) expression in CASE WHEN function
   * 
   * @param dest
   *          - case TOK_FUNCTION
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForWhenElse(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      if (dest.getChildCount() == 0 && child.getType() == PLSQLParser.LOGIC_EXPR) {
        SqlXlateUtil.attachChild(dest, SqlXlateUtil.newASTNode(HiveParser.KW_WHEN, "WHEN"));
      }
      if (child.getType() == PLSQLParser.LOGIC_EXPR) {
        attachGenForLogicExpr(dest, child);
      }
      if (child.getType() == PLSQLParser.EXPR) {
        attachGenForExpr(dest, child);
      }
    }
  }

  /**
   * Generate Hive AST for EXPR_LIST node and attach to dest.
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForExprList(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      attachGenForExpr(dest, child);
    }
  }

  /**
   * Generate Hive AST for TABLE_REF subtree
   * 
   * @param tableRef
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForTableRef(SqlASTNode tableRef) throws SqlXlateException {
    ASTNode topOp = null;
    // SqlASTNode tabRef = null;
    for (int i = 0; i < tableRef.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) tableRef.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.TABLE_REF_ELEMENT:
        topOp = genForTableRefElement(child);
        break;
      case PLSQLParser.TABLE_EXPRESSION:
        return genForTableExpression(child);
      case PLSQLParser.JOIN_DEF:
        topOp = genForJoinKey(child, topOp);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    return topOp;
  }

  /**
   * Generate Hive AST for Join key
   * 
   * @param src
   * @param firstChild
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForJoinKey(SqlASTNode src, ASTNode firstChild) throws SqlXlateException {
    ASTNode newjoin = null;
    SqlASTNode joinType = (SqlASTNode) src.getChild(0);
    // left semi join is not standard SQL not supported in direct syntax
    if (joinType.getType() == PLSQLParser.LEFT_VK) {
      newjoin = SqlXlateUtil.newASTNode(HiveParser.TOK_LEFTOUTERJOIN, "TOK_LEFTOUTERJOIN");
    } else if (joinType.getType() == PLSQLParser.RIGHT_VK) {
      newjoin = SqlXlateUtil.newASTNode(HiveParser.TOK_RIGHTOUTERJOIN, "TOK_RIGHTOUTERJOIN");
    } else if (joinType.getType() == PLSQLParser.FULL_VK) {
      newjoin = SqlXlateUtil.newASTNode(HiveParser.TOK_FULLOUTERJOIN, "TOK_FULLOUTERJOIN");
    } else {
      // inner join
      newjoin = SqlXlateUtil.newASTNode(HiveParser.TOK_JOIN, "TOK_JOIN");
    }

    if (firstChild == null) {
      // should already have at least one table ref when join exists
      throw new SqlXlateException("only one child found in join");
    }
    SqlXlateUtil.attachChild(newjoin, firstChild);
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.FULL_VK:
      case PLSQLParser.LEFT_VK:
      case PLSQLParser.RIGHT_VK:
        // do not go into error
        break;
      case PLSQLParser.TABLE_REF_ELEMENT:
        SqlXlateUtil.attachChild(newjoin, genForTableRefElement(child));
        break;
      case PLSQLParser.SQL92_RESERVED_ON:
        attachGenForOnKey(newjoin, child);
        break;
      case PLSQLParser.PLSQL_NON_RESERVED_USING:
        attachGenForUsing(newjoin, child);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    return newjoin;
  }

  private void attachGenForUsing(ASTNode newjoin, SqlASTNode child) {
    analysisStat.setHasJoinUsing(true);
    // create = Identifer
    // we'll fix it later in fixColumns as then
    // we'll have all table resolvers
    ASTNode equal = SqlXlateUtil.newASTNode(HiveParser.EQUAL, "=");
    ASTNode colId = SqlXlateUtil.newASTNode(HiveParser.Identifier,
        child.getFirstChildWithType(PLSQLParser.COLUMN_NAME).getChild(0).getText());
    SqlXlateUtil.attachChild(equal, colId);
    SqlXlateUtil.attachChild(newjoin, equal);

  }

  /**
   * Generate Hive AST for ON key node and attach to dest.
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForOnKey(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.LOGIC_EXPR:
        attachGenForLogicExpr(dest, child);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
  }

  /**
   * Generate Hive AST for TABLE_REF_ELEMENT node.
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForTableRefElement(SqlASTNode src) throws SqlXlateException {
    ASTNode alias = null;
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.TABLE_EXPRESSION:
        ASTNode tableRef = genForTableExpression(child);
        if (alias != null) {
          SqlXlateUtil.attachChild(tableRef, alias);
        }
        return tableRef;
      case PLSQLParser.ALIAS:
        // if direct mode handle alias here
        // subquery alias are handled in FilterBlocks
        if (SqlXlateUtil.isTableExprDirectMode((SqlASTNode) src
            .getFirstChildWithType(PLSQLParser.TABLE_EXPRESSION))) {
          alias = genForAlias(child);
        }
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    return null;
  }

  /**
   * Generate Hive AST for Alias node
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForAlias(SqlASTNode src) throws SqlXlateException {
    String text = src.getChild(0).getText();
    ASTNode alias = SqlXlateUtil.newASTNode(HiveParser.Identifier, text);
    // SqlXlateUtil.attachChild(dest, alias);
    return alias;
  }

  /**
   * Generate Hive AST for TABLE_EXPRESSION
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForTableExpression(SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.DIRECT_MODE:
        return genForDirectMode(child);
      case PLSQLParser.SELECT_MODE:
        return genForSelectMode(child);
      default:
        SqlXlateUtil.error(child);
      }
    }
    return null;
  }

  /**
   * Generate Hive AST for SELECT_MODE
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForSelectMode(SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SELECT_STATEMENT:
        return genForSelectStatement(child);
      default:
        SqlXlateUtil.error(child);
      }
    }
    return null;
  }

  /**
   * Generate Hive AST for DIRECT_MODE
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForDirectMode(SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.TABLEVIEW_NAME: {
        return genForTableviewName(child);
      }
      default:
        SqlXlateUtil.error(child);
      }
    }
    return null;
  }

  /**
   * Generate Hive AST for TABLEVIEW_NAME subtree
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForTableviewName(SqlASTNode src) throws SqlXlateException {
    ASTNode tabref = SqlXlateUtil.newASTNode(HiveParser.TOK_TABREF, "TOK_TABREF");
    ASTNode tabname = SqlXlateUtil.newASTNode(HiveParser.TOK_TABNAME, "TOK_TABNAME");
    SqlXlateUtil.attachChild(tabref, tabname);
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.ID:
        SqlXlateUtil.attachChild(tabname, genForID(child));
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    return tabref;
  }

  /**
   * Generate Hive AST for SELECT_LIST subtree
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForSelectList(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.SELECT_ITEM: {
        ASTNode selexpr = SqlXlateUtil.newASTNode(HiveParser.TOK_SELEXPR, "TOK_SELEXPR");
        attachGenForSelectItem(selexpr, child);
        SqlXlateUtil.attachChild(dest, selexpr);
        break;
      }
      default:
        SqlXlateUtil.error(child);
      }
    }
  }

  /**
   * Generate Hive AST for SELECT_ITEM subtree
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForSelectItem(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.EXPR: {
        attachGenForExpr(dest, child);
        break;
      }
      case PLSQLParser.ALIAS:
        ASTNode alias = genForAlias(child);
        SqlXlateUtil.attachChild(dest, alias);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
  }

  /**
   *
   */
  private ASTNode genForDotAsterisk(SqlASTNode src) throws SqlXlateException {
    SqlASTNode tbv = (SqlASTNode) src.getFirstChildWithType(PLSQLParser.TABLEVIEW_NAME);
    String tname = tbv.getChild(0).getText();
    ASTNode allcolref = SqlXlateUtil.newASTNode(HiveParser.TOK_ALLCOLREF, "TOK_ALLCOLREF");
    ASTNode tabname = SqlXlateUtil.newASTNode(HiveParser.TOK_TABNAME, "TOK_TABNAME");
    ASTNode id = SqlXlateUtil.newASTNode(HiveParser.Identifier, tname);
    SqlXlateUtil.attachChild(allcolref, tabname);
    SqlXlateUtil.attachChild(tabname, id);
    return allcolref;
  }

  /**
   * Generate Hive AST for SELECT_ITEM subtree
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForExpr(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.CASCATED_ELEMENT:
        SqlXlateUtil.attachChild(dest, genForCascatedElement(child));
        break;
      case PLSQLParser.STANDARD_FUNCTION:
        attachGenForStandardFunction(dest, child);
        break;
      case PLSQLParser.DOT_ASTERISK:
        SqlXlateUtil.attachChild(dest, genForDotAsterisk(child));
        break;
      case PLSQLParser.SQL92_RESERVED_DISTINCT:
        // forward to next node
        attachGenForExpr(dest, child);
        break;
      case PLSQLParser.MODEL_EXPRESSION:
        SqlXlateUtil.attachChild(dest, genForModelExpression(child));
        break;
      case PLSQLParser.SUBQUERY:
        SqlXlateUtil.attachChild(dest, SqlXlateUtil.newASTNode(HiveParser.TOK_SUBQUERY,
            "TOK_SUBQUERY"));
        break;
      default:
        try {
          if (OpFuncFactory.isOpOrFunc(child)) {
            SqlXlateUtil.attachChild(dest, genForOpFunc(child));
          } else {
            // if not match any of above, then treat it as literal.
            // if can't be walked, throw error then.
            SqlXlateUtil.attachChild(dest, genForLiteral(child));
          }
        } catch (SqlXlateException e) {
          throw e;
        }
      }
    }
  }

  /**
   * Generate for Model Expression
   * 
   * @param child
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForModelExpression(SqlASTNode src) throws SqlXlateException {
    ASTNode lbranket = SqlXlateUtil.newASTNode(HiveParser.LSQUARE, "[");
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.CASCATED_ELEMENT:
        SqlXlateUtil.attachChild(lbranket, genForCascatedElement(child));
        break;
      case PLSQLParser.LOGIC_EXPR:
        attachGenForLogicExpr(lbranket, child);
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    return lbranket;
  }

  /**
   * Generate standard function and attach result nodes to dest as children.
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForStandardFunction(ASTNode dest, SqlASTNode src) throws SqlXlateException {

    SqlASTNode func = (SqlASTNode) src.getChild(0);
    // start function
    ASTNode tokFunc = opfuncFactory.createFunction(func);
    if (tokFunc == null) {
      SqlXlateUtil.error(func);
    }
    SqlXlateUtil.attachChild(dest, tokFunc);

    if (tokFunc.getType() == HiveParser.TOK_FUNCTIONSTAR) {
      return;
    }
    if (func.getType() == PLSQLParser.CAST_VK) {
      if (func.getChildCount() != 2) {
        SqlXlateUtil.error(func);
      }

      // exchange chuildren's place.
      SqlASTNode child0 = (SqlASTNode) func.getChild(0);
      func.deleteChild(0);
      func.addChild(child0);

    }
    for (int i = 0; i < func.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) func.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.NATIVE_DATATYPE:// TODO:may be other datatype
        attachGenForDataType(tokFunc, child);
        break;
      case PLSQLParser.ARGUMENTS:
        attachGenForArguments(tokFunc, child);
        break;
      case PLSQLParser.EXPR:
        attachGenForExpr(tokFunc, child);
      }
    }
  }

  /**
   * generate dataype node
   * TODO should add other datatype
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForDataType(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    if (src.getChildCount() != 1) {
      SqlXlateUtil.error(src);
    }
    SqlASTNode child = (SqlASTNode) src.getChild(0);
    if (child.getType() == PLSQLParser.INT_VK) {
      SqlXlateUtil.attachChild(dest, SqlXlateUtil.newASTNode(HiveParser.TOK_INT, "TOK_INT"));

    }
  }

  /**
   * Generate Hive AST for ARGUMENTS and attach results to dest
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForArguments(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.ARGUMENT:
        attachGenForArgument(dest, child);
        break;
      default:
        // if not match any of above, then treat it as literal.
        // if can't be walked, throw error then.
        SqlXlateUtil.attachChild(dest, genForLiteral(child));
      }
    }
  }

  /**
   * Generate Hive AST for ARGUMENT and attach results to dest
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private void attachGenForArgument(ASTNode dest, SqlASTNode src) throws SqlXlateException {
    attachGenForExpr(dest, (SqlASTNode) src.getChild(0));
  }

  /**
   * Generate Hive AST for ID
   * 
   * @param dest
   * @param src
   * @throws SqlXlateException
   */
  private ASTNode genForID(SqlASTNode src) throws SqlXlateException {
    String text = src.getText();
    ASTNode id = SqlXlateUtil.newASTNode(HiveParser.Identifier, text);
    return id;
  }

  /**
   * Generate Literal node.
   * 
   * @param src
   * @throws SqlXlateException
   */
  private ASTNode genForLiteral(SqlASTNode src) throws SqlXlateException {
    ASTNode n = null;
    // available hive literals
    // CharSetLiteral,StringLiteral,BigintLiteral,TinyintLiteral,SmallintLiteral
    switch (src.getType()) {
    case PLSQLParser.APPROXIMATE_NUM_LIT:
    case PLSQLParser.EXACT_NUM_LIT:
    case PLSQLParser.UNSIGNED_INTEGER: {
      n = SqlXlateUtil.newASTNode(HiveParser.Number, src.getText());
      break;
    }
    case PLSQLParser.NATIONAL_CHAR_STRING_LIT:
    case PLSQLParser.CHAR_STRING_PERL:
    case PLSQLParser.CHAR_STRING: {
      n = SqlXlateUtil.newASTNode(HiveParser.StringLiteral, src.getText());
      break;
    }
    case PLSQLParser.SQL92_RESERVED_NULL:
      n = SqlXlateUtil.newASTNode(HiveParser.TOK_NULL, "TOK_NULL");
      break;
    case PLSQLParser.SQL92_RESERVED_TRUE:
      n = SqlXlateUtil.newASTNode(HiveParser.KW_TRUE, "TRUE");
      break;
    case PLSQLParser.SQL92_RESERVED_FALSE:
      n = SqlXlateUtil.newASTNode(HiveParser.KW_FALSE, "FALSE");
      break;
    case PLSQLParser.ID:
      // for string literals "val" in AnyElement
      n = SqlXlateUtil.newASTNode(HiveParser.StringLiteral, src.getText());
      break;
    default:
      SqlXlateUtil.error(src);
    }
    return n;
  }

  /**
   * Generate Hive AST for CASCATED_ELEMENT
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForCascatedElement(SqlASTNode src) throws SqlXlateException {
    return genForCascatedElement(src, null);
  }

  /**
   * Generate Hive AST for CASCATED_ELEMENT
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForCascatedElement(SqlASTNode src, String tabAlias) throws SqlXlateException {
    boolean alreadyHasTblName = false;
    List<ASTNode> results = new ArrayList<ASTNode>();
    for (int i = 0; i < src.getChildCount(); i++) {
      SqlASTNode child = (SqlASTNode) src.getChild(i);
      switch (child.getType()) {
      case PLSQLParser.ANY_ELEMENT:
        if (child.getChildCount() > 1) {
          alreadyHasTblName = true;
        }
        results.add(genForAnyElement(child));
        break;
      case PLSQLParser.ROUTINE_CALL:
        results.add(genForRoutineCall(child));
        break;
      default:
        SqlXlateUtil.error(child);
      }
    }
    if (results.size() == 1) {
      ASTNode result = results.get(0);
      if (tabAlias != null && !alreadyHasTblName && result.getType() == HiveParser.TOK_TABLE_OR_COL) {
        ASTNode dot = SqlXlateUtil.newASTNode(HiveParser.DOT, ".");
        ASTNode tblOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL, "TOK_TABLE_OR_COL");
        ASTNode col = (ASTNode) result.getChild(0);
        SqlXlateUtil
            .attachChild(tblOrCol, SqlXlateUtil.newASTNode(HiveParser.Identifier, tabAlias));
        SqlXlateUtil.attachChild(dot, tblOrCol);
        SqlXlateUtil.attachChild(dot, col);
        result = dot;
      }
      LOG.debug("Generated Cascated Element : " + result.toStringTree());
      return result;
    } else if (results.size() == 2) {
      ASTNode dot = SqlXlateUtil.newASTNode(HiveParser.DOT, ".");
      SqlXlateUtil.attachChild(dot, results.get(0));
      // if results == 2 the second should only be text element
      // a little hack here
      SqlXlateUtil.attachChild(dot, (ASTNode) results.get(1).getChild(0));
      LOG.debug("Generated Cascated Element : " + dot.toStringTree());
      return dot;
    } else {
      throw new SqlXlateException("More than 2 results are found in Cascated element");
    }
  }

  /**
   * Generate Hive AST for RoutineCall
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForRoutineCall(SqlASTNode routineCall) throws SqlXlateException {
    ASTNode func = opfuncFactory.createFunction(routineCall);
    // not need to handle not like here as it should not happen in routine call (which is in select
    // expr)
    SqlASTNode arguments = (SqlASTNode) routineCall.getFirstChildWithType(PLSQLParser.ARGUMENTS);
    attachGenForArguments(func, arguments);
    return func;
  }

  /**
   * Generate Hive AST for ANY_ELEMENT
   * 
   * @param src
   * @return
   * @throws SqlXlateException
   */
  private ASTNode genForAnyElement(SqlASTNode src) throws SqlXlateException {
    // if this is a column ref e.g. t1.a
    if (src.getChildCount() == 1) {
      // column
      SqlASTNode child = (SqlASTNode) src.getChild(0);
      switch (child.getType()) {
      case PLSQLParser.ID:
        // if id contains ", don't attach table or col
        if (child.getText().contains("\"")) {
          return genForLiteral(child);
        }
        ASTNode tableOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL,
            "TOK_TABLE_OR_COL");
        SqlXlateUtil.attachChild(tableOrCol, genForID(child));
        return tableOrCol;
      default:
        return genForLiteral(child);
      }
    } else if (src.getChildCount() == 2) {
      // table.column
      assert (src.getChild(0).getType() == PLSQLParser.ID && src.getChild(1).getType() == PLSQLParser.ID);
      SqlASTNode tname = (SqlASTNode) src.getChild(0);
      SqlASTNode col = (SqlASTNode) src.getChild(1);
      ASTNode dot = SqlXlateUtil.newASTNode(HiveParser.DOT, ".");
      ASTNode tableOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL, "TOK_TABLE_OR_COL");
      SqlXlateUtil.attachChild(dot, tableOrCol);
      SqlXlateUtil.attachChild(tableOrCol, genForID(tname));
      SqlXlateUtil.attachChild(dot, genForID(col));
      return dot;
    } else if (src.getChildCount() == 3) {
      // schema.table.column
      assert (src.getChild(0).getType() == PLSQLParser.ID &&
          src.getChild(1).getType() == PLSQLParser.ID && src.getChild(2).getType() == PLSQLParser.ID);
      // just replace schema.table.column with table.column
      // as we've add all alias for table like schema.table
      // currently we didn't consider cases where we have same table name
      // in different schemas, handle that case later
      SqlASTNode tname = (SqlASTNode) src.getChild(1);
      SqlASTNode col = (SqlASTNode) src.getChild(2);
      ASTNode dot = SqlXlateUtil.newASTNode(HiveParser.DOT, ".");
      ASTNode tableOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL, "TOK_TABLE_OR_COL");
      SqlXlateUtil.attachChild(dot, tableOrCol);
      SqlXlateUtil.attachChild(tableOrCol, genForID(tname));
      SqlXlateUtil.attachChild(dot, genForID(col));
      return dot;
    } else {
      SqlXlateUtil.error(src);
    }
    return null;
  }

  /**
   * Post Process the generated AST to fix alias and columns
   * 
   * @throws SqlXlateException
   */
  private void postprocess(ASTNode root) throws SqlXlateException {
    PostProcessContext ctx = new PostProcessContext();
    buildRowResolverMap(ctx, root);
    propagateAlias(ctx, root);
    fixColumns(ctx, root, true);
    ctx.dumpRRMapToLogs();
  }

  /**
   * 
   * Context Class used during post processing.
   * 
   */
  private static class PostProcessContext {
    /**
     * Map from TOK_UNION/TOK_QUERY to RowResolver
     */
    private final HashMap<ASTNode, RowResolver> nodeRRMap = new HashMap<ASTNode, RowResolver>();

    public Map<ASTNode, RowResolver> getNodeRRMap() {
      return nodeRRMap;
    }

    public void dumpRRMapToLogs() {
      for (Map.Entry<ASTNode, RowResolver> entry : getNodeRRMap().entrySet()) {
        ASTNode n = entry.getKey();
        RowResolver rr = entry.getValue();
        LOG.debug("Row Resolver For : " + n.toStringTree() + "-------> " + rr.toString());
      }
    }
  }

  /**
   * Build Row Resolver Map For each significant AST node
   * 
   * @param ctx
   * @param node
   * @return
   * @throws SqlXlateException
   */
  private RowResolver buildRowResolverMap(PostProcessContext ctx, ASTNode node)
      throws SqlXlateException {
    // build row resolver map in bottom up manner
    boolean skipRecursion = false;
    if (node.getType() == HiveParser.TOK_INSERT_INTO) {
      skipRecursion = true;
    }
    List<RowResolver> childRet = new ArrayList<RowResolver>();
    if (!skipRecursion) {
      for (int i = 0; i < node.getChildCount(); i++) {
        RowResolver rr = buildRowResolverMap(ctx, (ASTNode) node.getChild(i));
        if (rr != null) {
          childRet.add(rr);
        }
      }
    }
    switch (node.getType()) {
    case HiveParser.TOK_QUERY:
      // construct row resolver based on select
      return constructRRQuery(ctx, node, childRet);
    case HiveParser.TOK_JOIN:
    case HiveParser.TOK_CROSSJOIN:
    case HiveParser.TOK_FULLOUTERJOIN:
    case HiveParser.TOK_LEFTOUTERJOIN:
    case HiveParser.TOK_RIGHTOUTERJOIN:
    case HiveParser.TOK_LEFTSEMIJOIN:
      // construct row resolver based on several table join
      return constructRRJoin(ctx, node, childRet);
    case HiveParser.TOK_UNION:
      // construct row resolver based on union
      return constructRRUnion(ctx, node, childRet);
    case HiveParser.TOK_TABREF:
      return constructRRTab(ctx, node);
    case HiveParser.TOK_SUBQUERY:
      return constructRRSubQ(ctx, node, childRet);
    case HiveParser.TOK_FROM:
      return constructRRFrom(ctx, node, childRet);
    default:
      // for other nodes, should return just one rr or return null
      if (childRet.size() == 0) {
        return null;
      } else if (childRet.size() == 1) {
        return childRet.get(0);
      } else {
        throw new SqlXlateException(
            "More than one child returns none null RowResolver but didn't get processed");
      }
    }
  }

  /**
   * Construct Row Resolver for TOK_SUBQUERY
   * 
   * @param ctx
   * @param subq
   * @param childRet
   * @return
   */
  private RowResolver constructRRSubQ(PostProcessContext ctx, ASTNode subq,
      List<RowResolver> childRet) {
    String subQAlias = subq.getFirstChildWithType(HiveParser.Identifier).getText();
    RowResolver outRR = new RowResolver();
    RowResolver inputRR = childRet.get(0);
    ArrayList<ColumnInfo> fList = inputRR.getColumnInfos();
    for (int i = 0; i < fList.size(); i++) {
      ColumnInfo colInfo = fList.get(i);
      String colAlias = getColAliasBasedOnIndex(i);
      // it's a hack now to set all cols as string
      // as we didn't have to check column type here
      outRR.put(subQAlias, colAlias,
          new ColumnInfo(colAlias,
              TypeInfoFactory.stringTypeInfo, subQAlias, true));
    }
    ctx.getNodeRRMap().put(subq, outRR);
    return outRR;
  }

  /**
   * Construct Row Resolver for TOK_FROM
   * 
   * @param ctx
   * @param subq
   * @param childRet
   * @return
   */
  private RowResolver constructRRFrom(PostProcessContext ctx, ASTNode from,
      List<RowResolver> childRet) throws SqlXlateException {
    assert (childRet.size() == 1);
    RowResolver inputRR = childRet.get(0);
    LOG.debug(inputRR.toString());
    RowResolver outRR = new RowResolver();
    // TOK_FROM could only have child TOK_SUBQUERY or TOK_TABREF or join
    ASTNode child = (ASTNode) from.getChild(0);
    if (child.getType() == HiveParser.TOK_TABREF ||
        child.getType() == HiveParser.TOK_SUBQUERY ||
        SqlXlateUtil.isJoinOp(child)) {
      ArrayList<ColumnInfo> fList = inputRR.getColumnInfos();
      for (ColumnInfo colInfo : fList) {
        outRR.put(colInfo.getTabAlias(), colInfo.getInternalName(), colInfo);
      }
    } else {
      throw new SqlXlateException("Unhandled node as child of from " + child.toStringTree());
    }
    return outRR;
  }

  /**
   * Construct Row Resolver for Join Ops
   * 
   * @param ctx
   * @param subq
   * @param childRet
   * @return
   */
  private RowResolver constructRRJoin(PostProcessContext ctx, ASTNode join,
      List<RowResolver> childRet) {
    RowResolver outRR = new RowResolver();
    assert ((join.getChildCount() <= childRet.size() + 1) && (join.getChildCount() >= childRet
        .size()));
    for (int i = 0; i < childRet.size(); i++) {
      RowResolver inputRR = childRet.get(i);
      ArrayList<ColumnInfo> fList = inputRR.getColumnInfos();
      for (ColumnInfo colInfo : fList) {
        outRR.put(colInfo.getTabAlias(), colInfo.getInternalName(), colInfo);
      }
    }
    ctx.getNodeRRMap().put(join, outRR);
    return outRR;
  }

  /**
   * Construct Row Resolver for TOK_UNION
   * 
   * @param ctx
   * @param subq
   * @param childRet
   * @return
   */
  private RowResolver constructRRUnion(PostProcessContext ctx, ASTNode union,
      List<RowResolver> childRet) {
    RowResolver outRR = new RowResolver();
    assert (union.getChildCount() == 2 && childRet.size() == 2);
    RowResolver leftRR = childRet.get(0);
    ArrayList<ColumnInfo> fList = leftRR.getColumnInfos();
    for (ColumnInfo colInfo : fList) {
      outRR.put(colInfo.getTabAlias(), colInfo.getInternalName(), colInfo);
    }
    ctx.getNodeRRMap().put(union, outRR);
    return outRR;
  }

  /**
   * Construct Row Resovler for TabReference
   * 
   * @param ctx
   * @param tabRef
   * @return
   * @throws SqlXlateException
   */
  private RowResolver constructRRTab(PostProcessContext ctx, ASTNode tabRef)
      throws SqlXlateException {
    RowResolver outRR = new RowResolver();

    RowResolver inputRR = null;
    String tabStr = null;
    ASTNode tabName = (ASTNode) tabRef.getFirstChildWithType(HiveParser.TOK_TABNAME);
    if (tabName.getChildCount() == 1) {
      tabStr = getMeta().getFullTblName(null, tabName.getChild(0).getText());
      inputRR = getMeta().getRRForTbl(tabName.getChild(0).getText());
    } else if (tabName.getChildCount() == 2) {
      tabStr = getMeta().getFullTblName(tabName.getChild(0).getText(),
          tabName.getChild(1).getText());
      inputRR = getMeta().getRRForTbl(tabName.getChild(0).getText(), tabName.getChild(1).getText());
    } else {
      throw new SqlXlateException("More than 2 child found in TOK_TABNAME");
    }

    String tableAlias = null;
    if (tabRef.getChildCount() == 2) {
      tableAlias = tabRef.getChild(1).getText();
    }

    if (inputRR == null) {
      throw new SqlXlateException(
          "Can not find input Table or Source is view (which we don't support now)");
    }
    HashMap<String, ColumnInfo> fMap = inputRR.getFieldMap(tabStr);
    for (Map.Entry<String, ColumnInfo> entry : fMap.entrySet()) {
      ColumnInfo colInfo = entry.getValue();
      // just change table alias and keep all other things the same
      // keep original information in colInfo
      outRR.put((tableAlias != null) ? tableAlias : colInfo.getTabAlias(),
          colInfo.getInternalName(),
          new ColumnInfo(colInfo.getInternalName(),
              colInfo.getType(),
              (tableAlias != null) ? tableAlias : colInfo.getTabAlias(),
              colInfo.getIsVirtualCol()));
    }
    LOG.debug("Create Output RowResolver" + outRR.toString());
    ctx.getNodeRRMap().put(tabRef, outRR);
    return outRR;
  }


  /**
   * Get column alias name based on the index in row
   * 
   * @param i
   * @return
   */
  private String getColAliasBasedOnIndex(int i) {
    return "column_" + Integer.toString(i);
  }

  /**
   * Construct Row Resolver for TOK_QUERY
   * 
   * @param ctx
   * @param query
   * @param childRet
   * @return
   */
  private RowResolver constructRRQuery(PostProcessContext ctx, ASTNode query,
      List<RowResolver> childRet) {
    assert (childRet.size() == 1);
    // RR composed in from clause
    RowResolver inputRR = childRet.get(0);
    RowResolver outRR = new RowResolver();
    ASTNode insert = (ASTNode) query.getFirstChildWithType(HiveParser.TOK_INSERT);
    ASTNode select = (ASTNode) insert.getFirstChildWithType(HiveParser.TOK_SELECT);
    if (select == null) {
      select = (ASTNode) insert.getFirstChildWithType(HiveParser.TOK_SELECTDI);
    }
    for (int i = 0; i < select.getChildCount(); i++) {
      // each child should be a selexpr
      ASTNode selexpr = (ASTNode) select.getChild(i);
      if (selexpr.getChild(0).getType() == HiveParser.TOK_ALLCOLREF) {
        ArrayList<ColumnInfo> fList = inputRR.getColumnInfos();
        for (ColumnInfo colInfo : fList) {
          outRR.put(colInfo.getTabAlias(), colInfo.getInternalName(), colInfo);
        }
        break;
      } else {
        String aliasStr = null;
        ASTNode alias = (ASTNode) selexpr.getChild(1);
        if (alias == null) {
          aliasStr = getColAliasBasedOnIndex(i);
          // add column alias for position order by to refer to
          if (SqlXlateUtil.isOrderByPosition((ASTNode) insert
              .getFirstChildWithType(HiveParser.TOK_ORDERBY))) {
            alias = SqlXlateUtil.newASTNode(HiveParser.Identifier, aliasStr);
            SqlXlateUtil.attachChild(selexpr, alias);
          }
        } else {
          aliasStr = alias.getText();
        }
        // as we didn't do type checking and only cares about name,
        // just use a dummy string type as column type
        outRR.put(null, aliasStr, new ColumnInfo(aliasStr,
            TypeInfoFactory.stringTypeInfo, null, true));

      }
    }
    ctx.getNodeRRMap().put(query, outRR);
    return outRR;
  }

  /**
   * Propagate Aliases From top down
   * 
   * @param ctx
   * @param ast
   */
  private void propagateAlias(PostProcessContext ctx, ASTNode ast) {
    // propagate alias in top down manner
    if (ast.getType() == HiveParser.TOK_UNION) {
      RowResolver thisRR = ctx.getNodeRRMap().get(ast);
      ctx.getNodeRRMap().put((ASTNode) ast.getChild(0), thisRR);
      ctx.getNodeRRMap().put((ASTNode) ast.getChild(1), thisRR);
      // since hive will do semantic checking,
      // we don't do redundant type checking here
    } else if (ast.getType() == HiveParser.TOK_SUBQUERY) {
      RowResolver thisRR = ctx.getNodeRRMap().get(ast);
      ctx.getNodeRRMap().put((ASTNode) ast.getChild(0), thisRR);
    }
    for (int i = 0; i < ast.getChildCount(); i++) {
      propagateAlias(ctx, (ASTNode) ast.getChild(i));
    }
  }

  /**
   * Fix columns if necessary
   * 
   * @param ctx
   * @param ast
   * @param isRoot
   * @throws SqlXlateException
   */
  private void fixColumns(PostProcessContext ctx, ASTNode ast, boolean isRoot)
      throws SqlXlateException {
    // fix columns in a top down manner
    for (int i = 0; i < ast.getChildCount(); i++) {
      fixColumns(ctx, (ASTNode) ast.getChild(i), false);
    }
    if (ast.getType() == HiveParser.TOK_QUERY) {
      RowResolver outRR = ctx.getNodeRRMap().get(ast);
      ASTNode from = (ASTNode) ast.getFirstChildWithType(HiveParser.TOK_FROM);
      RowResolver inputRR = ctx.getNodeRRMap().get(from.getChild(0));
      ASTNode insert = (ASTNode) ast.getFirstChildWithType(HiveParser.TOK_INSERT);
      ASTNode select = (ASTNode) insert.getFirstChildWithType(HiveParser.TOK_SELECT);
      if (select == null) {
        select = (ASTNode) insert.getFirstChildWithType(HiveParser.TOK_SELECTDI);
      }
      ArrayList<ColumnInfo> inRow = inputRR.getColumnInfos();
      ArrayList<ColumnInfo> outRow = outRR.getColumnInfos();
      LOG.debug("InRow : " + inRow);
      LOG.debug("OutRow : " + outRow);

      if (!isRoot) {
        if (SqlXlateUtil.isSelectStar(select)) {
          LOG.debug("Select * Found in Subquery, Expanding ");
          select.deleteChild(0);

          for (int i = 0; i < outRow.size(); i++) {
            ColumnInfo inCol = inRow.get(i);
            ColumnInfo outCol = outRow.get(i);
            // create TAB_OR_COL
            ASTNode selexpr = SqlXlateUtil.newASTNode(HiveParser.TOK_SELEXPR, "TOK_SELEXPR");
            // ASTNode dot = SqlXlateUtil.newASTNode(HiveParser.DOT,".");
            ASTNode tabOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL,
                  "TOK_TABLE_OR_COL");
            SqlXlateUtil.attachChild(selexpr, tabOrCol);
            SqlXlateUtil.attachChild(tabOrCol,
                  SqlXlateUtil.newASTNode(HiveParser.Identifier, inCol.getInternalName()));

            SqlXlateUtil.attachChild(selexpr,
                  SqlXlateUtil.newASTNode(HiveParser.Identifier, outCol.getInternalName()));

            SqlXlateUtil.attachChild(select, selexpr);
          }
        } else {
          LOG.debug("Normal Select Expr found, adding alias ");
          for (int i = 0; i < select.getChildCount(); i++) {
            ASTNode selexpr = (ASTNode) select.getChild(i);
            ColumnInfo col = outRow.get(i);
            if (selexpr.getChildCount() == 1) {
              ASTNode exprAlias = SqlXlateUtil.newASTNode(HiveParser.Identifier,
                    col.getInternalName());
              SqlXlateUtil.attachChild(selexpr, exprAlias);
            } else {
              // TODO
              // replace alias and fix alias reference as well
            }
          }

        }
      }

      // fix select distinct *
      // (TOK_SELECTDI (TOK_SELEXPR TOK_ALLCOLREF)
      if (SqlXlateUtil.isSelectDistinctStar(select)) {
        select.deleteChild(0);
        for (int i = 0; i < inRow.size(); i++) {
          ColumnInfo inCol = inRow.get(i);
          ASTNode selexpr = SqlXlateUtil.newASTNode(HiveParser.TOK_SELEXPR, "TOK_SELEXPR");
          ASTNode dot = SqlXlateUtil.newASTNode(HiveParser.DOT, ".");
          ASTNode tabOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL,
              "TOK_TABLE_OR_COL");
          SqlXlateUtil.attachChild(selexpr, dot);
          SqlXlateUtil.attachChild(dot, tabOrCol);
          SqlXlateUtil.attachChild(tabOrCol,
              SqlXlateUtil.newASTNode(HiveParser.Identifier, inCol.getTabAlias()));
          SqlXlateUtil.attachChild(dot,
              SqlXlateUtil.newASTNode(HiveParser.Identifier, inCol.getInternalName()));
          SqlXlateUtil.attachChild(select, selexpr);
        }
      }
      // fix select count (distinct *)
      // TOK_FUNCTIONDI count
      if (SqlXlateUtil.isSelectCountDistinctStar(select)) {
        // expand count arguments
        ASTNode functionDi = (ASTNode) select.getChild(0).getChild(0);
        // a little bit hack here as we know we added one column for join
        // need to fix that to know the size
        int leftTableSize = inRow.size() - 1;
        for (int i = 0; i < leftTableSize; i++) {
          ColumnInfo inCol = inRow.get(i);
          ASTNode dot = SqlXlateUtil.newASTNode(HiveParser.DOT, ".");
          ASTNode tabOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL,
              "TOK_TABLE_OR_COL");
          SqlXlateUtil.attachChild(dot, tabOrCol);
          SqlXlateUtil.attachChild(tabOrCol,
              SqlXlateUtil.newASTNode(HiveParser.Identifier, inCol.getTabAlias()));
          SqlXlateUtil.attachChild(dot,
              SqlXlateUtil.newASTNode(HiveParser.Identifier, inCol.getInternalName()));
          SqlXlateUtil.attachChild(functionDi, dot);
        }

      }

      // fix position orderby : e.g. order by 1,2
      ArrayList<ColumnInfo> orderIndexRow = null;
      if (SqlXlateUtil.isSelectStar(select)) {
        orderIndexRow = inRow;
      } else {
        orderIndexRow = outRow;
      }
      ASTNode orderby = null;
      if ((orderby = (ASTNode) insert.getFirstChildWithType(HiveParser.TOK_ORDERBY)) != null) {
        for (int i = 0; i < orderby.getChildCount(); i++) {
          ASTNode id = (ASTNode) orderby.getChild(i).getChild(0);
          LOG.debug("Found orderby expr : \"" + id + "\"");
          if (Pattern.matches("\\d+", id.getText())) {
            // if it is numeric replace it with col alias
            ColumnInfo colInfo = orderIndexRow.get(Integer.parseInt(id.getText()) - 1);
            LOG.debug("matched integer, replace with " + colInfo.getInternalName());
            ((ASTNode) orderby.getChild(i)).deleteChild(0);
            ASTNode tabOrCol = SqlXlateUtil.newASTNode(HiveParser.TOK_TABLE_OR_COL,
                "TOK_TABLE_OR_COL");
            ASTNode col = SqlXlateUtil.newASTNode(HiveParser.Identifier, colInfo.getInternalName());
            SqlXlateUtil.attachChild(tabOrCol, col);
            SqlXlateUtil.attachChild((ASTNode) orderby.getChild(i), tabOrCol);
          }
        }
      }

    }
    // fix join using clause
    if (SqlXlateUtil.isJoinOp(ast)) {
      // if "using is found. = Identifer "
      ASTNode equal = (ASTNode) ast.getChild(2);
      if (equal != null &&
          equal.getChildCount() == 1 &&
          equal.getChild(0).getType() == HiveParser.Identifier) {
        String colName = ast.getChild(2).getChild(0).getText();
        RowResolver leftRR = ctx.getNodeRRMap().get(ast.getChild(0));
        RowResolver rightRR = ctx.getNodeRRMap().get(ast.getChild(1));
        // check for hit columns
        List<ColumnInfo> hitcolumns = new ArrayList<ColumnInfo>();
        for (ColumnInfo cInfo : leftRR.getColumnInfos()) {
          if (cInfo.getInternalName().equals(colName)) {
            hitcolumns.add(cInfo);
          }
        }
        for (ColumnInfo cInfo2 : rightRR.getColumnInfos()) {
          if (cInfo2.getInternalName().equals(colName)) {
            hitcolumns.add(cInfo2);
          }
        }
        if (hitcolumns.size() != 2) {
          throw new SqlXlateException("More than one columns found for using ");
        }

        ASTNode left = SqlXlateUtil.makeASTforColumn(
                  hitcolumns.get(0).getTabAlias(),
                  hitcolumns.get(0).getInternalName());
        ASTNode right = SqlXlateUtil.makeASTforColumn(
                  hitcolumns.get(1).getTabAlias(),
                  hitcolumns.get(1).getInternalName());
        equal.deleteChild(0);
        SqlXlateUtil.attachChild(equal, left);
        SqlXlateUtil.attachChild(equal, right);
      }
    }
  }


}
