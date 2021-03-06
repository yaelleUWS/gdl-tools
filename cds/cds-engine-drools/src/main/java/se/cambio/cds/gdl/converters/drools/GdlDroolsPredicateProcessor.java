package se.cambio.cds.gdl.converters.drools;

import org.apache.commons.lang.StringUtils;
import org.openehr.rm.datatypes.basic.DataValue;
import org.slf4j.LoggerFactory;
import se.cambio.cds.gdl.model.ArchetypeBinding;
import se.cambio.cds.gdl.model.expression.*;
import se.cambio.cds.util.ExpressionUtil;
import se.cambio.cds.util.export.DVDefSerializer;
import se.cambio.cm.model.archetype.vo.ArchetypeElementVO;
import se.cambio.openehr.controller.session.data.ArchetypeElements;

import static java.lang.String.format;

public class GdlDroolsPredicateProcessor {

    private GDLDroolsConverter gdlDroolsConverter;
    private ArchetypeBinding archetypeBinding;
    private StringBuffer sb;
    private static final String TAB = "\t";
    private static final String ARCHETYPE_REFERENCE_ID = "archetypeReference";

    public GdlDroolsPredicateProcessor(GDLDroolsConverter gdlDroolsConverter, ArchetypeBinding archetypeBinding) {
        this.gdlDroolsConverter = gdlDroolsConverter;
        this.archetypeBinding = archetypeBinding;
        this.sb = new StringBuffer();
    }

    public String process() {
        if (archetypeBinding.getPredicateStatements() != null) {
            for (ExpressionItem expressionItem : archetypeBinding.getPredicateStatements()) {
                if (expressionItem instanceof BinaryExpression) {
                    BinaryExpression binaryExpression = (BinaryExpression) expressionItem;
                    processBinaryExpressionPredicate(binaryExpression);
                } else if (expressionItem instanceof UnaryExpression) {
                    UnaryExpression unaryExpression = (UnaryExpression) expressionItem;
                    processUnaryExpressionPredicate(unaryExpression);
                }
            }
        }
        return sb.toString();
    }

    private void processUnaryExpressionPredicate(UnaryExpression unaryExpression) {
        gdlDroolsConverter.increasePredicateCount();
        int predicateCount = gdlDroolsConverter.getPredicateCount();
        if (!(unaryExpression.getOperand() instanceof Variable)) {
            String guideId = gdlDroolsConverter.getGuide().getId();
            String unaryOperand = unaryExpression.getOperand().getClass().getSimpleName();
            throw new RuntimeException(format("Guide=%s, Expecting variable for unary expression, instead got '%s'", guideId, unaryOperand));
        }
        Variable variable = (Variable) unaryExpression.getOperand();
        String idElement = archetypeBinding.getArchetypeId() + variable.getPath();
        sb.append(TAB);
        sb.append("ElementInstance(id==\""
                + idElement
                + "\", archetypeReference==$"
                + ARCHETYPE_REFERENCE_ID + "_" + archetypeBinding.getId() + ", "
                + "predicate || dataValue instanceof Comparable, "
                + "$predDV" + predicateCount + ":dataValue"
                + ")\n");
        ArchetypeElementVO archetypeElement = gdlDroolsConverter.getArchetypeManager().getArchetypeElements().getArchetypeElement(
                archetypeBinding.getTemplateId(),
                idElement);
        OperatorKind op = unaryExpression.getOperator();
        String opStr = getMaxMinOperator(op);
        if (archetypeElement != null && opStr != null) {
            String predAuxDef = getComparisonPredicateChecks(archetypeBinding);
            String predicateArchetypeRef = "";
            sb.append(TAB);
            sb.append("not(");
            if (predAuxDef != null) {
                sb.append(predAuxDef);
                predicateArchetypeRef = "archetypeReference==$archetypeReferencePredicate" + predicateCount + ",";
            }
            sb.append(TAB);
            sb.append(
                    format("ElementInstance(id==\"%s\", %sDVUtil.checkMaxMin($predDV%s, dataValue, \"%s\", $%s_%s, archetypeReference)))\n",
                            idElement, predicateArchetypeRef, predicateCount, op.getSymbol(), ARCHETYPE_REFERENCE_ID, archetypeBinding.getId()));
        } else {
            String guideId = gdlDroolsConverter.getGuide().getId();
            throw new RuntimeException(format("Guide=%s, Element not found '%s'", guideId, idElement));
        }
    }

