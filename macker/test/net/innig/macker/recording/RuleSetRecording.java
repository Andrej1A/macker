/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2003 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */
 
package net.innig.macker.recording;

import net.innig.macker.event.*;
import net.innig.macker.rule.Rule;
import net.innig.macker.rule.RuleSet;

import java.io.PrintWriter;
import java.util.*;

import org.jdom.Element;

import net.innig.collect.CollectionDiff;

public class RuleSetRecording
    extends EventRecording
    {
    public RuleSetRecording(EventRecording parent)
        {
        super(parent);
        children = new ArrayList();
        }
    
    public EventRecording record(MackerEvent event)
        {
        if(name == null)
            {
            RuleSet ruleSet = event.getRule().getParent();
            name = ruleSet.hasName() ? ruleSet.getName() : null;
            }
        
        EventRecording child;
        if(event instanceof ForEachStarted)
            child = new ForEachRecording(this);
        else if(event instanceof ForEachIterationFinished)
            return getParent().record(event);
        else
            child = new GenericRuleRecording(this);
        children.add(child);
        return child.record(event);
        }
    
    public void read(Element elem)
        {
        for(Iterator childIter = elem.getChildren().iterator(); childIter.hasNext(); )
            {
            Element childElem = (Element) childIter.next();
            EventRecording child;
            if(childElem.getName().equals("rule"))
                child = new GenericRuleRecording(this);
            else if(childElem.getName().equals("foreach"))
                child = new ForEachRecording(this);
            else
                throw new RuntimeException("Unknown element: " + childElem);
            child.read(childElem);
            children.add(child);
            }
        }
    
    public boolean compare(EventRecording actual, PrintWriter out)
        {
        if(!super.compare(actual, out))
            return false;
        
        RuleSetRecording actualRuleSet = (RuleSetRecording) actual;
        
        if(children.size() != actualRuleSet.children.size())
            {
            out.println(
                "expected " + children.size()
                + " rules generating events, but got " + actualRuleSet.children.size()
                + ": " + actualRuleSet.children);
            return false;
            }
        
        boolean match = true;
        Iterator
            expectedIter = children.iterator(),
            actualIter = actualRuleSet.children.iterator();
        while(expectedIter.hasNext())
            {
            EventRecording expectedChild = (EventRecording) expectedIter.next();
            EventRecording   actualChild = (EventRecording)   actualIter.next();
            match = expectedChild.compare(actualChild, out) && match;
            }
        return match;
        }
    
    public String toString()
        { return (name == null) ? "[ruleset]" : "[ruleset:" + name + "]"; }
    
    public void dump(PrintWriter out, int indent)
        {
        super.dump(out, indent);
        for(Iterator childIter = children.iterator(); childIter.hasNext(); )
            {
            EventRecording child = (EventRecording) childIter.next();
            child.dump(out, indent+3);
            }
        }
    
    private String name;
    private List/*<EventRecording>*/ children;
    }




