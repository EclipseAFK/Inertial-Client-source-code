package net.minecraft.util.text;

import java.util.Iterator;

public class TextComponentSelector extends TextComponentBase {
     private final String selector;

     public TextComponentSelector(String selectorIn) {
          this.selector = selectorIn;
     }

     public String getSelector() {
          return this.selector;
     }

     public String getUnformattedComponentText() {
          return this.selector;
     }

     public TextComponentSelector createCopy() {
          TextComponentSelector textcomponentselector = new TextComponentSelector(this.selector);
          textcomponentselector.setStyle(this.getStyle().createShallowCopy());
          Iterator var2 = this.getSiblings().iterator();

          while(var2.hasNext()) {
               ITextComponent itextcomponent = (ITextComponent)var2.next();
               textcomponentselector.appendSibling(itextcomponent.createCopy());
          }

          return textcomponentselector;
     }

     public boolean equals(Object p_equals_1_) {
          if (this == p_equals_1_) {
               return true;
          } else if (!(p_equals_1_ instanceof TextComponentSelector)) {
               return false;
          } else {
               TextComponentSelector textcomponentselector = (TextComponentSelector)p_equals_1_;
               return this.selector.equals(textcomponentselector.selector) && super.equals(p_equals_1_);
          }
     }

     public String toString() {
          return "SelectorComponent{pattern='" + this.selector + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
     }
}
