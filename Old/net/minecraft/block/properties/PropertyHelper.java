package net.minecraft.block.properties;

import com.google.common.base.MoreObjects;

public abstract class PropertyHelper implements IProperty {
      private final Class valueClass;
      private final String name;

      protected PropertyHelper(String name, Class valueClass) {
            this.valueClass = valueClass;
            this.name = name;
      }

      public String getName() {
            return this.name;
      }

      public Class getValueClass() {
            return this.valueClass;
      }

      public String toString() {
            return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.valueClass).add("values", this.getAllowedValues()).toString();
      }

      public boolean equals(Object p_equals_1_) {
            if (this == p_equals_1_) {
                  return true;
            } else if (!(p_equals_1_ instanceof PropertyHelper)) {
                  return false;
            } else {
                  PropertyHelper propertyhelper = (PropertyHelper)p_equals_1_;
                  return this.valueClass.equals(propertyhelper.valueClass) && this.name.equals(propertyhelper.name);
            }
      }

      public int hashCode() {
            return 31 * this.valueClass.hashCode() + this.name.hashCode();
      }
}
