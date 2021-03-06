package se.cambio.cds.gdl.converters.drools;

import org.openehr.rm.datatypes.basic.DataValue;
import se.cambio.cds.gdl.model.expression.*;
import se.cambio.cds.model.instance.ArchetypeReference;
import se.cambio.cds.util.ExpressionUtil;
import se.cambio.cds.util.RefStat;
import se.cambio.cds.util.export.DVDefSerializer;
import se.cambio.cm.model.archetype.vo.ArchetypeElementVO;
import se.cambio.openehr.util.OpenEHRConst;
import se.cambio.openehr.util.OpenEHRDataValues;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class GdlDroolsAssignmentExpressionProcessor {

    private final GDLDroolsConverter gdlDroolsConverter;
    private final AssignmentExpression assignmentExpression;
    private final Map<RefStat, Set<String>> stats;
    private StringBuffer stringBuffer;
    private static final String TAB = "\t";

    public GdlDroolsAssignmentExpressionProcessor(
            GDLDroolsConverter gdlDroolsConverter, AssignmentExpression assignmentExpression, Map<RefStat, Set<String>> stats) {
        this.gdlDroolsConverter = gdlDroolsConverter;
        this.assignmentExpression = assignmentExpression;
        this.stats = stats;
        stringBuffer = new StringBuffer();
    }

    public String process() {
        String gtCode = assignmentExpression.getVariable().getCode();
        Variable var = assignmentExpression.getVariable();
        String attribute = var.getAttribute();
        processAssignmentExpression(gtCode, gtCode, attribute, assignmentExpression.getAssignment(), false);
        return stringBuffer.toString();
    }

    private void processAssignmentExpression(String gtCode,
                                             String eiId,
                                             String attribute,
                                             ExpressionItem expressionItemAux,
                                             boolean creatingInstance) {
        if (!CreateInstanceExpression.FUNCTION_CREATE_NAME.equals(attribute) && !creatingInstance) {
            stats.get(RefStat.REFERENCE).add(gtCode);
            stats.get(RefStat.SET).add(gtCode);
        }
        if (attribute == null) {
            if (expressionItemAux instanceof Variable) {
                Variable var2 = (Variable) expressionItemAux;
                String gtCodeAux = var2.getCode();
                stats.get(RefStat.REFERENCE).add(gtCodeAux);
                stringBuffer.append("$")
                        .append(eiId)
                        .append(".setDataValue($")
                        .append(gtCodeAux)
                        .append(ExpressionUtil.getDataValueMethod(gtCode))
                        .append(");")
                        .append("$")
                        .append(eiId)
                        .append(".setNullFlavour(null);")
                        .append("$executionLogger.addLog(drools, $")
                        .append(eiId)
                        .append(");");
            } else if (expressionItemAux instanceof ConstantExpression) {
                String dvStr = ((ConstantExpression) expressionItemAux).getValue();
                ArchetypeElementVO archetypeElementVO = gdlDroolsConverter.getElementMap().get(gtCode);
                if (archetypeElementVO == null) {
                    String guideId = gdlDroolsConverter.getGuide().getId();
                    throw new RuntimeException(format("Guide=%s, Unknown element for gtCode '%s'", guideId, gtCode));
                }
                String rmType = archetypeElementVO.getType();
                DataValue dv = DataValue.parseValue(rmType + "," + dvStr);
                stringBuffer.append("$").append(eiId).append(".setDataValue(").append(DVDefSerializer.getDVInstantiation(dv)).append(");");
                stringBuffer.append("$").append(eiId).append(".setNullFlavour(null);");
                stringBuffer.append("$executionLogger.addLog(drools, $").append(eiId).append(");");
            } else {
                String guideId = gdlDroolsConverter.getGuide().getId();
                throw new RuntimeException(format("Guide=%s, Unknown expression '%s'", guideId, expressionItemAux));
            }
        } else {
            if (attribute.equals(OpenEHRConst.NULL_FLAVOR_ATTRIBUTE)) {
                String dvStr = ((ConstantExpression) expressionItemAux).getValue();
                DataValue dv = DataValue.parseValue(OpenEHRDataValues.DV_CODED_TEXT + "," + dvStr);
                stringBuffer.append("$")
                        .append(eiId)
                        .append(".setDataValue(null);")
                        .append("$")
                        .append(eiId)
                        .append(".setNullFlavour(")
                        .append(DVDefSerializer.getDVInstantiation(dv))
                        .append(");")
                        .append("$executionLogger.addLog(drools, $")
                        .append(eiId).append(");");
            } else if (attribute.equals(CreateInstanceExpression.FUNCTION_CREATE_NAME)) {
                ArchetypeReference ar = gdlDroolsConverter.getArchetypeReferenceMap().get(gtCode);
                int creationIndex = gdlDroolsConverter.getCreationIndex();
                String arId = "newAR" + creationIndex;
                stringBuffer.append("ArchetypeReference ")
                        .append(arId).append(" = new ArchetypeReference(\"CDS\", \"")
                        .append(ar.getIdArchetype())
                        .append("\",")
                        .append(ar.getIdTemplate() != null ? "\"" + ar.getIdTemplate() + "\"" : "null").append(");\n")
                        .append(TAB)
                        .append("insert(")
                        .append(arId)
                        .append(");\n");
                insertAssignments(arId, expressionItemAux);
                gdlDroolsConverter.increaseCreationIndex();
            } else {
                ArchetypeElementVO archetypeElementVO = gdlDroolsConverter.getElementMap().get(gtCode);
                if (archetypeElementVO == null) {
                    String guideId = gdlDroolsConverter.getGuide().getId();
                    throw new RuntimeException(format("GTCode '%s' not found. (guideId='%s')", gtCode, guideId));
                }
                Map<RefStat, Set<String>> statsAux = gdlDroolsConverter.initStats();
                String arithmeticExpStr =
                        ExpressionUtil.getArithmeticExpressionStr(gdlDroolsConverter.getElementMap(), expressionItemAux, statsAux);
                stats.get(RefStat.REFERENCE).addAll(statsAux.get(RefStat.REFERENCE));
                stats.get(RefStat.REFERENCE).addAll(statsAux.get(RefStat.ATT_FUNCTIONS_REF));
                stats.get(RefStat.ATT_SET_REF).addAll(statsAux.get(RefStat.REFERENCE));
                stats.get(RefStat.ATT_FUNCTIONS).addAll(statsAux.get(RefStat.ATT_FUNCTIONS));
                String rmName = archetypeElementVO.getType();
                stringBuffer.append("$")
                        .append(eiId)
                        .append(".")
                        .append(GDLDroolsConverter.getAttributeSettingStr(eiId, rmName, attribute, arithmeticExpStr))
                        .append(";")
                        .append("$")
                        .append(eiId)
                        .append(".setNullFlavour(null);")
                        .append("$executionLogger.addLog(drools, $")
                        .append(eiId)
                        .append(");");
            }
        }
    }

    private void insertAssignments(
            String arId,
            ExpressionItem expressionItem) {
        if (!(expressionItem instanceof MultipleAssignmentExpression)) {
            String guideId = gdlDroolsConverter.getGuide().getId();
            throw new RuntimeException(format("Guide=%s, Incorrect expression inside creation expression '%s'", guideId, expressionItem));
        }
        MultipleAssignmentExpression multipleAssignmentExpression = (MultipleAssignmentExpression) expressionItem;
        int index = 0;
        Map<String, String> elementIdsMap = new HashMap<>();
        for (AssignmentExpression assignmentExpressionAux : multipleAssignmentExpression.getAssignmentExpressions()) {
            String gtCode = assignmentExpressionAux.getVariable().getCode();
            ArchetypeElementVO archetypeElementVO = gdlDroolsConverter.getElementMap().get(gtCode);
            if (archetypeElementVO == null) {
                String guideId = gdlDroolsConverter.getGuide().getId();
                throw new RuntimeException(format("GTCode '%s' not found. (guideId='%s')", gtCode, guideId));
            }
            String elementId = archetypeElementVO.getId();
            String eiId = elementIdsMap.get(elementId);
            if (eiId == null) {
                int creationIndex = gdlDroolsConverter.getCreationIndex();
                eiId = "ei" + creationIndex + "_" + index;
                elementIdsMap.put(elementId, eiId);
                stringBuffer.append(TAB);
                stringBuffer.append(format("ElementInstance $%s = new ElementInstance(\"%s\", null, %s, null, null);\n", eiId, elementId, arId));
            } else {
                stringBuffer.append("\n");
            }
            stringBuffer.append(TAB);
            String attribute = assignmentExpressionAux.getVariable().getAttribute();
            processAssignmentExpression(gtCode, eiId, attribute, assignmentExpressionAux.getAssignment(), true);
            stringBuffer.append("insert($").append(eiId).append(");");
            index++;
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