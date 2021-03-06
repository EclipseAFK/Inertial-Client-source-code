package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import optifine.Config;
import optifine.Reflector;
import shadersmod.client.SVertexBuilder;

public class WorldVertexBufferUploader {
     public void draw(BufferBuilder vertexBufferIn) {
          if (vertexBufferIn.getVertexCount() > 0) {
               VertexFormat vertexformat = vertexBufferIn.getVertexFormat();
               int i = vertexformat.getNextOffset();
               ByteBuffer bytebuffer = vertexBufferIn.getByteBuffer();
               List list = vertexformat.getElements();
               boolean flag = Reflector.ForgeVertexFormatElementEnumUseage_preDraw.exists();
               boolean flag1 = Reflector.ForgeVertexFormatElementEnumUseage_postDraw.exists();

               int j1;
               int i1;
               for(j1 = 0; j1 < list.size(); ++j1) {
                    VertexFormatElement vertexformatelement = (VertexFormatElement)list.get(j1);
                    VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                    if (flag) {
                         Reflector.callVoid(vertexformatelement$enumusage, Reflector.ForgeVertexFormatElementEnumUseage_preDraw, new Object[]{vertexformat, j1, i, bytebuffer});
                    } else {
                         int k = vertexformatelement.getType().getGlConstant();
                         i1 = vertexformatelement.getIndex();
                         bytebuffer.position(vertexformat.getOffset(j1));
                         switch(vertexformatelement$enumusage) {
                         case POSITION:
                              GlStateManager.glVertexPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
                              GlStateManager.glEnableClientState(32884);
                              break;
                         case UV:
                              OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i1);
                              GlStateManager.glTexCoordPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
                              GlStateManager.glEnableClientState(32888);
                              OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                              break;
                         case COLOR:
                              GlStateManager.glColorPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
                              GlStateManager.glEnableClientState(32886);
                              break;
                         case NORMAL:
                              GlStateManager.glNormalPointer(k, i, bytebuffer);
                              GlStateManager.glEnableClientState(32885);
                         }
                    }
               }

               if (vertexBufferIn.isMultiTexture()) {
                    vertexBufferIn.drawMultiTexture();
               } else if (Config.isShaders()) {
                    SVertexBuilder.drawArrays(vertexBufferIn.getDrawMode(), 0, vertexBufferIn.getVertexCount(), vertexBufferIn);
               } else {
                    GlStateManager.glDrawArrays(vertexBufferIn.getDrawMode(), 0, vertexBufferIn.getVertexCount());
               }

               j1 = 0;

               for(int k1 = list.size(); j1 < k1; ++j1) {
                    VertexFormatElement vertexformatelement1 = (VertexFormatElement)list.get(j1);
                    VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
                    if (flag1) {
                         Reflector.callVoid(vertexformatelement$enumusage1, Reflector.ForgeVertexFormatElementEnumUseage_postDraw, new Object[]{vertexformat, j1, i, bytebuffer});
                    } else {
                         i1 = vertexformatelement1.getIndex();
                         switch(vertexformatelement$enumusage1) {
                         case POSITION:
                              GlStateManager.glDisableClientState(32884);
                              break;
                         case UV:
                              OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i1);
                              GlStateManager.glDisableClientState(32888);
                              OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                              break;
                         case COLOR:
                              GlStateManager.glDisableClientState(32886);
                              GlStateManager.resetColor();
                              break;
                         case NORMAL:
                              GlStateManager.glDisableClientState(32885);
                         }
                    }
               }
          }

          vertexBufferIn.reset();
     }
}
