package se.cambio.cds.gdl.model.readable.rule.lines;

import org.slf4j.LoggerFactory;
import se.cambio.cds.gdl.model.expression.ExpressionItem;
import se.cambio.cds.gdl.model.expression.OperatorKind;
import se.cambio.cds.gdl.model.expression.UnaryExpression;
import se.cambio.cds.gdl.model.expression.Variable;
import se.cambio.cds.gdl.model.readable.rule.lines.elements.ArchetypeElementRuleLineDefinitionElement;
import se.cambio.cds.gdl.model.readable.rule.lines.elements.PredicateComparisonFunctionRuleLineElement;
import se.cambio.cds.gdl.model.readable.rule.lines.elements.StaticTextRuleLineElement;
import se.cambio.cds.gdl.model.readable.rule.lines.interfaces.ArchetypeElementRuleLine;
import se.cambio.cds.gdl.model.readable.rule.lines.interfaces.DefinitionsRuleLine;
import se.cambio.cds.gdl.model.readable.rule.lines.interfaces.PredicateRuleLine;
import se.cambio.cds.model.instance.ArchetypeReference;
import se.cambio.cm.model.archetype.vo.ArchetypeElementVO;
import se.cambio.openehr.util.OpenEHRLanguageManager;


public class WithElementPredicateFunctionDefinitionRuleLine extends ExpressionRuleLine implements ArchetypeElementRuleLine, DefinitionsRuleLine, PredicateRuleLine {

    private ArchetypeElementRuleLineDefinitionElement archetypeElementRuleLineDefinitionElement = null;
    private PredicateComparisonFunctionRuleLineElement functionRuleLineElement = null;


    public WithElementPredicateFunctionDefinitionRuleLine() {
        super(OpenEHRLanguageManager.getMessage("ElementPredicateFunction"),
                OpenEHRLanguageManager.getMessage("ElementPredicateFunctionDesc"));
        archetypeElementRuleLineDefinitionElement = new ArchetypeElementRuleLineDefinitionElement(this);
        functionRuleLineElement = new PredicateComparisonFunctionRuleLineElement(this);

        getRuleLineElements().add(new StaticTextRuleLineElement(this, "WithElementRLE"));
        getRuleLineElements().add(archetypeElementRuleLineDefinitionElement);
        getRuleLineElements().add(functionRuleLineElement);
    }

    public ArchetypeElementRuleLineDefinitionElement getArchetypeElementRuleLineDefinitionElement() {
        return archetypeElementRuleLineDefinitionElement;
    }

    @Override
    public ArchetypeReference getArchetypeReference() {
        return getArchetypeInstantiationRuleLine().getArchetypeReferenceRuleLineDefinitionElement().getValue();
    }

    @Override
    public ArchetypeElementVO getArchetypeElement() {
        return archetypeElementRuleLineDefinitionElement.getValue();
    }

    public PredicateComparisonFunctionRuleLineElement getFunctionRuleLineElement() {
        return functionRuleLineElement;
    }

    private ArchetypeInstantiationRuleLine getArchetypeInstantiationRuleLine() {
        return (ArchetypeInstantiationRuleLine) getParentRuleLine();
    }

    @Override
    public ExpressionItem toExpressionItem() {
        ArchetypeElementVO archetypeElementVO = getArchetypeElementRuleLineDefinitionElement().getValue();
        String path = archetypeElementVO.getPath();
        OperatorKind operatorKind =
                getFunctionRuleLineElement().getValue();
        String name = getArchetypeManager().getArchetypeElements().getText(archetypeElementVO, getLanguage());
        return new UnaryExpression(
                new Variable(null, name, path),
                operatorKind);
    }

    @Override
    public String getPredicateDescription() {
        StringBuilder sb = new StringBuilder();
        ArchetypeElementRuleLineDefinitionElement aerlde = getArchetypeElementRuleLineDefinitionElement();
        if (aerlde != null) {
            ArchetypeElementVO archetypeElementVO = aerlde.getValue();
            if (archetypeElementVO != null) {
                String name = aerlde.getArchetypeManager().getArchetypeElements().getText(archetypeElementVO, getLanguage());
                sb.append(getFunctionRuleLineElement().getValue()).append("(").append(name).append(")");
            } else {
                LoggerFactory.getLogger(ArchetypeReference.class).warn("Unknown predicate for AR '" + aerlde.toString() + "'");
                sb.append("*UNKNOWN PREDICATE*");
            }
        }
        return sb.toString();
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