package se.cambio.cds.gdl.model.readable.util;

import org.slf4j.LoggerFactory;
import se.cambio.cds.gdl.model.readable.rule.lines.ArchetypeInstantiationRuleLine;
import se.cambio.cds.gdl.model.readable.rule.lines.RuleLine;
import se.cambio.cds.gdl.model.readable.rule.lines.interfaces.PredicateRuleLine;
import se.cambio.cds.model.instance.ArchetypeReference;
import se.cambio.openehr.controller.session.data.Archetypes;
import se.cambio.openehr.util.OpenEHRConstUI;
import se.cambio.openehr.util.OpenEHRImageUtil;
import se.cambio.openehr.util.OpenEHRLanguageManager;
import se.cambio.openehr.util.exceptions.InstanceNotFoundException;
import se.cambio.openehr.util.exceptions.InternalErrorException;

public class ReadableArchetypeReferencesUtil {

    private static short MAX_CHAR_PREDICATE_DESC_SIZE = 50;

    public static String getName(ArchetypeInstantiationRuleLine airl) throws InstanceNotFoundException, InternalErrorException {
        return getName(airl, true);
    }
    public static String getName(ArchetypeInstantiationRuleLine airl, boolean withPredicate) {
        if (airl!=null){
            ArchetypeReference ar = airl.getArchetypeReference();
            if (ar!=null){
                String name = ar.getIdArchetype();
                if (withPredicate){
                    String predicateDesc = getShortPredicateDescription(airl);
                    if (!predicateDesc.isEmpty()){
                        name = name+" ("+predicateDesc+")";
                    }
                }
                return name;
            }
        }
        LoggerFactory.getLogger(ArchetypeReference.class).warn("Unknown name for AR '"+airl+"'");
        return "*UNKNOWN*";
    }

    private static String getShortPredicateDescription(ArchetypeInstantiationRuleLine airl){
        String predicateDesc = getPredicateDescription(airl);
        if (predicateDesc.length()>MAX_CHAR_PREDICATE_DESC_SIZE){
            predicateDesc = predicateDesc.substring(0, MAX_CHAR_PREDICATE_DESC_SIZE)+"...";
        }
        return predicateDesc;
    }

    private static String getPredicateDescription(ArchetypeInstantiationRuleLine airl){
        StringBuffer sb = new StringBuffer();
        String prefix = "";
        for (RuleLine ruleLine : airl.getChildrenRuleLines().getRuleLines()) {
            if (ruleLine instanceof PredicateRuleLine) {
                sb.append(prefix);
                sb.append(((PredicateRuleLine)ruleLine).getPredicateDescription());
                prefix = ", ";
            }
        }
        return sb.toString();
    }

    public static String getDescription(ArchetypeInstantiationRuleLine airl) {
        if (airl!=null){
            ArchetypeReference ar = airl.getArchetypeReference();
            if (ar!=null){
                return ar.getIdArchetype();
            }
        }
        return "*UNKNOWN*";
    }

    public static String getHTMLPredicate(ArchetypeInstantiationRuleLine airl){
        String predicateDesc = getPredicateDescription(airl);
        return (predicateDesc.isEmpty()?"":"<tr><td colspan=2><b>"+OpenEHRLanguageManager.getMessage("Predicate")+": </b>"+predicateDesc+"</td></tr>");
    }

    public static String getHTMLTooltip(ArchetypeInstantiationRuleLine airl) {
        ArchetypeReference ar = airl.getArchetypeReference();
        if (ar!=null){
            String archetypeImageName = OpenEHRConstUI.getIconName(Archetypes.getEntryType(ar.getIdArchetype()));
            String archetypeName = getName(airl, false);
            return "<html><table width=500>"+
                    "<tr><td><b>"+OpenEHRLanguageManager.getMessage("Archetype")+": </b>"+OpenEHRImageUtil.getImgHTMLTag(archetypeImageName)+"&nbsp;"+archetypeName+"</td></tr>"+
                    "<tr><td><b>"+OpenEHRLanguageManager.getMessage("Description")+": </b>"+getDescription(airl)+"</td></tr>"+
                    getHTMLPredicate(airl)+
                    "</table></html>";
        }else{
            return "*UNKNOWN*";
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