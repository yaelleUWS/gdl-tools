package se.cambio.cds.gdl.model.readable.rule.lines;

import se.cambio.cds.gdl.model.expression.ExpressionItem;
import se.cambio.cds.gdl.model.expression.UnaryExpression;
import se.cambio.cds.gdl.model.expression.Variable;
import se.cambio.cds.gdl.model.readable.rule.lines.elements.FiredRuleOperatorRuleLineElement;
import se.cambio.cds.gdl.model.readable.rule.lines.elements.FiredRuleReferenceRuleElement;
import se.cambio.cds.gdl.model.readable.rule.lines.elements.StaticTextRuleLineElement;
import se.cambio.cds.gdl.model.readable.rule.lines.interfaces.ConditionRuleLine;
import se.cambio.cds.util.misc.CDSLanguageManager;

public class FiredRuleConditionRuleLine extends ExpressionRuleLine implements ConditionRuleLine {

    private final FiredRuleReferenceRuleElement firedRuleReferenceRuleElement;
    private final FiredRuleOperatorRuleLineElement firedRuleOperatorRuleLineElement;

    public FiredRuleConditionRuleLine() {
        super(CDSLanguageManager.getMessage("FiredRuleCondition"),
                CDSLanguageManager.getMessage("FiredRuleConditionDesc"));
        firedRuleReferenceRuleElement = new FiredRuleReferenceRuleElement(this);
        firedRuleOperatorRuleLineElement = new FiredRuleOperatorRuleLineElement(this);
        getRuleLineElements().add(new StaticTextRuleLineElement(this, "Rule"));
        getRuleLineElements().add(firedRuleReferenceRuleElement);
        getRuleLineElements().add(firedRuleOperatorRuleLineElement);
    }

    public FiredRuleReferenceRuleElement getFiredRuleReferenceRuleElement() {
        return firedRuleReferenceRuleElement;
    }

    public FiredRuleOperatorRuleLineElement getFiredRuleOperatorRuleLineElement() {
        return firedRuleOperatorRuleLineElement;
    }

    @Override
    public ExpressionItem toExpressionItem() throws IllegalStateException {
        String gtCode = firedRuleReferenceRuleElement.getValue().getValue();
        return new UnaryExpression(new Variable(gtCode), firedRuleOperatorRuleLineElement.getValue());
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