    private String getMaxMinOperator(OperatorKind op) {
        String opStr = null;
        if (OperatorKind.MAX.equals(op)) {
            opStr = ">";
        } else if (OperatorKind.MIN.equals(op)) {
            opStr = "<";
        } else {
            String guideId = gdlDroolsConverter.getGuide().getId();
            LoggerFactory.getLogger(GdlDroolsPredicateProcessor.class).warn("Guide=" + guideId + ", Operator for predicate '" + op + "' is not valid.");
        }
        return opStr;
    }

    private void processBinaryExpressionPredicate(BinaryExpression binaryExpression) {
        if (binaryExpression.getLeft() instanceof Variable) {
            gdlDroolsConverter.increasePredicateCount();
            Variable variable = (Variable) binaryExpression.getLeft();
            if (!isVariableAttribute(variable) && binaryExpression.getRight() instanceof ConstantExpression) {
                ConstantExpression constantExpression = (ConstantExpression) binaryExpression.getRight();
                processConstantExpressionInBinaryPredicate(constantExpression, variable, binaryExpression.getOperator());
            } else {
                ExpressionItem expressionItem = binaryExpression.getRight();
                processExpressionItemInBinaryPredicate(expressionItem, variable, binaryExpression.getOperator());
            }
        }
    }

    private boolean isVariableAttribute(Variable variable) {
        return variable.getPath().contains("/value/") && !StringUtils.substringAfterLast(variable.getPath(), "/value/").contains("/");
    }

    private void processExpressionItemInBinaryPredicate(ExpressionItem expressionItem, Variable variable, OperatorKind operatorKind) {
        String path = variable.getPath();
        String attribute = path.substring(path.lastIndexOf("/value/") + 7, path.length());
        path = path.substring(0, path.length() - attribute.length() - 7);
        String idElement = archetypeBinding.getArchetypeId() + path;
        sb.append(TAB);
        int predicateCount = gdlDroolsConverter.getPredicateCount();
        String predicateHandle = "predicate" + predicateCount;
        sb.append(format("$%s:ElementInstance(id==\"%s\", dataValue!=null, archetypeReference==$%s_%s)\n",
                predicateHandle, idElement, ARCHETYPE_REFERENCE_ID, archetypeBinding.getId()));
        ArchetypeElementVO archetypeElement = gdlDroolsConverter.getArchetypeManager().getArchetypeElements().getArchetypeElement(
                archetypeBinding.getTemplateId(),
                idElement);
        if (archetypeElement != null) {
            String arithmeticExpressionStr = ExpressionUtil.getArithmeticExpressionStr(gdlDroolsConverter.getElementMap(), expressionItem, null);
            arithmeticExpressionStr = "(" + arithmeticExpressionStr + ")";
            sb.append(TAB);
            sb.append("eval(");
            Variable var = new Variable(predicateHandle, predicateHandle, path, attribute);
            String rmName = archetypeElement.getType();
            String varCall = ExpressionUtil.getVariableWithAttributeStr(rmName, var);
            sb.append("(");
            if (GDLDroolsConverter.isString(rmName, attribute)) {
                sb.append(gdlDroolsConverter.getAttributeOperatorMVELLine(varCall, operatorKind, arithmeticExpressionStr));
            } else {
                sb.append(varCall);
                sb.append(operatorKind.getSymbol());
                sb.append(arithmeticExpressionStr);
            }
            sb.append("))\n");
        } else {
            String guideId = gdlDroolsConverter.getGuide().getId();
            String templateId = archetypeBinding.getTemplateId() != null ? "(" + archetypeBinding.getTemplateId() + ")" : "";
            throw new RuntimeException(format("Guide=%s, Element not found '%s' %s", guideId, idElement, templateId));
        }
    }

