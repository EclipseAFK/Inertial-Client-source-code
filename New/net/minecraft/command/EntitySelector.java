package net.minecraft.command;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

public class EntitySelector {
     private static final Pattern TOKEN_PATTERN = Pattern.compile("^@([pares])(?:\\[([^ ]*)\\])?$");
     private static final Splitter field_190828_b = Splitter.on(',').omitEmptyStrings();
     private static final Splitter field_190829_c = Splitter.on('=').limit(2);
     private static final Set field_190830_d = Sets.newHashSet();
     private static final String field_190831_e = func_190826_c("r");
     private static final String field_190832_f = func_190826_c("rm");
     private static final String field_190833_g = func_190826_c("l");
     private static final String field_190834_h = func_190826_c("lm");
     private static final String field_190835_i = func_190826_c("x");
     private static final String field_190836_j = func_190826_c("y");
     private static final String field_190837_k = func_190826_c("z");
     private static final String field_190838_l = func_190826_c("dx");
     private static final String field_190839_m = func_190826_c("dy");
     private static final String field_190840_n = func_190826_c("dz");
     private static final String field_190841_o = func_190826_c("rx");
     private static final String field_190842_p = func_190826_c("rxm");
     private static final String field_190843_q = func_190826_c("ry");
     private static final String field_190844_r = func_190826_c("rym");
     private static final String field_190845_s = func_190826_c("c");
     private static final String field_190846_t = func_190826_c("m");
     private static final String field_190847_u = func_190826_c("team");
     private static final String field_190848_v = func_190826_c("name");
     private static final String field_190849_w = func_190826_c("type");
     private static final String field_190850_x = func_190826_c("tag");
     private static final Predicate field_190851_y = new Predicate() {
          public boolean apply(@Nullable String p_apply_1_) {
               return p_apply_1_ != null && (EntitySelector.field_190830_d.contains(p_apply_1_) || p_apply_1_.length() > "score_".length() && p_apply_1_.startsWith("score_"));
          }
     };
     private static final Set WORLD_BINDING_ARGS;

     private static String func_190826_c(String p_190826_0_) {
          field_190830_d.add(p_190826_0_);
          return p_190826_0_;
     }

     @Nullable
     public static EntityPlayerMP matchOnePlayer(ICommandSender sender, String token) throws CommandException {
          return (EntityPlayerMP)matchOneEntity(sender, token, EntityPlayerMP.class);
     }

     public static List func_193531_b(ICommandSender p_193531_0_, String p_193531_1_) throws CommandException {
          return matchEntities(p_193531_0_, p_193531_1_, EntityPlayerMP.class);
     }

     @Nullable
     public static Entity matchOneEntity(ICommandSender sender, String token, Class targetClass) throws CommandException {
          List list = matchEntities(sender, token, targetClass);
          return list.size() == 1 ? (Entity)list.get(0) : null;
     }

     @Nullable
     public static ITextComponent matchEntitiesToTextComponent(ICommandSender sender, String token) throws CommandException {
          List list = matchEntities(sender, token, Entity.class);
          if (list.isEmpty()) {
               return null;
          } else {
               List list1 = Lists.newArrayList();
               Iterator var4 = list.iterator();

               while(var4.hasNext()) {
                    Entity entity = (Entity)var4.next();
                    list1.add(entity.getDisplayName());
               }

               return CommandBase.join(list1);
          }
     }

