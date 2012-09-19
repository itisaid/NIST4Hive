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

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;

import br.com.porcelli.parser.plsql.PLSQLParser;

/**
 *
 * Factory class for generating Operator and Functions.
 *
 */
public final class OpFuncFactory {

  private static final Log LOG = LogFactory.getLog("hive.ql.parse.sql.HiveASTGenerator");

  protected static HashMap<Integer, Token> opMap = new HashMap<Integer, Token>();
  protected static HashMap<Integer, Token> funcMap = new HashMap<Integer, Token>();
  static {
    /* Operator Map */
    opMap.put(PLSQLParser.EQUALS_OP, new CommonToken(HiveParser.EQUAL, "="));
    opMap.put(PLSQLParser.NOT_EQUAL_OP, new CommonToken(HiveParser.NOTEQUAL, "!="));
    opMap.put(PLSQLParser.LESS_THAN_OR_EQUALS_OP, new CommonToken(HiveParser.LESSTHANOREQUALTO,
        "<="));
    opMap.put(PLSQLParser.LESS_THAN_OP, new CommonToken(HiveParser.LESSTHAN, "<"));
    opMap.put(PLSQLParser.GREATER_THAN_OR_EQUALS_OP, new CommonToken(
        HiveParser.GREATERTHANOREQUALTO, ">="));
    opMap.put(PLSQLParser.GREATER_THAN_OP, new CommonToken(HiveParser.GREATERTHAN, ">"));
    opMap.put(PLSQLParser.SQL92_RESERVED_AND, new CommonToken(HiveParser.KW_AND, "AND"));
    opMap.put(PLSQLParser.SQL92_RESERVED_OR, new CommonToken(HiveParser.KW_OR, "OR"));
    opMap.put(PLSQLParser.SQL92_RESERVED_NOT, new CommonToken(HiveParser.KW_NOT, "NOT"));
    opMap.put(PLSQLParser.UNARY_OPERATOR, new CommonToken(HiveParser.MINUS, "-"));
    opMap.put(PLSQLParser.MINUS_SIGN, new CommonToken(HiveParser.MINUS, "-"));
    opMap.put(PLSQLParser.PLUS_SIGN, new CommonToken(HiveParser.PLUS, "+"));
    opMap.put(PLSQLParser.SOLIDUS, new CommonToken(HiveParser.DIVIDE, "/"));
    opMap.put(PLSQLParser.ASTERISK, new CommonToken(HiveParser.STAR, "*"));
    opMap.put(PLSQLParser.SQL92_RESERVED_LIKE, new CommonToken(HiveParser.KW_LIKE, "LIKE"));
    opMap.put(PLSQLParser.NOT_LIKE, new CommonToken(HiveParser.KW_LIKE, "LIKE"));
    /* Funciton Map */
    funcMap.put(PLSQLParser.IS_NULL, new CommonToken(HiveParser.TOK_ISNULL, "TOK_ISNULL"));
    funcMap.put(PLSQLParser.IS_NOT_NULL, new CommonToken(HiveParser.TOK_ISNOTNULL,
        "TOK_ISNOTNULL"));
    funcMap.put(PLSQLParser.SQL92_RESERVED_IN, new CommonToken(HiveParser.KW_IN, "IN"));
    funcMap.put(PLSQLParser.COUNT_VK, new CommonToken(HiveParser.Identifier, "count"));
    funcMap.put(PLSQLParser.SQL92_RESERVED_BETWEEN, new CommonToken(HiveParser.Identifier,
        "between"));
    funcMap.put(PLSQLParser.NOT_BETWEEN, new CommonToken(HiveParser.Identifier, "between"));
    funcMap.put(PLSQLParser.FUNCTION_ENABLING_WITHIN_OR_OVER, new CommonToken(
        HiveParser.Identifier, "TO_BE_REPLACED"));
    funcMap.put(PLSQLParser.FUNCTION_ENABLING_OVER, new CommonToken(HiveParser.Identifier,
        "TO_BE_REPLACED"));
    funcMap.put(PLSQLParser.FUNCTION_ENABLING_USING, new CommonToken(HiveParser.Identifier,
        "TO_BE_REPLACED"));
    funcMap.put(PLSQLParser.ROUTINE_CALL, new CommonToken(HiveParser.Identifier,
        "TO_BE_REPLACED"));
    funcMap.put(PLSQLParser.SIMPLE_CASE, new CommonToken(HiveParser.KW_CASE, "case"));
    funcMap.put(PLSQLParser.SEARCHED_CASE, new CommonToken(HiveParser.Identifier, "case"));
    funcMap.put(PLSQLParser.TRIM_VK, new CommonToken(HiveParser.Identifier, "trim"));
    funcMap.put(PLSQLParser.CAST_VK, new CommonToken(HiveParser.Identifier, "cast"));

  }

  /**
   * Create Operator from SQL AST node
   *
   * @param node
   * @return
   */
  private static ASTNode createOperator(SqlASTNode node) {
    int tokenType = node.getType();
    if (!opMap.containsKey(tokenType)) {
      return null;
    }
    Token token = opMap.get(tokenType);
    if (tokenType == PLSQLParser.UNARY_OPERATOR) {
      String text = node.getText();
      if ("+".equals(text)) {
        token = opMap.get(PLSQLParser.PLUS_SIGN);
      } else if ("-".equals(text)) {
        token = opMap.get(PLSQLParser.MINUS_SIGN);
      } else {
        // maybe exist some fault unconsidered.
        return null;
      }
    }
    ASTNode ret = SqlXlateUtil.newASTNode(token);
    if (tokenType == PLSQLParser.NOT_LIKE) {
      ASTNode not = SqlXlateUtil.newASTNode(HiveParser.KW_NOT, "NOT");
      SqlXlateUtil.attachChild(not, ret);
      ret = not;
    }
    if (tokenType == PLSQLParser.NOT_EQUAL_OP) {
      ret.getToken().setText(node.getText());
    }
    return ret;
  }