    private void processConstantExpressionInBinaryPredicate(ConstantExpression constantExpression, Variable variable, OperatorKind operatorKind) {
        String idElement = archetypeBinding.getArchetypeId() + variable.getPath();
        int predicateCount = gdlDroolsConverter.getPredicateCount();
        sb.append(TAB)
                .append("$predicate")
                .append(predicateCount)
                .append(":ElementInstance(id==\"")
                .append(idElement)
                .append("\", archetypeReference==$")
                .append(ARCHETYPE_REFERENCE_ID)
                .append("_")
                .append(archetypeBinding.getId())
                .append(")\n");
        ArchetypeElementVO archetypeElement =
                gdlDroolsConverter.getArchetypeManager().getArchetypeElements().getArchetypeElement(
                        archetypeBinding.getTemplateId(),
                        idElement);
        if (archetypeElement == null) {
            String templateId = archetypeBinding.getTemplateId() != null ? "(" + archetypeBinding.getTemplateId() + ")" : "";
            String guideId = gdlDroolsConverter.getGuide().getId();
            throw new RuntimeException(format("Guide=%s, Element not found '%s' %s", guideId, idElement, templateId));
        }
        String rmType = archetypeElement.getType();
        sb.append(TAB);
        String dvStr = "null";
        if (!constantExpression.getValue().equals("null")) {
            dvStr = DVDefSerializer.getDVInstantiation(DataValue.parseValue(rmType + "," + constantExpression.getValue()));
        }
        String predicateVariableName = "$predicate" + gdlDroolsConverter.getPredicateCount();
        String operatorMVELLine = gdlDroolsConverter.getOperatorMVELLine(predicateVariableName, operatorKind, dvStr, true);
        sb.append("eval(").append(operatorMVELLine).append(")\n");
    }


    private String getComparisonPredicateChecks(ArchetypeBinding archetypeBinding) {
        StringBuffer sb = new StringBuffer();
        int archetypeReferencePredicateCount = gdlDroolsConverter.getPredicateCount();
        for (ExpressionItem expressionItem : archetypeBinding.getPredicateStatements()) {
            if (expressionItem instanceof BinaryExpression) {
                BinaryExpression binaryExpression = (BinaryExpression) expressionItem;
                if (binaryExpression.getLeft() instanceof Variable
                        && binaryExpression.getRight() instanceof ConstantExpression) {
                    gdlDroolsConverter.increasePredicateCount();
                    int predicateCount = gdlDroolsConverter.getPredicateCount();
                    Variable variable = (Variable) binaryExpression.getLeft();
                    ConstantExpression constantExpression = (ConstantExpression) binaryExpression.getRight();
                    String idElement = archetypeBinding.getArchetypeId() + variable.getPath();
                    sb.append(TAB);
                    String predicateVariableName = "$predicateAux" + predicateCount;
                    sb.append(predicateVariableName);
                    sb.append(":ElementInstance(id==\"");
                    sb.append(idElement);
                    sb.append("\", archetypeReference==$archetypeReferencePredicate").append(archetypeReferencePredicateCount).append(") and \n");
                    ArchetypeElements archetypeElements = gdlDroolsConverter.getArchetypeManager().getArchetypeElements();
                    ArchetypeElementVO archetypeElement = archetypeElements.getArchetypeElement(archetypeBinding.getTemplateId(), idElement);
                    if (archetypeElement != null) {
                        String rmType = archetypeElement.getType();
                        String dvStr = "null";
                        if (!"null".equals(constantExpression.getValue())) {
                            dvStr = DVDefSerializer.getDVInstantiation(DataValue.parseValue(rmType + "," + constantExpression.getValue()));
                        }
                        sb.append(TAB);
                        String operatorMVELLine = gdlDroolsConverter.getOperatorMVELLine(predicateVariableName, binaryExpression.getOperator(), dvStr, true);
                        sb.append("eval(")
                                .append(operatorMVELLine)
                                .append(") and \n");
                    }
                }
            }
        }
        String predicateDef = sb.toString();
        if (!predicateDef.isEmpty()) {
            sb = new StringBuffer();
            sb.append(TAB);
            sb.append("$archetypeReferencePredicate")
                    .append(archetypeReferencePredicateCount)
                    .append(":ArchetypeReference(idDomain==\"EHR\",");
            sb.append("idArchetype==\"")
                    .append(archetypeBinding.getArchetypeId())
                    .append("\") and \n");
            return sb.toString() + predicateDef;
        } else {
            return null;
        }
    }

}

/*
 *  ***** BEGIN LICENSE BLOCK *****
 *  Version: MPL 2.0/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  2.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *
 *  The Initial Developers of the Original Code are Iago Corbal and Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2012-2013
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *  ***** END LICENSE BLOCK *****
 */