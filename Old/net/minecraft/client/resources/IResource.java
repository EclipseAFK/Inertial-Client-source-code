package net.minecraft.client.resources;

import java.io.Closeable;
import java.io.InputStream;
import javax.annotation.Nullable;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

public interface IResource extends Closeable {
      ResourceLocation getResourceLocation();

      InputStream getInputStream();

      boolean hasMetadata();

      @Nullable
      IMetadataSection getMetadata(String var1);

      String getResourcePackName();
}
