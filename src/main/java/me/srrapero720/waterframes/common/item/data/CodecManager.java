package me.srrapero720.waterframes.common.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class CodecManager {

    // Basic codec
    public static final Codec<RemoteData> REMOTE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("dimension").forGetter(RemoteData::dimension),
                    Codec.INT.fieldOf("x").forGetter(RemoteData::x),
                    Codec.INT.fieldOf("y").forGetter(RemoteData::y),
                    Codec.INT.fieldOf("z").forGetter(RemoteData::z)
            ).apply(instance, RemoteData::new)
    );
    public static final StreamCodec<ByteBuf, RemoteData> REMOTE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RemoteData::dimension,
            ByteBufCodecs.INT, RemoteData::x,
            ByteBufCodecs.INT, RemoteData::y,
            ByteBufCodecs.INT, RemoteData::z,
            RemoteData::new
    );


    // Unit stream codec if nothing should be sent across the network
    public static final StreamCodec<ByteBuf, RemoteData> UNIT_STREAM_CODEC = StreamCodec.unit(new RemoteData("", 0, 0, 0));
}