     public static List matchEntities(ICommandSender sender, String token, Class targetClass) throws CommandException {
          Matcher matcher = TOKEN_PATTERN.matcher(token);
          if (matcher.matches() && sender.canCommandSenderUseCommand(1, "@")) {
               Map map = getArgumentMap(matcher.group(2));
               if (!isEntityTypeValid(sender, map)) {
                    return Collections.emptyList();
               } else {
                    String s = matcher.group(1);
                    BlockPos blockpos = getBlockPosFromArguments(map, sender.getPosition());
                    Vec3d vec3d = getPosFromArguments(map, sender.getPositionVector());
                    List list = getWorlds(sender, map);
                    List list1 = Lists.newArrayList();
                    Iterator var10 = list.iterator();

                    while(var10.hasNext()) {
                         World world = (World)var10.next();
                         if (world != null) {
                              List list2 = Lists.newArrayList();
                              list2.addAll(getTypePredicates(map, s));
                              list2.addAll(getXpLevelPredicates(map));
                              list2.addAll(getGamemodePredicates(map));
                              list2.addAll(getTeamPredicates(map));
                              list2.addAll(getScorePredicates(sender, map));
                              list2.addAll(getNamePredicates(map));
                              list2.addAll(getTagPredicates(map));
                              list2.addAll(getRadiusPredicates(map, vec3d));
                              list2.addAll(getRotationsPredicates(map));
                              if ("s".equalsIgnoreCase(s)) {
                                   Entity entity = sender.getCommandSenderEntity();
                                   if (entity != null && targetClass.isAssignableFrom(entity.getClass())) {
                                        if (map.containsKey(field_190838_l) || map.containsKey(field_190839_m) || map.containsKey(field_190840_n)) {
                                             int i = getInt(map, field_190838_l, 0);
                                             int j = getInt(map, field_190839_m, 0);
                                             int k = getInt(map, field_190840_n, 0);
                                             AxisAlignedBB axisalignedbb = getAABB(blockpos, i, j, k);
                                             if (!axisalignedbb.intersectsWith(entity.getEntityBoundingBox())) {
                                                  return Collections.emptyList();
                                             }
                                        }

                                        Iterator var18 = list2.iterator();

                                        Predicate predicate;
                                        do {
                                             if (!var18.hasNext()) {
                                                  return Lists.newArrayList(new Entity[]{entity});
                                             }

                                             predicate = (Predicate)var18.next();
                                        } while(predicate.apply(entity));

                                        return Collections.emptyList();
                                   }

                                   return Collections.emptyList();
                              }

                              list1.addAll(filterResults(map, targetClass, list2, s, world, blockpos));
                         }
                    }

                    return getEntitiesFromPredicates(list1, map, sender, targetClass, s, vec3d);
               }
          } else {
               return Collections.emptyList();
          }
     }

     private static List getWorlds(ICommandSender sender, Map argumentMap) {
          List list = Lists.newArrayList();
          if (hasArgument(argumentMap)) {
               list.add(sender.getEntityWorld());
          } else {
               Collections.addAll(list, sender.getServer().worldServers);
          }

          return list;
     }

     private static boolean isEntityTypeValid(ICommandSender commandSender, Map params) {
          String s = getArgument(params, field_190849_w);
          if (s == null) {
               return true;
          } else {
               ResourceLocation resourcelocation = new ResourceLocation(s.startsWith("!") ? s.substring(1) : s);
               if (EntityList.isStringValidEntityName(resourcelocation)) {
                    return true;
               } else {
                    TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("commands.generic.entity.invalidType", new Object[]{resourcelocation});
                    textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
                    commandSender.addChatMessage(textcomponenttranslation);
                    return false;
               }
          }
     }

