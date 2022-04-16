package net.minecraft.client.tutorial;

import java.util.function.Function;

public enum TutorialSteps {
     MOVEMENT("movement", MovementStep::new),
     FIND_TREE("find_tree", FindTreeStep::new),
     PUNCH_TREE("punch_tree", PunchTreeStep::new),
     OPEN_INVENTORY("open_inventory", OpenInventoryStep::new),
     CRAFT_PLANKS("craft_planks", CraftPlanksStep::new),
     NONE("none", CompletedTutorialStep::new);

     private final String field_193316_g;
     private final Function field_193317_h;

     private TutorialSteps(String p_i47577_3_, Function p_i47577_4_) {
          this.field_193316_g = p_i47577_3_;
          this.field_193317_h = p_i47577_4_;
     }

     public ITutorialStep func_193309_a(Tutorial p_193309_1_) {
          return (ITutorialStep)this.field_193317_h.apply(p_193309_1_);
     }

     public String func_193308_a() {
          return this.field_193316_g;
     }

     public static TutorialSteps func_193307_a(String p_193307_0_) {
          TutorialSteps[] var1 = values();
          int var2 = var1.length;

          for(int var3 = 0; var3 < var2; ++var3) {
               TutorialSteps tutorialsteps = var1[var3];
               if (tutorialsteps.field_193316_g.equals(p_193307_0_)) {
                    return tutorialsteps;
               }
          }

          return NONE;
     }
}
