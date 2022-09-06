package hu.xannosz.flyingships.networking;

import hu.xannosz.flyingships.blockentity.RudderBlockEntity;
import hu.xannosz.flyingships.screen.widget.ButtonId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ButtonClickedPacket {

	private final ButtonId buttonId;
	private final BlockPos position;

	public ButtonClickedPacket(ButtonId buttonId, BlockPos position) {
		this.buttonId = buttonId;
		this.position = position;
	}

	public ButtonClickedPacket(FriendlyByteBuf buf) {
		buttonId = buf.readEnum(ButtonId.class);
		position = buf.readBlockPos();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeEnum(buttonId);
		buf.writeBlockPos(position);
	}

	public void handler(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			// SERVER SITE
			BlockEntity entity = Objects.requireNonNull(context.getSender()).getLevel().getBlockEntity(position);
			if (entity instanceof RudderBlockEntity) {
				((RudderBlockEntity) entity).executeButtonClick(buttonId);
			}
		});
	}
}