     private static List getTypePredicates(Map params, String type) {
          String s = getArgument(params, field_190849_w);
          if (s == null || !type.equals("e") && !type.equals("r") && !type.equals("s")) {
               return !type.equals("e") && !type.equals("s") ? Collections.singletonList(new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         return p_apply_1_ instanceof EntityPlayer;
                    }
               }) : Collections.emptyList();
          } else {
               final boolean flag = s.startsWith("!");
               final ResourceLocation resourcelocation = new ResourceLocation(flag ? s.substring(1) : s);
               return Collections.singletonList(new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         return EntityList.isStringEntityName(p_apply_1_, resourcelocation) != flag;
                    }
               });
          }
     }

     private static List getXpLevelPredicates(Map params) {
          List list = Lists.newArrayList();
          final int i = getInt(params, field_190834_h, -1);
          final int j = getInt(params, field_190833_g, -1);
          if (i > -1 || j > -1) {
               list.add(new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         if (!(p_apply_1_ instanceof EntityPlayerMP)) {
                              return false;
                         } else {
                              EntityPlayerMP entityplayermp = (EntityPlayerMP)p_apply_1_;
                              return (i <= -1 || entityplayermp.experienceLevel >= i) && (j <= -1 || entityplayermp.experienceLevel <= j);
                         }
                    }
               });
          }

          return list;
     }

     private static List getGamemodePredicates(Map params) {
          List list = Lists.newArrayList();
          String s = getArgument(params, field_190846_t);
          if (s == null) {
               return list;
          } else {
               final boolean flag = s.startsWith("!");
               if (flag) {
                    s = s.substring(1);
               }

               final GameType gametype;
               try {
                    int i = Integer.parseInt(s);
                    gametype = GameType.parseGameTypeWithDefault(i, GameType.NOT_SET);
               } catch (Throwable var6) {
                    gametype = GameType.parseGameTypeWithDefault(s, GameType.NOT_SET);
               }

               list.add(new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         if (!(p_apply_1_ instanceof EntityPlayerMP)) {
                              return false;
                         } else {
                              EntityPlayerMP entityplayermp = (EntityPlayerMP)p_apply_1_;
                              GameType gametype1 = entityplayermp.interactionManager.getGameType();
                              return flag ? gametype1 != gametype : gametype1 == gametype;
                         }
                    }
               });
               return list;
          }
     }

     private static List getTeamPredicates(Map params) {
          List list = Lists.newArrayList();
          final String s = getArgument(params, field_190847_u);
          final boolean flag = s != null && s.startsWith("!");
          if (flag) {
               s = s.substring(1);
          }

          if (s != null) {
               list.add(new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         if (!(p_apply_1_ instanceof EntityLivingBase)) {
                              return false;
                         } else {
                              EntityLivingBase entitylivingbase = (EntityLivingBase)p_apply_1_;
                              Team team = entitylivingbase.getTeam();
                              String s1 = team == null ? "" : team.getRegisteredName();
                              return s1.equals(s) != flag;
                         }
                    }
               });
          }

          return list;
     }

     private static List getScorePredicates(final ICommandSender sender, Map params) {
          final Map map = getScoreMap(params);
          return (List)(map.isEmpty() ? Collections.emptyList() : Lists.newArrayList(new Predicate[]{new Predicate() {
               public boolean apply(@Nullable Entity p_apply_1_) {
                    if (p_apply_1_ == null) {
                         return false;
                    } else {
                         Scoreboard scoreboard = sender.getServer().worldServerForDimension(0).getScoreboard();
                         Iterator var3 = map.entrySet().iterator();

                         Entry entry;
                         boolean flag;
                         int i;
                         do {
                              if (!var3.hasNext()) {
                                   return true;
                              }

                              entry = (Entry)var3.next();
                              String s = (String)entry.getKey();
                              flag = false;
                              if (s.endsWith("_min") && s.length() > 4) {
                                   flag = true;
                                   s = s.substring(0, s.length() - 4);
                              }

                              ScoreObjective scoreobjective = scoreboard.getObjective(s);
                              if (scoreobjective == null) {
                                   return false;
                              }

                              String s1 = p_apply_1_ instanceof EntityPlayerMP ? p_apply_1_.getName() : p_apply_1_.getCachedUniqueIdString();
                              if (!scoreboard.entityHasObjective(s1, scoreobjective)) {
                                   return false;
                              }

                              Score score = scoreboard.getOrCreateScore(s1, scoreobjective);
                              i = score.getScorePoints();
                              if (i < (Integer)entry.getValue() && flag) {
                                   return false;
                              }
                         } while(i <= (Integer)entry.getValue() || flag);

                         return false;
                    }
               }
          }}));
     }

     private static List getNamePredicates(Map params) {
          List list = Lists.newArrayList();
          final String s = getArgument(params, field_190848_v);
          final boolean flag = s != null && s.startsWith("!");
          if (flag) {
               s = s.substring(1);
          }

          if (s != null) {
               list.add(new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         return p_apply_1_ != null && p_apply_1_.getName().equals(s) != flag;
                    }
               });
          }

          return list;
     }

     private static List getTagPredicates(Map params) {
          List list = Lists.newArrayList();
          final String s = getArgument(params, field_190850_x);
          final boolean flag = s != null && s.startsWith("!");
          if (flag) {
               s = s.substring(1);
          }

          if (s != null) {
               list.add(new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         if (p_apply_1_ == null) {
                              return false;
                         } else if ("".equals(s)) {
                              return p_apply_1_.getTags().isEmpty() != flag;
                         } else {
                              return p_apply_1_.getTags().contains(s) != flag;
                         }
                    }
               });
          }

          return list;
     }

     private static List getRadiusPredicates(Map params, final Vec3d pos) {
          double d0 = (double)getInt(params, field_190832_f, -1);
          double d1 = (double)getInt(params, field_190831_e, -1);
          final boolean flag = d0 < -0.5D;
          final boolean flag1 = d1 < -0.5D;
          if (flag && flag1) {
               return Collections.emptyList();
          } else {
               double d2 = Math.max(d0, 1.0E-4D);
               final double d3 = d2 * d2;
               double d4 = Math.max(d1, 1.0E-4D);
               final double d5 = d4 * d4;
               return Lists.newArrayList(new Predicate[]{new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         if (p_apply_1_ == null) {
                              return false;
                         } else {
                              double d6 = pos.squareDistanceTo(p_apply_1_.posX, p_apply_1_.posY, p_apply_1_.posZ);
                              return (flag || d6 >= d3) && (flag1 || d6 <= d5);
                         }
                    }
               }});
          }
     }

     private static List getRotationsPredicates(Map params) {
          List list = Lists.newArrayList();
          final int k;
          final int l;
          if (params.containsKey(field_190844_r) || params.containsKey(field_190843_q)) {
               k = MathHelper.clampAngle(getInt(params, field_190844_r, 0));
               l = MathHelper.clampAngle(getInt(params, field_190843_q, 359));
               list.add(new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         if (p_apply_1_ == null) {
                              return false;
                         } else {
                              int i1 = MathHelper.clampAngle(MathHelper.floor(p_apply_1_.rotationYaw));
                              if (k > l) {
                                   return i1 >= k || i1 <= l;
                              } else {
                                   return i1 >= k && i1 <= l;
                              }
                         }
                    }
               });
          }

          if (params.containsKey(field_190842_p) || params.containsKey(field_190841_o)) {
               k = MathHelper.clampAngle(getInt(params, field_190842_p, 0));
               l = MathHelper.clampAngle(getInt(params, field_190841_o, 359));
               list.add(new Predicate() {
                    public boolean apply(@Nullable Entity p_apply_1_) {
                         if (p_apply_1_ == null) {
                              return false;
                         } else {
                              int i1 = MathHelper.clampAngle(MathHelper.floor(p_apply_1_.rotationPitch));
                              if (k > l) {
                                   return i1 >= k || i1 <= l;
                              } else {
                                   return i1 >= k && i1 <= l;
                              }
                         }
                    }
               });
          }

          return list;
     }

     private static List filterResults(Map params, Class entityClass, List inputList, String type, World worldIn, BlockPos position) {
          List list = Lists.newArrayList();
          String s = getArgument(params, field_190849_w);
          s = s != null && s.startsWith("!") ? s.substring(1) : s;
          boolean flag = !type.equals("e");
          boolean flag1 = type.equals("r") && s != null;
          int i = getInt(params, field_190838_l, 0);
          int j = getInt(params, field_190839_m, 0);
          int k = getInt(params, field_190840_n, 0);
          int l = getInt(params, field_190831_e, -1);
          Predicate predicate = Predicates.and(inputList);
          Predicate predicate1 = Predicates.and(EntitySelectors.IS_ALIVE, predicate);
          final AxisAlignedBB axisalignedbb1;
          if (!params.containsKey(field_190838_l) && !params.containsKey(field_190839_m) && !params.containsKey(field_190840_n)) {
               if (l >= 0) {
                    axisalignedbb1 = new AxisAlignedBB((double)(position.getX() - l), (double)(position.getY() - l), (double)(position.getZ() - l), (double)(position.getX() + l + 1), (double)(position.getY() + l + 1), (double)(position.getZ() + l + 1));
                    if (flag && !flag1) {
                         list.addAll(worldIn.getPlayers(entityClass, predicate1));
                    } else {
                         list.addAll(worldIn.getEntitiesWithinAABB(entityClass, axisalignedbb1, predicate1));
                    }
               } else if (type.equals("a")) {
                    list.addAll(worldIn.getPlayers(entityClass, predicate));
               } else if (type.equals("p") || type.equals("r") && !flag1) {
                    list.addAll(worldIn.getPlayers(entityClass, predicate1));
               } else {
                    list.addAll(worldIn.getEntities(entityClass, predicate1));
               }
          } else {
               axisalignedbb1 = getAABB(position, i, j, k);
               if (flag && !flag1) {
                    Predicate predicate2 = new Predicate() {
                         public boolean apply(@Nullable Entity p_apply_1_) {
                              return p_apply_1_ != null && axisalignedbb1.intersectsWith(p_apply_1_.getEntityBoundingBox());
                         }
                    };
                    list.addAll(worldIn.getPlayers(entityClass, Predicates.and(predicate1, predicate2)));
               } else {
                    list.addAll(worldIn.getEntitiesWithinAABB(entityClass, axisalignedbb1, predicate1));
               }
          }

          return list;
     }

     private static List getEntitiesFromPredicates(List matchingEntities, Map params, ICommandSender sender, Class targetClass, String type, final Vec3d pos) {
          int i = getInt(params, field_190845_s, !type.equals("a") && !type.equals("e") ? 1 : 0);
          if (!type.equals("p") && !type.equals("a") && !type.equals("e")) {
               if (type.equals("r")) {
                    Collections.shuffle((List)matchingEntities);
               }
          } else {
               Collections.sort((List)matchingEntities, new Comparator() {
                    public int compare(Entity p_compare_1_, Entity p_compare_2_) {
                         return ComparisonChain.start().compare(p_compare_1_.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord), p_compare_2_.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord)).result();
                    }
               });
          }

          Entity entity = sender.getCommandSenderEntity();
          if (entity != null && targetClass.isAssignableFrom(entity.getClass()) && i == 1 && ((List)matchingEntities).contains(entity) && !"r".equals(type)) {
               matchingEntities = Lists.newArrayList(new Entity[]{entity});
          }

          if (i != 0) {
               if (i < 0) {
                    Collections.reverse((List)matchingEntities);
               }

               matchingEntities = ((List)matchingEntities).subList(0, Math.min(Math.abs(i), ((List)matchingEntities).size()));
          }

          return (List)matchingEntities;
     }

     private static AxisAlignedBB getAABB(BlockPos pos, int x, int y, int z) {
          boolean flag = x < 0;
          boolean flag1 = y < 0;
          boolean flag2 = z < 0;
          int i = pos.getX() + (flag ? x : 0);
          int j = pos.getY() + (flag1 ? y : 0);
          int k = pos.getZ() + (flag2 ? z : 0);
          int l = pos.getX() + (flag ? 0 : x) + 1;
          int i1 = pos.getY() + (flag1 ? 0 : y) + 1;
          int j1 = pos.getZ() + (flag2 ? 0 : z) + 1;
          return new AxisAlignedBB((double)i, (double)j, (double)k, (double)l, (double)i1, (double)j1);
     }

     private static BlockPos getBlockPosFromArguments(Map params, BlockPos pos) {
          return new BlockPos(getInt(params, field_190835_i, pos.getX()), getInt(params, field_190836_j, pos.getY()), getInt(params, field_190837_k, pos.getZ()));
     }

     private static Vec3d getPosFromArguments(Map params, Vec3d pos) {
          return new Vec3d(getCoordinate(params, field_190835_i, pos.xCoord, true), getCoordinate(params, field_190836_j, pos.yCoord, false), getCoordinate(params, field_190837_k, pos.zCoord, true));
     }

     private static double getCoordinate(Map params, String key, double defaultD, boolean offset) {
          return params.containsKey(key) ? (double)MathHelper.getInt((String)params.get(key), MathHelper.floor(defaultD)) + (offset ? 0.5D : 0.0D) : defaultD;
     }

     private static boolean hasArgument(Map params) {
          Iterator var1 = WORLD_BINDING_ARGS.iterator();

          String s;
          do {
               if (!var1.hasNext()) {
                    return false;
               }

               s = (String)var1.next();
          } while(!params.containsKey(s));

          return true;
     }

     private static int getInt(Map params, String key, int defaultI) {
          return params.containsKey(key) ? MathHelper.getInt((String)params.get(key), defaultI) : defaultI;
     }

     @Nullable
     private static String getArgument(Map params, String key) {
          return (String)params.get(key);
     }

     public static Map getScoreMap(Map params) {
          Map map = Maps.newHashMap();
          Iterator var2 = params.keySet().iterator();

          while(var2.hasNext()) {
               String s = (String)var2.next();
               if (s.startsWith("score_") && s.length() > "score_".length()) {
                    map.put(s.substring("score_".length()), MathHelper.getInt((String)params.get(s), 1));
               }
          }

          return map;
     }

     public static boolean matchesMultiplePlayers(String selectorStr) throws CommandException {
          Matcher matcher = TOKEN_PATTERN.matcher(selectorStr);
          if (!matcher.matches()) {
               return false;
          } else {
               Map map = getArgumentMap(matcher.group(2));
               String s = matcher.group(1);
               int i = !"a".equals(s) && !"e".equals(s) ? 1 : 0;
               return getInt(map, field_190845_s, i) != 1;
          }
     }

     public static boolean hasArguments(String selectorStr) {
          return TOKEN_PATTERN.matcher(selectorStr).matches();
     }

     private static Map getArgumentMap(@Nullable String argumentString) throws CommandException {
          Map map = Maps.newHashMap();
          if (argumentString == null) {
               return map;
          } else {
               Iterator var2 = field_190828_b.split(argumentString).iterator();

               while(var2.hasNext()) {
                    String s = (String)var2.next();
                    Iterator iterator = field_190829_c.split(s).iterator();
                    String s1 = (String)iterator.next();
                    if (!field_190851_y.apply(s1)) {
                         throw new CommandException("commands.generic.selector_argument", new Object[]{s});
                    }

                    map.put(s1, iterator.hasNext() ? (String)iterator.next() : "");
               }

               return map;
          }
     }

     static {
          WORLD_BINDING_ARGS = Sets.newHashSet(new String[]{field_190835_i, field_190836_j, field_190837_k, field_190838_l, field_190839_m, field_190840_n, field_190832_f, field_190831_e});
     }
}