  /**
   * Create Function from SQL AST node
   *
   * @param node
   * @return
   */
  public ASTNode createFunction(SqlASTNode node) {
    LOG.debug("Creating Function for type : " + node.getType());
    if (!funcMap.containsKey(node.getType())) {
      return null;
    }
    ASTNode tokFunc = null;

    // start function
    if (node.getType() == PLSQLParser.ROUTINE_CALL) {
      tokFunc = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTION, "TOK_FUNCTION");
      SqlASTNode Arguments = (SqlASTNode) node.getFirstChildWithType(PLSQLParser.ARGUMENTS);
      if (Arguments != null) {
        SqlASTNode firstArg = (SqlASTNode) Arguments.getFirstChildWithType(PLSQLParser.ARGUMENT);
        if (firstArg != null) {
          SqlASTNode expr = (SqlASTNode) firstArg.getFirstChildWithType(PLSQLParser.EXPR);
          if (expr != null) {
            SqlASTNode distinct = (SqlASTNode) expr
                .getFirstChildWithType(PLSQLParser.SQL92_RESERVED_DISTINCT);
            if (distinct != null) {
              tokFunc = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTIONDI, "TOK_FUNCTIONDI");
            }
          }
        }
      }

    } else {
      SqlASTNode arg1 = (SqlASTNode) node.getChild(0);
      assert (arg1 != null);
      if (arg1.getType() == PLSQLParser.ASTERISK) {
        if (arg1.getChildCount() > 0) {
          tokFunc = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTION, "TOK_FUNCTION");
        } else {
          tokFunc = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTIONSTAR, "TOK_FUNCTIONSTAR");
        }
      } else if (SqlXlateUtil.hasNodeTypeInTree(node, PLSQLParser.SQL92_RESERVED_DISTINCT)) {
        tokFunc = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTIONDI, "TOK_FUNCTIONDI");
      } else {
        tokFunc = SqlXlateUtil.newASTNode(HiveParser.TOK_FUNCTION, "TOK_FUNCTION");
      }
    }

    int tokenType = node.getType();
    if (tokenType == PLSQLParser.CAST_VK) {
    }
    else if (tokenType == PLSQLParser.SEARCHED_CASE) {
    } else if (tokenType == PLSQLParser.FUNCTION_ENABLING_WITHIN_OR_OVER ||
        tokenType == PLSQLParser.FUNCTION_ENABLING_OVER ||
        tokenType == PLSQLParser.FUNCTION_ENABLING_USING) {
      SqlXlateUtil.attachChild(tokFunc, SqlXlateUtil.newASTNode(new CommonToken(
          HiveParser.Identifier, node.getText())));
      LOG.debug("General function call: " + node.getText());
    } else if (tokenType == PLSQLParser.ROUTINE_CALL) {
      SqlASTNode rName = (SqlASTNode) node.getFirstChildWithType(PLSQLParser.ROUTINE_NAME)
          .getChild(0);
      if (rName.getText().equals("array")) {
        SqlXlateUtil.attachChild(tokFunc, SqlXlateUtil.newASTNode(new CommonToken(
            HiveParser.KW_ARRAY, "array")));
      } else {
        SqlXlateUtil.attachChild(tokFunc, SqlXlateUtil.newASTNode(new CommonToken(
            HiveParser.Identifier, rName.getText())));
      }
    } else {
      tokFunc.addChild(SqlXlateUtil.newASTNode(funcMap.get(tokenType)));
      if (tokenType == PLSQLParser.SQL92_RESERVED_BETWEEN) {
        tokFunc.addChild(SqlXlateUtil.newASTNode(HiveParser.KW_FALSE, "KW_FALSE"));
      } else if (tokenType == PLSQLParser.NOT_BETWEEN) {
        tokFunc.addChild(SqlXlateUtil.newASTNode(HiveParser.KW_TRUE, "KW_TRUE"));
      }
    }
    return tokFunc;
  }


  /**
   * Create either function of operator.
   */
  public ASTNode create(SqlASTNode node) throws SqlXlateException {
    // try operator first
    ASTNode opfunc = createOperator(node);
    // then try function
    if (opfunc == null) {
      opfunc = createFunction(node);
    }
    if (opfunc != null) {
      return opfunc;
    }
    throw new SqlXlateException("Unknown Function or Operator : type - " + node.getType()
        + ", text -" + node.getText() + " !");
  }

  /**
   * Check whether node is Op or function node
   */
  public static boolean isOpOrFunc(SqlASTNode node) {
    return (isOperator(node) || isFunction(node));
  }

  /**
   * Check whether node is Op node
   */
  private static boolean isOperator(SqlASTNode node) {
    if (opMap.containsKey(node.getType())) {
      return true;
    }
    return false;
  }

  /**
   * Check whether node is function node
   */
  private static boolean isFunction(SqlASTNode node) {
    if (funcMap.containsKey(node.getType())) {
      return true;
    }
    return false;
  }
}
